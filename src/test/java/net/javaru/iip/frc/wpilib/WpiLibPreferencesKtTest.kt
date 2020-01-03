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

package net.javaru.iip.frc.wpilib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll


internal class WpiLibPreferencesKtTest
{

    @Test
    fun extractProjectYear()
    {
        assertAll (
                { assertEquals(2020, extractProjectYear("Beta2020"), "Wrong value for 'Beta2020'") },
                { assertEquals(2020, extractProjectYear("Beta2020-2"), "Wrong value for 'Beta2020-2'") },
                { assertEquals(2020, extractProjectYear("2020Beta"), "Wrong value for '2020Beta'") },
                { assertEquals(2021, extractProjectYear("2021"), "Wrong value for '2021'") },
                { assertEquals(2022, extractProjectYear("  2022  "), "Wrong value for '  2022  '") }
                  )
    }
}