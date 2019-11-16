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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;

import net.javaru.iip.frc.messaging.ProjectTeamNumberChangeListener;
import net.javaru.iip.frc.riolog.RioLogGlobals;
import net.javaru.iip.frc.settings.FrcSshSettings;
import net.javaru.iip.frc.settings.FrcSshSettingsKt;



public class SshConfigurationForm implements ProjectTeamNumberChangeListener
{
    private static final Logger LOG = Logger.getInstance(SshConfigurationForm.class);
    
    @NotNull
    private FrcSshSettings internalFrcSshSettings;
    @NotNull
    private final Project project;
    
    private JPanel rootPanel;
    private JBTextField sshUsername;
    private JButton sshUsernameToDefaultButton;
    private JBPasswordField sshPassword;
    private JButton sshPasswordToDefaultButton;
    private JButton tailCommandToDefaultValueButton;
    private JBTextField sshTailCommand;
    
    
    public SshConfigurationForm(@NotNull Project project)
    {
        this.project = project;
        internalFrcSshSettings = FrcSshSettings.clone(FrcSshSettings.getInstance(project));
        initForm();
        resetAll();
    }
    
    
    private void initForm()
    {
        initSshSettingComponents();
    }
    
    
    private void initSshSettingComponents()
    {
        // TODO should we add verifiers ?
        
        resetSshSettingsFields();
        
        sshUsername.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) { }
            
            
            @Override
            public void keyPressed(KeyEvent e) { }
            
            
            @Override
            public void keyReleased(KeyEvent e)
            {
                internalFrcSshSettings.setSshUsername(sshUsername.getText());
            }
        });
        
        sshUsernameToDefaultButton.addActionListener(e -> {
            sshUsername.setText(FrcSshSettingsKt.SSH_USERNAME_DEFAULT);
            internalFrcSshSettings.setSshUsername(FrcSshSettingsKt.SSH_USERNAME_DEFAULT);
        });
        
        sshPassword.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) { }
            
            
            @Override
            public void keyPressed(KeyEvent e) { }
            
            
            @Override
            public void keyReleased(KeyEvent e)
            {
                internalFrcSshSettings.setSshPassword(new String(sshPassword.getPassword()));
            }
        });
        sshPasswordToDefaultButton.addActionListener(e -> {
            sshPassword.setText(FrcSshSettingsKt.SSH_PASSWORD_DEFAULT);
            internalFrcSshSettings.setSshPassword(FrcSshSettingsKt.SSH_PASSWORD_DEFAULT);
        });
        
        sshTailCommand.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) { }
            
            
            @Override
            public void keyPressed(KeyEvent e) { }
            
            
            @Override
            public void keyReleased(KeyEvent e)
            {
                internalFrcSshSettings.setSshTailCommand(sshTailCommand.getText());
            }
        });
        tailCommandToDefaultValueButton.addActionListener(e -> {
            sshTailCommand.setText(RioLogGlobals.DEFAULT_TAIL_COMMAND);
            internalFrcSshSettings.setSshTailCommand(RioLogGlobals.DEFAULT_TAIL_COMMAND);
        });
    }
    
    
    private void resetSshSettingsFields()
    {
        sshUsername.setText(internalFrcSshSettings.getSshUsername());
        sshPassword.setText(internalFrcSshSettings.getSshPassword());
        sshTailCommand.setText(internalFrcSshSettings.getSshTailCommand());
    }
    
    
    /**
     * Resets the form's values to the current (i.e. previously) saved values.
     * Typically called by the `Configurable.reset()` method.
     */
    public void resetForm()
    {
        resetForm(FrcSshSettings.getInstance(project));
    }
    
    /**
     * Loads (or resets) the form's values to the supplied setting objects.
     * Typically called by the `Configurable.reset()` method.
     *
     * @param frcSshSettings the SSH settings to load (or reset to)
     */
    public void resetForm(@NotNull FrcSshSettings frcSshSettings)
    {
        internalFrcSshSettings = FrcSshSettings.clone(frcSshSettings);
        resetAll();
    }
    
    
    /**
     * Resets all components using the internal settings fields.
     */
    private void resetAll()
    {
        resetSshSettingsFields();
    }
    
    
    public boolean isModified()
    {
        final boolean modified = !FrcSshSettings.getInstance(project).equals(internalFrcSshSettings);
        LOG.trace("[FRC] SshConfigurationForm.isModified returning " + modified);
        return modified;
    }
    
    
    public synchronized void applyTo(@NotNull FrcSshSettings frcSshSettings)
    {
        LOG.debug("[FRC] Before applying form frcSshSettings of\n" + internalFrcSshSettings + "\nto current/previous frcSshSettings of\n" + frcSshSettings);
        
        // *** APPLY INTERNAL CHANGES TO THE SETTING INSTANCES
        frcSshSettings.setSshUsername(internalFrcSshSettings.getSshUsername());
        frcSshSettings.setSshPassword(internalFrcSshSettings.getSshPassword());
        frcSshSettings.setSshTailCommand(internalFrcSshSettings.getSshTailCommand());
        
        
        // ** NO CHANGES TO SETTINGS OBJECTS BELOW THIS
        LOG.debug("[FRC] After  applying form frcSshSettings of\n" + internalFrcSshSettings + "\nto current/previous frcSshSettings of\n" + frcSshSettings);
        internalFrcSshSettings = FrcSshSettings.clone(frcSshSettings);
    }
    
    
    @Override
    public void projectTeamNumberChanged(int teamNum)
    {
        // No updates needed at this time. The only changes needed are to the URLs/IPs which 
        // are in the RoboRioConfigurationForm and changes are handled there
    }
    
    
    @Override
    public void projectTeamNumberPotentialChange(int newTeamNum)
    {
        //TODO: Write this 'projectTeamNumberPotentialChange' implemented method in the 'SshConfigurationForm' class
    }
    
    
    public JPanel getRootPanel() { return rootPanel; }
    
    
    
}
