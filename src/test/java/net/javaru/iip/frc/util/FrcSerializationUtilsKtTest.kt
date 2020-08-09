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

package net.javaru.iip.frc.util

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertAll

internal class FrcSerializationUtilsKtTest
{
    @Language("JSON")
    private val nonPretty = """{"x":5,"y":10}"""

    @Language("JSON")
    private val pretty = """
        {
          "x" : 5,
          "y" : 10
        }""".trimIndent().eolTo(EOL.UNIX)


    private val nullPoint: MutableTestPoint? = null
    private val immutablePoint = ImmutableTestPoint(5, 10)
    private val mutablePoint = MutableTestPoint(5, 10)

    @Test
    fun toJson()
    {
        assertAll(
            { assertEquals(nonPretty, immutablePoint.toJson(false), "Failed for Immutable non-pretty") },
            { assertEquals(pretty, immutablePoint.toJson(true).eolTo(EOL.UNIX), "Failed for Immutable pretty") },
            { assertEquals(nonPretty, mutablePoint.toJson(false), "Failed for Mutable non-pretty") },
            { assertEquals(pretty, mutablePoint.toJson(true).eolTo(EOL.UNIX), "Failed for Mutable pretty") },

            { assertNull(nullPoint?.toJson(true), "Failed for a null receiver") }
             )

    }


    @Test
    fun fromJson()
    {
        assertAll(
            { assertEquals(immutablePoint, nonPretty.fromJson<ImmutableTestPoint>())},

            { assertEquals( immutablePoint, nonPretty.fromJson<ImmutableTestPoint>(), "Failed for Immutable non-pretty") },
            { assertEquals( immutablePoint, pretty.fromJson<ImmutableTestPoint>(), "Failed for Immutable pretty") },
            { assertEquals( mutablePoint, nonPretty.fromJson<MutableTestPoint>(), "Failed for Mutable non-pretty") },
            { assertEquals( mutablePoint, pretty.fromJson<MutableTestPoint>(), "Failed for Mutable pretty") }
                 )
    }
}

data class ImmutableTestPoint(val x: Int, val y: Int)
data class MutableTestPoint(var x: Int, var y: Int)