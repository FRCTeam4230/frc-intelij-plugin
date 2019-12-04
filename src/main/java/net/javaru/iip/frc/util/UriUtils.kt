/*
 * Copyright 2015-2019 the original author or authors
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
package net.javaru.iip.frc.util

import com.intellij.openapi.diagnostic.Logger
import org.apache.commons.lang3.StringUtils
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.Paths

private object UriUtils
private val LOG = Logger.getInstance(UriUtils::class.java)

/**
 * Creates a `URI` from ` `URL` throwing a Runtime based `IllegalArgumentException` in the event the URI
 * cannot be created.
 */
@Throws(IllegalArgumentException::class)
fun createUri(url: String): URI
{
    return try
    {
        URI(url)
    }
    catch (e: Exception)
    {
        val baseMsg = "Could not create a URI from the URL '$url' due to the exception: $e"
        LOG.warn("[FRC] $baseMsg")
        throw IllegalArgumentException(baseMsg, e)
    }
}

/**
 * Resolves a sibling URI, paying attention to whether a relative or absolute siblingResource is passed in.
 * For example given a `receiver` URI of `https://example.com/data/foo.txt`:
 * - Relative: `uri.resolveSiblingResource("images/chart.png")`  -->  https://example.com/data/images/chart.png
 * - Absolute: `uri.resolveSiblingResource("/images/chart.png")` -->  https://example.com/images/chart.png
 */
@Deprecated("Use URI.resolve() instead", ReplaceWith("URI.resolve(siblingResource)"), level = DeprecationLevel.ERROR)
@Throws(RuntimeException::class)
fun URI.resolveSiblingResource(siblingResource: String): URI
{
    return try
    {
        LOG.debug("    [FRC] uri =          $this")
        val pathString = path
        val path = Paths.get(pathString)
        LOG.debug("    [FRC] path =         $path")
        LOG.debug("    [FRC] host =         $host")
        //The File System wil "normalize" to the proper forward or back slash
        val rootPath = Paths.get("/")
        val siblingUri: URI
        siblingUri = if (rootPath == path || StringUtils.isBlank(path.toString()))
        {
            URI(scheme,
                host, "/$siblingResource",
                null)
        }
        else
        {
            val sibling = path.resolveSibling(siblingResource)
            URI(scheme,
                host,
                sibling.toString().replace('\\', '/'),
                null)
        }
        LOG.debug("    [FRC] siblingUri =   $siblingUri")
        siblingUri
    }
    catch (e: URISyntaxException)
    {
        val baseMsg = "Could not resolve sibling URI '$siblingResource' for URI '$this' due to the exception: $e."
        LOG.warn("[FRC] $baseMsg")
        throw RuntimeException(baseMsg, e)
    }
}



fun extractResourceName(uri: URI): String?
{
    val uriString = uri.toString()
    val indexOfLastSlash = uriString.lastIndexOf('/')
    return if (indexOfLastSlash > 0) uriString.substring(indexOfLastSlash + 1) else null
}
