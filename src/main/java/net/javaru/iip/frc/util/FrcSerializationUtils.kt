/*
 * Copyright 2015-2020 the original author or authors
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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.datatype.guava.GuavaModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import net.javaru.iip.frc.wpilib.gradlePluginRepo.MavenMetadata
import org.intellij.lang.annotations.Language
import org.jetbrains.annotations.Contract
import java.awt.Point


val mapper: ObjectMapper = ObjectMapper().registerKotlinModule().registerModule(GuavaModule())//.registerModule(JodaModule())!!

/**
 * @sample SerializationExamples.prettyPrintWriterExample
 *
 */
val prettyPrintWriter: ObjectWriter = mapper.writerWithDefaultPrettyPrinter()

/**
 * Returns the receiver object as JSON. If pretty printing is uses, system line ending are used.
 * If specific line endings are needed use the [eolTo] extension (in `FrcLangExts`) to convert the
 * line endings of the returned string.
 *
 * @sample SerializationExamples.toJsonExample
 */
@Language("JSON")
fun Any.toJson(prettyPrinted: Boolean = false): String
{
    val writer: ObjectWriter = if (prettyPrinted) prettyPrintWriter else mapper.writer()
    return writer.writeValueAsString(this)
}

/**
 * Returns the receiver object as JSON. If pretty printing is uses, system line ending are used.
 * If specific line endings are needed use the [eolTo] extension (in `FrcLangExts`) to convert the
 * line endings of the returned string.
 *
 * @sample SerializationExamples.toJsonOrNullExample
 */
@Contract("null,_ -> null, !null,_ -> !null")
@Language("JSON")
fun Any?.toJsonOrNull(prettyPrinted: Boolean = false): String? = this?.toJson(prettyPrinted)


/** @sample SerializationExamples.fromJsonExample */
inline fun <reified T> String.fromJson(): T = mapper.readValue<T>(this)

/** @sample SerializationExamples.fromJsonOrNullExample */
@Contract("null -> null, !null -> !null")
inline fun <reified T> String?.fromJsonOrNull(): T? = if (this == null) null else mapper.readValue<T>(this)



@Suppress("UNUSED_VARIABLE")
internal object SerializationExamples
{
    private val someObject: Any = Any()
    private val possibleNullObject: Any? = Any()

    @Language("JSON")
    private val jsonString: String = """{ "x": 5, "y": 10 }"""

    fun prettyPrintWriterExample(prettyPrint: Boolean)
    {
        // Directly
        @Language("JSON")
        val json1 = prettyPrintWriter.writeValueAsString(someObject)

        // With a prettyPrint option
        val writer: ObjectWriter = if (prettyPrint) prettyPrintWriter else mapper.writer()
        @Language("JSON")
        val json2 = writer.writeValueAsString(someObject)
    }

    fun toJsonExample()
    {
        val json1 = someObject.toJson()
        val json2 = someObject.toJson(prettyPrinted = true)
        val json3 = someObject.toJson(prettyPrinted = true).eolTo(EOL.UNIX)
    }

    fun toJsonOrNullExample()
    {
        val json1: String? = possibleNullObject?.toJsonOrNull()
        val json2: String? = possibleNullObject?.toJson(prettyPrinted = true)
        val json3: String? = possibleNullObject?.toJson(prettyPrinted = true)?.eolTo(EOL.UNIX)
    }

    fun fromJsonExample()
    {
        val point = jsonString.fromJson<Point>()
    }

    fun fromJsonOrNullExample()
    {
        val point = jsonString.fromJsonOrNull<Point>()
    }
}