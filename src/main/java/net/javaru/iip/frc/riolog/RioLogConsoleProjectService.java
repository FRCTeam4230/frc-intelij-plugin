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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.facet.Facet;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import net.javaru.iip.frc.riolog.ssh.SshRioLogConsoleProjectService;
import net.javaru.iip.frc.riolog.udp.UdpRioLogConsoleProjectService;



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

    private final RioLogMonitorProjectService udpRioLogConsoleProjectService;
    private final RioLogMonitorProjectService sshRioLogConsoleProjectService;

    @Nullable
    private AbstractRioLogContentExecutor udpContentExecutor;

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


    public static void updateAllOpenProjects()
    {
        final Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : openProjects)
        {
            update(project);
        }
    }


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


    public static void activateBothSafely(@Nullable Facet facet)
    {
        if (facet != null)
        {
            final Project project = facet.getModule().getProject();
            getInstance(project).activateBothSafely();
        }
    }


    public static void activateBothSafely(@Nullable Module module)
    {
        if (module != null)
        {
            final Project project = module.getProject();
            getInstance(project).activateBothSafely();
        }
    }


    public static void activateBothSafely(@Nullable Project project)
    {
        if (project != null)
        {
            getInstance(project).activateBothSafely();
        }
    }


    public static void activateBothNow(@Nullable Facet facet)
    {
        if (facet != null)
        {
            final Project project = facet.getModule().getProject();
            getInstance(project).activateBothNow();
        }
    }


    public static void activateBothNow(@Nullable Module module)
    {
        if (module != null)
        {
            final Project project = module.getProject();
            getInstance(project).activateBothNow();
        }
    }


    public static void activateBothNow(@Nullable Project project)
    {
        if (project != null)
        {
            getInstance(project).activateBothNow();
        }
    }


    public static void activateUdpSafely(@Nullable Facet facet)
    {
        if (facet != null)
        {
            final Project project = facet.getModule().getProject();
            getInstance(project).activateUdpSafely();
        }
    }


    public static void activateUdpSafely(@Nullable Module module)
    {
        if (module != null)
        {
            final Project project = module.getProject();
            getInstance(project).activateUdpSafely();
        }
    }


    public static void activateUdpSafely(@Nullable Project project)
    {
        if (project != null)
        {
            getInstance(project).activateUdpSafely();
        }
    }


    public static void activateUdpNow(@Nullable Facet facet)
    {
        if (facet != null)
        {
            final Project project = facet.getModule().getProject();
            getInstance(project).activateUdpNow();
        }
    }


    public static void activateUdpNow(@Nullable Module module)
    {
        if (module != null)
        {
            final Project project = module.getProject();
            getInstance(project).activateUdpNow();
        }
    }


    public static void activateUdpNow(@Nullable Project project)
    {
        if (project != null)
        {
            getInstance(project).activateUdpNow();
        }
    }

    public static void activateSshSafely(@Nullable Facet facet)
    {
        if (facet != null)
        {
            final Project project = facet.getModule().getProject();
            getInstance(project).activateSshSafely();
        }
    }


    public static void activateSshSafely(@Nullable Module module)
    {
        if (module != null)
        {
            final Project project = module.getProject();
            getInstance(project).activateSshSafely();
        }
    }


    public static void activateSshSafely(@Nullable Project project)
    {
        if (project != null)
        {
            getInstance(project).activateSshSafely();
        }
    }


    public static void activateSshNow(@Nullable Facet facet)
    {
        if (facet != null)
        {
            final Project project = facet.getModule().getProject();
            getInstance(project).activateSshNow();
        }
    }


    public static void activateSshNow(@Nullable Module module)
    {
        if (module != null)
        {
            final Project project = module.getProject();
            getInstance(project).activateSshNow();
        }
    }


    public static void activateSshNow(@Nullable Project project)
    {
        if (project != null)
        {
            getInstance(project).activateSshNow();
        }
    }


    public static void stopUdp(@Nullable Facet facet)
    {
        if (facet != null)
        {
            final Project project = facet.getModule().getProject();
            getInstance(project).stopUdp();
        }
    }

    public static void stopUdp(@Nullable Module module)
    {
        if (module != null)
        {
            final Project project = module.getProject();
            getInstance(project).stopUdp();
        }
    }

    public static void stopUdp(@Nullable Project project)
    {
        if (project != null)
        {
            getInstance(project).stopUdp();
        }
    }

    public static void stopSsh(@Nullable Facet facet)
    {
        if (facet != null)
        {
            final Project project = facet.getModule().getProject();
            getInstance(project).stopSsh();
        }
    }

    public static void stopSsh(@Nullable Module module)
    {
        if (module != null)
        {
            final Project project = module.getProject();
            getInstance(project).stopSsh();
        }
    }

    public static void stopSsh(@Nullable Project project)
    {
        if (project != null)
        {
            getInstance(project).stopSsh();
        }
    }
    
    
    public static void stopBoth(@Nullable Facet facet)
    {
        if (facet != null)
        {
            final Project project = facet.getModule().getProject();
            getInstance(project).stopBoth();
        }
    }

    public static void stopBoth(@Nullable Module module)
    {
        if (module != null)
        {
            final Project project = module.getProject();
            getInstance(project).stopBoth();
        }
    }

    public static void stopBoth(@Nullable Project project)
    {
        if (project != null)
        {
            getInstance(project).stopBoth();
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
    public static RioLogConsoleProjectService getInstance(@NotNull Project project)
    {
        return ServiceManager.getService(project, RioLogConsoleProjectService.class);
    }


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
        this.udpRioLogConsoleProjectService = UdpRioLogConsoleProjectService.getInstance(myProject);
        this.sshRioLogConsoleProjectService = SshRioLogConsoleProjectService.getInstance(myProject);
    }


    /**
     * Updates the RioLog Console view creating/opening, destroying/closing, or moving it as needed, or moving it
     * as needed based on the state of the UI and the current configuration of the project and the presence of any FRC facets.
     */
    public synchronized void update()
    {
        sshRioLogConsoleProjectService.update();
        udpRioLogConsoleProjectService.update();
    }


    public void activateBothSafely()
    {
        sshRioLogConsoleProjectService.activateSafely();
        udpRioLogConsoleProjectService.activateSafely();
    }


    public void activateBothNow()
    {
        sshRioLogConsoleProjectService.activateNow();
        udpRioLogConsoleProjectService.activateNow();
    }


    public void activateUdpSafely()
    {
        udpRioLogConsoleProjectService.activateSafely();
    }


    public void activateUdpNow()
    {
        udpRioLogConsoleProjectService.activateNow();
    }

    public void activateSshSafely()
    {
        sshRioLogConsoleProjectService.activateSafely();
    }


    public void activateSshNow()
    {
        sshRioLogConsoleProjectService.activateNow();
    }


    private void closeContentExecutor()
    {
        sshRioLogConsoleProjectService.closeContentExecutor();
        udpRioLogConsoleProjectService.closeContentExecutor();
    }
    
    
    public void stopUdp()
    {
        udpRioLogConsoleProjectService.stop();
    }
    
    public void stopSsh()
    {
        sshRioLogConsoleProjectService.stop();
    }
    
    public void stopBoth()
    {
        stopSsh();
        stopUdp();
    }
}
