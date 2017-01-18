/*
 * Copyright 2015-2017 Mark Vedder
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

package net.javaru.iip.frc.util.ui

import org.jetbrains.annotations.NotNull
import java.beans.PropertyChangeEvent
import java.util.*
import javax.swing.SwingUtilities
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.AbstractDocument
import javax.swing.text.Document
import javax.swing.text.JTextComponent


interface TextChangeListener : EventListener
{
    fun textChanged(updatedText: String)
}


/**
 * Installs a listener to receive notification when the text of any
 * `JTextComponent` is changed. Internally, it installs a
 * [DocumentListener] on the text component's [Document],
 * and a `addPropertyChangeListener` on the text component to detect
 * if the `Document` itself is replaced.


 * @param changeListener a listener to receive [ChangeEvent]s
 * *                       when the text is changed; the source object for the events
 * *                       will be the text component
 * *
 * *
 * @throws NullPointerException if either parameter is null
 */
@Suppress("unused")
fun JTextComponent.addChangeListener(@NotNull changeListener: ChangeListener)
{
    // Based on http://stackoverflow.com/a/27190162/1348743
    Objects.requireNonNull(changeListener)
    val docListener = object : DocumentListener
    {
        private var lastChange = 0
        private var lastNotifiedChange = 0


        override fun insertUpdate(e: DocumentEvent?)
        {
            changedUpdate(e)
        }


        override fun removeUpdate(e: DocumentEvent?)
        {
            changedUpdate(e)
        }


        override fun changedUpdate(e: DocumentEvent?)
        {
            handleChange()
        }

        private fun handleChange()
        {
            lastChange++
            SwingUtilities.invokeLater {
                if (lastNotifiedChange != lastChange)
                {
                    lastNotifiedChange = lastChange
                    changeListener.stateChanged(ChangeEvent(text))
                }
            }
        }
    }
    addPropertyChangeListener("document") { e: PropertyChangeEvent ->
        val oldDoc = e.oldValue as Document
        val newDoc = e.newValue as Document
        oldDoc.removeDocumentListener(docListener)
        newDoc.addDocumentListener(docListener)


        val docEvent: DocumentEvent? =
                if (newDoc is AbstractDocument)
                    newDoc.DefaultDocumentEvent(0, newDoc.getLength(), DocumentEvent.EventType.INSERT)
                else
                    null
        docListener.changedUpdate(docEvent)
    }
    document?.addDocumentListener(docListener)
}

/**
 * Installs a listener to receive notification when the text of any
 * `JTextComponent` is changed. Internally, it installs a
 * [DocumentListener] on the text component's [Document],
 * and a `addPropertyChangeListener` on the text component to detect
 * if the `Document` itself is replaced.


 * @param textChangeListener a listener to receive [ChangeEvent]s
 * *                         when the text is changed; the source object for the events
 * *                         will be the text component
 * *
 * *
 * @throws NullPointerException if either parameter is null
 */
@Suppress("unused")
fun JTextComponent.addTextChangeListener(@NotNull textChangeListener: TextChangeListener)
{
    // Based on http://stackoverflow.com/a/27190162/1348743
    Objects.requireNonNull(textChangeListener)
    val docListener = object : DocumentListener
    {
        private var lastChange = 0
        private var lastNotifiedChange = 0


        override fun insertUpdate(e: DocumentEvent?)
        {
            changedUpdate(e)
        }


        override fun removeUpdate(e: DocumentEvent?)
        {
            changedUpdate(e)
        }


        override fun changedUpdate(e: DocumentEvent?)
        {
            handleChange()
        }

        private fun handleChange()
        {
            lastChange++
            SwingUtilities.invokeLater {
                if (lastNotifiedChange != lastChange)
                {
                    lastNotifiedChange = lastChange
                    textChangeListener.textChanged(text)
                }
            }
        }
    }
    addPropertyChangeListener("document") { e: PropertyChangeEvent ->
        val oldDoc = e.oldValue as Document
        val newDoc = e.newValue as Document
        oldDoc.removeDocumentListener(docListener)
        newDoc.addDocumentListener(docListener)

        val docEvent: DocumentEvent? =
                if (newDoc is AbstractDocument)
                    newDoc.DefaultDocumentEvent(0, newDoc.getLength(), DocumentEvent.EventType.INSERT)
                else
                    null
        docListener.changedUpdate(docEvent)
    }
    document?.addDocumentListener(docListener)
}

