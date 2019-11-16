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

package net.javaru.iip.frc.wpilib.retrieval;

import java.net.URI;

import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.settings.FrcApplicationSettingsKt;



class WpiRepoUris
{
    private static final Logger LOG = Logger.getInstance(WpiRepoUris.class);
    public static final String SITE_XML = "site.xml";


    /**
     * Returns the full WPI Eclipse Repo URI configured for use (either release or beta). For example: http://first.wpi.edu/FRC/roborio/release/eclipse/
     *
     * @return the full WPI Eclipse Repo URI configured for use (either release or beta). For example: http://first.wpi.edu/FRC/roborio/release/eclipse/
     */
    @NotNull
    public static URI getActiveRepoUri()
    {
        return useBetaRepo() ? getBetaRepoUri() : getReleaseRepoUri();
    }


    public static boolean useBetaRepo()
    {
        return BooleanUtils.toBoolean(System.getProperty(FrcApplicationSettingsKt.USE_WPILIB_BETA_SITE, "false"));
    }


    /**
     * Returns the full WPI Eclipse <em>beta</em> Repo URI. For example: http://first.wpi.edu/FRC/roborio/beta/eclipse/
     * Generally the {@link #getActiveRepoUri()} should be used unless the beta repo is specifically being queried.
     *
     * @return the full WPI Eclipse <em>beta</em> Repo URI. For example: http://first.wpi.edu/FRC/roborio/beta/eclipse/
     * 
     * @see #getActiveRepoUri() 
     */
    @NotNull
    public static URI getBetaRepoUri()
    {
        return FrcApplicationSettings.getInstance().getWpiEclipsePluginBetaRepoUri();
    }


    /**
     * Returns the full WPI Eclipse <em>release</em> Repo URI. For example: http://first.wpi.edu/FRC/roborio/release/eclipse/
     * Generally the {@link #getActiveRepoUri()} should be used unless the release repo is specifically being queried.
     *
     * @return the full WPI Eclipse <em>release</em> Repo URI. For example: http://first.wpi.edu/FRC/roborio/release/eclipse/
     *
     * @see #getActiveRepoUri()
     */
    @NotNull
    public static URI getReleaseRepoUri()
    {
        return FrcApplicationSettings.getInstance().getWpiEclipsePluginReleaseRepoUri();
    }


//    /**
//     * Returns the base of the WPI Eclipse Repo URI. For example: http://first.wpi.edu
//     *
//     * @return the base of the WPI Eclipse Repo URI. For example: http://first.wpi.edu
//     */
//    @Nullable
//    public static URI getRepoBaseUri()
//    {
//        final URI fullRpoUri = getActiveRepoUri();
//        try
//        {
//            return new URI(fullRpoUri.getScheme(), fullRpoUri.getHost(), null, null);
//        }
//        catch (URISyntaxException e)
//        {
//            //this really should never happen given how we are constructing the URI
//            LOG.warn("[FRC] Could not extract base URI from URI. Summary: " + e.toString(), e);
//            return null;
//        }
//    }
    
    public static URI getSiteUri(@NotNull URI repoBaseUri)
    {
        return repoBaseUri.resolve(SITE_XML);
    }
}
