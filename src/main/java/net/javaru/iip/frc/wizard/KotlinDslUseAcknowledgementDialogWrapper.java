/*
 * Copyright 2015-2025 the original author or authors.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.javaru.iip.frc.wizard;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;

import net.javaru.iip.frc.i18n.FrcBundle;



public class KotlinDslUseAcknowledgementDialogWrapper extends DialogWrapper
{
    private static final Logger LOG = Logger.getInstance(KotlinDslUseAcknowledgementDialogWrapper.class);
    
    
    private JPanel mainPanel;
    private JPanel wrappingDisclaimerPanel;
    private JBLabel disclaimerLabel;
    private JPanel acknowledgmentPreamblePanel;
    private JBCheckBox acknowledgeCheckbox;
    
    
    public KotlinDslUseAcknowledgementDialogWrapper(JComponent parent)
    {
        // www.jetbrains.org/intellij/sdk/docs/user_interface_components/dialog_wrapper.html
        super(parent, true);
        super.init();
        setTitle(FrcBundle.message("frc.ui.wizard.projectSettingsStep.kotlinDsl.disclaimer.title"));
        initComponents();
    }
    
    @Override
    protected @Nullable JComponent createCenterPanel()
    {
        return mainPanel;
    }
    
    private void initComponents()
    {
        acknowledgeCheckbox.setSelected(false);
        this.setOKActionEnabled(false);
        acknowledgeCheckbox.addActionListener(e -> this.setOKActionEnabled(acknowledgeCheckbox.isSelected()));
    }
    
    public boolean isAcknowledged()
    {
        return acknowledgeCheckbox.isSelected();
    }
    
    
    @Override
    protected @NotNull DialogStyle getStyle()
    {
        return DialogStyle.COMPACT;
    }
}
