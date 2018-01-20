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

import javax.annotation.Nonnull;
import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.diagnostic.Logger;



public class FrcModuleWizardStep extends ModuleWizardStep
{
    private static final Logger LOG = Logger.getInstance(FrcModuleWizardStep.class);

    @NotNull
    private final FrcModuleBuilder frcModuleBuilder;

    @NotNull
    private final WizardContext wizardContext;
    
    @NotNull
    private final FrcModuleWizardPanel wizardPanel = new FrcModuleWizardPanel();

    public FrcModuleWizardStep(@Nonnull FrcModuleBuilder frcModuleBuilder, @NotNull WizardContext wizardContext) 
    {
        this.frcModuleBuilder = frcModuleBuilder;
        this.wizardContext = wizardContext;
    }


    @Override
    public JComponent getComponent()
    {
       return wizardPanel;
    }


    @Override
    public void updateDataModel()
    {
        final RobotType robotType = wizardPanel.getSelectedRobotType();
        LOG.debug("[FRC] FrcModuleWizardPanel.getSelectedRobotType() returned '" + robotType + "'. Updating data model (i.e. frcModuleBuilder)");
        frcModuleBuilder.setRobotType(robotType);
    }
}
