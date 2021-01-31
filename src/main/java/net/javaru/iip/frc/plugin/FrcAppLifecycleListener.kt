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

package net.javaru.iip.frc.plugin

import com.intellij.ide.AppLifecycleListener
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.FrcPluginGlobals
import net.javaru.iip.frc.FrcPluginGlobals.TEAM_NUM_NOTIFY_RUN_COUNT_APP_LEVEL
import net.javaru.iip.frc.notify.notifyAboutTeamNumberNeedingToBeConfigured
import net.javaru.iip.frc.settings.FrcApplicationSettings


class FrcAppLifecycleListener: AppLifecycleListener
{
    /*
        1. appFrameCreated()
        2. welcomeScreenDisplayed()
            • Obviously only called is the welcome screen is displayed rather than a project immediately
            • It is NOT called again if all projects are closed and you return to the Welcome screen, even id Welcome screen was not initially displayed on startup
        3. appStarting()
        4. appStarted() !!!!INTERNAL USE ONLY!!!!

        5. appClosing()
        6. appWillBeClosed()
    */


    override fun appStarting(projectFromCommandLine: Project?)
    {
        if (FrcPluginGlobals.IS_IN_FRC_INTERNAL_MODE)
        {
            val baseLogger = FrcPluginGlobals.GENERAL_LOGGER
            FrcPluginGlobals.GENERAL_LOGGER.info("[FRC] >>> isDebugEnabled = ${baseLogger.isDebugEnabled}  isTraceEnabled = ${baseLogger.isTraceEnabled} <<<")
        }

        // We should consider removing this functionality and have it only prompt for FRC projects and the on the Welcome Screen iva that configured Action
        val settings = FrcApplicationSettings.getInstance()
        settings.incrementRunCount()
        if (!settings.isTeamNumberConfigured() && settings.prc <= TEAM_NUM_NOTIFY_RUN_COUNT_APP_LEVEL)
        {
            notifyAboutTeamNumberNeedingToBeConfigured(null, true, false)
        }
    }
}

