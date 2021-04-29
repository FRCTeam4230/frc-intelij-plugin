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

package net.javaru.iip.frc.riolog

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import net.javaru.iip.frc.riolog.ui.FrcRioLogToolWindowExecutor

// SEE COMMENT IN RioLogProjectService.getInstance() ABOUT WHY THIS IS NOT IN USE AT THIS TIME
fun registerFrcToolWindowIfNeeded(project: Project): ToolWindow
{
    // It's Much easier to do this Kotlin than Java so we can take advantage of parameter defaults
    // when creating the RegisterToolWindowTask as it has a lot of parameters :)

    val toolWindowManager = ToolWindowManager.getInstance(project)
    // Make sure it is not already registered
    return toolWindowManager.getToolWindow(FrcRioLogToolWindowExecutor.FRC_RIO_LOG_TOOL_WINDOW_ID) ?: toolWindowManager.registerToolWindow(
        RegisterToolWindowTask(
            FrcRioLogToolWindowExecutor.FRC_RIO_LOG_TOOL_WINDOW_ID,
            ToolWindowAnchor.BOTTOM,
            sideTool = true, // sideTool = true means put it in the secondary group
            icon = FrcRioLogToolWindowExecutor.FRC_TOOL_WINDOW_ICON
                              )
                                                                                                                                          )
}