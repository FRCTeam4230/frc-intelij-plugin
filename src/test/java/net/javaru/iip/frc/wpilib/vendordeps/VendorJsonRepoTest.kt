/*
 * Copyright 2015-2026 the original author or authors.
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

package net.javaru.iip.frc.wpilib.vendordeps

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class VendorJsonRepoTest
{
    @ParameterizedTest
    @CsvSource(
        "2026.1.0, 2026.0.4",
        "2026.10.1, 2026.9.16",
        "2026.1.26.1, 2026.1.26",
        "2026.2.13, 2026.2.09",
        "2026.1.1, 2026.1.1-rc-4",
        "2026.1.1-rc-4, 2026.1.1-rc-3",
        "v2026.3.4, v2026.3.3",
        "26.3.0, 26.2.0",
              )
    fun `newer version compares greater than older version`(newer: String, older: String)
    {
        assertTrue(compareVersionText(newer, older) > 0, "Expected $newer > $older")
        assertTrue(compareVersionText(older, newer) < 0, "Expected $older < $newer")
    }

    @ParameterizedTest
    @CsvSource(
        "2027.0.0-alpha-3, 2026, 2027", // ChoreoLib's jsonUrl: frcYear not updated for the next season's alpha
        "2027.2.0,         ,     2027", // DogLog's jsonUrl: no frcYear
        "v2027.0.0-alpha-2,,     2027", // photonlib's jsonUrl
        "26.3.0,           2026, 2026", // CTRE Phoenix 6
        "26.0.2,           ,     2026", // AdvantageKit
        "2026.9.17,        2026, 2026", // YAGSL
        "5.36.0,           2026, 2026", // CTRE Phoenix 5
        "0.4.0-beta,       ,         ", // maple-sim
        "1.0.0,            ,         ", // WPILibNewCommands
              )
    fun `likely season year is determined from frcYear and version`(version: String, frcYear: Int?, expected: Int?)
    {
        val vendordeps = Vendordeps(UUID.randomUUID(), "Test", LibVersion.parse(version), frcYear, "Test.json", null, emptyList())
        assertEquals(expected, vendordeps.likelySeasonYear())
    }
}
