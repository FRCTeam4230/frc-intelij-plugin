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
import org.jetbrains.plugins.gradle.frameworkSupport.BuildScriptDataBuilder;
import com.intellij.framework.FrameworkTypeEx;
import com.intellij.framework.addSupport.FrameworkSupportInModuleProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.project.ProjectId;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ModifiableModelsProvider;
import com.intellij.openapi.roots.ModifiableRootModel;



public class FrcJavaFrameworkSupportProvider extends FrcFrameworkSupportProvider
{
    private static final Logger LOG = Logger.getInstance(FrcJavaFrameworkSupportProvider.class);
    
    public static final String ID = "java";
    
    @Override
    public void addSupport(@NotNull ProjectId projectId,
                           @NotNull Module module,
                           @NotNull ModifiableRootModel rootModel,
                           @NotNull ModifiableModelsProvider modifiableModelsProvider,
                           @NotNull BuildScriptDataBuilder buildScriptData)
    {
    
//        buildScriptData
//                .addPluginDefinitionInPluginsGroup("id 'java'")
//                .addPropertyDefinition("sourceCompatibility = 1.8")
//                .addRepositoriesDefinition("mavenCentral()")
//                .addDependencyNotation("testCompile group: 'junit', name: 'junit', version: '4.12'");
    }
    
    
    @NotNull
    @Override
    public FrameworkTypeEx getFrameworkType()
    {
        return new FrameworkTypeEx(ID)
        {
            @NotNull
            @Override
            public FrameworkSupportInModuleProvider createProvider()
            {
                return FrcJavaFrameworkSupportProvider.this;
            }
        
        
            @NotNull
            @Override
            public String getPresentableName()
            {
                return "Java";
            }
        
        
            @NotNull
            @Override
            public Icon getIcon()
            {
                return AllIcons.Nodes.Module;
            }
        };
    }
}
