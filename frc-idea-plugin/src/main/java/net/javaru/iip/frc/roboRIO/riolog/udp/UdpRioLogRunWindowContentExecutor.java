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

package net.javaru.iip.frc.roboRIO.riolog.udp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.execution.Executor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowId;

import net.javaru.iip.frc.roboRIO.riolog.AbstractRioLogContentExecutor;
import net.javaru.iip.frc.roboRIO.riolog.RioLogGlobals;
import net.javaru.iip.frc.roboRIO.riolog.RioLogMonitoringProcess;



public class UdpRioLogRunWindowContentExecutor extends AbstractRioLogContentExecutor
{
    public UdpRioLogRunWindowContentExecutor(@NotNull Project project, boolean activateToolWindow)
    {
        this(project, activateToolWindow, null);
    }


    public UdpRioLogRunWindowContentExecutor(@NotNull Project project, boolean activateToolWindow, @Nullable Runnable afterCompletionRunnable)
    {
        super(project, activateToolWindow, afterCompletionRunnable);
    }


    @Override
    public String getToolWindowId() {return ToolWindowId.RUN;}


    @Override
    public String getTabTitle() { return RioLogGlobals.UDP_TAB_TITLE; }

    @Override
    protected Executor createExecutor() { return DefaultRunExecutor.getRunExecutorInstance(); }


    @NotNull
    protected RioLogMonitoringProcess createRioLogMonitoringProcess()
    {
        return new UdpRioLogMonitoringProcess(this::invokeClearAll);
    }
}
