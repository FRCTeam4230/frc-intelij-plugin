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

import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle
import java.util.regex.Pattern


class WpiLibVersionImpl private constructor(override val versionString: String,
                                            override val generation: Int,
                                            override val major: Int,
                                            override val minor: Int,
                                            override val patch: Int,
                                            override val preReleaseModifier: WpiLibVersion.PreReleaseModifier?,
                                            override val preReleaseModifierVersion: Int?) : WpiLibVersion
{

    override fun cloneIt(): WpiLibVersion
    {
        return WpiLibVersionImpl(this.versionString, this.generation, this.major, this.minor, this.patch, this.preReleaseModifier, this.preReleaseModifierVersion)
    }


    override fun toString(): String
    {
        return versionString
    }


    @Suppress("unused")
    @JvmOverloads
    fun toStringDetailed(style: ToStringStyle = ToStringStyle.SHORT_PREFIX_STYLE): String
    {
        return ToStringBuilder(this, style)
                .append("version", versionString)
                .append("generation", generation)
                .append("major", major)
                .append("minor", minor)
                .append("patch", patch)
                .append("preReleaseModifier", preReleaseModifier)
                .append("preReleaseModifierVersion", preReleaseModifierVersion)
                .toString()
    }

    
    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true

        if (other == null || javaClass != other.javaClass) return false

        val that = other as WpiLibVersionImpl?

        return EqualsBuilder()
                .append(generation, that!!.generation)
                .append(major, that.major)
                .append(minor, that.minor)
                .append(patch, that.patch)
                .append(versionString, that.versionString)
                .append(preReleaseModifier, that.preReleaseModifier)
                .append(preReleaseModifierVersion, that.preReleaseModifierVersion)
                .isEquals
    }


    override fun hashCode(): Int
    {
        return HashCodeBuilder(17, 37)
                .append(versionString)
                .append(generation)
                .append(major)
                .append(minor)
                .append(patch)
                .append(preReleaseModifier)
                .append(preReleaseModifierVersion)
                .toHashCode()
    }

    companion object
    {

        private val pre2017Pattern = Pattern.compile("([\\d]{1,2})\\.([\\d]{1,2})\\.([\\d]{1,2})\\.([\\d]{12})")
        private val post2017Pattern = Pattern.compile(
                "(?<major>[\\d]{4})\\.(?<minor>[\\d]{1,2})(\\.(?<patch>[\\d]{1,2})(?<preAll>[-.](?<preName>alpha|beta|rc)([-.](?<preVer>[\\d]{1,2}))?)?)?")

        // E X A M P L E S
        // == PRE 2017 ==
        // 0.1.0.201502241928
        // 0.1.0.201602112135
        // 0.1.0.201603020231
        // == 2017 on ==
        // 2017.1.1.alpha-1   // there actually are no alpha releases, but we handle just in case
        // 2017.1.1.alpha-2
        // 2017.1.1.beta-1
        // 2017.1.1.beta-2
        // 2017.1.1.beta-3
        // 2017.1.1.beta-4
        // 2017.1.1.rc-1
        // 2017.1.1.rc-2
        // 2017.1.1
        // 2017.2.1
        // 2018.1.1.beta-5


        @Throws(IllegalArgumentException::class)
        fun parse(version: String): WpiLibVersion
        {
            var matcher = post2017Pattern.matcher(version.toLowerCase())
            if (matcher.find())
            {
                return WpiLibVersionImpl(version,
                                         2017,
                                         Integer.parseInt(matcher.group("major")),
                                         if (matcher.group("minor") != null) Integer.parseInt(matcher.group("minor")) else 0,
                                         if (matcher.group("patch") != null) Integer.parseInt(matcher.group("patch")) else 0,
                                         if (matcher.group("preAll") != null) WpiLibVersion.PreReleaseModifier.valueOf(matcher.group("preName")) else null,
                                         if (matcher.group("preVer") != null) Integer.parseInt(matcher.group("preVer")) else null)
            }
            else
            {
                matcher = pre2017Pattern.matcher(version)
                if (matcher.find())
                {
                    val dateTimeString = matcher.group(4)
                    return WpiLibVersionImpl(version,
                                             2015,
                                             Integer.parseInt(dateTimeString.substring(0, 4)),
                                             Integer.parseInt(dateTimeString.substring(4, 6)),
                                             Integer.parseInt(dateTimeString.substring(6, 12)), null, null)
                }
                else
                {
                    throw IllegalArgumentException("Cannot parse the version string '$version'")

                }

            }
        }
    }
}