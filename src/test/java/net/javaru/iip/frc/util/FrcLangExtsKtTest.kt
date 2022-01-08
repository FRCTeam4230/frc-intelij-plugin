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

import com.sampullara.cli.Argument
import org.apache.commons.text.TextStringBuilder

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class FrcLangExtsKtTest
{

    @ParameterizedTest
    @ArgumentsSource(TextDataArgumentsProvider::class)
    @DisplayName("eolTo() method")
    fun eolTo(textData: TextData)
    {
        val actual = textData.text.eolTo()
        assertEquals(TextData.unixData.text, actual)

        assertAll("Testing ${textData.name}",
                  { assertEquals(TextData.unixData.text, textData.text.eolTo(), "Wrong result for DEFAULT for input of ${textData.name}")},
                  { assertEquals(TextData.macOldData.text, textData.text.eolTo(EOL.MAC_OLD), "Wrong result for MAC_OLD for input of ${textData.name}")},
                  { assertEquals(TextData.systemData.text, textData.text.eolTo(EOL.SYSTEM), "Wrong result for SYSTEM for input of ${textData.name}")},
                  { assertEquals(TextData.unixData.text, textData.text.eolTo(EOL.UNIX), "Wrong result for UNIX for input of ${textData.name}")},
                  { assertEquals(TextData.winData.text, textData.text.eolTo(EOL.WINDOWS), "Wrong result for WINDOWS for input of ${textData.name}")}
                 )

    }

    @ParameterizedTest
    @MethodSource
    fun testInsertBeforeLast(input: String, delimiter: Char, value: String, expected: String)
    {
        assertEquals(expected, input.insertBeforeLast(delimiter, value))
    }


    @Suppress("unused")
    companion object
    {
        @JvmStatic
        fun testInsertBeforeLast(): Stream<Arguments>
        {
            return Stream.of(
                Arguments.of("foo.txt", '.', "-baz", "foo-baz.txt"),
                Arguments.of("foo.bar.txt", '.', "-baz", "foo.bar-baz.txt"),
                Arguments.of("foo", '.', "-baz", "foo-baz"),
                Arguments.of(".foo", '.', "-baz", "-baz.foo"),
                Arguments.of("SomeFileName.zip", '.', "-baz", "SomeFileName-baz.zip"),
                Arguments.of("SomeFileName.JAR", '.', "-baz", "SomeFileName-baz.JAR"),
                Arguments.of("Some.File.Name.pdf", '.', "-baz", "Some.File.Name-baz.pdf"),
                            )
        }
    }
}

class TextDataArgumentsProvider : ArgumentsProvider
{
    override fun provideArguments(context: ExtensionContext): Stream<out Arguments>
    {
        return Stream.of(TextData.unixData,
                         TextData.winData,
                         TextData.macOldData,
                         TextData.systemData,
                         TextData.mixedData
                        ).map { Arguments.of(it) }
    }
}


data class TextData(val name: String, val text: String)
{
    companion object Instances
    {
        val unixData = TextData("unix", createText(EOL.UNIX))
        val winData = TextData("win", createText(EOL.WINDOWS))
        val macOldData = TextData("macOld", createText(EOL.MAC_OLD))
        val systemData = TextData("system", createText(EOL.SYSTEM))
        val mixedData = TextData("mixed", "Line 1\r\nLine 2\nLine 3\r\nLine 4\n\nLine 6\n\nLine 8\n\n\nLine 11")
    }

    override fun toString(): String
    {
        return name
    }
}

private fun createText(eol: EOL): String
{
    val sb = TextStringBuilder()
    sb.newLineText = eol.eol
    sb.appendln("Line 1")
    sb.appendln("Line 2")
    sb.appendln("Line 3")
    sb.appendln("Line 4")
    sb.appendNewLine()
    sb.appendln("Line 6")
    sb.appendNewLine()
    sb.appendln("Line 8")
    sb.appendNewLine()
    sb.appendNewLine()
    sb.append("Line 11") // create with no trailing new line

    return sb.toString()
}