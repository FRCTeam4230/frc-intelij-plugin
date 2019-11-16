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

package net.javaru.iip.frc.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import net.javaru.iip.frc.riolog.RioLogGlobals

const val SSH_USERNAME_DEFAULT: String = "admin"
const val SSH_PASSWORD_DEFAULT: String = ""

// NOTE: This class is registered as an <applicationService> in the plugin.xml
@State(name = "FrcSsh", storages = [(Storage("frc/frc.xml"))])
data class FrcSshSettings @JvmOverloads constructor(
        private val project: Project,
        var sshUsername: String = SSH_USERNAME_DEFAULT,
        var sshPassword: String = SSH_PASSWORD_DEFAULT,
        var sshTailCommand: String = RioLogGlobals.DEFAULT_TAIL_COMMAND
                                                   ) : PersistentStateComponent<FrcSshSettings>
{

    companion object Settings
    {
        @JvmStatic
        fun getInstance(project: Project): FrcSshSettings = ServiceManager.getService(project, FrcSshSettings::class.java)

        @JvmStatic
        fun getImmutableInstance(project: Project): ImmutableFrcSshSettings
        {
            val (_, sshUsername, sshPassword, sshTailCommand) = getInstance(project)
            return ImmutableFrcSshSettings(sshUsername, sshPassword, sshTailCommand)
        }

        @JvmStatic
        fun clone(original: FrcSshSettings): FrcSshSettings = original.copy()
    }

    private val LOG = Logger.getInstance(FrcSshSettings::class.java)

    override fun getState(): FrcSshSettings
    {
        LOG.trace("[FRC] FrcSshSettings.getState() called. Returning current state of: " + toString())
        return this
    }

    override fun loadState(state: FrcSshSettings)
    {
        LOG.trace("[FRC] FrcSshSettings.loadState() called with state object of: $state")
        XmlSerializerUtil.copyBean(state, this)
    }
}

data class ImmutableFrcSshSettings internal constructor(val sshUsername: String,
                                                        val sshPassword: String,
                                                        val sshTailCommand: String) 