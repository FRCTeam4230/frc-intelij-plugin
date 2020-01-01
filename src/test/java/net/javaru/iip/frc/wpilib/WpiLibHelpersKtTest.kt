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

package net.javaru.iip.frc.wpilib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll


internal class WpiLibHelpersKtTest
{


    @Test
    fun getWpiLibRootTest()
    {
        assertAll(
                { assertEquals("C:\\Users\\Public\\frc2019", getWpiLibRootPath(2019).toString()) },
                { assertEquals("C:\\Users\\Public\\wpilib\\2020", getWpiLibRootPath(2020).toString()) },
                { assertEquals("C:\\Users\\Public\\wpilib\\2021", getWpiLibRootPath(2021).toString()) }
                 )
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