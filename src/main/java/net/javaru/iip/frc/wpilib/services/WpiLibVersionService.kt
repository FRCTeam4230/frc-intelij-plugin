/*
 * Copyright 2015-2021 the original author or authors.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.javaru.iip.frc.wpilib.services

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.startup.StartupManager
import com.intellij.psi.PsiElement
import com.intellij.util.DocumentUtil
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.i18n.FrcBundle.message
import net.javaru.iip.frc.i18n.FrcMessageKey
import net.javaru.iip.frc.notify.FrcNotificationType
import net.javaru.iip.frc.notify.FrcNotifications
import net.javaru.iip.frc.settings.FrcApplicationSettings
import net.javaru.iip.frc.util.asDate
import net.javaru.iip.frc.util.getGradleBuildPsiFile
import net.javaru.iip.frc.util.lastCheckedDateTimeFormatter
import net.javaru.iip.frc.util.reimportGradleProject
import net.javaru.iip.frc.wpilib.getAttachedWpiLibVersion
import net.javaru.iip.frc.wpilib.gradlePluginRepo.GradleRioMavenMetadataState
import net.javaru.iip.frc.wpilib.version.WpiLibVersion
import net.javaru.iip.frc.wpilib.version.WpiLibVersionImpl
import net.javaru.iip.frc.wpilib.version.filterToLatestForYear
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement
import org.jetbrains.plugins.groovy.lang.psi.GroovyRecursiveElementVisitor
import org.jetbrains.plugins.groovy.lang.psi.api.statements.blocks.GrClosableBlock
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrApplicationStatement
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrCommandArgumentList
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral
import org.jetbrains.plugins.groovy.lang.psi.impl.PsiImplUtil
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

const val gradleRioId = "edu.wpi.first.GradleRIO"

// GitConflictsToolWindowManager is a good example of using the StartupActivity
// AcceptedLanguageLevelsSettings sows a class tha is both a StartupActivity and an application Service

// NOTE: We CANNOT use StartupActivity.Background for this as per the docs, it "should not be used for any work that requires access to indices", which this does
class WpiLibVersionStartupActivity : StartupActivity
{
    override fun runActivity(project: Project)
    {
        if (project.isFrcFacetedProject())
        {
            StartupManager.getInstance(project).runWhenProjectIsInitialized() {
                val versionService = WpiLibVersionService.getInstance(project)
                if (FrcApplicationSettings.getInstance().checkWpiLibStatusOnProjectStartup)
                    versionService.checkWpiLibStatusAndAlertIfNeeded()
                else
                    versionService.scheduleStatusCheck()
            }
        }
    }
}

@State(name = "WpiLibVersionService", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class WpiLibVersionService private constructor(private val project: Project) : PersistentStateComponent<WpiLibVersionServiceState>,
                                                                               Disposable
{
    private val LOG = Logger.getInstance(WpiLibVersionService::class.java)

    private var myState: WpiLibVersionServiceState = WpiLibVersionServiceState()

    private val timer = Timer("WpiLibVersionService Check for Update Timer")
    private var timerTask: TimerTask? = null


    companion object
    {
        @JvmStatic
        fun getInstance(project: Project) = project.service<WpiLibVersionService>()
    }

    /**
     * Checks if there is a newer version of the WPI Lib available as compared to the one configured for the project, notifying the user
     * is an update is available.
     *
     * @param notifyIfNoUpdateAvailable whether to notify the user if an update is NOT available. Primarily meant for when the user
     *                                  initiates the check via an action. Default is false
     * @param maxTimeSinceLastCheck the maximum time since the last check for an update to allow. If the time since the last
     *                              check is less than this value, no check is performed. The default is 10 seconds, primarily
     *                              to prevent any accidental double checks during project startup or such.
     *                              Use `Duration.ofDays()`, `Duration.ofMinutes()`, etc. to create a value.
     */
    @JvmOverloads
    fun checkWpiLibStatusAndAlertIfNeeded(
        notifyIfNoUpdateAvailable: Boolean = false,
        maxTimeSinceLastCheck: Duration = Duration.ofSeconds(10)
                                         )
    {
        LOG.debug("[FRC] Preparing to check WPI Lib for update. notifyIfNoUpdateAvailable = $notifyIfNoUpdateAvailable  maxTimeSinceLastCheck = $maxTimeSinceLastCheck")
        val durationSinceLastCheck = myState.durationSinceLastCheck
        if (durationSinceLastCheck < maxTimeSinceLastCheck)
        {
            // TODO: Do we need to call scheduleStatusCheck here?
            LOG.debug("[FRC] time since last check of $durationSinceLastCheck is less than maxTimeSinceLastCheck or $maxTimeSinceLastCheck. No update check will be performed.")
            return
        }

        val versionStatus = getWpiLibVersionStatus()
        var updateAvailableNotification: Notification? = null

        if (versionStatus != null)
        {
            if (versionStatus.updateAvailableForAttachedYear())
            {
                LOG.debug("[FRC] notifying WPI Lib update is available. WpiLibVersionStatus: $versionStatus")
                updateAvailableNotification = notifyNewerWpiLibVersionIsAvailable(versionStatus)
            }
            else if (notifyIfNoUpdateAvailable)
            {
                LOG.debug("[FRC] notifying WPILib update NOT available. WpiLibVersionStatus: $versionStatus")
                notifyNoUpdateAvailable(versionStatus.attachedVersion.frcYear)
            }
        }
        else if (notifyIfNoUpdateAvailable)
        {
            LOG.debug("[FRC] notifying WPILib update NOT available due to null WpiLibVersionStatus")
            notifyUnableToCheckVersionStatus()
        }
        else
        {
            LOG.debug("[FRC] WpiLibVersionStatus was null, indicating it couold not be determined.")
        }
        // If we've notified an update is available, we do no want to schedule the next check until the user acknowledges the previous check
        if (updateAvailableNotification != null) updateAvailableNotification.whenExpired(::scheduleStatusCheck) else scheduleStatusCheck()
    }


    fun scheduleStatusCheck()
    {
        // NOTE: we don't use timer.scheduleAtFixedRate() because we do not want the next one to be scheduled 
        //       until the user acknowledges the notification of the last one to prevent multiple notifications

        // Cancel any current timer tasks
        cancelTimer()
        val applicationSettings = FrcApplicationSettings.getInstance()
        if (applicationSettings.checkWpiLibStatusPeriodically)
        {
            val last = myState.lastCheckedDateTime
            var next = last.plus(applicationSettings.checkWpiLibStatusInterval)
            val now = LocalDateTime.now()
            if (next.isBefore(now.plus(Duration.ofMinutes(15)))) // we have a small buffer to prevent strange behavior
            {
                next = now.plus(applicationSettings.checkWpiLibStatusInterval)
            }
            timerTask = CheckStatusTimerTask(project)
            timer.schedule(timerTask, next.asDate())
            LOG.info("[FRC] next check for WPI Lib update scheduled for $next")
        }
    }

    private fun notifyNewerWpiLibVersionIsAvailable(versionStatus: WpiLibVersionStatus): Notification?
    {
        if (versionStatus.updateAvailableForAttachedYear())
        {
            val availVerString = versionStatus.latestAvailableForSameYear.versionString
            val currVerString = versionStatus.attachedVersion.versionString

            val subtitle = message("frc.notification.wpiLibVersionStatus.updateAvailable.subtitle", availVerString)
            val content = message("frc.notification.wpiLibVersionStatus.updateAvailable.content", availVerString, currVerString)

            val applyAction = object : NotificationAction(message("frc.notification.wpiLibVersionStatus.updateAvailable.upgrade.action.text")) {
                override fun actionPerformed(e: AnActionEvent, notification: Notification)
                {
                    notification.expire()
                    updateWpiLibVersionInGradleBuild(versionStatus.latestAvailableForSameYear)
                }

            }
            val ignoreAction = object : NotificationAction(message("frc.notification.wpiLibVersionStatus.updateAvailable.ignore.action.text")) {
                override fun actionPerformed(e: AnActionEvent, notification: Notification)
                {
                    notification.expire()
                }
            }

            // TODO We need a doNotAskAgain action - but that adds some work in that we have to track

            return FrcNotifications.notifyWithActions(
                FrcNotificationType.ACTIONABLE_INFO,
                content, subtitle,
                project,
                applyAction, ignoreAction
                                          )
        }
        else
        {
            return null
        }

    }

    private fun notifyUnableToCheckVersionStatus()
    {
        val content = message("frc.notification.wpiLibVersionStatus.unableToCheck.content")
        FrcNotifications.notify(FrcNotificationType.ACTIONABLE_INFO, content, project = project)
    }

    private fun notifyNoUpdateAvailable(year: Int)
    {
        // we call toString on the year otherwise the resource bundle formats it with a comma: 2,019
        val content = message("frc.notification.wpiLibVersionStatus.haveTheLatest.content", year.toString())
        FrcNotifications.notify(FrcNotificationType.GENERAL_INFO, content, project = project)
    }

    /**
     * Function to update the WPI Lib plugin version in the Gradle build file.
     *
     * @param version the new wpilib version.
     */
    fun updateWpiLibVersionInGradleBuild(version: WpiLibVersion)
    {

        // Keep an eye on:  com.intellij.externalSystem.DependencyModifierService
        // It's experimental, but allows you to modify the build model such as adding a dependency
        // It does not (yet) support modifying a Plugin version. But JetBrains seems to indicate
        // that that is possibly planned:
        // https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010674120-Programatically-Update-Plugin-Version-in-Gradle-Build-File

        DocumentUtil.writeInRunUndoTransparentAction {
            val psiFile = project.getGradleBuildPsiFile()
            if (psiFile == null)
            {
                LOG.warn("[FRC] could not find Gradle Build File to update the WPI Lib / GradleRIO plugin")
                FrcNotifications.notify(
                    FrcNotificationType.ACTIONABLE_WARN,
                    FrcMessageKey.of("frc.notification.wpiLibVersionStatus.gradle.not.able.to.update", version.versionString)
                                       )
            }
            else
            {
                if (psiFile is GroovyFile)
                {
                    /*

                        plugins {
                            id "java"
                            id "edu.wpi.first.GradleRIO" version "2021.2.1"
                        }

                        For now we are not going to support legacy syntax... there are so many variations given that variables can be used and then can be
                        in turn be used via string concatenation or string replacement/placeholders
                        Legacy syntax:
                                buildscript {
                                  repositories {
                                    maven {
                                      url "https://plugins.gradle.org/m2/"
                                    }
                                  }
                                  dependencies {
                                    classpath "edu.wpi.first:GradleRIO:2020.3.2"
                                  }
                                }

                                apply plugin: "edu.wpi.first.GradleRIO"
                     */

                    // Examples of working with a Gradle PSI file:
                    //     ConvertGStringToStringIntention.processIntention()
                    //     GradleMiscContributor.process()

                    psiFile.accept(
                        object : GroovyRecursiveElementVisitor()
                        {
                            override fun visitElement(element: GroovyPsiElement)
                            {
                                super.visitElement(element)
                                if (element is GrReferenceExpression && element.qualifiedReferenceName == "plugins")
                                {
                                    // We have the plugins block... now let's find the GradleRIO declaration
                                    var block: PsiElement? = element.nextSibling
                                    while (block != null && block !is GrClosableBlock)
                                    {
                                        block = block.nextSibling
                                    }

                                    if (block is GrClosableBlock)
                                    {
                                        val children = block.children ?: emptyArray()
                                        // children will include things like PsiWhiteSpace, PsiElement(new line), GrParameterList (likely empty)
                                        // We want the "call expressions" which are the plugin declarations

                                        val gradleRioCallExpression = children
                                            .filterIsInstance<GrApplicationStatement>()
                                            .firstOrNull { it.text.contains(gradleRioId) }

                                        if (gradleRioCallExpression == null)
                                        {
                                            LOG.warn("[FRC] Could not find GradleRIO plugin Call Expression in order to update the WPI Lib / GradleRIO plugin")
                                            FrcNotifications.notify(
                                                FrcNotificationType.ACTIONABLE_WARN,
                                                FrcMessageKey.of("frc.notification.wpiLibVersionStatus.gradle.not.able.to.update", version.versionString)
                                                                   )
                                        }
                                        else
                                        {
                                            // The Call Expression has two primary children
                                            //     GrReferenceExpression [Reference Expression] : "id "edu.wpi.first.GradleRIO" version"
                                            //     GrCommandArgumentList [Command Arguments] : "2021.2.3"
                                            gradleRioCallExpression
                                                .children
                                                .filterIsInstance<GrCommandArgumentList>()
                                                .firstOrNull()
                                                ?.children
                                                ?.filterIsInstance<GrLiteral>()
                                                ?.firstOrNull()
                                                ?.let { literal: GrLiteral ->
                                                    val replacement =
                                                        if (literal.text?.startsWith('\'') == true)
                                                            "'${version.versionString}'"
                                                        else "\"${version.versionString}\""
                                                    PsiImplUtil.replaceExpression(replacement, literal)
                                                    doGradleReimport()
                                                }
                                        }
                                    }
                                }
                            }
                        }) // close accept
                }
                else if (psiFile.name.endsWith(".kts", ignoreCase = true))
                {
                    FrcNotifications.notify(
                        FrcNotificationType.ACTIONABLE_INFO,
                        "Auto updated of Kotlin DSL build files is not yet supported by the FRC plugin. You will need to manually " +
                            "edit the gradle build file and set version for the GradleRio plugin to '${version.versionString}'"
                                           )
                }
                else
                {
                    LOG.warn("[FRC] Unknown file type for gradle build: ${psiFile.name}")
                    FrcNotifications.notify(
                        FrcNotificationType.ACTIONABLE_WARN,
                        FrcMessageKey.of("frc.notification.wpiLibVersionStatus.gradle.not.able.to.update", version.versionString)
                                           )
                }
            }
        }
    }

    private fun doGradleReimport()
    {
        project.reimportGradleProject(callback = object : ExternalProjectRefreshCallback
                                      {

                                          override fun onSuccess(externalProject: DataNode<ProjectData>?)
                                          {
                                              FrcNotifications.notifyNoTitle(
                                                  FrcNotificationType.BUILD__INFO,
                                                  FrcMessageKey.of("frc.notification.wpiLibVersionStatus.gradle.reimport.success"),
                                                  project = project
                                                                            )
                                          }

                                          override fun onFailure(errorMessage: String, errorDetails: String?)
                                          {
                                              // The build tool window will automatically open
                                              FrcNotifications.notifyNoTitle(
                                                  FrcNotificationType.BUILD__ERROR,
                                                  FrcMessageKey.of("frc.notification.wpiLibVersionStatus.gradle.reimport.failure"),
                                                  project = project
                                                                            )
                                          }
                                      })
    }

    private fun getWpiLibVersionStatus(): WpiLibVersionStatus?
    {
        LOG.debug("[FRC] getWpiLibVersionStatus() called. Will perform work in runReadActionInSmartMode")
        var versionStatus: WpiLibVersionStatus? = null
        DumbService.getInstance(project).runReadActionInSmartMode() {
            if (!project.isDisposed && project.isFrcFacetedProject())
            {
                LOG.debug("[FRC] getWpiLibVersionStatus() : runReadActionInSmartMode has started.")
                val state = GradleRioMavenMetadataState.getInstance(true)
                val latestAvailableVersion = state.wpiLibMavenMetadata.latestAsWpiLibVersion

                val attachedVersion = project.getAttachedWpiLibVersion()
                if (attachedVersion != null)
                {
                    // TODO: we should check for a conflict between project year in the wpi_lib_preferences and the attached library? 
                    //       This should be added as inspection that runs on project startup, and anytime the preferences file changes
                    val projectYear = attachedVersion.frcYear

                    // TODO: if attached is null, we should check the Gradle file and see what is configured

                    val latestAvailableForSameYear = state.wpiLibMavenMetadata.wpiLibVersions.filterToLatestForYear(projectYear)
                    if (latestAvailableForSameYear != null)
                    {
                        versionStatus = WpiLibVersionStatus(attachedVersion, latestAvailableForSameYear, latestAvailableVersion)
                        LOG.debug("[FRC] WpiLibVersionStatus readActionInSmartMode determined to be: $versionStatus")
                    }
                }
            }
        }
        myState.updateLastCheckedTime()
        LOG.debug("[FRC] getWpiLibVersionStatus() returning WpiLibVersionStatus of:  $versionStatus")
        return versionStatus
    }

    override fun getState(): WpiLibVersionServiceState = myState

    override fun loadState(state: WpiLibVersionServiceState)
    {
        myState = state
    }

    override fun dispose()
    {
        cancelTimer()
    }

    private fun cancelTimer()
    {
        timerTask?.cancel()
        timer.purge()
    }

    class CheckStatusTimerTask(private val project: Project) : TimerTask()
    {
        override fun run() = project.service<WpiLibVersionService>().checkWpiLibStatusAndAlertIfNeeded()
    }
}


data class WpiLibVersionStatus(
    val attachedVersion: WpiLibVersion,
    val latestAvailableForSameYear: WpiLibVersion,
    val latestAvailableVersion: WpiLibVersion?
                              )
{
    fun updateAvailableForAttachedYear(): Boolean = latestAvailableForSameYear.isNewerThan(attachedVersion)

    @Suppress("unused")
    constructor(attachedVersion: String, latestAvailableForSameYear: String, latestAvailableVersion: String) :
        this(
            WpiLibVersionImpl.parse(attachedVersion),
            WpiLibVersionImpl.parse(latestAvailableForSameYear),
            WpiLibVersionImpl.parse(latestAvailableVersion)
            )
}

data class WpiLibVersionServiceState(var lastChecked: String = "20180101000000")
{
    val lastCheckedDateTime: LocalDateTime
        get() = LocalDateTime.parse(lastChecked, lastCheckedDateTimeFormatter)

    val durationSinceLastCheck: Duration
        get() = Duration.between(lastCheckedDateTime, LocalDateTime.now())

    @JvmOverloads
    fun updateLastCheckedTime(checkTime: LocalDateTime = LocalDateTime.now())
    {
        lastChecked = lastCheckedDateTimeFormatter.format(checkTime)
    }

}

