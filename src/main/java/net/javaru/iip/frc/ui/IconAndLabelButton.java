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

package net.javaru.iip.frc.ui;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

import org.jetbrains.annotations.Nullable;



public class IconAndLabelButton extends JPanel
{
    private static final long serialVersionUID = -392212524102824537L;
    private final AbstractButton button;
    private final JLabel label;
    
    
    public IconAndLabelButton(AbstractButton button, Icon icon, String text)
    {
        this.button = button;
        this.label = new JLabel();
        this.label.setLabelFor(this.button);
        setIcon(icon);
        setText(text);
        setLayout(new GridBagLayout());
        add(button);
        add(getLabel());
    }
    
    
    protected AbstractButton getButton()
    {
        return button;
    }
    
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
    
    public void addActionListener(ActionListener listener)
    {
        getButton().addActionListener(listener);
    }
    
    
    public void removeActionListener(ActionListener listener)
    {
        getButton().removeActionListener(listener);
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
     * @see #TOOL_TIP_TEXT_KEY
     */
    public void setToolTipText(@Nullable String text)
    {
        label.setToolTipText(text);
        button.setToolTipText(text);
    }
    
    
    @Nullable
    public String getToolTipText()
    {
        return label.getToolTipText();
    }
    
    
    public void setText(String text)
    {
        getLabel().setText(text);
    }
    
    
    public String getText()
    {
        return getLabel().getText();
    }
    
    
    public void setIcon(Icon icon)
    {
        getLabel().setIcon(icon);
    }
    
    
    public Icon getIcon()
    {
        return getLabel().getIcon();
    }
    
}
