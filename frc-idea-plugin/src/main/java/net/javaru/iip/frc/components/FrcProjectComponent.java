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

package net.javaru.iip.frc.components;


import org.jetbrains.annotations.NotNull;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetManagerAdapter;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.ui.RioLogConsoleProjectService;



public class FrcProjectComponent implements ProjectComponent
{
    private static final Logger LOG = Logger.getInstance(FrcProjectComponent.class);

    @NotNull
    private final Project myProject;


    public FrcProjectComponent(@NotNull Project project)
    {
        this.myProject = project;
    }


    @Override
    public void projectOpened()
    {
        registerMessageBusListeners();
        RioLogConsoleProjectService.update(myProject);
    }


    @Override
    public void projectClosed() { /* no op */ }


    @Override
    public void initComponent() { /* no op */ }


    @Override
    public void disposeComponent() { /* no op */ }


    @NotNull
    @Override
    public String getComponentName() { return getClass().getSimpleName(); }


    private static void registerMessageBusListeners()
    {
        try
        {
            ApplicationManager.getApplication().getMessageBus().connect().subscribe(FacetManager.FACETS_TOPIC, new FrcFacetManagerListener());
        }
        catch (IllegalStateException e)
        {
            LOG.info("[FRC] Already Subscribed for FACETS_TOPIC: " + e.toString(), e);
        }
    }


    public static class FrcFacetManagerListener extends FacetManagerAdapter
    {
        @Override
        public void facetAdded(@NotNull Facet facet)
        {
            updateForFrcFacet(facet);
        }


        @Override
        public void facetConfigurationChanged(@NotNull Facet facet)
        {
            updateForFrcFacet(facet);
        }


        @Override
        public void facetRemoved(@NotNull Facet facet)
        {
            updateForFrcFacet(facet);
        }


        private void updateForFrcFacet(@NotNull Facet facet)
        {
            if (facet instanceof FrcFacet)
            {
                RioLogConsoleProjectService.update(facet);
            }
        }
    }

}
