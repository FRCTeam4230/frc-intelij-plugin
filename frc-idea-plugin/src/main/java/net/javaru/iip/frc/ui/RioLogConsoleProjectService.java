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

package net.javaru.iip.frc.ui;

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
import net.javaru.iip.frc.facet.FrcFacetType;
import net.javaru.iip.frc.settings.FrcApplicationComponent;
import net.javaru.iip.frc.settings.FrcSettings;



/**
 * Access as a Project Service via the IntelliJ 
 * <a href="http://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_services.html">Plugin Services</a>.
 * For example:
 * <pre>
 * final RioLogConsoleProjectService rioLogConsoleProjectService = ServiceManager.getService(project, RioLogConsoleProjectService.class);
 * </pre>
 */
public class RioLogConsoleProjectService 
{
    private static final Logger LOG = Logger.getInstance(RioLogConsoleProjectService.class);
    
    @NotNull
    private final Project myProject;

    @Nullable
    private AbstractRioLogContentExecutor contentExecutor;

    
    /**
     * Do not call the constructor directly. Use as a project service:<br/>
     * <pre>
     * final RioLogConsoleProjectService rioLogConsoleProjectService = ServiceManager.getService(project, RioLogConsoleProjectService.class);
     * </pre>
     * @param myProject the project
     */
    private RioLogConsoleProjectService(@NotNull Project myProject)
    {
        this.myProject = myProject;
    }
    
    public void update()
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

        final FrcSettings frcSettings = FrcApplicationComponent.getInstance().getState();
        final boolean useRunWindow = (frcSettings != null && !frcSettings.isUseFrcToolWindow());
        
        LOG.debug("[FRC] needConsole:  " + needConsole);
        LOG.debug("[FRC] haveConsole:  " + (contentExecutor != null));
        LOG.debug("[FRC] useRunWindow: " + useRunWindow);
        
        
        // Case 1 - we have and need it, but we need to check if we have the right type (i.e. settings change)
        if (needConsole && contentExecutor != null)
        {
            LOG.debug("[FRC] Case 1: have console and need it. Checking if correct type.");

            //do we have the right console?
            if (useRunWindow && RioLogFrcWindowRunExecutor.TOOL_WINDOW_ID.equals(contentExecutor.getToolWindowId()))
            {
                //We have a FRC Tool Window, but need a Run Window
                LOG.debug("[FRC] Case 1.1: have a FRC Tool Window, but need a Run Tab. Closing FRC Tool Window and creating Run tab");
                closeContentExecutor();
                createContentExecutor(true);
            }
            else if (!useRunWindow && ToolWindowId.RUN.equals(contentExecutor.getToolWindowId()))
            {
                //We have a run window, but need a FRC tool window
                LOG.debug("[FRC] Case 1.2: have a Run tab, but need a FRC Tool Window. Closing Run tab and creating FRC Tool Window");
                closeContentExecutor();
                createContentExecutor(false);
            }
            else 
            {
                LOG.debug("[FRC] Case 1.3: have console, need it, and it is the right type. No action needed.");
            }
        }
        // Case 2 - we need it, but don't have it
        else if (needConsole)
        {
            LOG.debug("[FRC] Case 2: need a console, but we don't have one. Creating one.");
            createContentExecutor(useRunWindow);
        }
        // Case 3 we have it, but don't need it
        else if (contentExecutor != null)
        {
            LOG.debug("[FRC] Case 3: We have a console, but don't need it. Closing it.");
            closeContentExecutor();
        }
        else 
        {   
            //Case 4, we don't have it and don't need it... so do nothing
            LOG.debug("[FRC] Case 4: We don't have a console window an we don't need one. No action needed."); 
        }
    }


    private void closeContentExecutor()
    {
        if (contentExecutor != null)
        {
            contentExecutor.dispose();
            contentExecutor = null;
        }
        
    }
    
    private void createContentExecutor(final boolean useRunWindow)
    {
        LOG.debug("[FRC] Creating AbstractRioLogContentExecutor");
        contentExecutor = useRunWindow ? new RioLogRunWindowContentExecutor(myProject, true) : new RioLogFrcWindowContentExecutor(myProject, true);
        Disposer.register(myProject, contentExecutor);
        contentExecutor.run();
    }


    private boolean moduleHasFrcFacet(@NotNull Module module)
    {
        final FrcFacet frcFacet = checkForFrcFacet(module);
        return frcFacet != null;
    }


    @Nullable
    private FrcFacet checkForFrcFacet(@NotNull Module module)
    {
        final FacetManager facetManager = FacetManager.getInstance(module);
        final Collection<FrcFacet> facetsByType = facetManager.getFacetsByType(FrcFacetType.FACET_TYPE_ID);
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
