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

package net.javaru.iip.frc.notify

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.actions.ConfigureTeamNumberBasicAction
import net.javaru.iip.frc.settings.FrcApplicationSettings
import java.util.HashMap


private const val Title = "FRC"

private val configureTeamNumberNotifications = HashMap<String, Notification>()

fun notifyAboutTeamNumberNeedingToBeConfigured(project: Project?, useSticky: Boolean, asWarning: Boolean): Notification
{

    // See com/intellij/ide/plugins/PluginManager.java:177 for an example

    var notification: Notification? = configureTeamNumberNotifications[createProjectKey(project)]

    // We want to replace an existing info notification with a warning one if a warning one has been requested
    if (notification != null && asWarning && notification.type != NotificationType.WARNING)
    {
        notification.expire()
        notification = null
    }

    if (notification == null || notification.isExpired)
    {
        notification = createConfigureTeamNotification(project, useSticky, asWarning)
        // This makes the notification title & subtitle appear in bold in the Event Log window
        notification.isImportant = true
        if (project != null)
        {
            configureTeamNumberNotifications[createProjectKey(project)] = notification
        }
        Notifications.Bus.notify(notification, project)
    }
    return notification
}

private fun createConfigureTeamNotification(project: Project?, useSticky: Boolean, asWarning: Boolean): Notification
{
    val subtitle = if (asWarning) "Team Number Not Set" else "Configuration Needed"
    val contentPrefix = if (asWarning) "Without your FRC team number being set, robot deploys will fail. " else ""
    val content = contentPrefix + "Please <a href='configure'>configure</a> your FRC Team Number."
    val icon = if (asWarning) FrcNotifications.IconWarn else FrcNotifications.IconInfo
    val notificationType = if (asWarning) NotificationType.WARNING else NotificationType.INFORMATION

    val notificationGroup = if (useSticky) FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP else FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP
    return Notification(notificationGroup.displayId,
                        icon,
                        Title,
                        subtitle,
                        content,
                        notificationType
                       ) { theNotification, event ->
        if ("configure" == event.description)
        {
            //  final Configurable configurable = FrcApplicationSettingsConfigurable.getInstance();
            //  IdeFrame ideFrame = WindowManagerEx.getInstanceEx().findFrameFor(project);
            //  ShowSettingsUtil.getInstance().editConfigurable((JFrame) ideFrame, configurable);
            ConfigureTeamNumberBasicAction.openConfigureTeamNumberDialog(project)
        }

        if (FrcApplicationSettings.getInstance().isTeamNumberConfigured())
        {
            theNotification.expire()
        }
    }

    // THIS IS AN ALTERNATIVE WAY TO CREATE A NOTIFICATION FROM THE NotificationGroup CLASS
    //        return notificationGroup.createNotification(Title,
    //                                                    "Configuration Needed",
    //                                                    "Please <a href='configure'>configure</a> your FRC Team Number.",
    //                                                    NotificationType.INFORMATION,
    //                                                    (theNotification, event) ->
    //                                                    {
    //                                                        if ("configure".equals(event.getDescription()))
    //                                                        {
    ////                                                                       final Configurable configurable = FrcApplicationSettingsConfigurable.getInstance();
    ////                                                                       IdeFrame ideFrame = WindowManagerEx.getInstanceEx().findFrameFor(project);
    ////                                                                       ShowSettingsUtil.getInstance().editConfigurable((JFrame) ideFrame, configurable);
    //                                                            ConfigureTeamNumberBasicAction.openConfigureTeamNumberDialog(project);
    //                                                        }
    //
    //                                                        if (FrcFacetSettings.getInstance().isTeamNumberConfigured())
    //                                                        {
    //                                                            theNotification.expire();
    //                                                        }
    //                                                    }
    //        );
}


fun expireConfigureTeamNumberNotification(project: Project?)
{
    val notification = configureTeamNumberNotifications[createProjectKey(project)]
    notification?.expire()
}

private fun createProjectKey(project: Project?): String
{
    return if (project != null)
        "${project.name}--${project.basePath}"
    else
        "null-project"
}