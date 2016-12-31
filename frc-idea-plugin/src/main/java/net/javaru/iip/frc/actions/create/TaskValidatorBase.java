/*
 * Copyright 2015-2016 Mark Vedder
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

package net.javaru.iip.frc.actions.create;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBLabel;



public class TaskValidatorBase implements TaskValidator
{
    private static final Logger LOG = Logger.getInstance(TaskValidatorBase.class);


    protected final JPanel messagesPanel;
    protected final BoxLayout messagePanelLayout;


    public TaskValidatorBase()
    {
        messagesPanel = new JPanel();
        messagePanelLayout = new BoxLayout(messagesPanel, BoxLayout.Y_AXIS);
        messagesPanel.setLayout(messagePanelLayout);
    }
    


    public void setValidationPanel(@Nullable JPanel validationPanel) 
    {
        if (validationPanel != null)
        {
            validationPanel.add(messagesPanel, BorderLayout.NORTH);
            // 4 lines = 64 (in height) + 4 for padding
            messagesPanel.setPreferredSize(new Dimension(validationPanel.getWidth(), 68));
            validationPanel.revalidate();
            validationPanel.repaint();
        }
    }


    @Override
    public void updateDisplayedMessages(@Nullable List<ValidationMessage> messages)
    {
        LOG.trace("[FRC] Updating Validation messages");
        messagesPanel.removeAll();
        messagesPanel.repaint();
        
        if (messages != null)
        {
            LOG.trace("[FRC] Updating with " + messages.size() + " validation messages");
            List<ValidationMessage> sortedMessages = new ArrayList<>(messages);
            Collections.sort(sortedMessages);
            for (ValidationMessage msg : sortedMessages)
            {
                JBLabel label = new JBLabel(toHtml(msg.getText()), msg.getIcon(), SwingConstants.LEFT);
                messagesPanel.add(label);
            }
        }
        messagesPanel.revalidate();
        messagesPanel.repaint();
        LOG.trace("[FRC] After validation updateDisplayedMessages, messagePanel height: " + messagesPanel.getHeight() + "    width = " + messagesPanel.getWidth());
    }


    /**
     * Wrap the target string with html tags unless it is already tagged. If the input string is
     * {@code null} then the output string will also be {@code null}.
     */
    @Nullable
    @Contract("null -> null; !null -> !null")
    protected static String toHtml(@Nullable String text)
    {
        if (!StringUtil.isEmpty(text) && !text.startsWith("<html>"))
        {
            text = String.format("<html>%1$s</html>", text.trim());
        }
        return text;

    }
}
