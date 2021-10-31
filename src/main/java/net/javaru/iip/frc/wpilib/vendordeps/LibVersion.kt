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

/**
 * A version class for libraries and dependencies that handles not standard version schemes. If the version text
 * is a valid Semantic Version, a [SemVer] is used to provide functionality for comparison, equality, and hashing.
 * If the text does not represent a valid Semantic Version, the text representation itself is used for
 * comparison, equality, and hashing. This works sufficiently in most cases.
 */
class LibVersion private constructor(private val asText: String, private val backingSemVer: SemVer? = null) : Comparable<LibVersion>
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

    override fun toString(): String = asText

    companion object
    {
        private val logger = logger<LibVersion>()

        fun parse(text: String): LibVersion
        {
            val semVer: SemVer? = try
            {
                SemVer.parse(text)
            }
            catch (t: Throwable)
            {
                logger.trace{"[FRC] Could not parse '$text' to a SemVer. Will treat as simple text. Cause: $t"}
                null
            }
            return LibVersion(text, semVer)
        }

        fun fromSemVer(semVer: SemVer): LibVersion = LibVersion(semVer.toString(), semVer)
    }

}