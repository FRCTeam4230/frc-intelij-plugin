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

package net.javaru.iip.frc.ui.internal;

import javax.swing.*;

import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;



public class PlaceholderTextFieldPaddingDemoFormDialogWrapper extends DialogWrapper
{
    
    
    private final PlaceholderTextFieldPaddingDemoForm form;
    
    
    public PlaceholderTextFieldPaddingDemoFormDialogWrapper(@Nullable Project project)
    {
        super(project, true);
        form = new PlaceholderTextFieldPaddingDemoForm(project);
        setTitle("Placeholder Padding Demo");
        init();
    }
    
    
    @Override
    protected @Nullable JComponent createCenterPanel()
    {
        return form.getRootPanel();
    }
}
