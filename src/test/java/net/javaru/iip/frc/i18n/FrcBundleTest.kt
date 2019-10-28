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

package net.javaru.iip.frc.i18n

import net.javaru.iip.frc.i18n.FrcBundle.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

@Suppress("InvalidBundleOrProperty")
internal class FrcBundleTest
{
    private val expected0 = "My test message."
    private val expected1 = "My test message with a parameter of «foo»."
    private val expected2 = "My test message with parameter 0 of «foo» and parameter 1 of «bar»."
    private val expected2Flipped = "My test message with parameter 1 of «bar» and parameter 0 of «foo»."
    private val myDefaultMsg = "Default Message"
    private val foo = "foo"
    private val bar = "bar"
    @Test
    fun testMessage()
    {
        assertAll(
                { assertEquals(expected0, message("frc.unitTest.0")) },
                { assertEquals(expected1, message("frc.unitTest.1", foo)) },
                { assertEquals(expected2, message("frc.unitTest.2", foo, bar)) },
                { assertEquals(expected2Flipped, message("frc.unitTest.2.flipped", foo, bar)) }
                 )
    }

    @Test
    fun testMessageKeyObj()
    {
        assertAll(
                { assertEquals(expected0, message(FrcMessageKey.of("frc.unitTest.0"))) },
                { assertEquals(expected1, message(FrcMessageKey.of("frc.unitTest.1", foo))) },
                { assertEquals(expected2, message(FrcMessageKey.of("frc.unitTest.2", foo, bar))) },
                { assertEquals(expected2Flipped, message(FrcMessageKey.of("frc.unitTest.2.flipped", foo, bar))) }
                 )
    }

    @Test
    fun testMessageOrDefault()
    {
        assertAll(
                { assertEquals(expected0, messageOrDefault("frc.unitTest.0", myDefaultMsg)) },
                { assertEquals(expected1, messageOrDefault("frc.unitTest.1", myDefaultMsg, foo)) },
                { assertEquals(expected2, messageOrDefault("frc.unitTest.2", myDefaultMsg, foo, bar)) },
                { assertEquals(expected2Flipped, messageOrDefault("frc.unitTest.2.flipped", myDefaultMsg, foo, bar)) },

                { assertEquals(myDefaultMsg, messageOrDefault("my.invalid.key", myDefaultMsg)) },
                { assertEquals(myDefaultMsg, messageOrDefault("my.invalid.key", myDefaultMsg, foo)) },
                { assertEquals(myDefaultMsg, messageOrDefault("my.invalid.key", myDefaultMsg, foo, bar)) },
                { assertEquals(myDefaultMsg, messageOrDefault("my.invalid.key", myDefaultMsg, foo, bar)) },

                { assertEquals("!my.invalid.key!", messageOrDefault("my.invalid.key", null)) },
                { assertEquals("!my.invalid.key!", messageOrDefault("my.invalid.key", null, foo)) },
                { assertEquals("!my.invalid.key!", messageOrDefault("my.invalid.key", null, foo, bar)) },
                { assertEquals("!my.invalid.key!", messageOrDefault("my.invalid.key", null, foo, bar)) }
                 )
    }

    @Test
    fun testMessageOrDefaultKeyObj()
    {
        assertAll(
                { assertEquals(expected0, messageOrDefault(FrcMessageKey.of("frc.unitTest.0"), myDefaultMsg)) },
                { assertEquals(expected1, messageOrDefault(FrcMessageKey.of("frc.unitTest.1", foo), myDefaultMsg)) },
                { assertEquals(expected2, messageOrDefault(FrcMessageKey.of("frc.unitTest.2", foo, bar), myDefaultMsg)) },
                { assertEquals(expected2Flipped, messageOrDefault(FrcMessageKey.of("frc.unitTest.2.flipped", foo, bar), myDefaultMsg)) },

                { assertEquals(myDefaultMsg, messageOrDefault(FrcMessageKey.of("my.invalid.key"), myDefaultMsg)) },
                { assertEquals(myDefaultMsg, messageOrDefault(FrcMessageKey.of("my.invalid.key", foo), myDefaultMsg)) },
                { assertEquals(myDefaultMsg, messageOrDefault(FrcMessageKey.of("my.invalid.key", foo, bar), myDefaultMsg)) },
                { assertEquals(myDefaultMsg, messageOrDefault(FrcMessageKey.of("my.invalid.key", foo, bar), myDefaultMsg)) },

                { assertEquals("!my.invalid.key!", messageOrDefault(FrcMessageKey.of("my.invalid.key"), null)) },
                { assertEquals("!my.invalid.key!", messageOrDefault(FrcMessageKey.of("my.invalid.key", foo), null)) },
                { assertEquals("!my.invalid.key!", messageOrDefault(FrcMessageKey.of("my.invalid.key", foo, bar), null)) },
                { assertEquals("!my.invalid.key!", messageOrDefault(FrcMessageKey.of("my.invalid.key", foo, bar), null)) }
                 )
    }

    @Test
    fun testMessageOrNull()
    {
        assertAll(
                { assertNotNull(message("frc.unitTest.0")) },
                { assertNotNull(message("frc.unitTest.1", foo)) },
                { assertNotNull(message("frc.unitTest.2", foo, bar)) },
                { assertNotNull(message("frc.unitTest.2.flipped", foo, bar)) },

                { assertNull(messageOrNull("my.invalid.key")) },
                { assertNull(messageOrNull("my.invalid.key", foo)) },
                { assertNull(messageOrNull("my.invalid.key", foo, bar)) },
                { assertNull(messageOrNull("my.invalid.key", foo, bar)) }
                 )
    }

    @Test
    fun testMessageOrNullKeyObj()
    {
        assertAll(
                { assertNotNull(messageOrNull(FrcMessageKey.of("frc.unitTest.0"))) },
                { assertNotNull(messageOrNull(FrcMessageKey.of("frc.unitTest.1", foo))) },
                { assertNotNull(messageOrNull(FrcMessageKey.of("frc.unitTest.2", foo, bar))) },
                { assertNotNull(messageOrNull(FrcMessageKey.of("frc.unitTest.2.flipped", foo, bar))) },

                { assertNull(messageOrNull(FrcMessageKey.of("my.invalid.key"))) },
                { assertNull(messageOrNull(FrcMessageKey.of("my.invalid.key", foo))) },
                { assertNull(messageOrNull(FrcMessageKey.of("my.invalid.key", foo, bar))) },
                { assertNull(messageOrNull(FrcMessageKey.of("my.invalid.key", foo, bar))) }
                 )
    }

    @Test
    fun testMessageLabelCentered()
    {
        assertAll(
                { assertEquals("<html><div style='text-align: center;'>$expected0</div></html>", messageLabelCentered("frc.unitTest.0"))}
                 )
    }
}