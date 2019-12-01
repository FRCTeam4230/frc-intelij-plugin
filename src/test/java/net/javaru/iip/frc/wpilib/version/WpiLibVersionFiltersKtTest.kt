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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import net.javaru.iip.frc.wpilib.version.GradleRioVersionsForTesting as GRV


internal class WpiLibVersionFiltersKtTest
{

    @Test
    fun filterVersions()
    {
        assertAll(
                { assertEquals(GRV.releases, GRV.versions.filterVersions(IsReleaseFilter), "Wrong values for IsReleaseFilter") },
                { assertEquals(GRV.releaseCandidates, GRV.versions.filterVersions(IsReleaseCandidateFilter), "Wrong values for IsReleaseCandidateFilter") }
                 )
    }

    @Test
    fun filterOrTest()
    {
        assertAll(
                { assertEquals(GRV.releaseAndReleaseCandidates, GRV.versions.filterVersions(IsReleaseFilter.or(IsReleaseCandidateFilter)), "failed for ThisFilter.or(ThatFilter)") },
                { assertEquals(GRV.releaseAndReleaseCandidates, GRV.versions.filterVersions(WpiLibVersionFilter.or(IsReleaseFilter, IsReleaseCandidateFilter)), "failed for WpiLibVersionFilter.or(filter1, filter2)") }
                 )
    }

    @Test
    fun filterAndTest()
    {
        val expected = GRV.betas.filter { it.versionString.startsWith("2019") }
        assertAll(
                { assertEquals(expected, GRV.versions.filterVersions(IsBetaFilter.and(YearFilter(2019))), "failed for ThisFilter.and(ThatFilter)") },
                { assertEquals(expected, GRV.versions.filterVersions(WpiLibVersionFilter.and(IsBetaFilter, YearFilter(2019))), "failed for WpiLibVersionFilter.and(filter1, filter2)") }
                 )
    }
    @Test
    fun filterAndNotTest()
    {
        val expected = GRV.betas.filter { !it.versionString.startsWith("2019") }
        assertAll(
                { assertEquals(expected, GRV.versions.filterVersions(IsBetaFilter.andNot(YearFilter(2019))), "failed for ThisFilter.and(ThatFilter)") },
                { assertEquals(expected, GRV.versions.filterVersions(WpiLibVersionFilter.and(IsBetaFilter, WpiLibVersionFilter.not(YearFilter(2019)))), "failed for WpiLibVersionFilter.and(filter1, not(filter2))") }
                 )
    }
    
    @Test
    fun individualFilterTests()
    {
        assertAll(
                { assertEquals(GRV.betas, GRV.betasIncludingPreviews.filterVersions(IsNotPreReleasePreviewFilter), "Test 1 failed")},
                { assertEquals(GRV.betas, GRV.betasIncludingPreviews.filterVersions(IsPreReleasePreviewFilter.not()), "Test 2 failed")},
                { assertEquals(emptyList<WpiLibVersion>(), GRV.betas.filterOutVersions(IsPreReleasePreviewFilter.not()), "Test 3 failed")}
                 )
    }

    @Test
    fun yearFilterTest()
    {
        val expected = GRV.versions.filter { it.versionString.startsWith("2020") }
        assertEquals(expected, GRV.versions.filterVersions(YearFilter(2020)))
    }

    @Test
    fun filterOutVersions()
    {
        assertEquals(GRV.betas, GRV.betasIncludingPreviews.filterOutVersions(IsPreReleasePreviewFilter))
    }
}