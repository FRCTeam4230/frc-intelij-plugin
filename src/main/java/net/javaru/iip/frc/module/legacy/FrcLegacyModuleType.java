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

package net.javaru.iip.frc.module.legacy;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.JavaPsiFacade;

import net.javaru.iip.frc.FrcIcons;



public class FrcLegacyModuleType extends ModuleType<FrcLegacyModuleBuilder>
{
    private static final Logger LOG = Logger.getInstance(FrcLegacyModuleType.class);

    public static final String FRC_MODULE_TYPE_ID = "FRC_Legacy_Module";

    public FrcLegacyModuleType()
    {
        super(FRC_MODULE_TYPE_ID);
    }


    @NotNull
    @Override
    public FrcLegacyModuleBuilder createModuleBuilder()
    {
        return new FrcLegacyModuleBuilder();
    }


//    @NotNull
//    @Override
//    public ModuleWizardStep[] createWizardSteps(@NotNull WizardContext wizardContext,
//                                                @NotNull FrcLegacyModuleBuilder moduleBuilder,
//                                                @NotNull ModulesProvider modulesProvider)
//    {
//        return new ModuleWizardStep[]{new FrcLegacyModuleWizardStep(moduleBuilder, wizardContext)};
//    }


    @Nullable
    @Override
    public ModuleWizardStep modifyProjectTypeStep(@NotNull SettingsStep settingsStep, @NotNull final ModuleBuilder moduleBuilder)
    {
        return ProjectWizardStepFactory.getInstance().createJavaSettingsStep(settingsStep, moduleBuilder, moduleBuilder::isSuitableSdkType);
    }
    
    
    @Override
    public boolean isValidSdk(@NotNull final Module module, final Sdk projectSdk)
    {
        return isValidJavaSdk(module);
    }


    public static boolean isValidJavaSdk(@NotNull Module module)
    {
        if (ModuleRootManager.getInstance(module).getSourceRoots(JavaModuleSourceRootTypes.SOURCES).isEmpty()) return true;
        return JavaPsiFacade.getInstance(module.getProject()).findClass(CommonClassNames.JAVA_LANG_OBJECT,
                                                                        module.getModuleWithLibrariesScope()) != null;
    }
    

    @NotNull
    @Override
    public String getName()
    {
        return "FRC ModuleType Name";
    }


    @NotNull
    @Override
    public String getDescription()
    {
        return "FRC Module Type Description";
    }


    
    public Icon getBigIcon()
    {
        return FrcIcons.FRC.FIRST_ICON_MEDIUM_LARGE_24;
    }


    @Override
    public Icon getNodeIcon(boolean isOpened)
    {
        return FrcIcons.FRC.FIRST_ICON_MEDIUM_16;
    }
}
