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

package net.javaru.iip.frc.wizard;

import java.awt.event.ItemListener;
import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;

import net.javaru.iip.frc.settings.FrcApplicationSettingsKt;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public class TeamNumberDialogWrapper extends DialogWrapper
{
    private static final Logger LOG = Logger.getInstance(TeamNumberDialogWrapper.class);
    
    private static final String ACTUAL = "ACTUAL";
    private static final String VARIANT = "VARIANT";
    
    private JPanel mainPanel;
    private JBLabel introductionLabel;
    private JRadioButton actualNumberRadioButton;
    private JRadioButton variantNumberRadioButton;
    private JBTextField teamNumberInputTextField;
    
    private final int projectTeamNumber;
    private boolean isInputTeamNumberValid = false;

    public TeamNumberDialogWrapper(JComponent parent, int projectTeamNumber)
    {
        super(parent, true);
        setTitle(message("frc.ui.wizard.teamNumberDialogWrapper.dialog.title"));
        this.projectTeamNumber = projectTeamNumber;
        initComponents();
        init(); // from super class
    }
    
    
    private void initComponents()
    {
        setOKActionEnabled(false);
        String numNoCommas = String.format("%d", projectTeamNumber);
        teamNumberInputTextField.setEnabled(false);
        actualNumberRadioButton.setText(message("frc.ui.wizard.teamNumberDialogWrapper.radioButton.actualNumber.text", numNoCommas));
        variantNumberRadioButton.setText(message("frc.ui.wizard.teamNumberDialogWrapper.radioButton.variant.text", numNoCommas));
    
        actualNumberRadioButton.setActionCommand(ACTUAL);
        variantNumberRadioButton.setActionCommand(VARIANT);
    
        actualNumberRadioButton.setSelected(false);
        variantNumberRadioButton.setSelected(false);
    
        ItemListener itemListener = e -> {
            final AbstractButton button = (AbstractButton) e.getSource();
            final ButtonModel model = button.getModel();
            final String actionCommand = model.getActionCommand();
            if (ACTUAL.equals(actionCommand))
            {
                setOKActionEnabled(true);
                teamNumberInputTextField.setEnabled(false);
            }
            else if (VARIANT.equals(actionCommand))
            {
                setOKActionEnabled(isInputTeamNumberValid);
                teamNumberInputTextField.setEnabled(true);
            }
        };
    
        actualNumberRadioButton.addItemListener(itemListener);
        variantNumberRadioButton.addItemListener(itemListener);
    
        teamNumberInputTextField.addKeyListener(new TeamNumberKeyChangeListener(teamNumberInputTextField) {
            @Override
            public void makeUpdates(@NotNull String text, boolean isValidTeamNumber)
            {
                isInputTeamNumberValid = isValidTeamNumber;
                if (variantNumberRadioButton.isSelected())
                {
                    setOKActionEnabled(isValidTeamNumber);
                }
            }
        });
    }
    
    
    /**
     * Factory method. It creates panel with dialog options. Options panel is located at the
     * center of the dialog's content pane. The implementation can return {@code null}
     * value. In this case there will be no options panel.
     */
    @Nullable
    @Override
    protected JComponent createCenterPanel()
    {
        return mainPanel;
    }
    
    public int getTeamNumberForAppSettings()
    {
        if (actualNumberRadioButton.isSelected())
        {
            return projectTeamNumber;
        }
        else if (variantNumberRadioButton.isSelected())
        {
            final String text = teamNumberInputTextField.getText();
            try
            {
                return Integer.parseInt(text);
            }
            catch (NumberFormatException e)
            {
                LOG.warn("[FRC] Could not parse '" + text + "' to a team number");
            }
        }
        return FrcApplicationSettingsKt.UN_CONFIGURED_TEAM_NUMBER;
    }
}
