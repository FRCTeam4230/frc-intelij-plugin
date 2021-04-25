/*
 * Copyright 2015-2021 the original author or authors.
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

package net.javaru.iip.frc.ui;

import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;



/**
 * A decorator that synchronizes a Button (JRadioButton, JCheckBox, etc) and a JLabel.
 * The primary use case if in to allow for the virtual creation of a Button
 * that has both an icon and a text label. Since a Button cannot have both an icon and
 * text, a button without any text or icon is used alongside a JLabel that has both the
 * icon and text set on it.
 * @param <T> The Button type (JRadioButton, JCheckBox, etc)
 */
@SuppressWarnings("unused")
public class ButtonAndLabelSynchronizer<T extends AbstractButton>
{
    private static final Logger LOG = Logger.getInstance(ButtonAndLabelSynchronizer.class);
    
    @NotNull
    private final T button;
    @NotNull
    private final JLabel label;
    
    @Nullable final JPanel panel;
    
    private boolean buttonHasMouseHover = false;
    
    public ButtonAndLabelSynchronizer(@NotNull T buttonOrCheckbox, @NotNull JLabel label)
    {
        this(null, buttonOrCheckbox, label);
    }
    public ButtonAndLabelSynchronizer(@Nullable JPanel panel, @NotNull T buttonOrCheckbox, @NotNull JLabel label)
    {
        if (StringUtils.isNotBlank(buttonOrCheckbox.getText()))
        {
            LOG.warn("[FRC] Button passed in to constructor had text. Buttons used should have not text. Set the icon and/or text on the label.");
        }
        if (buttonOrCheckbox.getIcon() != null)
        {
            LOG.warn("[FRC] Button passed in to constructor had an icon set on it. Buttons used should have icons. Set the icon and/or text on the label.");
        }
        this.panel = panel;
        this.button = buttonOrCheckbox;
        this.label = label;
        if (panel != null) panel.addMouseListener(new OurPanelClickListener());
        this.button.addMouseListener(new OurButtonMouseStatusListener());
        this.button.addFocusListener(new OurMnemonicSelectionListener());
        this.label.addMouseListener(new OurLabelClickListener());
        this.button.addPropertyChangeListener(new OurSyncLabelEnablingListener());
        
        if (StringUtils.isNotBlank(button.getToolTipText()))
        {
            if (!button.getToolTipText().equals(label.getToolTipText()))
            {
                if (StringUtils.isNotBlank(label.getToolTipText()))
                {
                    LOG.warn("[FRC] label tooltip does not match button tool tip and will be replaced with button tooltip. Label tip: >>" + label.getToolTipText() + "<< will be replaced with >>" + button.getToolTipText() +"<<");
                }
                setToolTipText(button.getText());
            }
        }
        else if (StringUtils.isNotBlank(label.getToolTipText()))
        {
            setToolTipText(label.getToolTipText());
        }
    }
    
    @NotNull
    public T getButton() { return button; }
    
    
    @NotNull
    public JLabel getLabel()
    {
        return label;
    }
    
    public void setMnemonic(char aChar)
    {
        label.setDisplayedMnemonic(aChar);
    }
    
    public void setMnemonic(int key)
    {
        label.setDisplayedMnemonic(key);
    }
    
    public int getMnemonic()
    {
        return label.getDisplayedMnemonic();
    }
    
    
    /**
     * Registers the text to display in a tool tip.
     * The text displays when the cursor lingers over the component.
     * <p>
     * See <a href="https://docs.oracle.com/javase/tutorial/uiswing/components/tooltip.html">How to Use Tool Tips</a>
     * in <em>The Java Tutorial</em>
     * for further documentation.
     *
     * @param text the string to display; if the text is <code>null</code>,
     *             the tool tip is turned off for this component
     *
     * description: The text to display in a tool tip.
     */
    public void setToolTipText(@Nullable String text)
    {
        if (panel != null) panel.setToolTipText(text);
        label.setToolTipText(text);
        button.setToolTipText(text);
    }
    
    @Nullable
    public String getToolTipText()
    {
        return label.getToolTipText();
    }
    
    
    public void setText(String text) { label.setText(text); }
    public String getText() { return label.getText(); }
    
    public void setIcon(Icon icon) { label.setIcon(icon); }
    public Icon getIcon() { return label.getIcon(); }
    
    public void setDisabledIcon(Icon icon) {label.setDisabledIcon(icon); }
    public Icon getDisabledIcon() { return label.getDisabledIcon(); }
    
    public void setEnabled(boolean enabled)
    {
        if (panel != null) panel.setEnabled(enabled);
        button.setEnabled(enabled);
        label.setEnabled(enabled);
    }
    
    public boolean isEnabled() { return button.isEnabled();}
    
    
    public void setActionCommand(String actionCommand) {button.setActionCommand(actionCommand);}
    public String getActionCommand() {return button.getActionCommand();}
    
    public void setAction(Action a) {button.setAction(a);}
    public Action getAction() {return button.getAction();}
    
    private void toggleButtonSelection() { button.setSelected(!button.isSelected()); }
    
    public void addActionListener(ActionListener listener) { button.addActionListener(listener); }
    public void removeActionListener(ActionListener listener) { button.removeActionListener(listener); }
    public ActionListener[] getActionListeners() {return button.getActionListeners();}
    
    public void addChangeListener(ChangeListener listener) {button.addChangeListener(listener);}
    public void removeChangeListener(ChangeListener listener) {button.removeChangeListener(listener);}
    public ChangeListener[] getChangeListeners() {return button.getChangeListeners();}
    
    
    public void addItemListener(ItemListener listener) { button.addItemListener(listener);}
    public void removeItemListener(ItemListener listener) { button.removeItemListener(listener);}
    
    public ItemListener[] getItemListeners() {return button.getItemListeners();}
    
    public Object[] getSelectedObjects()
    {
        if (!isSelected())
        {
            return null;
        }
        Object[] selectedObjects = new Object[1];
        selectedObjects[0] = getText();
        return selectedObjects;
    }
    
    /**
     * Returns the state of the button. True if the
     * toggle button is selected, false if it's not.
     *
     * @return true if the toggle button is selected, otherwise false
     */
    public boolean isSelected() { return button.isSelected(); }
    
    
    /**
     * Sets the state of the button. Note that this method does not
     * trigger an <code>actionEvent</code>.
     * Call <code>doClick</code> to perform a programmatic action change.
     *
     * @param b true if the button is selected, otherwise false
     */
    public void setSelected(boolean b) { button.setSelected(b); }
    
    class OurMnemonicSelectionListener implements FocusListener
    {
        @Override
        public void focusGained(FocusEvent e)
        {
            if (!buttonHasMouseHover && isEnabled()) toggleButtonSelection();
        }
    
        @Override
        public void focusLost(FocusEvent e) {}
    }
    
    class OurButtonMouseStatusListener extends MouseAdapter
    {
        @Override
        public void mouseEntered(MouseEvent e)
        {
            buttonHasMouseHover = true;
        }
    
    
        @Override
        public void mouseExited(MouseEvent e)
        {
            buttonHasMouseHover = false;
        }
    }
    
    /**
     * A MouseAdapter / Listener to toggle the button's selection when the
     * label is clicked. This is to mimic the behavior of a typical button.
     */
    class OurLabelClickListener extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            if (isEnabled())
            {
                toggleButtonSelection();
            }
        }
    }
    
    /**
     * A MouseAdapter / Listener to toggle the button's selection when the
     * panel is clicked. This is to mimic the behavior of a typical button.
     */
    class OurPanelClickListener extends MouseAdapter
    {
        @Override
        public void mouseClicked(MouseEvent e)
        {
            if (isEnabled())
            {
                toggleButtonSelection();
            }
        }
    }
    
    /**
     * A PropertyChangeListener to be added to the button so that if it is enabled/disabled 'externally'
     * the label and panel enabling is synced to it.
     */
    class OurSyncLabelEnablingListener implements PropertyChangeListener
    {
        @Override
        public void propertyChange(PropertyChangeEvent evt)
        {
            try
            {
                // There is no Property Name constant defined in Swing for the "enabled" property, so we have to just hard code it.
                if ("enabled".equals(evt.getPropertyName()))
                {
                    if (LOG.isTraceEnabled()) { LOG.trace(String.format("[FRC] State changed for %s to %5s for %s%n", evt.getPropertyName(), evt.getNewValue(), evt.getSource())); }
                    setEnabled((Boolean) evt.getNewValue());
                }
            }
            catch (Throwable e)
            {
                 LOG.warn("An exception occurred in OurSyncLabelEnablingListener. Cause Summary: " + e.toString(), e);
            }
        }
    }
    
    
}
