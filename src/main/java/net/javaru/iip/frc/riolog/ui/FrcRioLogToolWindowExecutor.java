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

package net.javaru.iip.frc.riolog.ui;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.FrcIcons;



// This class is registered in the plugin.xml file in the extensions element
// Access is obtained via:  ExecutionManager.getInstance(myProject);
// For example, as used in AbstractRioLogContentExecutor:  
//           ExecutionManager.getInstance(myProject).getContentManager().showRunContent(myExecutor, myRunContentDescriptor);
public class FrcRioLogToolWindowExecutor extends Executor
{
    private static final Logger LOG = Logger.getInstance(FrcRioLogToolWindowExecutor.class);

    public static final String FRC_RIO_LOG_TOOL_WINDOW_ID = "FRC"; // We may want to change this to RioLog if/when we create an FRC tool window for other features
    // The executor ID must match the id attribute of the <extensions>/<executor> element in the plugin.xml file
    public static final String EXECUTOR_ID = FrcRioLogToolWindowExecutor.class.getSimpleName();


    @Override
    public String getToolWindowId() { return FRC_RIO_LOG_TOOL_WINDOW_ID; }


    @Override
    public Icon getToolWindowIcon() { return FrcIcons.FRC.FIRST_ICON_SMALL_13_ELEVATED; }


    @NotNull
    @Override
    public Icon getIcon() { return AllIcons.Actions.Execute; }


    @Override
    public Icon getDisabledIcon() { return AllIcons.Process.DisabledRun; }


    @Override
    public String getDescription() { return "FRC RioLog Tool Window"; }


    @NotNull
    @Override
    public String getActionName() { return "FRC RioLog Tool Window"; }


    @NotNull
    @Override
    public String getId() { return EXECUTOR_ID; }


    @NotNull
    @Override
    public String getStartActionText() { return "FRC RioLog Tool Window"; }


    @Override
    public String getContextActionId() { return "FrcRioLogToolWindowContextActionId"; }


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
