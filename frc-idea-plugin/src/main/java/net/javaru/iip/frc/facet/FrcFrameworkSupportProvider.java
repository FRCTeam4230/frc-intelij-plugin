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

import com.intellij.facet.ui.FacetBasedFrameworkSupportProvider;
import com.intellij.ide.util.frameworkSupport.FrameworkVersion;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.roots.ModifiableRootModel;



public class FrcFrameworkSupportProvider extends FacetBasedFrameworkSupportProvider<FrcFacet>
{
    //For example, see StrutsFrameworkSupportProvider fot Structs2 plugin in jetbrains open source plugins
    
    private static final Logger LOG = Logger.getInstance(FrcFrameworkSupportProvider.class);


    protected FrcFrameworkSupportProvider()
    {
        super(FrcFacetType.getInstance());
    }

     

    @Override
    protected void setupConfiguration(FrcFacet facet, ModifiableRootModel rootModel, FrameworkVersion version)
    {
        
    }
}
