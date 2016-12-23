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

package net.javaru.iip.frc.facet;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;

import net.javaru.iip.frc.FrcIcons;



public class FrcFacetType extends FacetType<FrcFacet, FrcFacetConfiguration>
{
    public final static FacetTypeId<FrcFacet> FACET_TYPE_ID = new FacetTypeId<>(FrcFacet.FACET_ID);


    public FrcFacetType()
    {
        // id, stringId, presentableName
        super(FACET_TYPE_ID, FrcFacet.FACET_ID, FrcFacet.FACET_NAME);
    }


    
    public static FrcFacetType getInstance()
    {
        return findInstance(FrcFacetType.class);
    }

    @Override
    public FrcFacetConfiguration createDefaultConfiguration()
    {
        return new FrcFacetConfiguration();
    }


    @Override
    public FrcFacet createFacet(@NotNull Module module, String name, @NotNull FrcFacetConfiguration facetConfiguration, @Nullable Facet underlyingFacet)
    {
        return new FrcFacet(this, module, name, facetConfiguration, underlyingFacet);
    }


    @Override
    public boolean isSuitableModuleType(ModuleType moduleType)
    {
        return moduleType instanceof JavaModuleType;
    }


    @Nullable
    @Override
    public Icon getIcon()
    {
        return FrcIcons.FIRST_ICON_MEDIUM_16;
    }
}
