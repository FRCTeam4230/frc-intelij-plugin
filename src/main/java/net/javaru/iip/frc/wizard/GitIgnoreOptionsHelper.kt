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

package net.javaru.iip.frc.wizard

import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.AbstractButton
import javax.swing.JRadioButton


fun initIdeButtonGroupChangeListener(sharedButton: JRadioButton, ignoreButton: JRadioButton, noEntryButton: JRadioButton, updateConfigAction: (IdeConfigOption) -> Unit)
{
    sharedButton.actionCommand = IdeConfigOption.Share.name
    ignoreButton.actionCommand = IdeConfigOption.Ignore.name
    noEntryButton.actionCommand = IdeConfigOption.NoEntry.name
    
    val listener = ItemListener { e: ItemEvent? ->
        if (e == null) return@ItemListener

        val button = e.source as AbstractButton
        val model = button.model
        val actionCommand = model.actionCommand
        val ideConfigOption = IdeConfigOption.valueOf(actionCommand)
        updateConfigAction.invoke(ideConfigOption)
    }

    sharedButton.addItemListener(listener)
    ignoreButton.addItemListener(listener)
    noEntryButton.addItemListener(listener)
}

fun initIdeButtonGroupChangeListener(sharedButton: JRadioButton, ignoreButton: JRadioButton, noEntryButton: JRadioButton, config: GitIgnoreConfiguration, configPropertyToUpdate: String)
{
    initIdeButtonGroupChangeListener(sharedButton, ignoreButton, noEntryButton) { ideConfigOption ->
        val clazz = config::class.java
        val field = clazz.getField(configPropertyToUpdate)
        field.isAccessible = true
        field.set(config, ideConfigOption)
    }
}
