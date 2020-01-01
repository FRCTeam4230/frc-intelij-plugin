/*
 * Copyright 2015-2020 the original author or authors
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

package net.javaru.iip.frc.components;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.settings.FrcApplicationSettings;

import static net.javaru.iip.frc.FrcPluginGlobals.TEAM_NUM_NOTIFY_RUN_COUNT_APP_LEVEL;

/*
    TODO: Issue #55: Migrate Plugin Components to Services, Extensions or Listeners as Components will become deprecated in the IntelliJ Plugin API

    TODO: Issue #26:  Migrate this ApplicationComponent to a "true" ApplicationService (or possibly remove it completely)
    
    The FrcApplicationComponent interface was originally extending ApplicationComponent
    However, the ApplicationComponent was deprecated. No methods from it were being 
    used, but the initComponent() method from its super interface, BaseComponent, is being 
    used to ensure the team number is configured and to increment the run count. The
    deprecation information in ApplicationComponent says to use 
    com.intellij.util.messages.MessageBus instead (full message below). For now, I changed
    the FrcApplicationComponent interface so it extends BaseComponent (rather than the deprecated
    ApplicationComponent). But at some point I should look at converting this class to be a true 
    ApplicationService. At this point, I probably do not need the "configure team" notification
    to happen immediately on IDE start (after the plugin is installed). I can do that the first time
    a project is loaded or the FRC project wizard is used.
    
    Deprecation Information:
    
    Please use application services or extensions instead of application component, because if you 
    register a class as an application component it will be loaded, its instance will be created 
    and initComponent() methods will be called each time IDE is started even if user doesn't use any 
    feature of your plugin. So consider using specific extensions instead to ensure that the plugin 
    will not impact IDE performance until user calls its actions explicitly.
    Deprecated:  This interface is not used anymore. Application component do no need to extend any 
    special interface. Instead of initComponent() please use com.intellij.util.messages.MessageBus 
    and corresponding topics. Instead of disposeComponent() please use com.intellij.openapi.Disposable. 
    If for some reasons replacing disposeComponent() / initComponent() is not a option, BaseComponent 
    can be extended.

 */

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
        baseLogger.info("[FRC] >>> isDebugEnabled = " + baseLogger.isDebugEnabled() + "  isTraceEnabled = " + baseLogger.isTraceEnabled() + " <<<");

        final FrcApplicationSettings settings = FrcApplicationSettings.getInstance();
        settings.incrementRunCount();
        
        if (!settings.isTeamNumberConfigured() && settings.getPrc() <= TEAM_NUM_NOTIFY_RUN_COUNT_APP_LEVEL)
        {
            FrcNotifications.Companion.notifyAboutTeamNumberNeedingToBeConfigured(null, true, false);
        }
    }
    
    


    @Override
    public void disposeComponent()
    {
        LOG.debug("[FRC] " + getClass().getSimpleName() + ".disposeComponent() called");
    }


}
