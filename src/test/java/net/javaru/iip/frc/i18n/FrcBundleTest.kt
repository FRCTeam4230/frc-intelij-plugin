/*
 * Copyright 2015-2021 the original author or authors.
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

package net.javaru.iip.frc.i18n

import net.javaru.iip.frc.i18n.FrcBundle.message
import net.javaru.iip.frc.i18n.FrcBundle.messageLabelCentered
import net.javaru.iip.frc.i18n.FrcBundle.messageOrDefault
import net.javaru.iip.frc.i18n.FrcBundle.messageOrNull
import net.javaru.iip.frc.setInTestModeSystemProperty
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.util.*


internal class FrcBundleTest
{
    private val foo = "foo"
    private val bar = "bar"
    private val uuid = UUID.fromString("1234abcd-12ab-34cd-56ef-123456abcdef")
    private val expected0 = "My test message."
    private val expected1 = "My test message with a parameter of «foo»."
    private val expected2 = "My test message with parameter 0 of «foo» and parameter 1 of «bar»."
    private val expected2Flipped = "My test message with parameter 1 of «bar» and parameter 0 of «foo»."
    private val expected3 = "My test message with parameter 0 of «foo», parameter 1 of «bar» and parameter 2 of «${uuid}»."
    private val myDefaultMsg = "My Unit Test's Default Message"


    @Suppress("unused")
    companion object
    {
        @JvmStatic
        @BeforeAll
        internal fun initAll() = setInTestModeSystemProperty()
    }

    @Test
    fun testMessage()
    {
        println(uuid)
        assertAll(
                { assertEquals(expected0, message("frc.internal.unitTest.0")) },
                { assertEquals(expected1, message("frc.internal.unitTest.1", foo)) },
                { assertEquals(expected2, message("frc.internal.unitTest.2", foo, bar)) },
                { assertEquals(expected2Flipped, message("frc.internal.unitTest.2.flipped", foo, bar)) },
                { assertEquals(expected3, message("frc.internal.unitTest.3", foo, bar, uuid)) }
                 )
    }

    @Test
    fun testMessageKeyObj()
    {
        assertAll(
                { assertEquals(expected0, message(FrcMessageKey.of("frc.internal.unitTest.0"))) },
                { assertEquals(expected1, message(FrcMessageKey.of("frc.internal.unitTest.1", foo))) },
                { assertEquals(expected2, message(FrcMessageKey.of("frc.internal.unitTest.2", foo, bar))) },
                { assertEquals(expected2Flipped, message(FrcMessageKey.of("frc.internal.unitTest.2.flipped", foo, bar))) },
                { assertEquals(expected3, message(FrcMessageKey.of("frc.internal.unitTest.3", foo, bar, uuid))) }
                 )
    }

    @Suppress("InvalidBundleOrProperty")
    @Test
    fun testMessageOrDefault()
    {
        assertAll(
                { assertEquals(expected0, messageOrDefault("frc.internal.unitTest.0", myDefaultMsg)) },
                { assertEquals(expected1, messageOrDefault("frc.internal.unitTest.1", myDefaultMsg, foo)) },
                { assertEquals(expected2, messageOrDefault("frc.internal.unitTest.2", myDefaultMsg, foo, bar)) },
                { assertEquals(expected2Flipped, messageOrDefault("frc.internal.unitTest.2.flipped", myDefaultMsg, foo, bar)) },
                { assertEquals(expected3, messageOrDefault("frc.internal.unitTest.3", myDefaultMsg, foo, bar, uuid)) },

                { assertEquals(myDefaultMsg, messageOrDefault("my.invalid.key", myDefaultMsg)) },
                { assertEquals(myDefaultMsg, messageOrDefault("my.invalid.key", myDefaultMsg, foo)) },
                { assertEquals(myDefaultMsg, messageOrDefault("my.invalid.key", myDefaultMsg, foo, bar)) },
                { assertEquals(myDefaultMsg, messageOrDefault("my.invalid.key", myDefaultMsg, foo, bar, uuid)) },

                { assertEquals("!my.invalid.key!", messageOrDefault("my.invalid.key", null)) },
                { assertEquals("!my.invalid.key!", messageOrDefault("my.invalid.key", null, foo)) },
                { assertEquals("!my.invalid.key!", messageOrDefault("my.invalid.key", null, foo, bar)) },
                { assertEquals("!my.invalid.key!", messageOrDefault("my.invalid.key", null, foo, bar, uuid)) }
                 )
    }

    @Suppress("InvalidBundleOrProperty")
    @Test
    fun testMessageOrDefaultKeyObj()
    {
        assertAll(
                { assertEquals(expected0, messageOrDefault(FrcMessageKey.of("frc.internal.unitTest.0"), myDefaultMsg)) },
                { assertEquals(expected1, messageOrDefault(FrcMessageKey.of("frc.internal.unitTest.1", foo), myDefaultMsg)) },
                { assertEquals(expected2, messageOrDefault(FrcMessageKey.of("frc.internal.unitTest.2", foo, bar), myDefaultMsg)) },
                { assertEquals(expected2Flipped, messageOrDefault(FrcMessageKey.of("frc.internal.unitTest.2.flipped", foo, bar), myDefaultMsg)) },
                { assertEquals(expected3, messageOrDefault(FrcMessageKey.of("frc.internal.unitTest.3", foo, bar, uuid), myDefaultMsg)) },

                { assertEquals(myDefaultMsg, messageOrDefault(FrcMessageKey.of("my.invalid.key"), myDefaultMsg)) },
                { assertEquals(myDefaultMsg, messageOrDefault(FrcMessageKey.of("my.invalid.key", foo), myDefaultMsg)) },
                { assertEquals(myDefaultMsg, messageOrDefault(FrcMessageKey.of("my.invalid.key", foo, bar), myDefaultMsg)) },
                { assertEquals(myDefaultMsg, messageOrDefault(FrcMessageKey.of("my.invalid.key", foo, bar, uuid), myDefaultMsg)) },

                { assertEquals("!my.invalid.key!", messageOrDefault(FrcMessageKey.of("my.invalid.key"), null)) },
                { assertEquals("!my.invalid.key!", messageOrDefault(FrcMessageKey.of("my.invalid.key", foo), null)) },
                { assertEquals("!my.invalid.key!", messageOrDefault(FrcMessageKey.of("my.invalid.key", foo, bar), null)) },
                { assertEquals("!my.invalid.key!", messageOrDefault(FrcMessageKey.of("my.invalid.key", foo, bar, uuid), null)) }
                 )
    }

    @Suppress("InvalidBundleOrProperty")
    @Test
    fun testMessageOrNull()
    {
        assertAll(
                { assertEquals(expected0, message("frc.internal.unitTest.0")) },
                { assertEquals(expected1, message("frc.internal.unitTest.1", foo)) },
                { assertEquals(expected2, message("frc.internal.unitTest.2", foo, bar)) },
                { assertEquals(expected2Flipped, message("frc.internal.unitTest.2.flipped", foo, bar)) },
                { assertEquals(expected3, message("frc.internal.unitTest.3", foo, bar, uuid)) },

                { assertNull(messageOrNull("my.invalid.key")) },
                { assertNull(messageOrNull("my.invalid.key", foo)) },
                { assertNull(messageOrNull("my.invalid.key", foo, bar)) },
                { assertNull(messageOrNull("my.invalid.key", foo, bar, uuid)) }
                 )
    }

    @Suppress("InvalidBundleOrProperty")
    @Test
    fun testMessageOrNullKeyObj()
    {
        assertAll(
                { assertEquals(expected0, messageOrNull(FrcMessageKey.of("frc.internal.unitTest.0"))) },
                { assertEquals(expected1, messageOrNull(FrcMessageKey.of("frc.internal.unitTest.1", foo))) },
                { assertEquals(expected2, messageOrNull(FrcMessageKey.of("frc.internal.unitTest.2", foo, bar))) },
                { assertEquals(expected2Flipped, messageOrNull(FrcMessageKey.of("frc.internal.unitTest.2.flipped", foo, bar))) },
                { assertEquals(expected3, messageOrNull(FrcMessageKey.of("frc.internal.unitTest.3", foo, bar, uuid))) },

                { assertNull(messageOrNull(FrcMessageKey.of("my.invalid.key"))) },
                { assertNull(messageOrNull(FrcMessageKey.of("my.invalid.key", foo))) },
                { assertNull(messageOrNull(FrcMessageKey.of("my.invalid.key", foo, bar))) },
                { assertNull(messageOrNull(FrcMessageKey.of("my.invalid.key", foo, bar, uuid))) }
                 )
    }

    @Test
    fun testMessageLabelCentered()
    {
        assertAll(
                { assertEquals("<html><div style='text-align: center;'>$expected0</div></html>", messageLabelCentered("frc.internal.unitTest.0"))},
                { assertEquals("<html><div style='text-align: center;'>$expected1</div></html>", messageLabelCentered("frc.internal.unitTest.1", foo))},
                { assertEquals("<html><div style='text-align: center;'>$expected2</div></html>", messageLabelCentered("frc.internal.unitTest.2", foo, bar))},
                { assertEquals("<html><div style='text-align: center;'>$expected3</div></html>", messageLabelCentered("frc.internal.unitTest.3", foo, bar, uuid))},
                { assertEquals("<html><div style='text-align: center;'>$expected2Flipped</div></html>", messageLabelCentered("frc.internal.unitTest.2.flipped", foo, bar))}
                 )
    }

    @Test
    fun testMessageLabelCenteredKeyObj()
    {
        assertAll(
                { assertEquals("<html><div style='text-align: center;'>$expected0</div></html>", messageLabelCentered(FrcMessageKey.of("frc.internal.unitTest.0")))},
                { assertEquals("<html><div style='text-align: center;'>$expected1</div></html>", messageLabelCentered(FrcMessageKey.of("frc.internal.unitTest.1", foo)))},
                { assertEquals("<html><div style='text-align: center;'>$expected2</div></html>", messageLabelCentered(FrcMessageKey.of("frc.internal.unitTest.2", foo, bar)))},
                { assertEquals("<html><div style='text-align: center;'>$expected3</div></html>", messageLabelCentered(FrcMessageKey.of("frc.internal.unitTest.3", foo, bar, uuid)))},
                { assertEquals("<html><div style='text-align: center;'>$expected2Flipped</div></html>", messageLabelCentered(FrcMessageKey.of("frc.internal.unitTest.2.flipped", foo, bar)))}
                 )
    }
}