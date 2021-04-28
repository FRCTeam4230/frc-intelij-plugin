/*
 * Copyright 2015-2021 the original author or authors.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package net.javaru.iip.frc.util

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import org.apache.commons.lang3.StringUtils
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.nio.file.Paths

private object UriUtils
private val LOG = logger<UriUtils>()

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
        val baseMsg = "Could not create a URI object from the URL path '$url' due to the exception: $e"
        LOG.warn("[FRC] $baseMsg")
        throw IllegalArgumentException(baseMsg, e)
    }
}

/**
 * Creates a `URL` from ` `URL` throwing a Runtime based `IllegalArgumentException` in the event the URL
 * cannot be created.
 */
@Throws(IllegalArgumentException::class)
fun createUrl(url: String): URL
{
    return try
    {
        URL(url)
    }
    catch (e: Exception)
    {
        val baseMsg = "Could not create a URL object from the URL path '$url' due to the exception: $e"
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
        LOG.debug {"    [FRC] uri =          $this"}
        val pathString = path
        val path = Paths.get(pathString)
        LOG.debug {"    [FRC] path =         $path"}
        LOG.debug {"    [FRC] host =         $host"}
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
        LOG.debug {"    [FRC] siblingUri =   $siblingUri"}
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
