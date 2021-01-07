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

package net.javaru.iip.frc;

import org.jetbrains.annotations.NotNull;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.util.lang.JavaVersion;



public class FrcPluginGlobals
{
    @SuppressWarnings("unused")
    @NotNull
    public static final String FRC_PLUGIN_ID_STRING = "net.javaru.idea.frc";
    @NotNull
    public static final String FRC_PLUGIN_NAME = "FRC";
    
    public static final int TEAM_NUM_NOTIFY_RUN_COUNT_APP_LEVEL = 12;
    public static final int TEAM_NUM_NOTIFY_RUN_COUNT_PROJECT_LEVEL_NON_FRC_PROJECT = 8;
    public static final int MAX_RUN_COUNT_TO_SAVE = 16;
    
    @NotNull
    public static final LanguageLevel DEFAULT_MIN_REQUIRED_LANGUAGE_LEVEL = LanguageLevel.JDK_11;
    @NotNull
    public static final JavaVersion DEFAULT_MIN_REQUIRED_JAVA_VERSION = DEFAULT_MIN_REQUIRED_LANGUAGE_LEVEL.toJavaVersion();

    private FrcPluginGlobals() { }
}
