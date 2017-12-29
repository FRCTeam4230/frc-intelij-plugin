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

package net.javaru.iip.frc.module;

import org.jetbrains.annotations.NotNull;
import com.intellij.ide.util.projectWizard.JavaModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleBuilderListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;



public class FrcModuleBuilder extends JavaModuleBuilder implements ModuleBuilderListener
{
    private static final Logger LOG = Logger.getInstance(FrcModuleBuilder.class);

    private static final FrcModuleType MODULE_TYPE = new FrcModuleType();
    
    private RobotType robotType = RobotType.Sample;

    @Override
    public void moduleCreated(@NotNull Module module)
    {
        LOG.debug("[FRC] FrcModuleBuilder.moduleCreated() called with module: " + module.getName() + " at " + module.getModuleFilePath());
    }


    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException
    {
        LOG.debug("[FRC] FrcModuleBuilder.setupRootModel() called");
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
        return "FRC Robot Project";
    }


    @Override
    public String getGroupName()
    {
        return "FRCGroupName";
    }


    // createWizardSteps in super calls moduleType.createWizardSteps
    
    


    void setRobotType(@NotNull RobotType robotType)
    {
        this.robotType = robotType;
    }
}
