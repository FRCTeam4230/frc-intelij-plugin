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

import net.javaru.iip.frc.settings.FrcApplicationSettings
import org.apache.commons.lang3.StringUtils
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JTextField

abstract class TeamNumberKeyChangeListener(private val teamNumberTextField: JTextField) : KeyListener
{
    private var previousText: String
    
    
    init
    {
        previousText = teamNumberTextField.text
    }
    
    
    /**
     * Invoked when a key has been typed.
     * See the class description for [KeyEvent] for a definition of a key typed event.
     */
    override fun keyTyped(e: KeyEvent) {}

    /**
     * Invoked when a key has been pressed.
     * See the class description for [KeyEvent] for a definition of a key pressed event.
     */
    override fun keyPressed(e: KeyEvent) {}

    /**
     * Invoked when a key has been released.
     * See the class description for [KeyEvent] for a definition of
     * a key released event.
     *
     * @param e
     */
    override fun keyReleased(e: KeyEvent)
    {
        val updatedText = StringUtils.replaceAll(teamNumberTextField.text.trim { it <= ' ' }, "\\s", "").trim { it <= ' ' }
        teamNumberTextField.text = updatedText // set to the trimmed value - this mostly handles values pasted in with spaces
        if (StringUtils.isBlank(updatedText))
        { // We all the filed to be blanked out (which is not a valid team number), but the updateTeamNumberWarningVisibility called at the end of this method will turn on the warning icon
            previousText = updatedText
        }
        else
        {
            try
            {
                updatedText.toLong() // handle case of extra digits while editing the field by checking for a valid Long rather than an Int
                // it is a valid number, so update the previousText for the next loop through
                previousText = updatedText
            }
            catch (ignore: NumberFormatException)
            { //not a valid integer; so replace the text with the previous value (effectively deleting the invalid character)
                teamNumberTextField.text = previousText.trim { it <= ' ' }
            }
        }
        
        makeUpdates(teamNumberTextField.text, FrcApplicationSettings.isValidTeamNumber(teamNumberTextField.text))
    }
    
    abstract fun makeUpdates(text: String, isValidTeamNumber: Boolean)
}