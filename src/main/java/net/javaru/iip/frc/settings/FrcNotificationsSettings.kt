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

package net.javaru.iip.frc.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "FrcPluginNotifications", storages = [Storage("frc.xml")])
data class FrcNotificationsSettings(var appLevelConfigureTeamNumNotifyCount: Int = 0
                                 ) : PersistentStateComponent<FrcNotificationsSettings>
{
    companion object Settings
    {
        @JvmStatic
        fun getInstance(): FrcNotificationsSettings = service()

        fun clone(original: FrcNotificationsSettings): FrcNotificationsSettings
        {
            return original.copy()
        }
    }

    private val LOG = Logger.getInstance(FrcNotificationsSettings::class.java)

    override fun getState(): FrcNotificationsSettings
    {
        LOG.trace("[FRC] FrcNotificationsSettings.getState() called. Returning current state of: ${toString()}")
        return this
    }

    override fun loadState(state: FrcNotificationsSettings)
    {
        LOG.trace("[FRC] FrcNotificationsSettings.loadState() called with state object of: $state")
        XmlSerializerUtil.copyBean<FrcNotificationsSettings>(state, this)
    }
}
