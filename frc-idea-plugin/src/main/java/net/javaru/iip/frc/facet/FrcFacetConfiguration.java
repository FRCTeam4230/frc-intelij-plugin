/*
 * Copyright 2015 Mark Vedder
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
import org.jetbrains.annotations.Nullable;
import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.xmlb.XmlSerializerUtil;

import net.javaru.iip.frc.facet.ui.FrcFacetEditorTab;



public class FrcFacetConfiguration implements FacetConfiguration,
                                              PersistentStateComponent<FrcFacetSettings>
{

    private static final FacetEditorTab[] NO_EDITOR_TABS = new FacetEditorTab[0];
    private final FrcFacetSettings myFrcFacetSettings = new FrcFacetSettings();
    
    @Override
    public FacetEditorTab[] createEditorTabs(FacetEditorContext facetEditorContext, FacetValidatorsManager facetValidatorsManager)
    {
        return new FacetEditorTab[] {new FrcFacetEditorTab(facetEditorContext, this)};
        // return NO_EDITOR_TABS;
    }


    @Nullable
    @Override
    public FrcFacetSettings getState()
    {
        return myFrcFacetSettings;
    }


    @Override
    public void loadState(FrcFacetSettings state)
    {
        //If there are any problems with this, we can just remove the final declaration on myFrcFacetSettings and do: myFrcFacetSettings = state;
        XmlSerializerUtil.copyBean(state, myFrcFacetSettings);
    }

    
    
    /** @deprecated  */
    @Deprecated
    @Override
    public void readExternal(Element element) throws InvalidDataException { /* no op */ }


    /** @deprecated  */
    @Deprecated
    @Override
    public void writeExternal(Element element) throws WriteExternalException { /* no op */ }


}
