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

package net.javaru.iip.frc.wpilib.version

import org.apache.commons.lang3.builder.CompareToBuilder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle

//private val LOG = logger<WpiLibVersion>()

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

    /** The project year, such as `2019` or `2020` */
    val frcYear:Int
            get() = major
    
    /** Indicates the version is a (full) release and not a pre-release (such as RC, Beta, or Alpha). */
    fun isRelease(): Boolean = preReleaseModifier == null

    /** Indicates the version is some type of pre-release such as RC, Beta, or Alpha, **excluding** preview releases such as `2019.1.1-beta-2-pre3`. */
    fun isPreRelease(): Boolean = preReleaseModifier != null && preReleasePreviewVersion == null

    /** Indicates the version is some type of pre-release such as RC, Beta, or Alpha, **including** preview releases such as `2019.1.1-beta-2-pre3`. */
    fun isPreReleaseIncludingPreviews(): Boolean = preReleaseModifier != null

    /** Indicates the version is a preview of some type of pre-release such as RC, Beta, or Alpha, such as `2019.1.1-beta-2-pre3`. A non preview pre-release, such as `2019.1.1-beta-3a` would return false as it is not a preview. */
    fun isPreReleasePreview(): Boolean = preReleaseModifier != null && preReleasePreviewVersion != null

    /** Indicates the version is either a full release, or a release candidate,  **excluding** preview releases such as `2020.1.2-rc-1-pre1`.*/
    fun isReleaseOrReleaseCandidate(): Boolean = isRelease() || isReleaseCandidate()

    /** Indicates the version is either a full release, or a release candidate,  **including** preview releases such as `2020.1.2-rc-1-pre1`.*/
    fun isReleaseOrReleaseCandidateOrRcPreview(): Boolean = isRelease() || isReleaseCandidateOrRcPreview()

    /** Indicates the version is a release candidate,  **excluding** preview releases such as `v2019_1_2_rc_1_pre1`.*/
    fun isReleaseCandidate(): Boolean = preReleaseModifier == PreReleaseModifier.rc && !isPreReleasePreview()

    /** Indicates the version is a release candidate,  **including** preview releases such as `2020.1.2-rc-1-pre1`.*/
    fun isReleaseCandidateOrRcPreview(): Boolean = preReleaseModifier == PreReleaseModifier.rc

    /** Indicates the version is a beta release,  **excluding** preview releases such as `2019.1.1-beta-2-pre3`.*/
    fun isBeta(): Boolean = preReleaseModifier == PreReleaseModifier.beta && !isPreReleasePreview()

    /** Indicates the version is a beta release,  **including** preview releases such as `2019.1.1-beta-2-pre3`.*/
    fun isBetaOrBetaPreview(): Boolean = preReleaseModifier == PreReleaseModifier.beta

    /** Indicates the version is a beta release or a release candidate,  **excluding** preview releases such as `2019.1.1-beta-2-pre3`.*/
    fun isBetaOrReleaseCandidate(): Boolean = isBeta() || isReleaseCandidate()
    
    /** Indicates the version is a release, a beta release, or a release candidate,  **excluding** preview releases such as `2019.1.1-beta-2-pre3`.*/
    fun isReleaseOrBetaOrReleaseCandidate(): Boolean = isBeta() || isReleaseCandidate() || isRelease()
    
    /** Indicates the version is a alpha release,  **excluding** preview releases such as `2019.0.0-alpha-3-pre1`.*/
    fun isAlpha(): Boolean = preReleaseModifier == PreReleaseModifier.alpha && !isPreReleasePreview()

    /** Indicates the version is a alpha release,  **excluding** preview releases such as `2019.0.0-alpha-3-pre1`.*/
    fun isAlphaOrAlphaPreview(): Boolean = preReleaseModifier == PreReleaseModifier.alpha
    
    
    override fun compareTo(other: WpiLibVersion): Int
    {
        return CompareToBuilder()
                .append(this.generation, other.generation)
                // For now, since it is the same as the major, we leave out frcYear
                .append(this.major, other.major)
                .append(this.minor, other.minor)
                .append(this.patch, other.patch)
                .append(if (this.isPreReleaseIncludingPreviews()) this.preReleaseModifier!!.ordinal else Integer.MAX_VALUE,
                        if (other.isPreReleaseIncludingPreviews()) other.preReleaseModifier!!.ordinal else Integer.MAX_VALUE)
                .append(if (this.isPreReleaseIncludingPreviews()) this.preReleaseModifierVersion else Integer.MAX_VALUE,
                        if (other.isPreReleaseIncludingPreviews()) other.preReleaseModifierVersion else Integer.MAX_VALUE)
                .append(if (this.isPreReleaseIncludingPreviews()) this.preReleaseModifierSubVersion else "",
                        if (other.isPreReleaseIncludingPreviews()) other.preReleaseModifierSubVersion else "")
                .append(if (this.isPreReleasePreview()) this.preReleasePreviewVersion else Integer.MAX_VALUE,
                        if (other.isPreReleasePreview()) other.preReleasePreviewVersion else Integer.MAX_VALUE)
                .toComparison()
    }
    
    /** 
     * For a pre-release, this returns the (anticipated) corresponding release version. 
     * For example, for `2019.1.2-beta-3` it would return `2019.1.2`. For non pre-release
     * versions, it simply returns itself.
     */
    fun getCorrespondingReleaseVersion(): WpiLibVersion


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

abstract class AbstractWpiLibVersion: WpiLibVersion
{
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
            .append("preReleaseModifierSubVersion", preReleaseModifierSubVersion)
            .append("preReleasePreviewVersion", preReleasePreviewVersion)
            .toString()
    }


    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true

        if (other == null || other !is WpiLibVersion) return false

        val that = other as WpiLibVersion?

        return EqualsBuilder()
            .append(generation, that!!.generation)
            .append(major, that.major)
            .append(minor, that.minor)
            .append(patch, that.patch)
            .append(versionString, that.versionString)
            .append(preReleaseModifier, that.preReleaseModifier)
            .append(preReleaseModifierVersion, that.preReleaseModifierVersion)
            .append(preReleaseModifierSubVersion, that.preReleaseModifierSubVersion)
            .append(preReleasePreviewVersion, that.preReleasePreviewVersion)
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
            .append(preReleaseModifierSubVersion)
            .append(preReleasePreviewVersion)
            .toHashCode()
    }
    
    protected fun createStandardVersionString(): String
    {
        val sb = StringBuilder()
        sb.append("${major}.${minor}.${patch}")
        if (preReleaseModifier != null)
        {
            sb.append("-${preReleaseModifier}")
            if (preReleaseModifierVersion != null)
            {
                sb.append("-${preReleaseModifier}${preReleaseModifierSubVersion}")
            }
            if (preReleasePreviewVersion != null)
            {
                sb.append("-pre${preReleasePreviewVersion}")
            }
        }
        return sb.toString()
    }
}
