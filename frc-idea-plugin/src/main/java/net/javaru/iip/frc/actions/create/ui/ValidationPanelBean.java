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

package net.javaru.iip.frc.actions.create.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.*;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBLabel;

import net.javaru.iip.frc.actions.create.ValidationMessage;
import net.javaru.iip.frc.actions.create.ui.forms.PanelBean;
import net.javaru.iip.frc.actions.create.ui.forms.ValidatingModelPanelBean;



public class ValidationPanelBean implements PanelBean
{
    private static final Logger LOG = Logger.getInstance(ValidationPanelBean.class);

    private JPanel myRootPanel;
    private JPanel myContentPanel;
    private JPanel myValidationPanel;

    protected final JPanel messagesPanel;
    protected final BoxLayout messagePanelLayout;

    public ValidationPanelBean(@NotNull ValidatingModelPanelBean panelBean)
    {
        myContentPanel.add(panelBean.getRootPanel());
        messagesPanel = new JPanel();
        messagePanelLayout = new BoxLayout(messagesPanel, BoxLayout.Y_AXIS);
        messagesPanel.setLayout(messagePanelLayout);
        myValidationPanel.add(messagesPanel, BorderLayout.NORTH);
        // 4 lines = 64 (in height) + 4 for padding
        messagesPanel.setPreferredSize(new Dimension(myValidationPanel.getWidth(), 68));
        myValidationPanel.revalidate();
        myValidationPanel.repaint();
    }


    /**
     * Updates the validation messages panel with the supplied messages.
     * If messages is null or empty, any previous messages are removed,
     * leaving a blank panel.
     *
     * @param messages the messages to display
     */
    public void updateValidationMessages(@Nullable ValidationMessage... messages)
    {
        if (messages == null)
        {
            updateValidationMessages();
        }
        else
        {
            updateValidationMessages(Arrays.asList(messages));
        }
    }


    /**
     * Updates the displayed validation messages to have no messages. That is,
     * it removes any previous messages and leaves a blank panel.
     */
    public void updateValidationMessages()
    {
        updateValidationMessages((java.util.List<ValidationMessage>) null);
    }


    /**
     * Updates the validation messages panel with the supplied messages.
     * If messages is null or empty, any previous messages are removed,
     * leaving a blank panel.
     *
     * @param messages the messages to display
     */
    public void updateValidationMessages(@Nullable java.util.List<ValidationMessage> messages)
    {
        LOG.trace("[FRC] Updating Validation messages");
        messagesPanel.removeAll();
        messagesPanel.repaint();

        if (messages != null)
        {
            LOG.trace("[FRC] Updating with " + messages.size() + " validation messages");
            java.util.List<ValidationMessage> sortedMessages = new ArrayList<>(messages);
            Collections.sort(sortedMessages);
            for (ValidationMessage msg : sortedMessages)
            {
                JBLabel label = new JBLabel(toHtml(msg.getText()), msg.getIcon(), SwingConstants.LEFT);
                messagesPanel.add(label);
            }
        }
        messagesPanel.revalidate();
        messagesPanel.repaint();
        LOG.trace(
            "[FRC] After validation updateValidationMessages, messagePanel height: " + messagesPanel.getHeight() + "    width = " + messagesPanel.getWidth());
    }


    /**
     * Clears the displayed validation messages. That is, it removes 
     * any previous messages and leaves a blank panel.
     */
    public void clearValidationMessages()
    {
        updateValidationMessages((java.util.List<ValidationMessage>) null);
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
    
    @NotNull
    @Override
    public JPanel getRootPanel() { return myRootPanel; }


    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
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
        myRootPanel = new JPanel();
        myRootPanel.setLayout(new BorderLayout(0, 0));
        myContentPanel = new JPanel();
        myContentPanel.setLayout(new BorderLayout(5, 5));
        myRootPanel.add(myContentPanel, BorderLayout.CENTER);
        myContentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
        myValidationPanel = new JPanel();
        myValidationPanel.setLayout(new BorderLayout(0, 0));
        myRootPanel.add(myValidationPanel, BorderLayout.SOUTH);
        myValidationPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), null));
    }


    /** @noinspection ALL */
    public JComponent $$$getRootComponent$$$() { return myRootPanel; }
}
