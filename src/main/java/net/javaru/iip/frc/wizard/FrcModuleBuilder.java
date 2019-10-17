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

package net.javaru.iip.frc.wizard;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.model.project.ProjectId;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import com.intellij.openapi.util.Disposer;

import net.javaru.iip.frc.FrcIcons.FRC;



public class FrcModuleBuilder extends JavaModuleBuilder implements ModuleBuilderListener
{
    private static final Logger LOG = Logger.getInstance(FrcModuleBuilder.class);
    
    private WizardContext myWizardContext;
    @Nullable
    private ProjectData myParentProject;
    private boolean myInheritGroupId;
    private boolean myInheritVersion;
    private ProjectId myProjectId;
    private String rootProjectPath;
    private boolean myUseKotlinDSL;
    private final FrcModuleConfig config = new FrcModuleConfig();
    
    @Override
    public String getParentGroup()
    {
        return JavaModuleType.BUILD_TOOLS_GROUP;
    }
    
    
    @Override
    public int getWeight()
    {
        return JavaModuleBuilder.BUILD_SYSTEM_WEIGHT;
    }
    
    
    @Override
    public ModuleType<?> getModuleType() { return FrcModuleType.getInstance(); /*return StdModuleTypes.JAVA;*/ }
    
    
    
    @Override
    public void moduleCreated(@NotNull Module module)
    {
        // This method is from the ModuleBuilderListener
        LOG.debug("[FRC] FrcModuleBuilder.moduleCreated() called with module: " + module.getName() + " at " + module.getModuleFilePath());
        // Module Configuration work could be done here
    }
    
    
    @Override
    public String getPresentableName()
    {
        // The default in super is: return getModuleTypeName();
        // This is the name that appears (on the left) in the initial new project dialog where all the possible project types/options are shown  
        return "FRC Robot Project";
    }
    
    
    @Override
    // The icon used (on the left) in the initial new project dialog where all the possible project types/options are shown
    public Icon getNodeIcon() { return FRC.FIRST_ICON_MEDIUM_16; }
    
    
    
    @Override
    public String getGroupName()
    {
        return FrcModuleConstantsKt.MODULE_BUILDER_GROUP_NAME;
    }
    
    @Override
    public void setupRootModel(@NotNull ModifiableRootModel modifiableRootModel) throws ConfigurationException
    {
        LOG.debug("[FRC] FrcModuleBuilder.setupRootModel() called");
        super.setupRootModel(modifiableRootModel);
    }
    
    
    @Override
    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext, @NotNull ModulesProvider modulesProvider)
    {
        this.myWizardContext = wizardContext;
        return new ModuleWizardStep[] {new FrcModuleWizardStep(this, wizardContext)};
    }
    
    
    public void setParentProject(@Nullable ProjectData parentProject)
    {
        myParentProject = parentProject;
    }
    
    
    public ProjectId getProjectId()
    {
        return myProjectId;
    }
    
    
    public void setProjectId(@NotNull ProjectId projectId)
    {
        myProjectId = projectId;
    }
    
    
    public FrcModuleConfig getConfig() { return config; }
    
    
    /**
     * Custom UI to be shown on the first wizard page
     *
     * @param context
     * @param parentDisposable
     */
    @Nullable
    @Override
    public ModuleWizardStep getCustomOptionsStep(WizardContext context, Disposable parentDisposable)
    {
        //TODO: Write this 'getCustomOptionsStep' overridden method
        //     This determines the potential frameworks  that can be selected (like kotlin, groovy, Thymeleaf, Ruby, yada yada yada
        //     Notice that when setProviders is called  "java" is set for the "preselected" parameter    In IDEA project: service/project/wizard/GradleFrameworksWizardStep.java:99 as well as  service/project/wizard/GradleFrameworksWizardStep.java:91 for the Kotlin DSL
        //     Others are dynamically loaded via extension point definitions as far as I can tell.
        //     So this is likely where we will want to put Kotlin 
        //     It looks like these ultimately get defined/configured via an extension is the plugin.xml
        //     For example with Gradle, there is:
        //           <frameworkSupport implementation="org.jetbrains.plugins.gradle.frameworkSupport.GradleGroovyFrameworkSupportProvider"/>
        //     in the gradle-groovy-integration.xml file.
        //     in turn that file is defined as an optional depends in the gradle-java-integration.xml file when defining "org.intellij.groovy" as an (optional) dependency
        //     For Java, I would want it to be a required provider rather than an optional that is preselected. Not sure if I need to "add" it behind the scenes or not.
        LOG.trace("[FRC] FrcModuleBuilder.getCustomOptionsStep() called");
        /*return super.getCustomOptionsStep(context, parentDisposable);*/
        
        
        //final FrcFrameworksWizardStep step = new FrcFrameworksWizardStep(context, this, config);
        final FrcFrameworksBlankWizardStep step = new FrcFrameworksBlankWizardStep();
        Disposer.register(parentDisposable, step);
        return step;
    }
}
