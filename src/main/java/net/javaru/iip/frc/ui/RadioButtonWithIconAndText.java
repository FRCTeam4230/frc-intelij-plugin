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

import javax.swing.*;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.i18n.FrcBundle;
import net.javaru.iip.frc.i18n.FrcMessageKey;



public class RadioButtonWithIconAndText extends ButtonWithIconAndTextBase<JRadioButton>
{
    private static final Logger LOG = Logger.getInstance(RadioButtonWithIconAndText.class);
    
    private static final long serialVersionUID = 6973040513142004333L;
    
    
    /**
     * @param radioButton the radioButton -- JRadioButton, JRadioButton, etc. -- with text already set on it, which will be used for the icon & text combination label
     * @param icon   the icon for the radioButton's label
     */
    @Contract("_, _ -> new")
    public static @NotNull RadioButtonWithIconAndText createForButtonWithText(@NotNull JRadioButton radioButton, @NotNull Icon icon)
    {
        return new RadioButtonWithIconAndText(radioButton, icon, radioButton.getText());
    }
    
    
    /**
     * @param radioButton         the radioButton -- JRadioButton, JRadioButton, etc. -- WITHOUT any text set on it as the text defined by the textMessageKey parameter will be used
     * @param icon           the icon for the radioButton's label
     * @param textMessageKey message key for the text for the radioButton's label
     */
    @Contract("_, _, _ -> new")
    public static @NotNull RadioButtonWithIconAndText createForButtonWithoutText(@NotNull JRadioButton radioButton, @NotNull Icon icon, @NotNull FrcMessageKey textMessageKey)
    {
        return createForButtonWithoutText(radioButton, icon, FrcBundle.message(textMessageKey));
    }
    
    
    /**
     * @param radioButton the radioButton -- JRadioButton, JRadioButton, etc. -- WITHOUT any text set on it as the text parameter will be used
     * @param icon   the icon for the radioButton's label
     * @param text   the text for the radioButton's label
     */
    @Contract("_, _, _ -> new")
    public static @NotNull RadioButtonWithIconAndText createForButtonWithoutText(@NotNull JRadioButton radioButton, @NotNull Icon icon, @NotNull String text)
    {
        if (StringUtils.isNotBlank(radioButton.getText()) && !radioButton.getText().equals(text))
        {
            LOG.warn("[FRC] Button passed in to constructor had text which will be ignored. Button text: " + radioButton.getText());
        }
        return new RadioButtonWithIconAndText(radioButton, icon, text);
    }
    
    
    public RadioButtonWithIconAndText(@NotNull JRadioButton checkBox, @NotNull Icon icon, @NotNull String text)
    {
        super(checkBox, icon, text);
    }
}
