/*
 * Copyright 2015-2018 the original author or authors
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

package net.javaru.iip.frc.facet

import com.intellij.facet.FacetConfiguration
import com.intellij.facet.ui.FacetEditorContext
import com.intellij.facet.ui.FacetEditorTab
import com.intellij.facet.ui.FacetValidatorsManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.InvalidDataException
import com.intellij.openapi.util.WriteExternalException
import org.jdom.Element


// TODO need to implement the PersistentStateComponent
class FrcFacetConfiguration : FacetConfiguration// ,PersistentStateComponent<FrcFacetSettings>
{
    companion object
    {
        private val LOG = Logger.getInstance(FrcFacetConfiguration::class.java)

        private val NO_EDITOR_TABS: Array<FacetEditorTab> = emptyArray<FacetEditorTab>()
    }

    var settings: FrcFacetSettings = FrcFacetSettings()
        private set

    override fun createEditorTabs(facetEditorContext: FacetEditorContext, validatorsManager: FacetValidatorsManager): Array<FacetEditorTab>
    {
        //TODO Need to implement the FrcFacetEditorTab
        //return new FacetEditorTab[] {new FrcFacetEditorTab(facetEditorContext, FrcFacetSettings.Settings.INSTANCE())};
        return NO_EDITOR_TABS
    }


    @Deprecated("Is deprecated in API interface ")
    @Throws(InvalidDataException::class)
    override fun readExternal(element: Element)
    {
        LOG.trace("[FRC] Deprecated (and no op) method" + javaClass.simpleName + ".readExternal() called. (Is deprecated in API interface.)")
        /* no op */
    }


    @Deprecated("Is deprecated in API interface ")
    @Throws(WriteExternalException::class)
    override fun writeExternal(element: Element)
    {
        LOG.trace("[FRC] Deprecated (and no op) method" + javaClass.simpleName + ".writeExternal() called. (Is deprecated in API interface.)")
        /* no op */
    }

    
}
