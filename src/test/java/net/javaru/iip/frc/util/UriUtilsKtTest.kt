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

package net.javaru.iip.frc.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.net.URI


internal class UriUtilsKtTest
{
    @Test
    fun resolveSiblingResource()
    {
        val uri = URI("https://example.com/data/foo.txt")
        assertAll(
                { assertEquals(URI("https://example.com/data/images/chart.png"), uri.resolve("images/chart.png"))},
                { assertEquals(URI("https://example.com/images/chart.png"), uri.resolve("/images/chart.png"))}
                 )
    }
}