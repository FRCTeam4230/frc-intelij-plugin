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
import com.intellij.ui.components.JBTextField;

import net.javaru.iip.frc.settings.FrcProjectGeneralSettings;
import net.javaru.iip.frc.settings.FrcRoboRioSettings;
import net.javaru.iip.frc.settings.FrcTeamNumberKt;
import net.javaru.iip.frc.settings.TeamNumberFormChangeListener;



public class RoboRioConfigurationForm implements TeamNumberFormChangeListener
{
    private static final Logger LOG = Logger.getInstance(RoboRioConfigurationForm.class);
    
    @NotNull
    private FrcRoboRioSettings internalFrcRoboRioSettings;
    @NotNull
    private final Project project;
//    @NotNull
//    private final MessageBusConnection messageBusConnection;
    
    private JPanel rootPanel;
    private JBTextField roboRioMDnsHostName;
    private JButton mDnsHostNameDefaultValueButton;
    private JButton dnsHostNameDefaultValueButton;
    private JBTextField roboRioDnsHostName;
    private JBTextField roboRioFieldLocalHostName;
    private JButton fieldLocalHostNameDefaultValueButton;
    private JBTextField roboRioStaticUsbIp;
    private JButton roboRioStaticUsbIpDefaultValueButton;
    private JBTextField roboRioIpAddress;
    private JButton roboRioIpAddressDefaultValueButton;
    
    private int internalRoboRioTeamNumber;
    
    public RoboRioConfigurationForm(@NotNull Project project)
    {
        this.project = project;
        internalFrcRoboRioSettings = FrcRoboRioSettings.clone(FrcRoboRioSettings.getInstance(project));
        initForm();
        resetAll();
        //messageBusConnection = ProjectTeamNumberChangeListener.Companion.subscribe(project, this);
    }
    
    
    public JPanel getRootPanel() { return rootPanel; }
    
    
    private void initForm()
    {
//        initRioLogTargetWindowRadioButtons();
//        initPortTextField();
        initRoboRioComponents();

//        portToDefaultValueButton.addActionListener(e -> setUdpPortToDefault());
    }
    
    /**
     * Resets the form's values to the current (i.e. previously) saved values.
     * Typically called by the `Configurable.reset()` method.
     */
    public void resetForm()
    {
        resetForm(FrcProjectGeneralSettings.getInstance(project), FrcRoboRioSettings.getInstance(project));
    }
    
    /**
     * Loads (or resets) the form's values to the supplied setting objects.
     * Typically called by the `Configurable.reset()` method.  
     * @param frcProjectGeneralSettings the project settings to load (or reset to)
     * @param frcRoboRioSettings the roboRIO settings to load (or reset to)
     */
    public void resetForm(@NotNull FrcProjectGeneralSettings frcProjectGeneralSettings,
                          @NotNull FrcRoboRioSettings frcRoboRioSettings)
    {
        internalFrcRoboRioSettings = FrcRoboRioSettings.clone(frcRoboRioSettings);
        resetAll();
    }
    
    
    /**
     * Resets all components using the internal settings fields.
     */
    private void resetAll()
    {
        resetRoboRioHostFields();
    }
    
    
    private void initRoboRioComponents()
    {
        // TODO should we add verifiers ?
        
        resetRoboRioHostFields();
        
        roboRioMDnsHostName.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) { }
            
            
            @Override
            public void keyPressed(KeyEvent e) { }
            
            
            @Override
            public void keyReleased(KeyEvent e)
            {
                internalFrcRoboRioSettings.setRoboRioHost_mDNS(roboRioMDnsHostName.getText());
            }
        });
        mDnsHostNameDefaultValueButton.addActionListener(e -> {
            final String defaultValue = internalFrcRoboRioSettings.getRoboRioHostDefault_mDNS();
            roboRioMDnsHostName.setText(defaultValue);
            internalFrcRoboRioSettings.setRoboRioHost_mDNS(defaultValue);
        });
    
    
        roboRioDnsHostName.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) { }
            
            
            @Override
            public void keyPressed(KeyEvent e) { }
            
            
            @Override
            public void keyReleased(KeyEvent e)
            {
                internalFrcRoboRioSettings.setRoboRioHost_DNS(roboRioDnsHostName.getText());
            }
        });
        dnsHostNameDefaultValueButton.addActionListener(e -> {
            final String defaultValue = internalFrcRoboRioSettings.getRoboRioHostDefault_DNS();
            roboRioDnsHostName.setText(defaultValue);
            internalFrcRoboRioSettings.setRoboRioHost_DNS(defaultValue);
        });
        
        roboRioFieldLocalHostName.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) { }
            
            
            @Override
            public void keyPressed(KeyEvent e) { }
            
            
            @Override
            public void keyReleased(KeyEvent e)
            {
                internalFrcRoboRioSettings.setRoboRioHost_FieldLocal(roboRioFieldLocalHostName.getText());
            }
        });
        fieldLocalHostNameDefaultValueButton.addActionListener(e -> {
            final String defaultValue = internalFrcRoboRioSettings.getRoboRioHostDefault_FieldLocal();
            roboRioFieldLocalHostName.setText(defaultValue);
            internalFrcRoboRioSettings.setRoboRioHost_FieldLocal(defaultValue);
        });
        
        
        roboRioIpAddress.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) { }
            
            
            @Override
            public void keyPressed(KeyEvent e) { }
            
            
            @Override
            public void keyReleased(KeyEvent e)
            {
                internalFrcRoboRioSettings.setRoboRioHost_IP(roboRioIpAddress.getText());
            }
        });
        roboRioIpAddressDefaultValueButton.addActionListener(e -> {
            final String defaultValue = internalFrcRoboRioSettings.getRoboRioHostDefault_IP();
            roboRioIpAddress.setText(defaultValue);
            internalFrcRoboRioSettings.setRoboRioHost_IP(defaultValue);
        });
        
        
        roboRioStaticUsbIp.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) { }
            
            
            @Override
            public void keyPressed(KeyEvent e) { }
            
            
            @Override
            public void keyReleased(KeyEvent e)
            {
                internalFrcRoboRioSettings.setRoboRioHost_USB(roboRioStaticUsbIp.getText());
            }
        });
        roboRioStaticUsbIpDefaultValueButton.addActionListener(e -> {
            final String defaultValue = internalFrcRoboRioSettings.getRoboRioHostDefault_USB();
            roboRioStaticUsbIp.setText(defaultValue);
            internalFrcRoboRioSettings.setRoboRioHost_USB(defaultValue);
        });
        
    }
    
    
    private void resetRoboRioHostFields()
    {
        roboRioMDnsHostName.setText(internalFrcRoboRioSettings.getRoboRioHost_mDNS());
        roboRioDnsHostName.setText(internalFrcRoboRioSettings.getRoboRioHost_DNS());
        roboRioFieldLocalHostName.setText(internalFrcRoboRioSettings.getRoboRioHost_FieldLocal());
        roboRioIpAddress.setText(internalFrcRoboRioSettings.getRoboRioHost_IP());
        roboRioStaticUsbIp.setText(internalFrcRoboRioSettings.getRoboRioHost_USB());
    }
    
    
    @Override
    public void onTeamNumberFormChange(@NotNull String newText, boolean isValidTeamNumber, @NotNull String previousText)
    {
        if (isValidTeamNumber)
        {
            updateFieldsWithNewTeamNumber(FrcTeamNumberKt.toTeamNumberOrUndefined(newText));
        }
    }
    
    
    private void updateFieldsWithNewTeamNumber(int newTeamNum)
    {
        if (internalFrcRoboRioSettings.isRoboRioHostTheDefault_mDNS()) { roboRioMDnsHostName.setText(internalFrcRoboRioSettings.getRoboRioHostDefault_mDNS(newTeamNum)); }
        if (internalFrcRoboRioSettings.isRoboRioHostTheDefault_DNS()) { roboRioDnsHostName.setText(internalFrcRoboRioSettings.getRoboRioHostDefault_DNS(newTeamNum)); }
        if (internalFrcRoboRioSettings.isRoboRioHostTheDefault_FieldLocal()) { roboRioFieldLocalHostName.setText(internalFrcRoboRioSettings.getRoboRioHostDefault_FieldLocal(newTeamNum)); }
        if (internalFrcRoboRioSettings.isRoboRioHostTheDefault_USB()) { roboRioStaticUsbIp.setText(internalFrcRoboRioSettings.getRoboRioHostDefault_USB()); }
        if (internalFrcRoboRioSettings.isRoboRioHostTheDefault_IP()) { roboRioIpAddress.setText(internalFrcRoboRioSettings.getRoboRioHostDefault_IP(newTeamNum));}
    }
    
    
   
    
    public synchronized void applyTo(@NotNull FrcProjectGeneralSettings frcProjectGeneralSettings,
                                     @NotNull FrcRoboRioSettings frcRoboRioSettings)
    {
        LOG.debug("[FRC] Before applying form frcRoboRioSettings of\n" + internalFrcRoboRioSettings + "\nto current/previous frcRoboRioSettings of\n" + frcRoboRioSettings);
    
        // *** APPLY INTERNAL CHANGES TO THE SETTING INSTANCES
    
        frcRoboRioSettings.setRoboRioHost_USB(internalFrcRoboRioSettings.getRoboRioHost_USB());
        frcRoboRioSettings.setRoboRioHost_IP(internalFrcRoboRioSettings.getRoboRioHost_IP());
        frcRoboRioSettings.setRoboRioHost_DNS(internalFrcRoboRioSettings.getRoboRioHost_DNS());
        frcRoboRioSettings.setRoboRioHost_mDNS(internalFrcRoboRioSettings.getRoboRioHost_mDNS());
        
        
        LOG.debug("[FRC] After  applying form frcRoboRioSettings of\n" + internalFrcRoboRioSettings + "\nto current/previous frcRoboRioSettings of\n" + frcRoboRioSettings);
        
        // ** NO CHANGES TO SETTINGS OBJECTS BELOW THIS
    
        //Reset the internal state to the updated setting
        internalFrcRoboRioSettings = FrcRoboRioSettings.clone(frcRoboRioSettings);
    }
    
    
    
    public boolean isModified()
    {
        final boolean modified = !FrcRoboRioSettings.getInstance(project).equals(internalFrcRoboRioSettings);
        LOG.trace("[FRC] RoboRioConfigurationForm.isModified returning " + modified);
        return modified;
    }
    
    
    
}
