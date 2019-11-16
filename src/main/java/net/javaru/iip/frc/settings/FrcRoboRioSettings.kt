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

@file:Suppress("FunctionName")

package net.javaru.iip.frc.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import net.javaru.iip.frc.util.ipAddressToByteArray


@Suppress("ConstPropertyName") // the lower case m makes it more discernible from the DNS one
private const val ROBORIO_HOST_mDNS_TEMPLATE = "roborio-%d-FRC.local"
private const val ROBORIO_HOST_DNS_TEMPLATE = "roborio-%d-FRC.lan"
private const val ROBORIO_HOST_FIELD_LOCAL_TEMPLATE = "roborio-%d-FRC.frc-field.local"
private const val ROBORIO_HOST_IP_TEMPLATE = "10.%d.%d.2"
private const val ROBORIO_HOST_USB_DEFAULT = "172.22.11.2"
private const val USE_DEFAULT_HOST_PLACEHOLDER = "<<<Use Default Host>>>"


/**
 * The project level settings for the RoboRio.
 * To get an instance, use the `getInstance(Project)` function.
 */
// NOTE: This class is registered as an <projectService> in the plugin.xml
@State(name = "FrcRoboRio", storages = [(Storage("frc/frc.xml"))])
data class FrcRoboRioSettings @JvmOverloads constructor(
        private val project: Project,
        var roboRioHostRawMDns: String = USE_DEFAULT_HOST_PLACEHOLDER,
        var roboRioHostRawDns: String = USE_DEFAULT_HOST_PLACEHOLDER,
        var roboRioHostRawIp: String = USE_DEFAULT_HOST_PLACEHOLDER,
        var roboRioHostRawUsb: String = USE_DEFAULT_HOST_PLACEHOLDER,
        var roboRioHostRawFieldLocal: String = USE_DEFAULT_HOST_PLACEHOLDER
                                                       ) : PersistentStateComponent<FrcRoboRioSettings>
{
    init
    {
        // TODO need to listen for applied team number changes, modify settings, and then publish URL/IP address change 
    }
    
    @Suppress("unused", "MemberVisibilityCanPrivate")
    companion object Settings
    {
        @JvmStatic
        fun getInstance(project: Project): FrcRoboRioSettings = ServiceManager.getService(project, FrcRoboRioSettings::class.java)
        
        @JvmStatic
        fun getImmutableInstance(project: Project): ImmutableFrcRoboRioSettings
        {
            val (_, roboRioHostRawMDns, roboRioHostRawDns, roboRioHostRawIp, roboRioHostRawUsb, roboRioHostRawFieldLocal) = getInstance(project)
            return ImmutableFrcRoboRioSettings(roboRioHostRawMDns, roboRioHostRawDns, roboRioHostRawIp, roboRioHostRawUsb, roboRioHostRawFieldLocal)
        }

        @JvmStatic
        fun clone(original: FrcRoboRioSettings): FrcRoboRioSettings = original.copy()
        
    }


    private val LOG = Logger.getInstance(FrcRoboRioSettings::class.java)

    override fun getState(): FrcRoboRioSettings
    {
        LOG.trace("[FRC] FrcRoboRioSettings.getState() called. Returning current state of: " + toString())
        return this
    }

    override fun loadState(state: FrcRoboRioSettings)
    {
        LOG.trace("[FRC] FrcRoboRioSettings.loadState() called with state object of: $state")
        XmlSerializerUtil.copyBean(state, this)
    }

    
    // NOTE: These helper methods break normal naming conventions by using 
    //       some snake_case rather than solely conventional camelCase.
    //       This is to make the multitude of similarly named functions
    //       (especially in the case of mDNS and DNS) more discernible
    //       as a preventative measure to reduce the risk of bugs

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

    fun getRoboRioHostDefault_mDNS(frcProjectGeneralSettings: FrcProjectGeneralSettings = FrcProjectGeneralSettings.getInstance(project)): String = createRoboRioHostDefault_mDNS(frcProjectGeneralSettings.teamNumber)

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

    fun getRoboRioHostDefault_DNS(frcProjectGeneralSettings: FrcProjectGeneralSettings = FrcProjectGeneralSettings.getInstance(project)): String = createRoboRioHostDefault_DNS(frcProjectGeneralSettings.teamNumber)

    fun createRoboRioHostDefault_DNS(teamNumber: Int): String = String.format(ROBORIO_HOST_DNS_TEMPLATE, teamNumber)

    fun isRoboRioHostTheDefault_DNS(): Boolean = USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawDns


    // ==== FieldLocal Host Helpers ====

    fun setRoboRioHost_FieldLocal(roboRioHostFieldLocal: String)
    {
        roboRioHostRawFieldLocal =
                if (getRoboRioHostDefault_FieldLocal() == roboRioHostFieldLocal)
                {
                    USE_DEFAULT_HOST_PLACEHOLDER
                }
                else
                {
                    roboRioHostFieldLocal
                }
    }

    fun getRoboRioHost_FieldLocal(): String
    {
        return if (USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawFieldLocal)
        {
            getRoboRioHostDefault_FieldLocal()
        }
        else
        {
            roboRioHostRawFieldLocal
        }
    }

    fun getRoboRioHostDefault_FieldLocal(frcProjectGeneralSettings: FrcProjectGeneralSettings = FrcProjectGeneralSettings.getInstance(project)): String = createRoboRioHostDefault_FieldLocal(frcProjectGeneralSettings.teamNumber)

    fun createRoboRioHostDefault_FieldLocal(teamNumber: Int): String = String.format(ROBORIO_HOST_FIELD_LOCAL_TEMPLATE, teamNumber)

    fun isRoboRioHostTheDefault_FieldLocal(): Boolean = USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawFieldLocal


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

    fun getRoboRioHost_IP_asIpByteArray(): ByteArray
    {
        return getRoboRioHost_IP().ipAddressToByteArray()
    }

    fun getRoboRioHostDefault_IP(frcProjectGeneralSettings: FrcProjectGeneralSettings = FrcProjectGeneralSettings.getInstance(project)): String = createRoboRioHostDefault_IP(frcProjectGeneralSettings.teamNumber)

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

    fun getRoboRioHost_USB_asIpByteArray(): ByteArray
    {
        return getRoboRioHost_USB().ipAddressToByteArray()
    }
    
    fun getRoboRioHostDefault_USB(): String = ROBORIO_HOST_USB_DEFAULT


    fun isRoboRioHostTheDefault_USB(): Boolean = USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawUsb
}

data class ImmutableFrcRoboRioSettings internal constructor(val roboRioHostRawMDns: String,
                                                            val roboRioHostRawDns: String,
                                                            val roboRioHostRawIp: String,
                                                            val roboRioHostRawUsb: String,
                                                            val roboRioHostRawFieldLocal: String)
