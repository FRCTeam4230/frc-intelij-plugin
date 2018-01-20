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

package net.javaru.iip.frc.components;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.settings.FrcApplicationSettings;

import static net.javaru.iip.frc.FrcPluginGlobals.TEAM_NUM_NOTIFY_RUN_COUNT_APP_LEVEL;



public class FrcApplicationComponentImpl implements FrcApplicationComponent
{
    private static final Logger LOG = Logger.getInstance(FrcApplicationComponentImpl.class);

    private static final FrcApplicationComponent defaultInstance = new FrcApplicationComponentImpl();


    /** Do not call constructor directly. Use the static {@link #getInstance()} method. */
    public FrcApplicationComponentImpl()
    {
        LOG.debug("[FRC] FrcApplicationComponentImpl constructor called");
    }


    @NotNull
    public static FrcApplicationComponent getInstance()
    {
        final FrcApplicationComponent component = ApplicationManager.getApplication().getComponent(FrcApplicationComponent.class, defaultInstance);
        return component != null ? component : defaultInstance;
    }
    

    @Override
    public void initComponent()
    {
        LOG.debug("[FRC] " + getClass().getSimpleName() + ".initComponent() called");
        Logger baseLogger = Logger.getInstance("#net.javaru.iip.frc");
        baseLogger.info("[FRC] isDebugEnabled = " + baseLogger.isDebugEnabled() + "  isTraceEnabled = " + baseLogger.isTraceEnabled() );

        final FrcApplicationSettings settings = FrcApplicationSettings.Settings.INSTANCE();
        settings.incrementRunCount();
        
        if (!settings.isTeamNumberConfigured() && settings.getPrc() <= TEAM_NUM_NOTIFY_RUN_COUNT_APP_LEVEL)
        {
            FrcNotifications.notifyAboutTeamNumberNeedingToBeConfigured(null, true);
        }
    }
    
    


    @Override
    public void disposeComponent()
    {
        LOG.debug("[FRC] " + getClass().getSimpleName() + ".disposeComponent() called");
    }


}
