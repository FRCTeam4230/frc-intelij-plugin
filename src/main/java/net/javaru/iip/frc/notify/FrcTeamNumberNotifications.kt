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

package net.javaru.iip.frc.notify

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.FrcPluginGlobals
import net.javaru.iip.frc.actions.ConfigureTeamNumberBasicAction
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.settings.FrcApplicationSettings


private val LOG = Logger.getInstance(FrcNotifications::class.java)

private  val notificationKey = FrcNotificationsTracker.NotificationKey.ConfigureTeamNumberQuery

fun notifyAboutTeamNumberNeedingToBeConfigured(project: Project?, useSticky: Boolean, asWarning: Boolean): Notification
{
    //val notificationMapForProject = FrcProjectNotificationsTracker.getNotificationMapForProject(project)
    var notification: Notification? = FrcNotificationsTracker.getNotification(project, notificationKey)

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
        FrcNotificationsTracker.putNotification(project, notificationKey, notification)
        Notifications.Bus.notify(notification, project)
    }
    return notification
}

fun notifyToConfigureTeamNumIfNecessary(project: Project, knownFacetedProject: Boolean)
{
    val settings = FrcApplicationSettings.getInstance()

    val shouldNotify =
            !settings.isTeamNumberConfigured()
            &&
            ((knownFacetedProject || project.isFrcFacetedProject()) || settings.prc <= FrcPluginGlobals.TEAM_NUM_NOTIFY_RUN_COUNT_PROJECT_LEVEL_NON_FRC_PROJECT)
            &&
            FrcNotificationsTracker.getNotification(project, notificationKey) == null

    if (shouldNotify)
    {
        LOG.debug("[FRC] Publishing 'configure team number' notification for Project '$project'")
        // Expire any application level notification to prevent duplicate notification in the event log
        FrcNotificationsTracker.expireAppNotification(notificationKey)
        val notification = notifyAboutTeamNumberNeedingToBeConfigured(project, true, false)
        FrcNotificationsTracker.putNotification(project, notificationKey, notification)
    }
}

private fun createConfigureTeamNotification(project: Project?, useSticky: Boolean, asWarning: Boolean): Notification
{
    val subtitle = if (asWarning) "Team number not Set" else "Configuration needed"
    val contentPrefix = if (asWarning) "Without your FRC team number being set, robot deploys will fail. " else ""
    val content = contentPrefix + "Please <a href='configure'>configure</a> your FRC Team Number."
    val icon = if (asWarning) FrcNotifications.IconWarn else FrcNotifications.IconInfo
    val notificationType = if (asWarning) NotificationType.WARNING else NotificationType.INFORMATION

    val notificationGroup = if (useSticky) FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP else FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP

    return Notification(notificationGroup.displayId, FrcNotifications.Title, content, notificationType).apply {
        this.icon = icon
        this.subtitle = subtitle
        setListener{ theNotification, event ->
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
    }
}

fun expireConfigureTeamNumberNotification(project: Project?)
{
    FrcNotificationsTracker.expireNotification(project, FrcNotificationsTracker.NotificationKey.ConfigureTeamNumberQuery)
}