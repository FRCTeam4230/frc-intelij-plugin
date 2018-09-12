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

package net.javaru.iip.frc.riolog.udp;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.riolog.AbstractRioLogContentExecutor;
import net.javaru.iip.frc.riolog.AbstractRioLogMonitorProjectService;



public class UdpRioLogMonitorProjectService extends AbstractRioLogMonitorProjectService
{
    private static final Logger LOG = Logger.getInstance(UdpRioLogMonitorProjectService.class);

    public static UdpRioLogMonitorProjectService getInstance(@NotNull Project project)
    {
        return ServiceManager.getService(project, UdpRioLogMonitorProjectService.class);
    }


    /**
     * Do not call the constructor directly. Use as a project service via {@code com.intellij.openapi.components.ServiceManager}:<br/>
     * <pre>
     * final AbstractRioLogMonitorProjectService rioLogConsoleProjectService = ServiceManager.getService(project, UdpRioLogMonitorProjectService.class);
     * </pre>
     * or use the {@link #getInstance(Project)} convenience method
     * @param myProject the project
     */
    private UdpRioLogMonitorProjectService(@NotNull Project myProject)
    {
        super(myProject);
        LOG.debug("[FRC] UdpRioLogMonitorProjectService constructor called.");
    }


    @NotNull
    protected AbstractRioLogContentExecutor createRioLogContentExecutor(boolean useRunWindow)
    {
        return useRunWindow ? new UdpRioLogRunWindowContentExecutor(myProject, true) : new UdpRioLogFrcWindowContentExecutor(myProject, true);
    }
}
