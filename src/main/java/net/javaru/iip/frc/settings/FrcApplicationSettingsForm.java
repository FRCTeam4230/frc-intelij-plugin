/*
 * Copyright 2015-2017 Mark Vedder
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.javaru.iip.frc.settings;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import javax.swing.*;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;

import net.javaru.iip.frc.components.FrcProjectComponentImpl;
import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.riolog.RioLogGlobals;
import net.javaru.iip.frc.wpilib.WpiLibPaths;
import net.javaru.iip.frc.wpilib.retrieval.WpiLibDownloader;



public class FrcApplicationSettingsForm
{
    private static final Logger LOG = Logger.getInstance(FrcApplicationSettingsForm.class);

    private FrcApplicationSettings internalFrcApplicationSettings = FrcApplicationSettings.Settings.clone(FrcApplicationSettings.Settings.INSTANCE());
    private FrcRoboRioSettings internalFrcRoboRioSettings = FrcRoboRioSettings.Settings.clone(FrcRoboRioSettings.Settings.INSTANCE());
    private FrcSshSettings internalFrcSshSettings = FrcSshSettings.Settings.clone(FrcSshSettings.Settings.INSTANCE());

    private JPanel rootPanel;
    private JPanel generalSettingsPanel;
    private JBTextField teamNumberTextField;
    private JBLabel teamNumberWarningIconLabel;
    private JPanel roboRioPanel;
    private JBTextField roboRioMDnsHostName;
    private JButton mDnsHostNameDefaultValueButton;
    private JBTextField roboRioDnsHostName;
    private JButton dnsHostNameDefaultValueButton;
    private JBTextField roboRioStaticUsbIp;
    private JButton roboRioStaticUsbIpDefaultValueButton;
    private JBTextField roboRioIpAddress;
    private JButton roboRioIpAddressDefaultValueButton;
    private JPanel rioLogOutputPanel;
//    private JPanel udpRioLogSettingsPanel;
//    private JBTextField rioLogPortTextField;
//    private JButton portToDefaultValueButton;
    private JPanel sshRioLogSettingsPanel;
    private JBTextField sshUsername;
    private JBPasswordField sshPassword;
    private JBTextField sshTailCommand;
    private JButton tailCommandToDefaultValueButton;
    private JButton sshUsernameToDefaultButton;
    private JButton sshPasswordToDefaultButton;
    private JBTextField roboRioFieldLocalHostName;
    private JButton fieldLocalHostNameDefaultValueButton;


    public FrcApplicationSettingsForm()
    {
        initForm();
        load(FrcApplicationSettings.Settings.INSTANCE(), 
             FrcRoboRioSettings.Settings.INSTANCE(),
             FrcSshSettings.Settings.INSTANCE());
    }


    private void initForm()
    {
//        initRioLogTargetWindowRadioButtons();
//        initPortTextField();
        initTeamNumberField();
        initRoboRioComponents();
        initSshSettingComponents();

//        portToDefaultValueButton.addActionListener(e -> setUdpPortToDefault());
    }


    public boolean isModified()
    {
        final boolean modified = !FrcApplicationSettings.Settings.INSTANCE().equals(internalFrcApplicationSettings) ||
                                 !FrcRoboRioSettings.Settings.INSTANCE().equals(internalFrcRoboRioSettings) ||
                                 !FrcSshSettings.Settings.INSTANCE().equals(internalFrcSshSettings);
        LOG.trace("[FRC] FrcApplicationSettingsForm.isModified returning " + modified);
        return modified;
    }


    public void load(@NotNull FrcApplicationSettings applicationSettings, 
                     @NotNull FrcRoboRioSettings frcRoboRioSettings,
                     @NotNull FrcSshSettings frcSshSettings)
    {
        internalFrcApplicationSettings = FrcApplicationSettings.Settings.clone(applicationSettings);
        internalFrcRoboRioSettings = FrcRoboRioSettings.Settings.clone(frcRoboRioSettings);
        internalFrcSshSettings = FrcSshSettings.Settings.clone(frcSshSettings);

//        resetRioLogTargetWindowRadioButtons();
        resetTeamNumberTextFieldValue();
        resetRoboRioHostFields();
//        resetPortTextField();
        resetSshSettingsFields();
    }
    
    public synchronized void applyTo(@NotNull FrcApplicationSettings frcApplicationSettings, 
                        @NotNull FrcRoboRioSettings frcRoboRioSettings,
                        @NotNull FrcSshSettings frcSshSettings)
    {
        LOG.debug("[FRC] Before applying form frcApplicationSettings of\n" + internalFrcApplicationSettings + "\nto current/previous frcApplicationSettings of\n" + frcApplicationSettings);
        LOG.debug("[FRC] Before applying form frcRoboRioSettings of\n" + internalFrcRoboRioSettings + "\nto current/previous frcRoboRioSettings of\n" + frcRoboRioSettings);
        LOG.debug("[FRC] Before applying form frcSshSettings of\n" + internalFrcSshSettings + "\nto current/previous frcSshSettings of\n" + frcSshSettings);

        
        // *** APPLY INTERNAL CHANGES TO THE SETTING INSTANCES
        
        final boolean teamNumberHasChanged = frcApplicationSettings.getTeamNumber() != internalFrcApplicationSettings.getTeamNumber();
        frcApplicationSettings.setTeamNumber(internalFrcApplicationSettings.getTeamNumber());
        frcApplicationSettings.setRioLogUdpPort(internalFrcApplicationSettings.getRioLogUdpPort());
        
        frcRoboRioSettings.setRoboRioHost_USB(internalFrcRoboRioSettings.getRoboRioHost_USB());
        frcRoboRioSettings.setRoboRioHost_IP(internalFrcRoboRioSettings.getRoboRioHost_IP());
        frcRoboRioSettings.setRoboRioHost_DNS(internalFrcRoboRioSettings.getRoboRioHost_DNS());
        frcRoboRioSettings.setRoboRioHost_mDNS(internalFrcRoboRioSettings.getRoboRioHost_mDNS());
        
        frcSshSettings.setSshUsername(internalFrcSshSettings.getSshUsername());
        frcSshSettings.setSshPassword(internalFrcSshSettings.getSshPassword());
        frcSshSettings.setSshTailCommand(internalFrcSshSettings.getSshTailCommand());
        
        
        LOG.debug("[FRC] After  applying form frcApplicationSettings of\n" + internalFrcApplicationSettings + "\nto current/previous frcApplicationSettings of\n" + frcApplicationSettings);
        LOG.debug("[FRC] After  applying form frcRoboRioSettings of\n" + internalFrcRoboRioSettings + "\nto current/previous frcRoboRioSettings of\n" + frcRoboRioSettings);
        LOG.debug("[FRC] After  applying form frcSshSettings of\n" + internalFrcSshSettings + "\nto current/previous frcSshSettings of\n" + frcSshSettings);
        
        // ** NO CHANGES TO SETTINGS OBJECTS BELOW THIS
        
        //Reset the internal state to the updated setting
        internalFrcApplicationSettings = FrcApplicationSettings.Settings.clone(frcApplicationSettings);     
        internalFrcRoboRioSettings = FrcRoboRioSettings.Settings.clone(frcRoboRioSettings);

        if (teamNumberHasChanged)
        {
            LOG.info("[FRC] Team number has been changed in settings. Updating the wpilib.properties file");
            ApplicationManager.getApplication().runWriteAction(() -> {
                try
                {
                    WpiLibDownloader.updateOrCreateWpilibPropertiesFile();
                }
                catch (IOException e)
                {
                    final Notification notification =
                        FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP
                            .createNotification("FRC",
                                                "Team Number Update Failure",
                                                "The '" + WpiLibPaths.getWpilibPropertiesFile() + "' file could not be updated with "
                                                + "the change to the team number. You will need to manually update the 'team-number' "
                                                + "property in the file in order for your robot deploys to work. Update Failure Cause: "
                                                + e.toString(),
                                                NotificationType.ERROR);
                    final Project[] projects = ProjectManager.getInstance().getOpenProjects();
                    for (Project project : projects)
                    {
                        if (FrcProjectComponentImpl.isFrcFacetedProject(project))
                        {
                            Notifications.Bus.notify(notification, project);
                        }
                    }
                }
            });
        }
        
        // ** NO CODE BELOW THIS **
    }


    private void resetTeamNumberTextFieldValue()
    {
        final int teamNumber = internalFrcApplicationSettings.getTeamNumber();
        teamNumberTextField.setText(teamNumber <= 0 ? "" : Integer.toString(teamNumber));
        setTeamNumberWarningVisibility(teamNumber <= 0);
    }
    
    private void initTeamNumberField()
    {
        resetTeamNumberTextFieldValue();
        teamNumberTextField.addKeyListener(new KeyListener()
        {

            private String previousText = teamNumberTextField.getText();


            @Override
            public void keyTyped(KeyEvent e) { }


            @Override
            public void keyPressed(KeyEvent e) { }

            
            @Override
            public void keyReleased(KeyEvent e)
            {
                String updatedText = teamNumberTextField.getText();

                if (StringUtils.isBlank(updatedText))
                {
                    previousText = updatedText;
                    setTeamNumberWarningVisibility(true);
                    internalFrcApplicationSettings.setTeamNumber(FrcApplicationSettingsKt.UN_CONFIGURED_TEAM_NUMBER);
                }
                else
                {
                    try
                    {
                        final int teamNum = Integer.parseInt(updatedText);
                        previousText = updatedText;
                        internalFrcApplicationSettings.setTeamNumber(teamNum);
                        setTeamNumberWarningVisibility(false);

                        if (internalFrcRoboRioSettings.isRoboRioHostTheDefault_mDNS()) { roboRioMDnsHostName.setText(internalFrcRoboRioSettings.getRoboRioHostDefault_mDNS(internalFrcApplicationSettings)); }
                        if (internalFrcRoboRioSettings.isRoboRioHostTheDefault_DNS()) { roboRioDnsHostName.setText(internalFrcRoboRioSettings.getRoboRioHostDefault_DNS(internalFrcApplicationSettings)); }
                        if (internalFrcRoboRioSettings.isRoboRioHostTheDefault_FieldLocal()) { roboRioFieldLocalHostName.setText(internalFrcRoboRioSettings.getRoboRioHostDefault_FieldLocal(internalFrcApplicationSettings)); }
                        if (internalFrcRoboRioSettings.isRoboRioHostTheDefault_USB()) { roboRioStaticUsbIp.setText(internalFrcRoboRioSettings.getRoboRioHostDefault_USB()); }
                        if (internalFrcRoboRioSettings.isRoboRioHostTheDefault_IP()) { roboRioIpAddress.setText(internalFrcRoboRioSettings.getRoboRioHostDefault_IP(internalFrcApplicationSettings)); }
                    }
                    catch (NumberFormatException ignore)
                    {
                        //not a valid integer....
                        teamNumberTextField.setText(previousText);
                        setTeamNumberWarningVisibility(true);
                    }
                }
            }
        });


        teamNumberTextField.setInputVerifier(new InputVerifier()
        {
            @Override
            public boolean verify(JComponent input)
            {
                final JTextField textField = (JTextField) input;
                final String text = textField.getText();
                return isEnteredTeamNumberValid(text);
            }
        });
    }

    
//    private void initPortTextField()
//    {
//        resetPortTextField();
//
//        rioLogPortTextField.addFocusListener(new FocusListener()
//        {
//            @Override
//            public void focusGained(FocusEvent e) { }
//
//
//            @Override
//            public void focusLost(FocusEvent e)
//            {
//                final String text = rioLogPortTextField.getText();
//                try
//                {
//                    internalFrcApplicationSettings.setRioLogUdpPort(DecimalFormat.getIntegerInstance().parse(text).intValue());
//                }
//                catch (ParseException e1)
//                {
//                    LOG.warn("[FRC] Could not parse the value '" + text + "' as an integer. Setting field and port to default value.");
//                    setUdpPortToDefault();
//                }
//            }
//        });
//
//        rioLogPortTextField.setInputVerifier(new InputVerifier()
//        {
//            @Override
//            public boolean verify(JComponent input)
//            {
//                final JTextField textField = (JTextField) input;
//                final String text = textField.getText();
//                try
//                {
//                    final Number number = DecimalFormat.getIntegerInstance().parse(text);
//                    final int i = number.intValue();
//                    return (i >= 0 && i <= 65_535);
//                }
//                catch (ParseException e)
//                {
//                    return false;
//                }
//            }
//        });
//
//        rioLogPortTextField.addKeyListener(new KeyListener()
//        {
//
//            private String previousText = rioLogPortTextField.getText();
//            private NumberFormat formatter = DecimalFormat.getIntegerInstance();
//
//
//            public void keyTyped(KeyEvent e) { }
//
//
//            @Override
//            public void keyPressed(KeyEvent e) { }
//
//
//            @Override
//            public void keyReleased(KeyEvent e)
//            {
//                String text = rioLogPortTextField.getText();
//                if (!StringUtils.isNotBlank(text))
//                {
//                    previousText = text;
//                }
//                else
//                {
//                    try
//                    {
//                        final Number number = formatter.parse(text);
//                        String formattedText = formatter.format(number);
//                        rioLogPortTextField.setText(formattedText);
//                        previousText = formattedText;
//                        internalFrcApplicationSettings.setRioLogUdpPort(number.intValue());
//                    }
//                    catch (ParseException ignore)
//                    {
//                        //not a valid integer....
//                        rioLogPortTextField.setText(previousText);
//                    }
//                }
//            }
//        });
//    }
//
//
//    private void resetPortTextField()
//    {
//        rioLogPortTextField.setText(DecimalFormat.getIntegerInstance().format(internalFrcApplicationSettings.getRioLogUdpPort()));
//    }


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
        mDnsHostNameDefaultValueButton.addActionListener(e ->
                                                         {
                                                             final String defaultValue = internalFrcRoboRioSettings.getRoboRioHostDefault_mDNS(internalFrcApplicationSettings);
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
        dnsHostNameDefaultValueButton.addActionListener(e ->
                                                        {
                                                            final String defaultValue = internalFrcRoboRioSettings.getRoboRioHostDefault_DNS(internalFrcApplicationSettings);
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
        fieldLocalHostNameDefaultValueButton.addActionListener(e ->
                                                        {
                                                            final String defaultValue = internalFrcRoboRioSettings.getRoboRioHostDefault_FieldLocal(internalFrcApplicationSettings);
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
        roboRioIpAddressDefaultValueButton.addActionListener(e ->
                                                             {
                                                                 final String defaultValue = internalFrcRoboRioSettings.getRoboRioHostDefault_IP(internalFrcApplicationSettings);
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
        roboRioStaticUsbIpDefaultValueButton.addActionListener(e ->
                                                               {
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
        sshUsernameToDefaultButton.addActionListener(e ->
                                                     {
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
                internalFrcSshSettings.setSshPassword(sshPassword.getText());
            }
        });
        sshPasswordToDefaultButton.addActionListener(e ->
                                                     {
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
        tailCommandToDefaultValueButton.addActionListener(e ->
                                                          {
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


//    private void setUdpPortToDefault()
//    {
//        rioLogPortTextField.setText(DecimalFormat.getIntegerInstance().format(FrcApplicationSettingsKt.DEFAULT_RIO_LOG_UDP_PORT));
//        internalFrcApplicationSettings.setRioLogUdpPort(FrcApplicationSettingsKt.DEFAULT_RIO_LOG_UDP_PORT);
//    }


//    private void initRioLogTargetWindowRadioButtons()
//    {
//        resetRioLogTargetWindowRadioButtons();
//
//        targetWindowIsRunWindowRadioButton.setActionCommand(RioLogTargetWindowActionCommands.RunWindow.name());
//        targetWindowIsFrcToolWindowRadioButton.setActionCommand(RioLogTargetWindowActionCommands.FrcWindow.name());
//        final Enumeration<AbstractButton> buttons = rioLogTargetWindowButtonGroup.getElements();
//        while (buttons.hasMoreElements())
//        {
//            AbstractButton button = buttons.nextElement();
//            button.addActionListener(e ->
//                                     {
//                                         final ButtonModel selectedModel = rioLogTargetWindowButtonGroup.getSelection();
//                                         final String actionCommandString = selectedModel.getActionCommand();
//                                         final RioLogTargetWindowActionCommands actionCommand = RioLogTargetWindowActionCommands.valueOf(actionCommandString);
//                                         switch (actionCommand)
//                                         {
//                                             case FrcWindow:
//                                                 frcSettings.setUseFrcToolWindow(true);
//                                                 break;
//                                             case RunWindow:
//                                                 frcSettings.setUseFrcToolWindow(false);
//                                         }
//                                     });
//        }
//        
//    }
//
//    
//    private void resetRioLogTargetWindowRadioButtons()
//    {
//        if (internalFrcApplicationSettings.getUseFrcToolWindow())
//        {
//            rioLogTargetWindowButtonGroup.setSelected(targetWindowIsFrcToolWindowRadioButton.getModel(), true);
//        }
//        else
//        {
//            rioLogTargetWindowButtonGroup.setSelected(targetWindowIsRunWindowRadioButton.getModel(), true);
//        }
//    }
    private boolean isEnteredTeamNumberValid(String text)
    {
        try
        {
            final Number number = DecimalFormat.getIntegerInstance().parse(text);
            final int i = number.intValue();
            return (i > 0);
        }
        catch (ParseException e)
        {
            return false;
        }
    }


    private void setTeamNumberWarningVisibility(boolean isVisible)
    {
        teamNumberWarningIconLabel.setVisible(isVisible);
    }
    
    public JPanel getRootPanel() { return rootPanel; }
}
