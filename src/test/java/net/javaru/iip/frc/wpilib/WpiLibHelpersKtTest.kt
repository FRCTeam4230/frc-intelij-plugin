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

package net.javaru.iip.frc.wpilib

import net.javaru.iip.frc.assertPathsEqual
import net.javaru.iip.frc.setInTestModeSystemProperty
import net.javaru.iip.frc.wpilib.version.WpiLibVersionImpl
import org.apache.commons.lang3.SystemUtils
import org.junit.jupiter.api.Assumptions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import java.nio.file.Paths


internal class WpiLibHelpersKtTest
{

    @Suppress("unused")
    companion object
    {
        @BeforeAll
        fun initAll()
        {
            setInTestModeSystemProperty()
        }
    }

    @Test
    fun getWpiLibRootTest()
    {
        assumingThat(SystemUtils.IS_OS_WINDOWS) {
            assertAll({ assertPathsEqual(Paths.get("C:\\Users\\Public\\frc2019"), getWpiLibRootPath(2019)) },
                      { assertPathsEqual(Paths.get("C:\\Users\\Public\\wpilib\\2020"), getWpiLibRootPath(2020)) },
                      { assertPathsEqual(Paths.get("C:\\Users\\Public\\wpilib\\2021"), getWpiLibRootPath(2021)) },
                      { assertPathsEqual(Paths.get("C:\\Users\\Public\\wpilib\\2020"), getWpiLibRootPath(WpiLibVersionImpl.parse("2020.1.2"))) })
        }

        assumingThat(!SystemUtils.IS_OS_WINDOWS) {
            val home = SystemUtils.getUserHome()?.toPath()
            assumeTrue(home != null) {
                "Cannot test getWpiLibRootPath as user home was null"
            }
            assertAll({ assertPathsEqual(home?.resolve("frc2019"), getWpiLibRootPath(2019)) },
                      { assertPathsEqual(home?.resolve("wpilib/2020"), getWpiLibRootPath(2020)) },
                      { assertPathsEqual(home?.resolve("wpilib/2021"), getWpiLibRootPath(2021)) },
                      { assertPathsEqual(home?.resolve("wpilib/2020"), getWpiLibRootPath(WpiLibVersionImpl.parse("2020.1.2"))) })
        }
    }


    // Needs to be run manually since the local machine may not have wpilib installed
    //@Test
    //fun getWpiLibJdkReleaseJavaVersionTest()
    //{
    //    assertAll(
    //            { assertEquals("11.0.1", getWpiLibJdkReleaseJavaVersionString(2019)) },
    //            { assertEquals(com.intellij.util.lang.JavaVersion.tryParse("11.0.1"), getWpiLibJdkReleaseJavaVersion(2019)) },
    //            { assertEquals(11, getWpiLibJdkReleaseJavaFeatureVersion(2019)) }
    //             )
    //}
}