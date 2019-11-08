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

import com.google.common.collect.ImmutableList
import net.javaru.iip.frc.wpilib.version.WpiLibVersion.PreReleaseModifier
import org.apache.commons.lang3.RandomUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.RepeatedTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import java.util.stream.Stream


internal class WpiLibVersionTest
{
    @Test
    fun usesGradle()
    {
        assertAll(
                { assertFalse(v15.usesGradle()) },
                { assertFalse(v16_03.usesGradle()) },
                { assertFalse(v2017_1_1.usesGradle()) },
                { assertFalse(v2018_1_1_beta_2.usesGradle()) },
                { assertFalse(v2018_6_1.usesGradle()) },
                { assertTrue(v2019_0_0.usesGradle()) },
                { assertTrue(v2019_1_1.usesGradle()) },
                { assertTrue(v2020_1_1.usesGradle()) }
                 )
    }

    @Test
    fun usesAnt()
    {
        assertAll(
                { assertTrue(v15.usesAnt()) },
                { assertTrue(v16_03.usesAnt()) },
                { assertTrue(v2017_1_1.usesAnt()) },
                { assertTrue(v2018_1_1_beta_2.usesAnt()) },
                { assertTrue(v2018_6_1.usesAnt()) },
                { assertFalse(v2019_0_0.usesAnt()) },
                { assertFalse(v2019_1_1.usesAnt()) },
                { assertFalse(v2020_1_1.usesAnt()) }
                 )
    }

    @ParameterizedTest
    @MethodSource("parseProvider")
    fun parse(expected: WpiLibVersion, actual: WpiLibVersion)
    {
        assertAll(
                { assertEquals(expected.generation, actual.generation, "Generation") },
                { assertEquals(expected.major, actual.major, "Major") },
                { assertEquals(expected.minor, actual.minor, "Minor") },
                { assertEquals(expected.patch, actual.patch, "Path") },
                { assertEquals(expected.preReleaseModifier, actual.preReleaseModifier, "PreReleaseModifier") },
                { assertEquals(expected.preReleaseModifierVersion, actual.preReleaseModifierVersion, "PreReleaseModifierVersion") }
                 )
    }
    
    @Test
    fun parseOneOffs()
    {
        // TODO: These parse ok, but they some do not sort properly. Need to modify the version classes, then add these to the list to be sorted
        //       See Issue #34 and the notes in Issue #23
        assertAll(
                
                { assertNotNull(WpiLibVersionImpl.parse("2020.1.1-beta-1"))},
                { assertNotNull(WpiLibVersionImpl.parse("2020.1.1-beta-2"))},
                { assertNotNull(WpiLibVersionImpl.parse("2019.1.1-beta-4-pre1"))},
                { assertNotNull(WpiLibVersionImpl.parse("2019.1.1-beta-4a"))},
                { assertNotNull(WpiLibVersionImpl.parse("2019.1.1-beta-4b"))},
                { assertNotNull(WpiLibVersionImpl.parse("2019.1.1-beta-4c"))}
                 )
    }

    @RepeatedTest(5)
    fun compareTo()
    {
        // To test compareTo, we shuffle the list, and then resort it (since sort uses the compareTo)
        val list = ArrayList(versionList)
        list.shuffle()
        list.sort()
        assertEquals(versionList, list, "Sorted list is not equal to expected")
    }

    @ParameterizedTest
    @MethodSource("versionListProvider")
    fun areEqual(libVersion: WpiLibVersion)
    {
        assertAll(
                { assertEquals(libVersion, libVersion, "Equals method does not return true for same object") },
                { assertEquals(libVersion, WpiLibVersionImpl.parse(libVersion.toString()), "Equals method did not return true for equal WpiLibVersion objects using parse") },
                { assertEquals(libVersion, libVersion.cloneIt(), "Equals method did not return true for equal WpiLibVersion objects using clone") }
                 )
    }

    @ParameterizedTest
    @MethodSource("areNotEqualProvider")
    fun areNotEqual(tested: WpiLibVersion, other: WpiLibVersion)
    {
        assertAll(
                { assertFalse(tested == other, "version '$tested' should not equal version '$other' but did")}
                 )
       
    }
    
    @ParameterizedTest
    @MethodSource("isNewerThanProvider")
    fun isNewerThan(x: WpiLibVersion, y: WpiLibVersion, expectedIsXNewerThanY: Boolean)
    {
        val actual = x.isNewerThan(y)
        assertEquals(expectedIsXNewerThanY,
                     actual,
                     "" + x.versionString + ".isNewerThan(" + y.versionString + ") returned " + actual + " but should have been "
                     + expectedIsXNewerThanY)
    }

    @ParameterizedTest
    @MethodSource("isNewerThanProvider")
    fun isSameOrNewerThan(x: WpiLibVersion, y: WpiLibVersion, expectedIsXNewerThanY: Boolean)
    {
        val actual = x.isSameOrNewerThan(y)
        val actualX = x.isSameOrNewerThan(x)
        val actualY = y.isSameOrNewerThan(y)
        assertAll(
                {
                    assertEquals(expectedIsXNewerThanY,
                                 actual,
                                 "" + x.versionString + ".isSameOrNewerThan(" + y.versionString + ") returned " + actual + " but should have been"
                                 + expectedIsXNewerThanY)
                },
                {
                    assertTrue(actualX,
                               "" + x.versionString + ".isSameOrNewerThan(" + x.versionString + ") returned " + actualX
                               + " but should have been true")
                },
                {
                    assertTrue(actualY,
                               "" + y.versionString + ".isSameOrNewerThan(" + y.versionString + ") returned " + actualY
                               + " but should have been true")
                }
                 )
    }


    @ParameterizedTest
    @MethodSource("isOlderThanProvider")
    fun isOlderThan(x: WpiLibVersion, y: WpiLibVersion, expectedIsXOlderThanY: Boolean)
    {
        val actual = x.isOlderThan(y)
        assertEquals(expectedIsXOlderThanY,
                     actual,
                     "" + x.versionString + ".isOlderThan(" + y.versionString + ") returned " + actual + " but should have been "
                     + expectedIsXOlderThanY)
    }


    @ParameterizedTest
    @MethodSource("isOlderThanProvider")
    fun isSameOrOlderThan(x: WpiLibVersion, y: WpiLibVersion, expectedIsXOlderThanY: Boolean)
    {
        val actual = x.isSameOrOlderThan(y)
        val actualX = x.isSameOrOlderThan(x)
        val actualY = y.isSameOrOlderThan(y)
        assertAll(
                {
                    assertEquals(expectedIsXOlderThanY,
                                 actual,
                                 "" + x.versionString + ".isSameOrOlderThan(" + y.versionString + ") returned " + actual + " but should have been"
                                 + expectedIsXOlderThanY)
                },
                {
                    assertTrue(actualX,
                               "" + x.versionString + ".isSameOrOlderThan(" + x.versionString + ") returned " + actualX
                               + " but should have been true")
                },
                {
                    assertTrue(actualY,
                               "" + y.versionString + ".isSameOrOlderThan(" + y.versionString + ") returned " + actualY
                               + " but should have been true")
                }
                 )
    }


    @ParameterizedTest
    @MethodSource("isOlderThanProvider")
    @SuppressWarnings("unused")
    fun isSameAs(x: WpiLibVersion, y: WpiLibVersion)
    {
        val actualX = x.isSameAs(x)
        val actualY = y.isSameAs(y)
        val actualXtoY = x.isSameAs(y)
        val actualYtoX = y.isSameAs(x)
        assertAll(
                {
                    assertTrue(actualX,
                               "" + x.versionString + ".isSameAs(" + x.versionString + ") returned " + actualX
                               + " but should have been true")
                },
                {
                    assertTrue(actualY,
                               "" + y.versionString + ".isSameAs(" + y.versionString + ") returned " + actualY
                               + " but should have been true")
                },
                {
                    assertFalse(actualXtoY,
                                "" + x.versionString + ".isSameAs(" + y.versionString + ") returned " + actualXtoY
                                + " but should have been false")
                },
                {
                    assertFalse(actualXtoY,
                                "" + y.versionString + ".isSameAs(" + x.versionString + ") returned " + actualYtoX
                                + " but should have been false")
                }
                 )
    }


    internal class ExpectedWpiLibVersion(override val generation: Int,
                                         override val major: Int,
                                         override val minor: Int,
                                         override val patch: Int,
                                         override val preReleaseModifier: PreReleaseModifier?,
                                         override val preReleaseModifierVersion: Int?) : WpiLibVersion
    {


        override val versionString: String
            get() = throw UnsupportedOperationException("Not supported fo test impl")


        override fun cloneIt(): WpiLibVersion
        {
            return ExpectedWpiLibVersion(this.generation, this.major, this.minor, this.patch, this.preReleaseModifier, this.preReleaseModifierVersion)
        }
    }

    @Suppress("unused")
    companion object
    {

        @JvmStatic
        fun versionListProvider(): Stream<Arguments> = versionList.stream().map { Arguments.of(it) }

        @JvmStatic
        fun isNewerThanProvider(): Iterable<Arguments> = createArgsList(false, true)


        @JvmStatic
        fun isOlderThanProvider(): Iterable<Arguments> = createArgsList(true, false)


        private fun createArgsList(oldToNew: Boolean, newToOld: Boolean): List<Arguments>
        {
            val args = ArrayList<Arguments>()

            for (i in 0 until versionList.size - 1)
            {
                val olderVer = versionList[i]
                val newerVer = versionList[i + 1]
                args.add(Arguments.of(olderVer, newerVer, oldToNew))
                args.add(Arguments.of(newerVer, olderVer, newToOld))
            }


            run {
                var i = 0
                while (i < versionList.size - 2)
                {
                    val olderVer = versionList[i]
                    val newerVer = versionList[i + 2]
                    args.add(Arguments.of(olderVer, newerVer, oldToNew))
                    args.add(Arguments.of(newerVer, olderVer, newToOld))
                    i += 2
                }
            }

            run {
                var i = 0
                while (i < versionList.size - 3)
                {
                    val olderVer = versionList[i]
                    val newerVer = versionList[i + 3]
                    args.add(Arguments.of(olderVer, newerVer, oldToNew))
                    args.add(Arguments.of(newerVer, olderVer, newToOld))
                    i += 3
                }
            }

            for (i in versionList.size - 1 downTo 2)
            {
                val olderVer = versionList[i - 1]
                val newerVer = versionList[i]
                args.add(Arguments.of(olderVer, newerVer, oldToNew))
                args.add(Arguments.of(newerVer, olderVer, newToOld))
            }
            return args
        }

        @JvmStatic
        fun areNotEqualProvider(): Iterable<Arguments>
        {
            val args = ArrayList<Arguments>()
            versionList.forEachIndexed { index, wpiLibVersion -> 
                var randomIndex: Int
                do {
                    randomIndex = RandomUtils.nextInt(0, versionList.size)
                } while (randomIndex == index)
                args.add(Arguments.of(wpiLibVersion, versionList.get(randomIndex)))
            }
            return args;
        }

        @JvmStatic
        fun parseProvider(): Iterable<Arguments>
        {
            val args = ArrayList<Arguments>()
            var expected: WpiLibVersion

            expected = ExpectedWpiLibVersion(2017, 2018, 1, 2, PreReleaseModifier.alpha, 5)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-alpha-5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.alpha-5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-alpha.5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.alpha.5")))

            expected = ExpectedWpiLibVersion(2017, 2018, 1, 2, PreReleaseModifier.beta, 5)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta-5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta-5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta.5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta.5")))

            expected = ExpectedWpiLibVersion(2017, 2018, 1, 2, PreReleaseModifier.rc, 5)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-rc-5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.rc-5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-rc.5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.rc.5")))

            expected = ExpectedWpiLibVersion(2017, 2018, 1, 2, PreReleaseModifier.beta, null)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta")))

            expected = ExpectedWpiLibVersion(2017, 2018, 1, 2, null, null)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2")))

            expected = ExpectedWpiLibVersion(2017, 2018, 1, 0, null, null)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1")))

            return args
        }

        private val v15 = WpiLibVersionImpl.parse("0.1.0.201502241928")
        private val v16_02 = WpiLibVersionImpl.parse("0.1.0.201602112135")
        private val v16_03 = WpiLibVersionImpl.parse("0.1.0.201603020231")
        private val v2017_1_1_alpha_1 = WpiLibVersionImpl.parse("2017.1.1.alpha-1")
        private val v2017_1_1_alpha_2 = WpiLibVersionImpl.parse("2017.1.1.alpha-2")
        private val v2017_1_1_beta_1 = WpiLibVersionImpl.parse("2017.1.1.beta-1")
        private val v2017_1_1_beta_2 = WpiLibVersionImpl.parse("2017.1.1.beta-2")
        private val v2017_1_1_beta_3 = WpiLibVersionImpl.parse("2017.1.1.beta-3")
        private val v2017_1_1_rc_1 = WpiLibVersionImpl.parse("2017.1.1.rc-1")
        private val v2017_1_1_rc_2 = WpiLibVersionImpl.parse("2017.1.1.rc-2")
        private val v2017_1_1 = WpiLibVersionImpl.parse("2017.1.1")
        private val v2017_1_2_rc_1 = WpiLibVersionImpl.parse("2017.1.2.rc-1")
        private val v2017_1_2 = WpiLibVersionImpl.parse("2017.1.2")
        private val v2017_2_1_beta_1 = WpiLibVersionImpl.parse("2017.2.1.beta-1")
        private val v2017_2_1_rc_1 = WpiLibVersionImpl.parse("2017.2.1.rc-1")
        private val v2017_2_1_rc_2 = WpiLibVersionImpl.parse("2017.2.1.rc-2")
        private val v2017_2_1 = WpiLibVersionImpl.parse("2017.2.1")
        private val v2018_1_1_alpha_1 = WpiLibVersionImpl.parse("2018.1.1.alpha-1")
        private val v2018_1_1_alpha_2 = WpiLibVersionImpl.parse("2018.1.1.alpha-2")
        private val v2018_1_1_alpha_3 = WpiLibVersionImpl.parse("2018.1.1.alpha-3")
        private val v2018_1_1_alpha_4 = WpiLibVersionImpl.parse("2018.1.1.alpha-4")
        private val v2018_1_1_alpha_5 = WpiLibVersionImpl.parse("2018.1.1.alpha-5")
        private val v2018_1_1_alpha_6 = WpiLibVersionImpl.parse("2018.1.1.alpha-6")
        private val v2018_1_1_beta_1 = WpiLibVersionImpl.parse("2018.1.1.beta-1")
        private val v2018_1_1_beta_2 = WpiLibVersionImpl.parse("2018.1.1.beta-2")
        private val v2018_1_1_beta_3 = WpiLibVersionImpl.parse("2018.1.1.beta-3")
        private val v2018_1_1_beta_4 = WpiLibVersionImpl.parse("2018.1.1.beta-4")
        private val v2018_1_1_beta_5 = WpiLibVersionImpl.parse("2018.1.1.beta-5")
        private val v2018_1_1_beta_6 = WpiLibVersionImpl.parse("2018.1.1.beta-6")
        private val v2018_1_1_rc_1 = WpiLibVersionImpl.parse("2018.1.1.rc-1")
        private val v2018_1_1_rc_2 = WpiLibVersionImpl.parse("2018.1.1.rc-2")
        private val v2018_1_1_rc_3 = WpiLibVersionImpl.parse("2018.1.1.rc-3")
        private val v2018_1_1_rc_4 = WpiLibVersionImpl.parse("2018.1.1.rc-4")
        private val v2018_1_1_rc_5 = WpiLibVersionImpl.parse("2018.1.1.rc-5")
        private val v2018_1_1_rc_6 = WpiLibVersionImpl.parse("2018.1.1.rc-6")
        private val v2018_1_1 = WpiLibVersionImpl.parse("2018.1.1")
        private val v2018_1_2 = WpiLibVersionImpl.parse("2018.1.2")
        private val v2018_1_3 = WpiLibVersionImpl.parse("2018.1.3")
        private val v2018_1_4 = WpiLibVersionImpl.parse("2018.1.4")
        private val v2018_1_5 = WpiLibVersionImpl.parse("2018.1.5")
        private val v2018_2_0 = WpiLibVersionImpl.parse("2018.2.0")
        private val v2018_2_1 = WpiLibVersionImpl.parse("2018.2.1")
        private val v2018_2_2 = WpiLibVersionImpl.parse("2018.2.2")
        private val v2018_2_3 = WpiLibVersionImpl.parse("2018.2.3")
        private val v2018_2_4 = WpiLibVersionImpl.parse("2018.2.4")
        private val v2018_2_5 = WpiLibVersionImpl.parse("2018.2.5")
        private val v2018_3_1 = WpiLibVersionImpl.parse("2018.3.1")
        private val v2018_4_1 = WpiLibVersionImpl.parse("2018.4.1")
        private val v2018_5_1 = WpiLibVersionImpl.parse("2018.5.1")
        private val v2018_5_2 = WpiLibVersionImpl.parse("2018.5.2")
        private val v2018_6_1 = WpiLibVersionImpl.parse("2018.6.1")
        private val v2019_0_0 = WpiLibVersionImpl.parse("2019.0.0")
        private val v2019_0_1 = WpiLibVersionImpl.parse("2019.0.1")
        private val v2019_1_1 = WpiLibVersionImpl.parse("2019.1.1")
        private val v2019_1_2 = WpiLibVersionImpl.parse("2019.1.2")
        private val v2020_1_1 = WpiLibVersionImpl.parse("2020.1.1")
        private val v2020_1_2 = WpiLibVersionImpl.parse("2020.1.2")

        private val versionList: ImmutableList<WpiLibVersion>

        init
        {
            val list = ImmutableList.builder<WpiLibVersion>()

            list.add(v15)
            list.add(v16_02)
            list.add(v16_03)
            list.add(v2017_1_1_alpha_1)
            list.add(v2017_1_1_alpha_2)
            list.add(v2017_1_1_beta_1)
            list.add(v2017_1_1_beta_2)
            list.add(v2017_1_1_beta_3)
            list.add(v2017_1_1_rc_1)
            list.add(v2017_1_1_rc_2)
            list.add(v2017_1_1)
            list.add(v2017_1_2_rc_1)
            list.add(v2017_1_2)
            list.add(v2017_2_1_beta_1)
            list.add(v2017_2_1_rc_1)
            list.add(v2017_2_1_rc_2)
            list.add(v2017_2_1)
            list.add(v2018_1_1_alpha_1)
            list.add(v2018_1_1_alpha_2)
            list.add(v2018_1_1_alpha_3)
            list.add(v2018_1_1_alpha_4)
            list.add(v2018_1_1_alpha_5)
            list.add(v2018_1_1_alpha_6)
            list.add(v2018_1_1_beta_1)
            list.add(v2018_1_1_beta_2)
            list.add(v2018_1_1_beta_3)
            list.add(v2018_1_1_beta_4)
            list.add(v2018_1_1_beta_5)
            list.add(v2018_1_1_beta_6)
            list.add(v2018_1_1_rc_1)
            list.add(v2018_1_1_rc_2)
            list.add(v2018_1_1_rc_3)
            list.add(v2018_1_1_rc_4)
            list.add(v2018_1_1_rc_5)
            list.add(v2018_1_1_rc_6)
            list.add(v2018_1_1)
            list.add(v2018_1_2)
            list.add(v2018_1_3)
            list.add(v2018_1_4)
            list.add(v2018_1_5)
            list.add(v2018_2_0)
            list.add(v2018_2_1)
            list.add(v2018_2_2)
            list.add(v2018_2_3)
            list.add(v2018_2_4)
            list.add(v2018_2_5)
            list.add(v2018_3_1)
            list.add(v2018_4_1)
            list.add(v2018_5_1)
            list.add(v2018_5_2)
            list.add(v2018_6_1)
            list.add(v2019_0_0)
            list.add(v2019_0_1)
            list.add(v2019_1_1)
            list.add(v2019_1_2)
            list.add(v2020_1_1)
            list.add(v2020_1_2)

            versionList = list.build()
        }
    }
}