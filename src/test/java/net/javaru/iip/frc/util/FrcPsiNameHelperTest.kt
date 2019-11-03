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

import net.javaru.iip.frc.FrcPluginGlobals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.test.assertEquals


internal class FrcPsiNameHelperTest
{
    val psiNameHelper = FrcPsiNameHelper(FrcPluginGlobals.DEFAULT_MIN_REQUIRED_JAVA_VERSION)

    @ParameterizedTest
    @MethodSource("identifierSource")
    fun testIsValidatorMethods(text: String, expectedIdentifier: Boolean, expectedQualifiedName: Boolean)
    {
        assertAll(
                {
                    assertEquals(expectedIdentifier, psiNameHelper.isIdentifier(text), "isIdentifier failed for $text. Expected: psiNameHelper.isIdentifier($text) == $expectedIdentifier")
                    assertEquals(expectedQualifiedName, psiNameHelper.isQualifiedName(text), "isQualifiedName failed for $text. Expected: psiNameHelper.isQualifiedName($text) == $expectedQualifiedName")
                }
                 )
        
    }
   

    @Suppress("unused")
    companion object
    {

        @JvmStatic
        fun identifierSource(): Stream<Arguments> = arrayListOf<Arguments>(
                Arguments.of("Foo", true, true),
                Arguments.of("main", true, true),
                Arguments.of("new", false, false),
                Arguments.of("com.example.Foo", false, true),
                Arguments.of("com.example..Foo", false, false),
                Arguments.of("com.example.new", false, false)
                                                                          ).stream()
    }

    @Test
    fun isKeyword()
    {
    }


    
}