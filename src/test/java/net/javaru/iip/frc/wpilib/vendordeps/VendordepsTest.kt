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

@file:Suppress("LocalVariableName")

package net.javaru.iip.frc.wpilib.vendordeps

import com.asarkar.semver.NormalVersion
import com.asarkar.semver.SemVer
import net.javaru.iip.frc.getResourceStream
import net.javaru.iip.frc.util.createUri
import net.javaru.iip.frc.wpilib.vendordeps.Vendordeps.Companion.dmc60cRemappedUuid
import net.javaru.iip.frc.wpilib.vendordeps.Vendordeps.Companion.libCuRemappedUuid
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.*
import java.util.stream.Stream

internal class VendordepsTest
{
    @ParameterizedTest
    @MethodSource
    fun `correctly parses vendordeps json file`(filePath: String, expected: Vendordeps)
    {
        assertEquals(expected, Vendordeps.parse(getResourceStream(filePath)))
    }

    @ParameterizedTest
    @MethodSource
    fun `correctly sorts vendordeps`(expected: List<Vendordeps>)
    {
        val actual: MutableList<Vendordeps> = ArrayList(expected)
        actual.shuffle()
        actual.sort()
        assertAll ({
            assertEquals(expected, actual)
            assertArrayEquals(expected.toTypedArray(), actual.toTypedArray())
        })

    }

    @Suppress("unused", "HttpUrlsUsage")
    companion object
    {
        @JvmStatic
        fun `correctly parses vendordeps json file`(): Stream<Arguments>
        {
            return Stream.of(
                Arguments.of("vendordeps/navx_frc-3.1.409.json", navX_3_1_409),
                Arguments.of("vendordeps/Phoenix-5.18.2.json", phoenix_5_18_2),
                Arguments.of("vendordeps/Phoenix-5.19.4.json", phoenix_5_19_4),
                Arguments.of("vendordeps/WPILibNewCommands-2020.0.0.json", commandsNew_2020_0_0),
                Arguments.of("vendordeps/WPILibOldCommands.json", commandsOld_2020_0_0),
                /* Test unusual version numbering scheme */
                /* The ADIS lib changed is version format in 2020. in 2019 it used a dash "2019-r2" but in 2020 a dot "2020.r3" */
                Arguments.of("vendordeps/ADIS16448-2019-r3.json", adis16488_2019_r3),
                Arguments.of("vendordeps/ADIS16448-2020.r3.json", adis16488_2020_r3),
                /* Test lib using another libs UUIDs */
                Arguments.of("vendordeps/DMC60C-1.0.13.json", dmc60C_1_0_13),
                /* Test lib using a non-conformant UUID */
                Arguments.of("vendordeps/LibCu.json", libCu_2020_2_1),

                )
        }

        @JvmStatic
        fun `correctly sorts vendordeps`(): Stream<Arguments>
        {
            return Stream.of(
                Arguments.of(listOf(phoenix_5_17_6, phoenix_5_18_0, phoenix_5_18_1, phoenix_5_18_2, phoenix_5_19_4)),
                Arguments.of(listOf(navX_3_1_405, navX_3_1_409)),
                Arguments.of(listOf(adis16488_2019_r1, adis16488_2019_r2, adis16488_2019_r3, adis16488_2020_r1, adis16488_2020_r2, adis16488_2020_r3)),
                Arguments.of(listOf(commandsNew_2020_0_0, commandsNew_2020_1_2, commandsNew_2020_1_3, commandsNew_2021_1_1, commandsNew_2021_1_2, commandsNew_2021_2_1)),
                            )
        }

        private val navX_3_1_405 = Vendordeps(
            UUID.fromString("cb311d09-36e9-4143-a032-55bb2b94443b"),
            "KauaiLabs_navX_FRC",
            LibVersion.fromSemVer(SemVer(NormalVersion(3, 1, 405))),
            "navx_frc.json",
            createUri("https://www.kauailabs.com/dist/frc/2020/navx_frc.json"),
            listOf(createUri("https://repo1.maven.org/maven2/"))
                                             )

        private val navX_3_1_409 = Vendordeps(
            UUID.fromString("cb311d09-36e9-4143-a032-55bb2b94443b"),
            "KauaiLabs_navX_FRC",
            LibVersion.fromSemVer(SemVer(NormalVersion(3, 1, 409))),
            "navx_frc.json",
            createUri("https://www.kauailabs.com/dist/frc/2020/navx_frc.json"),
            listOf(createUri("https://repo1.maven.org/maven2/"))
                                             )

        private val phoenix_5_17_6 = Vendordeps(
            UUID.fromString("ab676553-b602-441f-a38d-f1296eff6537"),
            "CTRE-Phoenix",
            LibVersion.fromSemVer(SemVer(NormalVersion(5, 17, 6))),
            "Phoenix.json",
            createUri("http://devsite.ctr-electronics.com/maven/release/com/ctre/phoenix/Phoenix-latest.json"),
            listOf(createUri("http://devsite.ctr-electronics.com/maven/release/"))
                                               )

        private val phoenix_5_18_0 = Vendordeps(
            UUID.fromString("ab676553-b602-441f-a38d-f1296eff6537"),
            "CTRE-Phoenix",
            LibVersion.fromSemVer(SemVer(NormalVersion(5, 18, 0))),
            "Phoenix.json",
            createUri("http://devsite.ctr-electronics.com/maven/release/com/ctre/phoenix/Phoenix-latest.json"),
            listOf(createUri("http://devsite.ctr-electronics.com/maven/release/"))
                                               )

        private val phoenix_5_18_1 = Vendordeps(
            UUID.fromString("ab676553-b602-441f-a38d-f1296eff6537"),
            "CTRE-Phoenix",
            LibVersion.fromSemVer(SemVer(NormalVersion(5, 18, 1))),
            "Phoenix.json",
            createUri("http://devsite.ctr-electronics.com/maven/release/com/ctre/phoenix/Phoenix-latest.json"),
            listOf(createUri("http://devsite.ctr-electronics.com/maven/release/"))
                                               )

        private val phoenix_5_18_2 = Vendordeps(
            UUID.fromString("ab676553-b602-441f-a38d-f1296eff6537"),
            "CTRE-Phoenix",
            LibVersion.fromSemVer(SemVer(NormalVersion(5, 18, 2))),
            "Phoenix.json",
            createUri("http://devsite.ctr-electronics.com/maven/release/com/ctre/phoenix/Phoenix-latest.json"),
            listOf(createUri("http://devsite.ctr-electronics.com/maven/release/"))
                                               )

        private val phoenix_5_19_4 = Vendordeps(
            UUID.fromString("ab676553-b602-441f-a38d-f1296eff6537"),
            "CTRE-Phoenix",
            LibVersion.fromSemVer(SemVer(NormalVersion(5, 19, 4))),
            "Phoenix.json",
            createUri("https://devsite.ctr-electronics.com/maven/release/com/ctre/phoenix/Phoenix-latest.json"),
            listOf(createUri("https://devsite.ctr-electronics.com/maven/release/"))
                                               )


        // The ADIS lib changed is version format in 2020. in 2019 it used a dash "2019-r2" but in 2020 a dot "2020.r3"

        private val adis16488_2019_r1 = Vendordeps(
            UUID.fromString("38c21ab6-aa8b-44bc-b844-8086c77f09ec"),
            "ADIS16448-IMU",
            LibVersion.parse("2019-r1"),
            "ADIS16448.json",
            createUri("http://maven.highcurrent.io/vendordeps/ADIS16448.json"),
            listOf(createUri("http://maven.highcurrent.io/maven"))
                                                  )

        private val adis16488_2019_r2 = Vendordeps(
            UUID.fromString("38c21ab6-aa8b-44bc-b844-8086c77f09ec"),
            "ADIS16448-IMU",
            LibVersion.parse("2019-r2"),
            "ADIS16448.json",
            createUri("http://maven.highcurrent.io/vendordeps/ADIS16448.json"),
            listOf(createUri("http://maven.highcurrent.io/maven"))
                                                  )

        private val adis16488_2019_r3 = Vendordeps(
            UUID.fromString("38c21ab6-aa8b-44bc-b844-8086c77f09ec"),
            "ADIS16448-IMU",
            LibVersion.parse("2019-r3"),
            "ADIS16448.json",
            createUri("http://maven.highcurrent.io/vendordeps/ADIS16448.json"),
            listOf(createUri("http://maven.highcurrent.io/maven"))
                                                  )

        private val adis16488_2020_r1 = Vendordeps(
            UUID.fromString("38c21ab6-aa8b-44bc-b844-8086c77f09ec"),
            "ADIS16448-IMU",
            LibVersion.parse("2020.r1"),
            "ADIS16448.json",
            createUri("http://maven.highcurrent.io/vendordeps/ADIS16448.json"),
            listOf(createUri("http://maven.highcurrent.io/maven"))
                                                  )

        private val adis16488_2020_r2 = Vendordeps(
            UUID.fromString("38c21ab6-aa8b-44bc-b844-8086c77f09ec"),
            "ADIS16448-IMU",
            LibVersion.parse("2020.r2"),
            "ADIS16448.json",
            createUri("http://maven.highcurrent.io/vendordeps/ADIS16448.json"),
            listOf(createUri("http://maven.highcurrent.io/maven"))
                                                  )

        private val adis16488_2020_r3 = Vendordeps(
            UUID.fromString("38c21ab6-aa8b-44bc-b844-8086c77f09ec"),
            "ADIS16448-IMU",
            LibVersion.parse("2020.r3"),
            "ADIS16448.json",
            createUri("http://maven.highcurrent.io/vendordeps/ADIS16448.json"),
            listOf(createUri("http://maven.highcurrent.io/maven"))
                                                  )

        private val commandsNew_2020_0_0 = Vendordeps(
            UUID.fromString("111e20f7-815e-48f8-9dd6-e675ce75b266"),
            "WPILib-New-Commands",
            LibVersion.fromSemVer(SemVer(NormalVersion(2020, 0, 0))),
            "WPILibNewCommands.json",
            null,
            emptyList()
                                                     )

        private val commandsNew_2020_1_2 = Vendordeps(
            UUID.fromString("111e20f7-815e-48f8-9dd6-e675ce75b266"),
            "WPILib-New-Commands",
            LibVersion.fromSemVer(SemVer(NormalVersion(2020, 1, 2))),
            "WPILibNewCommands.json",
            null,
            emptyList())
        
        private val commandsNew_2020_1_3 = Vendordeps(
            UUID.fromString("111e20f7-815e-48f8-9dd6-e675ce75b266"),
            "WPILib-New-Commands",
            LibVersion.fromSemVer(SemVer(NormalVersion(2020, 1, 3))),
            "WPILibNewCommands.json",
            null,
            emptyList())

        private val commandsNew_2021_1_1 = Vendordeps(
            UUID.fromString("111e20f7-815e-48f8-9dd6-e675ce75b266"),
            "WPILib-New-Commands",
            LibVersion.fromSemVer(SemVer(NormalVersion(2021, 1, 1))),
            "WPILibNewCommands.json",
            null,
            emptyList())

        private val commandsNew_2021_1_2 = Vendordeps(
            UUID.fromString("111e20f7-815e-48f8-9dd6-e675ce75b266"),
            "WPILib-New-Commands",
            LibVersion.fromSemVer(SemVer(NormalVersion(2021, 1, 2))),
            "WPILibNewCommands.json",
            null,
            emptyList())

        private val commandsNew_2021_2_1 = Vendordeps(
            UUID.fromString("111e20f7-815e-48f8-9dd6-e675ce75b266"),
            "WPILib-New-Commands",
            LibVersion.fromSemVer(SemVer(NormalVersion(2021, 2, 1))),
            "WPILibNewCommands.json",
            null,
            emptyList())

        private val commandsOld_2020_0_0 = Vendordeps(
            UUID.fromString("b066afc2-5c18-43c4-b758-43381fcb275e"),
            "WPILib-Old-Commands",
            LibVersion.fromSemVer(SemVer(NormalVersion(2020, 0, 0))),
            "WPILibOldCommands.json",
            null,
            emptyList())

        /**
         * A problematic file as it uses the navX UUID as its UUID.
         * See https://github.com/Digilent/dmc60c-frc-api/issues/13
         */
        private val dmc60C_1_0_13 = Vendordeps(
            dmc60cRemappedUuid,
            "Digilent-DMC60C",
            LibVersion.fromSemVer(SemVer(NormalVersion(1, 0, 13))),
            "DMC60C.json",
            createUri("https://s3-us-west-2.amazonaws.com/digilent/Software/DMC60C/test/DMC60C.json"),
            listOf(createUri("https://s3-us-west-2.amazonaws.com/digilent/Software/DMC60C/test")))
        
        /**
         * A problematic file as it uses a non-conformant UUID.
         * See https://github.com/Coppersource/LibCu/issues/4
         */
        private val libCu_2020_2_1 = Vendordeps(
            libCuRemappedUuid,
            "LibCu",
            LibVersion.fromSemVer(SemVer(NormalVersion(2020, 2, 1))),
            "LibCu.json",
            createUri("https://copperforge.cc/files/dev/vendordeps/LibCu-latest.json"),
            listOf(createUri("https://copperforge.cc/files/dev/maven")))
        
    }
}