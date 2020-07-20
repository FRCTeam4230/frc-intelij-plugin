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

package net.javaru.iip.frc.plugin

import com.intellij.notification.NotificationListener
import com.intellij.openapi.application.ex.ApplicationInfoEx
import com.intellij.openapi.application.impl.ApplicationInfoImpl
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.BuildNumber
import net.javaru.iip.frc.notify.FrcNotificationType
import net.javaru.iip.frc.notify.FrcNotifications
import org.intellij.lang.annotations.Language


private val LOG = Logger.getInstance(FrcPluginVersionManager::class.java)
object FrcPluginVersionManager
{
    private const val oldestSupportedBaseBuild = 193
    private const val oldestVersionString = "20${(oldestSupportedBaseBuild / 10)}.${oldestSupportedBaseBuild % 10}"

    fun checkPluginUpdateStatus(project: Project?)
    {
        LOG.debug("[FRC] checking plugin status. project? = $project")
        // TODO This is a temp hard coded solution to get an EOL notification out. This needs to be improved.
        try
        {
            val appInfo = ApplicationInfoEx.getInstanceEx() as ApplicationInfoImpl
            val build: BuildNumber = appInfo.build
            val baselineVersion = build.baselineVersion

            LOG.debug("[FRC] baseline version: $baselineVersion")

            if(baselineVersion < oldestSupportedBaseBuild)
            {
                @Suppress("HtmlRequiredLangAttribute")
                @Language("HTML")
                val content = """
                    <html>
                    <strong><em>FRC Plugin</em> support for IntelliJ IDEA versions older than $oldestVersionString has ended.</strong><br/>
                    You will need to upgrade to a newer Intellij IDEA version to get the latest FRC Plugin features.
                    The FRC Plugin typically supports the last three major versions of IntelliJ IDEA.
                    <br/><br/>
                    While I wish I could support more versions, doing so adds considerable time to the
                    development of the plugin as I have to maintain separate branches for each major version
                    since the Intellij IDEA Plugin API evolves between versions. I would much rather put that
                    time into adding new features. Given that this plugin works fully with the free
                    IntelliJ IDEA Community edition (and Education edition), I do not think asking users to use
                    a major version released within the past year is overly burdensome.
                    If you use the JetBrains <a href='https://www.jetbrains.com/toolbox-app/'>Toolbox App</a> to
                    install IntelliJ IDEA, upgrading is a snap. Thank you for your understanding.
                    </html>
                """.trimIndent()

                FrcNotifications.notifyBalloon(FrcNotificationType.ACTIONABLE_WARN,
                                              content,
                                              null,
                                              project,
                                              NotificationListener.URL_OPENING_LISTENER)
            }

        }
        catch (t: Throwable)
        {
            LOG.warn("[FRC] could not check Plugin Update status. Cause: $t", t)
        }


//
//
//        val now = LocalDate.now()
//        if (baselineVersion < 192 )
//        {
//            val lastNotify = LocalDate.parse(FrcApplicationSettings.getInstance().notify19Up)
//            val isFrcProject = project.isFrcFacetedProject()
//
//            // Notify every 3 days FRC projects forever, or
//            // every 21 days for non FRC projects, but only until May 2020. (After that we only notify for FRC projects)
//            val notify =
//                    ( isFrcProject && lastNotify.plusDays(2).isBefore(LocalDate.now()) )
//                    ||
//                    ( lastNotify.plusDays(20).isBefore(LocalDate.now()) && now.isBefore(LocalDate.parse("2020-05-01")) )
//
//
//
//            if (notify)
//            {
//                //TODO - rather than using a Date (and having to be absolutely sure we have a new version out by then), we want to to query the Jetbrains Plugin service
//                //       or maintain a status list
//                val newVersionAvailable = now.isAfter(LocalDate.parse("2019-09-15"))
//
//                val firstSentence = if (newVersionAvailable)
//                    "<strong>A new version of the FRC Plugin is available, but it requires IntelliJ IDEA v2019.2.x or later.</strong>"
//                else
//                    "The next release of the <strong>FRC Plugin</strong> will require IntelliJ IDEA v2019.2.x or later."
//
//                val suffix = if (PlatformUtils.isIdeaUltimate())
//                    " As a reminder, the FRC plugin works with the free Community Edition of IntelliJ IDEA. "
//                else
//                    ""
//
//                val notifyType = if (newVersionAvailable) FrcNotificationType2.ACTIONABLE_WARN else FrcNotificationType2.ACTIONABLE_INFO
//
//
//                FrcNotifications2.notify(notifyType,
//                                        "$firstSentence " +
//                                        "This is due to some significant changes to the IntelliJ IDEA plugin API being leveraged. " +
//                                        "Please upgrade to the latest version of IntelliJ IDEA at your convenience.$suffix Thanks.",
//                                        project = project)
//
//                // In settings:  var notify19Up: String = "2019-01-01",
//                FrcApplicationSettings.getInstance().notify19Up = now.toString()
//            }
//        }
    }
}
