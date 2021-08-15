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

package net.javaru.iip.frc.actions.create.advanced

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import net.javaru.iip.frc.actions.create.advanced.cmdBased.FrcComponentCreationDialog
import javax.swing.AbstractButton
import javax.swing.ButtonGroup
import javax.swing.ButtonModel

@State(name = "ComponentCreationSharedState", storages = [(Storage("frc.xml"))])
data class ComponentCreationSharedState(
        var includeJavaDoc: Boolean = true,
        @OptionTag private var autoAppend: MutableMap<String, Boolean> = mutableMapOf(),
        @OptionTag private var booleanOptions: MutableMap<String, Boolean> = mutableMapOf(),
        @OptionTag private var stringOptions: MutableMap<String, String> = mutableMapOf(),
        @OptionTag private var baseComponent: MutableMap<String, String> = mutableMapOf()
                                       ) : PersistentStateComponent<ComponentCreationSharedState>
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

  
    fun updateBooleanOption(key: String, value: Boolean)
    {
        booleanOptions[key] = value
    }
    
    fun updateBooleanOption(key: String, button: AbstractButton?)
    {
        if (button?.isEnabled == true)
        {
            booleanOptions[key] = button.isSelected
        }
    }
    
    fun getStringOption(key:String, defaultValue: String): String = stringOptions.getOrDefault(key, defaultValue)

    
    fun updateStringOption(key: String, value: String)
    {
        stringOptions[key] = value
    }
    
    /** Updates a String option to the button group's selected model's action command. */
    fun updateStringOption(key: String, buttonGroup: ButtonGroup?)
    {
        if (buttonGroup != null)
        {
            updateStringOption(key, buttonGroup.selection)
        }
    }
    /** Updates a String option to provided model's action command. */
    fun updateStringOption(key: String, buttonModel: ButtonModel?)
    {
        if (buttonModel?.actionCommand != null)
        {
            stringOptions[key] = buttonModel.actionCommand
        }
    }
    
    fun updateBaseComponentSelection(creationDialogClass: Class<out FrcComponentCreationDialog>,
                                     dataProvider: FrcComponentCreationDataProvider,
                                     selectedBaseFqName: String)
    {
        baseComponent[createBaseComponentSelectionKey(creationDialogClass, dataProvider)] = selectedBaseFqName
    }
    
    fun getBaseComponentSelection(creationDialogClass: Class<out FrcComponentCreationDialog>,
                                  dataProvider: FrcComponentCreationDataProvider): String
    {
        return baseComponent[createBaseComponentSelectionKey(creationDialogClass, dataProvider)] ?: dataProvider.typicalBaseClassFqName
    }
    
    private fun createBaseComponentSelectionKey(creationDialogClass: Class<out FrcComponentCreationDialog>,
                                        dataProvider: FrcComponentCreationDataProvider): String
    {
        return "${creationDialogClass.name}--${dataProvider.typicalBaseClassFqName}"
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
        fun getInstance(): ComponentCreationSharedState = service()

        @JvmStatic
        fun clone(original: ComponentCreationSharedState): ComponentCreationSharedState = original.copy()
    }
}