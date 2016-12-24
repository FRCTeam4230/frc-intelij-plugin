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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetType;
import com.intellij.facet.FacetTypeId;
import com.intellij.openapi.module.Module;



public class FrcFacet extends Facet<FrcFacetConfiguration>
{
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

}
