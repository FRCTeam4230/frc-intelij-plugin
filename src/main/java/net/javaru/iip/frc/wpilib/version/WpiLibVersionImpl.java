/*
 * Copyright 2015-2017 Mark Vedder
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.javaru.iip.frc.wpilib.version;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;



public class WpiLibVersionImpl implements WpiLibVersion
{

    private static final Pattern pre2017Pattern = Pattern.compile("([\\d]{1,2})\\.([\\d]{1,2})\\.([\\d]{1,2})\\.([\\d]{12})");
    private static final Pattern post2017Pattern = Pattern.compile(
        "(?<major>[\\d]{4})\\.(?<minor>[\\d]{1,2})(\\.(?<patch>[\\d]{1,2})(?<preAll>[-.](?<preName>alpha|beta|rc)([-.](?<preVer>[\\d]{1,2}))?)?)?");
    private final String version;
    private final int generation;
    private final int major;
    private final int minor;
    private final int patch;
    @Nullable
    private final PreReleaseModifier preReleaseModifier;

    @Nullable
    private final Integer preReleaseModifierVersion;

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


    public static WpiLibVersion parse(@NotNull String version) throws IllegalArgumentException
    {
        Matcher matcher = post2017Pattern.matcher(version.toLowerCase());
        if (matcher.find())
        {
            return new WpiLibVersionImpl(version,
                                         2017,
                                         Integer.parseInt(matcher.group("major")),
                                         matcher.group("minor") != null ? Integer.parseInt(matcher.group("minor")) : 0,
                                         matcher.group("patch") != null ? Integer.parseInt(matcher.group("patch")) : 0,
                                         matcher.group("preAll") != null ? PreReleaseModifier.valueOf(matcher.group("preName")) : null,
                                         matcher.group("preVer") != null ? Integer.parseInt(matcher.group("preVer")) : null);
        }
        else
        {
            matcher = pre2017Pattern.matcher(version);
            if (matcher.find())
            {
                final String dateTimeString = matcher.group(4);
                return new WpiLibVersionImpl(version,
                                             2015,
                                             Integer.parseInt(dateTimeString.substring(0, 4)),
                                             Integer.parseInt(dateTimeString.substring(4, 6)),
                                             Integer.parseInt(dateTimeString.substring(6, 12)),
                                             null,
                                             null);
            }
            else
            {
                throw new IllegalArgumentException("Cannot parse the version string '" + version + "'");

            }

        }
    }


    private WpiLibVersionImpl(String version,
                              int generation,
                              int major,
                              int minor,
                              int patch,
                              @Nullable PreReleaseModifier preReleaseModifier,
                              @Nullable Integer preReleaseModifierVersion)
    {
        this.generation = generation;
        this.version = version;
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.preReleaseModifier = preReleaseModifier;
        this.preReleaseModifierVersion = preReleaseModifierVersion;
    }


    @Override
    public int getGeneration() { return generation; }


    @Override
    public String getVersionString() { return version;}


    @Override
    public int getMajor() { return major;}


    @Override
    public int getMinor() { return minor; }


    @Override
    public int getPatch() { return patch; }


    @Nullable
    @Override
    public PreReleaseModifier getPreReleaseModifier() { return preReleaseModifier; }


    @Nullable
    @Override
    public Integer getPreReleaseModifierVersion() { return preReleaseModifierVersion; }


    @Override
    public String toString()
    {
        return getVersionString();
    }
    
    public String toStringDetailed()
    {
        return toStringDetailed(ToStringStyle.SHORT_PREFIX_STYLE);
    }
    
    public String toStringDetailed(final ToStringStyle style)
    {
        return new ToStringBuilder(this, style)
            .append("version", version)
            .append("generation", generation)
            .append("major", major)
            .append("minor", minor)
            .append("patch", patch)
            .append("preReleaseModifier", preReleaseModifier)
            .append("preReleaseModifierVersion", preReleaseModifierVersion)
            .toString();
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        WpiLibVersionImpl that = (WpiLibVersionImpl) o;

        return new EqualsBuilder()
            .append(getGeneration(), that.getGeneration())
            .append(getMajor(), that.getMajor())
            .append(getMinor(), that.getMinor())
            .append(getPatch(), that.getPatch())
            .append(getVersionString(), that.getVersionString())
            .append(getPreReleaseModifier(), that.getPreReleaseModifier())
            .append(getPreReleaseModifierVersion(), that.getPreReleaseModifierVersion())
            .isEquals();
    }


    @Override
    public int hashCode()
    {
        return new HashCodeBuilder(17, 37)
            .append(getVersionString())
            .append(getGeneration())
            .append(getMajor())
            .append(getMinor())
            .append(getPatch())
            .append(getPreReleaseModifier())
            .append(getPreReleaseModifierVersion())
            .toHashCode();
    }
}