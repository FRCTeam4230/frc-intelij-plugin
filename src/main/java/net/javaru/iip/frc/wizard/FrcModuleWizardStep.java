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

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;

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
import com.intellij.ui.components.JBList;
import com.intellij.util.lang.JavaVersion;

import net.javaru.iip.frc.FrcIcons.FRC;
import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.util.UiUtilsKt;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



// This is the content that gets displayed ion the initial new project dialog when the "FRC Robot Project" node is selected. 
// It typically lists the frameworks to include. For example for the Java module it lists kotlin, groovy in IJ community as well as Thymeleaf, Ruby, etc. in IJ ultimate
// it is returned by the    FrcModuleBuilder.getCustomOptionsStep(WizardContext context, Disposable parentDisposable)    method
public class FrcModuleWizardStep extends ModuleWizardStep
{
    private static final Logger LOG = Logger.getInstance(FrcModuleWizardStep.class);
    
    private static final String PROJECTS_CARD_NAME = "projectTemplatesListCard"; // must march the value entered in the form (in the UI Designer)
    private static final String EXAMPLES_CARD_NAME = "exampleTemplatesListCard"; // must march the value entered in the form (in the UI Designer)
    
    @NotNull
    private final FrcModuleBuilder myBuilder;
    @NotNull
    private final WizardContext myContext;
    
    @Nullable
    private final Project myProjectOrNull;
    
    @NotNull
    private final FrcParentProjectForm myParentProjectForm;
    private JPanel myMainPanel;
    private JPanel myAddToPanel;
    private JTextField teamNumberTextField;
    private JPanel robotTemplatePanel;
    private String activeCard = "";
    private JBList<FrcWizardTemplateDefinition> projectTemplatesJBList;
    private JBList<FrcWizardTemplateDefinition> exampleTemplatesJBList;
    private JBLabel templateDescriptionLabel;
    private ButtonGroup templatesTypeButtonGroup;
    private JRadioButton projectTemplatesRadioButton;
    private JRadioButton exampleTemplatesRadioButton;
    private JBLabel projectTemplatesRadioButtonGroupLabel;
    
    
    public FrcModuleWizardStep(@NotNull FrcModuleBuilder builder, @NotNull WizardContext context)
    {
        LOG.trace("[FRC] FrcModuleWizardStep constructor has been called.");
        this.myBuilder = builder;
        this.myContext = context;
        this.myProjectOrNull = context.getProject(); 
        myParentProjectForm = new FrcParentProjectForm(context, parentProject -> updateComponents());
        initComponents();
        loadSettings();
    }
    
    
    private void initComponents()
    {
        LOG.trace("[FRC] FrcModuleWizardStep.initComponents() has been called.");
        myAddToPanel.add(myParentProjectForm.getComponent());
        ActionListener updatingListener = e -> updateComponents();
        // TODO add the Action Listener to any components that need to take action upon updating
    
        // We may need to update this when the team number changed from an application setting to a project setting
        UiUtilsKt.setTextIfEmpty(teamNumberTextField, Integer.toString(FrcApplicationSettings.Settings.INSTANCE().getTeamNumber()));
        initRadioButtonGroup();
        initTemplatesLists();
    }
    
    
    @Override
    public void onStepLeaving()
    {
        LOG.trace("[FRC] FrcModuleWizardStep.onStepLeaving() has been called.");
        // TODO what other work needs to be done here?
        saveSettings();
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
        return myMainPanel;
    }
    
    
    @Override
    public boolean validate() throws ConfigurationException
    {
        LOG.trace("[FRC] FrcModuleWizardStep.validate() has been called.");
        if (!FrcApplicationSettings.Settings.isValidTeamNumber(teamNumberTextField.getText()))
        {
            ApplicationManager.getApplication().invokeLater(
                    () -> IdeFocusManager.getInstance(myProjectOrNull).requestFocus(teamNumberTextField, true));
            throw new ConfigurationException(message("frc.ui.wizard.mws.validate.teamNumberRequired.message"),
                                             message("frc.ui.wizard.mws.validate.teamNumberRequired.title"));
        }
        
        final Sdk sdk = myContext.getProjectJdk();
        if (sdk instanceof ProjectJdkImpl)
        {
            try
            {
               
                final ProjectJdkImpl jdk = (ProjectJdkImpl) sdk;
                final String jdkVersionString = jdk.getVersionString();
                final JavaVersion javaVersion = JavaVersion.tryParse(jdkVersionString);
                //final LanguageLevel languageLevel = LanguageLevel.parse(jdkVersionString);
    
                
                // There is also a JavaVersion in the Gradle API code: org.gradle.api.JavaVersion;
                
                //TODO get required minimum Java level from selected template of build year
                final JavaVersion requiredMinimumJavaVersion = JavaVersion.compose(11);
                
                
                if (javaVersion != null && !javaVersion.isAtLeast(requiredMinimumJavaVersion.feature))
                {
                    final String message = message("frc.ui.wizard.mws.validate.minJavaVersion.message",
                                                   requiredMinimumJavaVersion,
                                                   requiredMinimumJavaVersion.feature, 
                                                   javaVersion,
                                                   javaVersion.feature);
                    final String title = message("frc.ui.wizard.mws.validate.minJavaVersion.title");
                    throw new ConfigurationException(message, title);
                }
            }
            catch (Exception e)
            {
                if (e instanceof ConfigurationException) { throw e; }
                LOG.warn("Could not validate minimum JDK version due to the exception: " + e.toString(), e);
            }
        }
    
        return true;
    }
    
    
    @Override
    public void updateStep()
    {
        LOG.trace("[FRC] FrcModuleWizardStep.updateStep() has been called.");
//        ProjectData parentProject = myParentProjectForm.getParentProject();
//        ProjectId projectId = myBuilder.getProjectId();
    
        UiUtilsKt.setTextIfEmpty(teamNumberTextField, Integer.toString(myBuilder.getDataModel().getTeamNumber()));
        
        updateComponents();
    }
    
    
    /** Commits data from UI into ModuleBuilder and WizardContext */
    @Override
    public void updateDataModel()
    {
        LOG.trace("[FRC] FrcModuleWizardStep.updateDataModel() has been called.");
        myContext.setProjectBuilder(myBuilder);
        ProjectData parentProject = myParentProjectForm.getParentProject();
        myBuilder.setParentProject(parentProject);
        final String configuredTeamNum = teamNumberTextField.getText();
        myBuilder.getDataModel().setTeamNumber(Integer.parseInt(configuredTeamNum));
        
        myBuilder.setProjectId(new ProjectId("frc.team" + configuredTeamNum,
                                             "robot-" + configuredTeamNum,
                                             myBuilder.getDataModel().getFrcYear() + ".0"));
    
        
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
    }
    
    
    private void updateComponents()
    {
        LOG.trace("[FRC] FrcModuleWizardStep.updateComponents() has been called.");
        final boolean isAddToVisible = myParentProjectForm.isVisible();
    
        myParentProjectForm.updateComponents();
    }
    
    
    @Override
    public Icon getIcon()
    {
        return FRC.FIRST_ICON_WIZARD_PANELS;
    }
    
 
    
//    @Override
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
    
    private void initRadioButtonGroup()
    {
        ItemListener templatesButtonGroupChangeListener = e -> {
            final AbstractButton button = (AbstractButton) e.getSource();
            final ButtonModel model = button.getModel();
            final String actionCommand = model.getActionCommand();
            final CardLayout cards = (CardLayout) robotTemplatePanel.getLayout();
            if (actionCommand.equals(FrcWizardProjectTemplateDefinition.class.getSimpleName()))
            {
                showCard( PROJECTS_CARD_NAME);
            }
            else if (actionCommand.equals(FrcWizardExampleTemplateDefinition.class.getSimpleName()))
            {
                showCard(EXAMPLES_CARD_NAME);
            }
            else
            {
                LOG.warn("Unknown Radio Button actionCommand of '" + actionCommand + "' returned. Defaulting to the Project Templates.");
                showCard( PROJECTS_CARD_NAME);
                templatesTypeButtonGroup.setSelected(projectTemplatesRadioButton.getModel(), true);
            }
        };
        
        
        projectTemplatesRadioButton.setSelected(true);
        exampleTemplatesRadioButton.setSelected(false);
        
        projectTemplatesRadioButton.addItemListener(templatesButtonGroupChangeListener);
        exampleTemplatesRadioButton.addItemListener(templatesButtonGroupChangeListener);
        
        projectTemplatesRadioButton.setActionCommand(FrcWizardProjectTemplateDefinition.class.getSimpleName());
        exampleTemplatesRadioButton.setActionCommand(FrcWizardExampleTemplateDefinition.class.getSimpleName());
        
        templatesTypeButtonGroup = new ButtonGroup();
        templatesTypeButtonGroup.add(projectTemplatesRadioButton);
        templatesTypeButtonGroup.add(exampleTemplatesRadioButton);
        templatesTypeButtonGroup.setSelected(projectTemplatesRadioButton.getModel(), true);
    }
    
    
    private void initTemplatesLists()
    {
        ListSelectionListener templatesListSelectionListener = e -> {
            final Object source = e.getSource();
            if (source instanceof JBList)
            {
                @SuppressWarnings("unchecked")
                final JBList<FrcWizardTemplateDefinition> theList = (JBList<FrcWizardTemplateDefinition>) source;
                updateTemplateDescription(theList);
            }
        };
        
        final FrcWizardTemplateDefinition[] projectTemplates = FrcWizardProjectTemplateDefinition.values();
        projectTemplatesJBList.setListData(projectTemplates);
        projectTemplatesJBList.setSelectedIndex(0);
        projectTemplatesJBList.addListSelectionListener(templatesListSelectionListener);
        
        final FrcWizardTemplateDefinition[] exampleTemplates = FrcWizardExampleTemplateDefinition.values();
        exampleTemplatesJBList.setListData(exampleTemplates);
        exampleTemplatesJBList.setSelectedIndex(0);
        exampleTemplatesJBList.addListSelectionListener(templatesListSelectionListener);
    
        showCard( PROJECTS_CARD_NAME);
        
    }
    
    
    private void showCard(String cardName)
    {
        final CardLayout cardLayout = (CardLayout) robotTemplatePanel.getLayout();
        cardLayout.show(robotTemplatePanel, cardName);
        activeCard = cardName;
        if (cardName.equals(PROJECTS_CARD_NAME))
        {
            updateTemplateDescription(projectTemplatesJBList);
        }
        else if (cardName.equals(EXAMPLES_CARD_NAME))
        {
            updateTemplateDescription(exampleTemplatesJBList);
        }
        else
        {
            LOG.warn("Unknown Card name (for Card Layout). Defaulting to the Projects Card");
            cardLayout.show(robotTemplatePanel, PROJECTS_CARD_NAME);
            updateTemplateDescription(projectTemplatesJBList);
            templatesTypeButtonGroup.setSelected(projectTemplatesRadioButton.getModel(), true);
        }
    }
    
    
    protected void updateTemplateDescription(JBList<FrcWizardTemplateDefinition> theList)
    {
        final int index = theList.getLeadSelectionIndex();
        final FrcWizardTemplateDefinition templateDefinition = theList.getModel().getElementAt(index);
        templateDescriptionLabel.setText(templateDefinition.getDisplayNameAndDescription());
    }
    
    
}
