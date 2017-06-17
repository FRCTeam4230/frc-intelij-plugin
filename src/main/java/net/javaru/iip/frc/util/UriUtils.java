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

package net.javaru.iip.frc.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;



public final class UriUtils 
{
    private static final Logger LOG = Logger.getInstance(UriUtils.class);


    private UriUtils() { }


    @NotNull
    public static URI resolveSiblingResource(@NotNull URI uri, @NotNull String siblingResource) throws RuntimeException
    {
        try
        {
            LOG.debug("uri =          " +  uri);


            final String pathString = uri.getPath();
            final Path path = Paths.get(pathString);
            LOG.debug("path =         " +  path);
            LOG.debug("host =         " +  uri.getHost());

            //The File System wil "normalize" to the proper forward or back slash
            final Path rootPath = Paths.get("/");
            final URI siblingUri;
            if (rootPath.equals(path) || StringUtils.isBlank(path.toString()))
            {
                siblingUri = new URI(uri.getScheme(),
                                     uri.getHost(),
                                     '/' + siblingResource,
                                     null);
            }
            else
            {
                Path sibling = path.resolveSibling(siblingResource);
                siblingUri = new URI(uri.getScheme(),
                                     uri.getHost(),
                                     sibling.toString().replace('\\', '/'),
                                     null);
            }
            LOG.debug("siblingUri =   " +  siblingUri);
            return siblingUri;
        }
        catch (URISyntaxException e)
        {
            throw new RuntimeException("Could not resolve sibling URI. Cause summary: " + e.toString(), e);
        }
    }

    
    @Nullable
    public static String extractResourceName(@NotNull URI uri)
    {
        final String uriString = uri.toString();
        final int indexOfLastSlash = uriString.lastIndexOf('/');
        return indexOfLastSlash > 0 ? uriString.substring(indexOfLastSlash + 1) : null;
    }


    @NotNull
    public static URI createUri(String url) throws IllegalArgumentException
    {
        try
        {
            return new URI(url);
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] Could not create URL from '" + url + '\'');
            throw new IllegalArgumentException("Could not create URL from '" + url + "'. Cause summary: " + e.toString(), e);
        }
    }
}
