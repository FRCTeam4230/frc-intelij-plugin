/*
 * Copyright 2015-2022 the original author or authors.
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

package net.javaru.iip.frc.plugin

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.application.impl.ApplicationInfoImpl
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.BuildNumber
import com.intellij.util.xmlb.XmlSerializerUtil
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.net.FrcPseudoRestService
import net.javaru.iip.frc.notify.FrcNotificationsBuilder
import net.javaru.iip.frc.notify.FrcNotifyType
import net.javaru.iip.frc.util.runBackgroundTask
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.VisibleForTesting
import java.io.StringReader
import java.util.*


private val logger = logger<FrcPluginVersionManagerApplicationService>()


/**
 * A project level service that runs when a project starts up. This Background Activity
 * checks if the running version of IntelliJ IDEA is EOL for the FRC plugin and/or if a
 * new version of the FRC plugin is available but requires the user to upgrade Intellij IDEA.
 */
class FrcPluginVersionManagerStartupActivity : StartupActivity, DumbAware
{
    override fun runActivity(project: Project)
    {
        logger.debug("[FRC] FrcPluginVersionManagerStartupActivity.runActivity() running")
        FrcPluginVersionManagerApplicationService.getInstance().checkPluginUpdateStatusViaBackGroundTask(project)
    }
}

class FrcPluginVersionManagerApplicationService : Disposable
{
    
    private val notificationUUID = UUID.randomUUID()!!

    fun checkPluginUpdateStatusViaBackGroundTask(project: Project)
    {
        project.runBackgroundTask("FRC Plugin Status Check") {
            checkPluginUpdateStatus(project)
        }
    }
    
    @Suppress("MemberVisibilityCanBePrivate")
    fun checkPluginUpdateStatus(project: Project?)
    {
        logger.debug {"[FRC] checking plugin status. project? = $project"}
        // TODO This is a temp to get an EOL notification out. This needs to be improved.
        try
        {
            val resourcePath = "dynamic-notifications/eol.properties"
            val resource = FrcPseudoRestService.getResource(resourcePath) ?: "oldestSupportedBaseBuild=202"
            val properties = Properties()
            properties.load(StringReader(resource))
            val oldestSupportedBaseBuild = properties["oldestSupportedBaseBuild"]?.toString()?.toInt() ?: 202
            
            val appInfo = ApplicationInfoEx.getInstanceEx() as ApplicationInfoImpl
            val build: BuildNumber = appInfo.build
            val runningInstanceBaselineVersion = build.baselineVersion

            logger.debug {"[FRC] baseline version: $runningInstanceBaselineVersion"}

            checkPluginUpdateStatus(project, runningInstanceBaselineVersion, oldestSupportedBaseBuild)

        }
        catch (t: Throwable)
        {
            logger.warn("[FRC] could not check Plugin Update status. Cause: $t", t)
        }
    }

    @VisibleForTesting
    fun checkPluginUpdateStatus(project: Project?, runningInstanceBaselineVersion: Int, oldestSupportedBaseBuild: Int)
    {
        logger.debug("[FRC] Checking plugin update status. running: '$runningInstanceBaselineVersion' oldest supported: '$oldestSupportedBaseBuild'.")
        if (runningInstanceBaselineVersion < oldestSupportedBaseBuild)
        {
            logger.debug("[FRC] Current version is no longer supported. Notifying user.")
            val oldestVersionString = "20${(oldestSupportedBaseBuild / 10)}.${oldestSupportedBaseBuild % 10}"
            @Suppress("HtmlRequiredLangAttribute") @Language("HTML")
            val eolMessage = """
                         <html>
                         <h1><strong>FRC Plugin Support</strong></h1>
                         <span style='font-size: larger'>
                         <strong>Support for IntelliJ IDEA versions older than $oldestVersionString has ended.</strong><br/>
                         You will need to upgrade to Intellij IDEA $oldestVersionString or later to get the latest 
                         <strong>FRC Plugin</strong> features.
                         <br/><br/>
                         While I wish I could support more IntelliJ IDEA versions, doing so adds considerable time to the
                         development and maintenance of the plugin. This is because new features often have to be implemented 
                         differently (and thus written multiple times) when back ported to older versions since the 
                         Intellij IDEA Plugin API evolves between versions. I would much rather put that time into adding new 
                         features. I also think that most users given a choice between more features or support for more versions 
                         would desire more features.
                         <br/><br/>
                         Given that this plugin works fully with the free IntelliJ IDEA Community edition (and Education edition), 
                         I do not think asking users to use a fairly recent version is overly burdensome. If you use the 
                         <em>JetBrains Toolbox App</em> to install IntelliJ IDEA, upgrading is super easy, and you can have multiple 
                         versions and editions of IntelliJ IDEA installed simultaneously if needed. 
                         <br/><br/>
                         See the FRC Plugin EOL Policy for more detail.
                         <br/><br/>
                         Thank you for your understanding.
                         </span>
                         </html>
                         """.trimIndent()

            if (project.isFrcFacetedProject())
            {
                FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON
                    .createEolNotification(eolMessage)
                    .notifyAllProjectsViaBalloon(notifyFrcProjectsOnly = true, notificationUUID)
                FrcPluginVersionManagerState.getInstance().eolNotifiedForBuild.add(runningInstanceBaselineVersion)

            } 
            else if (!FrcPluginVersionManagerState.getInstance().eolNotifiedForBuild.contains(runningInstanceBaselineVersion))
            {
                FrcNotifyType.ACTIONABLE_WARN
                    .createEolNotification(eolMessage)
                    .notifyViaBalloon(project)
                FrcPluginVersionManagerState.getInstance().eolNotifiedForBuild.add(runningInstanceBaselineVersion)
            }
        }
        else
        {
            logger.debug("[FRC] Current IDEA version is still supported.")
        }
    }
    
    @Suppress("UNUSED_ANONYMOUS_PARAMETER")
    private fun FrcNotifyType.createEolNotification(eolMessage: String): FrcNotificationsBuilder.CompletableStep
    {
        return this.withContent(eolMessage)
            .withNoTitle()
            //.withListener(NotificationListener.URL_OPENING_LISTENER) 
            .withAction("FRC Plugin EOL policy", expiring = false) {actionEvent, notification ->  
                BrowserUtil.browse("https://gitlab.com/Javaru/frc-intellij-idea-plugin/-/blob/master/README.adoc#eol-policy")
            }.withAction("JetBrains Toolbox app", expiring = false) { actionEvent, notification ->
                BrowserUtil.browse("https://www.jetbrains.com/toolbox-app")
            }
            .noMoreActions()
    }

    override fun dispose()
    {
        // Nothing at this time
    }

    

    companion object
    {
        @Suppress("RemoveExplicitTypeArguments")
        @JvmStatic
        fun getInstance(): FrcPluginVersionManagerApplicationService = service<FrcPluginVersionManagerApplicationService>()
    }
}

@State(name = "FrcPluginVersionManagerState", storages = [Storage("frc.xml")])
data class FrcPluginVersionManagerState(var eolNotifiedForBuild: MutableSet<Int> = mutableSetOf()) : PersistentStateComponent<FrcPluginVersionManagerState>
{
    override fun getState(): FrcPluginVersionManagerState
    {
        logger.trace {"[FRC] FrcPluginVersionManagerState.getState() called. Returning current state of: ${toString()}"}
        return this
    }

    override fun loadState(state: FrcPluginVersionManagerState)
    {
        logger.trace {"[FRC] FrcPluginVersionManagerState.loadState() called with state object of: $state"}
        XmlSerializerUtil.copyBean<FrcPluginVersionManagerState>(state, this)
    }

    companion object
    {
        @JvmStatic
        fun getInstance(): FrcPluginVersionManagerState = service()
    }
}