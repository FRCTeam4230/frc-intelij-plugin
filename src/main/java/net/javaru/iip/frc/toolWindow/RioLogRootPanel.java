/*
 * Copyright 2015-2020 the original author or authors
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

package net.javaru.iip.frc.toolWindow;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindowManager;

import icons.FrcIcons.RioLog;
import net.javaru.iip.frc.actions.MockAction;


// **NOT IN USE AT THIS TIME** this was created as part of an attempt to place the RioLog into another tool window. However, it looks
//                             like Executors are (or have to be) a tool window in of themselves. Need to do some more research to see
//                             if we can either add the executor to a SimpleToolWindowPanel (the preferred option) or add other UI 
//                             components to the executor for when we want to add other things. If not, we'll need to have a "RioLog" 
//                             and "FRC" tool window 
// 
// Must be registered as a <projectService> in plugins.xml 
public class RioLogRootPanel extends SimpleToolWindowPanel implements Disposable
{
    /* We implement this as a SimpleToolWindowPanel in the event we want to break the RioLog out
       as a separate tool window in the event we add additional features to the FRC tool window. */
    
    private static final Logger LOG = Logger.getInstance(RioLogRootPanel.class);
    private static final long serialVersionUID = 3732957886793376891L;
    public static final String RIOLOG_TOOL_WINDOW_ID = ""; // TODO Set to Executor Tool Window ID

    @NotNull
    private final Project myProject;
    @NotNull
    private final ToolWindowManager myToolWindowManager;

    public RioLogRootPanel(@NotNull final Project project, @NotNull final ToolWindowManager toolWindowManager)
    {
        // TODO: it would be nice to find a way to set 'vertical' based on value from the FrcToolWindow if we are being created inside it
        super(false, true);
        this.myProject = project;
        this.myToolWindowManager = toolWindowManager;

        ActionToolbar toolbar = createToolbar();
        toolbar.setTargetComponent(this);
        setToolbar(toolbar.getComponent());
        
        setContent(createNoneActivePanel());
    }


    private static ActionToolbar createToolbar()
    {
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new MockAction("Net Console", RioLog.RIOLOG_UDP_CONSOLE));
        group.add(new MockAction("SSH Console", RioLog.RIOLOG_SSH_CONSOLE));
        return ActionManager.getInstance().createActionToolbar(RIOLOG_TOOL_WINDOW_ID, group, false);
    }
    
    private JPanel createNoneActivePanel()
    {
       return new EmptyRioLogPanel().getRootPanel();
    }

    @Override
    public void dispose()
    {
        //TODO: Write this 'dispose' implemented method in the 'RioLogRootPanel' class
        LOG.warn("TODO: RioLogRootPanel.dispose() needs to be implemented");
    }
}
