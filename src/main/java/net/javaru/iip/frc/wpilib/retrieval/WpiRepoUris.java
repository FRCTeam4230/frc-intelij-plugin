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

package net.javaru.iip.frc.wpilib.retrieval;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.settings.FrcApplicationSettingsKt;



class WpiRepoUris
{
    private static final Logger LOG = Logger.getInstance(WpiRepoUris.class);


    /**
     * Returns the full WPI Eclipse Repo URI. For example: http://first.wpi.edu/FRC/roborio/release/eclipse/
     *
     * @return the full WPI Eclipse Repo URI. For example: http://first.wpi.edu/FRC/roborio/release/eclipse/
     */
    public static URI getRepoFullUri()
    {
        return (BooleanUtils.toBoolean(System.getProperty(FrcApplicationSettingsKt.USE_WPILIB_BETA_SITE, "false")))
               ? FrcApplicationSettings.Settings.INSTANCE().getWpiEclipsePluginBetaRepoUri()
               : FrcApplicationSettings.Settings.INSTANCE().getWpiEclipsePluginReleaseRepoUri();
    }


    /**
     * Returns the base of the WPI Eclipse Repo URI. For example: http://first.wpi.edu
     *
     * @return the base of the WPI Eclipse Repo URI. For example: http://first.wpi.edu
     */
    @Nullable
    public static URI getRepoBaseUri()
    {
        final URI fullRpoUri = getRepoFullUri();
        try
        {
            return new URI(fullRpoUri.getScheme(), fullRpoUri.getHost(), null, null);
        }
        catch (URISyntaxException e)
        {
            //this really should never happen given how we are constructing the URI
            LOG.warn("[FRC] Could not extract base URI from URI. Summary: " + e.toString(), e);
            return null;
        }
    }


    public static URI getRepoSiteXmlFileUri()
    {
        return getRepoFullUri().resolve("site.xml");
    }


}
