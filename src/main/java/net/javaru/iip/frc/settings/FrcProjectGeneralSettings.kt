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

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.xmlb.XmlSerializerUtil

// NOTE: This class is registered as a <projectService> in the plugin.xml
@State(name = "FrcProjectGeneralSettings", storages = [(Storage("frc/frc.xml"))])
data class FrcProjectGeneralSettings @JvmOverloads constructor(
       
        var teamNumber: Int = check() /*initTeamNumber(project) */
                                                              ) : PersistentStateComponent<FrcProjectGeneralSettings>
{

    companion object Settings
    {
        val LOG = Logger.getInstance(FrcProjectGeneralSettings::class.java)
        
        @JvmStatic
        fun getInstance(project: Project) = project.service<FrcProjectGeneralSettings>() 

        @JvmStatic
        fun clone(original: FrcProjectGeneralSettings): FrcProjectGeneralSettings = original.copy()
        
        private fun check(): Int = UN_CONFIGURED_TEAM_NUMBER
        
        private fun initTeamNumber(project: Project): Int
        {
            LOG.debug("[FRC] initializing team number for project: $project   Type: ${project::class.java}")
            try
            {
                if (project != ProjectManager.getInstance().defaultProject)
                {
                    // TODO Need to get from the WpiLibPreferences data class once that is done
                }
                
            }
            catch (e: Exception)
            {
                LOG.warn("[FRC] An exception occurred when trying to read team number from wpilib_preferences.json file. Cause summary: $e", e)
            }
            
            
            return UN_CONFIGURED_TEAM_NUMBER;
        }
    }

    /**
     * @return a component state. All properties, public and annotated fields are serialized. Only values, which differ
     * from the default (i.e., the value of newly instantiated class) are serialized. `null` value indicates
     * that the returned state won't be stored, as a result previously stored state will be used.
     * @see com.intellij.util.xmlb.XmlSerializer
     */
    override fun getState(): FrcProjectGeneralSettings
    {
        LOG.trace("[FRC] FrcProjectGeneralSettings.getState() called. Returning current state of: ${toString()}")
        return this
    }

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
    override fun loadState(state: FrcProjectGeneralSettings)
    {
        LOG.trace("[FRC] FrcProjectGeneralSettings.loadState() called with state object of: $state")
        XmlSerializerUtil.copyBean(state, this)
    }

}