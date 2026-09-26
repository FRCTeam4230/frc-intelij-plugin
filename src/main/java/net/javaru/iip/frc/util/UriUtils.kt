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

import com.intellij.openapi.diagnostic.logger
import java.net.URI
import java.net.URL

private object UriUtils
private val LOG = logger<UriUtils>()

/**
 * Creates a `URI` from a String representation, throwing a Runtime based `IllegalArgumentException` in the event the URI
 * cannot be created. For an option that returns `null` instead of throwing an exception, see [createUriSafely].
 *
 * @see [createUriSafely]
 */
@Throws(IllegalArgumentException::class)
fun createUri(uri: String): URI
{
    return try
    {
        URI(uri)
    }
    catch (e: Exception)
    {
        val baseMsg = "Could not create a URI object from the URL path '$uri' due to the exception: $e"
        LOG.warn("[FRC] $baseMsg")
        throw IllegalArgumentException(baseMsg, e)
    }
}

/**
 * Creates a `URL` from  a String representation, throwing a Runtime based `IllegalArgumentException` in the event the URL
 * cannot be created. For an option that returns `null` instead of throwing an exception, see [createUrlSafely].
 *
 * @see [createUrlSafely]
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
 * Safely creates a `URI` from a String representation, returning `null` in the event the URI cannot be created.
 * For an option that throws an exception and never returns `null`, see [createUri].
 *
 * @see [createUri]
 */
fun createUriSafely(uri: String?):URI?
{
    return if (uri == null) null
    else
    {
        return try
        {
            URI(uri.trim())
        } catch (e: Exception)
        {
            LOG.warn("[FRC] Could not create a URI object from the URI path '$uri' due to the exception: $e")
            null
        }
    }
}

/**
 * Safely creates a `URL` from a String representation, returning `null` in the event the URL cannot be created.
 * For an option that throws an exception and never returns `null`, see [createUrl]
 *
 * @see [createUrl]
 */
fun createUrlSafely(uri: String?):URL?
{
    return if (uri == null) null
    else
    {
        return try
        {
            URL(uri)
        } catch (e: Exception)
        {
            LOG.warn("[FRC] Could not create a URL object from the URL path '$uri' due to the exception: $e")
            null
        }
    }
}

// ALSO SEE UTILS & EXTENSIONS IN IDEA com.intellij.util.Urls.kt

fun java.net.URI.toIdeaUrl(): com.intellij.util.Url?
{
    return try
    {
        com.intellij.util.Urls.parse(this.toString(), false)
    }
    catch (e: Exception)
    {
        LOG.warn("[FRC] Could not convert java.net.URI '$this' to com.intellij.util.Url. Cause Summary: $e", e)
        null
    }
}


fun extractResourceName(uri: URI): String?
{
    val uriString = uri.toString()
    val indexOfLastSlash = uriString.lastIndexOf('/')
    return if (indexOfLastSlash > 0) uriString.substring(indexOfLastSlash + 1) else null
}
