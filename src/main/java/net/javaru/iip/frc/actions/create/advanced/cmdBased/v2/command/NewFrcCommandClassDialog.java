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

package net.javaru.iip.frc.actions.create.advanced.cmdBased.v2.command;

import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;

import kotlin.Unit;
import net.javaru.iip.frc.actions.create.advanced.ClassCreator;
import net.javaru.iip.frc.actions.create.advanced.NewFrcClassDialog;
import net.javaru.iip.frc.util.UiUtilsKt;




public class NewFrcCommandClassDialog extends NewFrcClassDialog
{
    private static final Logger LOG = Logger.getInstance(NewFrcCommandClassDialog.class);
    private JPanel myTopPanel;
    private JBLabel myCommandNameLabel;
    private JBTextField myCommandNameTextField;
    private JCheckBox myAutoAppendCommandCheckBox;
    private JPanel mySubsystemsPanel;
    
    
    public NewFrcCommandClassDialog(@NotNull Module module,
                                    @NotNull ClassCreator classCreator,
                                    @NotNull PsiDirectory directory)
    {
        super(module, classCreator, directory);
        initUiComponents();
        init(); //from DialogWrapper SHOULD BE LAST STATEMENT IN CONSTRUCTOR 
    }
    
    
    protected void initUiComponents()
    {
        myAutoAppendCommandCheckBox.setSelected(true);
    
        UiUtilsKt.addTextChangedListener(myCommandNameTextField, (documentEvent, text) -> {
            myAutoAppendCommandCheckBox.setEnabled(!myCommandNameTextField.getText().endsWith("Command"));
            return Unit.INSTANCE;
        });
    }
    
    
    @NotNull
    @Override
    protected String getClassTypeSimpleName()
    {
        return "Command";
    }
    
    
    @Override
    protected JTextComponent getNewClassNameField()
    {
        return myCommandNameTextField;
    }
    
    
    @Override
    protected String getNewClassName()
    {
        return myAutoAppendCommandCheckBox.isEnabled() ? myCommandNameTextField.getText().trim() + "Command" : myCommandNameTextField.getText().trim();
    }
    
    
    @Nullable
    @Override
    protected JComponent createCenterPanel()
    {
        return myTopPanel;
    }
}    

