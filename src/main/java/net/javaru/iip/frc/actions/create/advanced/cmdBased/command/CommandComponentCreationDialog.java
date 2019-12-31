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

package net.javaru.iip.frc.actions.create.advanced.cmdBased.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.*;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.components.JBCheckBox;

import net.javaru.iip.frc.actions.create.advanced.ClassCreator;
import net.javaru.iip.frc.actions.create.advanced.FrcComponentCreationDataProvider;
import net.javaru.iip.frc.actions.create.advanced.cmdBased.FrcComponentCreationDialog;
import net.javaru.iip.frc.util.FrcClassUtilsKt;
import net.javaru.iip.frc.util.FrcCollectionExtsKt;
import net.javaru.iip.frc.util.FrcUiUtilsKt;
import net.javaru.iip.frc.util.PsiClassNameComparator;
import net.javaru.iip.frc.wpilib.WpiLibConstants;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public class CommandComponentCreationDialog extends FrcComponentCreationDialog
{
    private static final String SUBSYSTEMS_FQN_COMMA_DELIMITED_LIST = "requiredSubsystemsFqnCommaDelimitedString";
    private static final String SUBSYSTEMS_SIMPLE_NAME_COMMA_DELIMITED_LIST = "requiredSubsystemsNamesCommaDelimitedString";
    private static final String SUBSYSTEMS_VAR_NAME_COMMA_DELIMITED_LIST = "requiredSubsystemsVarsCommaDelimitedString";
    
    private Map<PsiClass, JBCheckBox> mySubsystemsClassesMap;
    
    
    public CommandComponentCreationDialog(@NotNull Module module,
                                          @NotNull ClassCreator classCreator,
                                          @NotNull PsiDirectory directory,
                                          @NotNull FrcComponentCreationDataProvider dataProvider)
    {
        super(module, classCreator, directory, dataProvider);
    }
    
    
    @Override
    protected void initMajorOptionsPanel(JPanel topPanel, JPanel optionsPanel)
    {
        // Display subsystems
        final String labelText = message("frc.new.class.adv.command.dialog.subsystems.label");
        List<PsiClass> subsystems = FrcClassUtilsKt.findImplementationsInModule(getSubsystemTopClassFQN(),
                                                                                myModule,
                                                                                false,
                                                                                false,
                                                                                null);
        subsystems = FrcClassUtilsKt.sortedByName(subsystems);
        // TODO: add option to filter out abstract 
        // See our FrcClassUtilsKt.isAbstract() extension
        this.mySubsystemsClassesMap = FrcUiUtilsKt.initClassSelectionPanelCheckBoxes(topPanel,
                                                                                     optionsPanel,
                                                                                     labelText,
                                                                                     subsystems,
                                                                                     true);
    }
    
    
    protected String getSubsystemTopClassFQN()
    {
        if (myDataProvider instanceof CommandCreationDataProvider)
        {
            return ((CommandCreationDataProvider) myDataProvider).getSubsystemTopFqName();
        }
        else
        {
            return WpiLibConstants.SUBSYSTEM_V2_TOP_FQN;
        }
    }
    
    
    @Override
    protected void addComponentSpecificProperties(@NotNull Builder<String, String> props,
                                                  @Nullable PsiClass baseClass,
                                                  @NotNull String targetPackageName,
                                                  @NotNull String newClassName)
    {
        addSubSystemProperties(props);
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
}
