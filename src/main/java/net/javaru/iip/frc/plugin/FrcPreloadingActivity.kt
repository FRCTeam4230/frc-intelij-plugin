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

import com.intellij.openapi.application.PreloadingActivity
import com.intellij.openapi.progress.ProgressIndicator
import net.javaru.iip.frc.FrcPluginGlobals
import net.javaru.iip.frc.notify.notifyAboutTeamNumberNeedingToBeConfigured
import net.javaru.iip.frc.settings.FrcApplicationSettings


class FrcPreloadingActivity: PreloadingActivity()
{
    override fun preload(indicator: ProgressIndicator)
    {
        if (FrcPluginGlobals.IS_IN_FRC_INTERNAL_MODE)
        {
            val baseLogger = FrcPluginGlobals.GENERAL_LOGGER
            FrcPluginGlobals.GENERAL_LOGGER.info("[FRC] >>> isDebugEnabled = ${baseLogger.isDebugEnabled}  isTraceEnabled = ${baseLogger.isTraceEnabled} <<<")
        }

        // We should consider removing this functionality and have it only prompt when an FRC project is loaded and the on the Welcome Screen via that configured Action
        val settings = FrcApplicationSettings.getInstance()
        settings.incrementRunCount()
        if (!settings.isTeamNumberConfigured() && settings.prc <= FrcPluginGlobals.TEAM_NUM_NOTIFY_RUN_COUNT_APP_LEVEL)
        {
            notifyAboutTeamNumberNeedingToBeConfigured(project = null, useSticky = true, asWarning = false)
        }
    }

}