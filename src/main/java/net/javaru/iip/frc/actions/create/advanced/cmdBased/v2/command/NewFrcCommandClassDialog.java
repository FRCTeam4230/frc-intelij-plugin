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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.StringUtils;
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
import net.javaru.iip.frc.util.FindClassUtilsKt;
import net.javaru.iip.frc.util.FrcCollectionExtsKt;
import net.javaru.iip.frc.util.UiUtilsKt;

import static com.intellij.uiDesigner.core.GridConstraints.*;



public class NewFrcCommandClassDialog extends NewFrcClassDialog
{
    public static final String SUBSYSTEMS_FQN_COMMA_DELIMITED_LIST = "requiredSubsystemsFqnCommaDelimitedString";
    public static final String SUBSYSTEMS_SIMPLE_NAME_COMMA_DELIMITED_LIST = "requiredSubsystemsNamesCommaDelimitedString";
    public static final String SUBSYSTEMS_VAR_NAME_COMMA_DELIMITED_LIST = "requiredSubsystemsVarsCommaDelimitedString";
    
    private static final Logger LOG = Logger.getInstance(NewFrcCommandClassDialog.class);
    private JPanel myTopPanel;
    private JBLabel myCommandNameLabel;
    private JBTextField myCommandNameTextField;
    private JCheckBox myAutoAppendCommandCheckBox;
    private JPanel mySubsystemsPanel;
    
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
        final List<PsiClass> subsystems = FindClassUtilsKt.findImplementationsInModule(getSubsystemBaseClassFQN(),
                                                                                       myModule,
                                                                                       false,
                                                                                       false,
                                                                                       null);
        LOG.debug("[FRC] Found the following Subsystem subclasses: " + subsystems);
        
        // TODO: add option to filter out abstract 
        // boolean isAbstract1 = psiClass.hasModifier(JvmModifier.ABSTRACT); // As of 2019-12-24 this is marked as experimental
        // boolean isAbstract2 = psiClass.hasModifierProperty(PsiModifier.ABSTRACT);
    
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
                subSystemsMapBuilder.put(subsystem, checkBox);
            }
        }
        this.mySubsystemsClassesBiMap = subSystemsMapBuilder.build();
    }
    
    
    @Override
    protected Map<String, String> getAdditionalProperties()
    {
        ImmutableMap.Builder<String, String> props = ImmutableMap.builder();
        addSubSystemProperties(props);
        return props.build();
    }
    
    protected void addSubSystemProperties(ImmutableMap.Builder<String, String> props)
    {
        final List<PsiClass> subsystems = getSelectedSubsystems();
        final String subSystemsFQN = FrcCollectionExtsKt.toCommaDelimitedString(subsystems, false, PsiClass::getQualifiedName);
        props.put(SUBSYSTEMS_FQN_COMMA_DELIMITED_LIST, subSystemsFQN);
        
        final String subSystemsSimpleNames = FrcCollectionExtsKt.toCommaDelimitedString(subsystems, false, PsiClass::getName);
        props.put(SUBSYSTEMS_SIMPLE_NAME_COMMA_DELIMITED_LIST, subSystemsSimpleNames);
        
        final String subSystemsVarNames = FrcCollectionExtsKt.toCommaDelimitedString(subsystems, false, psiClass -> 
                StringUtils.uncapitalize(psiClass.getName()));
        props.put(SUBSYSTEMS_VAR_NAME_COMMA_DELIMITED_LIST, subSystemsVarNames);
    }
    
    
    protected List<PsiClass> getSelectedSubsystems()
    {
        final List<PsiClass> subsystems = new ArrayList<>();
        for (Entry<PsiClass, JBCheckBox> entry : mySubsystemsClassesBiMap.entrySet())
        {
            if (entry.getValue().isSelected())
            {
                subsystems.add(entry.getKey());
            }
        }
        subsystems.sort(Comparator.comparing(NavigationItem::getName));
        return subsystems;
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

