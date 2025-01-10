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

import com.intellij.icons.AllIcons
import com.intellij.notification.Notification
import com.intellij.notification.NotificationDisplayType
import com.intellij.notification.NotificationGroup
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.notification.NotificationsConfiguration
import com.intellij.notification.impl.NotificationsManagerImpl
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.wm.WindowManager
import com.intellij.ui.BalloonLayoutData
import com.intellij.ui.awt.RelativePoint
import icons.FrcIcons
import net.javaru.iip.frc.util.getParentDisposable
import java.awt.Point
import javax.swing.Icon


val LOG = logger<FrcNotifications>()



@JvmOverloads
fun Notification.notifyViaBalloon(project: Project?, hideOnClickOutside: Boolean = false): BalloonResult
{
    val frame = WindowManager.getInstance().getIdeFrame(project)
    return if (frame == null)
    {
        this.notify(project)
        BalloonResult(this, null)
    } 
    else
    {
        val bounds = frame.component.bounds
        val target = RelativePoint(frame.component, Point(bounds.x + bounds.width, 20))

        try
        {
            val balloon = NotificationsManagerImpl.createBalloon(
                frame, this, true, hideOnClickOutside, BalloonLayoutData.fullContent(), project.getParentDisposable()
                                                                )
            balloon.show(target, Balloon.Position.atLeft)
            BalloonResult(this, balloon)
        } 
        catch (t: Throwable)
        {
            LOG.warn("[FRC] Could not display balloon. Cause details: $t", t)
            this.notify(project)
            BalloonResult(this, null)
        }
    }
}

/** Calls `notify()` on the Notification and also returns the Notification to allow use in a fluent programming chain. */
fun Notification.notifyFluent(project: Project?): Notification = this.also { this.notify(project) }

data class BalloonResult(val notification: Notification, val balloon: Balloon?)

@Suppress("unused")
enum class FrcNotifyType(val group: NotificationGroup, val notificationType: NotificationType, val icon: Icon?) : FrcNotificationsBuilder.ContentStep
{
    GENERAL_INFO(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP, NotificationType.INFORMATION, FrcNotifications.IconInfo), GENERAL_INFO_WITH_FRC_ICON(
    FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP,
    NotificationType.INFORMATION,
    FrcNotifications.IconFrc
                                                                                                                                                      ),
    GENERAL_WARN(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP, NotificationType.WARNING, FrcNotifications.IconWarn), GENERAL_ERROR(
    FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP,
    NotificationType.ERROR,
    FrcNotifications.IconError
                                                                                                                                     ),
    ACTIONABLE_INFO(
        FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP,
        NotificationType.INFORMATION,
        FrcNotifications.IconInfo
                   ),
    ACTIONABLE_INFO_WITH_FRC_ICON(
        FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP,
        NotificationType.INFORMATION,
        FrcNotifications.IconFrc
                                 ),
    ACTIONABLE_WARN(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP, NotificationType.WARNING, FrcNotifications.IconWarn), ACTIONABLE_ERROR(
    FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP,
    NotificationType.ERROR,
    FrcNotifications.IconError
                                                                                                                                              ),
    ERROR_REPORT_SUBMITTER(FrcNotifications.FRC_ERROR_REPORT_NOTIFICATION_GROUP, NotificationType.INFORMATION, FrcNotifications.IconFrc), BUILD__INFO(
    FrcNotifications.FRC_BUILD_TOOL_WINDOW_GROUP,
    NotificationType.INFORMATION,
    icon = null
                                                                                                                                                     ),
    BUILD__INFO_WITH_ICON(
        FrcNotifications.FRC_BUILD_TOOL_WINDOW_GROUP,
        NotificationType.INFORMATION,
        icon = FrcNotifications.IconInfo
                         ),
    BUILD__INFO_WITH_FRC_ICON(FrcNotifications.FRC_BUILD_TOOL_WINDOW_GROUP, NotificationType.INFORMATION, icon = FrcNotifications.IconFrc), BUILD__WARN(
    FrcNotifications.FRC_BUILD_TOOL_WINDOW_GROUP,
    NotificationType.WARNING,
    icon = null
                                                                                                                                                       ),
    BUILD__WARN_WITH_ICON(FrcNotifications.FRC_BUILD_TOOL_WINDOW_GROUP, NotificationType.WARNING, icon = FrcNotifications.IconWarn), BUILD__ERROR(
    FrcNotifications.FRC_BUILD_TOOL_WINDOW_GROUP,
    NotificationType.ERROR,
    icon = null
                                                                                                                                                 ),
    BUILD__ERROR_WITH_ICON(FrcNotifications.FRC_BUILD_TOOL_WINDOW_GROUP, NotificationType.ERROR, icon = FrcNotifications.IconError),

    RUN_TOOL_WINDOW__INFO(FrcNotifications.FRC_RUN_TOOL_WINDOW_GROUP, NotificationType.INFORMATION, icon = null), RUN_TOOL_WINDOW__INFO_WITH_ICON(
    FrcNotifications.FRC_RUN_TOOL_WINDOW_GROUP,
    NotificationType.INFORMATION,
    icon = FrcNotifications.IconInfo
                                                                                                                                                 ),
    RUN_TOOL_WINDOW__INFO_WITH_FRC_ICON(
        FrcNotifications.FRC_RUN_TOOL_WINDOW_GROUP,
        NotificationType.INFORMATION,
        icon = FrcNotifications.IconFrc
                                       ),
    RUN_TOOL_WINDOW__WARN(FrcNotifications.FRC_RUN_TOOL_WINDOW_GROUP, NotificationType.WARNING, icon = null), RUN_TOOL_WINDOW__WARN_WITH_ICON(
    FrcNotifications.FRC_RUN_TOOL_WINDOW_GROUP,
    NotificationType.WARNING,
    icon = FrcNotifications.IconWarn
                                                                                                                                             ),
    RUN_TOOL_WINDOW__ERROR(FrcNotifications.FRC_RUN_TOOL_WINDOW_GROUP, NotificationType.ERROR, icon = null), RUN_TOOL_WINDOW__ERROR_WITH_ICON(
    FrcNotifications.FRC_RUN_TOOL_WINDOW_GROUP,
    NotificationType.ERROR,
    icon = FrcNotifications.IconError
                                                                                                                                             ),

;


    /**
     * Creates and returns an FRC Notification Step Builder for building (and potentially displaying) notifications.
     */
    @Suppress("unused")
    fun builder(): FrcNotificationsBuilder.ContentStep = FrcNotificationsBuilder.builder().withFrcType(this)

    override fun withContent(content: String): FrcNotificationsBuilder.TitleStep = builder().withContent(content)
}

@Suppress("unused")
object FrcNotifications
{
    const val FrcTitle = "FRC"

    @JvmStatic
    val FRC_GENERAL_NOTIFICATION_GROUP 
        get () = NotificationGroupManager.getInstance().getNotificationGroup(/*id from plugin.xml notificationsGroup extension point*/"FRC General Notifications")!!

    @JvmStatic
    val FRC_ACTIONABLE_NOTIFICATION_GROUP
        get () = NotificationGroupManager.getInstance().getNotificationGroup(/*id from plugin.xml notificationsGroup extension point*/ "FRC Important or Actionable Notifications")!!

    @JvmStatic
    val FRC_ERROR_REPORT_NOTIFICATION_GROUP
        get () =NotificationGroupManager.getInstance().getNotificationGroup(/*id from plugin.xml notificationsGroup extension point*/ "FRC Error Submitter Notifications")!!

    @JvmStatic
    val FRC_BUILD_TOOL_WINDOW_GROUP
        get () = NotificationGroupManager.getInstance().getNotificationGroup(/*id from plugin.xml notificationsGroup extension point*/ "FRC Build Tool Window Notifications")!!

    @JvmStatic
    val FRC_RUN_TOOL_WINDOW_GROUP
        get () = NotificationGroupManager.getInstance().getNotificationGroup(/*id from plugin.xml notificationsGroup extension point*/ "FRC Run Tool Window Notifications")!!

    @JvmStatic
    val IconInfo: Icon = AllIcons.General.BalloonInformation

    @JvmStatic
    val IconWarn: Icon = AllIcons.General.BalloonWarning

    @JvmStatic
    val IconError: Icon = AllIcons.General.BalloonError

    @JvmStatic
    val IconFrc: Icon = FrcIcons.FRC.FIRST_ICON_MEDIUM_16


    init
    {
        LOG.debug {"[FRC] Registering FRC Notification Groups"}
        NotificationsConfiguration.getNotificationsConfiguration().register(FRC_GENERAL_NOTIFICATION_GROUP.displayId,
                                                                            NotificationDisplayType.BALLOON,
                                                                            true)
        NotificationsConfiguration.getNotificationsConfiguration().register(FRC_ACTIONABLE_NOTIFICATION_GROUP.displayId,
                                                                            NotificationDisplayType.STICKY_BALLOON,
                                                                            true)
    }
    
    fun builder(): FrcNotificationsBuilder.TypeStep = FrcNotificationsBuilder.builder()

}