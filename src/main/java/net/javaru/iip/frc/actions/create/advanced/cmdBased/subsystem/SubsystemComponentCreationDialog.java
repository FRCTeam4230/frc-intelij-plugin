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

package net.javaru.iip.frc.actions.create.advanced.cmdBased.subsystem;

import java.awt.*;
import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.ImmutableMap.Builder;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.ui.ContextHelpLabel;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import kotlin.Unit;
import net.javaru.iip.frc.actions.create.advanced.ClassCreator;
import net.javaru.iip.frc.actions.create.advanced.FrcComponentCreationDataProvider;
import net.javaru.iip.frc.actions.create.advanced.cmdBased.FrcComponentCreationDialog;
import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.util.FrcClassUtilsKt;
import net.javaru.iip.frc.util.FrcUiUtilsKt;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public class SubsystemComponentCreationDialog extends FrcComponentCreationDialog
{
    private static final String INCLUDE_ADD_CHILD_MESSAGE = "includeChildMessage";
    protected static final String MAKE_SINGLETON = "makeSingleton";
    protected static final String STATE_KEY_SUBSYSTEMS_MAKE_SINGLETON = "subsystems-makeSingleton";
    protected static final String SINGLETON_INIT_METHODOLOGY = "singletonInitMethodology";
    protected static final String STATE_KEY_SUBSYSTEMS_SINGLETON_INIT_METHODOLOGY = "subsystems-singletonInitMethodology";
    protected static final String SINGLETON_INCLUDE_JAVADOC = "singletonIncludeJavadoc";
    protected static final String STATE_KEY_SUBSYSTEMS_SINGLETON_INCLUDE_JAVADOC = "subsystems-singletonIncludeJavadoc";
    
    
    protected JBCheckBox makeSingletonCheckbox;
    protected JBRadioButton eagerSingletonRadioButton;
    protected JBRadioButton lazySingletonRadioButton;
    protected JBRadioButton doubleCheckedLockSingletonRadioButton;
    protected ButtonGroup singletonInitOptionButtonGroup;
    
    protected JBCheckBox includeSingletonJavadocCheckbox;
    
    public enum  SingletonMethodology { NON_SINGLETON, EAGER, LAZY, DOUBLE_CHECKED_LOCKING}
    
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
        
        final String singletonMethodology = makeSingleton ? singletonInitOptionButtonGroup.getSelection().getActionCommand() : "NONE";
        props.put(SINGLETON_INIT_METHODOLOGY, singletonMethodology);
        
        boolean includeJavadoc = (makeSingleton && includeSingletonJavadocCheckbox != null && includeSingletonJavadocCheckbox.isEnabled() && includeSingletonJavadocCheckbox.isSelected());
        props.put(SINGLETON_INCLUDE_JAVADOC, Boolean.toString(includeJavadoc));
    }
    
    public SingletonMethodology getDefaultSingletonMethodology()
    {
        if (FrcApplicationSettings.getInstance().isTeam3838())
            return SingletonMethodology.DOUBLE_CHECKED_LOCKING;
        else
            return SingletonMethodology.EAGER;
    }
    
    @Override
    protected void initMinorOptionsPanel(JPanel topPanel, JPanel optionsPanel)
    {
        makeSingletonCheckbox = new JBCheckBox(message("frc.templates.options.singletonSubsystems.checkbox.text.one"));
        makeSingletonCheckbox.setSelected(sharedState.getBooleanOption(STATE_KEY_SUBSYSTEMS_MAKE_SINGLETON, true));
    
        includeSingletonJavadocCheckbox = new JBCheckBox(message("frc.templates.options.singletonSubsystems.includeJavadoc.checkbox.text"));
        includeSingletonJavadocCheckbox.setSelected(sharedState.getBooleanOption(STATE_KEY_SUBSYSTEMS_SINGLETON_INCLUDE_JAVADOC, true));
        
        final ContextHelpLabel includeSingletonJavadocContextHelp =
            ContextHelpLabel.create(message("frc.templates.options.singletonSubsystems.includeJavadoc.contextHelp.text"));
        
    
        SingletonMethodology selectedMethodology = getDefaultSingletonMethodology();
    
        try
        {
            String singleMethodology = sharedState.getStringOption(STATE_KEY_SUBSYSTEMS_SINGLETON_INIT_METHODOLOGY,
                                                                   selectedMethodology.name());
            // handle previous name of DOUBLE_CHECKED_LOCKING option
            if ("THREAD_SAFE".equals(singleMethodology))
            {
                singleMethodology = SingletonMethodology.DOUBLE_CHECKED_LOCKING.name();
            }
            selectedMethodology = SingletonMethodology.valueOf(singleMethodology);
        }
        catch (Exception ignore)
        {
            selectedMethodology = getDefaultSingletonMethodology();
        }
    
        singletonInitOptionButtonGroup = new ButtonGroup();
        final boolean buttonsEnabled = makeSingletonCheckbox.isEnabled() && makeSingletonCheckbox.isSelected();
    
        final ContextHelpLabel makeSingletonContextHelp =
            ContextHelpLabel.createWithLink(null,
                                            message("frc.templates.options.singletonSubsystems.contextHelp.text"),
                                            message("frc.ui.common.learnMore.link.text"),
                                            () -> {BrowserUtil.browse("https://www.geeksforgeeks.org/singleton-design-pattern");});
    
        doubleCheckedLockSingletonRadioButton =
                initSingletonMethodologyButton(message("frc.templates.options.singletonSubsystems.doubleCheckedLockOption.text"),
                                               SingletonMethodology.DOUBLE_CHECKED_LOCKING,
                                               selectedMethodology,
                                               buttonsEnabled);
        
        final ContextHelpLabel doubleCheckedLockSingletonContextHelp =
            ContextHelpLabel.create(SubsystemOptionsHelpKt.getDoubleCheckedLockingInitializationHelp());
    
        
        lazySingletonRadioButton =
                initSingletonMethodologyButton(message("frc.templates.options.singletonSubsystems.lazyOption.text"),
                                               SingletonMethodology.LAZY,
                                               selectedMethodology,
                                               buttonsEnabled);
    
        final ContextHelpLabel lazySingletonContextHelp =
            ContextHelpLabel.create(SubsystemOptionsHelpKt.getClassicLazyInitializationHelp());
    
        
        eagerSingletonRadioButton =
                initSingletonMethodologyButton(message("frc.templates.options.singletonSubsystems.eagerOption.text"),
                                               SingletonMethodology.EAGER,
                                               selectedMethodology,
                                               buttonsEnabled);
    
        final ContextHelpLabel eagerSingletonContextHelp =
            ContextHelpLabel.create(SubsystemOptionsHelpKt.getEagerInitializationHelp());
    
        
        // Disable the make Singleton option is the name contains "abstract"
        FrcUiUtilsKt.addTextChangedListener(myComponentNameTextField, (documentEvent, text) -> {
            final String name = myComponentNameTextField.getText();
            makeSingletonCheckbox.setEnabled(name != null && !name.toLowerCase().contains("abstract"));
            includeSingletonJavadocCheckbox.setEnabled(makeSingletonCheckbox.isEnabled());
            final boolean optionsEnabled = makeSingletonCheckbox.isEnabled() && makeSingletonCheckbox.isSelected();
            doubleCheckedLockSingletonRadioButton.setEnabled(optionsEnabled);
            lazySingletonRadioButton.setEnabled(optionsEnabled);
            eagerSingletonRadioButton.setEnabled(optionsEnabled);
            return Unit.INSTANCE;
        });
        
        //Enable//disable the methodology buttons based on the make singleton checkbox selection
        makeSingletonCheckbox.addActionListener(e -> {
            final boolean optionsEnabled = makeSingletonCheckbox.isEnabled() && makeSingletonCheckbox.isSelected();
            includeSingletonJavadocCheckbox.setEnabled(optionsEnabled);
            doubleCheckedLockSingletonRadioButton.setEnabled(optionsEnabled);
            lazySingletonRadioButton.setEnabled(optionsEnabled);
            eagerSingletonRadioButton.setEnabled(optionsEnabled);
        });
        
        // Do layout
        final GridLayoutManager layoutManager = new GridLayoutManager(5, 3);
        final GridConstraints gc = createStandardGridConstraints();
        gc.setIndent(2);
        
        optionsPanel.setLayout(layoutManager);
        optionsPanel.add(makeSingletonCheckbox, gc);
    
        gc.setIndent(0);
        gc.setColumn(1);
        gc.setHSizePolicy(GridConstraints.SIZEPOLICY_FIXED);
        optionsPanel.add(makeSingletonContextHelp, gc);
        FrcUiUtilsKt.addHorizontalSpacer(optionsPanel, 0, 2);
    
        
        gc.setIndent(4);
        gc.setColumn(0);
        gc.setColSpan(3);
    
        gc.setRow(1);
        optionsPanel.add(createSubOptionPanel(eagerSingletonRadioButton, eagerSingletonContextHelp), gc);
        gc.setRow(2);
        optionsPanel.add(createSubOptionPanel(doubleCheckedLockSingletonRadioButton, doubleCheckedLockSingletonContextHelp), gc);
        gc.setRow(3);
        optionsPanel.add(createSubOptionPanel(lazySingletonRadioButton, lazySingletonContextHelp), gc);
        gc.setRow(4);
        optionsPanel.add(createSubOptionPanel(includeSingletonJavadocCheckbox, includeSingletonJavadocContextHelp), gc);
    }
    
    private JPanel createSubOptionPanel(Component button, ContextHelpLabel contextHelpLabel)
    {
        final GridLayoutManager layoutManager = new GridLayoutManager(1, 2);
        final GridConstraints gc = createStandardGridConstraints();
        JPanel panel = new JPanel(layoutManager);
        gc.setColumn(0);
        panel.add(button, gc);
        gc.setColumn(1);
        panel.add(contextHelpLabel, gc);
        return panel;
    }
    
    
    private JBRadioButton initSingletonMethodologyButton(@NotNull String buttonText,
                                                         @NotNull SingletonMethodology methodology,
                                                         @NotNull SingletonMethodology selectedMethodology,
                                                         boolean makeEnabled)
    {
        JBRadioButton button = new JBRadioButton();
        button.setText(buttonText);
        singletonInitOptionButtonGroup.add(button);
        button.setActionCommand(methodology.name());
        button.setSelected(methodology.equals(selectedMethodology));
        button.setEnabled(makeEnabled);
        return button;
    }
    
    
    @Override
    protected void saveState()
    {
        super.saveState();
        sharedState.updateBooleanOption(STATE_KEY_SUBSYSTEMS_MAKE_SINGLETON, makeSingletonCheckbox);
        sharedState.updateBooleanOption(STATE_KEY_SUBSYSTEMS_SINGLETON_INCLUDE_JAVADOC, includeSingletonJavadocCheckbox);
        sharedState.updateStringOption(STATE_KEY_SUBSYSTEMS_SINGLETON_INIT_METHODOLOGY, singletonInitOptionButtonGroup);
    }
}
