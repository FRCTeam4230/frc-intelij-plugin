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

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import javax.swing.*;

import org.apache.commons.lang3.BooleanUtils;
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
import com.intellij.ui.ContextHelpLabel;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.lang.JavaVersion;

import icons.FrcIcons.FRC;
import kotlin.Unit;
import net.javaru.iip.frc.FrcPluginGlobals;
import net.javaru.iip.frc.i18n.FrcMessageKey;
import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.settings.FrcTeamNumberKt;
import net.javaru.iip.frc.settings.TeamNumberFormChangeListener;
import net.javaru.iip.frc.settings.TeamNumberKeyChangeListener;
import net.javaru.iip.frc.ui.ButtonAndLabelSynchronizer;
import net.javaru.iip.frc.util.FrcJavaLangUtilsKt;
import net.javaru.iip.frc.util.FrcPsiNameHelper;
import net.javaru.iip.frc.util.FrcUiUtilsKt;
import net.javaru.iip.frc.wizard.FrcProjectWizardData.GradleDslOption;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



@SuppressWarnings({"FieldCanBeLocal", "unused" })
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
    private JPanel teamEntryNumberPanel;
    private JBLabel teamNumberLabel;
    private JBTextField teamNumberTextField;
    private JBLabel teamNumberWarningIconLabel;
    private JPanel packageEntryPanel;
    private JBLabel basePackageLabel;
    private JBTextField basePackageTextField;
    private JButton basePackageDefaultButton;
    private JBLabel basePackageWarningLabel;
    private JPanel generalSettingsPanel;
    private JPanel generalOptionsPanel;
    private JPanel desktopSupportPanel;
    private JBCheckBox enableDesktopSupportCheckBox;
    private JBLabel enableDesktopSupportCheckBoxLabel;
    private JPanel vsCodeOptionsPanel;
    private ContextHelpLabel desktopSupportContextHelpLabel;
    private JBLabel enableDesktopSupportWarningMessage;
    private JBCheckBox includeVsCodeConfigsCheckBox;
    private JBLabel includeVsCodeConfigsCheckBoxLabel;
    private ContextHelpLabel includeVsCodeContextHelpLabel;
    private JPanel gitPanel;
    private JPanel gitIgnorePanel;
    private JBCheckBox includeGitignoreFileCheckBox;
    private JBLabel includeGitignoreFileCheckBoxLabel;
    private JButton configureGitignoreButton;
    private JPanel includeGitIgnoreFileOptionPanel;
    private JPanel includeVsCodeConfigsOptionPanel;
    private JPanel enableDesktopSupportOptionPanel;
    private JPanel junitPanel;
    private JPanel includeJunitSupportOptionPanel;
    private JBCheckBox includeJunitSupportCheckBox;
    private JBLabel includeJunitSupportCheckBoxLabel;
    private JPanel gradlePanel;
    private JPanel gradleDslPanel;
    private JBLabel gradleDslLabel;
    private JBRadioButton groovyDslRadioButton;
    private JBLabel groovyDslRadioButtonLabel;
    private JBRadioButton kotlinDslRadioButton;
    private JBLabel kotlinDslRadioButtonLabel;
    private JPanel groovyDslSupportOptionPanel;
    private JPanel kotlinDslSupportOptionPanel;
    private JPanel kotlinOptionOutterPanel;
    private JPanel includeKotlinSupportOptionPanel;
    private JBCheckBox includeKotlinSupportCheckBox;
    private JBLabel includeKotlinSupportCheckBoxLabel;
    private ContextHelpLabel includeKotlinSupportContextHelpLabel;
    private JBLabel kotlinRequiredForTemplateLabel;
    
    
    private ButtonAndLabelSynchronizer<JBCheckBox> enableDesktopSupportOption;
    private ButtonAndLabelSynchronizer<JBCheckBox> includeVsCodeConfigsOption;
    private ButtonAndLabelSynchronizer<JBCheckBox> includeGitIgnoreFileOption;
    private ButtonAndLabelSynchronizer<JBCheckBox> includeJunitSupportOption;
    private ButtonAndLabelSynchronizer<JBCheckBox> includeKotlinSupportOption;
    private ButtonAndLabelSynchronizer<JBRadioButton> groovyDslSupportOption;
    private ButtonAndLabelSynchronizer<JBRadioButton> kotlinDslSupportOption;
    
    private boolean userHasModifiedDesktopSupport = false;
    
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
        initOptionSynchronizers();
    
        // TODO add this Action Listener to any components that need to take action upon updating
        ActionListener updatingListener = e -> updateComponents();
       
        initTeamNumberField();
        final FrcProjectWizardData dataModel = myBuilder.getDataModel();
        
        FrcUiUtilsKt.setTextIfEmpty(basePackageTextField, dataModel.getBasePackage());
        basePackageWarningLabel.setVisible(false);
        FrcUiUtilsKt.addTextChangedListener(basePackageTextField, text -> {
            basePackageWarningLabel.setVisible(StringUtils.isBlank(text));
            return Unit.INSTANCE;
        });
        basePackageDefaultButton.addActionListener(e -> basePackageTextField.setText(FrcProjectWizardDataKt.DEFAULT_BASE_PACKAGE));
        includeVsCodeConfigsCheckBox.setSelected(dataModel.getIncludeVsCodeConfigs());
        enableDesktopSupportCheckBox.setSelected(dataModel.getEnableDesktopSupport());
        includeKotlinSupportCheckBox.setSelected(dataModel.getIncludeKotlinSupport());
        updateEnableDesktopSupportOptions();
        updateDesktopSupportWarningVisibility();
        // We use an ActionListener and not a ChangeListener as we only want to catch a user initiated change. ChangeListener is fired is we programmatically change the value
        enableDesktopSupportCheckBox.addActionListener(e -> userHasModifiedDesktopSupport = true);
        enableDesktopSupportCheckBox.addChangeListener(e -> updateDesktopSupportWarningVisibility());
        
        includeGitignoreFileCheckBox.setSelected(dataModel.getGitIgnoreConfiguration().getIncludeGitIgnoreFile());
        includeGitignoreFileCheckBox.addActionListener(e -> dataModel.getGitIgnoreConfiguration().setIncludeGitIgnoreFile(includeGitignoreFileCheckBox.isSelected()));
        configureGitignoreButton.setEnabled(dataModel.getGitIgnoreConfiguration().getIncludeGitIgnoreFile());
        includeGitignoreFileCheckBox.addChangeListener(e -> configureGitignoreButton.setEnabled(includeGitignoreFileCheckBox.isSelected()));
        configureGitignoreButton.addActionListener(e -> displayGitIgnoreConfigurationDialog());
        
        includeJunitSupportCheckBox.setSelected(dataModel.getIncludeJUnitSupport());
    
        includeJunitSupportCheckBox.addChangeListener(e -> {
            dataModel.setIncludeJUnitSupport(includeJunitSupportCheckBox.isSelected());
        });
    
    
        groovyDslRadioButton.setActionCommand(GradleDslOption.GroovyDSL.name());
        kotlinDslRadioButton.setActionCommand(GradleDslOption.KotlinDSL.name());
        
        switch (dataModel.getGradleDslOption())
        {
            case GroovyDSL:
                groovyDslRadioButton.setSelected(true);
                break;
            case KotlinDSL:
                kotlinDslRadioButton.setSelected(true);
                break;
        }
    
        ItemListener gradleDslOptionChangeListener = e -> {
            final AbstractButton button = (AbstractButton) e.getSource();
            final ButtonModel model = button.getModel();
            final String actionCommand = model.getActionCommand();
            dataModel.setGradleDslOption(GradleDslOption.valueOf(actionCommand));
        };
        groovyDslRadioButton.addItemListener(gradleDslOptionChangeListener);
        kotlinDslRadioButton.addItemListener(gradleDslOptionChangeListener);
    
    
        includeKotlinSupportCheckBox.addChangeListener(e -> {
            final boolean isKotlinTemplate = dataModel.getTemplateLanguageOption() == TemplateLanguageOption.Kotlin;
            if (isKotlinTemplate && !includeKotlinSupportCheckBox.isSelected()) {
                includeKotlinSupportCheckBox.setSelected(true);
            }
        });
        
        
        // REMOVE ONCE NO LONGER IN DEVELOPMENT/EXPERIMENTAL
        gradlePanel.setVisible(BooleanUtils.toBoolean(System.getProperty("frc.experimental.gradleDslSelection", "false")));
        
        LOG.trace("[FRC] Entering FrcProjectSettingsWizardStep.initComponents()");
    }
    
    
    
    
    
    private void initOptionSynchronizers()
    {
        enableDesktopSupportOption = new ButtonAndLabelSynchronizer<>(enableDesktopSupportOptionPanel, enableDesktopSupportCheckBox, enableDesktopSupportCheckBoxLabel);
        includeVsCodeConfigsOption = new ButtonAndLabelSynchronizer<>(includeVsCodeConfigsOptionPanel, includeVsCodeConfigsCheckBox, includeVsCodeConfigsCheckBoxLabel);
        includeGitIgnoreFileOption = new ButtonAndLabelSynchronizer<>(includeGitIgnoreFileOptionPanel, includeGitignoreFileCheckBox, includeGitignoreFileCheckBoxLabel);
        includeJunitSupportOption = new ButtonAndLabelSynchronizer<>(includeJunitSupportOptionPanel, includeJunitSupportCheckBox, includeJunitSupportCheckBoxLabel);
        includeKotlinSupportOption = new ButtonAndLabelSynchronizer<>(includeKotlinSupportOptionPanel, includeKotlinSupportCheckBox, includeKotlinSupportCheckBoxLabel);
        groovyDslSupportOption = new ButtonAndLabelSynchronizer<>(groovyDslSupportOptionPanel, groovyDslRadioButton, groovyDslRadioButtonLabel);
        kotlinDslSupportOption = new ButtonAndLabelSynchronizer<>(kotlinDslSupportOptionPanel, kotlinDslRadioButton, kotlinDslRadioButtonLabel);
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
        FrcUiUtilsKt.setTextIfEmpty(teamNumberTextField, myBuilder.getDataModel().getTeamNumberAsStringOrEmptyString());
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
        if ("0".equals(teamNumberTextField.getText()))
        {
            teamNumberTextField.setText("");
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
        if (!FrcTeamNumberKt.isValidTeamNumber(teamNumberTextField.getText()))
        {
            return teamNumberTextField;
        }
        else
        {
            return basePackageTextField;
        }
    }
    
    
    @Override
    public JComponent getComponent()
    {
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
                LOG.warn("[FRC] Could not parse configured team number of '" + teamNumberTextField.getText() + "' despite it having just passed validation.");
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
        // Called whenever the step is shown. Good for updating things that may change by the user going back and them coming forward again
        LOG.trace("[FRC] Entering FrcProjectSettingsWizardStep.updateStep()");
//        ProjectData parentProject = myParentProjectForm.getParentProject();
//        ProjectId projectId = myBuilder.getProjectId();
        
        FrcUiUtilsKt.setTextIfEmpty(teamNumberTextField, myBuilder.getDataModel().getTeamNumberAsStringOrEmptyString());
        updateIncludeKotlinSupportOptionVisibility();
        
        updateComponents();
        LOG.trace("[FRC] Exiting FrcProjectSettingsWizardStep.updateStep()");
    }
    
    
    private void updateIncludeKotlinSupportOptionVisibility()
    {
        final FrcProjectWizardData dataModel = myBuilder.getDataModel();
        final boolean isKotlinTemplate = dataModel.getTemplateLanguageOption() == TemplateLanguageOption.Kotlin;
        kotlinOptionOutterPanel.setVisible(dataModel.getFrcYear() >= 2022);
        // TODO need to make it so option goes "back" to user's previous selection
        includeKotlinSupportCheckBox.setSelected(includeKotlinSupportCheckBox.isSelected() || isKotlinTemplate);
        // We have a change listener that does not allow the option to de deselected if it is a Kotlin Template
        // For now we'll leave the option enabled as it looks strange to be disabled
        // Ideally we need to enhance the ButtonAndLabelSynchronizer to allow for disabling the checkbox only
        //includeKotlinSupportCheckBox.setEnabled(!isKotlinTemplate);
        kotlinRequiredForTemplateLabel.setVisible(isKotlinTemplate);
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
        dataModel.setEnableDesktopSupport(enableDesktopSupportCheckBox.isSelected());
        dataModel.setIncludeVsCodeConfigs(includeVsCodeConfigsCheckBox.isSelected());
        dataModel.setIncludeKotlinSupport(includeKotlinSupportCheckBox.isSelected());
        
        myBuilder.setProjectId(new ProjectId("frc.team" + configuredTeamNum,
                                             "robot-" + configuredTeamNum,
                                             dataModel.getFrcYearString() + ".0"));
        
        
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
        //final FrcProjectWizardData dataModel = myBuilder.getDataModel();
        updateEnableDesktopSupportOptions();
        updateDesktopSupportWarningVisibility();
        myParentProjectForm.updateComponents();
        LOG.trace("[FRC] Exiting FrcProjectSettingsWizardStep.updateComponents()");
    }
    
    
    private void updateEnableDesktopSupportOptions()
    {
        final FrcProjectWizardData dataModel = myBuilder.getDataModel();
        
        enableDesktopSupportCheckBox.setVisible(dataModel.getWpilibVersion().getFrcYear() >= 2021);
        if (!userHasModifiedDesktopSupport)
        {
            enableDesktopSupportCheckBox.setSelected(dataModel.isRomiTemplate());
        }
    }
    
    
    private void updateDesktopSupportWarningVisibility()
    {
        enableDesktopSupportWarningMessage.setVisible(myBuilder.getDataModel().isRomiTemplate() && !enableDesktopSupportCheckBox.isSelected());
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
    
    private void createUIComponents()
    {
        // NOTE: when creating text for ContextHelpLabels: you need to have some additional tags inside the
        //       <html> tags or the text does not wrap on the popup, and instead you get just one super long
        //        box. As a trick, just wrap an period or comma in <em> tags
        
        // When titles are added, the main content shows in a lighter colored font. Since the titles aren't really needed, we are commenting them out… at least for now
        desktopSupportContextHelpLabel = ContextHelpLabel.create(
           /* message("frc.ui.wizard.projectSettingsStep.enableDesktopSupport.contextHelpLabel.title"),*/
            message("frc.ui.wizard.projectSettingsStep.enableDesktopSupport.contextHelpLabel.text"));
        
        includeVsCodeContextHelpLabel = ContextHelpLabel.create(
            /*message("frc.ui.wizard.projectSettingsStep.includeVsCodeConfigs.contextHelpLabel.title"),*/
            message("frc.ui.wizard.projectSettingsStep.includeVsCodeConfigs.contextHelpLabel.text"));
        
        includeKotlinSupportContextHelpLabel = ContextHelpLabel.create(
        message("frc.ui.wizard.projectSettingsStep.includeKotlin.contextHelpLabel.text", message("frc.new.project.wizard.kotlin.disclaimer")));
    }
}
