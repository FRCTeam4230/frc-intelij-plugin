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
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.lang.JavaVersion;

import net.javaru.iip.frc.FrcIcons.FRC;
import net.javaru.iip.frc.FrcPluginGlobals;
import net.javaru.iip.frc.i18n.FrcMessageKey;
import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.util.FrcUtilsKt;
import net.javaru.iip.frc.util.UiUtilsKt;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public class FrcProjectSettingsWizardStep extends ModuleWizardStep
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
    private JPanel teamInformationPanel;
    private JBTextField teamNumberTextField;
    private JPanel packageInformationPanel;
    private JBLabel basePackageLabel;
    private JBTextField basePackageTextField;
    
    
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
        
        // We may need to update this when the team number changed from an application setting to a project setting
        UiUtilsKt.setTextIfEmpty(teamNumberTextField, Integer.toString(myBuilder.getDataModel().getTeamNumber()));
        UiUtilsKt.setTextIfEmpty(basePackageTextField, myBuilder.getDataModel().getBasePackage());
        
        LOG.trace("[FRC] Entering FrcProjectSettingsWizardStep.initComponents()");
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
        if (!FrcApplicationSettings.Settings.isValidTeamNumber(teamNumberTextField.getText()))
        {
            ApplicationManager.getApplication().invokeLater(
                    () -> IdeFocusManager.getInstance(myProjectOrNull).requestFocus(teamNumberTextField, true));
            throw new ConfigurationException(message("frc.ui.wizard.mws.validate.teamNumberRequired.message"),
                                             message("frc.ui.wizard.mws.validate.teamNumberRequired.title"));
        }
        
        //TODO get required minimum Java level from selected template - and perhaps change the validation message
        FrcUtilsKt.validateMinimumJavaVersion(myBuilder,
                                              myContext,
                                              11,
                                              FrcMessageKey.of("frc.ui.wizard.validate.minJavaVersion.additionalMessage.goBack"));
        
        final String basePackageName = basePackageTextField.getText().trim();
    
        if (StringUtils.isBlank(basePackageName))
        {
            final int answer = Messages.showYesNoDialog(
                    getComponent(),
                    message("frc.ui.wizard.projectSettingsStep.validate.emptyPackageName.message"),
                    message("frc.ui.wizard.projectSettingsStep.validate.emptyPackageName.title"),
                    Messages.getWarningIcon());
            if (answer == Messages.YES)
            {
                return false;
            }
        }
        else
        {
            final Sdk sdk = myContext.getProjectJdk();
            // For the most part, the language level is not too critical for validating a valid package name has been entered.
            // So we set a default level in the event we can not set it more explicitly
            JavaVersion javaVersion = FrcPluginGlobals.DEFAULT_JAVA_VERSION;
            if (sdk instanceof ProjectJdkImpl)
            {
                ProjectJdkImpl jdk = (ProjectJdkImpl) sdk;
                JavaVersion parsedJavaVersion = JavaVersion.tryParse(jdk.getVersionString());
                if (parsedJavaVersion != null)
                {
                    javaVersion = parsedJavaVersion;
                }
            }
            final boolean isValidPackageName = FrcUtilsKt.isValidPackageName(basePackageName, javaVersion);
            if (!isValidPackageName)
            {
                throw new ConfigurationException(message("frc.ui.wizard.projectSettingsStep.validate.invalidPackageName.message", basePackageName),
                                                 message("frc.ui.wizard.projectSettingsStep.validate.invalidPackageName.title"));
            }
        }
    
    
        LOG.trace("[FRC] Exiting FrcProjectSettingsWizardStep.validate() (Gracefully with no validation errors)");
        return true;
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
        LOG.trace("[FRC] Entering FrcModuleWizardStep.updateComponents()");
        final boolean isAddToVisible = myParentProjectForm.isVisible();
        
        myParentProjectForm.updateComponents();
        LOG.trace("[FRC] Exiting FrcModuleWizardStep.updateComponents()");
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
