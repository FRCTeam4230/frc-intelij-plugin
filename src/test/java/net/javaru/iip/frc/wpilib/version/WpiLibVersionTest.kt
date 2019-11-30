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
//    @Test
//    fun quick()
//    {
//        val version = WpiLibVersionImpl.parse("2018.06.21")
//        val x = version.minor
//        println("x = '${x}'")
//    }
    
    @Test
    fun usesGradle()
    {
        assertAll(
                { assertFalse(v15.usesGradle()) },
                { assertFalse(v16_03.usesGradle()) },
                { assertFalse(v2017_1_1.usesGradle()) },
                { assertFalse(v2018_1_1_beta_2.usesGradle()) },
                { assertFalse(v2018_5_1.usesGradle()) },
                { assertTrue(v2018_06_21.usesGradle()) },
                { assertTrue(v2019_0_1.usesGradle()) },
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
                { assertTrue(v2018_5_1.usesAnt()) },
                { assertFalse(v2018_06_21.usesAnt()) },
                { assertFalse(v2019_0_1.usesAnt()) },
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
                { assertEquals(expected.preReleaseModifierVersion, actual.preReleaseModifierVersion, "PreReleaseModifierVersion") },
                { assertEquals(expected.preReleaseModifierSubVersion, actual.preReleaseModifierSubVersion, "preReleaseModifierSubVersion") },
                { assertEquals(expected.preReleasePreviewVersion, actual.preReleasePreviewVersion, "preReleasePreviewVersion") }
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
    @MethodSource("isPreReleaseProvider")
    fun isPreRelease(ver: WpiLibVersion, isPreReleaseExpected: Boolean, isPreReleasePreviewExpected: Boolean)
    {
        assertAll(
                {assertEquals(isPreReleaseExpected, ver.isPreRelease(), "Wrong result for ${ver.versionString}.isPreRelease(). Expected $isPreReleaseExpected")},
                {assertEquals(isPreReleasePreviewExpected, ver.isPreReleasePreview(), "Wrong result for ${ver.versionString}.isPreReleasePreview(). Expected $isPreReleasePreviewExpected")}
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
                                         override val preReleaseModifierVersion: Int?,
                                         override val preReleaseModifierSubVersion: String = "",
                                         override val preReleasePreviewVersion: Int?) : WpiLibVersion
    {


        override val versionString: String
            get() = throw UnsupportedOperationException("Not supported fo test impl")


        override fun cloneIt(): WpiLibVersion
        {
            return ExpectedWpiLibVersion(this.generation, this.major, this.minor, this.patch, this.preReleaseModifier, this.preReleaseModifierVersion, this.preReleaseModifierSubVersion, this.preReleasePreviewVersion)
        }
    }

    @Suppress("unused", "BooleanLiteralArgument")
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
                args.add(Arguments.of(wpiLibVersion, versionList[randomIndex]))
            }
            return args
        }

        @JvmStatic
        fun parseProvider(): Iterable<Arguments>
        {
            val args = ArrayList<Arguments>()
            var expected: WpiLibVersion

            expected = ExpectedWpiLibVersion(2017, 2018, 1, 2, PreReleaseModifier.alpha, 5, "", null)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-alpha-5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.alpha-5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-alpha.5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.alpha.5")))

            expected = ExpectedWpiLibVersion(2017, 2018, 1, 2, PreReleaseModifier.beta, 5, "", null)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta-5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta-5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta.5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta.5")))

            expected = ExpectedWpiLibVersion(2017, 2018, 1, 2, PreReleaseModifier.rc, 5, "", null)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-rc-5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.rc-5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-rc.5")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.rc.5")))

            expected = ExpectedWpiLibVersion(2017, 2018, 1, 2, PreReleaseModifier.beta, null, "", null)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta")))

            expected = ExpectedWpiLibVersion(2017, 2018, 1, 2, null, null, "", null)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2")))

            expected = ExpectedWpiLibVersion(2017, 2018, 1, 0, null, null, "", null)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1")))

            expected = ExpectedWpiLibVersion(2017, 2018, 1, 2, PreReleaseModifier.beta, 5, "a", null)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta-5a")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta-5a")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta.5a")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta.5a")))
            
            expected = ExpectedWpiLibVersion(2017, 2018, 1, 2, PreReleaseModifier.beta, 5, "", 2)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta-5-pre2")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta-5-pre2")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta.5-pre2")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta.5-pre2")))
            
            expected = ExpectedWpiLibVersion(2017, 2018, 1, 2, PreReleaseModifier.beta, 5, "", 2)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta-5-pre-2")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta-5-pre-2")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2-beta.5-pre-2")))
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.1.2.beta.5-pre-2")))

            expected = ExpectedWpiLibVersion(2019, 2019, 1, 1, PreReleaseModifier.beta, 3, "", 2)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2019.1.1-beta-3-p-2")))
            
            expected = ExpectedWpiLibVersion(2019, 2018, 6, 21, null, null, "", null)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2018.06.21"))) // The first GradleRIO release after WPI took over the project. We classify it as a generation 2019.
            
            expected = ExpectedWpiLibVersion(2019, 2019, 0, 0, PreReleaseModifier.alpha, 1, "", null)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2019.0.0-alpha-1"))) // The second GradleRIO release after WPI took over the project, a few days after 2018.06.21
            
            expected = ExpectedWpiLibVersion(2019, 2019, 1, 1, PreReleaseModifier.rc, 1, "", null)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2019.1.1-rc-1")))
            
            expected = ExpectedWpiLibVersion(2019, 2019, 1, 1, null, null, "", null)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2019.1.1")))
            
            expected = ExpectedWpiLibVersion(2019, 2020, 2, 3, null, null, "", null)
            args.add(Arguments.of(expected, WpiLibVersionImpl.parse("2020.2.3")))
            
            
            return args
        }
        
        @JvmStatic
        fun isPreReleaseProvider(): Iterable<Arguments>
        {
            val args = ArrayList<Arguments>()
            // version, isPreRelease, isPreview
            args.add(Arguments.of(v2019_0_0_alpha_1, true, false))
            args.add(Arguments.of(v2019_0_0_beta0_pre1, true, true))
            args.add(Arguments.of(v2019_1_1_beta_1, true, false))
            args.add(Arguments.of(v2019_1_1_beta_2a, true, false))
            args.add(Arguments.of(v2019_1_1_beta_3_pre1, true, true))
            args.add(Arguments.of(v2019_1_1_beta_3_p_2, true, true))
            args.add(Arguments.of(v2019_1_1_beta_3, true, false))
            args.add(Arguments.of(v2019_1_1, false, false))
            args.add(Arguments.of(v2018_1_1_rc_1, true, false))
            args.add(Arguments.of(v2019_1_1_beta_99, true, false))
            args.add(Arguments.of(v2019_1_1_rc_1, true, false))
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
        private val v2018_06_21 = WpiLibVersionImpl.parse("2018.06.21")
        private val v2019_0_0_alpha_1    = WpiLibVersionImpl.parse("2019.0.0-alpha-1")
        private val v2019_0_0_alpha_2    = WpiLibVersionImpl.parse("2019.0.0-alpha-2")
        private val v2019_0_0_alpha_3    = WpiLibVersionImpl.parse("2019.0.0-alpha-3")
        private val v2019_0_0_beta0_pre1 = WpiLibVersionImpl.parse("2019.0.0-beta0-pre1")
        private val v2019_0_0_beta0_pre3 = WpiLibVersionImpl.parse("2019.0.0-beta0-pre3")
        private val v2019_0_0_beta0_pre4 = WpiLibVersionImpl.parse("2019.0.0-beta0-pre4")
        private val v2019_0_0_beta0_pre5 = WpiLibVersionImpl.parse("2019.0.0-beta0-pre5")
        private val v2019_0_0_beta0_pre6 = WpiLibVersionImpl.parse("2019.0.0-beta0-pre6")
        private val v2019_0_1 = WpiLibVersionImpl.parse("2019.0.1")
        private val v2019_1_1_beta_1     = WpiLibVersionImpl.parse("2019.1.1-beta-1")
        private val v2019_1_1_beta_2a     = WpiLibVersionImpl.parse("2019.1.1-beta-2a")
        private val v2019_1_1_beta_3_pre1  = WpiLibVersionImpl.parse("2019.1.1-beta-3-pre1")
        private val v2019_1_1_beta_3_p_2  = WpiLibVersionImpl.parse("2019.1.1-beta-3-p-2")
        private val v2019_1_1_beta_3_pre3 = WpiLibVersionImpl.parse("2019.1.1-beta-3-pre3")
        private val v2019_1_1_beta_3_pre4 = WpiLibVersionImpl.parse("2019.1.1-beta-3-pre4")
        private val v2019_1_1_beta_3_pre5 = WpiLibVersionImpl.parse("2019.1.1-beta-3-pre5")
        private val v2019_1_1_beta_3_pre6 = WpiLibVersionImpl.parse("2019.1.1-beta-3-pre6")
        private val v2019_1_1_beta_3_pre7 = WpiLibVersionImpl.parse("2019.1.1-beta-3-pre7")
        private val v2019_1_1_beta_3_pre8 = WpiLibVersionImpl.parse("2019.1.1-beta-3-pre8")
        private val v2019_1_1_beta_3_pre9 = WpiLibVersionImpl.parse("2019.1.1-beta-3-pre9")
        private val v2019_1_1_beta_3_pre10 = WpiLibVersionImpl.parse("2019.1.1-beta-3-pre10")
        private val v2019_1_1_beta_3      = WpiLibVersionImpl.parse("2019.1.1-beta-3")
        private val v2019_1_1_beta_3a     = WpiLibVersionImpl.parse("2019.1.1-beta-3a")
        private val v2019_1_1_beta_4_pre1 = WpiLibVersionImpl.parse("2019.1.1-beta-4-pre1")
        private val v2019_1_1_beta_4_pre2 = WpiLibVersionImpl.parse("2019.1.1-beta-4-pre2")
        private val v2019_1_1_beta_4_pre4 = WpiLibVersionImpl.parse("2019.1.1-beta-4-pre4")
        private val v2019_1_1_beta_4      = WpiLibVersionImpl.parse("2019.1.1-beta-4")
        private val v2019_1_1_beta_4a     = WpiLibVersionImpl.parse("2019.1.1-beta-4a")
        private val v2019_1_1_beta_4b     = WpiLibVersionImpl.parse("2019.1.1-beta-4b")
        private val v2019_1_1_beta_4c     = WpiLibVersionImpl.parse("2019.1.1-beta-4c")
        private val v2019_1_1_beta_5     = WpiLibVersionImpl.parse("2019.1.1-beta-5")
        private val v2019_1_1_beta_99     = WpiLibVersionImpl.parse("2019.1.1-beta-99")
        private val v2019_1_1_rc_1 = WpiLibVersionImpl.parse("2019.1.1-rc-1")
        private val v2019_1_1 = WpiLibVersionImpl.parse("2019.1.1")
        private val v2019_1_2 = WpiLibVersionImpl.parse("2019.1.2")
        private val v2019_2_1 = WpiLibVersionImpl.parse("2019.2.1")
        private val v2019_3_1 = WpiLibVersionImpl.parse("2019.3.1")
        private val v2019_3_2 = WpiLibVersionImpl.parse("2019.3.2")
        private val v2019_4_1 = WpiLibVersionImpl.parse("2019.4.1")
        private val v2020_1_1_beta_1  = WpiLibVersionImpl.parse("2020.1.1-beta-1")
        private val v2020_1_1_beta_2  = WpiLibVersionImpl.parse("2020.1.1-beta-2")
        private val v2020_1_1_beta_3  = WpiLibVersionImpl.parse("2020.1.1-beta-3")
        private val v2020_1_1_beta_3a = WpiLibVersionImpl.parse("2020.1.1-beta-3a")
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
            list.add(v2018_06_21)
            list.add(v2019_0_0_alpha_1)
            list.add(v2019_0_0_alpha_2)
            list.add(v2019_0_0_alpha_3)
            list.add(v2019_0_0_beta0_pre1)
            list.add(v2019_0_0_beta0_pre3)
            list.add(v2019_0_0_beta0_pre4)
            list.add(v2019_0_0_beta0_pre5)
            list.add(v2019_0_0_beta0_pre6)
            list.add(v2019_0_1)
            list.add(v2019_1_1_beta_1)
            list.add(v2019_1_1_beta_2a)
            list.add(v2019_1_1_beta_3_pre1)
            list.add(v2019_1_1_beta_3_p_2)
            list.add(v2019_1_1_beta_3_pre3)
            list.add(v2019_1_1_beta_3_pre4)
            list.add(v2019_1_1_beta_3_pre5)
            list.add(v2019_1_1_beta_3_pre6)
            list.add(v2019_1_1_beta_3_pre7)
            list.add(v2019_1_1_beta_3_pre8)
            list.add(v2019_1_1_beta_3_pre9)
            list.add(v2019_1_1_beta_3_pre10)
            list.add(v2019_1_1_beta_3)
            list.add(v2019_1_1_beta_3a)
            list.add(v2019_1_1_beta_4_pre1)
            list.add(v2019_1_1_beta_4_pre2)
            list.add(v2019_1_1_beta_4_pre4)
            list.add(v2019_1_1_beta_4)
            list.add(v2019_1_1_beta_4a)
            list.add(v2019_1_1_beta_4b)
            list.add(v2019_1_1_beta_4c)
            list.add(v2019_1_1_beta_5)
            list.add(v2019_1_1_beta_99)
            list.add(v2019_1_1_rc_1)
            list.add(v2019_1_1)
            list.add(v2019_1_2)
            list.add(v2019_2_1)
            list.add(v2019_3_1)
            list.add(v2019_3_2)
            list.add(v2019_4_1)
            list.add(v2020_1_1_beta_1)
            list.add(v2020_1_1_beta_2)
            list.add(v2020_1_1_beta_3)
            list.add(v2020_1_1_beta_3a)
            list.add(v2020_1_1)
            list.add(v2020_1_2)

            versionList = list.build()
        }
    }
    
    /*
        ===NOTES===
        Extracted from Gradle Plugin UI
            2018.06.21                 2018-06-21
            2019.0.0-alpha-1           2018-06-28
            2019.0.0-alpha-2           2018-07-01
            2019.0.0-alpha-3           2018-07-22
            2019.0.0-beta0-pre1        2018-09-28
            2019.0.0-beta0-pre3        2018-10-06
            2019.0.0-beta0-pre4        2018-10-15
            2019.0.0-beta0-pre5        2018-10-17
            2019.0.0-beta0-pre6        2018-10-18
            2019.1.1-beta-1            2018-10-19
            2019.1.1-beta-2a           2018-11-13
            2019.1.1-beta-3-p-2        2018-12-03
            2019.1.1-beta-3-pre3       2018-12-05
            2019.1.1-beta-3-pre4       2018-12-05
            2019.1.1-beta-3-pre5       2018-12-05
            2019.1.1-beta-3-pre6       2018-12-07
            2019.1.1-beta-3-pre7       2018-12-09
            2019.1.1-beta-3-pre8       2018-12-13
            2019.1.1-beta-3-pre9       2018-12-13
            2019.1.1-beta-3            2018-12-10
            2019.1.1-beta-3a           2018-12-10
            2019.1.1-beta-4-pre1       2018-12-14
            2019.1.1-beta-4-pre2       2018-12-15
            2019.1.1-beta-4-pre4       2018-12-15
            2019.1.1-beta-4            2018-12-16
            2019.1.1-beta-4a           2018-12-17
            2019.1.1-beta-4b           2019-12-27
            2019.1.1-beta-4c           2019-01-01
            2019.1.1-rc-1              2019-01-02
            2019.1.1                   2019-01-04
            2019.2.1                   2019-11-15
            2019.3.1                   2019-02-15
            2019.3.2                   2019-02-15
            2019.4.1                   2019-02-26
            2020.1.1-beta-1            2019-10-12
            2020.1.1-beta-2            2019-10-28
            2020.1.1-beta-3            2019-11-22
            2020.1.1-beta-3a           2019-11-23
    */
}