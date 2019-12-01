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

import net.javaru.iip.frc.util.getAdjustedBuildYear
import net.javaru.iip.frc.util.getCurrentBuildYear
import java.util.stream.Stream

/**
 * Returns a List containing only elements matching the given [WpiLibVersionFilter].
 */
fun Iterable<WpiLibVersion>.filterVersions(filter: WpiLibVersionFilter): List<WpiLibVersion>
{
    return this.filter { filter.predicate().invoke(it) }
}

/**
 * Returns a Stream containing only elements matching the given [WpiLibVersionFilter].
 */
fun Stream<WpiLibVersion>.filterVersions(filter: WpiLibVersionFilter): Stream<WpiLibVersion>
{
    return this.filter { filter.predicate().invoke(it) }
}

/**
 *  Returns a List with elements matching the given [WpiLibVersionFilter] removed.
 */
fun Iterable<WpiLibVersion>.filterOutVersions(filter: WpiLibVersionFilter): List<WpiLibVersion>
{
    return this.filter { filter.not().predicate().invoke(it) }
}

/**
 *  Returns a Stream with elements matching the given [WpiLibVersionFilter] removed..
 */
fun Stream<WpiLibVersion>.filterOutVersions(filter: WpiLibVersionFilter): Stream<WpiLibVersion>
{
    return this.filter { filter.not().predicate().invoke(it) }
}


interface WpiLibVersionFilter
{
    fun predicate(): (WpiLibVersion) -> Boolean

    /** Returns an OR filter that "ors" this filter with the supplied filter. */
    fun or(filter: WpiLibVersionFilter): WpiLibVersionFilter = Filters.or(this, filter)

    /** Returns an AND filter that "ands" this filter with the supplied filter. */
    fun and(filter: WpiLibVersionFilter): WpiLibVersionFilter = Filters.and(this, filter)

    /** Returns an AND filter that "ands" this filter with the inverse of the supplied filter. For example
     * `IsPreReleaseFilter.andNot(IsAlphaFilter)` would return a filter that returns all prerelease versions
     * that are not alpha release. Thus it would return only release candidates and betas.*/
    fun andNot(filter: WpiLibVersionFilter): WpiLibVersionFilter = Filters.and(this, filter.not())

    /** Returns the inverse of this boolean. For example, for the IsBetaFilter, it effectually returns an IsNotBetaFilter. */
    fun not(): WpiLibVersionFilter = Filters.not(this)


    companion object Filters
    {
        fun not(filter: WpiLibVersionFilter) = object : WpiLibVersionFilter
        {
            override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion: WpiLibVersion ->
                !filter.predicate().invoke(wpiLibVersion)
            }
        }

        fun or(vararg filters: WpiLibVersionFilter) = object : WpiLibVersionFilter
        {
            override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion: WpiLibVersion ->
                var result = false
                for (filter in filters)
                {
                    if (filter.predicate().invoke(wpiLibVersion))
                    {
                        result = true
                        break;
                    }
                }
                result
            }
        }

        fun and(vararg filters: WpiLibVersionFilter) = object : WpiLibVersionFilter
        {
            override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion: WpiLibVersion ->
                var result = true
                for (filter in filters)
                {
                    if (!filter.predicate().invoke(wpiLibVersion))
                    {
                        result = false
                        break;
                    }
                }
                result
            }
        }
    }
}

object IsReleaseFilter: WpiLibVersionFilter
{
    override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion -> wpiLibVersion.isRelease() }
}


object IsReleaseCandidateFilter: WpiLibVersionFilter
{
    override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion -> wpiLibVersion.isReleaseCandidate() }
}

object IsBetaFilter: WpiLibVersionFilter
{
    override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion -> wpiLibVersion.isBeta() }
}

object IsPreReleasePreviewFilter: WpiLibVersionFilter
{
    override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion -> wpiLibVersion.isPreReleasePreview() }
}

object IsNotPreReleasePreviewFilter: WpiLibVersionFilter
{
    override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion -> !wpiLibVersion.isPreReleasePreview() }
}

open class YearFilter(private val year:Int): WpiLibVersionFilter
{
    override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion -> wpiLibVersion.major == year }
}

object CurrentBuildYearFilter: YearFilter(getCurrentBuildYear())

object NearBuildYearFilter: YearFilter(getAdjustedBuildYear())
