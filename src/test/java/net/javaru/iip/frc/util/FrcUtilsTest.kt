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

package net.javaru.iip.frc.util

import com.intellij.util.lang.JavaVersion
import net.javaru.iip.frc.i18n.FrcMessageKey
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll


internal class FrcUtilsTest
{

    @Test
    fun createTitleMessagePair()
    {
        val requiredMinimumJavaVersion: JavaVersion = JavaVersion.parse("11.0.1")
        val configuredJavaVersion: JavaVersion = JavaVersion.parse("1.8.0_221")

        println("requiredMinimumJavaVersion = '${requiredMinimumJavaVersion}'")
        println("configuredJavaVersion = '${configuredJavaVersion}'")
        assertAll(
                { 
                    assertEquals(TitleMessagePair("Invalid JDK Version", "<html><b style=\"font-size:x-large;\">Invalid JDK Version</b><br>The template you've selected requires a version 11.0.1 (feature level 11) or better JDK as the SDK, but the SDK version selected is 1.8.0_221 (feature level 8).</html>"),
                                 createInvalidJdkTitleMessagePair(requiredMinimumJavaVersion, configuredJavaVersion, null)) 
                },

                {
                    assertEquals(TitleMessagePair("Invalid JDK Version", "<html><b style=\"font-size:x-large;\">Invalid JDK Version</b><br>The template you've selected requires a version 11.0.1 (feature level 11) or better JDK as the SDK, but the SDK version selected is 1.8.0_221 (feature level 8). My test message with parameter 0 of «foo» and parameter 1 of «bar».</html>"),
                                 createInvalidJdkTitleMessagePair(requiredMinimumJavaVersion, configuredJavaVersion, FrcMessageKey.of("frc.unitTest.2", "foo", "bar")))
                }
                 )
        
    }
}