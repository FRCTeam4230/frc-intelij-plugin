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

import com.beust.klaxon.JsonObject
import com.intellij.openapi.diagnostic.logger
import java.net.URI
import java.util.*

private object FrcJsonExts

private val logger = logger<FrcJsonExts>()

fun JsonObject.uuid(fieldName: String): UUID = UUID.fromString(this.string(fieldName))

fun JsonObject.uri(fieldName: String): URI?
{
    val rawUri = this.string(fieldName)?.trim()
    return if (rawUri.isNullOrBlank())
    {
        null
    }
    else
    {
        try
        {
            URI(rawUri)
        }
        catch (e: Exception)
        {
            logger.warn("[FRC] Could not convert '$rawUri' to URI: $e")
            null
        }
    }
}

fun JsonObject.urisList(fieldName: String): List<URI>
{
    val strings = this.array<String>(fieldName)?.toList() ?: emptyList<String>()
    return strings.mapNotNull { createUriSafely(it.trim()) }
}