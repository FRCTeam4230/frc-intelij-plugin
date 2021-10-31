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

package net.javaru.iip.frc.settings.forms;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;

import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.settings.FrcTeamNumberKt;
import net.javaru.iip.frc.settings.TeamNumberFormChangeListener;
import net.javaru.iip.frc.settings.TeamNumberInputVerifier;
import net.javaru.iip.frc.settings.TeamNumberKeyChangeListener;
import net.javaru.iip.frc.util.FrcProjectExtsKt;



public class FrcApplicationSettingsForm implements TeamNumberFormChangeListener
{
    private static final Logger LOG = Logger.getInstance(FrcApplicationSettingsForm.class);

    @NotNull
    private FrcApplicationSettings internalFrcApplicationSettings;

    private JPanel rootPanel;
    private JPanel generalSettingsPanel;
    private JBTextField teamNumberTextField;
    private JBLabel teamNumberWarningIconLabel;
    private JPanel projectWizardSettingsPanel;
    private JBCheckBox enableGradleImportUponNewProjectCreationCheckBox;
    private JBLabel teamNumberAdditionalInfoLabel;
    private JCheckBox provideCustomIconsCheckBox;
    private TeamNumberKeyChangeListener teamNumberKeyChangeListener;
    
    
    public FrcApplicationSettingsForm()
    {
        internalFrcApplicationSettings = FrcApplicationSettings.clone(FrcApplicationSettings.getInstance());
        initForm();
        resetAll();
    }


    private void initForm()
    {
        initTeamNumberField();
        initMiscSelections();
    }


    public boolean isModified()
    {
        final boolean modified = !FrcApplicationSettings.getInstance().equals(internalFrcApplicationSettings);
        LOG.trace("[FRC] FrcApplicationSettingsForm.isModified returning " + modified);
        return modified;
    }
    
    
    /**
     * Resets the form's values to the current (i.e. previously) saved values.
     * Typically called by the `Configurable.reset()` method.
     */
    public void resetForm()
    {
        resetForm(FrcApplicationSettings.getInstance());
    }
    
    
    /**
     * Resets the form's values to the supplied setting objects.
     * Typically called by the `Configurable.reset()` method.
     *
     * @param applicationSettings    the application settings to load (or reset to)
     */
    public void resetForm(@NotNull FrcApplicationSettings applicationSettings)
    {
        internalFrcApplicationSettings = FrcApplicationSettings.clone(applicationSettings);
        resetAll();
    }
    
    
    /**
     * Resets all components using the internal settings fields.
     */
    private void resetAll()
    {
        resetTeamNumberTextFieldValue();
        resetMiscSelections();
    }
    
    public synchronized void applyTo(@NotNull FrcApplicationSettings frcApplicationSettings)
    {
        LOG.debug("[FRC] Before applying form frcApplicationSettings of\n" + internalFrcApplicationSettings + "\nto current/previous frcApplicationSettings of\n" + frcApplicationSettings);

        
        // *** APPLY INTERNAL CHANGES TO THE SETTING INSTANCES
        
        //final boolean teamNumberHasChanged = frcApplicationSettings.getTeamNumber() != internalFrcApplicationSettings.getTeamNumber();
        frcApplicationSettings.setTeamNumber(internalFrcApplicationSettings.getTeamNumber());
        frcApplicationSettings.setRioLogUdpPort(internalFrcApplicationSettings.getRioLogUdpPort());
        frcApplicationSettings.setEnableGradleImportUponNewProjectCreation(internalFrcApplicationSettings.getEnableGradleImportUponNewProjectCreation());
        boolean iconsOptionHasChanged = frcApplicationSettings.getProvideCustomFileIcons() != internalFrcApplicationSettings.getProvideCustomFileIcons();
        frcApplicationSettings.setProvideCustomFileIcons(internalFrcApplicationSettings.getProvideCustomFileIcons());
        
        LOG.debug("[FRC] After  applying form frcApplicationSettings of\n" + internalFrcApplicationSettings + "\nto current/previous frcApplicationSettings of\n" + frcApplicationSettings);
        
        // ** NO CHANGES TO SETTINGS OBJECTS BELOW THIS
        
        //Reset the internal state to the updated setting
        internalFrcApplicationSettings = FrcApplicationSettings.clone(frcApplicationSettings);
        if (iconsOptionHasChanged)
        {
            FrcProjectExtsKt.refreshProjectViewsForAllFrcProjects();
        }
        // ** NO CODE BELOW THIS **
    }


    private void resetTeamNumberTextFieldValue()
    {
        final int teamNumber = internalFrcApplicationSettings.getTeamNumber();
        teamNumberTextField.setText(teamNumber <= 0 ? "" : Integer.toString(teamNumber));
        if (teamNumberKeyChangeListener != null) {teamNumberKeyChangeListener.reset();}
        setTeamNumberWarningVisibility(teamNumber <= 0);
    }
    
    private void resetMiscSelections()
    {
        enableGradleImportUponNewProjectCreationCheckBox.setSelected(internalFrcApplicationSettings.getEnableGradleImportUponNewProjectCreation());
        provideCustomIconsCheckBox.setSelected(internalFrcApplicationSettings.getProvideCustomFileIcons());
    }
    
    private void initTeamNumberField()
    {
        // The TeamNumberKeyChangeListener calls the 'onTeamNumberFormChange' method upon changes to the team number text field
        teamNumberKeyChangeListener = new TeamNumberKeyChangeListener(teamNumberTextField, this);
        teamNumberTextField.addKeyListener(teamNumberKeyChangeListener);
        teamNumberTextField.setInputVerifier(new TeamNumberInputVerifier());
        resetTeamNumberTextFieldValue();
    }
    
    
    @Override // Called by the TeamNumberKeyChangeListener
    public void onTeamNumberFormChange(@NotNull String text, boolean isValidTeamNumber, @NotNull String previousText)
    {
        setTeamNumberWarningVisibility(!isValidTeamNumber);
        internalFrcApplicationSettings.setTeamNumber(FrcTeamNumberKt.toTeamNumberOrUndefined(text));
    }
    
    private void initMiscSelections()
    {
        enableGradleImportUponNewProjectCreationCheckBox.setSelected(internalFrcApplicationSettings.getEnableGradleImportUponNewProjectCreation());
        enableGradleImportUponNewProjectCreationCheckBox.addActionListener(e-> internalFrcApplicationSettings.setEnableGradleImportUponNewProjectCreation(enableGradleImportUponNewProjectCreationCheckBox.isSelected()));
    
        provideCustomIconsCheckBox.setSelected(internalFrcApplicationSettings.getProvideCustomFileIcons());
        provideCustomIconsCheckBox.addActionListener(e -> internalFrcApplicationSettings.setProvideCustomFileIcons(provideCustomIconsCheckBox.isSelected()));
    }


    private void setTeamNumberWarningVisibility(boolean isVisible)
    {
        teamNumberWarningIconLabel.setVisible(isVisible);
    }
    
    public JPanel getRootPanel() { return rootPanel; }
}
