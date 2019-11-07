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

package net.javaru.iip.frc.util

import com.intellij.openapi.util.text.StringUtil
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.JTextComponent

/**
 * Sets the text of the supplied field to the supplied value iff the text field is blank.
 * In other words, it fills in blank fields with a default value.
 *
 * @param text  the (default) text to use if the field is empty
 */
fun JTextField.setTextIfEmpty(text: String?)
{
    if (StringUtil.isEmpty(this.text))
    {
        this.text = StringUtil.notNullize(text)
    }
}

/**
 * Adds a `DocumentListener` to the components; document that will call the supplied action anytime the text is changed.
 */
fun JTextComponent.addTextChangedListener(action: (text: String) -> Unit)
{
    document.addDocumentListener(object : DocumentListener
                                 {
                                     override fun changedUpdate(e: DocumentEvent?) = fire()
                                     override fun insertUpdate(e: DocumentEvent?) = fire()
                                     override fun removeUpdate(e: DocumentEvent?) = fire()
                                     fun fire() = action.invoke(text)
                                 })
}

/**
 * Adds a `DocumentListener` to the components; document that will call the supplied action anytime the text is changed.
 */
fun JTextComponent.addTextChangedListener(action: (e: DocumentEvent?, text: String) -> Unit)
{
    document.addDocumentListener(object : DocumentListener
                                 {
                                     override fun changedUpdate(e: DocumentEvent?) = fire(e)
                                     override fun insertUpdate(e: DocumentEvent?) = fire(e)
                                     override fun removeUpdate(e: DocumentEvent?) = fire(e)
                                     fun fire(e: DocumentEvent?) = action.invoke(e, text)
                                 })
}

