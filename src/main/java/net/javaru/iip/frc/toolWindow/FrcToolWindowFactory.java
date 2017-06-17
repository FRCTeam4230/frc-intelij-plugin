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
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;

import net.javaru.iip.frc.components.FrcProjectComponentImpl;



public class FrcToolWindowFactory implements ToolWindowFactory
{
    /*
        LIFECYCLE
            - Project is opened (or a Facet is added)
            - shouldBeAvailable(Project) is called
            - init(ToolWindow) is called (assuming above returned true)
            - User opens the tool window for the First time (or the project loads and the tool window was previously open when the project was last closed)
            - createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) is called
     */
    
    
    private static final Logger LOG = Logger.getInstance(FrcToolWindowFactory.class);
    

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow)
    {
        // If we can only run when IDE is Smart, see example in TodoToolWindowFactory in the JetBrains Open Source Plugins project
        //   DumbService.getInstance(project).runWhenSmart(() -> ServiceManager.getService(project, TodoView.class).initToolWindow(toolWindow));
        
        // Called the first time the tool window is opened (NOT when the tool window button is put on the tool bar)
        LOG.debug("[FRC] FrcToolWindowFactory.createToolWindowContent() called for Project '" + project.getName() + "'");

        // If we can only run when IDE is Smart, use DumService.runWhenSmart() see example in TodoToolWindowFactory in the JetBrains Open Source Plugins project
        //DumbService.getInstance(project).runWhenSmart(() -> ServiceManager.getService(project, FrcToolWindow.class).initToolWindow(toolWindow));
        ServiceManager.getService(project, FrcToolWindow.class).initToolWindow(toolWindow);
    }


    @Override
    public void init(ToolWindow toolWindow)
    {
        // Called when the tool window is created (i.e. when the tool window button is put on the tool window bar)
        // At this point we don't have access to the Project. So any project specific initialization should occur in createToolWindowContent()
        LOG.debug("[FRC] FrcToolWindowFactory.init() called");
    }


    @Override
    public boolean shouldBeAvailable(@NotNull Project project)
    {
        // Because we've defined an <facet.toolWindow> with a list of valid facets, this should only be called for an FRC Faceted project,
        // but we do a sanity check anyways. In the future, we may have additional information to check.
        LOG.debug("[FRC] FrcToolWindowFactory.shouldBeAvailable() called for Project '" + project.getName() + "'");
        return FrcProjectComponentImpl.isFrcFacetedProject(project);
    }


    @Override
    public boolean isDoNotActivateOnStart()
    {
        LOG.debug("[FRC] FrcToolWindowFactory.isDoNotActivateOnStart() called");
        return false;
    }
}
