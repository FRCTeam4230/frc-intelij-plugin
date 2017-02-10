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



public class RioLogRunWindowContentExecutor extends AbstractRioLogContentExecutor
{
    public static final String TOOL_WINDOW_ID = ToolWindowId.RUN;


    RioLogRunWindowContentExecutor(@NotNull Project project, boolean activateToolWindow)
    {
        this(project, activateToolWindow, null);
    }


    RioLogRunWindowContentExecutor(@NotNull Project project, boolean activateToolWindow, @Nullable Runnable afterCompletionRunnable)
    {
        super(project, activateToolWindow, afterCompletionRunnable);
    }


    @Override
    protected String getToolWindowId() {return TOOL_WINDOW_ID;}


    @Override
    protected Executor createExecutor() { return DefaultRunExecutor.getRunExecutorInstance(); }
}
