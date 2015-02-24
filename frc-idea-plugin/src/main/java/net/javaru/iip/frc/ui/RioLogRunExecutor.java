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

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;


// This class is registered in the plugin.xml file in the extensions element
public class RioLogRunExecutor extends Executor
{
    public static final Icon TOOL_WINDOW_ICON = IconLoader.getIcon("/net/javaru/iip/frc/ui/images/FIRST_icon_13x13.png"); // 13x13
    public static final String TOOL_WINDOW_ID = "RIO Log";
    // The executor ID must match the id attribute of the <extensions>/<executor> element in the plugin.xml file
    public static final String EXECUTOR_ID = "FrcRioLog";

    @Override
    public String getToolWindowId() { return TOOL_WINDOW_ID; }


    @Override
    public Icon getToolWindowIcon() { return TOOL_WINDOW_ICON; }


    @NotNull
    @Override
    public Icon getIcon() { return AllIcons.Actions.Execute; }


    @Override
    public Icon getDisabledIcon() { return AllIcons.Process.DisabledRun; }


    @Override
    public String getDescription() { return "FRC RIOLog"; }


    @NotNull
    @Override
    public String getActionName() { return "Rio Log"; }


    @NotNull
    @Override
    public String getId() { return EXECUTOR_ID; }


    @NotNull
    @Override
    public String getStartActionText() { return "Tail RIOLog"; }


    @Override
    public String getContextActionId() { return "FrcRioLogViewer"; }


    @Override
    public String getHelpId()
    {
        return null;
    }


    public static Executor getRunExecutorInstance()
    {
        return ExecutorRegistry.getInstance().getExecutorById(EXECUTOR_ID);
    }
}
