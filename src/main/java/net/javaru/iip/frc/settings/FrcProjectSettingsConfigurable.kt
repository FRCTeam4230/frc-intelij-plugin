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

package net.javaru.iip.frc.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.settings.forms.FrcProjectSettingsForm
import javax.swing.JComponent

// This class is registered in the plugin.xml as an <projectConfigurable>
class FrcProjectSettingsConfigurable(val project:Project): SearchableConfigurable
{
    private val myForm = FrcProjectSettingsForm(project)
    
    companion object
    {
        val ID = "preferences.language.FRC.project" // Needs to match the is in the plugin.xml <projectConfigurable>
    }
    
    /**
     * Indicates whether the Swing form was modified or not.
     * This method is called very often, so it should not take a long time.
     *
     * @return `true` if the settings were modified, `false` otherwise
     */
    override fun isModified(): Boolean = myForm.isModified

    /**
     * Loads the settings from the configurable component to the Swing form.
     * This method is called on EDT immediately after the form creation or later upon user's request.
     */
    override fun reset() = myForm.resetForm()

    /**
     * Stores the settings from the Swing form to the configurable component.
     * This method is called on EDT upon user's request.
     *
     * @throws com.intellij.openapi.options.ConfigurationException if values cannot be applied
     */
    override fun apply()
    {
        myForm.applyTo(FrcProjectGeneralSettings.getInstance(project), FrcRoboRioSettings.getInstance(project), FrcSshSettings.getInstance(project))
        // TODO - need to update the wpilib json file - probably best to do that in the FrcProjectGeneralSettings class  
    }
    
    /**
     * Unique configurable id.
     * Note this id should be THE SAME as the one specified in XML.
     * @see com.intellij.openapi.options.ConfigurableEP.id
     */
    override fun getId(): String = ID

    /**
     * Returns the visible name of the configurable component.
     * Note, that this method must return the display name
     * that is equal to the display name declared in XML
     * to avoid unexpected errors.
     *
     * @return the visible name of the configurable component
     */
    override fun getDisplayName(): String = "FRC Project"
    

    

    /**
     * Creates new Swing form that enables user to configure the settings.
     * Usually this method is called on the EDT, so it should not take a long time.
     *
     * Also this place is designed to allocate resources (subscriptions/listeners etc.)
     * @see .disposeUIResources
     *
     *
     * @return new Swing form to show, or `null` if it cannot be created
     */
    override fun createComponent(): JComponent?
    {
        myForm.resetForm() // Not sure if we need this. Need to test: 1) edit form, 2) cancel, 3) reopen
        return myForm.rootPanel
    }

}