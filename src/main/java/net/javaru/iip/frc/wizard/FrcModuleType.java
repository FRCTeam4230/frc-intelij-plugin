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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.module.JavaModuleType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;

import net.javaru.iip.frc.FrcIcons.FRC;



public class FrcModuleType extends ModuleType<FrcModuleBuilder>
{
    // The Module type ID must match that defined in the id attribute of the moduleType extension definition in the plugin.xml file
    public static final String FRC_MODULE_TYPE_ID = "FRC_Module";
    
    public static FrcModuleType INSTANCE = new FrcModuleType();
    
    private FrcModuleType()
    {
        this(FRC_MODULE_TYPE_ID);
    }
    
    
    private FrcModuleType(@NonNls String id)
    {
        super(id);
    }
    
    
    public static FrcModuleType getInstance() { return INSTANCE; }
    
    
    @NotNull
    @Override
    public FrcModuleBuilder createModuleBuilder() { return new FrcModuleBuilder(); }
    
    
//    @NotNull
//    @Override
//    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext,
//                                                @NotNull FrcModuleBuilder moduleBuilder,
//                                                @NotNull ModulesProvider modulesProvider)
//    {
//        return new ModuleWizardStep[]{new FrcModuleWizardStep(moduleBuilder, wizardContext)};
//    }
    
    
    
    
    @Nullable
    @Override
    public ModuleWizardStep modifyProjectTypeStep(@NotNull SettingsStep settingsStep, @NotNull final ModuleBuilder moduleBuilder)
    {
        // This is called when the Module Type is selected (on the left side) in the initial new project dialog (including when it first opens and is selected due ot being selected the last time the dialog was used) 
        return ProjectWizardStepFactory.getInstance().createJavaSettingsStep(settingsStep, moduleBuilder, moduleBuilder::isSuitableSdkType);
    }
    
    
    @Override
    public boolean isValidSdk(@NotNull final Module module, final Sdk projectSdk)
    {
        return JavaModuleType.isValidJavaSdk(module);
    }
    
    
    
    //@Nls(capitalization = Nls.Capitalization.Title)
    @NotNull
    @Override
    public String getName()
    {
        // Not 100% sure where this gets used.
        return "FRC Module";
    }
    
    
    //@Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getDescription()
    {
        // This shows in the tool tip when you hover over the name in the new project dialog
        // embedded HTML tags are ok. For example:  "Create an <em>FRC<em> robot project."
        return "FRC modules are used for developing <b><em>FIRST</em> Robotics Competition</b> WPI Lib based robot projects using GradleRIO.";
    }
    
    
    @Override
    public Icon getNodeIcon(boolean isOpened) { return getIcon(); }
    
    
    /**
     * Gets used in the project tree (for the root module node) and in the Project Structure dialog listing of modules.
     * @return the module icon
     */
    @Override
    public Icon getIcon() { return FRC.FIRST_ICON_MEDIUM_16; }
    
}
