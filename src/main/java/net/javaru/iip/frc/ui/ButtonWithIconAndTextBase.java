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

import java.awt.*;
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.i18n.FrcBundle;
import net.javaru.iip.frc.i18n.FrcMessageKey;



@SuppressWarnings("unused")
public class ButtonWithIconAndTextBase<T extends AbstractButton> extends JPanel implements ItemSelectable
{
    private static final Logger LOG = Logger.getInstance(ButtonWithIconAndTextBase.class);
    
    private static final long serialVersionUID = -392212524102824537L;
    @Nullable
    private T button;
    @NotNull
    private final JLabel label = new JLabel();
    private boolean buttonHasMouseHover = false;
    
    
    /**
     * @param button the button -- JRadioButton, JCheckBox, etc. -- with text already set on it, which will be used for the icon & text combination label
     * @param icon   the icon for the button's label
     */
    @Contract("_, _ -> new")
    public static <T extends AbstractButton> @NotNull ButtonWithIconAndTextBase<T> createForButtonWithText(@NotNull T button, @NotNull Icon icon)
    {
        return new ButtonWithIconAndTextBase<>(button, icon, button.getText());
    }
    
    
    /**
     * @param button         the button -- JRadioButton, JCheckBox, etc. -- WITHOUT any text set on it as the text defined by the textMessageKey parameter will be used
     * @param icon           the icon for the button's label
     * @param textMessageKey message key for the text for the button's label
     */
    @Contract("_, _, _ -> new")
    public static <T extends AbstractButton> @NotNull ButtonWithIconAndTextBase<T> createForButtonWithoutText(@NotNull T button, @NotNull Icon icon, @NotNull FrcMessageKey textMessageKey)
    {
        return createForButtonWithoutText(button, icon, FrcBundle.message(textMessageKey));
    }
    
    
    /**
     * @param button the button -- JRadioButton, JCheckBox, etc. -- WITHOUT any text set on it as the text parameter will be used
     * @param icon   the icon for the button's label
     * @param text   the text for the button's label
     */
    @Contract("_, _, _ -> new")
    public static <T extends AbstractButton> @NotNull ButtonWithIconAndTextBase<T> createForButtonWithoutText(@NotNull T button, @NotNull Icon icon, @NotNull String text)
    {
        if (StringUtils.isNotBlank(button.getText()) && !button.getText().equals(text))
        {
            LOG.warn("[FRC] Button passed in to constructor had text which will be ignored. Button text: " + button.getText());
        }
        return new ButtonWithIconAndTextBase<>(button, icon, text);
    }
    
    protected ButtonWithIconAndTextBase()
    {
        this.label.setLabelFor(this.button);
        final JComponent spacer = createSpacer();
        
    }
    
    protected ButtonWithIconAndTextBase(@NotNull T button, @NotNull Icon icon, @NotNull String text)
    {
        this.button = button;
        this.button.setText("");
        this.label.setLabelFor(this.button);
        final JComponent spacer = createSpacer();
        setIcon(icon);
        setText(text);
        setLayout(new GridBagLayout());
        add(button);
        add(spacer); // without the spacer, the label is too tight next to the radioButton or checkBox
        add(getLabel());
        addMouseListener(new OurPanelClickListener());
        this.button.addMouseListener(new OurButtonMouseStatusListener());
        this.button.addFocusListener(new OurMnemonicSelectionListener());
        this.label.addMouseListener(new OurLabelClickListener());
        this.button.addPropertyChangeListener(new OurSyncLabelEnablingListener());
    }
    
    
    private static JComponent createSpacer()
    {
        JLabel spacer = new JLabel("");
        final Dimension size = new Dimension(5, -1);
        spacer.setMinimumSize(size);
        spacer.setMaximumSize(size);
        spacer.setPreferredSize(size);
        return spacer;
    }
    
    
    @NotNull
    protected T getButton() { return button; }
    
    
    /**
     * Sets the button. This is meant for use solely in the UI Designer.
     * @param button the backing button
     */
    public void setButton(T button)
    {
        this.button = button;
        this.button.addMouseListener(new OurButtonMouseStatusListener());
        this.button.addFocusListener(new OurMnemonicSelectionListener());
        this.label.addMouseListener(new OurLabelClickListener());
        this.button.addPropertyChangeListener(new OurSyncLabelEnablingListener());
    }
    
    
    
    @NotNull
    protected JLabel getLabel()
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
     *             description: The text to display in a tool tip.
     *
     * @see #TOOL_TIP_TEXT_KEY
     */
    public void setToolTipText(@Nullable String text)
    {
        super.setToolTipText(text);
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
        super.setEnabled(enabled);
        button.setEnabled(enabled);
        label.setEnabled(enabled);
    }
    
    
    @Override
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
    
    
    @Override
    public void addItemListener(ItemListener listener) { button.addItemListener(listener);}
    
    
    @Override
    public void removeItemListener(ItemListener listener) { button.removeItemListener(listener);}
    
    
    public ItemListener[] getItemListeners() {return button.getItemListeners();}
    
    
    @Override
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
