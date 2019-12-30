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

package net.javaru.iip.frc.actions.create.advanced

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import javax.swing.AbstractButton

@State(name = "ComponentCreationSharedState", storages = [(Storage("frc.xml"))])
data class ComponentCreationSharedState(
        var includeJavaDoc: Boolean = true,
        @OptionTag private var autoAppend: MutableMap<String, Boolean> = mutableMapOf(),
        @OptionTag private var booleanOptions: MutableMap<String, Boolean> = mutableMapOf()) :
        PersistentStateComponent<ComponentCreationSharedState>
{
    fun getShouldAutoAppend(suffix: String) = autoAppend.getOrDefault(suffix, true)
    
    /** Updates the shouldAutoAppendValue to the buttons selection iff the button is enabled. */
    fun updateShouldAutoAppend(suffix: String, button: AbstractButton)
    {
       if (button.isEnabled)
       {
           autoAppend[suffix] = button.isSelected
       } 
    }
    
    @JvmOverloads
    fun getBooleanOption(key:String, defaultValue: Boolean = true): Boolean = booleanOptions.getOrDefault(key, defaultValue)

    /** Updates the shouldAutoAppendValue to the buttons selection iff the button is not null && enabled. */
    fun updateBooleanOption(key: String, button: AbstractButton?)
    {
        if (button?.isEnabled == true)
        {
            booleanOptions[key] = button.isSelected
        }
    }
    
    /**
     * @return a component state. All properties, public and annotated fields are serialized. Only values, which differ
     * from the default (i.e., the value of newly instantiated class) are serialized. `null` value indicates
     * that the returned state won't be stored, as a result previously stored state will be used.
     * @see com.intellij.util.xmlb.XmlSerializer
     */
    override fun getState(): ComponentCreationSharedState? = this

    /**
     * This method is called when new component state is loaded. The method can and will be called several times, if
     * config files were externally changed while IDE was running.
     *
     *
     * State object should be used directly, defensive copying is not required.
     *
     * @param state loaded component state
     * @see com.intellij.util.xmlb.XmlSerializerUtil.copyBean
     */
    override fun loadState(state: ComponentCreationSharedState) = XmlSerializerUtil.copyBean(state, this)
    
    companion object
    {
        @JvmStatic
        fun getInstance(): ComponentCreationSharedState = ServiceManager.getService(ComponentCreationSharedState::class.java)

        @JvmStatic
        fun clone(original: ComponentCreationSharedState): ComponentCreationSharedState = original.copy()
    }
}