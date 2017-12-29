/*
 * Copyright 2015-2017 Mark Vedder
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

@file:Suppress("FunctionName")

package net.javaru.iip.frc.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.xmlb.XmlSerializerUtil


private const val ROBORIO_HOST_mDNS_TEMPLATE = "roborio-%d-FRC.local"
private const val ROBORIO_HOST_DNS_TEMPLATE = "roborio-%d-FRC.lan"
private const val ROBORIO_HOST_IP_TEMPLATE = "10.%d.%d.2"
private const val ROBORIO_HOST_USB_DEFAULT = "172.22.11.2"
private const val USE_DEFAULT_HOST_PLACEHOLDER = "<<<Use Default Host>>>"


enum class RoboRioConnectionType
{
    mDNS,
    DNS,
    USB,
    IP
}

// NOTE: This class is registered as an <applicationService> in the plugin.xml
@State(name = "FrcRoboRio", storages = [(Storage("frc.xml"))])
data class FrcRoboRioSettings(var roboRioHostRawMDns: String = USE_DEFAULT_HOST_PLACEHOLDER,
                              var roboRioHostRawDns: String = USE_DEFAULT_HOST_PLACEHOLDER,
                              var roboRioHostRawIp: String = USE_DEFAULT_HOST_PLACEHOLDER,
                              var roboRioHostRawUsb: String = USE_DEFAULT_HOST_PLACEHOLDER
                             ) : PersistentStateComponent<FrcRoboRioSettings>
{
    @Suppress("unused", "MemberVisibilityCanPrivate")
    companion object Settings
    {
        fun INSTANCE(): FrcRoboRioSettings
        {
            return ServiceManager.getService(FrcRoboRioSettings::class.java)
        }

        fun clone(original: FrcRoboRioSettings): FrcRoboRioSettings
        {
            return original.copy()
        }
    }


    private val LOG = Logger.getInstance(FrcRoboRioSettings::class.java)

    override fun getState(): FrcRoboRioSettings
    {
        LOG.trace("[FRC] FrcRoboRioSettings.getState() called. Returning current state of: " + toString())
        return this
    }

    override fun loadState(state: FrcRoboRioSettings)
    {
        LOG.trace("[FRC] FrcRoboRioSettings.loadState() called with state object of: " + state)
        XmlSerializerUtil.copyBean(state, this)
    }


    // ==== mDNS Host Helpers ====

    fun setRoboRioHost_mDNS(roboRioHostMDns: String)
    {
        roboRioHostRawMDns =
                if (getRoboRioHostDefault_mDNS() == roboRioHostMDns)
                {
                    USE_DEFAULT_HOST_PLACEHOLDER
                }
                else
                {
                    roboRioHostMDns
                }
    }

    fun getRoboRioHost_mDNS(): String
    {
        return if (USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawMDns)
        {
            getRoboRioHostDefault_mDNS()
        }
        else
        {
            roboRioHostRawMDns
        }
    }

    fun getRoboRioHostDefault_mDNS(): String = createRoboRioHostDefault_mDNS(FrcApplicationSettings.INSTANCE().teamNumber)

    fun createRoboRioHostDefault_mDNS(teamNumber: Int): String = String.format(ROBORIO_HOST_mDNS_TEMPLATE, teamNumber)

    fun isRoboRioHostTheDefault_mDNS(): Boolean = USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawMDns

    // ==== DNS Host Helpers ====

    fun setRoboRioHost_DNS(roboRioHostDns: String)
    {
        roboRioHostRawDns =
                if (getRoboRioHostDefault_DNS() == roboRioHostDns)
                {
                    USE_DEFAULT_HOST_PLACEHOLDER
                }
                else
                {
                    roboRioHostDns
                }
    }

    fun getRoboRioHost_DNS(): String
    {
        return if (USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawDns)
        {
            getRoboRioHostDefault_DNS()
        }
        else
        {
            roboRioHostRawDns
        }
    }

    fun getRoboRioHostDefault_DNS(): String = createRoboRioHostDefault_DNS(FrcApplicationSettings.INSTANCE().teamNumber)

    fun createRoboRioHostDefault_DNS(teamNumber: Int): String = String.format(ROBORIO_HOST_DNS_TEMPLATE, teamNumber)

    fun isRoboRioHostTheDefault_DNS(): Boolean = USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawDns


    // ==== IP Host Helpers ====

    fun setRoboRioHost_IP(roboRioHostIp: String)
    {
        roboRioHostRawIp =
                if (getRoboRioHostDefault_IP() == roboRioHostIp)
                {
                    USE_DEFAULT_HOST_PLACEHOLDER
                }
                else
                {
                    roboRioHostIp
                }
    }

    fun getRoboRioHost_IP(): String
    {
        return if (USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawIp)
        {
            getRoboRioHostDefault_IP()
        }
        else
        {
            roboRioHostRawIp
        }
    }

    fun getRoboRioHostDefault_IP(): String = createRoboRioHostDefault_IP(FrcApplicationSettings.INSTANCE().teamNumber)

    fun createRoboRioHostDefault_IP(teamNumber: Int): String
    {
        val high = teamNumber / 100
        val low = teamNumber % 100
        return String.format(ROBORIO_HOST_IP_TEMPLATE, high, low)
    }

    fun isRoboRioHostTheDefault_IP(): Boolean = USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawIp

    // ==== USB Host Helpers ====

    fun setRoboRioHost_USB(roboRioHostUsb: String)
    {
        roboRioHostRawUsb =
                if (getRoboRioHostDefault_USB() == roboRioHostUsb)
                {
                    USE_DEFAULT_HOST_PLACEHOLDER
                }
                else
                {
                    roboRioHostUsb
                }
    }

    fun getRoboRioHost_USB(): String
    {
        return if (USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawUsb)
        {
            getRoboRioHostDefault_USB()
        }
        else
        {
            roboRioHostRawUsb
        }
    }

    fun getRoboRioHostDefault_USB(): String = ROBORIO_HOST_USB_DEFAULT


    fun isRoboRioHostTheDefault_USB(): Boolean = USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawUsb
}