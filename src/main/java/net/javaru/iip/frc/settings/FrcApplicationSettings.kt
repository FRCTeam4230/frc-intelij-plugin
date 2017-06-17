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

package net.javaru.iip.frc.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Transient
import net.javaru.iip.frc.util.UriUtils
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

const val UN_CONFIGURED_TEAM_NUMBER: Int = 0

const val DEFAULT_RIO_LOG_UDP_PORT: Int = 6666



// NOTE: This class is registered as an <applicationService> in the plugin.xml
@State(name = "FrcPlugin", storages = arrayOf(Storage("frc.xml")))
data class FrcApplicationSettings(var teamNumber: Int = UN_CONFIGURED_TEAM_NUMBER,
                                  var rioLogUdpPort: Int = DEFAULT_RIO_LOG_UDP_PORT,
                                  /* Plugin Run Count (PRC) */
                                  var prc: Int = 0,
                                  var wpiLibDir: Path = Paths.get(System.getProperty("frc.alt.user.home.dir", System.getProperty("user.home", "C:\\Users\\Public"))).resolve("wpilib").toAbsolutePath(),
                                  var wpiEclipsePluginReleaseRepoUri: URI = FrcApplicationSettings.DEFAULT_WPI_ECLIPSE_PLUGIN_RELEASE_REPO_URI,
                                  var wpiEclipsePluginBetaRepoUri: URI = FrcApplicationSettings.DEFAULT_WPI_ECLIPSE_PLUGIN_BETA_REPO_URI
                                 ) : PersistentStateComponent<FrcApplicationSettings>
{
    companion object Settings
    {
        //The trailing slash is important so the uri.resolves method sees the last entry as a directory and not the endpoint
        val DEFAULT_WPI_ECLIPSE_PLUGIN_RELEASE_REPO_URI: URI = UriUtils.createUri("http://first.wpi.edu/FRC/roborio/release/eclipse/")
        val DEFAULT_WPI_ECLIPSE_PLUGIN_BETA_REPO_URI: URI = UriUtils.createUri("http://first.wpi.edu/FRC/roborio/beta/eclipse/")

        fun INSTANCE(): FrcApplicationSettings
        {
            return ServiceManager.getService(FrcApplicationSettings::class.java)
        }

        fun clone(original: FrcApplicationSettings): FrcApplicationSettings
        {
            return original.copy()
        }

        fun isValidTeamNumber(teamNumberString: String): Boolean
        {
            try
            {
                return isValidTeamNumber(Integer.valueOf(teamNumberString))
            }
            catch (ignore: NumberFormatException)
            {
                return false
            }
        }
        
        fun isValidTeamNumber(teamNumber: Int): Boolean
        {
            return teamNumber > 0
        }
    }


    private val LOG = Logger.getInstance(FrcApplicationSettings::class.java)

    override fun getState(): FrcApplicationSettings
    {
        LOG.trace("[FRC] FrcApplicationSettings.getState() called. Returning current state of: " + toString())
        return this
    }

    override fun loadState(state: FrcApplicationSettings)
    {
        LOG.trace("[FRC] FrcApplicationSettings.loadState() called with state object of: " + state)
        XmlSerializerUtil.copyBean<FrcApplicationSettings>(state, this)
    }

    @Transient
    fun isTeamNumberConfigured(): Boolean { return teamNumber > 0 }

    fun incrementRunCount() { prc++ }
}
