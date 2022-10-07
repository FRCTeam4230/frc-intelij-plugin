/*
 * Copyright 2015-2022 the original author or authors.
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

package net.javaru.iip.frc.services

import com.intellij.ide.AppLifecycleListener
import net.javaru.iip.frc.FrcPluginGlobals

/*
Call Order:
    1. appFrameCreated() called.  commandLineArgs=[]
    2. welcomeScreenDisplayed() called.  
        • Obviously only called is the welcome screen is displayed rather than a project immediately
        • It is NOT called again if all projects are closed and you return to the Welcome screen, even if Welcome screen was not initially displayed on startup
    3. appStarting() called. projectFromCommandLine=null Deprecated and removed in 2021.3
    4. appStarted() called.  !!!!INTERNAL USE ONLY!!!!
    
    5. appClosing() called.
    6. appWillBeClosed() called. isRestart=false

projectFrameClosed() 
    • Appears to only be called if more than one project is opened, and you close one, this is called. It was not called if you closed a single project, either by just closing it and going to the Welcome screen.
projectOpenFailed()
    • Did not try to get this called.

 */
class FrcAppLifecycleListener : AppLifecycleListener
{
    override fun appFrameCreated(commandLineArgs: MutableList<String>)
    {
        if (FrcPluginGlobals.IS_IN_FRC_INTERNAL_MODE)
        {
            val baseLogger = FrcPluginGlobals.GENERAL_LOGGER
            FrcPluginGlobals.GENERAL_LOGGER.info("[FRC] >>> isDebugEnabled = ${baseLogger.isDebugEnabled}  isTraceEnabled = ${baseLogger.isTraceEnabled} <<<")
        }
    }

    override fun welcomeScreenDisplayed()
    {
    }

    override fun projectFrameClosed()
    {
    }

    override fun projectOpenFailed()
    {
    }

    override fun appClosing()
    {
    }

    override fun appWillBeClosed(isRestart: Boolean)
    {
    }
}