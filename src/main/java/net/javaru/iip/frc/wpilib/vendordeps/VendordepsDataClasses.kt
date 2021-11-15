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
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.intellij.json.psi.JsonFile
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import net.javaru.iip.frc.util.uri
import net.javaru.iip.frc.util.urisList
import java.io.InputStream
import java.io.Reader
import java.io.StringReader
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.name


data class Vendordeps(
    val uuid: UUID,
    val name: String,
    val version: LibVersion,
    val fileName: String,
    val jsonUrl: URI?,
    val mavenUrls: List<URI>
                     ) : Comparable<Vendordeps>
{
    companion object
    {
        private val nonConformingUuidsMap = mutableMapOf<String, UUID>()
        private val parser: Parser = Parser.default()
        private val LOG = logger<Vendordeps>()

        // @formatter:off
        fun parse(jsonFile: PsiFile): Result<Vendordeps, Throwable> = transform(parser.parse(jsonFile.virtualFile.inputStream) as JsonObject)
        fun parse(jsonFile: VirtualFile): Result<Vendordeps, Throwable> = transform(parser.parse(jsonFile.inputStream) as JsonObject)
        fun parse(jsonFile: Path, charset: Charset = Charsets.UTF_8): Result<Vendordeps, Throwable> = transform(parser.parse(Files.newBufferedReader(jsonFile, charset)) as JsonObject)
        fun parse(json: InputStream, charset: Charset = Charsets.UTF_8): Result<Vendordeps, Throwable> = transform(parser.parse(json, charset) as JsonObject)
        fun parse(json: Reader): Result<Vendordeps, Throwable> = transform(parser.parse(json) as JsonObject)
        fun parse(json: String): Result<Vendordeps, Throwable> = transform(parser.parse(StringReader(json)) as JsonObject)
        // @formatter:on

        fun transform(json: JsonObject): Result<Vendordeps, Throwable>
        {
            return try
            {
                val name = json.string("name")?.trim() ?: "unknown-name"
                val version = LibVersion.parse(json.string("version")?.trim() ?: "0.0.0")
                val fileName = json.string("fileName")?.trim() ?: "unknown.json"
                val jsonUrl = json.uri("jsonUrl")
                val mavenUrls = json.urisList("mavenUrls")
                val uuid = json.parseUuid(name, fileName, jsonUrl)

                val vendordeps = Vendordeps(uuid, name, version, fileName, jsonUrl, mavenUrls)
                Ok(vendordeps)
            }
            catch (t: Throwable)
            {
                LOG.info("[FRC] An exception occurred when parsing Vendordeps JSON: $t")
                Err(t)
            }
        }

        const val navxUuidString = "cb311d09-36e9-4143-a032-55bb2b94443b"
        val navxUuid: UUID = UUID.fromString(navxUuidString)
        val dmc60cRemappedUuid: UUID = UUID.fromString("d2dafb2b-4b81-40d1-98ff-7e66289fcfb4")
        val libCuRemappedUuid: UUID = UUID.fromString("ba9f250f-1ebc-4897-9782-d3f4517df53b")

        private fun JsonObject.parseUuid(name: String, fileName: String, jsonUrl: URI?): UUID
        {
            val uuidString = this.string("uuid")?.trim()
            // We have to handle some special cases
            @Suppress("SpellCheckingInspection")
            return when
            {
                uuidString == null                                     -> nonConformingUuidsMap.computeIfAbsent(name) { UUID.randomUUID() }
                // the  Coppersource "LibCu" library has an invalid UUID. I've opened an issue: https://github.com/Coppersource/LibCu/issues/4
                // for now we just map it to another UUID
                (uuidString == "libcufrc-e6e8-4db6-89f0-copperforge0") -> libCuRemappedUuid
                // Honor the navX UUID when in the navX file
                (uuidString == navxUuidString &&
                    (
                        name.contains("navX", ignoreCase = true)) ||
                    fileName.contains("navX", ignoreCase = true) ||
                    jsonUrl?.toString()?.contains("kauailabs", ignoreCase = true) ?: false ||
                    jsonUrl?.toString()?.contains("navX", ignoreCase = true) ?: false
                    )                                                  -> navxUuid
                // Handle the known duplicate UUID of the DMC60C lib: https://github.com/Digilent/dmc60c-frc-api/issues/13
                (uuidString == navxUuidString &&
                    (
                        name.contains("DMC60C", ignoreCase = true)) ||
                    fileName.contains("DMC60C", ignoreCase = true) ||
                    jsonUrl?.toString()?.contains("DMC60C", ignoreCase = true) ?: false
                    )                                                  -> dmc60cRemappedUuid
                // Handle any other duplicates of the navx UUID
                uuidString == navxUuidString                           -> nonConformingUuidsMap.computeIfAbsent(name) { UUID.randomUUID() }
                else                                                   -> try
                {
                    UUID.fromString(uuidString)
                }
                catch (e: Exception)
                {
                    LOG.warn("[FRC] Vendordeps UUID string '$uuidString' could not be converted to UUID. Reason: $e")
                    nonConformingUuidsMap.computeIfAbsent(name) { UUID.randomUUID() }
                }
            }
        }
    }

    override fun compareTo(other: Vendordeps): Int = compareValuesBy(this, other, {it.uuid}, {it.version})

    /** Returns a Simple String of: $name: $version */
    fun toStringSimple(): String = "$name : $version"
}
open class VendordepsFile(val file: Path, val vendordeps: Vendordeps): Comparable<VendordepsFile>
{
    override fun compareTo(other: VendordepsFile): Int = compareValuesBy(this, other, { it.vendordeps }, { it.file })


    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (other !is VendordepsFile) return false

        if (file != other.file) return false
        if (vendordeps != other.vendordeps) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = file.hashCode()
        result = 31 * result + vendordeps.hashCode()
        return result
    }

    override fun toString(): String
    {
        return "$vendordeps [${file.name}]"
    }
}


/**
 * A data class to virtually represent a Vendordeps file. It contains the properties:
 * @param jsonPsiFile the [JsonFile] (sub-interface of [PsiFile]) for the vendordeps file
 * @param vendordeps a [Vendordeps] data class representing the content of the vendordeps file
 */
class VendordepsProjectFile(val jsonPsiFile: JsonFile, vendordeps: Vendordeps) : VendordepsFile(jsonPsiFile.virtualFile.toNioPath(), vendordeps)

/** A data class to represent a known Vendordeps library. */
data class KnownVendordepsInfo(
    val uuid:UUID,
    val name: String,
    val jsonUrl: URI,
    
    
                               ) : Comparable<KnownVendordepsInfo>
{
    override fun compareTo(other: KnownVendordepsInfo): Int = compareValuesBy(this, other, {it.uuid}, {it.name})

    
}


