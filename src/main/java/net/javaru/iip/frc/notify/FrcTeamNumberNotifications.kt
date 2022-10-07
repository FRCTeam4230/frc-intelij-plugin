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

package net.javaru.iip.frc.notify

import com.intellij.ide.util.RunOnceUtil
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.FrcPluginGlobals
import net.javaru.iip.frc.actions.ConfigureTeamNumberBasicAction
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.i18n.FrcBundle.message
import net.javaru.iip.frc.settings.FrcApplicationSettings
import net.javaru.iip.frc.util.frcPluginVersion


private val logger = logger<FrcNotifications>()

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
            ((knownFacetedProject || project.isFrcFacetedProject()))
            &&
            FrcNotificationsTracker.getNotification(project, notificationKey) == null

    if (shouldNotify)
    {
        logger.debug {"[FRC] Publishing 'configure team number' notification for Project '$project'"}
        // Expire any application level notification to prevent duplicate notification in the event log
        FrcNotificationsTracker.expireAppNotification(notificationKey)
        val notification = notifyAboutTeamNumberNeedingToBeConfigured(project, useSticky = true, asWarning = false)
        FrcNotificationsTracker.putNotification(project, notificationKey, notification)
    }
    else if(!knownFacetedProject && !project.isFrcFacetedProject())
    {
        RunOnceUtil.runOnceForApp("configureFrcTeamNumber-$frcPluginVersion") {
            FrcNotificationsTracker.expireAppNotification(notificationKey)
            val notification = notifyAboutTeamNumberNeedingToBeConfigured(project, useSticky = true, asWarning = false)
            FrcNotificationsTracker.putNotification(project, notificationKey, notification)
        }
    }
}

private fun createConfigureTeamNotification(project: Project?, useSticky: Boolean, asWarning: Boolean): Notification
{
    val title = message("frc.team.number.notification.title")
    val warningClause = if (asWarning) message("frc.team.number.notification.warning.clause") else ""
    val content = message("frc.wpilib.root.path.not.found.user.notification.content", warningClause)
    val actionText = message("frc.team.number.notification.action.text")
    val frcNotifyType = when
    {
        asWarning && useSticky -> FrcNotifyType.ACTIONABLE_WARN
        asWarning && !useSticky -> FrcNotifyType.GENERAL_WARN
        !asWarning && useSticky -> FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON
        else -> FrcNotifyType.GENERAL_INFO_WITH_FRC_ICON
    }
    
    return frcNotifyType.builder()
        .withContent(content)
        .withFrcPrefixedTitle(title)
        .withNoSubTitle()
        .withAction(actionText) {event, notification ->
            
            ConfigureTeamNumberBasicAction.openConfigureTeamNumberDialog(event.getData(CommonDataKeys.PROJECT) ?: project)
            if (FrcApplicationSettings.getInstance().isTeamNumberConfigured())
            {
                notification.expire()
            }
        }
        .noMoreActions()
        .build()
}

fun expireConfigureTeamNumberNotification(project: Project?)
{
    FrcNotificationsTracker.expireNotification(project, FrcNotificationsTracker.NotificationKey.ConfigureTeamNumberQuery)
}