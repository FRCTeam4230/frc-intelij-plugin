/*
 * Copyright 2015-2017 Mark Vedder
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

package net.javaru.iip.frc.riolog.ui;

import java.awt.*;
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
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPanel;
import com.intellij.util.ui.UIUtil;

import net.javaru.iip.frc.actions.MockAction;
import net.javaru.iip.frc.i18n.FrcMessageBundle;
import net.javaru.iip.frc.toolWindow.FrcToolWindowFactory;



// Must be registered as a <projectService> in plugins.xml
public class RioLogRootPanel extends SimpleToolWindowPanel implements Disposable
{
    /* We implement this as a SimpleToolWindowPanel in the event we want to break the RioLog out
       as a separate tool window in the event we add additional features to the FRC tool window. */
    
    private static final Logger LOG = Logger.getInstance(RioLogRootPanel.class);
    private static final long serialVersionUID = 3732957886793376891L;

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
        group.add(MockAction.createMockAddAction());
        group.add(MockAction.createMockRemoveAction());
        group.add(MockAction.createMockCancelAction());
        group.add(MockAction.createMockRefreshAction());

        return ActionManager.getInstance().createActionToolbar(FrcToolWindowFactory.FRC_TOOL_WINDOW_ID, group, false);
    }

    
    private JPanel createNoneActivePanel()
    {
        JBPanel panel = new JBPanel();
        JBLabel label = new JBLabel(FrcMessageBundle.message("frc.riolog.ui.inactive.panel.text"));
        label.setIcon(UIUtil.getInformationIcon());
        panel.add(label, BorderLayout.CENTER);
        
        
        panel.revalidate();
        panel.repaint();
        return panel;
    }

    @Override
    public void dispose()
    {
        //TODO: Write this 'dispose' implemented method in the 'RioLogRootPanel' class
        LOG.warn("TODO: RioLogRootPanel.dispose() needs to be implemented");
    }
}
