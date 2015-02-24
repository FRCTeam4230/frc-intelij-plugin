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

import org.jetbrains.annotations.NotNull;
import com.intellij.execution.Executor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.project.Project;




public class RioLogFrcWindowContentExecutor extends AbstractRioLogContentExecutor
{
    public RioLogFrcWindowContentExecutor(@NotNull Project project, @NotNull ProcessHandler process) { super(project, process); }


    @Override
    protected String getToolWindowId() { return RioLogFrcWindowRunExecutor.TOOL_WINDOW_ID; }


    @Override
    protected Executor createExecutor() { return RioLogFrcWindowRunExecutor.getRunExecutorInstance(); }
}
