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

import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.text.JTextComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import kotlin.Unit;
import net.javaru.iip.frc.actions.create.advanced.ClassCreator;
import net.javaru.iip.frc.actions.create.advanced.NewFrcClassDialog;
import net.javaru.iip.frc.util.FindClassUtils;
import net.javaru.iip.frc.util.UiUtilsKt;

import static com.intellij.uiDesigner.core.GridConstraints.*;



public class NewFrcCommandClassDialog extends NewFrcClassDialog
{
    private static final Logger LOG = Logger.getInstance(NewFrcCommandClassDialog.class);
    private JPanel myTopPanel;
    private JBLabel myCommandNameLabel;
    private JBTextField myCommandNameTextField;
    private JCheckBox myAutoAppendCommandCheckBox;
    private JPanel mySubsystemsPanel;
    
    private Map<String, PsiClass> mySubsystemsActionCommandsMap;
    private BiMap<PsiClass, JBCheckBox>  mySubsystemsClassesBiMap;
    
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
            myAutoAppendCommandCheckBox.setEnabled(!myCommandNameTextField.getText().endsWith("Command") && 
                                                   !myCommandNameTextField.getText().endsWith("Cmd"));
            return Unit.INSTANCE;
        });
    
        initSubsystemSelectionPanel();
    }
    
    protected void initSubsystemSelectionPanel()
    {
        final List<PsiClass> subsystems = FindClassUtils.findImplementationsInModule(getSubsystemBaseClassFQN(),
                                                                                     myModule,
                                                                                     false,
                                                                                     false,
                                                                                     null);
        LOG.debug("[FRC] Found the following Subsystem subclasses: " + subsystems);
        
        // TODO: add option to filter out abstract 
        // boolean isAbstract1 = psiClass.hasModifier(JvmModifier.ABSTRACT); // As of 2019-12-24 this is marked as experimental
        // boolean isAbstract2 = psiClass.hasModifierProperty(PsiModifier.ABSTRACT);
    
        final ImmutableMap.Builder<String, PsiClass> actionCommandsMapBuilder = ImmutableMap.builder();
        final ImmutableBiMap.Builder<PsiClass, JBCheckBox> subSystemsMapBuilder = ImmutableBiMap.builder();
    
      ;
        if (!subsystems.isEmpty())
        {
            final GridLayoutManager manager = new GridLayoutManager(subsystems.size() + 1, 1);
            mySubsystemsPanel.setLayout(manager);
            
            mySubsystemsPanel.setBorder(new EtchedBorder());
            
            final GridConstraints gc = new GridConstraints(0, 0, 1, 1,
                                                     ANCHOR_WEST,
                                                     FILL_NONE,
                                                     (SIZEPOLICY_CAN_GROW | SIZEPOLICY_CAN_SHRINK),
                                                     SIZEPOLICY_FIXED,
                                                     new Dimension(-1, -1),
                                                     new Dimension(-1, -1),
                                                     new Dimension(-1, -1),
                                                     1);
            
            JBLabel label = new JBLabel("Select required subsystem(s):");
            mySubsystemsPanel.add(label, gc);
    
            subsystems.sort(Comparator.comparing(NavigationItem::getName));
            gc.setIndent(3);
            int row = 0;
            for (PsiClass subsystem : subsystems)
            {
                gc.setRow(++row);
                JBCheckBox checkBox = new JBCheckBox(subsystem.getName());
                checkBox.setActionCommand(subsystem.getQualifiedName());
                mySubsystemsPanel.add(checkBox, gc);
            }
        }
        this.mySubsystemsActionCommandsMap = actionCommandsMapBuilder.build();
        this.mySubsystemsClassesBiMap = subSystemsMapBuilder.build();
    }
    
    
    
    protected String getSubsystemBaseClassFQN()
    {
        return "edu.wpi.first.wpilibj2.command.Subsystem";
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
        return myAutoAppendCommandCheckBox.isEnabled() && myAutoAppendCommandCheckBox.isSelected() 
               ? myCommandNameTextField.getText().trim() + "Command" 
               : myCommandNameTextField.getText().trim();
    }
    
    
    @Nullable
    @Override
    protected JComponent createCenterPanel()
    {
        return myTopPanel;
    }
}    

