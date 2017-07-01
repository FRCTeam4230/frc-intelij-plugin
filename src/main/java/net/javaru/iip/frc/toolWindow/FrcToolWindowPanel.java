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

package net.javaru.iip.frc.toolWindow;

import org.jetbrains.annotations.NotNull;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.ui.UISettingsListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindowManager;

import net.javaru.iip.frc.riolog.ui.RioLogRootPanel;



// Must be registered as a <projectService> in plugins.xml
@SuppressWarnings("NonSerializableFieldInSerializableClass")
public class FrcToolWindowPanel extends SimpleToolWindowPanel implements Disposable,
                                                                         UISettingsListener
{
    private static final Logger LOG = Logger.getInstance(FrcToolWindowPanel.class);
    private static final long serialVersionUID = -9184866872227986834L;

    @NotNull
    private final Project myProject;
    @NotNull
    private final ToolWindowManager myToolWindowManager;


    private FrcToolWindowPanel(@NotNull final Project project, @NotNull final ToolWindowManager toolWindowManager)
    {
        super(false, true);
        this.myProject = project;
        this.myToolWindowManager = toolWindowManager;
        
        ActionToolbar toolbar = createToolbar();
        toolbar.setTargetComponent(this);
        setToolbar(toolbar.getComponent());
        
        /* We've implemented the RioLogRootPanel as a SimpleToolWindowPanel in the event we want to break the RioLog out
           as a separate tool window in the event we add additional features to the FRC tool window. */
        final RioLogRootPanel rioLogRootPanel = ServiceManager.getService(project, RioLogRootPanel.class);
        setContent(rioLogRootPanel);
    }


    public static FrcToolWindowPanel getInstance(@NotNull final Project project)
    {
        return ServiceManager.getService(project, FrcToolWindowPanel.class);
    }


    private static ActionToolbar createToolbar()
    {
        DefaultActionGroup group = new DefaultActionGroup();
        // Right now we do not have any actions for the "outside" FrcToolWindowPanel since all we have is the RioLog Panel/Functionality
        // If/when we add additional things, we may need tool buttons.
        //  group.add(MockAction.createMockAddAction());
        //  group.add(MockAction.createMockRemoveAction());
        return ActionManager.getInstance().createActionToolbar(FrcToolWindowFactory.FRC_TOOL_WINDOW_ID, group, false);
    }


    @Override
    public void uiSettingsChanged(UISettings uiSettings)
    {
        LOG.debug("[FRC] FrcToolWindowPanel.uiSettingsChanged() called for project " + myProject);
    }


    @Override
    public void dispose()
    {
        //TODO: Write this 'dispose' implemented method in the 'FrcToolWindowPanel' class
        LOG.warn("TODO: FrcToolWindowPanel.dispose() needs to be implemented");
    }
}
