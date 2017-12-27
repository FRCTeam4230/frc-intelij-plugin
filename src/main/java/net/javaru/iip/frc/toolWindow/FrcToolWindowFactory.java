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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;

import net.javaru.iip.frc.components.FrcProjectComponentImpl;



// **NOT IN USE AT THIS TIME** this was created as part of an attempt to place the RioLog into another tool window. However, it looks
//                             like Executors are (or have to be) a tool window in of themselves. Need to do some more research to see
//                             if we can either add the executor to a SimpleToolWindowPanel (the preferred option) or add other UI 
//                             components to the executor for when we want to add other things. If not, we'll need to have a "RioLog" 
//                             and "FRC" tool window 
//
//        To use, add the following to the <extensions> element in plugin.xml
//        Just after <facetType id="FRC_FACET"... /> is a good spot
//        
//        <facet.toolWindow id="FRC" 
//                          anchor="bottom"
//                          icon="/icons/first/FIRST_icon_13x13_elevated.png"
//                          facetIdList="FRC_FACET"
//                          factoryClass="net.javaru.iip.frc.toolWindow.FrcToolWindowFactory"
//                          canCloseContents="false"
//        />
//        <projectService serviceInterface="net.javaru.iip.frc.toolWindow.FrcToolWindowPanel" serviceImplementation="net.javaru.iip.frc.toolWindow.FrcToolWindowPanel" />
//       
//       and if needed
//       <projectService serviceInterface="net.javaru.iip.frc.riolog.ui.RioLogRootPanel" serviceImplementation="net.javaru.iip.frc.riolog.ui.RioLogRootPanel" />
// 
// We implement DumbAware as we want the FRC Tool Window to be available when IDEA is indexing, and none of its content is dependent on indexes
public class FrcToolWindowFactory implements ToolWindowFactory,
                                             DumbAware
{
    public static final String FRC_TOOL_WINDOW_ID = "FRC Tools";
    /*
        LIFECYCLE
            - Project is opened (or a Facet is added)
            - shouldBeAvailable(Project) is called
            - init(ToolWindow) is called (assuming above returned true)
            - User opens the tool window for the First time (or the project loads and the tool window was previously open when the project was last closed)
            - createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) is called
     */
    
    
    /*
     */
    
    
    private static final Logger LOG = Logger.getInstance(FrcToolWindowFactory.class);
    
    
    // TODO: Let's use EventLogToolWindowFactory as a starting example

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow)
    {
        // Called the first time the tool window is opened (NOT when the tool window button is put on the tool bar)
        LOG.debug("[FRC] FrcToolWindowFactory.createToolWindowContent() called for Project '" + project.getName() + "'");

        // If a change requires that this create method only run when IDE is Smart, use DumService.runWhenSmart() 
        //      see example in TodoToolWindowFactory in the JetBrains Open Source Plugins project

        final FrcToolWindowPanel frcToolWindowPanel = FrcToolWindowPanel.getInstance(project);
        final ContentManager contentManager = toolWindow.getContentManager();
        // TODO: Need to either rename the displayName, or pass in null
        final Content content = contentManager.getFactory().createContent(frcToolWindowPanel, "Options Panel", false);
        contentManager.addContent(content);
        Disposer.register(project, frcToolWindowPanel);
    }


    @Override
    public boolean shouldBeAvailable(@NotNull Project project)
    {
        // Because we've defined an <facet.toolWindow> with a list of valid facets, this should only be called for an FRC Faceted project,
        // but we do a sanity check anyways. In the future, we may have additional information to check.
        final boolean shouldBeAvailable = FrcProjectComponentImpl.isFrcFacetedProject(project);
        LOG.debug("[FRC] FrcToolWindowFactory.shouldBeAvailable() returning '" + shouldBeAvailable + "' for Project '" + project.getName() + "'");
        return shouldBeAvailable;
    }
    
    @Override
    public void init(ToolWindow toolWindow)
    {
        // Called when the tool window is created (i.e. when the tool window button is put on the tool window bar)
        // At this point we don't have access to the Project. So any project specific initialization should occur in createToolWindowContent()
        LOG.debug("[FRC] FrcToolWindowFactory.init() called");
    }

    @Override
    public boolean isDoNotActivateOnStart()
    {
        LOG.debug("[FRC] FrcToolWindowFactory.isDoNotActivateOnStart() called");
        return false;
    }
}
