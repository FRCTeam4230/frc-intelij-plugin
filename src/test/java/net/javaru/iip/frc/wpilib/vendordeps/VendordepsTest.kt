/*
 * Copyright 2015-2022 the original author or authors.
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
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
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
        Vendordeps.parse(getResourceStream(filePath)).onSuccess {
            assertEquals(expected, it)
        }.onFailure {
            fail("Parsing failed", it)
        }
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

    @ParameterizedTest
    @MethodSource
    fun `can create InvalidVendordepsProjectFile from valid files`(filePath: String, vendordeps: Vendordeps)
    {
        val text = getResourceStream(filePath).use { 
            it.bufferedReader().readText()
        }
        val data = InvalidVendordepsProjectFile.extractVendordepsData(text)
        assertAll(
            { assertEquals(vendordeps.uuid.toString(), data.libUuidString, "Wrong value for libUuidString") },
            { assertEquals(vendordeps.uuid, data.libUuid, "Wrong value for libUuid") },
            { assertEquals(vendordeps.fileName, data.libFileName, "Wrong value for libFileName") },
            { assertEquals(vendordeps.name, data.libName, "Wrong value for libName") },
            { assertEquals(vendordeps.version.asText, data.libVersionString, "Wrong value for libVersionString") },
            { assertEquals(vendordeps.version, data.libVersion, "Wrong value for libVersion") },
            { 
                if (vendordeps.frcYear == null)
                {
                    assertAll(
                        { assertNull(data.libFrcYearString) },
                        { assertNull(data.libFrcYear) },
                             )
                    
                }
                else
                {
                   assertAll(
                       { assertEquals(vendordeps.frcYear.toString(), data.libFrcYearString, "Wrong value for libFrcYearString")},
                       { assertEquals(vendordeps.frcYear, data.libFrcYear, "Wrong value for libFrcYear") },
                       ) 
                }
             },
            {
                if (vendordeps.jsonUrl == null)
                {
                    assertAll(
                        { assertNull(data.libJsonUrlString) },
                        { assertNull(data.libJsonUrl) },
                             )
                }
                else
                {
                    assertAll(
                        { assertEquals(vendordeps.jsonUrl.toString(), data.libJsonUrlString, "Wrong value for libJsonUrlString") },
                        { assertEquals(vendordeps.jsonUrl, data.libJsonUrl, "Wrong value for libJsonUrl") },
                        )
                }
            },
            
            )
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
                Arguments.of("vendordeps/Phoenix-5.20.2.json", phoenix_5_20_2),
                Arguments.of("vendordeps/Phoenix-5.21.2.json", phoenix_5_21_2),
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
                /* Test lib that has extra space in the quoted UUID */
                Arguments.of("vendordeps/PhotonLib-json-1.0.json", photonLib_1_0),
                Arguments.of("vendordeps/NeedsTrimming.json", needsTrimming),
                Arguments.of("vendordeps/playingwithfusion2021.json", playingWithFusion_2021_3_22),
                )
        }

        @JvmStatic
        fun `can create InvalidVendordepsProjectFile from valid files`(): Stream<Arguments>
        {
            return Stream.of(
                Arguments.of("vendordeps/navx_frc-3.1.409.json", navX_3_1_409),
                Arguments.of("vendordeps/Phoenix-5.18.2.json", phoenix_5_18_2),
                Arguments.of("vendordeps/Phoenix-5.19.4.json", phoenix_5_19_4),
                Arguments.of("vendordeps/Phoenix-5.20.2.json", phoenix_5_20_2),
                Arguments.of("vendordeps/Phoenix-5.21.2.json", phoenix_5_21_2),
                Arguments.of("vendordeps/WPILibNewCommands-2020.0.0.json", commandsNew_2020_0_0),
                Arguments.of("vendordeps/WPILibOldCommands.json", commandsOld_2020_0_0),
                /* Test unusual version numbering scheme */
                /* The ADIS lib changed is version format in 2020. in 2019 it used a dash "2019-r2" but in 2020 a dot "2020.r3" */
                Arguments.of("vendordeps/ADIS16448-2019-r3.json", adis16488_2019_r3),
                Arguments.of("vendordeps/ADIS16448-2020.r3.json", adis16488_2020_r3),
                /* Test lib that has extra space in the quoted UUID */
                Arguments.of("vendordeps/PhotonLib-json-1.0.json", photonLib_1_0),
                Arguments.of("vendordeps/NeedsTrimming.json", needsTrimming),
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
            uuid = UUID.fromString("cb311d09-36e9-4143-a032-55bb2b94443b"),
            name = "KauaiLabs_navX_FRC",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(3, 1, 405))),
            frcYear = null,
            fileName = "navx_frc.json",
            jsonUrl = createUri("https://www.kauailabs.com/dist/frc/2020/navx_frc.json"),
            mavenUrls = listOf(createUri("https://repo1.maven.org/maven2/"))
                                             )

        private val navX_3_1_409 = Vendordeps(
            uuid = UUID.fromString("cb311d09-36e9-4143-a032-55bb2b94443b"),
            name = "KauaiLabs_navX_FRC",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(3, 1, 409))),
            frcYear = null,
            fileName = "navx_frc.json",
            jsonUrl = createUri("https://www.kauailabs.com/dist/frc/2020/navx_frc.json"),
            mavenUrls = listOf(createUri("https://repo1.maven.org/maven2/"))
                                             )

        private val phoenix_5_17_6 = Vendordeps(
            uuid = UUID.fromString("ab676553-b602-441f-a38d-f1296eff6537"),
            name = "CTRE-Phoenix",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(5, 17, 6))),
            frcYear = null,
            fileName = "Phoenix.json",
            jsonUrl = createUri("http://devsite.ctr-electronics.com/maven/release/com/ctre/phoenix/Phoenix-latest.json"),
            mavenUrls = listOf(createUri("http://devsite.ctr-electronics.com/maven/release/"))
                                               )

        private val phoenix_5_18_0 = Vendordeps(
            uuid = UUID.fromString("ab676553-b602-441f-a38d-f1296eff6537"),
            name = "CTRE-Phoenix",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(5, 18, 0))),
            frcYear = null,
            fileName = "Phoenix.json",
            jsonUrl = createUri("http://devsite.ctr-electronics.com/maven/release/com/ctre/phoenix/Phoenix-latest.json"),
            mavenUrls = listOf(createUri("http://devsite.ctr-electronics.com/maven/release/"))
                                               )

        private val phoenix_5_18_1 = Vendordeps(
            uuid = UUID.fromString("ab676553-b602-441f-a38d-f1296eff6537"),
            name = "CTRE-Phoenix",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(5, 18, 1))),
            frcYear = null,
            fileName = "Phoenix.json",
            jsonUrl = createUri("http://devsite.ctr-electronics.com/maven/release/com/ctre/phoenix/Phoenix-latest.json"),
            mavenUrls = listOf(createUri("http://devsite.ctr-electronics.com/maven/release/"))
                                               )

        private val phoenix_5_18_2 = Vendordeps(
            uuid = UUID.fromString("ab676553-b602-441f-a38d-f1296eff6537"),
            name = "CTRE-Phoenix",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(5, 18, 2))),
            frcYear = null,
            fileName = "Phoenix.json",
            jsonUrl = createUri("http://devsite.ctr-electronics.com/maven/release/com/ctre/phoenix/Phoenix-latest.json"),
            mavenUrls = listOf(createUri("http://devsite.ctr-electronics.com/maven/release/"))
                                               )

        private val phoenix_5_19_4 = Vendordeps(
            uuid = UUID.fromString("ab676553-b602-441f-a38d-f1296eff6537"),
            name = "CTRE-Phoenix",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(5, 19, 4))),
            frcYear = null,
            fileName = "Phoenix.json",
            jsonUrl = createUri("https://devsite.ctr-electronics.com/maven/release/com/ctre/phoenix/Phoenix-latest.json"),
            mavenUrls = listOf(createUri("https://devsite.ctr-electronics.com/maven/release/"))
                                               )

        // NOTE: The phoenix 'latest' URL changed for 2022, i.e. v5.20.2 (it was broken in v5.20.1)
        //       https://store.ctr-electronics.com/content/release-notes/RELEASE_NOTES.txt

        private val phoenix_5_20_2 = Vendordeps(
            uuid = UUID.fromString("ab676553-b602-441f-a38d-f1296eff6537"),
            name = "CTRE-Phoenix",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(5, 20, 2))),
            frcYear = 2022,
            fileName = "Phoenix.json",
            jsonUrl = createUri("https://maven.ctr-electronics.com/release/com/ctre/phoenix/Phoenix-frc2022-latest.json"),
            mavenUrls = listOf(createUri("https://maven.ctr-electronics.com/release/"))
                                               )

        private val phoenix_5_21_2 = Vendordeps(
            uuid = UUID.fromString("ab676553-b602-441f-a38d-f1296eff6537"),
            name = "CTRE-Phoenix",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(5, 21, 2))),
            frcYear = 2022,
            fileName = "Phoenix.json",
            jsonUrl = createUri("https://maven.ctr-electronics.com/release/com/ctre/phoenix/Phoenix-frc2022-latest.json"),
            mavenUrls = listOf(createUri("https://maven.ctr-electronics.com/release/"))
                                               )


        /** This has a problem in that the UUI String has an extra space in it after the UUID and before the closing quote.
         *  It is also on of the few to have multiple maven URLs*/
        @Suppress("SpellCheckingInspection")
        private val photonLib_1_0 = Vendordeps(
            uuid = UUID.fromString("515fe07e-bfc6-11fa-b3de-0242ac130004"),
            name = "photonlib",
            version = LibVersion.fromSemVerAltText(SemVer(NormalVersion(2021, 1, 7)), "v2021.1.7"),
            frcYear = null,
            fileName = "photonlib.json",
            jsonUrl = createUri("https://maven.photonvision.org/repository/internal/org/photonvision/PhotonLib-json/1.0/PhotonLib-json-1.0.json"),
            mavenUrls = listOf(
                createUri("https://maven.photonvision.org/repository/internal"),
                createUri("https://maven.photonvision.org/repository/snapshots"),
                              )
                                              )

        @Suppress("SpellCheckingInspection")
        private val needsTrimming = Vendordeps(
            uuid = UUID.fromString("03880f9b-e0e6-4cd3-baa5-4039fa38316b"),
            name = "needsTrimming",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(1, 3, 2))),
            frcYear = null,
            fileName = "needsTrimming.json",
            jsonUrl = createUri("https://maven.example.org/repository/internal/org/example/needsTrimming-json/1.0/needsTrimming-json-1.0.json"),
            mavenUrls = listOf(
                createUri("https://maven.example.org/repository/internal"),
                createUri("https://maven.example.org/repository/snapshots"),
                              )
                                              )

        private val playingWithFusion_2021_3_22 = Vendordeps(
            uuid = UUID.fromString("14b8ad04-24df-11ea-978f-2e728ce88125"),
            name = "PlayingWithFusion",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(2021, 3, 22))),
            frcYear = null,
            fileName = "playingwithfusion2021.json",
            jsonUrl = createUri("https://www.playingwithfusion.com/frc/playingwithfusion2021.json"),
            mavenUrls = listOf(createUri("https://www.playingwithfusion.com/frc/maven/"))
                                                            )


        // The ADIS lib changed is version format in 2020. in 2019 it used a dash "2019-r2" but in 2020 a dot "2020.r3"

        private val adis16488_2019_r1 = Vendordeps(
            uuid = UUID.fromString("38c21ab6-aa8b-44bc-b844-8086c77f09ec"),
            name = "ADIS16448-IMU",
            version = LibVersion.parse("2019-r1"),
            frcYear = null,
            fileName = "ADIS16448.json",
            jsonUrl = createUri("http://maven.highcurrent.io/vendordeps/ADIS16448.json"),
            mavenUrls = listOf(createUri("http://maven.highcurrent.io/maven"))
                                                  )

        private val adis16488_2019_r2 = Vendordeps(
            uuid = UUID.fromString("38c21ab6-aa8b-44bc-b844-8086c77f09ec"),
            name = "ADIS16448-IMU",
            version = LibVersion.parse("2019-r2"),
            frcYear = null,
            fileName = "ADIS16448.json",
            jsonUrl = createUri("http://maven.highcurrent.io/vendordeps/ADIS16448.json"),
            mavenUrls = listOf(createUri("http://maven.highcurrent.io/maven"))
                                                  )

        private val adis16488_2019_r3 = Vendordeps(
            uuid = UUID.fromString("38c21ab6-aa8b-44bc-b844-8086c77f09ec"),
            name = "ADIS16448-IMU",
            version = LibVersion.parse("2019-r3"),
            frcYear = null,
            fileName = "ADIS16448.json",
            jsonUrl = createUri("http://maven.highcurrent.io/vendordeps/ADIS16448.json"),
            mavenUrls = listOf(createUri("http://maven.highcurrent.io/maven"))
                                                  )

        private val adis16488_2020_r1 = Vendordeps(
            uuid = UUID.fromString("38c21ab6-aa8b-44bc-b844-8086c77f09ec"),
            name = "ADIS16448-IMU",
            version = LibVersion.parse("2020.r1"),
            frcYear = null,
            fileName = "ADIS16448.json",
            jsonUrl = createUri("http://maven.highcurrent.io/vendordeps/ADIS16448.json"),
            mavenUrls = listOf(createUri("http://maven.highcurrent.io/maven"))
                                                  )

        private val adis16488_2020_r2 = Vendordeps(
            uuid = UUID.fromString("38c21ab6-aa8b-44bc-b844-8086c77f09ec"),
            name = "ADIS16448-IMU",
            version = LibVersion.parse("2020.r2"),
            frcYear = null,
            fileName = "ADIS16448.json",
            jsonUrl = createUri("http://maven.highcurrent.io/vendordeps/ADIS16448.json"),
            mavenUrls = listOf(createUri("http://maven.highcurrent.io/maven"))
                                                  )

        private val adis16488_2020_r3 = Vendordeps(
            uuid = UUID.fromString("38c21ab6-aa8b-44bc-b844-8086c77f09ec"),
            name = "ADIS16448-IMU",
            version = LibVersion.parse("2020.r3"),
            frcYear = null,
            fileName = "ADIS16448.json",
            jsonUrl = createUri("http://maven.highcurrent.io/vendordeps/ADIS16448.json"),
            mavenUrls = listOf(createUri("http://maven.highcurrent.io/maven"))
                                                  )

        private val commandsNew_2020_0_0 = Vendordeps(
            uuid = UUID.fromString("111e20f7-815e-48f8-9dd6-e675ce75b266"),
            name = "WPILib-New-Commands",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(2020, 0, 0))),
            frcYear = null,
            fileName = "WPILibNewCommands.json",
            jsonUrl = null,
            mavenUrls = emptyList()
                                                     )

        private val commandsNew_2020_1_2 = Vendordeps(
            uuid = UUID.fromString("111e20f7-815e-48f8-9dd6-e675ce75b266"),
            name = "WPILib-New-Commands",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(2020, 1, 2))),
            frcYear = null,
            fileName = "WPILibNewCommands.json",
            jsonUrl = null,
            mavenUrls = emptyList()
                                                     )
        
        private val commandsNew_2020_1_3 = Vendordeps(
            uuid = UUID.fromString("111e20f7-815e-48f8-9dd6-e675ce75b266"),
            name = "WPILib-New-Commands",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(2020, 1, 3))),
            frcYear = null,
            fileName = "WPILibNewCommands.json",
            jsonUrl = null,
            mavenUrls = emptyList()
                                                     )

        private val commandsNew_2021_1_1 = Vendordeps(
            uuid = UUID.fromString("111e20f7-815e-48f8-9dd6-e675ce75b266"),
            name = "WPILib-New-Commands",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(2021, 1, 1))),
            frcYear = null,
            fileName = "WPILibNewCommands.json",
            jsonUrl = null,
            mavenUrls = emptyList()
                                                     )

        private val commandsNew_2021_1_2 = Vendordeps(
            uuid = UUID.fromString("111e20f7-815e-48f8-9dd6-e675ce75b266"),
            name = "WPILib-New-Commands",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(2021, 1, 2))),
            frcYear = null,
            fileName = "WPILibNewCommands.json",
            jsonUrl = null,
            mavenUrls = emptyList()
                                                     )

        private val commandsNew_2021_2_1 = Vendordeps(
            uuid = UUID.fromString("111e20f7-815e-48f8-9dd6-e675ce75b266"),
            name = "WPILib-New-Commands",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(2021, 2, 1))),
            frcYear = null,
            fileName = "WPILibNewCommands.json",
            jsonUrl = null,
            mavenUrls = emptyList()
                                                     )

        private val commandsOld_2020_0_0 = Vendordeps(
            uuid = UUID.fromString("b066afc2-5c18-43c4-b758-43381fcb275e"),
            name = "WPILib-Old-Commands",
            version = LibVersion.fromSemVer(SemVer(NormalVersion(2020, 0, 0))),
            frcYear = null,
            fileName = "WPILibOldCommands.json",
            jsonUrl = null,
            mavenUrls = emptyList()
                                                     )

        /**
         * A problematic file as it uses the navX UUID as its UUID.
         * See https://github.com/Digilent/dmc60c-frc-api/issues/13
         */
        private val dmc60C_1_0_13 = Vendordeps(
            dmc60cRemappedUuid,
            "Digilent-DMC60C",
            LibVersion.fromSemVer(SemVer(NormalVersion(1, 0, 13))),
            null,
            "DMC60C.json",
            createUri("https://s3-us-west-2.amazonaws.com/digilent/Software/DMC60C/test/DMC60C.json"),
            listOf(createUri("https://s3-us-west-2.amazonaws.com/digilent/Software/DMC60C/test"))
                                              )
        
        /**
         * A problematic file as it uses a non-conformant UUID.
         * See https://github.com/Coppersource/LibCu/issues/4
         */
        private val libCu_2020_2_1 = Vendordeps(
            libCuRemappedUuid,
            "LibCu",
            LibVersion.fromSemVer(SemVer(NormalVersion(2020, 2, 1))),
            null,
            "LibCu.json",
            createUri("https://copperforge.cc/files/dev/vendordeps/LibCu-latest.json"),
            listOf(createUri("https://copperforge.cc/files/dev/maven"))
                                               )
        
    }
}