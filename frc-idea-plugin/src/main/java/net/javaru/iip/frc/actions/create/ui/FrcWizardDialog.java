/*
 * Copyright 2015-2016 Mark Vedder
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

package net.javaru.iip.frc.actions.create.ui;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import net.javaru.iip.frc.actions.create.TaskValidator;
import net.javaru.iip.frc.actions.create.ui.forms.FrcWizardMasterPanel;



public class FrcWizardDialog extends DialogWrapper
{

    @Nullable
    private final Project project;
    private final JPanel myPanel;


    //TODO convert to Builder
    public FrcWizardDialog(@Nullable Project project,
                           @NotNull JPanel contentPanel,
                           @NotNull String taskTitle,
                           @Nullable String taskDescription, 
                           @Nullable TaskValidator taskValidator)
    {
        super(project);
        this.project = project;
        final FrcWizardMasterPanel masterPanel = new FrcWizardMasterPanel(contentPanel, taskTitle, taskDescription, taskValidator);
        this.myPanel = masterPanel.getRootPanel();
        init();
    }
    

    @Nullable
    @Override
    protected JComponent createCenterPanel()
    {
        return myPanel;
    }


    
}
