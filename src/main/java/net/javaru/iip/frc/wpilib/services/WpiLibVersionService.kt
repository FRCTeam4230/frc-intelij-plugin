/*
 * Copyright 2015-2020 the original author or authors
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.javaru.iip.frc.wpilib.services

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.startup.StartupManager
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.i18n.FrcBundle.message
import net.javaru.iip.frc.notify.FrcNotifications
import net.javaru.iip.frc.notify.FrcNotifications.Companion.FRC_ACTIONABLE_NOTIFICATION_GROUP
import net.javaru.iip.frc.notify.FrcNotifications.Companion.FRC_GENERAL_NOTIFICATION_GROUP
import net.javaru.iip.frc.settings.FrcApplicationSettings
import net.javaru.iip.frc.util.asDate
import net.javaru.iip.frc.util.lastCheckedDateTimeFormatter
import net.javaru.iip.frc.wpilib.getAttachedWpiLibVersion
import net.javaru.iip.frc.wpilib.gradlePluginRepo.GradleRioMavenMetadataState
import net.javaru.iip.frc.wpilib.version.WpiLibVersion
import net.javaru.iip.frc.wpilib.version.WpiLibVersionImpl
import net.javaru.iip.frc.wpilib.version.filterToLatestForYear
import java.time.Duration
import java.time.LocalDateTime
import java.util.*


// GitConflictsToolWindowManager is a good example of using the StartupActivity
// AcceptedLanguageLevelsSettings sows a class tha is both a StartupActivity and an application Service

// For IJ v2019.3+ we can/should use StartupActivity.Background (and change the plugin.xml element to <backgroundPostStartupActivity>)
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
@State(name = "WpiLibVersionService", storages =[Storage(StoragePathMacros.WORKSPACE_FILE)])
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
    fun checkWpiLibStatusAndAlertIfNeeded(notifyIfNoUpdateAvailable: Boolean = false,
                                          maxTimeSinceLastCheck: Duration = Duration.ofSeconds(10))
    {
        if (myState.durationSinceLastCheck < maxTimeSinceLastCheck) return
        
        val versionStatus = getWpiLibVersionStatus()
        var updateAvailableNotification: Notification? = null
        
        if (versionStatus != null)
        {
            if (versionStatus.updateAvailableForAttachedYear())
            {
                updateAvailableNotification = notifyNewerWpiLibVersionIsAvailable(versionStatus)
            }
            else if(notifyIfNoUpdateAvailable)
            {
                notifyNoUpdateAvailable(versionStatus.attachedVersion.frcYear)
            }
        }
        else if (notifyIfNoUpdateAvailable)
        {
            notifyUnableToCheckVersionStatus()
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
            
            // TODO need to implement update capability - and then move this to I18N into the above resource bundle string and update the below event handler
            //content.append("Would you like to update the Gradle build script to use the new version? <a href='makeUpdate'>Yes</a>  <a href='doNotUpdate'>No</a>")

            val notification = FRC_ACTIONABLE_NOTIFICATION_GROUP
                .createNotification(FrcNotifications.Title,
                                    subtitle,
                                    content,
                                    NotificationType.INFORMATION
                                   ) 
//                { theNotification: Notification, event: HyperlinkEvent ->
//                    theNotification.expire()
//                    if ("makeUpdate" == event.description)
//                    {
//                        TODO("Call Update Gradle Function once written")
//                    }
//                }
            notification.notify(project)
            return notification
        }
        else
        {
            return null
        }
            
    }

    private fun notifyUnableToCheckVersionStatus()
    {
        val content = message("frc.notification.wpiLibVersionStatus.unableToCheck.content")
        val notification = FRC_GENERAL_NOTIFICATION_GROUP
            .createNotification(FrcNotifications.Title,
                                null,
                                content,
                                NotificationType.INFORMATION)

        notification.notify(project)
    }
    
    private fun notifyNoUpdateAvailable(year: Int)
    {
        // we call toString on the year otherwise the resource bundle formats it wiath a comma: 2,019
        val content = message("frc.notification.wpiLibVersionStatus.haveTheLatest.content", year.toString())
        val notification = FRC_GENERAL_NOTIFICATION_GROUP
            .createNotification(FrcNotifications.Title,
                                null,
                                content,
                                NotificationType.INFORMATION)

        notification.notify(project)
    }
    
    
    private fun getWpiLibVersionStatus(): WpiLibVersionStatus?
    {
        var versionStatus: WpiLibVersionStatus? = null
        DumbService.getInstance(project).runReadActionInSmartMode() {
            if (!project.isDisposed && project.isFrcFacetedProject())
            {
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
                    }
                }
            }
        }
        myState.updateLastCheckedTime()
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


data class WpiLibVersionStatus(val attachedVersion: WpiLibVersion,
                               val latestAvailableForSameYear: WpiLibVersion,
                               val latestAvailableVersion: WpiLibVersion?)
{
    fun updateAvailableForAttachedYear(): Boolean = latestAvailableForSameYear.isNewerThan(attachedVersion)

    @Suppress("unused")
    constructor(attachedVersion: String, latestAvailableForSameYear: String, latestAvailableVersion: String) :
            this(WpiLibVersionImpl.parse(attachedVersion),
                 WpiLibVersionImpl.parse(latestAvailableForSameYear),
                 WpiLibVersionImpl.parse(latestAvailableVersion))
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

