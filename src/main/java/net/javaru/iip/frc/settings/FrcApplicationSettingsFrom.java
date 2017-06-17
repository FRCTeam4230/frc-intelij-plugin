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
import java.text.DecimalFormat;
import java.text.ParseException;
import javax.swing.*;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;



public class FrcApplicationSettingsFrom
{
    private static final Logger LOG = Logger.getInstance(FrcApplicationSettingsFrom.class);

    private FrcApplicationSettings internalState = FrcApplicationSettings.Settings.clone(FrcApplicationSettings.Settings.INSTANCE());

    private JPanel rootPanel;
    private JPanel generalSettingsPanel;
    private JBTextField teamNumberTextField;
    private JBLabel teamNumberWarningIconLabel;


    public FrcApplicationSettingsFrom()
    {
        initForm();
        load(FrcApplicationSettings.Settings.INSTANCE());
    }


    private void initForm()
    {
        initTeamNumberField();
    }
    

    public boolean isModified()
    {
        final boolean modified = !FrcApplicationSettings.Settings.INSTANCE().equals(internalState);
        LOG.trace("[FRC] FrcApplicationSettingsFrom.isModified returning " + modified);
        return modified;
    }


    public void load(FrcApplicationSettings settings)
    {
        internalState = FrcApplicationSettings.Settings.clone(settings);
        //TODO: set state of Fields and components to values in settings
        setTeamNumberTextFieldValue(settings.getTeamNumber());
    }
    
    public void applyTo(@NotNull FrcApplicationSettings settings)
    {
        LOG.debug("[FRC] Before applying form settings of\n" + internalState + "\nto current/previous settings of\n" + settings);
       
        settings.setTeamNumber(internalState.getTeamNumber());
        settings.setRioLogUdpPort(internalState.getRioLogUdpPort());

        LOG.debug("[FRC] After applying form settings of\n" + internalState + "\nto current/previous settings of\n" + settings);
        //Reset the internal state to the updated setting
        internalState = FrcApplicationSettings.Settings.clone(settings);     
        // ** NO CODE BELOW THIS **
    }


    private void setTeamNumberTextFieldValue(int teamNumber)
    {
        teamNumberTextField.setText(teamNumber <= 0 ? "" : Integer.toString(teamNumber));
        setTeamNumberWarningVisibility(teamNumber <= 0);
    }
    
    private void initTeamNumberField()
    {
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
                    internalState.setTeamNumber(FrcApplicationSettingsKt.UN_CONFIGURED_TEAM_NUMBER);
                }
                else
                {
                    try
                    {
                        final int teamNum = Integer.parseInt(updatedText);
                        previousText = updatedText;
                        internalState.setTeamNumber(teamNum);
                        setTeamNumberWarningVisibility(false);

                        //TODO: Reimplement when ready
//                        if (frcSettings.isRoboRioHostMDnsTheDefault()) { roboRioMdnsHostName.setText(frcSettings.getDefaultRoboRioHost_mDNS()); }
//                        if (frcSettings.isRoboRioHostDnsTheDefault()) { roboRioDnsHostName.setText(frcSettings.getDefaultRoboRioHost_DNS()); }
//                        if (frcSettings.isRoboRioHostUsbTheDefault()) { roboRioStaticUsbIp.setText(frcSettings.getDefaultRoboRioHost_USB()); }
//                        if (frcSettings.isRoboRioHostIpTheDefault()) { roboRioIpAddress.setText(frcSettings.getDefaultRoboRioHost_IP()); }

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
