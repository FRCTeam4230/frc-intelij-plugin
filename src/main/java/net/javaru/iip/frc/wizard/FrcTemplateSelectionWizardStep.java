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

import java.awt.*;
import java.awt.event.ItemListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.esotericsoftware.minlog.Log;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.ContextHelpLabel;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTabbedPane;

import icons.FrcIcons.FRC;
import net.javaru.iip.frc.i18n.FrcBundle;
import net.javaru.iip.frc.i18n.FrcMessageKey;
import net.javaru.iip.frc.ui.ButtonAndLabelSynchronizer;
import net.javaru.iip.frc.util.FrcJavaLangUtilsKt;
import net.javaru.iip.frc.util.FrcSystemConfigs.FeatureFlagKotlinTemplates;
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
    private JBLabel projectTemplatesPaneLabel;
    private JPanel wpilibVersionSelectedPanel;
    private JBLabel wpilibVersionSelectedLabel;
    private JScrollPane projectTemplatesScrollPane;
    private JBList<FrcWizardTemplateDefinition> projectTemplatesJBList;
    private JScrollPane projectExamplesScrollPane;
    private JBList<FrcWizardTemplateDefinition> exampleTemplatesJBList;
    /**
     * An outer panel that holds template options. At this time, the only template
     * option is the {@link #templateLangSelectOuterPanel}. It also holds a vertical
     * spacer, so that when the {@code templateLangSelectOuterPanel} is hidden, the
     * other content in the step does not jump/move. Generally speaking, this panel
     * should *NOT* be disabled or hidden.
     */
    private JPanel templateOptionsPanel2;
    /**
     * The outer panel for template (programming) language selection. This panel is
     * hidden or shown based on whether the experimental Kotlin templates feature
     * flag is enabled or not. It is *NOT* hidden when only one language option
     * is available. The {@link #templateLangSelectInnerPanel} is what is hidden
     * or shown based on available languages for the selected template.
     *
     *
     * <pre>
     * ┏━━━━━━━templateOptionsPanel━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
     * ┃    ┏━━━━templateLangSelectOuterPanel (1)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓┃
     * ┃    ┃    ┏━━━templateLangSelectInnerPanel (2)━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓┃┃
     * ┃    ┃    ┃   ┏━templateLangSelectContexHelpPanel━━┓┏━templateLangSelectJavaOptionPanel (3)━━━━━━━━━━━━━━━━━━━━━━┓┏━templateLangSelectKotlinOptionPanel (3)━━━━━━━━━━━━━━━━━━━━━┓       ╱(4) ┃┃┃
     * ┃    ┃    ┃   ┃                                    ┃┃                                                            ┃┃                                                             ┃       ╲    ┃┃┃
     * ┃    ┃    ┃   ┃                                    ┃┃                                                            ┃┃                                                             ┃       ╱    ┃┃┃
     * ┃    ┃    ┃   ┃                                    ┃┃                                                            ┃┃                                                             ┃       ╲    ┃┃┃
     * ┃    ┃    ┃   ┃                                    ┃┃                                                            ┃┃                                                             ┃       ╱    ┃┃┃
     * ┃    ┃    ┃   ┃                                    ┃┃                                                            ┃┃                                                             ┃       ╲    ┃┃┃
     *
     * (1) Hidden/Shown based on the FrcSystemConfigs.FeatureFlagKotlinTemplates
     * (2) Hidden/Shown based on the number of available for the selected template: shown when >1; hidden when <=1
     * (3) These language specific panels hold the Radio Button and Label for use (along with the panel) with a ButtonAndLabelSynchronizer
     * (4) a vertical spacer with a min & preferred height so panel does not shrink when inner panel is hidden. (We also set a min height on the outer panel)
     * </pre>
     */
    private JPanel templateLangSelectOuterPanel;
    /**
     * The inner panel for template (programming) language selection. This panel is
     * hidden or shown based on the number of available for the selected template:
     * shown when >1; hidden when <=1.
     */
    private JPanel templateLangSelectInnerPanel;
    /**
     * Panel to hold the {@link  #templateLangSelectContextHelpLabel}.
     */
    private JPanel templateLangSelectContexHelpPanel;
    private ContextHelpLabel templateLangSelectContextHelpLabel;
    /**
     * Panel to hold the 'Java' language option. It holds the
     * Radio Button and Label fo the option. The three items
     * (panel, button label) are used to create a 'ButtonAndLabelSynchronizer'.
     */
    private JPanel templateLangSelectJavaOptionPanel;
    private JBRadioButton templateLangSelectJavaOptionRadioButton;
    private JBLabel templateLangSelectJavaOptionLabel;
    /**
     * Panel to hold the 'Kotlin' language option. It holds the
     * Radio Button and Label fo the option. The three items
     * (panel, button label) are used to create a 'ButtonAndLabelSynchronizer'.
     */
    private JPanel templateLangSelectKotlinOptionPanel;
    private JBRadioButton templateLangSelectKotlinOptionRadioButton;
    private JBLabel templateLangSelectKotlinOptionLabel;
    private JPanel templateLangSelectLanguageLabelPanel;
    private JBLabel templateLangSelectLanguageLabel;
    private JPanel templateDescriptionPanel;
    private ButtonGroup langOptionButtonGroup;
    
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private ButtonAndLabelSynchronizer<JBRadioButton> javaLangOptionSynchronizer;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private ButtonAndLabelSynchronizer<JBRadioButton> kotlinLangOptionSynchronizer;
    private final Map<FrcWizardTemplateDefinition, TemplateLanguageOption> lastSelectedLanguage = new HashMap<>();
    
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
    
        // Enable copyable so that html links are clickable. But first need to set to allow auto wrapping
        templateDescriptionLabel.setAllowAutoWrapping(true);
        templateDescriptionLabel.setCopyable(true);
        initTabPane();
        initTemplatesLists();
        initTemplateLanguageOption();
        initOptionSynchronizers();
        lastSelectedLanguage.clear();
        LOG.trace("[FRC] Exiting FrcTemplateSelectionWizardStep.initComponents()");
    }
    
    
    private void initOptionSynchronizers()
    {
        javaLangOptionSynchronizer = new ButtonAndLabelSynchronizer<>(templateLangSelectJavaOptionPanel, templateLangSelectJavaOptionRadioButton, templateLangSelectJavaOptionLabel);
        kotlinLangOptionSynchronizer = new ButtonAndLabelSynchronizer<>(templateLangSelectKotlinOptionPanel, templateLangSelectKotlinOptionRadioButton, templateLangSelectKotlinOptionLabel);
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
    
    }
    
    
    private void saveSettings()
    {
        /*
        JPanel templateLanguageInnerMainPanel;
            JPanel templateLanguageLeftHelpPanel;
            JPanel templateLanguageRightSelectionPanel;
                JPanel templateOptionsPanel;
                    JBLabel templateLanguageLabel;
                    JBRadioButton templateLangSelectJavaOptionRadioButton;
                    JBRadioButton kotlinLanguageOptionRadioButton;
         */
        
        
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
        return myRootPanel;
    }
    
    
    @Override
    public boolean validate() throws ConfigurationException
    {
        LOG.trace("[FRC] Entering FrcTemplateSelectionWizardStep.validate()");
        // TODO : https://gitlab.com/Javaru/frc-intellij-idea-plugin/issues/53 
        //        Get required minimum Java level from selected template, and if none there, then from the 
        //           JAVA_VERSION key in 
        //            2019: C:\Users\Public\frc${frcYear}\jdk\release
        //            2020+ C:\Users\Public\wpilib\${frcYear}\jdk\release 
        //        From https://docs.wpilib.org/en/latest/docs/getting-started/getting-started-frc-control-system/wpilib-setup.html
        //        The installation directory has changed for 2020. In 2019 the software was installed to  ~\frcYYYY where ~ is C:\Users\Public on Windows and YYYY is the FRC year. 
        //        In 2020 and later it is installed to  ~\wpilib\YYYY  This lessens clutter when multiple years software are installed.
        //        Regardless of whether All Users or Current User is chosen, the software is installed to C:\Users\Public\wpilib\YYYY where YYYY is the current FRC year. 
        //            If you choose All Users, then shortcuts are installed to all users desktop and start menu and system environment variables are set. 
        //            If Current User is chosen, then shortcuts and environment variables are set for only the current user.
        FrcJavaLangUtilsKt.validateMinimumJavaVersion(myContext,
                                                      11,
                                                      FrcMessageKey.of("frc.ui.wizard.validate.minJavaVersion.additionalMessage.goBack"));
    
        
        // There's a situation where this gets called with a null value after the user goes back to the previous step, changes the wpilibVersion, and then comes forward again
        //   So its called prior to the 
        final FrcWizardTemplateDefinition selectedTemplate = determineSelectedTemplate();
        if (selectedTemplate == null)
        {
            Messages.showWarningDialog(getComponent(), 
                                       message("frc.ui.wizard.templateSelectionStep.validate.noTemplateSelected.message"), 
                                       message("frc.ui.wizard.templateSelectionStep.validate.noTemplateSelected.title"));
            return false;
        }
        else if(selectedTemplate.isDeprecated())
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
        
        LOG.trace("[FRC] Exiting FrcTemplateSelectionWizardStep.validate() gracefully with no validation errors");
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
        if (templateDefinition != null)
        {
            dataModel.setFrcWizardTemplateDefinition(templateDefinition);
            String langSelection = langOptionButtonGroup.getSelection().getActionCommand();
            dataModel.setTemplateLanguageOption(TemplateLanguageOption.valueOf(langSelection));
        }
        else 
        {
            LOG.warn("[FRC] Could not set/update the template definition on the FrcProjectWizardData as 'determineSelectedTemplate' returned null");
        }
        LOG.debug("[FRC] FrcWizardTemplateDefinition set to: " + templateDefinition);
        
        LOG.trace("[FRC] Exiting FrcTemplateSelectionWizardStep.updateDataModel()");
    }
    
    @Nullable
    private FrcWizardTemplateDefinition determineSelectedTemplate()
    {
        try
        {
            final Component selectedComponent = templateListsTabbedPane.getSelectedComponent();
            final Container panel = (Container) selectedComponent;
            final JViewport viewport = (JViewport) panel.getComponent(0);
            @SuppressWarnings("unchecked")
            final JList<FrcWizardTemplateDefinition> jList = (JList<FrcWizardTemplateDefinition>) viewport.getComponent(0);
            final FrcWizardTemplateDefinition selectedValue = jList.getSelectedValue();
            return selectedValue;
        }
        catch (Exception e)
        {
            Log.warn("[FRC] Exception when determining selected template in new project wizard: " + e.toString(), e);
            return null;
        }
    }
    
    
    private void updateComponents()
    {
        LOG.trace("[FRC] Entering FrcTemplateSelectionWizardStep.updateComponents()");
        // final boolean isAddToVisible = myParentProjectForm.isVisible();
        updateTemplatesLists();
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
        // NOTE: its important that in the form the min width for the tabbed pane is set to 200 (or so), otherwise the tabs can end up stacked on top of each other in some situations, 
        //        such as if the template names for the selected wpiLib version are shorter (than one tab's needed width for its name)
        templateListsTabbedPane.setSelectedIndex(PROJECTS_TAB_INDEX);
        templateListsTabbedPane.addChangeListener(e -> {
            updateTemplateDescription();
            updateTemplateLanguageSelection();
        });
    }
    
    private void initTemplatesLists()
    {
        ListSelectionListener templatesListSelectionListener = e -> {
            updateTemplateDescription();
            updateTemplateLanguageSelection();
        };
        projectTemplatesJBList.addListSelectionListener(templatesListSelectionListener);
        exampleTemplatesJBList.addListSelectionListener(templatesListSelectionListener);
    }
    
    private void initTemplateLanguageOption()
    {
        templateLangSelectJavaOptionRadioButton.setSelected(true);
        // This is the primary options panel, which for now just jas the one option: the templateLangSelectionOuterPanel
        templateOptionsPanel2.setVisible(true);
        templateOptionsPanel2.setEnabled(true);
        final boolean isKotlinTemplatesFeatureEnabled = BooleanUtils.toBoolean(FeatureFlagKotlinTemplates.INSTANCE.getValue());
        templateLangSelectOuterPanel.setVisible(isKotlinTemplatesFeatureEnabled);
        // We initialize to non-visible, so it does not show until a template that supports multiple languages is selected
        templateLangSelectInnerPanel.setVisible(false);
        templateLangSelectJavaOptionRadioButton.setActionCommand(TemplateLanguageOption.Java.name());
        templateLangSelectKotlinOptionRadioButton.setActionCommand(TemplateLanguageOption.Kotlin.name());
    
        ItemListener languageOptionChangeListener = e -> {
            final AbstractButton button = (AbstractButton) e.getSource();
            final ButtonModel model = button.getModel();
            final String actionCommand = model.getActionCommand();
            final TemplateLanguageOption languageOption = TemplateLanguageOption.valueOf(actionCommand);
            final FrcWizardTemplateDefinition selectedTemplate = determineSelectedTemplate();
            if (selectedTemplate != null)
            {
                lastSelectedLanguage.put(selectedTemplate, languageOption);
            }
        };
    
        templateLangSelectKotlinOptionRadioButton.addItemListener(languageOptionChangeListener);
        templateLangSelectJavaOptionRadioButton.addItemListener(languageOptionChangeListener);
    }
    
    
    private void updateTemplatesLists()
    {
        // We only want to update if:
        //   1) the list has not yet been set (i.e. first time the step is appearing after the user selects the wpilibVersion and we now know what templates to use)
        //   2) The user has gone back and changed the wpilibVersion such that it causes the list of templates to change
        // We do NOT want to update the list if this is hit from the user using the "Previous" button to return to this step since if we update, their selection gets reset 
        final WpiLibVersion wpilibVersion = myBuilder.getDataModel().getWpilibVersion();
        final FrcWizardTemplateDefinition[] projectTemplates = FrcWizardTemplateDefinitionsKt.projectTemplateDefinitionsFor(wpilibVersion);
        final FrcWizardTemplateDefinition[] exampleTemplates = FrcWizardTemplateDefinitionsKt.exampleTemplateDefinitionsFor(wpilibVersion);
        
        if (dataListNeedsUpdating(projectTemplates, projectTemplatesJBList) || dataListNeedsUpdating(exampleTemplates, exampleTemplatesJBList))
        {
            @Nullable
            final FrcWizardTemplateDefinition previouslySelectedTemplate = determineSelectedTemplate();
            @Nullable
            final Component previouslySelectedComponent = templateListsTabbedPane.getSelectedComponent();
            
            // We set the selected Index first because updating the list data will fire our change listener to update the description
            // That change listener reads the selected index. There is a chance that the selected index is out of bounds 
            //    (because the new list is shorter, and the selected index was for an item near the end of the previous list)
            projectTemplatesJBList.setSelectedIndex(0);
            projectTemplatesJBList.setListData(projectTemplates);
    
            exampleTemplatesJBList.setSelectedIndex(0);
            exampleTemplatesJBList.setListData(exampleTemplates);
            
            if (previouslySelectedComponent != null)
            {
                templateListsTabbedPane.setSelectedComponent(previouslySelectedComponent);
            }
            else
            {
                templateListsTabbedPane.setSelectedIndex(PROJECTS_TAB_INDEX);
            }
            
            // lets see if we can find the corresponding template and use select it.
            if (previouslySelectedTemplate != null && previouslySelectedComponent != null)
            {   
                try
                {
                    final String previousId = previouslySelectedTemplate.id();
                    final Container panel = (Container) previouslySelectedComponent;
                    final JViewport viewport = (JViewport) panel.getComponent(0);
                    @SuppressWarnings("unchecked")
                    final JList<FrcWizardTemplateDefinition> jList = (JList<FrcWizardTemplateDefinition>) viewport.getComponent(0);
                    final ListModel<FrcWizardTemplateDefinition> listModel = jList.getModel();
                    
                    for (int i = 0; i < listModel.getSize(); i++)
                    {
                        final FrcWizardTemplateDefinition templateDefinition = listModel.getElementAt(i);
                        if (templateDefinition.id().equals(previousId))
                        {
                            jList.setSelectedValue(templateDefinition, true);
                            break;
                        }
                    }
                }
                catch (Exception e)
                {
                    LOG.info("[FRC] Exception when attempting to potentially set selected template to match previously selected template: " + e.toString(), e);
                }
            }
            updateTemplateDescription();
        }
    }
    
    private boolean dataListNeedsUpdating(FrcWizardTemplateDefinition[] neededProjectTemplates, JBList<FrcWizardTemplateDefinition> jbList)
    {
        final ListModel<FrcWizardTemplateDefinition> listModel = jbList.getModel();
        if (neededProjectTemplates.length != listModel.getSize()) { return true;}
        for (int i = 0; i < neededProjectTemplates.length; i++)
        {
            if (neededProjectTemplates[i] != listModel.getElementAt(i))
            {
                return true;
            }
        }
        return false;
    }
    
    protected void updateTemplateDescription()
    {
        try
        {
            updateTemplateDescription(determineSelectedTemplate());
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] An exception occurred when attempting to update the template description: " + e.toString(), e);
        }
    }
    
    
    protected void updateTemplateDescription(@Nullable FrcWizardTemplateDefinition selectedTemplate)
    {
        if (selectedTemplate != null)
        {
            templateDescriptionLabel.setText(selectedTemplate.getDisplayNameAndDescription());
        }
        else 
        {
            templateDescriptionLabel.setText(message("frc.ui.wizard.templateSelectionStep.templateDescriptionLabel.default.text"));
            LOG.debug("[FRC] selectedTemplate was null. Template description set default string.");
        }
    }
    
    protected void updateTemplateLanguageSelection()
    {
        try
        {
            updateTemplateLanguageSelection(determineSelectedTemplate());
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] An exception occurred when attempting to update the template description: " + e.toString(), e);
        }
    }
    
    
    protected void updateTemplateLanguageSelection(@Nullable FrcWizardTemplateDefinition selectedTemplate)
    {
        if (selectedTemplate != null)
        {
            // Set to the last selected language for the template, or the template's default if no previous language
            TemplateLanguageOption lastLanguageOption =
                lastSelectedLanguage.computeIfAbsent(selectedTemplate, frcWizardTemplateDefinition -> selectedTemplate.getAvailableTemplateLanguages().get(0));
            setSelectedTemplateLanguageOptionButton(lastLanguageOption);
            // Hide the language option panel if the template does not support more than 1 language
            templateLangSelectInnerPanel.setVisible(selectedTemplate.getAvailableTemplateLanguages().size() > 1);
            // Disable buttons for unsupported languages
            final Enumeration<AbstractButton> buttons = langOptionButtonGroup.getElements();
            while (buttons.hasMoreElements())
            {
                final AbstractButton button = buttons.nextElement();
                final TemplateLanguageOption buttonLanguageOption = TemplateLanguageOption.valueOf(button.getActionCommand());
                final boolean isSupportedLang = selectedTemplate.supportsLanguageOption(buttonLanguageOption);
                button.setEnabled(isSupportedLang);
                button.setVisible(isSupportedLang);
            }
        }
        else
        {
            LOG.debug("[FRC] selectedTemplate was null. Disabling Template Language Option.");
            templateLangSelectInnerPanel.setVisible(false);
        }
    }

    
    
    protected void setSelectedTemplateLanguageOptionButton(TemplateLanguageOption languageOption)
    {
        switch (languageOption)
        {
            case Kotlin:
                templateLangSelectKotlinOptionRadioButton.setSelected(true);
                break;
            case Java:
                templateLangSelectJavaOptionRadioButton.setSelected(true);
                break;
            default:
                LOG.warn("Unknown TemplateLanguageOption of " + languageOption + " in setSelectedTemplateLanguageOptionButton()");
                templateLangSelectJavaOptionRadioButton.setSelected(true);
                break;
        }
    }
    
    
    private void createUIComponents()
    {
        templateLangSelectContextHelpLabel = ContextHelpLabel.create(message("frc.ui.wizard.templateSelectionStep.languageOption.helpContext.title"),
                                                                     message("frc.ui.wizard.templateSelectionStep.languageOption.helpContext.text"));
    }
}
