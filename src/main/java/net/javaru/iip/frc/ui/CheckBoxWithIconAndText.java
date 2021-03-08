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



public class CheckBoxWithIconAndText extends ButtonWithIconAndTextBase<JCheckBox>
{
    private static final Logger LOG = Logger.getInstance(CheckBoxWithIconAndText.class);
    
    private static final long serialVersionUID = 6973040513142004333L;
    
    
    /**
     * @param checkBox the checkBox -- JRadioButton, JCheckBox, etc. -- with text already set on it, which will be used for the icon & text combination label
     * @param icon   the icon for the checkBox's label
     */
    @Contract("_, _ -> new")
    public static @NotNull CheckBoxWithIconAndText createForButtonWithText(@NotNull JCheckBox checkBox, @NotNull Icon icon)
    {
        return new CheckBoxWithIconAndText(checkBox, icon, checkBox.getText());
    }
    
    
    /**
     * @param checkBox         the checkBox -- JRadioButton, JCheckBox, etc. -- WITHOUT any text set on it as the text defined by the textMessageKey parameter will be used
     * @param icon           the icon for the checkBox's label
     * @param textMessageKey message key for the text for the checkBox's label
     */
    @Contract("_, _, _ -> new")
    public static @NotNull CheckBoxWithIconAndText createForButtonWithoutText(@NotNull JCheckBox checkBox, @NotNull Icon icon, @NotNull FrcMessageKey textMessageKey)
    {
        return createForButtonWithoutText(checkBox, icon, FrcBundle.message(textMessageKey));
    }
    
    
    /**
     * @param checkBox the checkBox -- JRadioButton, JCheckBox, etc. -- WITHOUT any text set on it as the text parameter will be used
     * @param icon   the icon for the checkBox's label
     * @param text   the text for the checkBox's label
     */
    @Contract("_, _, _ -> new")
    public static @NotNull CheckBoxWithIconAndText createForButtonWithoutText(@NotNull JCheckBox checkBox, @NotNull Icon icon, @NotNull String text)
    {
        if (StringUtils.isNotBlank(checkBox.getText()) && !checkBox.getText().equals(text))
        {
            LOG.warn("[FRC] Button passed in to constructor had text which will be ignored. Button text: " + checkBox.getText());
        }
        return new CheckBoxWithIconAndText(checkBox, icon, text);
    }
    
    
    public CheckBoxWithIconAndText(@NotNull JCheckBox checkBox, @NotNull Icon icon, @NotNull String text)
    {
        super(checkBox, icon, text);
    }
    
    
    public CheckBoxWithIconAndText()
    {
    
    }
}
