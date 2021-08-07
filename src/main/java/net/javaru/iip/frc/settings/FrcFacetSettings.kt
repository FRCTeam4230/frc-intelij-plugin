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

package net.javaru.iip.frc.settings

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.util.xmlb.XmlSerializerUtil


data class FrcFacetSettings(val unusedPlaceholder: Int = 0) : PersistentStateComponent<FrcFacetSettings>
{
    companion object Settings
    {
        fun getInstance(): FrcFacetSettings
        {
            return ServiceManager.getService(FrcFacetSettings::class.java)
        }

        fun clone(original: FrcFacetSettings): FrcFacetSettings
        {
            return original.copy()
        }
    }


    private val logger = logger <FrcFacetSettings>()

    override fun getState(): FrcFacetSettings
    {
        logger.trace {"[FRC] FrcFacetSettings.getState() called. Returning current state of: ${toString()}"}
        return this
    }

    override fun loadState(state: FrcFacetSettings)
    {
        logger.trace {"[FRC] FrcFacetSettings.loadState() called with state object of: $state"}
        XmlSerializerUtil.copyBean<FrcFacetSettings>(state, this)
    }
}