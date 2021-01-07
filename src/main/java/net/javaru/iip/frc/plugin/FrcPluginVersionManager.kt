/*
 * Copyright 2015-2021 the original author or authors
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

package net.javaru.iip.frc.plugin

import com.intellij.notification.NotificationListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.application.impl.ApplicationInfoImpl
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.util.BuildNumber
import com.intellij.util.xmlb.XmlSerializerUtil
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.notify.FrcNotificationType
import net.javaru.iip.frc.notify.FrcNotifications
import net.javaru.iip.frc.notify.FrcNotifications.createNotification
import org.intellij.lang.annotations.Language
import java.util.*


private val LOG = logger<FrcPluginVersionManagerApplicationService>()


/**
 * A project level service that runs when a project starts up. This Background Activity
 * checks if the running version of IntelliJ IDEA is EOL for the FRC plugin and/or if a
 * new version of the FRC plugin is available but requires the user to upgrade Intellij IDEA.
 */
class FrcPluginVersionManagerStartupActivity : StartupActivity, DumbAware
{
    override fun runActivity(project: Project)
    {
        FrcPluginVersionManagerApplicationService.getInstance().checkPluginUpdateStatus(project)
    }
}

class FrcPluginVersionManagerApplicationService : Disposable
{
    private val oldestSupportedBaseBuild = 202
    private val oldestVersionString = "20${(oldestSupportedBaseBuild / 10)}.${oldestSupportedBaseBuild % 10}"
    private val notificationUUID = UUID.randomUUID()!!

    fun checkPluginUpdateStatus(project: Project?)
    {
        LOG.debug("[FRC] checking plugin status. project? = $project")
        // TODO This is a temp to get an EOL notification out. This needs to be improved.
        try
        {
            val appInfo = ApplicationInfoEx.getInstanceEx() as ApplicationInfoImpl
            val build: BuildNumber = appInfo.build
            val baselineVersion = build.baselineVersion

            LOG.debug("[FRC] baseline version: $baselineVersion")

            if(baselineVersion < oldestSupportedBaseBuild)
            {
                if (project.isFrcFacetedProject())
                {
                    FrcNotifications.notifyBalloonAllOpenProjects(notificationUUID, notifyFrcProjectsOnly = true) {
                        createNotification(FrcNotificationType.ACTIONABLE_INFO_WITH_FRC_ICON, content = eolMessage, subTitle = null, listener = NotificationListener.URL_OPENING_LISTENER)
                    }
                    FrcPluginVersionManagerState.getInstance().eolNotifiedForBuild.add(baselineVersion)

                }
                else if (!FrcPluginVersionManagerState.getInstance().eolNotifiedForBuild.contains(baselineVersion))
                {
                    FrcNotifications.notifyBalloon(
                        FrcNotificationType.ACTIONABLE_WARN,
                        eolMessage,
                        null,
                        project,
                        NotificationListener.URL_OPENING_LISTENER)
                    FrcPluginVersionManagerState.getInstance().eolNotifiedForBuild.add(baselineVersion)
                }
            }

        }
        catch (t: Throwable)
        {
            LOG.warn("[FRC] could not check Plugin Update status. Cause: $t", t)
        }
    }

    override fun dispose()
    {
        // Nothing at this time
    }

    @Suppress("HtmlRequiredLangAttribute")
    @Language("HTML")
    val eolMessage = """
                    <html>
                    <strong><em>FRC Plugin</em> support for IntelliJ IDEA versions older than $oldestVersionString has ended.</strong><br/>
                    You will need to upgrade to Intellij IDEA $oldestVersionString or later to get the latest FRC Plugin features.
                    <br/><br/>
                    While I wish I could support more older IntelliJ IDEA versions, doing so adds considerable time to the
                    development and maintenance of the plugin as new features often have to be back ported to the older versions
                    since the Intellij IDEA Plugin API evolves between versions. I would much rather put that
                    time into adding new features. Given that this plugin works fully with the free
                    IntelliJ IDEA Community edition (and Education edition), I do not think asking users to use
                    a fairly recent version is overly burdensome.
                    Please note that if you use the JetBrains <a href='https://www.jetbrains.com/toolbox-app/'>Toolbox App</a> to
                    install IntelliJ IDEA, upgrading is super easy, and you can have multiple versions of IntelliJ IDEA installed
                    simultaneously if needed. 
                    <br/><br/>
                    See the <a href='https://gitlab.com/Javaru/frc-intellij-idea-plugin/-/blob/master/README.adoc#eol-policy'>FRC Plugin's EOL Policy</a> 
                    for more detail.
                    <br/><br/>
                    Thank you for your understanding.
                    </html>
                """.trimIndent()

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
        LOG.trace("[FRC] FrcPluginVersionManagerState.getState() called. Returning current state of: ${toString()}")
        return this
    }

    override fun loadState(state: FrcPluginVersionManagerState)
    {
        LOG.trace("[FRC] FrcPluginVersionManagerState.loadState() called with state object of: $state")
        XmlSerializerUtil.copyBean<FrcPluginVersionManagerState>(state, this)
    }

    companion object
    {
        @JvmStatic
        fun getInstance(): FrcPluginVersionManagerState = service()
    }
}