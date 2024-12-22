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

package net.javaru.iip.frc.facet.framework;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.facet.ui.FacetBasedFrameworkSupportProvider;
import com.intellij.framework.library.DownloadableLibraryType;
import com.intellij.framework.library.LibraryBasedFrameworkType;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.facet.FrcFacet;


// TODO - need to finish seeing if this is something we want/need to implement/use See Issue 106: https://gitlab.com/Javaru/frc-intellij-idea-plugin/-/issues/106
public class FrcLibraryBasedFrameworkType extends LibraryBasedFrameworkType
{
    private static final Logger LOG = Logger.getInstance(FrcLibraryBasedFrameworkType.class);


    protected FrcLibraryBasedFrameworkType(@NotNull String id, Class<? extends DownloadableLibraryType> libraryTypeClass)
    {
        super(id, libraryTypeClass);
    }


    @NotNull
    @Override
    public String getPresentableName()
    {
        return FrcFacet.FACET_NAME;
    }


    @Nullable
    @Override
    public String getUnderlyingFrameworkTypeId()
    {
        return FacetBasedFrameworkSupportProvider.getProviderId(FrcFacet.Companion.getFACET_TYPE_ID());
    }
}
