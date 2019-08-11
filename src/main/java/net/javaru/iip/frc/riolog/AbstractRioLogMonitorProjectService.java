/*
 * Copyright 2015-2018 the original author or authors
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

package net.javaru.iip.frc.riolog;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowId;
import com.intellij.openapi.wm.ToolWindowManager;

import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.riolog.ui.FrcRioLogToolWindowExecutor;
import net.javaru.iip.frc.settings.FrcApplicationSettings;



/**
 * Service that manages RioLog monitoring. 
 */
public abstract class AbstractRioLogMonitorProjectService
{

    // TODO: This class needs a some refactoring/rework. 
    //       It's kind of grown into a mess as it was written when first starting 
    //       out with less understanding of the IntelliJ IDEA plugin API. And the
    //       idea of having an option to appear in the Run Tool Window as an option
    //       isn't really adding much as a feature since in normal robot dev work
    //       you are not doing much in the run window. _Maybe_ an option to have
    //       it as a tab in the debug window might be cool... but a low priority
    //       Also, its not acting as a ProjectService but more an ApplicationService
    //       We need to fix that and have better cross project support in the event 
    //       multiple FRC projects are open
    
    private static final Logger LOG = Logger.getInstance(AbstractRioLogMonitorProjectService.class);
    @NotNull
    protected final Project myProject;
    @Nullable
    private AbstractRioLogContentExecutor contentExecutor;

    protected AbstractRioLogMonitorProjectService(@NotNull Project myProject) {this.myProject = myProject;}


    public synchronized void update()
    {
        final Module[] modules = ModuleManager.getInstance(myProject).getModules();

        boolean needConsole = false;
        for (Module module : modules)
        {
            if (moduleHasFrcFacet(module))
            {
                needConsole = true;
                break;
            }
        }

        final FrcApplicationSettings frcSettings = FrcApplicationSettings.Settings.INSTANCE();
        final boolean useRunWindow = !frcSettings.getUseFrcToolWindow();
        
        final int configuredPort = frcSettings.getRioLogUdpPort();
        final int currentPort = determineCurrentlyMonitoredPort();
        final boolean portBounceNeeded = currentPort != -1 && currentPort != configuredPort;
        final boolean haveConsole = contentExecutor != null && isFrcToolWindowAvailable();


        final Thread currentThread = Thread.currentThread();
        LOG.debug("[FRC]     Current thread:   " + currentThread.getId() + " :: " + currentThread.getName());
        LOG.debug("[FRC]     needConsole:      " + needConsole);
        LOG.debug("[FRC]     haveConsole:      " + haveConsole);
        LOG.debug("[FRC]     useRunWindow:     " + useRunWindow);
        LOG.debug("[FRC]     configuredPort:   " + configuredPort);
        LOG.debug("[FRC]     currentPort:      " + currentPort);
        LOG.debug("[FRC]     portBounceNeeded: " + portBounceNeeded);


        if (needConsole && haveConsole && contentExecutor.getRioLogMonitorProcess() != null && contentExecutor.getRioLogMonitorProcess().isEnabled())
        {
            // Case 1 - we have and need it, but we need to check if we have the right type (i.e. settings change)
            LOG.debug("[FRC] Case 1: have console and need it. Checking if correct type & port. Project is: " + myProject.getName());

            //do we have the right console?
            if (useRunWindow && FrcRioLogToolWindowExecutor.FRC_RIO_LOG_TOOL_WINDOW_ID.equals(contentExecutor.getToolWindowId()))
            {
                //We have a FRC Tool Window, but need a Run Window
                LOG.debug("[FRC] Case 1.1: have a FRC Tool Window, but need a Run Tab. Closing FRC Tool Window and creating Run tab. Project is: "
                          + myProject.getName());
                closeContentExecutor();
                initContentExecutor(true, false);
            }
            else if (!useRunWindow && ToolWindowId.RUN.equals(contentExecutor.getToolWindowId()))
            {
                //We have a run window, but need a FRC tool window
                LOG.debug("[FRC] Case 1.2: have a Run tab, but need a FRC Tool Window. Closing Run tab and creating FRC Tool Window. Project is: "
                          + myProject.getName());
                closeContentExecutor();
                initContentExecutor(false, false);
            }
            else
            {
                if (portBounceNeeded)
                {
                    LOG.debug("[FRC] Case 1.3A: have console, need it, and it is the right type, but port has changed. Triggering 'reRun' action. Project is: "
                              + myProject.getName());
                    if (contentExecutor != null) {contentExecutor.reRun();}
                }
                else
                {
                    LOG.debug("[FRC] Case 1.3B: have console, need it, and it is the right type, listening on the correct port. Just need to activate it. Project is: "
                              + myProject.getName());
                    activate();
                }
            }
        }
        else if (needConsole && haveConsole && contentExecutor.getRioLogMonitorProcess() != null && !contentExecutor.getRioLogMonitorProcess().isEnabled())
        {
            // Case 2 - We need it, have, but its just not enabled
            LOG.debug("[FRC] Case 2: need a console, have it, but its not enabled. Activating it. Project is: " + myProject.getName());
            activate();
        }
        else if (needConsole)
        {
            // Case 3 - we need it, but don't have it
            LOG.debug("[FRC] Case 3: need a console, but we don't have one. Creating one. Project is: " + myProject.getName());
            initContentExecutor(useRunWindow, true);
        }
        else if (haveConsole)
        {
            // Case 4 we have it, but don't need it
            LOG.debug("[FRC] Case 4: We have a console, but don't need it. Closing it. Project is: " + myProject.getName());
            closeContentExecutor();
        }
        else
        {
            //Case 5, we don't have it and don't need it... so do nothing
            LOG.debug("[FRC] Case 5: We don't have a console window an we don't need one. No action needed. Project is: " + myProject.getName());
        }
    }


    protected boolean isFrcToolWindowAvailable()
    {
        if (contentExecutor != null)
        {
            final ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(myProject);
            if (toolWindowManager != null)
            {
                final ToolWindow toolWindow = toolWindowManager.getToolWindow(contentExecutor.getToolWindowId());
                if (toolWindow != null)
                {
                    return toolWindow.isAvailable();
                }
            }
        }
        
        return false;
    }


    private int determineCurrentlyMonitoredPort()
    {
        if (contentExecutor != null)
        {
            final AbstractRioLogMonitorProcess rioLogMonitorProcess = contentExecutor.getRioLogMonitorProcess();
            if (rioLogMonitorProcess != null)
            {
                return rioLogMonitorProcess.getMonitoredPort();
            }
        }
        return -1;
    }


    public void activate()
    {
        if (contentExecutor != null)
        {
            LOG.debug("[FRC] activate called for " + contentExecutor.toString());
            contentExecutor.activateRioLogConsole();
        }
        else
        {
            LOG.debug("[FRC] activate called, but contentExecutor is null. No action taken.");
        }
    }

    public void stop()
    {
        if (contentExecutor != null)
        {
            contentExecutor.invokeStop();
        }
    }

    public void closeContentExecutor()
    {
        if (contentExecutor != null)
        {
            try
            {
                contentExecutor.close();
            }
            catch (Exception e)
            {
                LOG.debug("[FRC] An exception occurred when closing the contentExecutor. Cause Summary: " + e.toString(), e);
            }
            contentExecutor = null;
        }
    }


    private void initContentExecutor(final boolean useRunWindow, final boolean isFreshActivation)
    {
        LOG.debug("[FRC] Creating AbstractRioLogContentExecutor: useRunWindow=" + useRunWindow +  " isFreshActivation=" + isFreshActivation);
        try
        {
            contentExecutor = createRioLogContentExecutor(useRunWindow);
            Disposer.register(myProject, contentExecutor);
            contentExecutor.run(isFreshActivation);
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] An exception occurred when creating the content executor. Cause Summary: " + e.toString(), e);
            try
            {
                closeContentExecutor();
            }
            catch (Exception ignore) {}
        }
    }


    @NotNull
    protected abstract AbstractRioLogContentExecutor createRioLogContentExecutor(boolean useRunWindow);


    private boolean moduleHasFrcFacet(@NotNull Module module)
    {
        final FrcFacet frcFacet = checkForFrcFacet(module);
        final boolean moduleHasFrcFacet = frcFacet != null;
        LOG.debug("[FRC] The module " + module.getName() + " has FRC Facet: " + frcFacet);
        return moduleHasFrcFacet;
    }


    @Nullable
    private FrcFacet checkForFrcFacet(@NotNull Module module)
    {
        final FacetManager facetManager = FacetManager.getInstance(module);
        final Collection<FrcFacet> facetsByType = facetManager.getFacetsByType(FrcFacet.Companion.getFACET_TYPE_ID());
        if (facetsByType.isEmpty())
        {
            return null;
        }
        else
        {
            return facetsByType.iterator().next();
        }
    }
}
