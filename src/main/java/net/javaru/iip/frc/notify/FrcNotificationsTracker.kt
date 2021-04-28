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

@file:Suppress("MemberVisibilityCanBePrivate")

package net.javaru.iip.frc.notify

import com.intellij.notification.Notification
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import java.util.*
import kotlin.collections.HashMap


object FrcNotificationsTracker
{

    private val logger = logger<FrcNotificationsTracker>()

    enum class NotificationKey
    {
        ConfigureTeamNumberQuery, AttachWpiLibQuery, AttachUserLibQuery, DownloadNewWpiLibVersionQuery, LatestWpiLibIsBeingDownloaded
    }
    private val trackingMap: MutableMap<Project, MutableMap<NotificationKey, Notification>> = HashMap()
    private val nullProjectTrackingMap: MutableMap<NotificationKey, Notification> = EnumMap(NotificationKey::class.java)

    private fun getNotificationMapForProject(project: Project?): MutableMap<NotificationKey, Notification>
    {
        return if (project == null) nullProjectTrackingMap
        else trackingMap.computeIfAbsent(project) { EnumMap(NotificationKey::class.java) }
    }

    private fun getApplicationLevelMap(): MutableMap<NotificationKey, Notification> = nullProjectTrackingMap

    @JvmStatic
    fun getNotification(project: Project?, notificationKey: NotificationKey): Notification? = getNotificationMapForProject(project)[notificationKey]

    @JvmStatic
    fun getAppNotification(notificationKey: NotificationKey): Notification? = getNotification(null, notificationKey)

    @JvmStatic
    fun putNotification(project: Project?, notificationKey: NotificationKey, notification: Notification)
    {
        removeNotification(project, notificationKey, expireNotification = true)
        getNotificationMapForProject(project)[notificationKey] = notification
    }

    @JvmStatic
    fun putAppNotification(notificationKey: NotificationKey, notification: Notification)
    {
        removeAppNotification(notificationKey, expireNotification = true)
        getApplicationLevelMap()[notificationKey] = notification
    }

    @JvmStatic
    @JvmOverloads
    fun removeNotification(project: Project?, notificationKey: NotificationKey, expireNotification: Boolean = true)
    {
        val map = getNotificationMapForProject(project)
        val notification = map.remove(notificationKey)
        if (expireNotification)
        {
            notification?.expire()
        }
    }

    @JvmStatic
    @JvmOverloads
    fun removeAppNotification(notificationKey: NotificationKey, expireNotification: Boolean = true)
    {
        val map = getApplicationLevelMap()
        val notification = map.remove(notificationKey)
        if (expireNotification)
        {
            notification?.expire()
        }
    }

    @JvmStatic
    @JvmOverloads
    fun expireNotification(project: Project?, notificationKey: NotificationKey, removeFromTracking: Boolean = true)
    {
        getNotification(project, notificationKey)?.expire()
        if (removeFromTracking)
        {
            removeNotification(project, notificationKey, false)
        }
    }

    @JvmStatic
    @JvmOverloads
    fun expireAppNotification(notificationKey: NotificationKey, removeFromTracking: Boolean = true)
    {
        getAppNotification(notificationKey)?.expire()
        if (removeFromTracking)
        {
            removeAppNotification(notificationKey, false)
        }
    }

    @JvmStatic
    fun clearAllForProject(project: Project?)
    {
        val map = getNotificationMapForProject(project)
        try
        {
            map.forEach {
                try
                {
                    it.value.expire()
                }
                catch (t: Throwable)
                {
                    logger.warn("[FRC] Could not expire notification (project = $project). Cause: $t", t)
                }
            }

        }
        catch (t: Throwable)
        {
            logger.warn("[FRC] An exception occurred when expiring all notifications for project $project. Cause: $t", t)
        }
        finally
        {
            try
            {
                map.clear()
            }
            catch (t: Throwable)
            {
                logger.warn("[FRC] An exception occurred when clearing notifications map for project $project. Cause: $t", t)
            }
        }
    }

    @JvmStatic
    fun clearAllForApp()
    {
        clearAllForProject(null)
    }


}