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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
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
                { assertEquals(expected, GRV.versions.filterVersions(IsBetaFilter.and(YearFilter(2019, false))), "failed for ThisFilter.and(ThatFilter)") },
                { assertEquals(expected, GRV.versions.filterVersions(WpiLibVersionFilter.and(IsBetaFilter, YearFilter(2019, false))), "failed for WpiLibVersionFilter.and(filter1, filter2)") }
                 )
    }
    
    @Test
    fun filterAndNotTest()
    {
        val expected = GRV.betas.filter { !it.versionString.startsWith("2019") }
        assertAll(
                { assertEquals(expected, GRV.versions.filterVersions(IsBetaFilter.andNot(YearFilter(2019, false))), "failed for ThisFilter.and(ThatFilter)") },
                { assertEquals(expected, GRV.versions.filterVersions(WpiLibVersionFilter.and(IsBetaFilter, WpiLibVersionFilter.not(YearFilter(2019, false)))), "failed for WpiLibVersionFilter.and(filter1, not(filter2))") }
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
        assertAll(
                { assertEquals(listOf(GRV.v2020_1_1, GRV.v2020_1_2), GRV.versions.filterVersions(YearFilter(2020, true))) },
                { assertEquals(GRV.versions.filter { it.versionString.startsWith("2020") }, GRV.versions.filterVersions(YearFilter(2020, false))) },
                { assertEquals(GRV.versions.filter { it.versionString.startsWith("2019") }, GRV.versions.filterVersions(YearFilter(2019, false))) }
                 )
        
    }

    @Test
    fun filterOutVersions() = assertEquals(GRV.betas, GRV.betasIncludingPreviews.filterOutVersions(IsPreReleasePreviewFilter))
    
    @Test
    fun filterOutTransitionalVersions() = assertEquals(listOf(GRV.v2018_06_21), GRV.versions.filterVersions(Is2018TransitionalRelease))

    @Test
    fun filterOutAllButLatestForYear()
    {
        val inputFull = listOf(
            GRV.v2018_06_21,
            GRV.v2019_0_0_alpha_1,
            GRV.v2019_0_0_alpha_2_pre1,
            GRV.v2019_0_0_alpha_2,
            GRV.v2019_0_0_alpha_3,
            GRV.v2019_0_0_beta0_pre1,
            GRV.v2019_0_0_beta0_pre3,
            GRV.v2019_0_0_beta0_pre4,
            GRV.v2019_0_0_beta0_pre5,
            GRV.v2019_0_0_beta0_pre6,
            GRV.v2019_0_1,
            GRV.v2019_1_1_beta_1,
            GRV.v2019_1_1_beta_2a,
            GRV.v2019_1_1_beta_3_pre1,
            GRV.v2019_1_1_beta_3_p_2,
            GRV.v2019_1_1_beta_3_pre3,
            GRV.v2019_1_1_beta_3_pre4,
            GRV.v2019_1_1_beta_3_pre5,
            GRV.v2019_1_1_beta_3_pre6,
            GRV.v2019_1_1_beta_3_pre7,
            GRV.v2019_1_1_beta_3_pre8,
            GRV.v2019_1_1_beta_3_pre9,
            GRV.v2019_1_1_beta_3_pre10,
            GRV.v2019_1_1_beta_3,
            GRV.v2019_1_1_beta_3a,
            GRV.v2019_1_1_beta_4_pre1,
            GRV.v2019_1_1_beta_4_pre2,
            GRV.v2019_1_1_beta_4_pre4,
            GRV.v2019_1_1_beta_4,
            GRV.v2019_1_1_beta_4a,
            GRV.v2019_1_1_beta_4b,
            GRV.v2019_1_1_beta_4c,
            GRV.v2019_1_1_beta_5,
            GRV.v2019_1_1_beta_99,
            GRV.v2019_1_1_rc_1,
            GRV.v2019_1_1,
            GRV.v2019_1_2,
            GRV.v2019_2_1,
            GRV.v2019_3_1_rc,
            GRV.v2019_3_1,
            GRV.v2019_3_2_rc,
            GRV.v2019_3_2_rc2,
            GRV.v2019_3_2,
            GRV.v2019_4_1_rc1,
            GRV.v2019_4_1_rc2,
            GRV.v2019_4_1_rc3,
            GRV.v2020_1_1_beta_1,
            GRV.v2020_1_1_beta_2,
            GRV.v2020_1_1_beta_3,
            GRV.v2020_1_1_beta_3a,
            GRV.v2020_1_1,
            GRV.v2020_1_2_rc_1_pre1,
            GRV.v2020_1_2_rc_1,
            GRV.v2020_1_2).shuffled()

        
        assertAll(
                { assertEquals(listOf(GRV.v2020_1_2), inputFull.filterOutAllButLatestForYear(2020), "Failed for 2020") },
                { assertEquals(listOf(GRV.v2019_3_2), inputFull.filterOutAllButLatestForYear(2019, true), "Failed for 2019 & 'true'") },
                { assertEquals(listOf(GRV.v2019_4_1_rc3), inputFull.filterOutAllButLatestForYear(2019, false), "Failed for 2019 & 'false'") },
                { assertEquals(listOf(GRV.v2018_06_21), inputFull.filterOutAllButLatestForYear(2018), "Failed for 2018") }
                 )
    }
    
    @Test
    fun filterToDefaultListingNoOverride()
    {
        System.setProperty(SHOW_BETAS_SYS_PROP_KEY, "false")
        val expectedWithFinal = listOf(
                GRV.v2019_0_1,
                GRV.v2019_1_1,
                GRV.v2019_1_2,
                GRV.v2019_2_1,
                GRV.v2019_3_1,
                GRV.v2019_3_2,
                GRV.v2019_4_1,
                GRV.v2020_1_1,
                GRV.v2020_1_2).sortedDescending()
        
        val inputForUnreleasedRC = listOf(
            GRV.v2018_06_21,
            GRV.v2019_0_0_alpha_1,
            GRV.v2019_0_0_alpha_2_pre1,
            GRV.v2019_0_0_alpha_2,
            GRV.v2019_0_0_alpha_3,
            GRV.v2019_0_0_beta0_pre1,
            GRV.v2019_0_0_beta0_pre3,
            GRV.v2019_0_0_beta0_pre4,
            GRV.v2019_0_0_beta0_pre5,
            GRV.v2019_0_0_beta0_pre6,
            GRV.v2019_0_1,
            GRV.v2019_1_1_beta_1,
            GRV.v2019_1_1_beta_2a,
            GRV.v2019_1_1_beta_3_pre1,
            GRV.v2019_1_1_beta_3_p_2,
            GRV.v2019_1_1_beta_3_pre3,
            GRV.v2019_1_1_beta_3_pre4,
            GRV.v2019_1_1_beta_3_pre5,
            GRV.v2019_1_1_beta_3_pre6,
            GRV.v2019_1_1_beta_3_pre7,
            GRV.v2019_1_1_beta_3_pre8,
            GRV.v2019_1_1_beta_3_pre9,
            GRV.v2019_1_1_beta_3_pre10,
            GRV.v2019_1_1_beta_3,
            GRV.v2019_1_1_beta_3a,
            GRV.v2019_1_1_beta_4_pre1,
            GRV.v2019_1_1_beta_4_pre2,
            GRV.v2019_1_1_beta_4_pre4,
            GRV.v2019_1_1_beta_4,
            GRV.v2019_1_1_beta_4a,
            GRV.v2019_1_1_beta_4b,
            GRV.v2019_1_1_beta_4c,
            GRV.v2019_1_1_beta_5,
            GRV.v2019_1_1_beta_99,
            GRV.v2019_1_1_rc_1,
            GRV.v2019_1_1,
            GRV.v2019_1_2,
            GRV.v2019_2_1,
            GRV.v2019_3_1_rc,
            GRV.v2019_3_1,
            GRV.v2019_3_2_rc,
            GRV.v2019_3_2_rc2,
            GRV.v2019_3_2,
            GRV.v2019_4_1_rc1,
            GRV.v2019_4_1_rc2,
            GRV.v2019_4_1_rc3,
            GRV.v2019_4_1,
            GRV.v2020_1_1_beta_1,
            GRV.v2020_1_1_beta_2,
            GRV.v2020_1_1_beta_3,
            GRV.v2020_1_1_beta_3a,
            GRV.v2020_1_1,
            GRV.v2020_1_2_rc_1_pre1,
            GRV.v2020_1_2_rc_1
                                         )
        
        val expectedForUnreleasedRC = listOf(
            GRV.v2019_0_1,
            GRV.v2019_1_1,
            GRV.v2019_1_2,
            GRV.v2019_2_1,
            GRV.v2019_3_1,
            GRV.v2019_3_2,
            GRV.v2019_4_1,
            GRV.v2020_1_1,
            GRV.v2020_1_2_rc_1
                                            ).sortedDescending()

        val inputForUnreleasedBeta = listOf(
                GRV.v2018_06_21,
                GRV.v2019_0_0_alpha_1,
                GRV.v2019_0_0_alpha_2_pre1,
                GRV.v2019_0_0_alpha_2,
                GRV.v2019_0_0_alpha_3,
                GRV.v2019_0_0_beta0_pre1,
                GRV.v2019_0_0_beta0_pre3,
                GRV.v2019_0_0_beta0_pre4,
                GRV.v2019_0_0_beta0_pre5,
                GRV.v2019_0_0_beta0_pre6,
                GRV.v2019_0_1,
                GRV.v2019_1_1_beta_1,
                GRV.v2019_1_1_beta_2a,
                GRV.v2019_1_1_beta_3_pre1,
                GRV.v2019_1_1_beta_3_p_2,
                GRV.v2019_1_1_beta_3_pre3,
                GRV.v2019_1_1_beta_3_pre4,
                GRV.v2019_1_1_beta_3_pre5,
                GRV.v2019_1_1_beta_3_pre6,
                GRV.v2019_1_1_beta_3_pre7,
                GRV.v2019_1_1_beta_3_pre8,
                GRV.v2019_1_1_beta_3_pre9,
                GRV.v2019_1_1_beta_3_pre10,
                GRV.v2019_1_1_beta_3,
                GRV.v2019_1_1_beta_3a,
                GRV.v2019_1_1_beta_4_pre1,
                GRV.v2019_1_1_beta_4_pre2,
                GRV.v2019_1_1_beta_4_pre4,
                GRV.v2019_1_1_beta_4,
                GRV.v2019_1_1_beta_4a,
                GRV.v2019_1_1_beta_4b,
                GRV.v2019_1_1_beta_4c,
                GRV.v2019_1_1_beta_5,
                GRV.v2019_1_1_beta_99,
                GRV.v2019_1_1_rc_1,
                GRV.v2019_1_1,
                GRV.v2019_1_2,
                GRV.v2019_2_1,
                GRV.v2019_3_1_rc,
                GRV.v2019_3_1,
                GRV.v2019_3_2_rc,
                GRV.v2019_3_2_rc2,
                GRV.v2019_3_2,
                GRV.v2019_4_1_rc1,
                GRV.v2019_4_1_rc2,
                GRV.v2019_4_1_rc3,
                GRV.v2019_4_1,
                GRV.v2020_1_1_beta_1,
                GRV.v2020_1_1_beta_2,
                GRV.v2020_1_1_beta_3,
                GRV.v2020_1_1_beta_3a)

        val expectedForUnreleasedBeta = listOf(
                GRV.v2019_0_1,
                GRV.v2019_1_1,
                GRV.v2019_1_2,
                GRV.v2019_2_1,
                GRV.v2019_3_1,
                GRV.v2019_3_2,
                GRV.v2019_4_1).sortedDescending()
        
        assertAll(
                { assertEquals(expectedWithFinal, GRV.versions.filterToDefaultListing(), "Failure for full list with a final released version")},
                { assertEquals(expectedForUnreleasedRC, inputForUnreleasedRC.filterToDefaultListing(), "Failure for list with a unreleased Release Candidate")},
                { assertEquals(expectedForUnreleasedBeta, inputForUnreleasedBeta.filterToDefaultListing(), "Failure for list with a unreleased Beta")}
                 )
    }
    
    @Disabled("Need to fix. The test only expects the latest beta. But we modified the filter for now")
    @Test
    fun filterToDefaultListingWithOverride()
    {
        System.setProperty(SHOW_BETAS_SYS_PROP_KEY, "true")
        val expectedWithFinal = listOf(
                GRV.v2019_0_1,
                GRV.v2019_1_1,
                GRV.v2019_1_2,
                GRV.v2019_2_1,
                GRV.v2019_3_1,
                GRV.v2019_3_2,
                GRV.v2019_4_1,
                GRV.v2020_1_1,
                GRV.v2020_1_2).sortedDescending()
        
        val inputForUnreleasedRC = listOf(
            GRV.v2018_06_21,
            GRV.v2019_0_0_alpha_1,
            GRV.v2019_0_0_alpha_2_pre1,
            GRV.v2019_0_0_alpha_2,
            GRV.v2019_0_0_alpha_3,
            GRV.v2019_0_0_beta0_pre1,
            GRV.v2019_0_0_beta0_pre3,
            GRV.v2019_0_0_beta0_pre4,
            GRV.v2019_0_0_beta0_pre5,
            GRV.v2019_0_0_beta0_pre6,
            GRV.v2019_0_1,
            GRV.v2019_1_1_beta_1,
            GRV.v2019_1_1_beta_2a,
            GRV.v2019_1_1_beta_3_pre1,
            GRV.v2019_1_1_beta_3_p_2,
            GRV.v2019_1_1_beta_3_pre3,
            GRV.v2019_1_1_beta_3_pre4,
            GRV.v2019_1_1_beta_3_pre5,
            GRV.v2019_1_1_beta_3_pre6,
            GRV.v2019_1_1_beta_3_pre7,
            GRV.v2019_1_1_beta_3_pre8,
            GRV.v2019_1_1_beta_3_pre9,
            GRV.v2019_1_1_beta_3_pre10,
            GRV.v2019_1_1_beta_3,
            GRV.v2019_1_1_beta_3a,
            GRV.v2019_1_1_beta_4_pre1,
            GRV.v2019_1_1_beta_4_pre2,
            GRV.v2019_1_1_beta_4_pre4,
            GRV.v2019_1_1_beta_4,
            GRV.v2019_1_1_beta_4a,
            GRV.v2019_1_1_beta_4b,
            GRV.v2019_1_1_beta_4c,
            GRV.v2019_1_1_beta_5,
            GRV.v2019_1_1_beta_99,
            GRV.v2019_1_1_rc_1,
            GRV.v2019_1_1,
            GRV.v2019_1_2,
            GRV.v2019_2_1,
            GRV.v2019_3_1_rc,
            GRV.v2019_3_1,
            GRV.v2019_3_2_rc,
            GRV.v2019_3_2_rc2,
            GRV.v2019_3_2,
            GRV.v2019_4_1_rc1,
            GRV.v2019_4_1_rc2,
            GRV.v2019_4_1_rc3,
            GRV.v2019_4_1,
            GRV.v2020_1_1_beta_1,
            GRV.v2020_1_1_beta_2,
            GRV.v2020_1_1_beta_3,
            GRV.v2020_1_1_beta_3a,
            GRV.v2020_1_1,
            GRV.v2020_1_2_rc_1_pre1,
            GRV.v2020_1_2_rc_1
                                         )
        
        val expectedForUnreleasedRC = listOf(
            GRV.v2019_0_1,
            GRV.v2019_1_1,
            GRV.v2019_1_2,
            GRV.v2019_2_1,
            GRV.v2019_3_1,
            GRV.v2019_3_2,
            GRV.v2019_4_1,
            GRV.v2020_1_1,
            GRV.v2020_1_2_rc_1
                                            ).sortedDescending()

        val inputForUnreleasedBeta = listOf(
                GRV.v2018_06_21,
                GRV.v2019_0_0_alpha_1,
                GRV.v2019_0_0_alpha_2_pre1,
                GRV.v2019_0_0_alpha_2,
                GRV.v2019_0_0_alpha_3,
                GRV.v2019_0_0_beta0_pre1,
                GRV.v2019_0_0_beta0_pre3,
                GRV.v2019_0_0_beta0_pre4,
                GRV.v2019_0_0_beta0_pre5,
                GRV.v2019_0_0_beta0_pre6,
                GRV.v2019_0_1,
                GRV.v2019_1_1_beta_1,
                GRV.v2019_1_1_beta_2a,
                GRV.v2019_1_1_beta_3_pre1,
                GRV.v2019_1_1_beta_3_p_2,
                GRV.v2019_1_1_beta_3_pre3,
                GRV.v2019_1_1_beta_3_pre4,
                GRV.v2019_1_1_beta_3_pre5,
                GRV.v2019_1_1_beta_3_pre6,
                GRV.v2019_1_1_beta_3_pre7,
                GRV.v2019_1_1_beta_3_pre8,
                GRV.v2019_1_1_beta_3_pre9,
                GRV.v2019_1_1_beta_3_pre10,
                GRV.v2019_1_1_beta_3,
                GRV.v2019_1_1_beta_3a,
                GRV.v2019_1_1_beta_4_pre1,
                GRV.v2019_1_1_beta_4_pre2,
                GRV.v2019_1_1_beta_4_pre4,
                GRV.v2019_1_1_beta_4,
                GRV.v2019_1_1_beta_4a,
                GRV.v2019_1_1_beta_4b,
                GRV.v2019_1_1_beta_4c,
                GRV.v2019_1_1_beta_5,
                GRV.v2019_1_1_beta_99,
                GRV.v2019_1_1_rc_1,
                GRV.v2019_1_1,
                GRV.v2019_1_2,
                GRV.v2019_2_1,
                GRV.v2019_3_1_rc,
                GRV.v2019_3_1,
                GRV.v2019_3_2_rc,
                GRV.v2019_3_2_rc2,
                GRV.v2019_3_2,
                GRV.v2019_4_1_rc1,
                GRV.v2019_4_1_rc2,
                GRV.v2019_4_1_rc3,
                GRV.v2019_4_1,
                GRV.v2020_1_1_beta_1,
                GRV.v2020_1_1_beta_2,
                GRV.v2020_1_1_beta_3,
                GRV.v2020_1_1_beta_3a)

        val expectedForUnreleasedBeta = listOf(
                GRV.v2019_0_1,
                GRV.v2019_1_1,
                GRV.v2019_1_2,
                GRV.v2019_2_1,
                GRV.v2019_3_1,
                GRV.v2019_3_2,
                GRV.v2019_4_1,
                GRV.v2020_1_1_beta_3a).sortedDescending()
        
        assertAll(
                { assertEquals(expectedWithFinal, GRV.versions.filterToDefaultListing(), "Failure for full list with a final released version")},
                { assertEquals(expectedForUnreleasedRC, inputForUnreleasedRC.filterToDefaultListing(), "Failure for list with a unreleased Release Candidate")},
                { assertEquals(expectedForUnreleasedBeta, inputForUnreleasedBeta.filterToDefaultListing(), "Failure for list with a unreleased Beta")}
                 )
    }
}