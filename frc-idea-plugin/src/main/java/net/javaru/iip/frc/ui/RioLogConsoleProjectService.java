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
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.facet.FrcFacetType;
import net.javaru.iip.frc.settings.FrcApplicationComponent;
import net.javaru.iip.frc.settings.FrcSettings;



/**
 * Class that manages the displaying of the RioLog console window. It {@link #update() updates} the RioLog Console 
 * view creating/opening, destroying/closing, or moving it as needed based on the state of the UI and on the current
 * configuration of the project and the presence of any FRC facets.
 * Access via the {@link #getInstance(Project)} method or as a Project Service via the IntelliJ
 * <a href="http://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_services.html">Plugin Services</a>.
 * For example:
 * <pre>
 * final RioLogConsoleProjectService rioLogConsoleProjectService = ServiceManager.getService(project, RioLogConsoleProjectService.class);
 * </pre>
 * There are also three static {@code update} methods that can be used when the caller has access to a facet, a module, or a project.
 */
public class RioLogConsoleProjectService
{
    private static final Logger LOG = Logger.getInstance(RioLogConsoleProjectService.class);

    @NotNull
    private final Project myProject;

    @Nullable
    private AbstractRioLogContentExecutor contentExecutor;

    /*
        Use cases we want to be sure the RioLog Console status is updated, and where they are handled:
        
        1) Project Open
            Handled via: This classes implementation of ProjectComponent.projectOpened()
        2) Facet Added to Project
            Handled via: FrcFacetManagerListener.facetAdded() (inner class in FrcProjectComponent)
        3) Facet Removed from Project
            a) was only facet and  we want to close the console
            b) there are other FRC facets still configured on the project
            Handled via: FrcFacetManagerListener.facetRemoved() (inner class in FrcProjectComponent)
        4) New module created and facet was Added - likely dup of #2, but we want t test it
            Handled via: FrcModuleComponent.moduleAdded()
        5) Module imported (with FRC facet)
            Handled via: FrcModuleComponent.moduleAdded()
        6) Module Removed from project
            a) was only module with an FRC facet and  we want to close the console
            b) there are other modules with FRC facets still configured on the project
            Handled via: FrcModuleComponent.disposeComponent()
        7) Change to the target window in the FrcSettings
            Handled via: FrcApplicationComponent's impl of UnnamedConfigurable.apply()
        
     */

    /**
     * A null safe convenience static utility method for {@link #update() updating} the RioLog Condole for a facet.
     * Equivalent to calling:<br/><br/>
     * <pre>
     * ServiceManager.getService(facet.getModule().getProject(), RioLogConsoleProjectService.class).update();
     * </pre>
     * but with full null safety
     *
     * @param facet the facet
     */
    public static void update(@Nullable Facet facet)
    {
        if (facet != null)
        {
            final Module module = facet.getModule();
            update(module);
        }
    }


    /**
     * A null safe convenience static utility method for {@link #update() updating} the RioLog Condole for a module.
     * Equivalent to calling:<br/><br/>
     * <pre>
     * ServiceManager.getService(module.getProject(), RioLogConsoleProjectService.class).update();
     * </pre>
     * but with full null safety
     *
     * @param module the module
     */
    public static void update(@Nullable Module module)
    {
        if (module != null)
        {
            final Project project = module.getProject();
            update(project);
        }
    }


    /**
     * A null safe convenience static utility method for {@link #update() updating} the RioLog Condole for a project.
     * Equivalent to calling:<br/><br/>
     * <pre>
     * ServiceManager.getService(project, RioLogConsoleProjectService.class).update();
     * </pre>
     * but with full null safety
     *
     * @param project the project
     */
    public static void update(@Nullable Project project)
    {
        if (project != null)
        {
            getInstance(project).update();
        }
    }


    public static void activate(@Nullable Facet facet)
    {
        if (facet!= null)
        {
            final Project project = facet.getModule().getProject();
            getInstance(project).activate();
        }
    }
    
    public static void activate(@Nullable Module module)
    {
        if (module!= null)
        {
            final Project project = module.getProject();
            getInstance(project).activate();
        }
    }
    
    public static void activate(@Nullable Project project)
    {
        if (project!= null)
        {
            getInstance(project).activate();
        }
    }


    public static void close(@Nullable Facet facet)
    {
        if (facet != null)
        {
            final Project project = facet.getModule().getProject();
            getInstance(project).closeContentExecutor();
        }
    }


    public static void close(@Nullable Module module)
    {
        if (module != null)
        {
            final Project project = module.getProject();
            getInstance(project).closeContentExecutor();
        }
    }


    public static void close(@Nullable Project project)
    {
        if (project != null)
        {
            getInstance(project).closeContentExecutor();
        }
    }

    // See http://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_services.html for more information
    public static RioLogConsoleProjectService getInstance(@NotNull Project project) {return ServiceManager.getService(project, RioLogConsoleProjectService.class);}


    /**
     * Do not call the constructor directly. Use as a project service via {@code com.intellij.openapi.components.ServiceManager}:<br/>
     * <pre>
     * final RioLogConsoleProjectService rioLogConsoleProjectService = ServiceManager.getService(project, RioLogConsoleProjectService.class);
     * </pre>
     *
     * @param myProject the project
     */
    private RioLogConsoleProjectService(@NotNull Project myProject)
    {
        LOG.debug("[FRC] RioLogConsoleProjectService constructor called.");
        this.myProject = myProject;
    }


    /**
     * Updates the RioLog Console view creating/opening, destroying/closing, or moving it as needed, or moving it 
     * as needed based on the state of the UI and the current configuration of the project and the presence of any FRC facets.
     */
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


        if (needConsole && contentExecutor != null)
        {
            // Case 1 - we have and need it, but we need to check if we have the right type (i.e. settings change)
            LOG.debug("[FRC] Case 1: have console and need it. Checking if correct type. Project is: " + myProject.getName());

            //do we have the right console?
            if (useRunWindow && RioLogFrcWindowContentExecutor.TOOL_WINDOW_ID.equals(contentExecutor.getToolWindowId()))
            {
                //We have a FRC Tool Window, but need a Run Window
                LOG.debug("[FRC] Case 1.1: have a FRC Tool Window, but need a Run Tab. Closing FRC Tool Window and creating Run tab. Project is: "
                          + myProject.getName());
                closeContentExecutor();
                createContentExecutor(true);
            }
            else if (!useRunWindow && RioLogRunWindowContentExecutor.TOOL_WINDOW_ID.equals(contentExecutor.getToolWindowId()))
            {
                //We have a run window, but need a FRC tool window
                LOG.debug("[FRC] Case 1.2: have a Run tab, but need a FRC Tool Window. Closing Run tab and creating FRC Tool Window. Project is: "
                          + myProject.getName());
                closeContentExecutor();
                createContentExecutor(false);
            }
            else
            {
                LOG.debug("[FRC] Case 1.3: have console, need it, and it is the right type. No action needed. Project is: " + myProject.getName());
            }
        }
        else if (needConsole)
        {
            // Case 2 - we need it, but don't have it
            LOG.debug("[FRC] Case 2: need a console, but we don't have one. Creating one. Project is: " + myProject.getName());
            createContentExecutor(useRunWindow);
        }
        else if (contentExecutor != null)
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

    public void activate()
    {
        if (contentExecutor != null)
        {
            contentExecutor.activateRioLogConsole();
        }
    }

    private void closeContentExecutor()
    {
        if (contentExecutor != null)
        {
            try
            {
                contentExecutor.close();
            }
            catch (Exception e)
            {
                LOG.debug("[FRC] An exception occurred when closing the contentExecutor. Cause Summary: " + e.toString(),e);
            }
            contentExecutor = null;
        }
    }


    private void createContentExecutor(final boolean useRunWindow)
    {
        LOG.debug("[FRC] Creating AbstractRioLogContentExecutor");
        try
        {
            contentExecutor = useRunWindow ? new RioLogRunWindowContentExecutor(myProject, true) : new RioLogFrcWindowContentExecutor(myProject, true);
            Disposer.register(myProject, contentExecutor);
            contentExecutor.run();
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
