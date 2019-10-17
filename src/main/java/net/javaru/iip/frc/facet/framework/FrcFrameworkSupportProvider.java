/*
 * Copyright 2015-2019 the original author or authors
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

package net.javaru.iip.frc.facet.framework;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.frameworkSupport.BuildScriptDataBuilder;
import com.intellij.framework.addSupport.FrameworkSupportInModuleConfigurable;
import com.intellij.framework.addSupport.FrameworkSupportInModuleProvider;
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModel;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.externalSystem.model.project.ProjectId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;

import net.javaru.iip.frc.wizard.FrcModuleBuilder;

import static org.jetbrains.plugins.gradle.service.project.wizard.GradleModuleBuilder.getBuildScriptData;



//NOTE: Configured in plugin.xml 
public abstract class FrcFrameworkSupportProvider  extends FrameworkSupportInModuleProvider
{
    
    //  Example: see org.jetbrains.plugins.gradle.frameworkSupport.GradleFrameworkSupportProvider
    //  There is also a FacetBasedFrameworkSupportProvider<FrcFacet> an example of which is StrutsFrameworkSupportProvider fot Structs2 plugin in jetbrains open source plugins
    
    
    private static final Logger LOG = Logger.getInstance(FrcFrameworkSupportProvider.class);
    
    public static final ExtensionPointName<FrcFrameworkSupportProvider> EP_NAME = ExtensionPointName.create("net.javaru.iip.frc.frameworkSupport");
    
    
    public abstract void addSupport(@NotNull ProjectId projectId,
                                    @NotNull Module module,
                                    @NotNull ModifiableRootModel rootModel,
                                    @NotNull ModifiableModelsProvider modifiableModelsProvider,
                                    @NotNull BuildScriptDataBuilder buildScriptData);
    
    
    public JComponent createComponent()
    {
        return null;
    }
    
    
    @NotNull
    @Override
    public FrameworkSupportInModuleConfigurable createConfigurable(@NotNull FrameworkSupportModel model)
    {
        return new FrameworkSupportInModuleConfigurable()
        {
            @Nullable
            @Override
            public JComponent createComponent()
            {
                return FrcFrameworkSupportProvider.this.createComponent();
            }
    
    
            @Override
            public void addSupport(@NotNull Module module,
                                   @NotNull ModifiableRootModel rootModel,
                                   @NotNull ModifiableModelsProvider modifiableModelsProvider)
            {
                final BuildScriptDataBuilder buildScriptData = getBuildScriptData(module);
                if (buildScriptData != null)
                {
                    ModuleBuilder builder = model.getModuleBuilder();
                    @SuppressWarnings("CastToConcreteClass")
                    ProjectId projectId = builder instanceof FrcModuleBuilder ? ((FrcModuleBuilder) builder).getProjectId()
                                                                              : new ProjectId(null, module.getName(), null);
                    FrcFrameworkSupportProvider.this.addSupport(projectId, module, rootModel, modifiableModelsProvider, buildScriptData);
                }
            }
        };
    }
    
    
    @Override
    public boolean isEnabledForModuleType(@NotNull ModuleType moduleType)
    {
        return false;
    }
}
