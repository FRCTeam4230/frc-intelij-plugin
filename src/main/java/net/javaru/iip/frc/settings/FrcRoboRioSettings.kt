/*
 * Copyright 2015-2019 the original author or authors
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
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
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
        /* TODO: the team number needs to be removed as a property of this data class  as it should always be whats in the wpilib_preferences.json file which the FrcProjectTeamNumberService manages*/
        @com.intellij.util.xmlb.annotations.Transient var teamNumber: Int = FrcApplicationSettings.getInstance().teamNumber,
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
            settings.teamNumber = FrcProjectTeamNumberService.getInstance(project).teamNumber
            return settings
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
    fun getRoboRioHostDefault_mDNS(roboRioTeamNumber: Int = teamNumber): String = createRoboRioHostDefault_mDNS(roboRioTeamNumber)

    private fun createRoboRioHostDefault_mDNS(teamNumber: Int): String = String.format(ROBORIO_HOST_mDNS_TEMPLATE, teamNumber)

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
    fun getRoboRioHostDefault_DNS(roboRioTeamNumber: Int = teamNumber): String = createRoboRioHostDefault_DNS(roboRioTeamNumber)

    private fun createRoboRioHostDefault_DNS(teamNumber: Int): String = String.format(ROBORIO_HOST_DNS_TEMPLATE, teamNumber)

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
    fun getRoboRioHostDefault_FieldLocal(roboRioTeamNumber: Int = teamNumber): String = createRoboRioHostDefault_FieldLocal(roboRioTeamNumber)

    private fun createRoboRioHostDefault_FieldLocal(teamNumber: Int): String = String.format(ROBORIO_HOST_FIELD_LOCAL_TEMPLATE, teamNumber)

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
    fun getRoboRioHostDefault_IP(roboRioTeamNumber: Int = teamNumber): String = createRoboRioHostDefault_IP(roboRioTeamNumber)

    private fun createRoboRioHostDefault_IP(teamNumber: Int): String
    {
        val high = teamNumber / 100
        val low = teamNumber % 100
        return String.format(ROBORIO_HOST_IP_TEMPLATE, high, low)
    }

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

data class ImmutableFrcRoboRioSettings internal constructor(val roboRioHostRawMDns: String,
                                                            val roboRioHostRawDns: String,
                                                            val roboRioHostRawIp: String,
                                                            val roboRioHostRawUsb: String,
                                                            val roboRioHostRawFieldLocal: String)

// TODO: This is a temporary work around to handle team number changes until we can remove it as a property in the FrcRoboRioSettings 
class TeamNumberChangeListenerServiceForFrcRoboRioSettings private constructor(private val project: Project): FrcProjectTeamNumberChangeListener
{
    init
    {
        project.messageBus.connect().subscribe(FrcProjectTeamNumberService.PROJECT_TEAM_NUMBER_CHANGES, this)
    }
    
    override fun onTeamNumberChange(previousTeamNumber: Int, newTeamNumber: Int)
    {
        FrcRoboRioSettings.getInstance(project).teamNumber = newTeamNumber
    }

    companion object
    {
        @JvmStatic
        fun getInstance(project: Project) = project.service<TeamNumberChangeListenerServiceForFrcRoboRioSettings>()
    }
}