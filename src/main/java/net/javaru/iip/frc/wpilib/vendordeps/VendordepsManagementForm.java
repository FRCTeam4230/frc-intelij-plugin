/*
 * Copyright 2015-2021 the original author or authors.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.javaru.iip.frc.wpilib.vendordeps;

import javax.swing.*;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;



public class VendordepsManagementForm
{
    private static final Logger LOG = Logger.getInstance(VendordepsManagementForm.class);
    
    
    private JPanel rootPanel;
    private JPanel mainPanel;
    private JBLabel textField;
    
    private Project project;
    
    
    public VendordepsManagementForm()
    {
    }
    
    
    public VendordepsManagementForm(Project project)
    {
        this.project = project;
    }
    
    
    public void setProject(Project project) {this.project = project;}
    
    
    public JPanel getRootPanel() {return rootPanel;}
}
