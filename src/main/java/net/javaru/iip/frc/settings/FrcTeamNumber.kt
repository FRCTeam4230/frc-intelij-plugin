/*
 * Copyright 2015-2020 the original author or authors
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

package net.javaru.iip.frc.settings

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.text.StringUtil
import org.apache.commons.lang3.RegExUtils
import org.apache.commons.lang3.StringUtils
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.InputVerifier
import javax.swing.JComponent
import javax.swing.JTextField


const val UN_CONFIGURED_TEAM_NUMBER: Int = 0


/**
 * Validates that the supplied text is not null, not empty, not blank, is a valid integer (after trimming), 
 * and is an integer greater than 0.
 */
fun CharSequence?.isValidTeamNumber(): Boolean
{
    return try
    {
        !StringUtil.isEmptyOrSpaces(this) && Integer.valueOf(toString().trim()).isValidTeamNumber()
    }
    catch (ignore: NumberFormatException)
    {
        false
    }
}

/**
 * Validates the teamNumber is greater than 0.
 */
fun Int.isValidTeamNumber(): Boolean = this > 0

/**
 * Safely converts text to a team number, returning `null` if it is not a valid team number.
 * This will handle the rare case of the string text containing commas or dot grouping characters.
 * It also trims the text prior to attempting to convert to an Integer.
 */
fun CharSequence?.toTeamNumberOrNull(): Int?
{
    if (StringUtil.isEmptyOrSpaces(this)) return null
    return try 
    {
        Integer.valueOf(this.toString().replace(",", "").replace(".", "").trim())
    }
    catch (ignore: NumberFormatException)
    {
        null
    }
}

/**
 * Safely converts text to a team number, returning the value of [UN_CONFIGURED_TEAM_NUMBER] if it is 
 * not a valid team number. This will handle the rare case of the string text containing commas or 
 * dot grouping characters. It also trims the text prior to attempting to convert to an Integer.
 */
fun CharSequence?.toTeamNumberOrUndefined(): Int = this.toTeamNumberOrNull() ?: UN_CONFIGURED_TEAM_NUMBER

/**
 * Converts an integer to a non formatted String if it is a valid team number, or an empty String if it is not a valid team number 
 */
fun Int?.toValidTeamNumberOrEmptyString(): String = if (this?.isValidTeamNumber() == true) this.toString() else ""

class TeamNumberInputVerifier : InputVerifier()
{
    /**
     * Checks whether the JComponent's input is valid. This method should
     * have no side effects. It returns a boolean indicating the status
     * of the argument's input.
     *
     * @param input the JComponent to verify
     *
     * @return `true` when valid, `false` when invalid
     *
     * @see JComponent.setInputVerifier
     *
     * @see JComponent.getInputVerifier
     */
    override fun verify(input: JComponent): Boolean
    {
        return try
        {
            val textField = input as JTextField
            val text = textField.text
            text.isValidTeamNumber()
        }
        catch (e: Exception)
        {
            LOG.warn("[FRC] An exception occurred during team number verification: $e", e)
            false
        }
    }

    companion object
    {
        private val LOG = Logger
            .getInstance(TeamNumberInputVerifier::class.java)
    }
}

class TeamNumberKeyChangeListener(private val teamNumberTextField: JTextField, 
                                       private vararg val formChangeListeners: TeamNumberFormChangeListener ) : KeyListener
{
    private var previousText: String

    private val spaceRegex = """\s""".toRegex().toPattern()
    
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
        val updatedText = RegExUtils.replaceAll(teamNumberTextField.text.trim { it <= ' ' }, spaceRegex, "")
            .trim { it <= ' ' }
        teamNumberTextField.text = updatedText // set to the trimmed value - this mostly handles values pasted in with spaces
        if (StringUtils.isBlank(updatedText))
        { 
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
            { 
                //not a valid integer; so replace the text with the previous value (effectively deleting the invalid character)
                teamNumberTextField.text = previousText.trim { it <= ' ' }
            }
        }
        formChangeListeners.forEach { it.onTeamNumberFormChange(teamNumberTextField.text, teamNumberTextField.text.isValidTeamNumber(), previousText) }
    }

    /**
     * Resets the change listener by reinitializing the previousText to the current text of the teamNumberTextField.
     */
    fun reset()
    {
        previousText = teamNumberTextField.text
    }
}

interface TeamNumberFormChangeListener
{
    fun onTeamNumberFormChange(newText: String, isValidTeamNumber: Boolean, previousText: String)
}