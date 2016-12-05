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

package net.javaru.iip.frc;

import java.nio.file.Path;

import com.intellij.openapi.application.PathManager;



public final class FrcPluginGlobals
{
    public static final String FRC_PLUGIN_BASE_NAME = "FrcPlugin";
    public static final String FRC_PLUGIN_DISPLAY_NAME = "FRC Plugin";

    //TODO enter the following info
    public static final String CONTACT_EMAIL = "TODO - NEED TO POPULATE CONTACT_EMAIL";
    public static final String PLUGIN_WEBSITE = "https://gitlab.com/Javaru/frc-intellij-idea";
    public static final String ISSUES_WEBSITE = "https://gitlab.com/Javaru/frc-intellij-idea/issues";

    /** The standard log message (line) output by the robRIO on startup. */
    //  ➔ Launching «'/usr/local/frc/JRE/bin/java' '-jar' '/home/lvuser/FRCUserProgram.jar'»
    public static final String ROBO_RIO_STARTUP_LOG_MSG = "\u2794 Launching \u00AB'/usr/local/frc/JRE/bin/java' '-jar' '/home/lvuser/FRCUserProgram.jar'\u00BB";


    public static final Path FRC_OPTIONS_DIR = PathManager.getOptionsFile("FRC").toPath();


    private FrcPluginGlobals() { }
}
