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

package net.javaru.iip.frc.services

import com.intellij.notification.Notification
import com.intellij.notification.NotificationListener
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.actions.tools.legacy.AttachLegacyUserLibDirAction
import net.javaru.iip.frc.actions.tools.legacy.AttachLegacyWpilibAction
import net.javaru.iip.frc.actions.tools.legacy.DownloadLegacyWpiLibAction
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.notify.FrcNotificationType
import net.javaru.iip.frc.notify.FrcNotifications
import net.javaru.iip.frc.notify.FrcNotifications.notify
import net.javaru.iip.frc.notify.FrcNotificationsTracker
import net.javaru.iip.frc.notify.FrcNotificationsTracker.NotificationKey.AttachUserLibQuery
import net.javaru.iip.frc.notify.FrcNotificationsTracker.NotificationKey.AttachWpiLibQuery
import net.javaru.iip.frc.util.createUri
import net.javaru.iip.frc.util.isAntBasedFrcProject
import net.javaru.iip.frc.wpilib.legacy.LegacyWpiLibLibrariesUtils
import javax.swing.event.HyperlinkEvent


/**
 * Checks if a project is a legacy Ant based project, and if so, checks if the WPI Library and/or the User Library need to be attached.
 *
 * @param project             the project
 * @param knownFacetedProject if the project is already known to be an FrcFaceted project
 */
fun checkLegacyProjectLibraryAttachmentStatus(project: Project, knownFacetedProject: Boolean)
{
    if ((knownFacetedProject || project.isFrcFacetedProject())
        && project.isAntBasedFrcProject()) /* TODO: The ant project check needs improvement. Right now it just checks it is not a gradle project  */
    {

        if (FrcNotificationsTracker.getNotification(project, AttachWpiLibQuery) == null && !LegacyWpiLibLibrariesUtils.isLegacyWpilibAttachedViaReadAction(project))
        {
            if (LegacyWpiLibLibrariesUtils.isLegacyWpilibDownloadedToSystem())
            {
                queueAttachWpilibQueryNotification(project)
            }
            else
            {
                queueDownloadAndAttachWpilibNotification(project)
            }
        }
        if (FrcNotificationsTracker.getNotification(project, AttachUserLibQuery) == null && !LegacyWpiLibLibrariesUtils.isLegacyUserLibAttachedViaReadAction(project))
        {
            queueMissingUserLibQueryNotification(project)
        }
    }
}

private fun queueMissingUserLibQueryNotification(project: Project): Notification?
{
    val notification = notify(FrcNotificationType.ACTIONABLE_INFO,
                              "Would you like to <a href='attach'>attach</a> the User Lib directory as a Module Library?",
                              "User Lib Directory Not Attached",
                              null,
                              NotificationListener { theNotification: Notification, event: HyperlinkEvent ->
                                  theNotification.expire()
                                  if ("attach" == event.description)
                                  {
                                      AttachLegacyUserLibDirAction.attachUserLib(project, false)
                                  }
                              })
    FrcNotificationsTracker.putNotification(project, AttachUserLibQuery, notification)
    return notification
}

private fun queueDownloadAndAttachWpilibNotification(project: Project)
{
    val notification = notify(FrcNotificationType.ACTIONABLE_INFO,
                              "Would you like to <a href='download'>download and attach</a> WPILib as a Module Library?",
                              "WPILib Not Found on System",
                              null,
                              NotificationListener { _: Notification?, event: HyperlinkEvent ->
                                  if ("download" == event.description)
                                  {
                                      DownloadLegacyWpiLibAction.downloadLatestInBackground(project,
                                                                                            true,
                                                                                            true)
                                  }
                              })
    FrcNotificationsTracker.putNotification(project, AttachWpiLibQuery, notification)
}

private fun queueAttachWpilibQueryNotification(project: Project): Notification?
{
    val notification = notify(FrcNotificationType.ACTIONABLE_INFO,
                              "Would you like to <a href='attach'>attach</a> WPILib as a Module Library?",
                              "WPILib not Attached",
                              null,
                              NotificationListener { theNotification: Notification, event: HyperlinkEvent ->
                                  theNotification.expire()
                                  if ("attach" == event.description)
                                  {
                                      AttachLegacyWpilibAction.attachWpiLib(project, false, true)
                                  }
                              })
    FrcNotificationsTracker.putNotification(project, AttachWpiLibQuery, notification)
    return notification
}

fun checkLegacyIssue8Refresh(project: Project)
{
    if (project.isFrcFacetedProject() && LegacyWpiLibLibrariesUtils.is2018CommonRefreshNeededViaReadAction())
    {
        val uri = createUri("https://gitlab.com/Javaru/frc-intellij-idea-plugin/issues/8")
        val refreshNotification = FrcNotifications.createNotification(FrcNotificationType.ACTIONABLE_INFO,
                                                                      "In order to fully resolve "
                                                                      + "<a href='" + uri
                                                                      + "'>Issue #8: Cannot deploy code to roboRIO</a>, caused by a change "
                                                                      + "to the 2018 WPILib, the previously downloaded WPILib and its associated tools needs to "
                                                                      + "be refreshed. A fresh download has been started.",
                                                                      DownloadLegacyWpiLibAction.NOTIFICATIONS_SUBTITLE + " Refresh Required",
                                                                      NotificationListener.URL_OPENING_LISTENER)
        refreshNotification.notify(project)
        DownloadLegacyWpiLibAction.downloadLatestInBackground(project,
                                                              true,
                                                              false,
                                                              true,
                                                              {
                                                                  refreshNotification.expire()
                                                                  notify(FrcNotificationType.GENERAL_INFO,
                                                                         "Refresh of WPILib has completed",
                                                                         DownloadLegacyWpiLibAction.NOTIFICATIONS_SUBTITLE,
                                                                         project)
                                                              }
                                                             ) {
            refreshNotification.expire()
            notify(FrcNotificationType.GENERAL_INFO,
                   "Refresh of WPILib failed. You will likely have problems deploying code to a 2018 roboRIO until "
                   + "a fresh copy of the WPILib is downloaded. Please verify your internet connection and download a "
                   + "fresh copy of the WPILIb and its tools via the menu: <em>Tools > FRC > Download Latest WPILib</em>",
                   DownloadLegacyWpiLibAction.NOTIFICATIONS_SUBTITLE + " Refresh Failed",
                   project)
        }
    }
}

fun cancelLegacyWpiLibIsDownloadingNotifications(project: Project)
{
    FrcNotificationsTracker.expireNotification(project, FrcNotificationsTracker.NotificationKey.DownloadNewWpiLibVersionQuery)
}