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

package net.javaru.iip.frc.facet.framework.detector;

import org.jetbrains.annotations.NotNull;
import com.intellij.facet.FacetType;
import com.intellij.framework.detection.FacetBasedFrameworkDetector;

import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.facet.FrcFacetConfiguration;
import net.javaru.iip.frc.facet.FrcFacetType;



public abstract class FrcAbstractFrameworkDetector extends FacetBasedFrameworkDetector<FrcFacet, FrcFacetConfiguration>
{
    FrcAbstractFrameworkDetector(String detectorId, int detectorVersion) {super(detectorId, detectorVersion);}


    @NotNull
    @Override
    public FacetType<FrcFacet, FrcFacetConfiguration> getFacetType()
    {
        return FrcFacetType.getInstance();
    }
}
