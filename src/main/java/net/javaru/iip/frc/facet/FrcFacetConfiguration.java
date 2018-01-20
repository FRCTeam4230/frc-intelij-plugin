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

package net.javaru.iip.frc.facet;

import org.jdom.Element;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;



// TODO need to implement the PersistentStateComponent
public class FrcFacetConfiguration implements FacetConfiguration// ,PersistentStateComponent<FrcFacetSettings>
{
    private static final Logger LOG = Logger.getInstance(FrcFacetConfiguration.class);

    private static final FacetEditorTab[] NO_EDITOR_TABS = new FacetEditorTab[0];


    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext facetEditorContext, FacetValidatorsManager validatorsManager)
    {
        //TODO Need to implement the FrcFacetEditorTab
        //return new FacetEditorTab[] {new FrcFacetEditorTab(facetEditorContext, FrcFacetSettings.Settings.INSTANCE())};
        return NO_EDITOR_TABS;
    }


    /** @deprecated  */
    @Deprecated
    @Override
    public void readExternal(Element element) throws InvalidDataException
    {
        LOG.trace("[FRC] Deprecated (and no op) method" + getClass().getSimpleName() + ".readExternal() called");
        /* no op */
    }


    /** @deprecated  */
    @Deprecated
    @Override
    public void writeExternal(Element element) throws WriteExternalException
    {
        LOG.trace("[FRC] Deprecated (and no op) method" + getClass().getSimpleName() + ".writeExternal() called");
        /* no op */
    }
}
