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
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
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
import net.javaru.iip.frc.util.FrcUiUtilsKt;
import net.javaru.iip.frc.wpilib.WpiLibConstants;

import static com.intellij.uiDesigner.core.GridConstraints.*;
import static net.javaru.iip.frc.i18n.FrcBundle.message;
import static net.javaru.iip.frc.wpilib.WpiLibConstants.COMMAND_V2_INTERFACE_FQN;



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
    
    private Map<PsiClass, JBCheckBox> mySubsystemsClassesMap;
    
    public NewFrcCommandClassDialog(@NotNull Module module,
                                    @NotNull ClassCreator classCreator,
                                    @NotNull PsiDirectory directory)
    {
        super(module, classCreator, directory);
        setTitle(getTitle());
        initUiComponents();
        init(); //from DialogWrapper SHOULD BE LAST STATEMENT IN CONSTRUCTOR 
    }
    
    
    protected void initUiComponents()
    {
        myAutoAppendCommandCheckBox.setSelected(true);
    
        FrcUiUtilsKt.addTextChangedListener(myCommandNameTextField, (documentEvent, text) -> {
            myAutoAppendCommandCheckBox.setEnabled(!myCommandNameTextField.getText().endsWith("Command") && 
                                                   !myCommandNameTextField.getText().endsWith("Cmd"));
            return Unit.INSTANCE;
        });
    
        initSuperClassPanel();
        initSubsystemSelectionPanel();
    }
    
    protected void initSubsystemSelectionPanel()
    {
        final String labelText = message("frc.new.class.adv.command.dialog.subsystems.label");
        final List<PsiClass> subsystems = FindClassUtilsKt.findImplementationsInModule(getSubsystemTopClassFQN(),
                                                                                       myModule,
                                                                                       false,
                                                                                       false,
                                                                                       null);
        
        // TODO: add option to filter out abstract 
        // See our FindClassUtils (to be renamed FrcClassUtils) isAbstract() extension
        this.mySubsystemsClassesMap = FrcUiUtilsKt.initClassSelectionPanel(myTopPanel, mySubsystemsPanel, labelText, subsystems);
    }
    
    
    protected static BiMap<PsiClass, JBCheckBox> initClassSelectionPanel(JComponent topComponent, JPanel panel, String labelText, List<PsiClass> classes)
    {
        final ImmutableBiMap.Builder<PsiClass, JBCheckBox> mapBuilder = ImmutableBiMap.builder();
        if (!classes.isEmpty())
        {
            final int classesCount = classes.size();
            int rowCount = classesCount + 1;
            int colCount = 1;
            
            if (classesCount > 12)
            {
                colCount = 3;
                rowCount = (classesCount / 3) + 1;
                if (classesCount % 3 != 0)
                {
                    rowCount++;
                }
            }
            else if (classesCount > 6)
            {
                colCount = 2;
                rowCount = (classesCount / 2) + 1;
                if (classesCount % 2 != 0)
                {
                    rowCount++;
                }
            }
        
            final GridLayoutManager manager = new GridLayoutManager(rowCount, colCount);
            panel.setLayout(manager);
        
            final GridConstraints gc = new GridConstraints(0, 0, 1, 1,
                                                           ANCHOR_WEST,
                                                           FILL_NONE,
                                                           (SIZEPOLICY_CAN_GROW | SIZEPOLICY_CAN_SHRINK),
                                                           SIZEPOLICY_FIXED,
                                                           new Dimension(-1, -1),
                                                           new Dimension(-1, -1),
                                                           new Dimension(-1, -1),
                                                           1);
            gc.setColSpan(colCount);
            JBLabel label = new JBLabel(labelText);
            panel.add(label, gc);
            gc.setColSpan(1);
            
            classes.sort(Comparator.comparing(NavigationItem::getName));
            final Map<PsiClass, Character> mnemonics = FrcUiUtilsKt.calculateMnemonics(topComponent, classes, NavigationItem::getName);
    
            gc.setIndent(3);
            int row = 0;
            for (PsiClass psiClass : classes)
            {
                gc.setRow(++row);
                final JBCheckBox checkBox = new JBCheckBox(psiClass.getName());
            }
        }
    
        return mapBuilder.build();
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
        for (Entry<PsiClass, JBCheckBox> entry : mySubsystemsClassesMap.entrySet())
        {
            if (entry.getValue().isSelected())
            {
                subsystems.add(entry.getKey());
            }
        }
        subsystems.sort(Comparator.comparing(NavigationItem::getName));
        return subsystems;
    }
    
    protected String getSubsystemTopClassFQN()
    {
        return WpiLibConstants.SUBSYSTEM_V2_TOP_FQN;
    }
    
    protected List<PsiClass> getCommandTopClasses()
    {
        final ImmutableList.Builder<PsiClass> classes = ImmutableList.builder();
        classes.add(FindClassUtilsKt.findClass(myModule, COMMAND_V2_INTERFACE_FQN));
        classes.add(FindClassUtilsKt.findClass(myModule, WpiLibConstants.COMMAND_V2_BASE_FQN));
    
        final List<PsiClass> projectImpls = FindClassUtilsKt.findImplementationsInModule(COMMAND_V2_INTERFACE_FQN,
                                                                                       myModule,
                                                                                       false,
                                                                                       false,
                                                                                       null);
        final List<PsiClass> list = projectImpls.stream().filter(FindClassUtilsKt::isInterfaceOrAbstract).collect(Collectors.toList());
        classes.addAll(list);
    
        return classes.build();
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

