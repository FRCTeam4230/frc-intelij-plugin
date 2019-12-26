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

package net.javaru.iip.frc.module.legacy;

import java.awt.*;
import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ui.JBUI;

import net.javaru.iip.frc.util.FrcUiUtilsKt;



public class FrcLegacyModuleWizardPanel extends JPanel
{
    private static final Logger LOG = Logger.getInstance(FrcLegacyModuleWizardPanel.class);
    private static final long serialVersionUID = -7663072295702631169L;


    private JPanel rootPanel;
    private JPanel selectRobotTypePanel;
    private ButtonGroup robotTypeButtonGroup;

    public FrcLegacyModuleWizardPanel()
    {
        super(new BorderLayout());
        add(rootPanel, BorderLayout.CENTER);
    }

    
    @NotNull
    public LegacyRobotType getSelectedRobotType()
    {
        final ButtonModel selectedButtonModel = robotTypeButtonGroup.getSelection();
        //TODO: we want to make sure a button is actual selected. Need to implement a validate method. For now, below in initRobotTypeSelectionPanel() we set the 1st robot type as selected
        return selectedButtonModel == null ? LegacyRobotType.Iterative : LegacyRobotType.valueOf(selectedButtonModel.getActionCommand());
    }

    private void createUIComponents()
    {
        initRobotTypeSelectionPanel();
    }
    
    private void initRobotTypeSelectionPanel()
    {
        final LegacyRobotType[] legacyRobotTypes = LegacyRobotType.values();
        final GridLayoutManager layoutManager = new GridLayoutManager(((legacyRobotTypes.length * 2) + 1 ), 1, JBUI.emptyInsets(), -1, -1);
        robotTypeButtonGroup = new ButtonGroup();
        selectRobotTypePanel = new JPanel(layoutManager);
        

        int currentRow = 0;
        for (LegacyRobotType legacyRobotType : legacyRobotTypes)
        {
            JRadioButton button = new JRadioButton(FrcUiUtilsKt.boldLabelText(legacyRobotType.getLabelName()));
            button.setActionCommand(legacyRobotType.name());
            robotTypeButtonGroup.add(button);
            if (legacyRobotType == legacyRobotTypes[0]) {button.setSelected(true);}
            selectRobotTypePanel.add(button,
                                     new GridConstraints(currentRow++,
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
            JLabel label = new JLabel(legacyRobotType.getDescription());
            selectRobotTypePanel.add(label,
                                     new GridConstraints(currentRow++,
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
                                                         6,
                                                         false));
            
        }
        
        final Spacer spacer = new Spacer();
        selectRobotTypePanel.add(spacer,
                                 new GridConstraints(currentRow,
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
        
        
    }
}
