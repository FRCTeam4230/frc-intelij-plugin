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

package net.javaru.iip.frc.facet;

import java.util.Collection;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;



public class FrcFacet extends Facet<FrcFacetConfiguration>
{
    private static final Logger LOG = Logger.getInstance(FrcFacet.class);

    public static final String FACET_TYPE_ID_STRING = "FRC_FACET";
    public final static FacetTypeId<FrcFacet> FACET_TYPE_ID = new FacetTypeId<>(FrcFacet.FACET_TYPE_ID_STRING);
    public static final String FACET_NAME = "FRC";
    public static final String FULL_NAME = "FRC (FIRST Robotics Competition)";


    public FrcFacet(@NotNull final FacetType facetType,
                    @NotNull final Module module,
                    final String name,
                    @NotNull final FrcFacetConfiguration configuration,
                    Facet underlyingFacet)
    {
        super(facetType, module, name, configuration, underlyingFacet);
    }


    @Nullable
    public static FrcFacet getInstance(Module module)
    {
        return FacetManager.getInstance(module).getFacetByType(FACET_TYPE_ID);
    }


    @NotNull
    public static ImmutableList<FrcFacet> getAllFrcFacetsForAllOpenProjects()
    {
        final Project[] openProjects = ProjectManager.getInstance().getOpenProjects();

        final Builder<FrcFacet> listBuilder = ImmutableList.builder();

        for (Project openProject : openProjects)
        {
            listBuilder.addAll(getAllFrcFacetsForProject(openProject));
        }

        return listBuilder.build();
    }


    public static ImmutableList<FrcFacet> getAllFrcFacetsForProject(@NotNull Project project)
    {
        final Builder<FrcFacet> listBuilder = ImmutableList.builder();
        final Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules)
        {
            final Collection<FrcFacet> frcFacets = FacetManager.getInstance(module).getFacetsByType(FACET_TYPE_ID);
            listBuilder.addAll(frcFacets);
        }
        return listBuilder.build();
    }


    public static boolean isFrcFacet(@Nullable Facet facet)
    {
        return facet instanceof FrcFacet;
    }
    
    @Contract("null -> false")
    public static boolean isFrcFacetedModule(@Nullable Module module)
    {
        if (module != null)
        {
            final FrcFacet frcFacet = FacetManager.getInstance(module).getFacetByType(FrcFacet.FACET_TYPE_ID);
            if (frcFacet != null) { return true; }
        }
        return false;
    }


    @Contract("null -> false")
    public static boolean isFrcFacetedProject(@Nullable Project project)
    {
        if (project != null)
        {
            final Module[] modules = ModuleManager.getInstance(project).getModules();
            for (Module module : modules)
            {
                if (isFrcFacetedModule(module)) {return true;}
            }
        }
        return false;
    }
}
