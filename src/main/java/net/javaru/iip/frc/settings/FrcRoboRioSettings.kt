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

@file:Suppress("FunctionName")

package net.javaru.iip.frc.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
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

@Suppress("EnumEntryName")
enum class RoboRioAddressType
{
    /** The DNS (aka LAN) address for the roboRIO. For example, for team 1234  it would be `roborio-1234-FRC.lan`. */
    DNS,
    /** mDNS, or multicast Domain Name System, address for the roboRIO. For example, for team 1234 it would be `roborio-1234-FRC.local`. */
    mDNS,
    /** The FieldLocal address for the roboRIO. For example, for team 1234  it would be `roborio-1234-FRC.frc-field.local`. */
    FieldLocal,
    /** The "TE.AM IP Notation" IP address for the roboRIO. For example, for team 1234 it would be `10.12.34.2` */
    IP,
    /** The USB address for the roboRIO, which is always `172.22.11.2`. */
    USB;
}

/**
 * The project level settings for the RoboRio.
 * To get an instance, use the `getInstance(Project)` function.
 */
// NOTE: This class is registered as an <projectService> in the plugin.xml
@State(name = "FrcRoboRio", storages = [(Storage("frc/frc.xml"))])
data class FrcRoboRioSettings @JvmOverloads constructor(
        var roboRioHostRawMDns: String = USE_DEFAULT_HOST_PLACEHOLDER,
        var roboRioHostRawDns: String = USE_DEFAULT_HOST_PLACEHOLDER,
        var roboRioHostRawIp: String = USE_DEFAULT_HOST_PLACEHOLDER,
        var roboRioHostRawUsb: String = USE_DEFAULT_HOST_PLACEHOLDER,
        var roboRioHostRawFieldLocal: String = USE_DEFAULT_HOST_PLACEHOLDER
                                                       ) : PersistentStateComponent<FrcRoboRioSettings>
{
    
    @Suppress("unused", "MemberVisibilityCanPrivate")
    companion object Settings
    {
        @JvmStatic
        fun getInstance(project: Project): FrcRoboRioSettings
        {
            val settings = project.service<FrcRoboRioSettings>()
            settings.project = project
            return settings
        }
        

        @JvmStatic
        fun clone(original: FrcRoboRioSettings): FrcRoboRioSettings = original.copy()

        fun createRoboRioAddressDefault(teamNumber: Int, addressType: RoboRioAddressType): String
        {
            return when(addressType)
            {
                RoboRioAddressType.DNS -> createRoboRioHostDefault_DNS(teamNumber)
                RoboRioAddressType.FieldLocal -> createRoboRioHostDefault_FieldLocal(teamNumber)
                RoboRioAddressType.IP -> createRoboRioHostDefault_IP(teamNumber)
                RoboRioAddressType.mDNS -> createRoboRioHostDefault_mDNS(teamNumber)
                RoboRioAddressType.USB -> createRoboRioHostDefault_USB()
            }
        }
        
        fun createRoboRioHostDefault_USB(): String = ROBORIO_HOST_USB_DEFAULT
        fun createRoboRioHostDefault_mDNS(teamNumber: Int): String = String.format(ROBORIO_HOST_mDNS_TEMPLATE, teamNumber)
        fun createRoboRioHostDefault_DNS(teamNumber: Int): String = String.format(ROBORIO_HOST_DNS_TEMPLATE, teamNumber)
        fun createRoboRioHostDefault_FieldLocal(teamNumber: Int): String = String.format(ROBORIO_HOST_FIELD_LOCAL_TEMPLATE, teamNumber)
        fun createRoboRioHostDefault_IP(teamNumber: Int): String
        {
            val high = teamNumber / 100
            val low = teamNumber % 100
            return String.format(ROBORIO_HOST_IP_TEMPLATE, high, low)
        }
    }

    private var project: Project? = null
    
    private val logger = logger<FrcRoboRioSettings>()

    override fun getState(): FrcRoboRioSettings
    {
        logger.trace {"[FRC] FrcRoboRioSettings.getState() called. Returning current state of: ${toString()}"}
        return this
    }

    override fun loadState(state: FrcRoboRioSettings)
    {
        logger.trace {"[FRC] FrcRoboRioSettings.loadState() called with state object of: $state"}
        XmlSerializerUtil.copyBean(state, this)
    }

    // The project should never be null since it is immediately set when an instance is obtained. 
    // But just in case we default to the Application level team number since in 90% of the time that will be OK
    private fun teamNumber() = if (project != null) FrcProjectTeamNumberService.getInstance(project!!).teamNumber else FrcApplicationSettings.getInstance().teamNumber
    
    // NOTE: These helper methods break normal naming conventions by using 
    //       some snake_case rather than solely conventional camelCase.
    //       This is to make the multitude of similarly named functions
    //       (especially in the case of mDNS and DNS) more discernible
    //       as a preventative measure to reduce the risk of bugs

    // ==== mDNS Host Helpers ====

    @com.intellij.util.xmlb.annotations.Transient
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

    @com.intellij.util.xmlb.annotations.Transient
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

    @JvmOverloads
    @com.intellij.util.xmlb.annotations.Transient
    fun getRoboRioHostDefault_mDNS(roboRioTeamNumber: Int = teamNumber()): String = createRoboRioHostDefault_mDNS(roboRioTeamNumber)


    @com.intellij.util.xmlb.annotations.Transient
    fun isRoboRioHostTheDefault_mDNS(): Boolean = USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawMDns

    // ==== DNS Host Helpers ====

    @com.intellij.util.xmlb.annotations.Transient
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

    @com.intellij.util.xmlb.annotations.Transient
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

    @JvmOverloads
    @com.intellij.util.xmlb.annotations.Transient
    fun getRoboRioHostDefault_DNS(roboRioTeamNumber: Int = teamNumber()): String = createRoboRioHostDefault_DNS(roboRioTeamNumber)


    @com.intellij.util.xmlb.annotations.Transient
    fun isRoboRioHostTheDefault_DNS(): Boolean = USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawDns


    // ==== FieldLocal Host Helpers ====

    @com.intellij.util.xmlb.annotations.Transient
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

    @com.intellij.util.xmlb.annotations.Transient
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

    @JvmOverloads
    @com.intellij.util.xmlb.annotations.Transient
    fun getRoboRioHostDefault_FieldLocal(roboRioTeamNumber: Int = teamNumber()): String = createRoboRioHostDefault_FieldLocal(roboRioTeamNumber)

    @com.intellij.util.xmlb.annotations.Transient
    fun isRoboRioHostTheDefault_FieldLocal(): Boolean = USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawFieldLocal


    // ==== IP Host Helpers ====

    @com.intellij.util.xmlb.annotations.Transient
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

    @com.intellij.util.xmlb.annotations.Transient
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

    @com.intellij.util.xmlb.annotations.Transient
    fun getRoboRioHost_IP_asIpByteArray(): ByteArray
    {
        return getRoboRioHost_IP().ipAddressToByteArray()
    }

    @JvmOverloads
    @com.intellij.util.xmlb.annotations.Transient
    fun getRoboRioHostDefault_IP(roboRioTeamNumber: Int = teamNumber()): String = createRoboRioHostDefault_IP(roboRioTeamNumber)

    @com.intellij.util.xmlb.annotations.Transient
    fun isRoboRioHostTheDefault_IP(): Boolean = USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawIp

    // ==== USB Host Helpers ====

    @com.intellij.util.xmlb.annotations.Transient
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

    @com.intellij.util.xmlb.annotations.Transient
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

    @com.intellij.util.xmlb.annotations.Transient
    fun getRoboRioHost_USB_asIpByteArray(): ByteArray
    {
        return getRoboRioHost_USB().ipAddressToByteArray()
    }

    @com.intellij.util.xmlb.annotations.Transient
    fun getRoboRioHostDefault_USB(): String = ROBORIO_HOST_USB_DEFAULT

    @com.intellij.util.xmlb.annotations.Transient
    fun isRoboRioHostTheDefault_USB(): Boolean = USE_DEFAULT_HOST_PLACEHOLDER == roboRioHostRawUsb
}

@ConsistentCopyVisibility
data class ImmutableFrcRoboRioSettings internal constructor(val roboRioHostRawMDns: String,
                                                            val roboRioHostRawDns: String,
                                                            val roboRioHostRawIp: String,
                                                            val roboRioHostRawUsb: String,
                                                            val roboRioHostRawFieldLocal: String)
