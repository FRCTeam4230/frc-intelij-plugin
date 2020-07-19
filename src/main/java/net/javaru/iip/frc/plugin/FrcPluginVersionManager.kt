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

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

private val LOG = Logger.getInstance(FrcPluginVersionManager::class.java)
object FrcPluginVersionManager
{
    
    fun checkPluginUpdateStatus(project: Project?)
    {
        LOG.debug("[FRC] checking plugin status. project? = $project")
        // TODO This is a temp hard coded hack to get a notification out now. This needs to be improved.
        //      Want some functionality that automatic informs people when there is a new update that requires a newer version of IntelliJ IDEA.

//        val appInfo = ApplicationInfoEx.getInstanceEx() as ApplicationInfoImpl
//        val build: BuildNumber = appInfo.build
//        val baselineVersion = build.baselineVersion
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
//                //TODO - rather than using a Date (and having to be absolutely sure we have a new version out by then), we want to ti query the Jetbrains Plugin service
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
