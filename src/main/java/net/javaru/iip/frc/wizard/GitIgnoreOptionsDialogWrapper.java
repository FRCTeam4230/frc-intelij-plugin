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

package net.javaru.iip.frc.wizard;

import javax.swing.*;

import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;

import kotlin.Unit;
import net.javaru.iip.frc.util.FrcCollectionExtsKt;
import net.javaru.iip.frc.util.FrcUiUtilsKt;



public class GitIgnoreOptionsDialogWrapper extends DialogWrapper
{
    private static final Logger LOG = Logger.getInstance(GitIgnoreOptionsDialogWrapper.class);
    
    
    private JPanel rootPanel;
    private JBLabel intellijIdeaOptionsLabel;
    private JBRadioButton intellijShareRadioButton;
    private JBRadioButton intellijIgnoreRadioButton;
    private JBRadioButton intellijNoEntryRadioButton;
    private JBLabel vscodeOptionsLabel;
    private JBRadioButton vscodeShareRadioButton;
    private JBRadioButton vscodeIgnoreRadioButton;
    private JBRadioButton vscodeNoEntryRadioButton;
    private JBCheckBox gradleCheckBox;
    private JBCheckBox javaCheckBox;
    private JBCheckBox linuxCheckBox;
    private JBCheckBox macCheckBox;
    private JBCheckBox winCheckBox;
    private JBCheckBox cppCheckBox;
    private JLabel advancedLabel;
    private JBLabel additionalGitignoreTemplatesLabel;
    private JBTextField additionalGitignoreTemplatesTextField;
    private JButton defaultButton;
    private JBCheckBox generateFromSiteCheckBox;
    
    private GitIgnoreConfiguration configuration;
    
    public GitIgnoreOptionsDialogWrapper(JComponent parent, GitIgnoreConfiguration gitIgnoreConfiguration)
    {
        // www.jetbrains.org/intellij/sdk/docs/user_interface_components/dialog_wrapper.html
        super(parent, true);
        this.configuration = gitIgnoreConfiguration;
        init();
        //noinspection DialogTitleCapitalization
        setTitle("Configure .gitignore Options"); // TODO move to messages bundle
        initComponents();
    }
    
    
    /**
     * Factory method. It creates panel with dialog options. Options panel is located at the
     * center of the dialog's content pane. The implementation can return {@code null}
     * value. In this case there will be no options panel.
     */
    @Nullable
    @Override
    protected JComponent createCenterPanel()
    {
       return rootPanel;
    }
    
    
    private void initComponents()
    {
        updateComponentValues();
        GitIgnoreOptionsHelperKt.initIdeButtonGroupChangeListener(intellijShareRadioButton, intellijIgnoreRadioButton, intellijNoEntryRadioButton, ideConfigOption -> {
            configuration.setIntellij(ideConfigOption);
            return Unit.INSTANCE;
        });
        
        GitIgnoreOptionsHelperKt.initIdeButtonGroupChangeListener(vscodeShareRadioButton, vscodeIgnoreRadioButton, vscodeNoEntryRadioButton, ideConfigOption -> {
            configuration.setVscode(ideConfigOption);
            return Unit.INSTANCE;
        });
    
//        GitIgnoreOptionsHelperKt.initIdeButtonGroupChangeListener(eclipseShareRadioButton, eclipseIgnoreRadioButton, eclipseNoEntryRadioButton, ideConfigOption -> {
//            configuration.setEclipse(ideConfigOption);
//            return Unit.INSTANCE;
//        }); 
//        
//        GitIgnoreOptionsHelperKt.initIdeButtonGroupChangeListener(netBeansShareRadioButton, netBeansIgnoreRadioButton, netBeansNoEntryRadioButton, ideConfigOption -> {
//            configuration.setNetBeans(ideConfigOption);
//            return Unit.INSTANCE;
//        });
    
        gradleCheckBox.addActionListener(e -> configuration.setGradle(gradleCheckBox.isSelected()));
        javaCheckBox.addActionListener(e -> configuration.setJava(javaCheckBox.isSelected()));
        linuxCheckBox.addActionListener(e -> configuration.setLinux(linuxCheckBox.isSelected()));
        macCheckBox.addActionListener(e -> configuration.setMacOS(macCheckBox.isSelected()));
        winCheckBox.addActionListener(e -> configuration.setWindows(winCheckBox.isSelected()));
        cppCheckBox.addActionListener(e -> configuration.setCpp(cppCheckBox.isSelected()));
    
        FrcUiUtilsKt.addTextChangedListener(additionalGitignoreTemplatesTextField, text -> {
            configuration.setAdditionalGitignoreTemplates(FrcCollectionExtsKt.commaDelimitedToList(additionalGitignoreTemplatesTextField.getText()));
            return Unit.INSTANCE; });
    
        generateFromSiteCheckBox.setSelected(configuration.getGenerateFromSite());
        additionalGitignoreTemplatesTextField.setEnabled(configuration.getGenerateFromSite());
        additionalGitignoreTemplatesLabel.setCopyable(true);
        additionalGitignoreTemplatesLabel.setEnabled(configuration.getGenerateFromSite());
        generateFromSiteCheckBox.addActionListener(e-> {
            final boolean isSelected = generateFromSiteCheckBox.isSelected();
            additionalGitignoreTemplatesTextField.setEnabled(isSelected);
            additionalGitignoreTemplatesLabel.setEnabled(isSelected);
            configuration.setGenerateFromSite(isSelected);
        });
        
        defaultButton.addActionListener(e -> {resetAllToDefaultValues();});
    }
    
    private void resetAllToDefaultValues()
    {
        GitIgnoreConfiguration defaults = GitIgnoreConfiguration.Companion.defaultInstance();
        configuration.setIntellij(defaults.getIntellij());
        configuration.setVscode(defaults.getVscode());
        configuration.setEclipse(defaults.getEclipse());
        configuration.setNetbeans(defaults.getNetbeans());
        configuration.setGradle(defaults.getGradle());
        configuration.setJava(defaults.getJava());
        configuration.setLinux(defaults.getLinux());
        configuration.setMacOS(defaults.getMacOS());
        configuration.setWindows(defaults.getWindows());
        configuration.setCpp(defaults.getCpp());
        configuration.setAdditionalGitignoreTemplates(defaults.getAdditionalGitignoreTemplates());
        configuration.setGenerateFromSite(defaults.getGenerateFromSite());
        updateComponentValues();
    }
    
    private void updateComponentValues()
    {
        switch (configuration.getIntellij())
        {
            case Share:
                intellijShareRadioButton.setSelected(true);
                break;
            case Ignore:
                intellijIgnoreRadioButton.setSelected(true);
                break;
            case NoEntry:
                intellijNoEntryRadioButton.setSelected(true);
        }
        
        switch (configuration.getVscode())
        {
            case Share:
                vscodeShareRadioButton.setSelected(true);
                break;
            case Ignore:
                vscodeIgnoreRadioButton.setSelected(true);
                break;
            case NoEntry:
                vscodeNoEntryRadioButton.setSelected(true);
        }
    
//        switch (configuration.getEclipse())
//        {
//            case Share:
//                eclipseShareRadioButton.setSelected(true);
//                break;
//            case Ignore:
//                eclipseIgnoreRadioButton.setSelected(true);
//                break;
//            case NoEntry:
//                eclipseNoEntryRadioButton.setSelected(true);
//        }
//    
//        switch (configuration.getNetBeans())
//        {
//            case Share:
//                netBeansShareRadioButton.setSelected(true);
//                break;
//            case Ignore:
//                netBeansIgnoreRadioButton.setSelected(true);
//                break;
//            case NoEntry:
//                netBeansNoEntryRadioButton.setSelected(true);
//        }
    
        gradleCheckBox.setSelected(configuration.getGradle());
        javaCheckBox.setSelected(configuration.getJava());
        linuxCheckBox.setSelected(configuration.getLinux());
        macCheckBox.setSelected(configuration.getMacOS());
        winCheckBox.setSelected(configuration.getWindows());
        cppCheckBox.setSelected(configuration.getCpp());
        generateFromSiteCheckBox.setSelected(configuration.getGenerateFromSite());
        additionalGitignoreTemplatesTextField.setText(FrcCollectionExtsKt.toCommaDelimitedString(configuration.getAdditionalGitignoreTemplates()));
    }
    
 
}
