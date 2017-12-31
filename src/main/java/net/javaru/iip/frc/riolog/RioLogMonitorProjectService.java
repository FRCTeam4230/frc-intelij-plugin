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
import com.intellij.openapi.wm.ToolWindowId;

import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.riolog.ui.FrcRioLogToolWindowExecutor;
import net.javaru.iip.frc.settings.FrcApplicationSettings;



public abstract class RioLogMonitorProjectService
{
    private static final Logger LOG = Logger.getInstance(RioLogMonitorProjectService.class);
    @NotNull
    protected final Project myProject;
    @Nullable
    private AbstractRioLogContentExecutor udpContentExecutor;


    protected RioLogMonitorProjectService(@NotNull Project myProject) {this.myProject = myProject;}


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

        // TODO: Move to application service so it works across multiple projects
        final int configuredPort = frcSettings.getRioLogUdpPort();
        final int currentPort = determineCurrentlyMonitoredPort();
        final boolean portBounceNeeded = currentPort != -1 && currentPort != configuredPort;


        final Thread currentThread = Thread.currentThread();
        LOG.debug("[FRC] Current thread:   " + currentThread.getId() + " :: " + currentThread.getName());
        LOG.debug("[FRC] needConsole:      " + needConsole);
        LOG.debug("[FRC] haveConsole:      " + (udpContentExecutor != null));
        LOG.debug("[FRC] useRunWindow:     " + useRunWindow);
        LOG.debug("[FRC] configuredPort:   " + configuredPort);
        LOG.debug("[FRC] currentPort:      " + currentPort);
        LOG.debug("[FRC] portBounceNeeded: " + portBounceNeeded);


        if (needConsole && udpContentExecutor != null && udpContentExecutor.getRioLogMonitoringProcess() != null
            && udpContentExecutor.getRioLogMonitoringProcess().isEnabled())
        {
            // Case 1 - we have and need it, but we need to check if we have the right type (i.e. settings change)
            LOG.debug("[FRC] Case 1: have console and need it. Checking if correct type & port. Project is: " + myProject.getName());

            //do we have the right console?
            if (useRunWindow && FrcRioLogToolWindowExecutor.FRC_RIO_LOG_TOOL_WINDOW_ID.equals(udpContentExecutor.getToolWindowId()))
            {
                //We have a FRC Tool Window, but need a Run Window
                LOG.debug("[FRC] Case 1.1: have a FRC Tool Window, but need a Run Tab. Closing FRC Tool Window and creating Run tab. Project is: "
                          + myProject.getName());
                closeContentExecutor();
                initContentExecutor(true);
            }
            else if (!useRunWindow && ToolWindowId.RUN.equals(udpContentExecutor.getToolWindowId()))
            {
                //We have a run window, but need a FRC tool window
                LOG.debug("[FRC] Case 1.2: have a Run tab, but need a FRC Tool Window. Closing Run tab and creating FRC Tool Window. Project is: "
                          + myProject.getName());
                closeContentExecutor();
                initContentExecutor(false);
            }
            else
            {
                if (portBounceNeeded)
                {
                    LOG.debug("[FRC] Case 1.3A: have console, need it, and it is the right type, but port has changed. Triggering 'reRun' action. Project is: "
                              + myProject.getName());
                    if (udpContentExecutor != null) {udpContentExecutor.reRun();}
                }
                else
                {
                    LOG.debug("[FRC] Case 1.3B: have console, need it, and it is the right type, listening on the correct port. No action needed. Project is: "
                              + myProject.getName());
                }
            }
        }
        else if (needConsole)
        {
            // Case 2 - we need it, but don't have it
            LOG.debug("[FRC] Case 2: need a console, but we don't have one. Creating one. Project is: " + myProject.getName());
            initContentExecutor(useRunWindow);
        }
        else if (udpContentExecutor != null)
        {
            // Case 3 we have it, but don't need it
            LOG.debug("[FRC] Case 3: We have a console, but don't need it. Closing it. Project is: " + myProject.getName());
            closeContentExecutor();
        }
        else
        {
            //Case 4, we don't have it and don't need it... so do nothing
            LOG.debug("[FRC] Case 4: We don't have a console window an we don't need one. No action needed. Project is: " + myProject.getName());
        }
    }


    private int determineCurrentlyMonitoredPort()
    {
        if (udpContentExecutor != null)
        {
            final RioLogMonitoringProcess rioLogMonitoringProcess = udpContentExecutor.getRioLogMonitoringProcess();
            if (rioLogMonitoringProcess != null)
            {
                return rioLogMonitoringProcess.getMonitoredPort();
            }
        }
        return -1;
    }


    public void activateSafely()
    {
        if (udpContentExecutor != null)
        {
            udpContentExecutor.activateRioLogConsoleSafely();
        }
    }


    public void activateNow()
    {
        if (udpContentExecutor != null)
        {
            udpContentExecutor.activateRioLogConsoleNow();
        }
    }


    public void closeContentExecutor()
    {
        if (udpContentExecutor != null)
        {
            try
            {
                udpContentExecutor.close();
            }
            catch (Exception e)
            {
                LOG.debug("[FRC] An exception occurred when closing the udpContentExecutor. Cause Summary: " + e.toString(), e);
            }
            udpContentExecutor = null;
        }
    }


    private void initContentExecutor(final boolean useRunWindow)
    {
        LOG.debug("[FRC] Creating AbstractRioLogContentExecutor");
        try
        {
            udpContentExecutor = createRioLogContentExecutor(useRunWindow);
            Disposer.register(myProject, udpContentExecutor);
            udpContentExecutor.run();
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
        final Collection<FrcFacet> facetsByType = facetManager.getFacetsByType(FrcFacet.FACET_TYPE_ID);
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
