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

import javax.swing.*;
import javax.swing.event.ListSelectionListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTabbedPane;

import net.javaru.iip.frc.FrcIcons.FRC;
import net.javaru.iip.frc.i18n.FrcBundle;
import net.javaru.iip.frc.i18n.FrcMessageKey;
import net.javaru.iip.frc.util.FrcJavaLangUtilsKt;
import net.javaru.iip.frc.wpilib.version.WpiLibVersion;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public class FrcTemplateSelectionWizardStep extends ModuleWizardStep
{
    private static final Logger LOG = Logger.getInstance(FrcTemplateSelectionWizardStep.class);
    public static final int PROJECTS_TAB_INDEX = 0;
    public static final int EXAMPLES_TAB_INDEX = 1;
    
    @NotNull
    private final FrcModuleBuilder myBuilder;
    @NotNull
    private final WizardContext myContext;
    @Nullable
    private final Project myProjectOrNull;
    
    @NotNull
    private final FrcParentProjectForm myParentProjectForm;
    private JPanel myRootPanel;
    private JPanel templateSelectionMainPanel;
    private JPanel templateListsOuterPanel;
    private JBLabel templateDescriptionLabel;
    private JBTabbedPane templateListsTabbedPane;
    private JPanel projectTemplatesPane;
    private JPanel exampleTemplatesPane;
    private JBList<FrcWizardTemplateDefinition> projectTemplatesJBList;
    private JBList<FrcWizardTemplateDefinition> exampleTemplatesJBList;
    private JBLabel projectTemplatesPaneLabel;
    private JPanel wpilibVersionSelectedPanel;
    private JBLabel wpilibVersionSelectedLabel;
    
    
    public FrcTemplateSelectionWizardStep(@NotNull FrcModuleBuilder builder,
                                          @NotNull WizardContext context)
    {
        LOG.trace("[FRC] Entering FrcTemplateSelectionWizardStep constructor");
        this.myBuilder = builder;
        this.myContext = context;
        this.myProjectOrNull = context.getProject();
        myParentProjectForm = new FrcParentProjectForm(context, parentProject -> updateComponents());
        initComponents();
        loadSettings();
        LOG.trace("[FRC] Existing FrcTemplateSelectionWizardStep constructor");
    }
    
    
    private void initComponents()
    {
        LOG.trace("[FRC] Entering FrcTemplateSelectionWizardStep.initComponents()");
    
        // add the Action Listener to any components that need to take action upon updating
        // ActionListener updatingListener = e -> updateComponents();

        initTabPane();
        initTemplatesLists();
        LOG.trace("[FRC] Exiting FrcTemplateSelectionWizardStep.initComponents()");
    }
    
    
    @Override
    public void onStepLeaving()
    {
        LOG.trace("[FRC] Entering FrcTemplateSelectionWizardStep.onStepLeaving()");
        // TODO what other work needs to be done here?
        saveSettings();
        LOG.trace("[FRC] Exiting FrcTemplateSelectionWizardStep.onStepLeaving()");
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
        return templateListsTabbedPane;
    }
    
    
    @Override
    public JComponent getComponent()
    {
        // return new JLabel("A placeholder. New Project Form will go here :)");
        return myRootPanel;
    }
    
    
    @Override
    public boolean validate() throws ConfigurationException
    {
        LOG.trace("[FRC] Entering FrcTemplateSelectionWizardStep.validate()");
        //TODO get required minimum Java level from selected template, and if none there, then from the 
        //     JAVA_VERSION key in C:\Users\Public\frc${frcYear}\jdk\release 
        FrcJavaLangUtilsKt.validateMinimumJavaVersion(myContext,
                                                      11,
                                                      FrcMessageKey.of("frc.ui.wizard.validate.minJavaVersion.additionalMessage.goBack"));
    
    
        final FrcWizardTemplateDefinition selectedTemplate = determineSelectedTemplate();
        if(selectedTemplate.isDeprecated())
        {
            final int answer = Messages.showYesNoDialog(
                    getComponent(),
                    message("frc.ui.wizard.templateSelectionStep.validate.deprecatedTemplate.message", selectedTemplate.getDisplayName()),
                    message("frc.ui.wizard.templateSelectionStep.validate.deprecatedTemplate.title"),
                    Messages.getWarningIcon());
            if (answer == Messages.YES)
            {
                return false;
            }
        }
        
        LOG.trace("[FRC] Exiting FrcTemplateSelectionWizardStep.validate() (Gracefully with no validation errors)");
        return true;
    }
    
    
    @Override
    public void updateStep()
    {
        LOG.trace("[FRC] Entering FrcTemplateSelectionWizardStep.updateStep()");
//        ProjectData parentProject = myParentProjectForm.getParentProject();
//        ProjectId projectId = myBuilder.getProjectId();
    
//        UiUtilsKt.setTextIfEmpty(teamNumberTextField, Integer.toString(myBuilder.getDataModel().getTeamNumber()));
    
        updateComponents();
        LOG.trace("[FRC] Exiting FrcTemplateSelectionWizardStep.updateStep()");
    }
    
    
    /** Commits data from UI into ModuleBuilder and WizardContext */
    @Override
    public void updateDataModel()
    {
        LOG.trace("[FRC] Entering FrcTemplateSelectionWizardStep.updateDataModel()");
        final FrcProjectWizardData dataModel = myBuilder.getDataModel();
        
        myContext.setProjectBuilder(myBuilder);
        ProjectData parentProject = myParentProjectForm.getParentProject();
        myBuilder.setParentProject(parentProject);
    
        FrcWizardTemplateDefinition templateDefinition = determineSelectedTemplate();
        dataModel.setFrcWizardTemplateDefinition(templateDefinition);
        LOG.debug("[FRC] FrcWizardTemplateDefinition set to: " + templateDefinition);
        
        LOG.trace("[FRC] Exiting FrcTemplateSelectionWizardStep.updateDataModel()");
    }
    
    
    private FrcWizardTemplateDefinition determineSelectedTemplate()
    {
        final int selectedIndex = templateListsTabbedPane.getSelectedIndex();
        final JBList<FrcWizardTemplateDefinition> templatesJBList;
        if (selectedIndex == EXAMPLES_TAB_INDEX)
        {
            templatesJBList = exampleTemplatesJBList;
        }
        else
        {
            templatesJBList = projectTemplatesJBList;
        }
        return templatesJBList.getSelectedValue();
    }
    
    
    private void updateComponents()
    {
        LOG.trace("[FRC] Entering FrcTemplateSelectionWizardStep.updateComponents()");
        // final boolean isAddToVisible = myParentProjectForm.isVisible();
        updateWpiLibSelectedLabel();
        myParentProjectForm.updateComponents();
        LOG.trace("[FRC] Exiting FrcTemplateSelectionWizardStep.updateComponents()");
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
    
    
    private void updateWpiLibSelectedLabel()
    {
        final FrcProjectWizardData dataModel = myBuilder.getDataModel();
        final WpiLibVersion wpilibVersion = dataModel.getWpilibVersion();
        wpilibVersionSelectedLabel.setText(FrcBundle.message("frc.ui.wizard.templateSelectionStep.wpilibVersionSetToLabel.text", wpilibVersion));
    }
    
    private void initTabPane()
    {
        // When adding a Tab, you must update the updateTemplateDescription() method and the below count
        if (templateListsTabbedPane.getTabCount() > 2)
        {
            LOG.error("[FRC] Tab count for Templates Tabbed Pane is greater than the expected count. This is likely due to some code changes. " 
                      + "Please see the initTab() method for information on properly updating the code to add a tab.");
        }
        
        templateListsTabbedPane.setSelectedIndex(PROJECTS_TAB_INDEX);
        templateListsTabbedPane.addChangeListener(e -> updateTemplateDescription());
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
        
        final FrcWizardTemplateDefinition[] projectTemplates = FrcWizard2019ProjectTemplateDefinition.values();
        projectTemplatesJBList.setListData(projectTemplates);
        projectTemplatesJBList.setSelectedIndex(0);
        projectTemplatesJBList.addListSelectionListener(templatesListSelectionListener);
        
        final FrcWizardTemplateDefinition[] exampleTemplates = FrcWizard2019ExampleTemplateDefinition.values();
        exampleTemplatesJBList.setListData(exampleTemplates);
        exampleTemplatesJBList.setSelectedIndex(0);
        exampleTemplatesJBList.addListSelectionListener(templatesListSelectionListener);
    
        templateListsTabbedPane.setSelectedIndex(PROJECTS_TAB_INDEX);
        updateTemplateDescription();
    }

    
    protected void updateTemplateDescription()
    {
        //templateListsTabbedPane.getSelectedComponent();
        final int selectedIndex = templateListsTabbedPane.getSelectedIndex();
        switch (selectedIndex)
        {
            case PROJECTS_TAB_INDEX:
                updateTemplateDescription(projectTemplatesJBList);
                break;
            case EXAMPLES_TAB_INDEX:
                updateTemplateDescription(exampleTemplatesJBList);
                break;
            default:
                LOG.warn("[FRC] Unknown selected tab index of " + selectedIndex + ". No action can be taken to update the template description");
        }
    }
    
    protected void updateTemplateDescription(JBList<FrcWizardTemplateDefinition> theList)
    {
        final int index = theList.getLeadSelectionIndex();
        final FrcWizardTemplateDefinition templateDefinition = theList.getModel().getElementAt(index);
        templateDescriptionLabel.setText(templateDefinition.getDisplayNameAndDescription());
    }
    
}
