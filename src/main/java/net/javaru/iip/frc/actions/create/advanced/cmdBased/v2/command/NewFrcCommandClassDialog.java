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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;

import kotlin.Unit;
import net.javaru.iip.frc.actions.create.advanced.ClassCreator;
import net.javaru.iip.frc.actions.create.advanced.NewFrcClassDialog;
import net.javaru.iip.frc.util.FrcClassUtilsKt;
import net.javaru.iip.frc.util.FrcCollectionExtsKt;
import net.javaru.iip.frc.util.FrcUiUtilsKt;
import net.javaru.iip.frc.util.PsiClassNameComparator;
import net.javaru.iip.frc.wpilib.WpiLibConstants;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public class NewFrcCommandClassDialog extends NewFrcClassDialog
{
    public static final String SUBSYSTEMS_FQN_COMMA_DELIMITED_LIST = "requiredSubsystemsFqnCommaDelimitedString";
    public static final String SUBSYSTEMS_SIMPLE_NAME_COMMA_DELIMITED_LIST = "requiredSubsystemsNamesCommaDelimitedString";
    public static final String SUBSYSTEMS_VAR_NAME_COMMA_DELIMITED_LIST = "requiredSubsystemsVarsCommaDelimitedString";
    public static final String BASE_CLASS_FQ_NAME = "baseClassFqName";
    public static final String BASE_CLASS_NEEDS_IMPORTING = "baseClassNeedsImporting";
    public static final String BASE_CLASS_EXTENDS_CLAUSE = "baseClassExtendsClause";
    public static final String NEEDS_GET_REQUIREMENTS = "needsGetRequirementsImpl";
    public static final String INCLUDE_JAVADOC_FOR_OVERRIDES = "includeJavaDocsForOverrides";
    public static final String MAKE_ABSTRACT = "makeAbstract";
    
    private static final Logger LOG = Logger.getInstance(NewFrcCommandClassDialog.class);
    private JPanel myTopPanel;
    private JBLabel myCommandNameLabel;
    private JBTextField myCommandNameTextField;
    private JCheckBox myAutoAppendComponentTypeCheckBox;
    private JPanel mySubsystemsPanel;
    private JPanel mySuperClassPanel;
    private JCheckBox myIncludeJavaDocCheckBox;
    
    private Map<PsiClass, JBCheckBox> mySubsystemsClassesMap;
    private Map<PsiClass, JBRadioButton> myTopLevelCommandClassesMap;
    private ButtonGroup mySuperButtonGroup = new ButtonGroup();
    
    public NewFrcCommandClassDialog(@NotNull Module module,
                                    @NotNull ClassCreator classCreator,
                                    @NotNull PsiDirectory directory)
    {
        super(module, classCreator, directory, NewFrcCommandClassV2DataProvider.INSTANCE);
        setTitle(getTitle());
        initUiComponents();
        init(); //from DialogWrapper SHOULD BE LAST STATEMENT IN CONSTRUCTOR 
    }
    
    
    protected void initUiComponents()
    {
        //TODO init to last used
        myAutoAppendComponentTypeCheckBox.setSelected(true);
        myAutoAppendComponentTypeCheckBox.setText(message("frc.new.class.adv.general.dialog.autoAppend.text", myDataProvider.getClassTypeSimpleName()));
        
        myIncludeJavaDocCheckBox.setSelected(true);
    
        FrcUiUtilsKt.addTextChangedListener(myCommandNameTextField, (documentEvent, text) -> {
            myAutoAppendComponentTypeCheckBox.setEnabled(nameCanBeAutoAppended(myCommandNameTextField.getText().trim()));
            return Unit.INSTANCE;
        });
    
        initSuperClassPanel();
        initOptionsPanel();
    }
    
    protected void initSuperClassPanel()
    {
        final String labelText = message("frc.new.class.adv.command.dialog.commandBase.label");
        final List<PsiClass> classes = getExtendableClasses();
        this.myTopLevelCommandClassesMap = FrcUiUtilsKt.initClassSelectionPanelRadioButtons(myTopPanel,
                                                                                            mySuperClassPanel,
                                                                                            labelText,
                                                                                            mySuperButtonGroup,
                                                                                            classes,
                                                                                            true);
    
        final Set<Entry<PsiClass, JBRadioButton>> entries = this.myTopLevelCommandClassesMap.entrySet();
        for (Entry<PsiClass, JBRadioButton> entry : entries)
        {
            // TODO: modify to use last selected value
            if (WpiLibConstants.COMMAND_V2_BASE_FQN.equals(entry.getKey().getQualifiedName()))
            {
                entry.getValue().setSelected(true);
            }
        }
    }
    
    protected void initOptionsPanel()
    {
        initSubsystemSelectionPanel();
    }
    
    protected void initSubsystemSelectionPanel()
    {
        final String labelText = message("frc.new.class.adv.command.dialog.subsystems.label");
        List<PsiClass> subsystems = FrcClassUtilsKt.findImplementationsInModule(getSubsystemTopClassFQN(),
                                                                                myModule,
                                                                                false,
                                                                                false,
                                                                                null);
        subsystems = FrcClassUtilsKt.sortedByName(subsystems);
        // TODO: add option to filter out abstract 
        // See our FrcClassUtilsKt.isAbstract() extension
        this.mySubsystemsClassesMap = FrcUiUtilsKt.initClassSelectionPanelCheckBoxes(myTopPanel, 
                                                                                     mySubsystemsPanel, 
                                                                                     labelText, 
                                                                                     subsystems,
                                                                                     true);
    }
    
    
    @Override
    protected Map<String, String> getAdditionalProperties(@NotNull String targetPackageName, @NotNull String newClassName)
    {
        ImmutableMap.Builder<String, String> props = ImmutableMap.builder();
        addGeneralProperties(props);
        addSubSystemProperties(props);
        addBaseClassProperties(props, targetPackageName, newClassName);
        return props.build();
    }
    
    protected void addGeneralProperties(@NotNull ImmutableMap.Builder<String, String> props)
    {
        props.put(INCLUDE_JAVADOC_FOR_OVERRIDES, Boolean.toString(myIncludeJavaDocCheckBox.isSelected()));
    }
    
    protected void addSubSystemProperties(@NotNull ImmutableMap.Builder<String, String> props)
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
    
    protected void addBaseClassProperties(@NotNull Builder<String, String> props, @NotNull String targetPackageName, @NotNull String newClassName)
    {
        PsiClass base = null;
        for (Entry<PsiClass, JBRadioButton> entry : myTopLevelCommandClassesMap.entrySet())
        {
            if (entry.getValue().isSelected())
            {
                base = entry.getKey();
                break;
            }
        }
        if (base == null)
        {
            // should not happen, but just in case
            LOG.warn("[FRC] No selected Option found for the Base Class. Will attempt to default to the Typical Base Class.");
            final PsiClass[] classes = FrcClassUtilsKt.findClass(myProject, myDataProvider.getTypicalBaseClassFqName());
            if (classes.length > 0)
            {
                base = classes[0];
            }
        }
        
        if (base == null)
        {
            LOG.warn("[FRC] Could not find the Typical Base Class. Cannot configure base class properties for the template. Template creation will result in invalid class.");
            return;
        }
        
        final String baseFqName = base.getQualifiedName() != null ? base.getQualifiedName() : myDataProvider.getTypicalBaseClassFqName();
        props.put(BASE_CLASS_FQ_NAME, baseFqName);
        final boolean baseClassNeedsImporting = !baseFqName.contains(".") || !baseFqName.substring(0, baseFqName.lastIndexOf('.')).equals(targetPackageName);
        props.put(BASE_CLASS_NEEDS_IMPORTING, Boolean.toString(baseClassNeedsImporting));
        final String baseName = base.getName() != null ? base.getName() : myDataProvider.getTypicalBaseClassFqName().substring(myDataProvider.getTypicalBaseClassFqName().lastIndexOf('.') + 1);
        final String extendsClause = base.isInterface() ? "implements " + baseName : "extends " + baseName;
        props.put(BASE_CLASS_EXTENDS_CLAUSE, extendsClause);
        props.put(MAKE_ABSTRACT, Boolean.toString(newClassName.contains("Abstract")));
        // TODO it'd be nice to make this more sophisticated (to handle the event of a custom interface that has the getRequirements as a default
        props.put(NEEDS_GET_REQUIREMENTS, Boolean.toString(base.isInterface()));
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
        subsystems.sort(PsiClassNameComparator.INSTANCE);
        return subsystems;
    }
    
    protected String getSubsystemTopClassFQN()
    {
        return WpiLibConstants.SUBSYSTEM_V2_TOP_FQN;
    }
    
    
    @NotNull
    @Override
    protected JTextComponent getNewClassNameField() { return myCommandNameTextField; }
    
    
    @Nullable
    @Override
    protected JComponent createCenterPanel()
    {
        return myTopPanel;
    }
    
    
    @Override
    protected String getNewClassName()
    {
        final String name = getNewClassNameField().getText().trim();
        return myAutoAppendComponentTypeCheckBox.isEnabled() && myAutoAppendComponentTypeCheckBox.isSelected() && !isBaseClassName(name)
               ? name + myDataProvider.getClassTypeSimpleName() 
               : name;
    }
}    

