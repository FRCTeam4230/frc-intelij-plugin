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

package net.javaru.iip.frc.run

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import kotlin.test.assertEquals


internal class RunDebugConfigurationsKtTest
{
    @Test
    fun determineNextName()
    {
        val baseName = "My Run Config"
        assertAll(
                { assertEquals(baseName, determineNextName(listOf(), baseName)) },
                { assertEquals(baseName, determineNextName(listOf("Foo", "Bar"), baseName)) },
                { assertEquals("$baseName (1)", determineNextName(listOf(baseName), baseName)) },
                { assertEquals("$baseName (1)", determineNextName(listOf("Foo", baseName, "Bar"), baseName)) },
                { assertEquals("$baseName (2)", determineNextName(listOf("$baseName (1)"), baseName)) },
                { assertEquals("$baseName (3)", determineNextName(listOf("$baseName (1)", "$baseName (2)"), baseName)) },
                { assertEquals("$baseName (4)", determineNextName(listOf("$baseName (3)", "$baseName (1)", "$baseName (2)"), baseName)) },
                { assertEquals("$baseName (5)", determineNextName(listOf("Foo", "$baseName (3)", "$baseName (4)", "Bar", "$baseName (1)", "$baseName (2)", "Baz"), baseName)) },
                { assertEquals("$baseName (6)", determineNextName(listOf("$baseName (5)", "Baz"), baseName)) }
                 )
    }
}