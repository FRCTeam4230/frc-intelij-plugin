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

import javax.annotation.Nonnull;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;



public interface WpiLibVersion extends Comparable<WpiLibVersion>
{
    int getGeneration();

    String getVersionString();

    int getMajor();

    int getMinor();

    int getPatch();


    default boolean isPreRelease()
    {
        return getPreReleaseModifier() != null;
    }

    @Nullable
    PreReleaseModifier getPreReleaseModifier();

    @Nullable
    Integer getPreReleaseModifierVersion();

    @Override
    default int compareTo(@NotNull WpiLibVersion other)
    {
        //noinspection ConstantConditions
        return new CompareToBuilder()
            .append(this.getGeneration(), other.getGeneration())
            .append(this.getMajor(), other.getMajor())
            .append(this.getMinor(), other.getMinor())
            .append(this.getPatch(), other.getPatch())
            .append((this.isPreRelease() ? this.getPreReleaseModifier().ordinal() : Integer.MAX_VALUE),
                    (other.isPreRelease() ? other.getPreReleaseModifier().ordinal() : Integer.MAX_VALUE))
            .append((this.isPreRelease() ? this.getPreReleaseModifierVersion() : Integer.MAX_VALUE),
                    (other.isPreRelease() ? other.getPreReleaseModifierVersion() : Integer.MAX_VALUE))
            .toComparison();

    }


    enum PreReleaseModifier
    {
        alpha, beta, rc
    }

    default boolean isNewerThan(@Nonnull WpiLibVersion other) 
    {
        final boolean isNewer = compareTo(other) > 0;
        Logger.getInstance(WpiLibVersion.class).debug("" + getVersionString() + ".isNewerThan(" + other.getVersionString() + ") = " + isNewer);
        return isNewer; 
    }

    default boolean isOlderThan(@Nonnull WpiLibVersion other) { return compareTo(other) < 0;}
}
