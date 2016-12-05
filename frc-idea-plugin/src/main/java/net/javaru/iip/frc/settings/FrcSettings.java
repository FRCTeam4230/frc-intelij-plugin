/*
 * Copyright 2015 Mark Vedder
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

package net.javaru.iip.frc.settings;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.util.ClonerImpl;

/*
     ___                     _            _   
    |_ _|_ __  _ __  ___ _ _| |_ __ _ _ _| |_ 
     | || '  \| '_ \/ _ \ '_|  _/ _` | ' \  _|
    |___|_|_|_| .__/\___/_|  \__\__,_|_||_\__|
              |_|                             
    **IMPORTANT** All settings/properties must have a default value upon construction.
                  Be sure to add any new properties/settings to the equals & hashcode methods

                  
    The implementation of PersistentStateComponent works by serializing public fields, annotated private 
    fields and bean properties into an XML format. The following types of values can be persisted:
      • numbers (both primitive types, such as int, and boxed types, such as Integer)
      • booleans
      • strings
      • collections
      • maps
      • enums
 */

// Example:  org.intellij.plugins.intelliLang.AdvancedSettingsUI
// To use:   FrcSettings frcSettings = FrcApplicationComponent.getInstance().getState();
@SuppressWarnings({"ClassWithoutLogger", "unused"})
public class FrcSettings implements Cloneable
{
    // **IMPORTANT** All settings/properties must have a default value upon construction.
    //               Be sure to add any new properties/settings to the equals & hashcode methods

    private static final Logger LOG = Logger.getInstance(FrcSettings.class);
    
    // Actual Full Statement logged by roboRIO is as follows. It starts with the
    // arrow flush left and ends with the closing/right-pointing guillemet flush right:
    //    ➔ Launching «'/usr/local/frc/JRE/bin/java' '-jar' '/home/lvuser/FRCUserProgram.jar'»
    // See FrcPluginGlobals.ROBO_RIO_STARTUP_LOG_MSG
    public static final Pattern DEFAULT_RIO_RESTART_REGEX = Pattern.compile(".*FRCUserProgram\\.jar.*",
                                                                            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
    //TODO: change the default to the FRC installation directory or such
    public static final Path DEFAULT_LOG_DIRECTORY = Paths.get("/").resolve("tmp").resolve("frc").toAbsolutePath();
    public static final String DEFAULT_LOG_FILE_BASENAME = "rioLog-${time}.log";
    public static final int DEFAULT_RIO_LOG_PORT = 6666;
    
    //The trailing slash is important so the uri.resolves method sees the last entry as a directory and not the endpoint
    public static final URI DEFAULT_WPI_ECLIPSE_PLUGIN_RELEASE_REPO_URI = createUrlQuietly("http://first.wpi.edu/FRC/roborio/release/eclipse/");
    public static final URI DEFAULT_WPI_ECLIPSE_PLUGIN_BETA_REPO_URI = createUrlQuietly("http://first.wpi.edu/FRC/roborio/beta/eclipse/");

    private boolean useFrcToolWindow = true;
    
    private int rioLogPort = DEFAULT_RIO_LOG_PORT;

    private Pattern rioRestartRegex = DEFAULT_RIO_RESTART_REGEX;

    private boolean clearOnRobotRestart = false;

    private boolean logToFile = true;

    private boolean logFileAppend = true;

    private Path logFileDirectory = DEFAULT_LOG_DIRECTORY;

    private String logFileBaseName = DEFAULT_LOG_FILE_BASENAME;

    
    
    private URI wpiEclipsePluginReleaseRepoUri = DEFAULT_WPI_ECLIPSE_PLUGIN_RELEASE_REPO_URI;

    private URI wpiEclipsePluginBetaRepoUri = DEFAULT_WPI_ECLIPSE_PLUGIN_BETA_REPO_URI;
    


    // **IMPORTANT** All settings/properties must have a default value upon construction.
    //               Be sure to add any new properties/settings to the equals & hashcode methods
    public FrcSettings() { }


    @SuppressWarnings({"CloneDoesntCallSuperClone", "CloneDoesntDeclareCloneNotSupportedException"})
    @Override
    protected FrcSettings clone()
    {
        return ClonerImpl.deepClone(this);
    }


    public boolean isUseFrcToolWindow() { return useFrcToolWindow; }


    public void setUseFrcToolWindow(boolean useFrcToolWindow) { this.useFrcToolWindow = useFrcToolWindow; }


    public int getRioLogPort()
    {
        return rioLogPort;
    }


    public void setRioLogPort(int rioLogPort)
    {
        this.rioLogPort = rioLogPort;
    }


    public Pattern getRioRestartRegex()
    {
        return rioRestartRegex;
    }


    public void setRioRestartRegex(Pattern rioRestartRegex)
    {
        this.rioRestartRegex = rioRestartRegex;
    }


    public boolean isLogFileAppend()
    {
        return logFileAppend;
    }


    public void setLogFileAppend(boolean logFileAppend)
    {
        this.logFileAppend = logFileAppend;
    }


    public Path getLogFileDirectory()
    {
        return logFileDirectory;
    }


    public void setLogFileDirectory(Path logFileDirectory)
    {
        this.logFileDirectory = logFileDirectory;
    }


    public String getLogFileBaseName()
    {
        return logFileBaseName;
    }


    public void setLogFileBaseName(String logFileBaseName)
    {
        this.logFileBaseName = logFileBaseName;
    }


    public boolean isLogToFile()
    {
        return logToFile;
    }


    public void setLogToFile(boolean logToFile)
    {
        this.logToFile = logToFile;
    }


    public boolean isClearOnRobotRestart()
    {
        return clearOnRobotRestart;
    }


    public void setClearOnRobotRestart(boolean clearOnRobotRestart)
    {
        this.clearOnRobotRestart = clearOnRobotRestart;
    }


    public Path getWpiLibDir()
    {
        //TODO: need to make configurable
        final String userHome = System.getProperty("user.home", "C:\\Users\\Public");
        return Paths.get(userHome).resolve("wpilib").toAbsolutePath();
    }
    
    @Nullable
    protected static URI createUrlQuietly(String url)
    {
        try
        {
            return new URI(url);
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] Could not create URL from '" + url + '\'');
            return null;
        }
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FrcSettings that = (FrcSettings) o;

        return new org.apache.commons.lang3.builder.EqualsBuilder()
            .append(useFrcToolWindow, that.useFrcToolWindow)
            .append(rioLogPort, that.rioLogPort)
            .append(rioRestartRegex, that.rioRestartRegex)
            .append(logToFile, that.logToFile)
            .append(logFileAppend, that.logFileAppend)
            .append(logFileDirectory, that.logFileDirectory)
            .append(logFileBaseName, that.logFileBaseName)
            .append(clearOnRobotRestart, that.clearOnRobotRestart)
            .isEquals();
    }


    @Override
    public int hashCode()
    {
        return new org.apache.commons.lang3.builder.HashCodeBuilder(17, 37)
            .append(useFrcToolWindow)
            .append(rioLogPort)
            .append(rioRestartRegex)
            .append(logToFile)
            .append(logFileAppend)
            .append(logFileDirectory)
            .append(logFileBaseName)
            .append(clearOnRobotRestart)
            .toHashCode();
    }
    
/*
     ___                     _            _   
    |_ _|_ __  _ __  ___ _ _| |_ __ _ _ _| |_ 
     | || '  \| '_ \/ _ \ '_|  _/ _` | ' \  _|
    |___|_|_|_| .__/\___/_|  \__\__,_|_||_\__|
              |_|                             
    **IMPORTANT** All settings/properties must have a default value upon construction.
                  Be sure to add any new properties/settings to the equals & hashcode methods

                  
    The implementation of PersistentStateComponent works by serializing public fields, annotated private 
    fields and bean properties into an XML format. The following types of values can be persisted:
      • numbers (both primitive types, such as int, and boxed types, such as Integer)
      • booleans
      • strings
      • collections
      • maps
      • enums
 */
}
