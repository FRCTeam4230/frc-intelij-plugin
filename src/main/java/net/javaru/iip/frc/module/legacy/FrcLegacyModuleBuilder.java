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

import org.jetbrains.annotations.NotNull;
import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;



public class FrcLegacyModuleBuilder extends JavaModuleBuilder implements ModuleBuilderListener
{
    private static final Logger LOG = Logger.getInstance(FrcLegacyModuleBuilder.class);

    private static final FrcLegacyModuleType MODULE_TYPE = new FrcLegacyModuleType();
    
    private LegacyRobotType legacyRobotType = LegacyRobotType.Sample;

    @Override
    public void moduleCreated(@NotNull Module module)
    {
        LOG.debug("[FRC] FrcLegacyModuleBuilder.moduleCreated() called with module: " + module.getName() + " at " + module.getModuleFilePath());
    }


    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException
    {
        LOG.debug("[FRC] FrcLegacyModuleBuilder.setupRootModel() called");
        super.setupRootModel(modifiableRootModel);
    }


    @Override
    public ModuleType getModuleType()
    {
        // I'm pretty sure we're ok with a singleton here as the ModuleType is mostly constants
       return MODULE_TYPE;
    }

    @Override
    public String getPresentableName()
    {
        // The default in super is: return getModuleTypeName();
        // This is the name tha appears on the left in the initial new project dialog
        return "FRC LEGACY (pre 2019) Robot Project";
    }


    @Override
    public String getGroupName()
    {
        return "FRCGroupName";
    }


    // createWizardSteps in super calls moduleType.createWizardSteps
    
    


    void setLegacyRobotType(@NotNull LegacyRobotType legacyRobotType)
    {
        this.legacyRobotType = legacyRobotType;
    }
}
