/*
 * Copyright 2015-2018 the original author or authors
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

import com.intellij.icons.AllIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.notification.NotificationsConfiguration
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.actions.ConfigureTeamNumberBasicAction
import net.javaru.iip.frc.i18n.FrcBundle
import net.javaru.iip.frc.settings.FrcApplicationSettings
import java.util.*
import javax.swing.Icon
import javax.swing.event.HyperlinkEvent


@Suppress("unused")
enum class FrcNotificationType(val group: NotificationGroup, val notificationType: NotificationType, val icon: Icon)
{ 
    GENERAL_INFO(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP, NotificationType.INFORMATION, FrcNotifications.IconInfo), 
    GENERAL_WARN(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP, NotificationType.WARNING, FrcNotifications.IconWarn), 
    GENERAL_ERROR(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP, NotificationType.ERROR, FrcNotifications.IconError), 
    ACTIONABLE_INFO(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP, NotificationType.INFORMATION, FrcNotifications.IconInfo),
    ACTIONABLE_WARN(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP, NotificationType.WARNING, FrcNotifications.IconWarn),
    ACTIONABLE_ERROR(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP, NotificationType.ERROR, FrcNotifications.IconError)
}
/**
 * Also see [net.javaru.iip.frc.components.FrcProjectComponentImpl] for some notification methods.
 */
// NOTE: This class is registered in the plugin.xml as an ApplicationService
class FrcNotifications private constructor()
{

    init
    {
        LOG.debug("[FRC] Registering FRC Notification Groups")
        NotificationsConfiguration.getNotificationsConfiguration().register(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP.displayId,
                                                                            NotificationDisplayType.BALLOON,
                                                                            true)
        NotificationsConfiguration.getNotificationsConfiguration().register(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP.displayId,
                                                                            NotificationDisplayType.STICKY_BALLOON,
                                                                            true)
    }

    companion object
    {
        private val LOG = Logger.getInstance(FrcNotifications::class.java)


        val FRC_GENERAL_NOTIFICATION_GROUP = NotificationGroup(FrcBundle.message("frc.notifications.group.name.general"),
                                                               NotificationDisplayType.BALLOON,
                                                               true)

        val FRC_ACTIONABLE_NOTIFICATION_GROUP = NotificationGroup(FrcBundle.message("frc.notifications.group.name.actionable"),
                                                                  NotificationDisplayType.STICKY_BALLOON,
                                                                  true)

        val IconInfo: Icon = AllIcons.General.BalloonInformation
        val IconWarn: Icon = AllIcons.General.BalloonWarning
        val IconError: Icon = AllIcons.General.BalloonError

        const val Title = "FRC"


        private val configureTeamNumberNotifications = HashMap<String, Notification>()


        //TODO: Move to FrcProjectComponentImpl and then move the notificationsMap placement into this method
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
            val icon = if (asWarning) IconWarn else IconInfo
            val notificationType = if (asWarning) NotificationType.WARNING else NotificationType.INFORMATION

            val notificationGroup = if (useSticky) FRC_ACTIONABLE_NOTIFICATION_GROUP else FRC_GENERAL_NOTIFICATION_GROUP
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
            //        return notificationGroup.createNotification(FrcNotifications.Title,
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
        
        private fun createProjectKey(project: Project?) : String
        {
            return if (project != null)
                "${project.name}--${project.basePath}"
            else 
                "null-project"
        }

        /**
         * @sample notifyExampleUsage
         */
        fun notify(type: FrcNotificationType, content: String, subTitle: String? = null, project: Project? = null, listener: NotificationListener? = null): Notification
        {
            val notification = Notification(type.group.displayId,
                                            type.icon,
                                            Title,
                                            subTitle,
                                            content,
                                            type.notificationType,
                                            listener)
            Notifications.Bus.notify(notification, project)
            return notification
        }

        /**
         * @sample notifyExampleUsage
         */
        fun notify(type: FrcNotificationType, content: String, subTitle: String? = null, project: Project? = null, listener: (notification: Notification, event: HyperlinkEvent) -> Unit): Notification
        {
            return notify(type, content, subTitle, project, listener)
        }

        @Suppress("ObjectLiteralToLambda")
        private fun notifyExampleUsage()
        {
            // Most Idiomatic
            notify(FrcNotificationType.ACTIONABLE_ERROR, "<a href='open'>Click</a> to open item.", "Open?", null) { _, event ->
                if (event.description == "open") { /* Action code goes here */ }
            }

            
            // OK
            notify(FrcNotificationType.ACTIONABLE_ERROR, "<a href='open'>Click</a> to open item.", "Open?", null, NotificationListener { _, event ->
                if (event.description == "open") { /* Action code goes here */ } 
            })

            
            // Least Idiomatic - Object could be converted to a lambda to give the second option, then that lambda could be moved outside the parameters to get the first 
            notify(FrcNotificationType.ACTIONABLE_ERROR, "<a href='open'>Click</a> to open item.", "Open?", null, object : NotificationListener
            {
                override fun hyperlinkUpdate(notification: Notification, event: HyperlinkEvent)
                {
                    if (event.description == "open") { /* Action code goes here */ }
                }
            })

        }
    }

}
