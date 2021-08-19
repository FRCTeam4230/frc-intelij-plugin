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

package net.javaru.iip.frc.wpilib.vendordeps


import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import net.javaru.iip.frc.util.uri
import net.javaru.iip.frc.util.urisList
import net.javaru.iip.frc.util.uuid
import org.apache.commons.lang3.builder.CompareToBuilder
import java.io.InputStream
import java.io.Reader
import java.io.StringReader
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.util.*


data class Vendordeps(val uuid: UUID, val name: String, val version: LibVersion, val fileName: String, val jsonUrl: URI?, val mavenUrls: List<URI>) : Comparable<Vendordeps>
{
    companion object
    {
        private val parser: Parser = Parser.default()
        fun parse(jsonFile: Path, charset: Charset = Charsets.UTF_8): Vendordeps = transform(parser.parse(Files.newBufferedReader(jsonFile, charset)) as JsonObject)
        fun parse(json: InputStream, charset: Charset = Charsets.UTF_8): Vendordeps = transform(parser.parse(json, charset) as JsonObject)
        fun parse(json: Reader): Vendordeps = transform(parser.parse(json) as JsonObject)
        fun parse(json: String): Vendordeps = transform(parser.parse(StringReader(json)) as JsonObject)

        fun transform(json: JsonObject): Vendordeps
        {
            return Vendordeps(
                uuid = json.uuid("uuid"),
                name = json.string("name") ?: "unknown-name",
                version = LibVersion.parse(json.string("version") ?: "0.0.0"),
                fileName = json.string("fileName") ?: "unknown.json",
                jsonUrl = json.uri("jsonUrl"),
                mavenUrls = json.urisList("mavenUrls")
                             )
        }
    }

    override fun compareTo(other: Vendordeps): Int
    {
        return CompareToBuilder()
            .append(this.name, other.name)
            .append(this.version, other.version)
            .toComparison()
    }
}