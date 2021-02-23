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

import com.intellij.icons.AllIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationListener
import com.intellij.notification.NotificationType
import com.intellij.notification.NotificationsConfiguration
import com.intellij.notification.impl.NotificationsManagerImpl
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.ui.popup.LightweightWindowEvent
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.BalloonLayoutData
import com.intellij.ui.awt.RelativePoint
import icons.FrcIcons
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.i18n.FrcBundle.message
import net.javaru.iip.frc.i18n.FrcBundle.messageNullable
import net.javaru.iip.frc.i18n.FrcMessageKey
import net.javaru.iip.frc.util.getParentDisposable
import org.intellij.lang.annotations.Language
import java.awt.Point
import java.util.*
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
                However, this expires the notification. If that is not desired, use our own
                URL_OPENING_LISTENER_NO_EXPIRE
 */

val URL_OPENING_LISTENER_NO_EXPIRE = NotificationListener.UrlOpeningListener(false)

@Suppress("unused")
enum class FrcNotificationType(val group: NotificationGroup, val notificationType: NotificationType, val icon: Icon?)
{
    GENERAL_INFO(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP, NotificationType.INFORMATION, FrcNotifications.IconInfo),
    GENERAL_WARN(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP, NotificationType.WARNING, FrcNotifications.IconWarn),
    GENERAL_ERROR(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP, NotificationType.ERROR, FrcNotifications.IconError),
    ACTIONABLE_INFO(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP, NotificationType.INFORMATION, FrcNotifications.IconInfo),
    ACTIONABLE_INFO_WITH_FRC_ICON(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP, NotificationType.INFORMATION, FrcIcons.FRC.FIRST_ICON_MEDIUM_16),
    ACTIONABLE_WARN(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP, NotificationType.WARNING, FrcNotifications.IconWarn),
    ACTIONABLE_ERROR(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP, NotificationType.ERROR, FrcNotifications.IconError),
    BUILD__INFO(FrcNotifications.FRC_BUILD_TOOL_WINDOW_GROUP, NotificationType.INFORMATION, icon = null),
    BUILD__WARN(FrcNotifications.FRC_BUILD_TOOL_WINDOW_GROUP, NotificationType.WARNING, icon = null),
    BUILD__ERROR(FrcNotifications.FRC_BUILD_TOOL_WINDOW_GROUP, NotificationType.ERROR, icon = null),
    ;

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

fun Notification.notifyViaBalloonForFrc(project: Project?, hideOnClickOutside: Boolean = false): BalloonResult
{
    return BalloonResult(this, FrcNotifications.showBalloon(this, project, hideOnClickOutside))
}

fun Notification.notifyFluent(project: Project?): Notification
{
    this.notify(project)
    return this
}

data class BalloonResult(val notification: Notification, val balloon: Balloon?)

@Suppress("unused")
object FrcNotifications
{
    private val LOG = Logger.getInstance(FrcNotifications::class.java)

    const val Title = "FRC"

    @JvmStatic
    val FRC_GENERAL_NOTIFICATION_GROUP = NotificationGroupManager.getInstance().getNotificationGroup(/*id from plugin.xml notificationsGroup extension point*/"FRC General Notifications")!!

    @JvmStatic
    val FRC_ACTIONABLE_NOTIFICATION_GROUP = NotificationGroupManager.getInstance().getNotificationGroup(/*id from plugin.xml notificationsGroup extension point*/ "FRC Important or Actionable Notifications")!!

    @JvmStatic
    val FRC_BUILD_TOOL_WINDOW_GROUP = NotificationGroupManager.getInstance().getNotificationGroup("FRC Build Tool Window Notifications")!!

    @JvmStatic
    val IconInfo: Icon = AllIcons.General.BalloonInformation

    @JvmStatic
    val IconWarn: Icon = AllIcons.General.BalloonWarning

    @JvmStatic
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

    // region == notify ==

    /**
     * Creates and shows -- i.e. calls `notification.notify(project)` -- a notification. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notify` function takes Strings and an (optional) `NotificationListener` instance.
     *
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
     * Creates and shows -- i.e. calls `notification.notify(project)` -- a notification. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notify` function takes Strings and an (optional) `NotificationListener` instance.
     *
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
     * Creates and shows -- i.e. calls `notification.notify(project)` -- a notification. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notify` function takes FrcMessageKeys and an (optional) `NotificationListener` instance.
     *
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
     * Creates and shows -- i.e. calls `notification.notify(project)` -- a notification. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notify` function takes FrcMessageKeys and an (optional) `NotificationListener` lambda.
     *
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

    /**
     * Creates and shows -- i.e. calls `notification.notify(project)` -- a notification. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notify` function takes Strings and an (optional) `NotificationListener` instance.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notifyWithActions(
        type: FrcNotificationType,
        content: String,
        subTitle: String? = null,
        project: Project? = null,
        vararg actions: NotificationAction = emptyArray()
              ): Notification
    {
        val notification = createNotification(type, content, subTitle)
        actions.forEach { notification.addAction(it) }
        notification.notify(project)
        return notification
    }

    /**
     * Creates and shows -- i.e. calls `notification.notify(project)` -- a notification. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notify` function takes FrcMessageKeys and an (optional) `NotificationListener` instance.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notifyWithActions(
        type: FrcNotificationType,
        contentKey: FrcMessageKey,
        subTitleKey: FrcMessageKey? = null,
        project: Project? = null,
        vararg actions: NotificationAction = emptyArray()
              ): Notification
    {
        val notification = createNotification(type, contentKey, subTitleKey)
        actions.forEach { notification.addAction(it) }
        notification.notify(project)
        return notification
    }



    /**
     * Creates and shows -- i.e. calls `notification.notify(project)` -- a notification. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notify` function takes Strings and an (optional) `NotificationListener` instance.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notifyNoTitle(
        type: FrcNotificationType,
        content: String,
        project: Project? = null,
        listener: NotificationListener? = null
              ): Notification
    {
        val notification = createNotificationNoTitle(type, content, listener)
        notification.notify(project)
        return notification
    }

    /**
     * Creates and shows -- i.e. calls `notification.notify(project)` -- a notification. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notify` function takes Strings and an (optional) `NotificationListener` instance.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notifyNoTitle(
        type: FrcNotificationType,
        content: String,
        project: Project? = null,
        listener: (notification: Notification, event: HyperlinkEvent) -> Unit
              ): Notification
    {
        val notification = createNotificationNoTitle(type, content, listener)
        notification.notify(project)
        return notification
    }

    /**
     * Creates and shows -- i.e. calls `notification.notify(project)` -- a notification. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notify` function takes FrcMessageKeys and an (optional) `NotificationListener` instance.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notifyNoTitle(
        type: FrcNotificationType,
        contentKey: FrcMessageKey,
        project: Project? = null,
        listener: NotificationListener? = null
              ): Notification
    {
        val notification = createNotificationNoTitle(type, contentKey, listener)
        notification.notify(project)
        return notification
    }

    /**
     * Creates and shows -- i.e. calls `notification.notify(project)` -- a notification. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notify` function takes FrcMessageKeys and an (optional) `NotificationListener` lambda.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notifyNoTitle(
        type: FrcNotificationType,
        contentKey: FrcMessageKey,
        project: Project? = null,
        listener: (notification: Notification, event: HyperlinkEvent) -> Unit
              ): Notification
    {
        val notification = createNotificationNoTitle(type, contentKey, listener)
        notification.notify(project)
        return notification
    }
    // endregion == notify ==


    // region == notifyAllFrcProjects ==


    /**
     * Creates and shows -- i.e. calls `notification.notify(project)` -- a set of notifications, one for each open
     * FRC project, and displays then. A `whenExpired` lambda is added to the notifications so that when one of them
     * is expired, they are all expired. Use one of the `createNotification()` functions as the notification creation
     * lambda. The UUID is used as a key in the [FrcSharedNotificationTracker]. The notifications are removed
     * upon expiring.
     */
    fun notifyAllOpenProjects(uuidKey: UUID = UUID.randomUUID(),
                              notifyFrcProjectsOnly: Boolean = true,
                              createNotificationFunction: () -> Notification): UUID
    {
        ProjectManager.getInstance().openProjects.filter {
            if (notifyFrcProjectsOnly)
                it.isFrcFacetedProject()
            else
                true
        }.forEach {
            FrcSharedNotificationTracker.add(uuidKey, it,
                                             createNotificationFunction.invoke()
                                                 .whenExpired { FrcSharedNotificationTracker.expireAll(uuidKey) }
                                                 .notifyFluent(it))
        }
        return uuidKey
    }

    /**
     * Creates and shows -- i.e. calls `notification.notify(project)` -- a set of Balloon notifications, one for each open
     * FRC project, and displays then. A `whenExpired` lambda is added to the notifications so that when one of them
     * is expired, they are all expired. Use one of the `createNotification()` functions as the notification creation
     * lambda. The UUID is used as a key in the [FrcSharedNotificationTracker]. The notifications are removed
     * upon expiring.
     *
     * @sample notifyBalloonAllOpenProjectsExample
     */
    fun notifyBalloonAllOpenProjects(uuidKey: UUID = UUID.randomUUID(),
                                     notifyFrcProjectsOnly: Boolean = true,
                                     createNotificationFunction: () -> Notification): UUID
    {
        ProjectManager.getInstance().openProjects.filter {
            if (notifyFrcProjectsOnly)
                 it.isFrcFacetedProject()
            else
                true
        }.forEach {
            val balloonResult = createNotificationFunction.invoke()
                .whenExpired { FrcSharedNotificationTracker.expireAll(uuidKey) }
                .notifyViaBalloonForFrc(it)
            FrcSharedNotificationTracker.add(uuidKey, it, balloonResult)
            balloonResult.balloon?.addListener(object : JBPopupListener {
                override fun onClosed(event: LightweightWindowEvent)
                {
                    FrcSharedNotificationTracker.expireAll(uuidKey)
                }
            })
        }
        return uuidKey
    }

    // endregion == notifyAllFrcProjects ==


    // region == createNotification ==

    /**
     * Creates, but does *not* show a notification. The caller will be responsible for queuing up (i.e. showing) the notification
     * by calling `Notification.notify()` Prior to that, the caller can make changes such as adding a
     * `whenExpired` listener, or setting `isImportant(true)`.
     *
     * This version of the `notify` function takes Strings and an (optional) `NotificationListener` instance.
     *
     * @sample notificationExamples
     */
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
     * Creates, but does *not* show a notification. The caller will be responsible for queuing up (i.e. showing) the notification
     * by calling `Notification.notify()` Prior to that, the caller can make changes such as adding a
     * `whenExpired` listener, or setting `isImportant(true)`.
     *
     * This version of the `notify` function takes Strings and an (optional) `NotificationListener` lambda.
     *
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


    /**
     * Creates, but does *not* show a notification. The caller will be responsible for queuing up (i.e. showing) the notification
     * by calling `Notification.notify()` Prior to that, the caller can make changes such as adding a
     * `whenExpired` listener, or setting `isImportant(true)`.
     *
     * This version of the `notify` function takes FrcMessageKeys and an (optional) `NotificationListener` instance.
     *
     * @sample notificationExamples
     */
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


    /**
     * Creates, but does *not* show a notification. The caller will be responsible for queuing up (i.e. showing) the notification
     * by calling `Notification.notify()` Prior to that, the caller can make changes such as adding a
     * `whenExpired` listener, or setting `isImportant(true)`.
     *
     * This version of the `notify` function takes FrcMessageKeys and an (optional) `NotificationListener` lambda.
     *
     * @sample notificationExamples
     */
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


    /**
     * Creates, but does *not* show a notification. The caller will be responsible for queuing up (i.e. showing) the notification
     * by calling `Notification.notify()` Prior to that, the caller can make changes such as adding a
     * `whenExpired` listener, or setting `isImportant(true)`.
     *
     * This version of the `notify` function takes Strings and an (optional) `NotificationListener` instance.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun createNotificationNoTitle(
        type: FrcNotificationType,
        content: String,
        listener: NotificationListener? = null
                          ): Notification
    {
        return Notification(
            type.group.displayId,
            type.icon,
            null,
            null,
            content,
            type.notificationType,
            listener
                           )
    }


    /**
     * Creates, but does *not* show a notification. The caller will be responsible for queuing up (i.e. showing) the notification
     * by calling `Notification.notify()` Prior to that, the caller can make changes such as adding a
     * `whenExpired` listener, or setting `isImportant(true)`.
     *
     * This version of the `notify` function takes Strings and an (optional) `NotificationListener` lambda.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    fun createNotificationNoTitle(
        type: FrcNotificationType,
        content: String,
        listener: (notification: Notification, event: HyperlinkEvent) -> Unit
                          ): Notification
    {
        return Notification(
            type.group.displayId,
            type.icon,
            null,
            null,
            content,
            type.notificationType,
            listener
                           )

    }


    /**
     * Creates, but does *not* show a notification. The caller will be responsible for queuing up (i.e. showing) the notification
     * by calling `Notification.notify()` Prior to that, the caller can make changes such as adding a
     * `whenExpired` listener, or setting `isImportant(true)`.
     *
     * This version of the `notify` function takes FrcMessageKeys and an (optional) `NotificationListener` instance.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun createNotificationNoTitle(
        type: FrcNotificationType,
        contentKey: FrcMessageKey,
        listener: NotificationListener? = null
                          ): Notification
    {
        return Notification(
            type.group.displayId,
            type.icon,
            null,
           null,
            message(contentKey),
            type.notificationType,
            listener
                           )
    }


    /**
     * Creates, but does *not* show a notification. The caller will be responsible for queuing up (i.e. showing) the notification
     * by calling `Notification.notify()` Prior to that, the caller can make changes such as adding a
     * `whenExpired` listener, or setting `isImportant(true)`.
     *
     * This version of the `notify` function takes FrcMessageKeys and an (optional) `NotificationListener` lambda.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    fun createNotificationNoTitle(
        type: FrcNotificationType,
        contentKey: FrcMessageKey,
        listener: (notification: Notification, event: HyperlinkEvent) -> Unit
                          ): Notification
    {
        return Notification(
            type.group.displayId,
            type.icon,
            null,
            null,
            message(contentKey),
            type.notificationType,
            listener
                           )

    }

    // endregion createNotification

    // region == notifyBalloon ==

    /**
     * Creates and shows a Balloon  notification. It is recommended for the content to be HTML. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notifyBalloon` function takes Strings and an (optional) `NotificationListener` instance.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notifyBalloon(type: FrcNotificationType,
                      @Language("HTML") content: String,
                      subTitle: String? = null,
                      project: Project? = null,
                      listener: NotificationListener? = null): Notification
    {
        val notification = createNotification(type, content, subTitle, listener)
        showBalloon(notification, project)
        return notification
    }

    /**
     * Creates and shows a Balloon  notification. It is recommended for the content to be HTML. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notifyBalloon` function takes Strings and an (optional) `NotificationListener` instance.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notifyBalloon(type: FrcNotificationType,
                      @Language("HTML") content: String,
                      subTitle: String? = null,
                      project: Project? = null,
                      listener: (notification: Notification, event: HyperlinkEvent) -> Unit): BalloonResult
    {
        val notification = createNotification(type, content, subTitle, listener)
        return BalloonResult(notification, showBalloon(notification, project))
    }

    /**
     * Creates and shows a Balloon  notification. It is recommended for the content to be HTML. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notifyBalloon` function takes FrcMessageKeys and an (optional) `NotificationListener` instance.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notifyBalloon(type: FrcNotificationType,
                      contentKey: FrcMessageKey,
                      subTitleKey: FrcMessageKey? = null,
                      project: Project? = null,
                      listener: NotificationListener? = null): BalloonResult
    {
        val notification = createNotification(type, contentKey, subTitleKey, listener)
        return BalloonResult(notification, showBalloon(notification, project))
    }

    /**
     * Creates and shows a Balloon  notification. It is recommended for the content to be HTML. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notifyBalloon` function takes FrcMessageKeys and an (optional) `NotificationListener` lambda.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notifyBalloon(type: FrcNotificationType,
                      contentKey: FrcMessageKey,
                      subTitleKey: FrcMessageKey? = null,
                      project: Project? = null,
                      listener: (notification: Notification, event: HyperlinkEvent) -> Unit): BalloonResult
    {
        val notification = createNotification(type, contentKey, subTitleKey, listener)
        return BalloonResult(notification, showBalloon(notification, project))
    }

    // endregion notifyBalloon

    // region == notifyInfoBalloon ==

    /**
     * Creates and shows an Information Balloon notification. It is recommended for the content to be HTML. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notifyBalloon` function takes Strings and an (optional) `NotificationListener` instance.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notifyInfoBalloon(@Language("HTML") content: String,
                          subTitle: String? = null,
                          project: Project? = null,
                          listener: NotificationListener? = null): BalloonResult
    {
        val notification = createNotification(FrcNotificationType.ACTIONABLE_INFO, content, subTitle, listener)
        return BalloonResult(notification, showBalloon(notification, project))
    }

    /**
     * Creates and shows an Information Balloon notification. It is recommended for the content to be HTML. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notifyBalloon` function takes Strings and an (optional) `NotificationListener` instance.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notifyInfoBalloon(@Language("HTML") content: String,
                          subTitle: String? = null,
                          project: Project? = null,
                          listener: (notification: Notification, event: HyperlinkEvent) -> Unit): BalloonResult
    {
        val notification = createNotification(FrcNotificationType.ACTIONABLE_INFO, content, subTitle, listener)
        return BalloonResult(notification, showBalloon(notification, project))
    }

    /**
     * Creates and shows an Information Balloon notification. It is recommended for the content to be HTML. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notifyBalloon` function takes FrcMessageKeys and an (optional) `NotificationListener` instance.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notifyInfoBalloon(contentKey: FrcMessageKey,
                          subTitleKey: FrcMessageKey? = null,
                          project: Project? = null,
                          listener: NotificationListener? = null): BalloonResult
    {
        val notification = createNotification(FrcNotificationType.ACTIONABLE_INFO, contentKey, subTitleKey, listener)
        return BalloonResult(notification, showBalloon(notification, project))
    }

    /**
     * Creates and shows an Information Balloon notification. It is recommended for the content to be HTML. The notification is returned in
     * case the caller needs access to the notification for future actions such as calling `notification.expire()`. In the
     * event you want to add a `whenExpired` listener, it is recommended to instead use the corresponding [createNotification]
     * method, add the when expired listener, and then call `notification.notify(project)`. This is shown in the examples.
     *
     * This version of the `notifyBalloon` function takes FrcMessageKeys and an (optional) `NotificationListener` lambda.
     *
     * @sample notificationExamples
     */
    @JvmStatic
    @JvmOverloads
    fun notifyInfoBalloon(contentKey: FrcMessageKey,
                          subTitleKey: FrcMessageKey? = null,
                          project: Project? = null,
                          listener: (notification: Notification, event: HyperlinkEvent) -> Unit): BalloonResult
    {
        val notification = createNotification(FrcNotificationType.ACTIONABLE_INFO, contentKey, subTitleKey, listener)
        return BalloonResult(notification, showBalloon(notification, project))
    }

    // endregion notifyInfoBalloon

    // region == misc ==

    @JvmStatic
    @JvmOverloads
    fun showBalloon(notification: Notification, project: Project?, hideOnClickOutside: Boolean = false): Balloon?
    {
        val frame = WindowManager.getInstance().getIdeFrame(project)
        return if (frame == null)
        {
            notification.notify(project)
            null
        }
        else
        {
            val bounds = frame.component.bounds
            val target = RelativePoint(frame.component, Point(bounds.x + bounds.width, 20))

            try
            {
                val balloon = NotificationsManagerImpl.createBalloon(frame,
                                                                     notification,
                                                                     true,
                                                                     hideOnClickOutside,
                                                                     BalloonLayoutData.fullContent(),
                                                                     project.getParentDisposable())
                balloon.show(target, Balloon.Position.atLeft)
                balloon
            }
            catch (t: Throwable)
            {
                LOG.warn("[FRC] Could not display balloon. Cause details: $t", t)
                notification.notify(project)
                null
            }
        }
    }

    // endregion misc

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

        // You can also add NotificationAction (abstract class) actions. These appear below the notifications as a series of links
        val myNotification = createNotification(FrcNotificationType.ACTIONABLE_INFO, content = "Some message")

        val myActionA = object : NotificationAction("Link text")
        {
            override fun actionPerformed(e: AnActionEvent, notification: Notification)
            {
                notification.expire()
                // Do work here
            }
        }
        val myActionB = object : NotificationAction("Don't show again")
        {
            override fun actionPerformed(e: AnActionEvent, notification: Notification)
            {
                notification.expire()
                // Update setting so this message is not shown again
            }
        }

        myNotification.addAction(myActionA)
        myNotification.addAction(myActionB)
        myNotification.notify(project)


        // We can also add Actions in a fluent/builder way
        createNotification(FrcNotificationType.ACTIONABLE_INFO, content = "Some message")
            .addAction(object : NotificationAction("Link text")
                       {
                           override fun actionPerformed(e: AnActionEvent, notification: Notification)
                           {
                               notification.expire()
                               // Do work here
                           }
                       }).addAction(object : NotificationAction("Don't show again")
                                    {
                                        override fun actionPerformed(e: AnActionEvent, notification: Notification)
                                        {
                                            notification.expire()
                                            // Update setting so this message is not shown again
                                        }
                                    }).notify(project)
    }

    private fun notifyBalloonAllOpenProjectsExample(contentFrcMessageKey: FrcMessageKey, subtitleFrcMessageKey: FrcMessageKey)
    {
        notifyBalloonAllOpenProjects(notifyFrcProjectsOnly = true) {
            createNotification(
                FrcNotificationType.ACTIONABLE_INFO_WITH_FRC_ICON,
                contentFrcMessageKey, // Alternatively supply an HTML String
                subtitleFrcMessageKey // Alternatively supply a String
                              )
        }
    }
}