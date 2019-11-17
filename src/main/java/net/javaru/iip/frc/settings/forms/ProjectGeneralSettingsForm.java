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

package net.javaru.iip.frc.settings.forms;

import javax.swing.*;

import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;

import net.javaru.iip.frc.actions.ConfigureTeamNumberBasicAction;
import net.javaru.iip.frc.settings.FrcProjectGeneralSettings;
import net.javaru.iip.frc.settings.FrcTeamNumberKt;
import net.javaru.iip.frc.settings.TeamNumberFormChangeListener;
import net.javaru.iip.frc.settings.TeamNumberInputVerifier;
import net.javaru.iip.frc.settings.TeamNumberKeyChangeListener;



public class ProjectGeneralSettingsForm implements TeamNumberFormChangeListener
{
    private static final Logger LOG = Logger.getInstance(ProjectGeneralSettingsForm.class);
    
    
    private JPanel rootPanel;
    private JBTextField projectTeamNumberTextField;
    private JBLabel projectTeamNumberWarningIconLabel;
    private JBLabel projectTeamNumberInfoLabel;
    
    @NotNull
    private FrcProjectGeneralSettings internalFrcProjectGeneralSettings;
    
    @NotNull
    private final Project project;
    private TeamNumberKeyChangeListener teamNumberKeyChangeListener;
    private final TeamNumberFormChangeListener[] externalTeamNumberFormChangeListeners;
    
    
    public ProjectGeneralSettingsForm(@NotNull Project project, TeamNumberFormChangeListener... teamNumberFormChangeListeners)
    {
        this.project = project;
        this.externalTeamNumberFormChangeListeners = teamNumberFormChangeListeners;
        internalFrcProjectGeneralSettings = FrcProjectGeneralSettings.clone(FrcProjectGeneralSettings.getInstance(project));
        initForm();
        resetAll();
    }
    
    
    private void initForm()
    {
        initTeamNumberField();
    }
    
    
    private void initTeamNumberField()
    {
        ;
        // The TeamNumberKeyChangeListener calls the 'onTeamNumberFormChange' method upon changes to the team number text field
        teamNumberKeyChangeListener = new TeamNumberKeyChangeListener(projectTeamNumberTextField, ArrayUtils.add(externalTeamNumberFormChangeListeners, this));
        projectTeamNumberTextField.addKeyListener(teamNumberKeyChangeListener);
        projectTeamNumberTextField.setInputVerifier(new TeamNumberInputVerifier());
        resetTeamNumberTextFieldValue();
    }
    
    
    @Override // Called by the TeamNumberKeyChangeListener
    public void onTeamNumberFormChange(@NotNull String text, boolean isValidTeamNumber, @NotNull String previousText)
    {
        setTeamNumberWarningVisibility(!isValidTeamNumber);
        if (isValidTeamNumber)
        {
            int newTeamNumber = Integer.parseInt(text);
            internalFrcProjectGeneralSettings.setTeamNumber(newTeamNumber);
            
        
            // TODO - MAJOR
            // Need_to_publish_team_number_change_?
        }
    }
    
    
    /**
     * Resets the form's values to the current (i.e. previously) saved values.
     * Typically called by the `Configurable.reset()` method.
     */
    public void resetForm()
    {
        resetForm(FrcProjectGeneralSettings.getInstance(project));
    }
    
    /**
     * Resets the form's values to the supplied setting objects.
     * Typically called by the `Configurable.reset()` method.
     * 
     * @param frcProjectGeneralSettings the project settings to load (or reset to)
     */
    public void resetForm(@NotNull FrcProjectGeneralSettings frcProjectGeneralSettings)
    {
        // TODO Need to implement - copy from old Applications form
        internalFrcProjectGeneralSettings = FrcProjectGeneralSettings.clone(frcProjectGeneralSettings);
//        internalFrcRoboRioSettings = FrcRoboRioSettings.clone(frcRoboRioSettings);
//        internalFrcSshSettings = FrcSshSettings.clone(frcSshSettings);
        resetAll();
    }
    
    
    /**
     * Resets all components using the internal settings fields.
     */
    private void resetAll()
    {
        resetTeamNumberTextFieldValue();
    }
    
    private void resetTeamNumberTextFieldValue()
    {
        final int teamNumber = internalFrcProjectGeneralSettings.getTeamNumber();
        projectTeamNumberTextField.setText(FrcTeamNumberKt.toValidTeamNumberOrEmptyString(teamNumber));
        if (teamNumberKeyChangeListener != null) {teamNumberKeyChangeListener.reset();}
        setTeamNumberWarningVisibility(teamNumber);
    }
    
    public boolean isModified()
    {
        return  !FrcProjectGeneralSettings.getInstance(project).equals(internalFrcProjectGeneralSettings);
    }
    
    
    public synchronized void applyTo(@NotNull FrcProjectGeneralSettings frcProjectGeneralSettings)
    {
        LOG.debug("[FRC] Before applying form frcProjectGeneralSettings of\n" + internalFrcProjectGeneralSettings + "\nto current/previous frcProjectGeneralSettings of\n" + frcProjectGeneralSettings);
        
        // *** APPLY INTERNAL CHANGES TO THE SETTING INSTANCES
        final boolean teamNumberHasChanged = frcProjectGeneralSettings.getTeamNumber() != internalFrcProjectGeneralSettings.getTeamNumber();
        frcProjectGeneralSettings.setTeamNumber(internalFrcProjectGeneralSettings.getTeamNumber());
        
        LOG.debug("[FRC] After  applying form frcProjectGeneralSettings of\n" + internalFrcProjectGeneralSettings + "\nto current/previous frcProjectGeneralSettings of\n" + frcProjectGeneralSettings);
    
        // ** NO CHANGES TO SETTINGS OBJECTS BELOW THIS
        //Reset the internal state to the updated setting
        internalFrcProjectGeneralSettings = FrcProjectGeneralSettings.clone(frcProjectGeneralSettings);
    
        if (teamNumberHasChanged)
        {
            LOG.info("[FRC] Team number has been changed in settings. Updating the wpilib.properties file");
            // TODO - MAJOR
            // Need_to_publish_team_number_change_AndReplaceThisMethod_Need_ToUpdate_the_New_Json_file();
            ConfigureTeamNumberBasicAction.performTeamNumberChangeUpdates();
        }
        // ** NO CODE BELOW THIS *
    }
    
    private void setTeamNumberWarningVisibility(int teamNumber)
    {
        setTeamNumberWarningVisibility(teamNumber <= 0);
    }
    
    private void setTeamNumberWarningVisibility(boolean isVisible)
    {
        projectTeamNumberWarningIconLabel.setVisible(isVisible);
    }
    
    public JPanel getRootPanel() { return rootPanel; }
}
