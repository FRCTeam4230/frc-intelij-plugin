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
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.Balloon
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


object FrcSharedNotificationTracker
{
    private val trackingMap: ConcurrentMap<UUID, ConcurrentMap<Project, FrcSharedNotification>> = ConcurrentHashMap()

    fun add(uuidKey: UUID, project: Project, notification: Notification)
    {
        add(FrcSharedNotification(uuidKey, project, notification, null))
    }

    fun add(uuidKey: UUID, project: Project, balloonResult: BalloonResult)
    {
        add(FrcSharedNotification(uuidKey, project, balloonResult.notification, balloonResult.balloon))
    }

    fun add(frcSharedNotification: FrcSharedNotification)
    {
        frcSharedNotification.apply {
            trackingMap.getOrPut(uuidKey) { ConcurrentHashMap<Project, FrcSharedNotification>() }[project] = this
        }
    }

    fun expireAll(uuidKey: UUID)
    {
        val projectsBeingClosed = trackingMap[uuidKey]?.filterKeys { !it.isOpen }
        val noProjectIsBeingClosed = projectsBeingClosed?.isEmpty() ?: false
        if (noProjectIsBeingClosed)
        {
            trackingMap.remove(uuidKey)?.forEach {
                it.value?.notification?.expire()
                it.value?.balloon?.dispose()
            }
        }
        else
        {
            projectsBeingClosed?.forEach {
                it.value?.notification?.expire()
                it.value?.balloon?.dispose()
                trackingMap[it.value.uuidKey]?.remove(it.value.project)
            }
        }
    }
}

data class FrcSharedNotification(val uuidKey: UUID, val project: Project, val notification: Notification, val balloon: Balloon?)