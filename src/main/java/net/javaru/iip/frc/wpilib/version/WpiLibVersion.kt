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

package net.javaru.iip.frc.wpilib.version

import org.apache.commons.lang3.builder.CompareToBuilder


interface WpiLibVersion : Comparable<WpiLibVersion>
{
    /**
     * The version generation:
     *   - 2015 : indicates a 2015 or 2016 version when a timestamp version number was used, such as `0.1.0.201502241928` 
     *   - 2017 : indicates a 2017 or 2018 version when WpiLib was still part of the Eclipse Plugin, but a "year for the major version" based version was used such as `2017.1.1`
     *   - 2019 : indicates a 2019+ version when GradleRIO started to be used and the WPI Lib became a Gradle dependency. The format is generally the same as a 2017 generation, but we 
     *     identify it as a new generation. Note that the version `2018.06.21` is a 2019 generation as it was the first release by WPI upon taking over the GradleRIO project. A few
     *     days later `2019.0.0-alpha-1` was released.
     */
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
