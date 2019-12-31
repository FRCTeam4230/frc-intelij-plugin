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

package net.javaru.iip.frc.actions.create.advanced.cmdBased.subsystem;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.ImmutableMap.Builder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.ContextHelpLabel;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import kotlin.Unit;
import net.javaru.iip.frc.actions.create.advanced.ClassCreator;
import net.javaru.iip.frc.actions.create.advanced.FrcComponentCreationDataProvider;
import net.javaru.iip.frc.actions.create.advanced.cmdBased.FrcComponentCreationDialog;
import net.javaru.iip.frc.util.FrcClassUtilsKt;
import net.javaru.iip.frc.util.FrcUiUtilsKt;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public class SubsystemComponentCreationDialog extends FrcComponentCreationDialog
{
    private static final String INCLUDE_ADD_CHILD_MESSAGE = "includeChildMessage";
    protected static final String MAKE_SINGLETON = "makeSingleton";
    protected static final String STATE_KEY_SUBSYSTEMS_MAKE_SINGLETON = "subsystems-makeSingleton";
    
    protected JBCheckBox makeSingletonCheckbox;
    
    public SubsystemComponentCreationDialog(@NotNull Module module,
                                            @NotNull ClassCreator classCreator,
                                            @NotNull PsiDirectory directory,
                                            @NotNull FrcComponentCreationDataProvider dataProvider)
    {
        super(module, classCreator, directory, dataProvider);
    }
    
    
    @Override
    protected void addComponentSpecificProperties(@NotNull Builder<String, String> props,
                                                  @Nullable PsiClass baseClass,
                                                  @NotNull String targetPackageName,
                                                  @NotNull String newClassName)
    {
        if (myDataProvider.getComponentVersion() >= 2 && baseClass != null)
        {
            final PsiClass subsystemBaseClass = FrcClassUtilsKt.findClassAssumeOneIfAny(myProject, myDataProvider.getTypicalBaseClassFqName());
            boolean includeChildMessage =  subsystemBaseClass != null && (baseClass.equals(subsystemBaseClass) ||  baseClass.isInheritor(subsystemBaseClass, true));
            props.put(INCLUDE_ADD_CHILD_MESSAGE, Boolean.toString(includeChildMessage));
        }
        boolean makeSingleton = (makeSingletonCheckbox != null && makeSingletonCheckbox.isEnabled() && makeSingletonCheckbox.isSelected());
        props.put(MAKE_SINGLETON, Boolean.toString(makeSingleton));
    }
    
    
    @Override
    protected void initMinorOptionsPanel(JPanel topPanel, JPanel optionsPanel)
    {
        final GridLayoutManager layoutManager = new GridLayoutManager(1, 3);
        final GridConstraints gc = createStandardGridConstraints();
        gc.setIndent(2);
        makeSingletonCheckbox = new JBCheckBox(message("frc.new.class.adv.subsystem.dialog.makeSingleton.checkbox.text"));
        makeSingletonCheckbox.setSelected(sharedState.getBooleanOption(STATE_KEY_SUBSYSTEMS_MAKE_SINGLETON, true));
        optionsPanel.setLayout(layoutManager);
        optionsPanel.add(makeSingletonCheckbox, gc);
    
        final ContextHelpLabel helpLabel = ContextHelpLabel.create(message("frc.new.class.adv.subsystem.dialog.makeSingleton.help.title"), 
                                                                   message("frc.new.class.adv.subsystem.dialog.makeSingleton.help.text"));
        gc.setIndent(0);
        gc.setColumn(1);
        gc.setHSizePolicy(GridConstraints.SIZEPOLICY_FIXED);
        optionsPanel.add(helpLabel, gc);
        FrcUiUtilsKt.addHorizontalSpacer(optionsPanel, 0, 2);
        
        // Disable the make Singleton option is the name contains "abstract"
        FrcUiUtilsKt.addTextChangedListener(myComponentNameTextField, (documentEvent, text) -> {
            final String name = myComponentNameTextField.getText();
            makeSingletonCheckbox.setEnabled(name != null && !name.toLowerCase().contains("abstract"));
            return Unit.INSTANCE;
        });
    }
    
    
    @Override
    protected void saveState()
    {
        super.saveState();
        sharedState.updateBooleanOption(STATE_KEY_SUBSYSTEMS_MAKE_SINGLETON, makeSingletonCheckbox);
    }
}
