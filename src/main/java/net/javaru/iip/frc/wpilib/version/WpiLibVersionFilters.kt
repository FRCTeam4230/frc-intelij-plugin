/*
 * Copyright 2015-2020 the original author or authors
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

import org.apache.commons.lang3.BooleanUtils
import java.util.stream.Stream

/** The 2018 transitional release (when WPI took over the development of GradleRIO). */
val ver2018_06_21 = WpiLibVersionImpl.parse("2018.06.21")

const val SHOW_BETAS_SYS_PROP_KEY = "frc.show.betas.in.new.project.wizard"

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

/**
 * For the given year, filters out ALL but the latest version for that year, in essence returning
 * a list of a sinfge element, or an empty list if no versions for the year are present in the initial 
 * list (or no non-pre-release versions)
 * If [excludePreReleases] is set to true, pre-releases (betas, release candidates, etc) are
 * excluded so that the latest "full" release is returned.
 */
@JvmOverloads
fun Iterable<WpiLibVersion>.filterOutAllButLatestForYear(year: Int, excludePreReleases: Boolean = true): List<WpiLibVersion>
{
    val latest = this.filterToLatestForYear(year, excludePreReleases)
    return if (latest != null) listOf(latest) else listOf()
}

@JvmOverloads
fun Iterable<WpiLibVersion>.filterToLatestForYear(year: Int, excludePreReleases: Boolean = true): WpiLibVersion? =  this.filterVersions(YearFilter(year, excludePreReleases)).max()


/**
 * Filter the Iterable to include: only releases.  and the latest version if it is a release candidate. It also filters
 * out the transitional `2018.06.21` version. Finally, it sorts the list in descending order' so the latest version is
 * first.
 *
 * For now we will not show betas because there may be peculiarities with betas. For example, during the beta program of 2020,
 * the `projectYear` in the wpilib_preferences.json file was "Beta2020-2" and not 2020. But I could find no mapping of
 * the WpiLib Version to this value. It was basically a hard coded value in the code checking for it.
 *
 */
fun Iterable<WpiLibVersion>.filterToDefaultListing(): List<WpiLibVersion>
{
    val versionList = this.toList().sortedDescending()
    val filter = if (BooleanUtils.toBoolean(System.getProperty(SHOW_BETAS_SYS_PROP_KEY, "false"))) IsReleaseOrRcOrBetaFilter else IsReleaseFilter
    val filteredList = versionList.filterVersions(filter).filterOutVersions(Is2018TransitionalRelease)


    return if (versionList.isNotEmpty() && !filteredList.contains(versionList[0]) && versionList[0].isReleaseCandidate())
    {
        // the latest version is not included, and is a release candidate and we want to show it.
        val mutableList = filteredList.toMutableList()
        mutableList.add(versionList[0])
        mutableList.sortDescending()
        mutableList
    }
    else
    {
        filteredList
    }
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


object Is2018TransitionalRelease : WpiLibVersionFilter
{
    override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion -> wpiLibVersion == ver2018_06_21 }
}

object IsReleaseFilter : WpiLibVersionFilter
{
    override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion -> wpiLibVersion.isRelease() }
}


object IsReleaseCandidateFilter : WpiLibVersionFilter
{
    override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion -> wpiLibVersion.isReleaseCandidate() }
}

object IsBetaFilter : WpiLibVersionFilter
{
    override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion -> wpiLibVersion.isBeta() }
}

object IsPreReleasePreviewFilter : WpiLibVersionFilter
{
    override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion -> wpiLibVersion.isPreReleasePreview() }
}

object IsReleaseOrRcOrBetaFilter : WpiLibVersionFilter
{
    override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion -> wpiLibVersion.isRelease() || wpiLibVersion.isReleaseCandidate() || wpiLibVersion.isBeta() }
}

object IsNotPreReleasePreviewFilter : WpiLibVersionFilter
{
    override fun predicate(): (WpiLibVersion) -> Boolean = { wpiLibVersion -> !wpiLibVersion.isPreReleasePreview() }
}

/**
 * Filters a list so that it contains just the values for a particular year. By default, it will filter out pre-releases. Note that NO
 * sorting takes place. the items are returned in the same order as they were in the original list.
 */
open class YearFilter @JvmOverloads constructor(private val year: Int, private val excludePreReleases: Boolean = true) : WpiLibVersionFilter
{
    override fun predicate(): (WpiLibVersion) -> Boolean =
            if (excludePreReleases)
                { wpiLibVersion -> wpiLibVersion.frcYear == year && wpiLibVersion.isRelease() }
            else 
                { wpiLibVersion -> wpiLibVersion.frcYear == year }
}


