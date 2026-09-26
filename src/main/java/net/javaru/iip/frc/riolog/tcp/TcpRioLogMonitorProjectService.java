/*
 * Copyright 2015-2021 the original author or authors.
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

package net.javaru.iip.frc.riolog.tcp;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.riolog.AbstractRioLogContentExecutor;
import net.javaru.iip.frc.riolog.AbstractRioLogMonitorProjectService;



public class TcpRioLogMonitorProjectService extends AbstractRioLogMonitorProjectService
{
    private static final Logger LOG = Logger.getInstance(TcpRioLogMonitorProjectService.class);

    public static TcpRioLogMonitorProjectService getInstance(@NotNull Project project)
    {
        return project.getService(TcpRioLogMonitorProjectService.class);
    }


    /**
     * Do not call the constructor directly. Use as a project service via {@code com.intellij.openapi.components.ServiceManager}:<br/>
     * <pre>
     * final AbstractRioLogMonitorProjectService rioLogConsoleProjectService = project.getService(TcpRioLogMonitorProjectService.class);
     * </pre>
     * or use the {@link #getInstance(Project)} convenience method
     * @param myProject the project
     */
    private TcpRioLogMonitorProjectService(@NotNull Project myProject)
    {
        super(myProject);
        LOG.debug("[FRC] TcpRioLogMonitorProjectService constructor called.");
    }


    @NotNull
    protected AbstractRioLogContentExecutor createRioLogContentExecutor()
    {
        return new TcpRioLogFrcWindowContentExecutor(myProject, true);
    }
}
