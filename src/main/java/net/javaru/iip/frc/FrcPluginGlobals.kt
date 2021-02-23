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
package net.javaru.iip.frc

import com.intellij.openapi.diagnostic.Logger
import com.intellij.pom.java.LanguageLevel
import org.apache.commons.lang3.BooleanUtils

object FrcPluginGlobals
{
    const val FRC_PLUGIN_ID_STRING = "net.javaru.idea.frc"
    const val FRC_PLUGIN_NAME = "FRC"
    const val TEAM_NUM_NOTIFY_RUN_COUNT_APP_LEVEL = 12
    const val TEAM_NUM_NOTIFY_RUN_COUNT_PROJECT_LEVEL_NON_FRC_PROJECT = 8
    const val MAX_RUN_COUNT_TO_SAVE = 16
    const val FRC_IN_UNIT_TEST_MODE_KEY = "frc.testing.inUnitTestMode"

    /** A logger name `#net.javaru.iip.frc` for logging plugin level events. It should be used sparingly. */
    @JvmField
    val GENERAL_LOGGER = Logger.getInstance("#net.javaru.iip.frc")
    @JvmField
    val DEFAULT_MIN_REQUIRED_LANGUAGE_LEVEL = LanguageLevel.JDK_11
    @JvmField
    val DEFAULT_MIN_REQUIRED_JAVA_VERSION = DEFAULT_MIN_REQUIRED_LANGUAGE_LEVEL.toJavaVersion()
    @JvmField
    val IS_IN_FRC_INTERNAL_MODE = BooleanUtils.toBoolean(System.getProperty("frc.is.internal", false.toString()))
    @JvmField
    val IS_IN_FRC_UNIT_TEST_MODE = BooleanUtils.toBoolean(System.getProperty(FRC_IN_UNIT_TEST_MODE_KEY, false.toString()))
    @JvmField
    val IS_NOT_IN_FRC_UNIT_TEST_MODE = !IS_IN_FRC_UNIT_TEST_MODE
}