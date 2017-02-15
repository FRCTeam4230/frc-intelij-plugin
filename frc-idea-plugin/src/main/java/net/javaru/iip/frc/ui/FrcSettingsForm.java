/*
 * Copyright 2015 Mark Vedder
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

package net.javaru.iip.frc.ui;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Enumeration;
import javax.swing.*;

import org.apache.commons.lang3.StringUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import net.javaru.iip.frc.roboRIO.riolog.RioLogGlobals;
import net.javaru.iip.frc.settings.FrcSettings;



public class FrcSettingsForm
{
    private static final Logger LOG = Logger.getInstance(FrcSettingsForm.class);
    private FrcSettings frcSettings;
    private JPanel rootPanel;
    private JRadioButton targetWindowIsFrcToolWindowRadioButton;
    private JRadioButton targetWindowIsRunWindowRadioButton;
    private JTextField rioLogPortTextField;
    private JButton portToDefaultValueButton;
    private JBTextField teamNumberTextField;
    private JBTextField sshTailUsername;
    private JBTextField sshTailPassword;
    private JBTextField roboRioMdnsHostName;
    private JBTextField sshTailCommand;
    private JButton tailCommandToDefaultValueButton;
    private JButton mDnsHostNameDefaultValueButton;
    private JButton dnsHostNameDefaultValueButton;
    private JBTextField roboRioDnsHostName;
    private JBTextField roboRioStaticUsbIp;
    private JButton roboRioStaticUsbIpDefaultValueButton;
    private JBTextField roboRioIpAddress;
    private JButton roboRioIpAddressDefaultValueButton;
    private JPanel rioLogOutputPanel;
    private JPanel sshTailSettingsPanel;
    private JPanel generalPanel;
    private JPanel roboRioPanel;
    private JBLabel teamNumberWarningIconLabel;
    private ButtonGroup rioLogTargetWindowButtonGroup;


    public FrcSettingsForm(FrcSettings frcSettings)
    {
        this.frcSettings = frcSettings;
        $$$setupUI$$$();
        initForm();
    }


    private void initForm()
    {
        configureTargetWindowRadioButtons();
        configurePortTextField();
        configureTeamNumberField();
        configureRoboRioComponents();
        configureSshSettingComponents();

        portToDefaultValueButton.addActionListener(e -> setPortToDefault());
    }


    private void configureTeamNumberField()
    {
        String teamNum = frcSettings.getTeamNumber() <= 0 ? "" : Integer.toString(frcSettings.getTeamNumber());
        teamNumberTextField.setText(teamNum);
        setTeamNumberWarningVisibility(!frcSettings.isTeamNumberConfigured());

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
                }
                else
                {
                    try
                    {
                        final int teamNum = Integer.parseInt(updatedText);
                        previousText = updatedText;
                        frcSettings.setTeamNumber(teamNum);
                        setTeamNumberWarningVisibility(false);

                        if (frcSettings.isRoboRioHostMDnsTheDefault()) { roboRioMdnsHostName.setText(frcSettings.getDefaultRoboRioHost_mDNS()); }
                        if (frcSettings.isRoboRioHostDnsTheDefault()) { roboRioDnsHostName.setText(frcSettings.getDefaultRoboRioHost_DNS()); }
                        if (frcSettings.isRoboRioHostUsbTheDefault()) { roboRioStaticUsbIp.setText(frcSettings.getDefaultRoboRioHost_USB()); }
                        if (frcSettings.isRoboRioHostIpTheDefault()) { roboRioIpAddress.setText(frcSettings.getDefaultRoboRioHost_IP()); }

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


    private void setTeamNumberWarningVisibility(boolean isVisible)
    {
        teamNumberWarningIconLabel.setVisible(isVisible);
    }


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


    private void setPortToDefault()
    {
        rioLogPortTextField.setText(DecimalFormat.getIntegerInstance().format(FrcSettings.DEFAULT_RIO_LOG_PORT));
        frcSettings.setRioLogPort(FrcSettings.DEFAULT_RIO_LOG_PORT);
    }


    private void configureRoboRioComponents()
    {
        // TODO should we add verifiers ?

        roboRioMdnsHostName.setText(frcSettings.getRoboRioHostMDns());
        roboRioMdnsHostName.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) { }


            @Override
            public void keyPressed(KeyEvent e) { }


            @Override
            public void keyReleased(KeyEvent e)
            {
                frcSettings.setRoboRioHostMDns(roboRioMdnsHostName.getText());
            }
        });
        mDnsHostNameDefaultValueButton.addActionListener(e ->
                                                         {
                                                             final String defaultValue = frcSettings.getDefaultRoboRioHost_mDNS();
                                                             roboRioMdnsHostName.setText(defaultValue);
                                                             frcSettings.setRoboRioHostMDns(defaultValue);
                                                         });


        roboRioDnsHostName.setText(frcSettings.getRoboRioHostDns());
        roboRioDnsHostName.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) { }


            @Override
            public void keyPressed(KeyEvent e) { }


            @Override
            public void keyReleased(KeyEvent e)
            {
                frcSettings.setRoboRioHostDns(roboRioDnsHostName.getText());
            }
        });
        dnsHostNameDefaultValueButton.addActionListener(e ->
                                                        {
                                                            final String defaultValue = frcSettings.getDefaultRoboRioHost_DNS();
                                                            roboRioDnsHostName.setText(defaultValue);
                                                            frcSettings.setRoboRioHostDns(defaultValue);
                                                        });

        roboRioIpAddress.setText(frcSettings.getRoboRioHostIp());
        roboRioIpAddress.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) { }


            @Override
            public void keyPressed(KeyEvent e) { }


            @Override
            public void keyReleased(KeyEvent e)
            {
                frcSettings.setRoboRioHostIp(roboRioIpAddress.getText());
            }
        });
        roboRioIpAddressDefaultValueButton.addActionListener(e ->
                                                             {
                                                                 final String defaultValue = frcSettings.getDefaultRoboRioHost_IP();
                                                                 roboRioIpAddress.setText(defaultValue);
                                                                 frcSettings.setRoboRioHostIp(defaultValue);
                                                             });

        roboRioStaticUsbIp.setText(frcSettings.getRoboRioHostUsb());
        roboRioStaticUsbIp.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) { }


            @Override
            public void keyPressed(KeyEvent e) { }


            @Override
            public void keyReleased(KeyEvent e)
            {
                frcSettings.setRoboRioHostUsb(roboRioStaticUsbIp.getText());
            }
        });
        roboRioStaticUsbIpDefaultValueButton.addActionListener(e ->
                                                               {
                                                                   final String defaultValue = frcSettings.getDefaultRoboRioHost_USB();
                                                                   roboRioStaticUsbIp.setText(defaultValue);
                                                                   frcSettings.setRoboRioHostUsb(defaultValue);
                                                               });

    }


    private void configureSshSettingComponents()
    {
        // TODO should we add verifiers ?

        sshTailUsername.setText(frcSettings.getSshTailUsername());
        sshTailPassword.setText(frcSettings.getSshTailPassword());
        sshTailCommand.setText(frcSettings.getSshTailCommand());

        sshTailUsername.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) { }


            @Override
            public void keyPressed(KeyEvent e) { }


            @Override
            public void keyReleased(KeyEvent e)
            {
                frcSettings.setSshTailUsername(sshTailUsername.getText());
            }
        });

        sshTailPassword.addKeyListener(new KeyListener()
        {
            @Override
            public void keyTyped(KeyEvent e) { }


            @Override
            public void keyPressed(KeyEvent e) { }


            @Override
            public void keyReleased(KeyEvent e)
            {
                frcSettings.setSshTailPassword(sshTailPassword.getText());
            }
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
                frcSettings.setSshTailCommand(sshTailCommand.getText());
            }
        });
        tailCommandToDefaultValueButton.addActionListener(e ->
                                                          {
                                                              sshTailCommand.setText(RioLogGlobals.DEFAULT_TAIL_COMMAND);
                                                              frcSettings.setSshTailCommand(RioLogGlobals.DEFAULT_TAIL_COMMAND);
                                                          });
    }


    private void configureTargetWindowRadioButtons()
    {
        if (frcSettings.isUseFrcToolWindow())
        {
            rioLogTargetWindowButtonGroup.setSelected(targetWindowIsFrcToolWindowRadioButton.getModel(), true);
        }
        else
        {
            rioLogTargetWindowButtonGroup.setSelected(targetWindowIsRunWindowRadioButton.getModel(), true);
        }

        targetWindowIsRunWindowRadioButton.setActionCommand(RioLogTargetWindowActionCommands.RunWindow.name());
        targetWindowIsFrcToolWindowRadioButton.setActionCommand(RioLogTargetWindowActionCommands.FrcWindow.name());
        final Enumeration<AbstractButton> buttons = rioLogTargetWindowButtonGroup.getElements();
        while (buttons.hasMoreElements())
        {
            AbstractButton button = buttons.nextElement();
            button.addActionListener(e ->
                                     {
                                         final ButtonModel selectedModel = rioLogTargetWindowButtonGroup.getSelection();
                                         final String actionCommandString = selectedModel.getActionCommand();
                                         final RioLogTargetWindowActionCommands actionCommand = RioLogTargetWindowActionCommands.valueOf(actionCommandString);
                                         switch (actionCommand)
                                         {
                                             case FrcWindow:
                                                 frcSettings.setUseFrcToolWindow(true);
                                                 break;
                                             case RunWindow:
                                                 frcSettings.setUseFrcToolWindow(false);
                                         }
                                     });
        }
    }


    private void configurePortTextField()
    {
        rioLogPortTextField.setText(DecimalFormat.getIntegerInstance().format(frcSettings.getRioLogPort()));


        rioLogPortTextField.addFocusListener(new FocusListener()
        {
            @Override
            public void focusGained(FocusEvent e) { }


            @Override
            public void focusLost(FocusEvent e)
            {
                final String text = rioLogPortTextField.getText();
                try
                {
                    frcSettings.setRioLogPort(DecimalFormat.getIntegerInstance().parse(text).intValue());
                }
                catch (ParseException e1)
                {
                    LOG.warn("[FRC] Could not parse the value '" + text + "' as an integer. Setting field and port to default value.");
                    setPortToDefault();
                }
            }
        });

        rioLogPortTextField.setInputVerifier(new InputVerifier()
        {
            @Override
            public boolean verify(JComponent input)
            {
                final JTextField textField = (JTextField) input;
                final String text = textField.getText();
                try
                {
                    final Number number = DecimalFormat.getIntegerInstance().parse(text);
                    final int i = number.intValue();
                    return (i >= 0 && i <= 65_535);
                }
                catch (ParseException e)
                {
                    return false;
                }
            }
        });

        rioLogPortTextField.addKeyListener(new KeyListener()
        {

            private String previousText = rioLogPortTextField.getText();
            private NumberFormat formatter = DecimalFormat.getIntegerInstance();


            public void keyTyped(KeyEvent e) { }


            @Override
            public void keyPressed(KeyEvent e) { }


            @Override
            public void keyReleased(KeyEvent e)
            {
                String text = rioLogPortTextField.getText();
                if (!StringUtils.isNotBlank(text))
                {
                    previousText = text;
                }
                else
                {
                    try
                    {
                        final Number number = formatter.parse(text);
                        String formattedText = formatter.format(number);
                        rioLogPortTextField.setText(formattedText);
                        previousText = formattedText;
                        frcSettings.setRioLogPort(number.intValue());
                    }
                    catch (ParseException ignore)
                    {
                        //not a valid integer....
                        rioLogPortTextField.setText(previousText);
                    }
                }
            }
        });
    }


    private void createUIComponents()
    {
    }


    public JPanel getRootComponent()
    {
        return rootPanel;
    }


    public void importFrom(FrcSettings frcSettings)
    {
        this.frcSettings = frcSettings;
        initForm();
    }


    public boolean isSettingsModified(FrcSettings data)
    {
        return !this.frcSettings.equals(data);
    }


    public FrcSettings getFrcSettings()
    {
        return frcSettings;
    }


    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        rootPanel = new JPanel();
        rootPanel.setLayout(new GridLayoutManager(4, 1, new Insets(0, 0, 0, 0), -1, -1));
        rioLogOutputPanel = new JPanel();
        rioLogOutputPanel.setLayout(new GridLayoutManager(6, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(rioLogOutputPanel,
                      new GridConstraints(2,
                                          0,
                                          1,
                                          1,
                                          GridConstraints.ANCHOR_CENTER,
                                          GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          null,
                                          null,
                                          null,
                                          0,
                                          false));
        rioLogOutputPanel.setBorder(BorderFactory.createTitledBorder("RIO Log Output"));
        targetWindowIsFrcToolWindowRadioButton = new JRadioButton();
        targetWindowIsFrcToolWindowRadioButton.setText("FRC Tool Window");
        targetWindowIsFrcToolWindowRadioButton.setMnemonic('F');
        targetWindowIsFrcToolWindowRadioButton.setDisplayedMnemonicIndex(0);
        targetWindowIsFrcToolWindowRadioButton.setToolTipText("RIO Log output will appear in a dedicated FRC Tool Window");
        rioLogOutputPanel.add(targetWindowIsFrcToolWindowRadioButton,
                              new GridConstraints(2,
                                                  0,
                                                  1,
                                                  2,
                                                  GridConstraints.ANCHOR_WEST,
                                                  GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                  GridConstraints.SIZEPOLICY_FIXED,
                                                  null,
                                                  null,
                                                  null,
                                                  2,
                                                  false));
        targetWindowIsRunWindowRadioButton = new JRadioButton();
        targetWindowIsRunWindowRadioButton.setText("Run Window");
        targetWindowIsRunWindowRadioButton.setMnemonic('R');
        targetWindowIsRunWindowRadioButton.setDisplayedMnemonicIndex(0);
        targetWindowIsRunWindowRadioButton.setToolTipText("RIO Log output will appear as a tab in the IDEA Run tool window.");
        rioLogOutputPanel.add(targetWindowIsRunWindowRadioButton,
                              new GridConstraints(3,
                                                  0,
                                                  1,
                                                  1,
                                                  GridConstraints.ANCHOR_WEST,
                                                  GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                  GridConstraints.SIZEPOLICY_FIXED,
                                                  null,
                                                  null,
                                                  null,
                                                  2,
                                                  false));
        final JLabel label1 = new JLabel();
        label1.setText("Target Tool Window:");
        rioLogOutputPanel.add(label1,
                              new GridConstraints(1,
                                                  0,
                                                  1,
                                                  1,
                                                  GridConstraints.ANCHOR_WEST,
                                                  GridConstraints.FILL_NONE,
                                                  GridConstraints.SIZEPOLICY_FIXED,
                                                  GridConstraints.SIZEPOLICY_FIXED,
                                                  null,
                                                  null,
                                                  null,
                                                  1,
                                                  false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        rioLogOutputPanel.add(panel1,
                              new GridConstraints(4,
                                                  0,
                                                  1,
                                                  1,
                                                  GridConstraints.ANCHOR_CENTER,
                                                  GridConstraints.FILL_BOTH,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                  null,
                                                  null,
                                                  null,
                                                  0,
                                                  false));
        final JLabel label2 = new JLabel();
        label2.setText("Net Console UDP Port:");
        label2.setToolTipText("UDP Port (0 to 65,535) of the roboRIO for logging.");
        panel1.add(label2,
                   new GridConstraints(0,
                                       0,
                                       1,
                                       1,
                                       GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       null,
                                       null,
                                       null,
                                       1,
                                       false));
        rioLogPortTextField = new JTextField();
        rioLogPortTextField.setEnabled(true);
        rioLogPortTextField.setToolTipText("UDP Port (0 to 65,535) of the roboRIO for logging.");
        panel1.add(rioLogPortTextField,
                   new GridConstraints(0,
                                       1,
                                       1,
                                       1,
                                       GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_HORIZONTAL,
                                       GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       new Dimension(50, -1),
                                       new Dimension(75, -1),
                                       new Dimension(200, -1),
                                       0,
                                       false));
        portToDefaultValueButton = new JButton();
        portToDefaultValueButton.setHideActionText(false);
        portToDefaultValueButton.setText("Default");
        portToDefaultValueButton.setToolTipText("Sets the RIO Log port to its default value");
        panel1.add(portToDefaultValueButton,
                   new GridConstraints(0,
                                       2,
                                       1,
                                       1,
                                       GridConstraints.ANCHOR_CENTER,
                                       GridConstraints.FILL_HORIZONTAL,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       null,
                                       null,
                                       null,
                                       0,
                                       false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1,
                   new GridConstraints(0,
                                       3,
                                       1,
                                       1,
                                       GridConstraints.ANCHOR_CENTER,
                                       GridConstraints.FILL_HORIZONTAL,
                                       GridConstraints.SIZEPOLICY_WANT_GROW,
                                       1,
                                       null,
                                       null,
                                       null,
                                       0,
                                       false));
        final Spacer spacer2 = new Spacer();
        rioLogOutputPanel.add(spacer2,
                              new GridConstraints(0,
                                                  0,
                                                  1,
                                                  1,
                                                  GridConstraints.ANCHOR_CENTER,
                                                  GridConstraints.FILL_VERTICAL,
                                                  1,
                                                  GridConstraints.SIZEPOLICY_FIXED,
                                                  new Dimension(-1, 5),
                                                  new Dimension(-1, 5),
                                                  new Dimension(-1, 5),
                                                  0,
                                                  false));
        sshTailSettingsPanel = new JPanel();
        sshTailSettingsPanel.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
        rioLogOutputPanel.add(sshTailSettingsPanel,
                              new GridConstraints(5,
                                                  0,
                                                  1,
                                                  1,
                                                  GridConstraints.ANCHOR_CENTER,
                                                  GridConstraints.FILL_BOTH,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                  GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                  null,
                                                  null,
                                                  null,
                                                  1,
                                                  false));
        sshTailSettingsPanel.setBorder(BorderFactory.createTitledBorder("SSH Tail Settings"));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        sshTailSettingsPanel.add(panel2,
                                 new GridConstraints(0,
                                                     0,
                                                     1,
                                                     1,
                                                     GridConstraints.ANCHOR_CENTER,
                                                     GridConstraints.FILL_BOTH,
                                                     GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                     GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                                     null,
                                                     null,
                                                     null,
                                                     0,
                                                     false));
        final JBLabel jBLabel1 = new JBLabel();
        jBLabel1.setText("User name:");
        jBLabel1.setToolTipText("roboRIO user name for SSH access for tailing logs. Default is 'admin'.");
        panel2.add(jBLabel1,
                   new GridConstraints(0,
                                       0,
                                       1,
                                       1,
                                       GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                       null,
                                       null,
                                       null,
                                       2,
                                       false));
        sshTailUsername = new JBTextField();
        sshTailUsername.setToolTipText("roboRIO user name for SSH access for tailing logs. Default is 'admin'.");
        panel2.add(sshTailUsername,
                   new GridConstraints(0,
                                       1,
                                       1,
                                       1,
                                       GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       new Dimension(50, -1),
                                       new Dimension(125, -1),
                                       new Dimension(400, -1),
                                       0,
                                       false));
        final JBLabel jBLabel2 = new JBLabel();
        jBLabel2.setText("Password:");
        jBLabel2.setToolTipText("roboRIO password for SSH access for tailing logs. Default is blank (i.e. no password).");
        panel2.add(jBLabel2,
                   new GridConstraints(1,
                                       0,
                                       1,
                                       1,
                                       GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                       null,
                                       null,
                                       null,
                                       2,
                                       false));
        sshTailPassword = new JBTextField();
        sshTailPassword.setToolTipText("roboRIO password for SSH access for tailing logs. Default is blank (i.e. no password).");
        panel2.add(sshTailPassword,
                   new GridConstraints(1,
                                       1,
                                       1,
                                       1,
                                       GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       new Dimension(50, -1),
                                       new Dimension(125, -1),
                                       new Dimension(400, -1),
                                       0,
                                       false));
        final JBLabel jBLabel3 = new JBLabel();
        jBLabel3.setText("Tail Command:");
        panel2.add(jBLabel3,
                   new GridConstraints(2,
                                       0,
                                       1,
                                       1,
                                       GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                       null,
                                       null,
                                       null,
                                       2,
                                       false));
        sshTailCommand = new JBTextField();
        panel2.add(sshTailCommand,
                   new GridConstraints(2,
                                       1,
                                       1,
                                       1,
                                       GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       new Dimension(250, -1),
                                       new Dimension(325, -1),
                                       new Dimension(500, -1),
                                       0,
                                       false));
        tailCommandToDefaultValueButton = new JButton();
        tailCommandToDefaultValueButton.setText("Default");
        tailCommandToDefaultValueButton.setToolTipText("Sets the Tail command to its default value");
        panel2.add(tailCommandToDefaultValueButton,
                   new GridConstraints(2,
                                       2,
                                       1,
                                       1,
                                       GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_NONE,
                                       GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       null,
                                       null,
                                       null,
                                       0,
                                       false));
        final Spacer spacer3 = new Spacer();
        sshTailSettingsPanel.add(spacer3,
                                 new GridConstraints(0,
                                                     1,
                                                     1,
                                                     1,
                                                     GridConstraints.ANCHOR_CENTER,
                                                     GridConstraints.FILL_HORIZONTAL,
                                                     GridConstraints.SIZEPOLICY_WANT_GROW,
                                                     1,
                                                     null,
                                                     null,
                                                     null,
                                                     0,
                                                     false));
        final Spacer spacer4 = new Spacer();
        sshTailSettingsPanel.add(spacer4,
                                 new GridConstraints(1,
                                                     0,
                                                     1,
                                                     1,
                                                     GridConstraints.ANCHOR_CENTER,
                                                     GridConstraints.FILL_VERTICAL,
                                                     1,
                                                     GridConstraints.SIZEPOLICY_WANT_GROW,
                                                     null,
                                                     null,
                                                     null,
                                                     0,
                                                     false));
        final Spacer spacer5 = new Spacer();
        rootPanel.add(spacer5,
                      new GridConstraints(3,
                                          0,
                                          1,
                                          1,
                                          GridConstraints.ANCHOR_CENTER,
                                          GridConstraints.FILL_VERTICAL,
                                          1,
                                          GridConstraints.SIZEPOLICY_WANT_GROW,
                                          null,
                                          null,
                                          null,
                                          0,
                                          false));
        generalPanel = new JPanel();
        generalPanel.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(generalPanel,
                      new GridConstraints(0,
                                          0,
                                          1,
                                          1,
                                          GridConstraints.ANCHOR_CENTER,
                                          GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          null,
                                          null,
                                          null,
                                          0,
                                          false));
        generalPanel.setBorder(BorderFactory.createTitledBorder("General"));
        final JBLabel jBLabel4 = new JBLabel();
        jBLabel4.setFont(new Font(jBLabel4.getFont().getName(), Font.BOLD, jBLabel4.getFont().getSize()));
        jBLabel4.setText("Team Number:");
        jBLabel4.setDisplayedMnemonic('T');
        jBLabel4.setDisplayedMnemonicIndex(0);
        jBLabel4.setToolTipText("Enter your FRC team number");
        generalPanel.add(jBLabel4,
                         new GridConstraints(0,
                                             0,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_CENTER,
                                             GridConstraints.FILL_NONE,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             null,
                                             null,
                                             null,
                                             0,
                                             false));
        final Spacer spacer6 = new Spacer();
        generalPanel.add(spacer6,
                         new GridConstraints(0,
                                             3,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_CENTER,
                                             GridConstraints.FILL_HORIZONTAL,
                                             GridConstraints.SIZEPOLICY_WANT_GROW,
                                             1,
                                             null,
                                             null,
                                             null,
                                             0,
                                             false));
        teamNumberTextField = new JBTextField();
        generalPanel.add(teamNumberTextField,
                         new GridConstraints(0,
                                             1,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_CENTER,
                                             GridConstraints.FILL_NONE,
                                             GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_FIXED,
                                             new Dimension(50, -1),
                                             new Dimension(75, -1),
                                             new Dimension(150, -1),
                                             0,
                                             false));
        teamNumberWarningIconLabel = new JBLabel();
        teamNumberWarningIconLabel.setIcon(new ImageIcon(getClass().getResource("/general/balloonError.png")));
        teamNumberWarningIconLabel.setText("");
        teamNumberWarningIconLabel.setToolTipText("Please configure your FRC team number");
        generalPanel.add(teamNumberWarningIconLabel,
                         new GridConstraints(0,
                                             2,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_CENTER,
                                             GridConstraints.FILL_NONE,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             null,
                                             null,
                                             null,
                                             0,
                                             false));
        roboRioPanel = new JPanel();
        roboRioPanel.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(roboRioPanel,
                      new GridConstraints(1,
                                          0,
                                          1,
                                          1,
                                          GridConstraints.ANCHOR_CENTER,
                                          GridConstraints.FILL_BOTH,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                          null,
                                          null,
                                          null,
                                          0,
                                          false));
        roboRioPanel.setBorder(BorderFactory.createTitledBorder("roboRIO"));
        final JBLabel jBLabel5 = new JBLabel();
        jBLabel5.setText("mDNS Host Name:");
        jBLabel5.setToolTipText("robRio mDNS Host name");
        roboRioPanel.add(jBLabel5,
                         new GridConstraints(0,
                                             0,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_WEST,
                                             GridConstraints.FILL_NONE,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             null,
                                             null,
                                             null,
                                             2,
                                             false));
        roboRioMdnsHostName = new JBTextField();
        roboRioMdnsHostName.setText("");
        roboRioMdnsHostName.setToolTipText("robRio Host (mDNS) name or IP");
        roboRioPanel.add(roboRioMdnsHostName,
                         new GridConstraints(0,
                                             1,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_WEST,
                                             GridConstraints.FILL_NONE,
                                             GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_FIXED,
                                             new Dimension(100, -1),
                                             new Dimension(250, -1),
                                             new Dimension(500, -1),
                                             0,
                                             false));
        mDnsHostNameDefaultValueButton = new JButton();
        mDnsHostNameDefaultValueButton.setText("Default");
        roboRioPanel.add(mDnsHostNameDefaultValueButton,
                         new GridConstraints(0,
                                             2,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_WEST,
                                             GridConstraints.FILL_NONE,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_FIXED,
                                             null,
                                             null,
                                             null,
                                             0,
                                             false));
        final JBLabel jBLabel6 = new JBLabel();
        jBLabel6.setText("DNS Host Name:");
        roboRioPanel.add(jBLabel6,
                         new GridConstraints(1,
                                             0,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_WEST,
                                             GridConstraints.FILL_NONE,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             null,
                                             null,
                                             null,
                                             2,
                                             false));
        roboRioDnsHostName = new JBTextField();
        roboRioPanel.add(roboRioDnsHostName,
                         new GridConstraints(1,
                                             1,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_WEST,
                                             GridConstraints.FILL_NONE,
                                             GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_FIXED,
                                             new Dimension(100, -1),
                                             new Dimension(250, -1),
                                             new Dimension(500, -1),
                                             0,
                                             false));
        dnsHostNameDefaultValueButton = new JButton();
        dnsHostNameDefaultValueButton.setText("Default");
        roboRioPanel.add(dnsHostNameDefaultValueButton,
                         new GridConstraints(1,
                                             2,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_WEST,
                                             GridConstraints.FILL_NONE,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_FIXED,
                                             null,
                                             null,
                                             null,
                                             0,
                                             false));
        final Spacer spacer7 = new Spacer();
        roboRioPanel.add(spacer7,
                         new GridConstraints(0,
                                             3,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_CENTER,
                                             GridConstraints.FILL_HORIZONTAL,
                                             GridConstraints.SIZEPOLICY_WANT_GROW,
                                             1,
                                             null,
                                             null,
                                             null,
                                             0,
                                             false));
        final JBLabel jBLabel7 = new JBLabel();
        jBLabel7.setText("USB Static IP:");
        roboRioPanel.add(jBLabel7,
                         new GridConstraints(2,
                                             0,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_WEST,
                                             GridConstraints.FILL_NONE,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             null,
                                             null,
                                             null,
                                             2,
                                             false));
        roboRioStaticUsbIp = new JBTextField();
        roboRioPanel.add(roboRioStaticUsbIp,
                         new GridConstraints(2,
                                             1,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_WEST,
                                             GridConstraints.FILL_NONE,
                                             GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             new Dimension(100, -1),
                                             new Dimension(250, -1),
                                             new Dimension(500, -1),
                                             0,
                                             false));
        roboRioStaticUsbIpDefaultValueButton = new JButton();
        roboRioStaticUsbIpDefaultValueButton.setText("Default");
        roboRioPanel.add(roboRioStaticUsbIpDefaultValueButton,
                         new GridConstraints(2,
                                             2,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_WEST,
                                             GridConstraints.FILL_NONE,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_FIXED,
                                             null,
                                             null,
                                             null,
                                             0,
                                             false));
        final JBLabel jBLabel8 = new JBLabel();
        jBLabel8.setText("IP Address:");
        roboRioPanel.add(jBLabel8,
                         new GridConstraints(3,
                                             0,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_WEST,
                                             GridConstraints.FILL_NONE,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             null,
                                             null,
                                             null,
                                             2,
                                             false));
        roboRioIpAddress = new JBTextField();
        roboRioPanel.add(roboRioIpAddress,
                         new GridConstraints(3,
                                             1,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_WEST,
                                             GridConstraints.FILL_NONE,
                                             GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             new Dimension(100, -1),
                                             new Dimension(250, -1),
                                             new Dimension(500, -1),
                                             0,
                                             false));
        roboRioIpAddressDefaultValueButton = new JButton();
        roboRioIpAddressDefaultValueButton.setText("Default");
        roboRioPanel.add(roboRioIpAddressDefaultValueButton,
                         new GridConstraints(3,
                                             2,
                                             1,
                                             1,
                                             GridConstraints.ANCHOR_WEST,
                                             GridConstraints.FILL_NONE,
                                             GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                             GridConstraints.SIZEPOLICY_FIXED,
                                             null,
                                             null,
                                             null,
                                             0,
                                             false));
        label2.setLabelFor(rioLogPortTextField);
        jBLabel4.setLabelFor(rioLogPortTextField);
        rioLogTargetWindowButtonGroup = new ButtonGroup();
        rioLogTargetWindowButtonGroup.add(targetWindowIsFrcToolWindowRadioButton);
        rioLogTargetWindowButtonGroup.add(targetWindowIsRunWindowRadioButton);
    }


    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$() { return rootPanel; }


    private enum RioLogTargetWindowActionCommands
    {
        FrcWindow,
        RunWindow
    }

}
