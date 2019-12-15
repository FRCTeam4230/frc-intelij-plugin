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

package net.javaru.iip.frc.wizard;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import javax.swing.*;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.model.project.ProjectId;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.lang.JavaVersion;

import kotlin.Unit;
import net.javaru.iip.frc.FrcIcons.FRC;
import net.javaru.iip.frc.FrcPluginGlobals;
import net.javaru.iip.frc.i18n.FrcMessageKey;
import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.settings.FrcTeamNumberKt;
import net.javaru.iip.frc.settings.TeamNumberFormChangeListener;
import net.javaru.iip.frc.settings.TeamNumberKeyChangeListener;
import net.javaru.iip.frc.util.FrcJavaLangUtilsKt;
import net.javaru.iip.frc.util.FrcPsiNameHelper;
import net.javaru.iip.frc.util.UiUtilsKt;
import net.javaru.iip.frc.wizard.FrcProjectWizardData.JUnitOption;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public class FrcProjectSettingsWizardStep extends ModuleWizardStep implements TeamNumberFormChangeListener
{
    private static final Logger LOG = Logger.getInstance(FrcProjectSettingsWizardStep.class);
    
    @NotNull
    private final FrcModuleBuilder myBuilder;
    @NotNull
    private final WizardContext myContext;
    @Nullable
    private final Project myProjectOrNull;
    @NotNull
    private final FrcParentProjectForm myParentProjectForm;
    
    private JPanel rootPanel;
    private JPanel robotDetailsPanel;
    private JBTextField teamNumberTextField;
    private JPanel nextPanelToBeRenamed;
    private JBLabel basePackageLabel;
    private JBTextField basePackageTextField;
    private JButton basePackageDefaultButton;
    private JBLabel teamNumberWarningIconLabel;
    private JCheckBox includeVsCodeConfigsCheckBox;
    private JBLabel basePackageWarningLabel;
    private JPanel gitignorePanel;
    private JCheckBox includeGitignoreFileCheckBox;
    private JButton configureGitignoreButton;
    private JPanel junitPanel;
    private JCheckBox junitCheckBox;
    private JRadioButton junit5RadioButton;
    private JRadioButton junit5withVintageRadioButton;
    private JRadioButton junit4RadioButton;
    
    
    public FrcProjectSettingsWizardStep(@NotNull FrcModuleBuilder builder, @NotNull WizardContext context)
    {
        LOG.trace("[FRC] FrcProjectSettingsWizardStep constructor has been called.");
        this.myBuilder = builder;
        this.myContext = context;
        this.myProjectOrNull = context.getProject();
        myParentProjectForm = new FrcParentProjectForm(context, parentProject -> updateComponents());
        initComponents();
        loadSettings();
        LOG.trace("[FRC] FrcProjectSettingsWizardStep constructor has completed.");
    }
    
    
    private void initComponents()
    {
        LOG.trace("[FRC] Entering FrcProjectSettingsWizardStep.initComponents()");
 //       myAddToPanel.add(myParentProjectForm.getComponent());
        ActionListener updatingListener = e -> updateComponents();
        // TODO add the Action Listener to any components that need to take action upon updating
        
       
        initTeamNumberField();
        final FrcProjectWizardData dataModel = myBuilder.getDataModel();
        
        UiUtilsKt.setTextIfEmpty(basePackageTextField, dataModel.getBasePackage());
        basePackageWarningLabel.setVisible(false);
        UiUtilsKt.addTextChangedListener(basePackageTextField, text -> { basePackageWarningLabel.setVisible(StringUtils.isBlank(text));
            return Unit.INSTANCE;
        });
        basePackageDefaultButton.addActionListener(e -> basePackageTextField.setText(FrcProjectWizardDataKt.DEFAULT_BASE_PACKAGE));
        includeVsCodeConfigsCheckBox.setSelected(dataModel.getIncludeVsCodeConfigs());
        includeGitignoreFileCheckBox.setSelected(dataModel.getGitIgnoreConfiguration().getIncludeGitIgnoreFile());
        includeGitignoreFileCheckBox.addActionListener(e -> {dataModel.getGitIgnoreConfiguration().setIncludeGitIgnoreFile(includeGitignoreFileCheckBox.isSelected());});
        configureGitignoreButton.setEnabled(dataModel.getGitIgnoreConfiguration().getIncludeGitIgnoreFile());
        includeGitignoreFileCheckBox.addChangeListener(e -> configureGitignoreButton.setEnabled(includeGitignoreFileCheckBox.isSelected()));
        configureGitignoreButton.addActionListener(e -> displayGitIgnoreConfigurationDialog());
        
        junit5RadioButton.setActionCommand(JUnitOption.JUnit5.name());
        junit5withVintageRadioButton.setActionCommand(JUnitOption.JUnit5withVintage.name());
        junit4RadioButton.setActionCommand(JUnitOption.JUnit4.name());
        
        
        junitCheckBox.setSelected(dataModel.getIncludeJUnitSupport());
        junit5RadioButton.setEnabled(dataModel.getIncludeJUnitSupport());
        junit5withVintageRadioButton.setEnabled(dataModel.getIncludeJUnitSupport());
        junit4RadioButton.setEnabled(dataModel.getIncludeJUnitSupport());
        
        switch (dataModel.getJunitOption())
        {
            case JUnit5:
                junit5RadioButton.setSelected(true);
                break;
            case JUnit5withVintage:
                junit5withVintageRadioButton.setSelected(true);
                break;
            case JUnit4:
                junit4RadioButton.setSelected(true);
        }
    
        ItemListener junitVersionOptionChangeListener = e -> {
            final AbstractButton button = (AbstractButton) e.getSource();
            final ButtonModel model = button.getModel();
            final String actionCommand = model.getActionCommand();
            dataModel.setJunitOption(JUnitOption.valueOf(actionCommand));
        };
    
        junit5RadioButton.addItemListener(junitVersionOptionChangeListener);
        junit5withVintageRadioButton.addItemListener(junitVersionOptionChangeListener);
        junit4RadioButton.addItemListener(junitVersionOptionChangeListener);
    
        junitCheckBox.addChangeListener(e -> {
            dataModel.setIncludeJUnitSupport(junitCheckBox.isSelected());
            junit5RadioButton.setEnabled(dataModel.getIncludeJUnitSupport());
            junit5withVintageRadioButton.setEnabled(dataModel.getIncludeJUnitSupport());
            junit4RadioButton.setEnabled(dataModel.getIncludeJUnitSupport());
        });
        
        LOG.trace("[FRC] Entering FrcProjectSettingsWizardStep.initComponents()");
    }
    
    
    private void displayGitIgnoreConfigurationDialog()
    {
    
        final FrcProjectWizardData dataModel = myBuilder.getDataModel();
        final GitIgnoreConfiguration gitIgnoreConfiguration = dataModel.getGitIgnoreConfiguration().fullCopy();
        final GitIgnoreOptionsDialogWrapper dialogWrapper = new GitIgnoreOptionsDialogWrapper(rootPanel, gitIgnoreConfiguration);
        final boolean ok = dialogWrapper.showAndGet();
        if (ok)
        {
            dataModel.setGitIgnoreConfiguration(gitIgnoreConfiguration); 
        }
    }
    
    
    private void initTeamNumberField()
    {
        // We may need to update this when the team number changed from an application setting to a project setting
        UiUtilsKt.setTextIfEmpty(teamNumberTextField, Integer.toString(myBuilder.getDataModel().getTeamNumber()));
        updateTeamNumberWarningVisibility(FrcTeamNumberKt.isValidTeamNumber(teamNumberTextField.getText()));
        // The TeamNumberKeyChangeListener calls the 'onTeamNumberFormChange' method upon changes to the team number text field
        teamNumberTextField.addKeyListener(new TeamNumberKeyChangeListener(teamNumberTextField, this));
    }
    
    
    @Override // Called by the TeamNumberKeyChangeListener
    public void onTeamNumberFormChange(@NotNull String text, boolean isValidTeamNumber, @NotNull String previousText)
    {
        updateTeamNumberWarningVisibility(isValidTeamNumber);
    }
    
    private void updateTeamNumberWarningVisibility(boolean isValidTeamNumber)
    {
        try
        {
            teamNumberWarningIconLabel.setVisible(!isValidTeamNumber);
        }
        catch (Exception e)
        {
            teamNumberWarningIconLabel.setVisible(false);         
            LOG.warn("[FRC] could not update team number warning icon visibility due to an exception: " + e.toString());
        }
    }
    
   
    
    @Override
    public void onStepLeaving()
    {
        LOG.trace("[FRC] Entering FrcProjectSettingsWizardStep.onStepLeaving()");
        // TODO what other work needs to be done here?
        saveSettings();
        LOG.trace("[FRC] Exiting FrcProjectSettingsWizardStep.onStepLeaving()");
        
    }
    
    
    private void loadSettings()
    {
        // TODO: might be nice to load "primary" team number, or last used team number
        
    }
    
    
    private void saveSettings()
    {
        
    }
    
    
    private static boolean getSavedValue(String key, boolean defaultValue)
    {
        return getSavedValue(key, String.valueOf(defaultValue)).equals(String.valueOf(true));
    }
    
    
    private static String getSavedValue(String key, String defaultValue)
    {
        String value = PropertiesComponent.getInstance().getValue(key);
        return value == null ? defaultValue : value;
    }
    
    
    private static void saveValue(String key, boolean value)
    {
        saveValue(key, String.valueOf(value));
    }
    
    
    private static void saveValue(String key, String value)
    {
        PropertiesComponent.getInstance().setValue(key, value);
    }
    
    
    @Override
    public JComponent getPreferredFocusedComponent()
    {
        return teamNumberTextField;
    }
    
    
    @Override
    public JComponent getComponent()
    {
        // return new JLabel("A placeholder. New Project Form will go here :)");
        return rootPanel;
    }
    
    
    @Override
    public boolean validate() throws ConfigurationException
    {
        LOG.trace("[FRC] Entering FrcProjectSettingsWizardStep.validate()");
        if (!FrcTeamNumberKt.isValidTeamNumber(teamNumberTextField.getText()))
        {
            ApplicationManager.getApplication().invokeLater(
                    () -> IdeFocusManager.getInstance(myProjectOrNull).requestFocus(teamNumberTextField, true));
            throw new ConfigurationException(message("frc.ui.wizard.mws.validate.teamNumberRequired.message"),
                                             message("frc.ui.wizard.mws.validate.teamNumberRequired.title"));
        }
        else if (!FrcApplicationSettings.getInstance().isTeamNumberConfigured())
        {
            int projectTeamNumber = 0;
            try
            {
                projectTeamNumber = Integer.parseInt(teamNumberTextField.getText());
            }
            catch (NumberFormatException e)
            {
                LOG.warn("[FRC] Could not parse configured team number of '" + teamNumberTextField.getText() + "' despite it having just passed validation." );
            }
            final TeamNumberDialogWrapper dialogWrapper = new TeamNumberDialogWrapper(rootPanel, projectTeamNumber);
            final boolean ok = dialogWrapper.showAndGet();
            if (ok)
            {
                FrcApplicationSettings.getInstance().setTeamNumber(dialogWrapper.getTeamNumberForAppSettings());
            }
        }
        
        //TODO get required minimum Java level from selected template - and perhaps change the validation message
        FrcJavaLangUtilsKt.validateMinimumJavaVersion(myContext,
                                                      11,
                                                      FrcMessageKey.of("frc.ui.wizard.validate.minJavaVersion.additionalMessage.goBack"));
        
        
        final String basePackageName = basePackageTextField.getText().trim();
        if (StringUtils.isBlank(basePackageName))
        {
            throw new ConfigurationException(message("frc.ui.wizard.projectSettingsStep.validate.blankPackageName.message"),
                                             message("frc.ui.wizard.projectSettingsStep.validate.blankPackageName.title"));
        }
        else if (!isValidPackageName(basePackageName))
        {
            throw new ConfigurationException(message("frc.ui.wizard.projectSettingsStep.validate.invalidPackageName.message", basePackageName),
                                             message("frc.ui.wizard.projectSettingsStep.validate.invalidPackageName.title"));
        }
        LOG.trace("[FRC] Exiting FrcProjectSettingsWizardStep.validate() (Gracefully with no validation errors)");
        return true;
    }
    
    
    protected boolean isValidPackageName(@Nullable String basePackageName)
    {
        if (StringUtils.isBlank(basePackageName))
        {
            return false;
        }
        
        return new FrcPsiNameHelper(determineJavaVersion()).isQualifiedName(basePackageName);
    }
    
    
    
    
    
    private JavaVersion determineJavaVersion()
    {
        @Nullable
        Sdk sdk = myContext.getProjectJdk();
        if (sdk == null)
        {
            sdk = myBuilder.getSelectedSdk(); // may still be null
        }
        
        // For the most part, the language level is not too critical for validating a valid package name has been entered.
        // So we set a default level in the event we can not set it more explicitly
        JavaVersion javaVersion = FrcPluginGlobals.DEFAULT_MIN_REQUIRED_JAVA_VERSION;
        if (sdk instanceof ProjectJdkImpl)
        {
            ProjectJdkImpl jdk = (ProjectJdkImpl) sdk;
            JavaVersion parsedJavaVersion = JavaVersion.tryParse(jdk.getVersionString());
            if (parsedJavaVersion != null)
            {
                javaVersion = parsedJavaVersion;
            }
        }
        return javaVersion;
    }
    
    
    
    
    
    @Override
    public void updateStep()
    {
        LOG.trace("[FRC] Entering FrcProjectSettingsWizardStep.updateStep()");
//        ProjectData parentProject = myParentProjectForm.getParentProject();
//        ProjectId projectId = myBuilder.getProjectId();
        
        UiUtilsKt.setTextIfEmpty(teamNumberTextField, Integer.toString(myBuilder.getDataModel().getTeamNumber()));
        
        updateComponents();
        LOG.trace("[FRC] Exiting FrcProjectSettingsWizardStep.updateStep()");
    }
    
    
    /** Commits data from UI into ModuleBuilder and WizardContext */
    @Override
    public void updateDataModel()
    {
        LOG.trace("[FRC] Entering FrcProjectSettingsWizardStep.updateDataModel()");
        myContext.setProjectBuilder(myBuilder);
        ProjectData parentProject = myParentProjectForm.getParentProject();
        
        final FrcProjectWizardData dataModel = myBuilder.getDataModel();
        myBuilder.setParentProject(parentProject);
        final String configuredTeamNum = teamNumberTextField.getText().trim();
        
        dataModel.setTeamNumber(Integer.parseInt(configuredTeamNum));
        
        dataModel.setBasePackage(basePackageTextField.getText().trim());
        
        dataModel.setIncludeVsCodeConfigs(includeVsCodeConfigsCheckBox.isSelected());
        
        myBuilder.setProjectId(new ProjectId("frc.team" + configuredTeamNum,
                                             "robot-" + configuredTeamNum,
                                             dataModel.getFrcYear() + ".0"));
        
        
        if (myBuilder.getProjectId() != null && StringUtils.isNotEmpty(myBuilder.getProjectId().getArtifactId()))
        {
            myContext.setProjectName(myBuilder.getProjectId().getArtifactId());
        }
        
        if (parentProject != null)
        {
            myContext.setProjectFileDirectory(parentProject.getLinkedExternalProjectPath() + '/' + myContext.getProjectName());
        }
        else
        {
            if (myProjectOrNull != null)
            {
                myContext.setProjectFileDirectory(myProjectOrNull.getBasePath() + '/' + myContext.getProjectName());
            }
        }
        LOG.trace("[FRC] Exiting FrcProjectSettingsWizardStep.updateDataModel()");
    }
    
    
    private void updateComponents()
    {
        LOG.trace("[FRC] Entering FrcProjectSettingsWizardStep.updateComponents()");
        final boolean isAddToVisible = myParentProjectForm.isVisible();
        
        myParentProjectForm.updateComponents();
        LOG.trace("[FRC] Exiting FrcProjectSettingsWizardStep.updateComponents()");
    }
    
    
    @Override
    public Icon getIcon()
    {
        return FRC.FIRST_ICON_WIZARD_PANELS;
    }
    
    
    @Override
    public String getHelpId()
    {
        return null;
        //return "FRC_Wizard_Dialog";
    }
    
    
    @Override
    public void disposeUIResources()
    {
        Disposer.dispose(myParentProjectForm);
    }
    
    
}
