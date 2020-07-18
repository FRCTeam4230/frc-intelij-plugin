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

import com.intellij.icons.AllIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.notification.NotificationsConfiguration
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.i18n.FrcBundle.message
import net.javaru.iip.frc.i18n.FrcBundle.messageNullable
import net.javaru.iip.frc.i18n.FrcMessageKey
import javax.swing.Icon
import javax.swing.event.HyperlinkEvent

/*
    Notification Notes:

        SDK Docs: https://www.jetbrains.org/intellij/sdk/docs/user_interface_components/notifications.html

        Per class level Javadoc comment for NotificationListener interface:
            'NotificationAction' should be be considered instead of "action" links in HTML content.
                NotificationActions add links below the content/message (i.e. links are not inline)
                Use Notification.addAction(AnAction) to add actions
            Use NotificationListener.URL_OPENING_LISTENER to open external links in browser
 */


@Suppress("unused")
enum class FrcNotificationType(val group: NotificationGroup, val notificationType: NotificationType, val icon: Icon)
{
    GENERAL_INFO(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP, NotificationType.INFORMATION, FrcNotifications.IconInfo),
    GENERAL_WARN(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP, NotificationType.WARNING, FrcNotifications.IconWarn),
    GENERAL_ERROR(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP, NotificationType.ERROR, FrcNotifications.IconError),
    ACTIONABLE_INFO(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP, NotificationType.INFORMATION, FrcNotifications.IconInfo),
    ACTIONABLE_WARN(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP, NotificationType.WARNING, FrcNotifications.IconWarn),
    ACTIONABLE_ERROR(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP, NotificationType.ERROR, FrcNotifications.IconError);

    @JvmOverloads
    fun notify(content: String,
               subTitle: String? = null,
               project: Project? = null,
               listener: NotificationListener? = null): Notification = FrcNotifications.notify(this, content, subTitle, project, listener)

    @JvmOverloads
    fun notify(content: String,
               subTitle: String? = null,
               project: Project? = null,
               listener: (notification: Notification, event: HyperlinkEvent) -> Unit): Notification = FrcNotifications.notify(this, content, subTitle, project, listener)

    @JvmOverloads
    fun createNotification(content: String,
                           subTitle: String? = null,
                           listener: NotificationListener? = null): Notification = FrcNotifications.createNotification(this, content, subTitle, listener)

    @JvmOverloads
    fun createNotification(content: String,
                           subTitle: String? = null,
                           listener: (notification: Notification, event: HyperlinkEvent) -> Unit): Notification = FrcNotifications.createNotification(this, content, subTitle, listener)


}

object FrcNotifications
{
    private val LOG = Logger.getInstance(FrcNotifications::class.java)

    const val Title = "FRC"

    @JvmStatic
    val FRC_GENERAL_NOTIFICATION_GROUP = NotificationGroup(message("frc.notifications.group.name.general"),
                                                           NotificationDisplayType.BALLOON,
                                                           true)

    @JvmStatic
    val FRC_ACTIONABLE_NOTIFICATION_GROUP = NotificationGroup(message("frc.notifications.group.name.actionable"),
                                                              NotificationDisplayType.STICKY_BALLOON,
                                                              true)
    val IconInfo: Icon = AllIcons.General.BalloonInformation
    val IconWarn: Icon = AllIcons.General.BalloonWarning
    val IconError: Icon = AllIcons.General.BalloonError


    init
    {
        LOG.debug("[FRC] Registering FRC Notification Groups")
        NotificationsConfiguration.getNotificationsConfiguration().register(FRC_GENERAL_NOTIFICATION_GROUP.displayId,
                                                                            NotificationDisplayType.BALLOON,
                                                                            true)
        NotificationsConfiguration.getNotificationsConfiguration().register(FRC_ACTIONABLE_NOTIFICATION_GROUP.displayId,
                                                                            NotificationDisplayType.STICKY_BALLOON,
                                                                            true)
    }


    /**
     * Creates and shows -- i.e. calls `notification.notify(project)` -- a notification. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notify(type: FrcNotificationType,
               content: String,
               subTitle: String? = null,
               project: Project? = null,
               listener: NotificationListener? = null): Notification
    {
        val notification = createNotification(type, content, subTitle, listener)
        notification.notify(project)
        return notification
    }

    /**
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notify(type: FrcNotificationType,
               content: String,
               subTitle: String? = null,
               project: Project? = null,
               listener: (notification: Notification, event: HyperlinkEvent) -> Unit): Notification
    {
        val notification = createNotification(type, content, subTitle, listener)
        notification.notify(project)
        return notification
    }

    /**
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notify(type: FrcNotificationType,
               contentKey: FrcMessageKey,
               subTitleKey: FrcMessageKey? = null,
               project: Project? = null,
               listener: NotificationListener? = null): Notification
    {
        val notification = createNotification(type, contentKey, subTitleKey, listener)
        notification.notify(project)
        return notification
    }

    /**
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notify(type: FrcNotificationType,
               contentKey: FrcMessageKey,
               subTitleKey: FrcMessageKey? = null,
               project: Project? = null,
               listener: (notification: Notification, event: HyperlinkEvent) -> Unit): Notification
    {
        val notification = createNotification(type, contentKey, subTitleKey, listener)
        notification.notify(project)
        return notification
    }


    @JvmStatic
    @JvmOverloads
    fun createNotification(type: FrcNotificationType,
                           content: String,
                           subTitle: String? = null,
                           listener: NotificationListener? = null): Notification
    {
        return Notification(type.group.displayId,
                            type.icon,
                            Title,
                            subTitle,
                            content,
                            type.notificationType,
                            listener)
    }

    /**
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun createNotification(type: FrcNotificationType,
                           content: String,
                           subTitle: String? = null,
                           listener: (notification: Notification, event: HyperlinkEvent) -> Unit): Notification
    {
        return Notification(type.group.displayId,
                            type.icon,
                            Title,
                            subTitle,
                            content,
                            type.notificationType,
                            listener)

    }

    @JvmStatic
    @JvmOverloads
    fun createNotification(type: FrcNotificationType,
                           contentKey: FrcMessageKey,
                           subTitleKey: FrcMessageKey? = null,
                           listener: NotificationListener? = null): Notification
    {
        return Notification(type.group.displayId,
                            type.icon,
                            Title,
                            messageNullable(subTitleKey),
                            message(contentKey),
                            type.notificationType,
                            listener)
    }


    @JvmStatic
    @JvmOverloads
    fun createNotification(type: FrcNotificationType,
                           contentKey: FrcMessageKey,
                           subTitleKey: FrcMessageKey? = null,
                           listener: (notification: Notification, event: HyperlinkEvent) -> Unit): Notification
    {
        return Notification(type.group.displayId,
                            type.icon,
                            Title,
                            messageNullable(subTitleKey),
                            message(contentKey),
                            type.notificationType,
                            listener)

    }




    @Suppress("ObjectLiteralToLambda", "ControlFlowWithEmptyBody")
    private fun notificationExamples(project: Project, logger: Logger)
    {

        // Notification with Web Link, using NotificationListener URL_OPENING_LISTENER
        notify(FrcNotificationType.ACTIONABLE_INFO,
               "More info <a href='https://google.com'>here</a>.",
               "My Subtitle",
               project,
               NotificationListener.URL_OPENING_LISTENER)

        // Notification with Web Link, using NotificationListener URL_OPENING_LISTENER
        notify(FrcNotificationType.ACTIONABLE_INFO,
               content = "More info <a href='https://google.com'>here</a>.",
               project = project,
               listener = NotificationListener.URL_OPENING_LISTENER)


        // Listener ex 1: Most Idiomatic
        notify(FrcNotificationType.ACTIONABLE_ERROR,
               "<a href='showDialog'>Click</a> to show the dialog.",
               "Open Dialog?",
               project) { notification, event ->
            if (event.description == "showDialog")
            {
                notification.expire()
                /* Action code for 'showDialog' goes here */
            }
        }


        // Listener ex 2: OK, but the lambda could/should be moved outside the
        //                the parameters parenthesis to be more idiomatic
        notify(FrcNotificationType.ACTIONABLE_ERROR,
               "<a href='showDialog'>Click</a> to show the dialog.",
               "Open Dialog?", project,
               NotificationListener { notification, event ->
                   if (event.description == "showDialog")
                   {
                       notification.expire()
                       /* Action code goes here */
                   }
               })


        // Listener ex 3: Least Idiomatic - Anonymous Object could be converted to a lambda to give the second option,
        //                then that lambda could be moved outside the parameters to get the first
        notify(FrcNotificationType.ACTIONABLE_ERROR,
               "<a href='showDialog'>Click</a> to show the dialog.",
               "Open Dialog?",
               project,
               object : NotificationListener
               {
                   override fun hyperlinkUpdate(notification: Notification, event: HyperlinkEvent)
                   {
                       if (event.description == "showDialog")
                       {
                           notification.expire()
                           /* Action code goes here */
                       }
                   }
               })

        // Listener ex 4: Multiple Actions
        notify(FrcNotificationType.ACTIONABLE_ERROR,
               "Do you want to go <a href='this'>this</a>,  <a href='that'>this</a>, or the <a href='other'>other</a> thing?.",
               "Open Dialog?",
               project) { notification, event ->
            notification.expire()
            when (event.description)
            {
                "this"  -> logger.debug("Do 'this' action")
                "that"  -> logger.debug("Do 'that' action")
                "other" -> logger.debug("Do 'other' action")
            }
        }


        // Adding a when expired listener
        createNotification(FrcNotificationType.ACTIONABLE_INFO, content = "Some message")
            .whenExpired { logger.debug("When expired action code would go here") }
            .notify(project)


        // Adding a when expired listener along with a NotificationListener
        createNotification(FrcNotificationType.ACTIONABLE_INFO,
                           "<a href='showDialog'>Click</a> to show the dialog.") { notification, event ->
            if (event.description == "showDialog")
            {
                notification.expire()
                /* Action code for 'showDialog' goes here */
            }

        }.whenExpired {
            logger.debug("When expired action code would go here")
        }
            .notify(project)

    }
}