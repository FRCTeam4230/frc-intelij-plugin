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
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

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

        portToDefaultValueButton.addActionListener(e -> setPortToDefault());
    }


    private void setPortToDefault()
    {
        rioLogPortTextField.setText(DecimalFormat.getIntegerInstance().format(FrcSettings.DEFAULT_RIO_LOG_PORT));
        frcSettings.setRioLogPort(FrcSettings.DEFAULT_RIO_LOG_PORT);
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
            button.addActionListener(e -> {
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
        rootPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(5, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel1,
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
        panel1.setBorder(BorderFactory.createTitledBorder("RIO Log Output"));
        targetWindowIsFrcToolWindowRadioButton = new JRadioButton();
        targetWindowIsFrcToolWindowRadioButton.setText("FRC Tool Window");
        targetWindowIsFrcToolWindowRadioButton.setMnemonic('F');
        targetWindowIsFrcToolWindowRadioButton.setDisplayedMnemonicIndex(0);
        targetWindowIsFrcToolWindowRadioButton.setToolTipText("RIO Log output will appear in a dedicated FRC Tool Window");
        panel1.add(targetWindowIsFrcToolWindowRadioButton,
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
        panel1.add(targetWindowIsRunWindowRadioButton,
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
        panel1.add(label1,
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
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel1.add(panel2,
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
        label2.setText("Port:");
        label2.setDisplayedMnemonic('P');
        label2.setDisplayedMnemonicIndex(0);
        label2.setToolTipText("UDP Port (0 to 65,535) of the roboRIO for logging.");
        panel2.add(label2,
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
        panel2.add(rioLogPortTextField,
                   new GridConstraints(0,
                                       1,
                                       1,
                                       1,
                                       GridConstraints.ANCHOR_WEST,
                                       GridConstraints.FILL_HORIZONTAL,
                                       GridConstraints.SIZEPOLICY_CAN_GROW,
                                       GridConstraints.SIZEPOLICY_FIXED,
                                       null,
                                       new Dimension(50, -1),
                                       new Dimension(100, -1),
                                       0,
                                       false));
        portToDefaultValueButton = new JButton();
        portToDefaultValueButton.setHideActionText(false);
        portToDefaultValueButton.setText("Default");
        portToDefaultValueButton.setMnemonic('D');
        portToDefaultValueButton.setDisplayedMnemonicIndex(0);
        portToDefaultValueButton.setToolTipText("Sets the RIO Log port to its default value");
        panel2.add(portToDefaultValueButton,
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
        panel2.add(spacer1,
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
        panel1.add(spacer2,
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
        final Spacer spacer3 = new Spacer();
        rootPanel.add(spacer3,
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
        label2.setLabelFor(rioLogPortTextField);
        rioLogTargetWindowButtonGroup = new ButtonGroup();
        rioLogTargetWindowButtonGroup.add(targetWindowIsFrcToolWindowRadioButton);
        rioLogTargetWindowButtonGroup.add(targetWindowIsRunWindowRadioButton);
    }


    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$() { return rootPanel; }


    private static enum RioLogTargetWindowActionCommands
    {
        FrcWindow,
        RunWindow
    }

}
