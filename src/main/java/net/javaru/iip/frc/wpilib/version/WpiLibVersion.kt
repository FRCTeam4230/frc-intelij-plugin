/*
 * Copyright 2015-2018 the original author or authors
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

package net.javaru.iip.frc.wpilib.version

import org.apache.commons.lang3.builder.CompareToBuilder


interface WpiLibVersion : Comparable<WpiLibVersion>
{
    val generation: Int

    val versionString: String

    val major: Int

    val minor: Int

    val patch: Int

    val preReleaseModifier: PreReleaseModifier?

    val preReleaseModifierVersion: Int?
    
    val preReleaseModifierSubVersion: String
    
    val preReleasePreviewVersion: Int?
    
    fun isPreRelease(): Boolean
    {
        return preReleaseModifier != null
    }
    
    fun isPreReleasePreview(): Boolean
    {
        return isPreRelease() && preReleasePreviewVersion != null
    }
    
    override fun compareTo(other: WpiLibVersion): Int
    {

        return CompareToBuilder()
                .append(this.generation, other.generation)
                .append(this.major, other.major)
                .append(this.minor, other.minor)
                .append(this.patch, other.patch)
                .append(if (this.isPreRelease()) this.preReleaseModifier!!.ordinal else Integer.MAX_VALUE,
                        if (other.isPreRelease()) other.preReleaseModifier!!.ordinal else Integer.MAX_VALUE)
                .append(if (this.isPreRelease()) this.preReleaseModifierVersion else Integer.MAX_VALUE,
                        if (other.isPreRelease()) other.preReleaseModifierVersion else Integer.MAX_VALUE)
                .append(if (this.isPreRelease()) this.preReleaseModifierSubVersion else "",
                        if (other.isPreRelease()) other.preReleaseModifierSubVersion else "")
                .append(if (this.isPreReleasePreview()) this.preReleasePreviewVersion else Integer.MAX_VALUE,
                        if (other.isPreReleasePreview()) other.preReleasePreviewVersion else Integer.MAX_VALUE)
                .toComparison()

    }


    @Suppress("EnumEntryName")
    enum class PreReleaseModifier
    {
        alpha, beta, rc
    }

    fun isSameAs(other: WpiLibVersion): Boolean
    {
        return compareTo(other) == 0
    }

    fun isSameOrNewerThan(other: WpiLibVersion): Boolean
    {
        return compareTo(other) >= 0
    }

    fun isNewerThan(other: WpiLibVersion): Boolean
    {
        return compareTo(other) > 0
    }

    fun isSameOrOlderThan(other: WpiLibVersion): Boolean
    {
        return compareTo(other) <= 0
    }

    fun isOlderThan(other: WpiLibVersion): Boolean
    {
        return compareTo(other) < 0
    }

    fun cloneIt(): WpiLibVersion

}
