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
@file:Suppress("HtmlRequiredLangAttribute")

package net.javaru.iip.frc.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.wm.ex.WindowManagerEx
import org.intellij.lang.annotations.Language
import java.awt.Component
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.JTextComponent

/**
 * Returns the provided text inside HTML tags centering the text for use on a Swing label.
 * For example, given the text 'My Message', this will return:
 * <pre>
 * "&lt;html&gt;&lt;div style='text-align: center;'&gt;" + text + "&lt;/div&gt;&lt;/html&gt;"
</pre> *
 * @param text the text to wrap
 * @return the provided text inside HTML tags centering the text
 */
@Language("HTML")
fun centerLabelText(text: String): String
{
    return "<html><div style='text-align: center;'>$text</div></html>"
}

@Language("HTML")
fun boldLabelText(text: String): String
{
    return "<html><div style='font-weight: bold;'>$text</div></html>"
}


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

/**
 * Finds the `IdeFrame` for the supplied project (which may be null when no project is 
 * opened). In the event that cannot be found, it attempts to find the most recently
 * focused component for the project is found. In the event that cannot be found, it
 * attempts to find the most recently focused window. In the event that cannot be 
 * found, `null` is returned. Primarily meant for use when needing a parent component 
 * for use when opening a dialog window.
 * 
 * @receiver project – may be null when no project is opened.
 */
fun Project?.findIdeFrameOrAlternateParentComponent(): Component?
{
    val windowManager = WindowManagerEx.getInstanceEx()
    
    var parentComponent: Component? = null
    val ideFrame = windowManager.findFrameFor(this)
    if (ideFrame != null)
    {
        parentComponent = ideFrame.component
    }
    if (parentComponent == null)
    {
        parentComponent = windowManager.getFocusedComponent(this)
    }
    
    if (parentComponent == null)
    {
        parentComponent = windowManager.mostRecentFocusedWindow
    }
    
    return parentComponent
}