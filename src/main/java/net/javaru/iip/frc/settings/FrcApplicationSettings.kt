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

package net.javaru.iip.frc.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Transient
import net.javaru.iip.frc.FrcPluginGlobals.MAX_RUN_COUNT_TO_SAVE
import org.apache.commons.lang3.StringUtils
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.Paths

const val DEFAULT_RIO_LOG_UDP_PORT: Int = 6666

const val WPILIB_BASE_DIR_ENV_VAR: String = "wpilib.base.dir"
/**
 * Alternative base directory for the wpilib directory. By default the `user.dir` directory is used.
 * A value set via this will override that default. Thus is this is set to `C:\libs` a wpilib
 * directory of `C:\libs\wpilib` is used for wpi lib directory. 
 */
const val ALT_WPILIB_BASE_DIR_SYS_PROP: String = "frc.alt.wpilib.base.dir"

const val USE_WPILIB_BETA_SITE: String = "frc.use.wpilib.beta.site"

// NOTE: This class is registered as an <applicationService> in the plugin.xml
@State(name = "FrcPlugin", storages = [(Storage("frc.xml"))])
data class FrcApplicationSettings(var teamNumber: Int = UN_CONFIGURED_TEAM_NUMBER,
                                  var rioLogUdpPort: Int = DEFAULT_RIO_LOG_UDP_PORT,
                                  /* Plugin Run Count (PRC) */
                                  var prc: Int = 0,
                                  var useFrcToolWindow: Boolean = true,
                                  var clearRioLogOnRobotRestart: Boolean = false,
                                  var logNetConsoleToFile: Boolean = false,
                                  var logNetConsoleToFileAppend: Boolean = false,
                                  var logNetConsoleToFilePath: String = "/tmp/frc/riolog",
                                  var logNetConsoleToFileBaseName: String = "rioLog-\${time}.log",
                                  var useRegexForRestartCheck: Boolean = false,
                                  var autoAttachWpiLib: Boolean = true,
                                  var autoAttachUserLib: Boolean = true,
                                  var checkForNewWpiLibVersionOnProjectOpen: Boolean = true,
                                  /** Whether to prompt (false) user to download new version or just automatically download it and then notify (true). */
                                  var autoDownloadNewWpiLibVersions: Boolean = false,
                                  /**
                                   * Regex for detecting roboRIO restart.
                                   * NOTE:  We limit the searched for text in the regex in case of encoding issues and in case an alternate path is used 
                                   *        on the roboRIO. The actual full Statement logged by roboRIO starts with the arrow flush left and ends with the 
                                   *        closing/right-pointing guillemet flush right as shown here:
                                   *        
                                   *            ➔ Launching «'/usr/local/frc/JRE/bin/java' '-jar' '/home/lvuser/FRCUserProgram.jar'»
                                   */
                                  var rioRestartRegexString: String = "(?ium).*Launching.*FRCUserProgram\\.jar.*", /* TODO Need to update for Gradle based deploy*/
                                  /* Tracking for notification of the 2019 update requirement */
                                  var notify19Up: String = "2019-01-01",
                                  var enableGradleImportUponNewProjectCreation: Boolean = true
                                 ) : PersistentStateComponent<FrcApplicationSettings>
{
    companion object Settings
    {
        @JvmStatic
        fun getInstance(): FrcApplicationSettings
        {
            return ServiceManager.getService(FrcApplicationSettings::class.java)
        }

        @JvmStatic
        fun clone(original: FrcApplicationSettings): FrcApplicationSettings
        {
            return original.copy()
        }
    }


    private val LOG = Logger.getInstance(FrcApplicationSettings::class.java)

    override fun getState(): FrcApplicationSettings
    {
        LOG.trace("[FRC] FrcApplicationSettings.getState() called. Returning current state of: ${toString()}")
        return this
    }

    override fun loadState(state: FrcApplicationSettings)
    {
        LOG.trace("[FRC] FrcApplicationSettings.loadState() called with state object of: $state")
        XmlSerializerUtil.copyBean(state, this)
    }

    @Transient
    fun isTeamNumberConfigured(): Boolean { return teamNumber > 0 }

    fun incrementRunCount() { if (prc < MAX_RUN_COUNT_TO_SAVE) {prc++} }
}


fun determineWpiLibDir(): Path
{
    val logger = Logger.getInstance(FrcApplicationSettings::class.java)
    var basePath: Path? = null

    if (StringUtils.isNotBlank(System.getProperty (ALT_WPILIB_BASE_DIR_SYS_PROP)))
    {
        try
        {
            basePath = Paths.get(System.getProperty(ALT_WPILIB_BASE_DIR_SYS_PROP))
            logger.debug("[FRC] calculated wpilib base dir set to '$basePath' via system property '$ALT_WPILIB_BASE_DIR_SYS_PROP'")
        }
        catch (e: InvalidPathException)
        {
            logger.warn("[FRC] The value, '${System.getProperty(ALT_WPILIB_BASE_DIR_SYS_PROP)}', configured in system property '$ALT_WPILIB_BASE_DIR_SYS_PROP' is " +
                        "not a valid path. It wil be ignored when determining the wpilib base directory. Details: " + e.toString())
        }
    }
    else
    {
        try
        {
            val envVar: String? = System.getenv(WPILIB_BASE_DIR_ENV_VAR)
            if (!envVar.isNullOrBlank())
            {
                try
                {
                    basePath = Paths.get(envVar).toAbsolutePath()
                    logger.debug("[FRC] calculated wpilib base dir set to '$basePath' via env var '$WPILIB_BASE_DIR_ENV_VAR'")
                }
                catch (e: InvalidPathException)
                {
                    logger.warn("[FRC] The value, '$envVar', configured in env variable '$WPILIB_BASE_DIR_ENV_VAR' is not a valid path. " +
                                "It wil be ignored when determining the wpilib base directory. Details: " + e.toString())
                }
            }
        }
        catch (e: Exception) // Potential for SecurityException is the main concern
        {
            logger.warn("""[FRC] An exception occurred when checking env variable '$WPILIB_BASE_DIR_ENV_VAR'. Cause Summary: ${e.toString()}""")
        }
    }

    if (basePath == null)
    {
        basePath = Paths.get(System.getProperty("user.home", "C:\\Users\\Public"))
        logger.debug("[FRC] calculated wpilib base dir set to '$basePath' via (default) system property 'user.home'")
    }


    val wpiLibDir = basePath!!.resolve("wpilib").toAbsolutePath()
    logger.debug("[FRC] calculated wpilib dir set to '$wpiLibDir'.")

    return wpiLibDir
}

