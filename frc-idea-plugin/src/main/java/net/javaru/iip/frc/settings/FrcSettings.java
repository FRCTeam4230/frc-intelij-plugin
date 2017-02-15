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

import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.roboRIO.riolog.RioLogGlobals;
import net.javaru.iip.frc.util.ClonerImpl;
import net.javaru.iip.frc.util.UriUtils;

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
    public static final Pattern DEFAULT_RIO_RESTART_REGEX = Pattern.compile(".*Launching.*FRCUserProgram\\.jar.*",
                                                                            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
    //TODO: change the default to the FRC installation directory or such
    public static final Path DEFAULT_LOG_DIRECTORY = Paths.get("/").resolve("tmp").resolve("frc").toAbsolutePath();
    public static final String DEFAULT_LOG_FILE_BASENAME = "rioLog-${time}.log";
    public static final int DEFAULT_RIO_LOG_PORT = 6666;
    
    //The trailing slash is important so the uri.resolves method sees the last entry as a directory and not the endpoint
    public static final URI DEFAULT_WPI_ECLIPSE_PLUGIN_RELEASE_REPO_URI = UriUtils.createUriQuietly("http://first.wpi.edu/FRC/roborio/release/eclipse/");
    public static final URI DEFAULT_WPI_ECLIPSE_PLUGIN_BETA_REPO_URI = UriUtils.createUriQuietly("http://first.wpi.edu/FRC/roborio/beta/eclipse/");

    private static final String ROBORIO_HOST_mDNS_TEMPLATE = "roborio-%d-FRC.local";
    private static final String ROBORIO_HOST_DNS_TEMPLATE = "roborio-%d-FRC.lan";
    private static final String ROBORIO_HOST_IP_TEMPLATE = "10.%d.%d.2";
    
    private boolean useFrcToolWindow = true;
    
    private int rioLogPort = DEFAULT_RIO_LOG_PORT;

    private Pattern rioRestartRegex = DEFAULT_RIO_RESTART_REGEX;
    
    private boolean useRegexForRestartCheck = false;

    private boolean clearOnRobotRestart = false;

    private boolean logToFile = false;

    private boolean logFileAppend = true;

    private Path logFileDirectory = DEFAULT_LOG_DIRECTORY;

    private String logFileBaseName = DEFAULT_LOG_FILE_BASENAME;

    private URI wpiEclipsePluginReleaseRepoUri = DEFAULT_WPI_ECLIPSE_PLUGIN_RELEASE_REPO_URI;

    private URI wpiEclipsePluginBetaRepoUri = DEFAULT_WPI_ECLIPSE_PLUGIN_BETA_REPO_URI;
    
    private Path wpiLibDir = Paths.get(System.getProperty("frc.alt.user.home.dir", System.getProperty("user.home", "C:\\Users\\Public"))).resolve("wpilib").toAbsolutePath();
    
    public static final int UN_CONFIGURED_TEAM_NUMBER = 0;
    
    private int teamNumber = UN_CONFIGURED_TEAM_NUMBER;
    
    private String sshTailUsername = "admin";
    private String sshTailPassword = "";
    private String sshTailCommand = RioLogGlobals.DEFAULT_TAIL_COMMAND;
    
    public static final String USE_DEFAULT_HOST = "<<<Use Default Host>>>";
    
    private String roboRioHostMDns = USE_DEFAULT_HOST;
    private String roboRioHostDns = USE_DEFAULT_HOST;
    private String roboRioHostUsb = USE_DEFAULT_HOST;
    private String roboRioHostIp = USE_DEFAULT_HOST;


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


    public boolean isUseRegexForRestartCheck()
    {
        return useRegexForRestartCheck;
    }

    public void setUseRegexForRestartCheck(boolean useRegexForRestartCheck) { this.useRegexForRestartCheck = useRegexForRestartCheck; }


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


    public URI getWpiEclipsePluginReleaseRepoUri() { return wpiEclipsePluginReleaseRepoUri; }


    public void setWpiEclipsePluginReleaseRepoUri(URI wpiEclipsePluginReleaseRepoUri) { this.wpiEclipsePluginReleaseRepoUri = wpiEclipsePluginReleaseRepoUri; }


    public URI getWpiEclipsePluginBetaRepoUri() { return wpiEclipsePluginBetaRepoUri; }


    public void setWpiEclipsePluginBetaRepoUri(URI wpiEclipsePluginBetaRepoUri) { this.wpiEclipsePluginBetaRepoUri = wpiEclipsePluginBetaRepoUri; }

    /** The path to the local wpilib directory. Default/typical value would be: <tt>C:\\Users\\userName\\wpilib</tt>  */
    public Path getWpiLibDir() { return wpiLibDir; }


    public void setWpiLibDir(Path wpiLibDir) { this.wpiLibDir = wpiLibDir; }


    public int getTeamNumber() { return teamNumber; }


    public void setTeamNumber(int teamNumber) { this.teamNumber = teamNumber; }
    
    public boolean isTeamNumberConfigured()
    {
        return teamNumber != UN_CONFIGURED_TEAM_NUMBER && teamNumber > 0;
    }


    public String getSshTailUsername() { return sshTailUsername; }


    public void setSshTailUsername(String sshTailUsername) { this.sshTailUsername = sshTailUsername; }


    public String getSshTailPassword() { return sshTailPassword; }


    public void setSshTailPassword(String sshTailPassword) { this.sshTailPassword = sshTailPassword; }


    public String getSshTailCommand() { return sshTailCommand; }


    public void setSshTailCommand(String sshTailCommand) { this.sshTailCommand = sshTailCommand; }


    public String getRoboRioHostMDns() 
    {
        if (USE_DEFAULT_HOST.equals(roboRioHostMDns))
        {
            return getDefaultRoboRioHost_mDNS();
        }
        else
        {
            return roboRioHostMDns;
        }
    }

    public void setRoboRioHostMDns(String roboRioHostMDns) 
    {
        this.roboRioHostMDns = (getDefaultRoboRioHost_mDNS().equals(roboRioHostMDns)) ? USE_DEFAULT_HOST : roboRioHostMDns;
    }


    /**
     * Gets the default mDNS Host for the configured team number in the format:
     *  <tt> roboRIO-${team-number}-FRC.local</tt>
     * @return the default mDNS Host for the configured team number
     */
    public String getDefaultRoboRioHost_mDNS()
    {
        return createDefaultRoboRioHost_mDNS(teamNumber);
        
    }
    
    public static String createDefaultRoboRioHost_mDNS(int teamNumber)
    {
        return String.format(ROBORIO_HOST_mDNS_TEMPLATE, teamNumber);
    }


    public String getRoboRioHostDns()
    {
        if (USE_DEFAULT_HOST.equals(roboRioHostDns))
        {
            return getDefaultRoboRioHost_DNS();
        }
        else
        {
            return roboRioHostDns;
        }
    }


    public void setRoboRioHostDns(String roboRioHostDns)
    {
        this.roboRioHostDns = (getDefaultRoboRioHost_DNS().equals(roboRioHostDns)) ? USE_DEFAULT_HOST : roboRioHostDns;
    }


    /**
     * Gets the default DNS Host for the configured team number in the format:
     *  <tt> roboRIO-${team-number}-FRC.lan</tt>
     * @return the default DNS Host for the configured team number
     */
    public String getDefaultRoboRioHost_DNS()
    {
        return createDefaultRoboRioHost_DNS(teamNumber);
        
    }
    
    public static String createDefaultRoboRioHost_DNS(int teamNumber)
    {
        return String.format(ROBORIO_HOST_DNS_TEMPLATE, teamNumber);
    }


    public String getRoboRioHostUsb()
    {
        if (USE_DEFAULT_HOST.equals(roboRioHostUsb))
        {
            return getDefaultRoboRioHost_USB();
        }
        else
        {
            return roboRioHostUsb;
        }
    }


    public void setRoboRioHostUsb(String roboRioHostUsb)
    {
        this.roboRioHostUsb = (getDefaultRoboRioHost_USB().equals(roboRioHostUsb)) ? USE_DEFAULT_HOST : roboRioHostUsb;
    }


    public String getDefaultRoboRioHost_USB()
    {
        return "172.22.11.2";
    }


    public String getRoboRioHostIp()
    {
        if (USE_DEFAULT_HOST.equals(roboRioHostIp))
        {
            return getDefaultRoboRioHost_IP();
        }
        else
        {
            return roboRioHostIp;
        }
    }


    public void setRoboRioHostIp(String roboRioHostIp)
    {
        this.roboRioHostIp = (getDefaultRoboRioHost_IP().equals(roboRioHostIp)) ? USE_DEFAULT_HOST : roboRioHostIp;
    }


    /**
     * Gets the default IP Host for the configured team number in the format:
     * <tt> roboRIO-${team-number}-FRC.lan</tt>
     *
     * @return the default IP Host for the configured team number
     */
    public String getDefaultRoboRioHost_IP()
    {
        return createDefaultRoboRioHost_IP(teamNumber);

    }


    public static String createDefaultRoboRioHost_IP(int teamNumber)
    {
        final int high = teamNumber / 100;   
        final int low = teamNumber % 100;   
        return String.format(ROBORIO_HOST_IP_TEMPLATE, high, low);
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
            .append(wpiLibDir, that.wpiLibDir)
            .append(wpiEclipsePluginReleaseRepoUri, that.wpiEclipsePluginReleaseRepoUri)
            .append(wpiEclipsePluginBetaRepoUri, that.wpiEclipsePluginBetaRepoUri)
            .append(teamNumber, that.teamNumber)
            .append(useRegexForRestartCheck, that.useRegexForRestartCheck)
            .append(sshTailUsername, that.sshTailUsername)
            .append(sshTailPassword, that.sshTailPassword)
            .append(sshTailCommand, that.sshTailCommand)
            .append(roboRioHostMDns, that.roboRioHostMDns)
            .append(roboRioHostDns, that.roboRioHostDns)
            .append(roboRioHostUsb, that.roboRioHostUsb)
            .append(roboRioHostIp, that.roboRioHostIp)
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
            .append(wpiLibDir)
            .append(wpiEclipsePluginReleaseRepoUri)
            .append(wpiEclipsePluginBetaRepoUri)
            .append(teamNumber)
            .append(useRegexForRestartCheck)
            .append(sshTailUsername)
            .append(sshTailPassword)
            .append(sshTailCommand)
            .append(roboRioHostMDns)
            .append(roboRioHostDns)
            .append(roboRioHostUsb)
            .append(roboRioHostIp)
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
