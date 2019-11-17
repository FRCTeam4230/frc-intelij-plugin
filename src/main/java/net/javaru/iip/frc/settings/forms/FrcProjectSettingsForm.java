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

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.settings.FrcProjectGeneralSettings;
import net.javaru.iip.frc.settings.FrcRoboRioSettings;
import net.javaru.iip.frc.settings.FrcSshSettings;



public class FrcProjectSettingsForm
{
    private static final Logger LOG = Logger.getInstance(FrcProjectSettingsForm.class);
    
    
    private JPanel rootPanel;
    private JPanel generalProjectSettingsPanel;
    private JPanel roboRioSettingsPanel;
    private JPanel sshSettingsPanel;
    
    @NotNull
    private final Project project;
    private ProjectGeneralSettingsForm projectGeneralSettingsForm;
    private RoboRioConfigurationForm roboRioConfigurationForm;
    private SshConfigurationForm sshConfigurationForm;
    
    public FrcProjectSettingsForm(@NotNull Project project)
    {
        this.project = project;
        
        initForm();
        resetAll();
    }
    
    private void initForm()
    {
        LOG.debug("[FRC] FrcProjectSettingsForm.initForm() called");
        // createUIComponents();  // does this get called automatically??
    }
    
    private void createUIComponents()
    {
        LOG.debug("[FRC] FrcProjectSettingsForm.createUIComponents() called");
        roboRioConfigurationForm = new RoboRioConfigurationForm(project);
        sshConfigurationForm = new SshConfigurationForm(project);
        projectGeneralSettingsForm = new ProjectGeneralSettingsForm(project, roboRioConfigurationForm);
        generalProjectSettingsPanel = projectGeneralSettingsForm.getRootPanel();
        roboRioSettingsPanel = roboRioConfigurationForm.getRootPanel();
        sshSettingsPanel = sshConfigurationForm.getRootPanel();
    }
    
    
    /**
     * Resets the form's values to the current (i.e. previously) saved values.
     * Typically called by the `Configurable.reset()` method.
     */
    public void resetForm()
    {
        resetForm(FrcProjectGeneralSettings.getInstance(project),
                  FrcRoboRioSettings.getInstance(project),
                  FrcSshSettings.getInstance(project));
    }
    
    /**
     * Resets the form's values to the supplied setting objects.
     * Typically called by the `Configurable.reset()` method.
     *
     * @param frcProjectGeneralSettings the project settings to load (or reset to)
     * @param frcRoboRioSettings the roboRIO settings to load (or reset to)
     * @param frcSshSettings     the SSH settings to load (or reset to)
     */
    public void resetForm(@NotNull FrcProjectGeneralSettings frcProjectGeneralSettings,
                          @NotNull FrcRoboRioSettings frcRoboRioSettings,
                          @NotNull FrcSshSettings frcSshSettings)
    {
        projectGeneralSettingsForm.resetForm(frcProjectGeneralSettings);
        roboRioConfigurationForm.resetForm(frcProjectGeneralSettings, frcRoboRioSettings);
        sshConfigurationForm.resetForm(frcSshSettings);
        resetAll();
    }
    
    
    /**
     * Resets all components using the internal settings fields.
     */
    private void resetAll()
    {
        // nothing at this time
    }
    
    
    public boolean isModified()
    {
        return projectGeneralSettingsForm.isModified() ||
               roboRioConfigurationForm.isModified() ||
               sshConfigurationForm.isModified();
    }
    
    
    public synchronized void applyTo(@NotNull FrcProjectGeneralSettings frcProjectGeneralSettings,
                                     @NotNull FrcRoboRioSettings frcRoboRioSettings,
                                     @NotNull FrcSshSettings frcSshSettings)
    {
        projectGeneralSettingsForm.applyTo(frcProjectGeneralSettings);
        roboRioConfigurationForm.applyTo(frcProjectGeneralSettings, frcRoboRioSettings);
        sshConfigurationForm.applyTo(frcSshSettings);
    }
    
    
    public JPanel getRootPanel() { return rootPanel; }
}
