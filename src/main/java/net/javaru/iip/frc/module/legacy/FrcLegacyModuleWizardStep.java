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
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.diagnostic.Logger;



public class FrcLegacyModuleWizardStep extends ModuleWizardStep
{
    private static final Logger LOG = Logger.getInstance(FrcLegacyModuleWizardStep.class);

    @NotNull
    private final FrcLegacyModuleBuilder frcLegacyModuleBuilder;

    @NotNull
    private final WizardContext wizardContext;
    
    @NotNull
    private final FrcLegacyModuleWizardPanel wizardPanel = new FrcLegacyModuleWizardPanel();

    public FrcLegacyModuleWizardStep(@NotNull FrcLegacyModuleBuilder frcLegacyModuleBuilder, @NotNull WizardContext wizardContext) 
    {
        this.frcLegacyModuleBuilder = frcLegacyModuleBuilder;
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
        final LegacyRobotType legacyRobotType = wizardPanel.getSelectedRobotType();
        LOG.debug("[FRC] FrcLegacyModuleWizardPanel.getSelectedRobotType() returned '" + legacyRobotType
                  + "'. Updating data model (i.e. frcLegacyModuleBuilder)");
        frcLegacyModuleBuilder.setLegacyRobotType(legacyRobotType);
    }
}
