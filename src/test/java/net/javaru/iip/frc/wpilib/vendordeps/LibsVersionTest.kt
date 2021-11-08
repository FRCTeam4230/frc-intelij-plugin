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

package net.javaru.iip.frc.net.javaru.iip.frc.wpilib.vendordeps

import com.asarkar.semver.NormalVersion
import com.asarkar.semver.SemVer
import net.javaru.iip.frc.wpilib.vendordeps.LibVersion
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals



internal class LibsVersionTest
{
    @ParameterizedTest
    @MethodSource
    fun `correctly parse lib versions`(text: String, expected: LibVersion)
    {
        assertEquals(expected, LibVersion.parse(text))
    }

    companion object
    {
        @Suppress("unused")
        @JvmStatic
        fun `correctly parse lib versions`(): Stream<Arguments>
        {
            return Stream.of(
                Arguments.of("1", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 0, 0)), "1")),
                Arguments.of("1.0", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 0, 0)), "1.0")),
                Arguments.of("1.0.0", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 0, 0)), "1.0.0")),
                Arguments.of("v1", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 0, 0)), "v1")),
                Arguments.of("v1.0", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 0, 0)), "v1.0")),
                Arguments.of("v1.0.0", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 0, 0)), "v1.0.0")),
                Arguments.of("1.2.0", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 2, 0)), "1.2.0")),
                Arguments.of("v1.2.0", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 2, 0)), "v1.2.0")),
                Arguments.of("v1.2.3", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 2, 3)), "v1.2.3")),
                Arguments.of("1.20.3", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 20, 3)), "1.20.3")),
                Arguments.of("1.2.30", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 2, 30)), "1.2.30")),
                Arguments.of("1.20.30", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 20, 30)), "1.20.30")),
                Arguments.of("10.20.30", LibVersion.fromSemVerAltText(SemVer(NormalVersion(10, 20, 30)), "10.20.30")),
                Arguments.of("01.2.3", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 2, 3)), "01.2.3")),
                Arguments.of("1.02.3", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 2, 3)), "1.02.3")),
                Arguments.of("1.2.03", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 2, 3)), "1.2.03")),
                Arguments.of("01.02.03", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 2, 3)), "01.02.03")),
                Arguments.of("001.002.003", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 2, 3)), "001.002.003")),
                Arguments.of("0.2.3", LibVersion.fromSemVerAltText(SemVer(NormalVersion(0, 2, 3)), "0.2.3")),
                Arguments.of("0.0.3", LibVersion.fromSemVerAltText(SemVer(NormalVersion(0, 0, 3)), "0.0.3")),
                Arguments.of("0.0.30", LibVersion.fromSemVerAltText(SemVer(NormalVersion(0, 0, 30)), "0.0.30")),
                Arguments.of("v10.20.30", LibVersion.fromSemVerAltText(SemVer(NormalVersion(10, 20, 30)), "v10.20.30")),
                Arguments.of("v01.2.3", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 2, 3)), "v01.2.3")),
                Arguments.of("v1.02.3", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 2, 3)), "v1.02.3")),
                Arguments.of("v1.2.03", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 2, 3)), "v1.2.03")),
                Arguments.of("v01.02.03", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 2, 3)), "v01.02.03")),
                Arguments.of("v001.002.003", LibVersion.fromSemVerAltText(SemVer(NormalVersion(1, 2, 3)), "v001.002.003")),
                Arguments.of("v0.2.3", LibVersion.fromSemVerAltText(SemVer(NormalVersion(0, 2, 3)), "v0.2.3")),
                Arguments.of("v0.0.3", LibVersion.fromSemVerAltText(SemVer(NormalVersion(0, 0, 3)), "v0.0.3")),
                Arguments.of("v0.0.30", LibVersion.fromSemVerAltText(SemVer(NormalVersion(0, 0, 30)), "v0.0.30")),

                            )
        }
    }
}