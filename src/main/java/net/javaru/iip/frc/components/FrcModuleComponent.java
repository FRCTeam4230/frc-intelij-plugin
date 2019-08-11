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

package net.javaru.iip.frc.components;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;

import net.javaru.iip.frc.FrcPluginGlobals;
import net.javaru.iip.frc.facet.FrcFacetKt;



public interface FrcModuleComponent extends ModuleComponent 
{
    boolean isThisModuleFrcFaceted();
    
    
    @NotNull
    @Override
    default String getComponentName()
    {
        // Each component has a unique name which is used for its externalization and other internal needs.
        // Per http://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_components.html
        //  "It is recommended to name components in the form <plugin_name>.<component_name>"
        Logger.getInstance(FrcModuleComponent.class).debug("[FRC] " + getClass().getSimpleName() + ".getComponentName() called");
        return FrcPluginGlobals.FRC_PLUGIN_NAME + "." + FrcModuleComponent.class.getSimpleName();
    }

    @Nullable
    static FrcApplicationComponent getInstance(@NotNull Module module)
    {
        return module.getComponent(FrcApplicationComponent.class);
    }


    @Contract("null -> false")
    static boolean isFrcFacetedModule(@Nullable Module module)
    {   
        return FrcFacetKt.isFrcFacetedModule(module);
    }
    
}
