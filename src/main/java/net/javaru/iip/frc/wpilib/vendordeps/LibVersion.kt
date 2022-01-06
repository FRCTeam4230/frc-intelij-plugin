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


import com.asarkar.semver.SemVer
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import kotlin.streams.toList

/**
 * A version class for libraries and dependencies that handles not standard version schemes. If the version text
 * is a valid Semantic Version, a [SemVer] is used to provide functionality for comparison, equality, and hashing.
 * If the text does not represent a valid Semantic Version, the text representation itself is used for
 * comparison, equality, and hashing. This works sufficiently in most cases.
 */
class LibVersion private constructor(val asText: String, private val backingSemVer: SemVer? = null) : Comparable<LibVersion>
{
    override fun compareTo(other: LibVersion): Int
    {
        return if (backingSemVer != null && other.backingSemVer != null)
        {
            backingSemVer.compareTo(other.backingSemVer)
        }
        else
        {
            asText.compareTo(other.asText)
        }
    }

    override fun equals(other: Any?): Boolean
    {
        if (other == null) return false
        if (other !is LibVersion) return false
        return if (backingSemVer != null && other.backingSemVer != null)
        {
            backingSemVer == other.backingSemVer
        }
        else
        {
            asText == other.asText
        }
    }

    override fun hashCode(): Int = backingSemVer?.hashCode() ?: asText.hashCode()

    override fun toString(): String = asText + if (backingSemVer != null) " [$backingSemVer]" else ""

    companion object
    {
        private val logger = logger<LibVersion>()
        private val leadingZeroRegex = """^0+(?!${'$'})""".toRegex()

        fun parse(text: String): LibVersion
        {
            val parts = text.removePrefix("v").removePrefix("V")
                .split('.')
                .stream()
                .map {
                    // remove leading zeros
                    val modified = it.replace(leadingZeroRegex, "")
                    if (modified.isEmpty()) "0" else modified
            }.toList().toMutableList()

            if (parts.size < 3)
            {
                val needed = 3 - parts.size
                for (i in 1 .. needed)
                {
                    parts.add("0")
                }
            }

            val iterator = parts.iterator()
            val sb = StringBuilder()
            while (iterator.hasNext())
            {
                sb.append(iterator.next())
                if (iterator.hasNext())
                {
                    sb.append('.')
                }
            }
            val normalized = sb.toString()
            val semVer: SemVer? = try
            {
                SemVer.parse(normalized)
            }
            catch (t: Throwable)
            {
                logger.trace{"[FRC] Could not parse '$text' (normalized to '$normalized' to a SemVer. Will treat as simple text. Cause: $t"}
                null
            }
            return LibVersion(text, semVer)
        }

        fun fromSemVer(semVer: SemVer): LibVersion = LibVersion(semVer.toString(), semVer)

        fun fromSemVerAltText(semVer: SemVer, text: String): LibVersion = LibVersion(text, semVer)
    }

}