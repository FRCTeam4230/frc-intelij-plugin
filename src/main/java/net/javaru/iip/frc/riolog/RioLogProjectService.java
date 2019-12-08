/*
 * Copyright 2015-2019 the original author or authors
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

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import net.javaru.iip.frc.riolog.ssh.SshRioLogMonitorProjectService;
import net.javaru.iip.frc.riolog.tcp.TcpRioLogMonitorProjectService;
import net.javaru.iip.frc.riolog.udp.UdpRioLogMonitorProjectService;
import net.javaru.iip.frc.settings.FrcProjectTeamNumberChangeListener;
import net.javaru.iip.frc.settings.FrcProjectTeamNumberService;
import net.javaru.iip.frc.settings.FrcRoboRioSettings;



/**
 * Class that manages the displaying of the RioLog console window. It {@link #update() updates} the RioLog Console
 * view creating/opening, destroying/closing, or moving it as needed based on the state of the UI and on the current
 * configuration of the project and the presence of any FRC facets.
 * Access via the {@link #getInstance(Project)} method or as a Project Service via the IntelliJ
 * <a href="http://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_services.html">Plugin Services</a>.
 * For example:
 * <pre>
 * final RioLogProjectService rioLogConsoleProjectService = ServiceManager.getService(project, RioLogProjectService.class);
 * </pre>
 * There are also three static {@code update} methods that can be used when the caller has access to a facet, a module, or a project.
 */
public class RioLogProjectService implements FrcProjectTeamNumberChangeListener
{
    private static final Logger LOG = Logger.getInstance(RioLogProjectService.class);

    @NotNull
    private final Project myProject;

    private final AbstractRioLogMonitorProjectService udpRioLogConsoleProjectService;
    private final AbstractRioLogMonitorProjectService sshRioLogConsoleProjectService;
    private final AbstractRioLogMonitorProjectService tcpRioLogConsoleProjectService;

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
            getInstance(project).update();
        }
    }

    public static RioLogProjectService getInstance(@NotNull Module module)
    {
        return getInstance(module.getProject());
    }
    
    public static RioLogProjectService getInstance(@NotNull Project project)
    {
        return ServiceManager.getService(project, RioLogProjectService.class);
    }


    /**
     * Do not call the constructor directly. Use as a project service via {@code com.intellij.openapi.components.ServiceManager}:<br/>
     * <pre>
     * final RioLogProjectService rioLogConsoleProjectService = ServiceManager.getService(project, RioLogProjectService.class);
     * </pre>
     *
     * @param myProject the project
     */
    private RioLogProjectService(@NotNull Project myProject)
    {
        LOG.debug("[FRC] RioLogProjectService constructor called.");
        this.myProject = myProject;
        myProject.getMessageBus().connect().subscribe(FrcProjectTeamNumberService.getPROJECT_TEAM_NUMBER_CHANGES(), this);
        this.udpRioLogConsoleProjectService = UdpRioLogMonitorProjectService.getInstance(myProject);
        this.sshRioLogConsoleProjectService = SshRioLogMonitorProjectService.getInstance(myProject);
        this.tcpRioLogConsoleProjectService = TcpRioLogMonitorProjectService.getInstance(myProject);
    }


    /**
     * Updates the RioLog Console view creating/opening, destroying/closing, or moving it as needed, or moving it
     * as needed based on the state of the UI and the current configuration of the project and the presence of any FRC facets.
     */
    public synchronized void update()
    {
        //For now, we are only going to update the primary console
        tcpRioLogConsoleProjectService.update();
    }

    public synchronized void updateAll()
    {
        //For now, we are only going to update the primary console
        sshRioLogConsoleProjectService.update();
        udpRioLogConsoleProjectService.update();
        tcpRioLogConsoleProjectService.update();
    }

    public synchronized void updateUdp()
    {
        udpRioLogConsoleProjectService.update();
    }

    public synchronized void updateSsh()
    {
        sshRioLogConsoleProjectService.update();
    }

    public synchronized void updateTcp() { tcpRioLogConsoleProjectService.update(); }
    
    
    @Override
    public void onTeamNumberChange(int previousTeamNumber, int newTeamNumber)
    {
        LOG.debug("[FRC] RioLog Service Responding to a change in the Team Number from '" + previousTeamNumber + "' to '" + newTeamNumber + "'.");
        FrcRoboRioSettings.getInstance(myProject).setTeamNumber(newTeamNumber); // FrcRoboRioSettings has its own listener, but since we can't be sure of the call order, we set it here as well.
        updateAll();
    }
    
    
    public void activateUdp()
    {
        udpRioLogConsoleProjectService.activate();
    }

    public void activateTcp()
    {
        tcpRioLogConsoleProjectService.activate();
    }

    public void activateSsh()
    {
        sshRioLogConsoleProjectService.activate();
    }

    public void stopUdp()
    {
        udpRioLogConsoleProjectService.stop();
    }
    
    public void stopTcp()
    {
        udpRioLogConsoleProjectService.stop();
    }
    
    public void stopSsh()
    {
        sshRioLogConsoleProjectService.stop();
    }
    
    public void stopAll()
    {
        stopSsh();
        stopUdp();
        stopTcp();
    }
}
