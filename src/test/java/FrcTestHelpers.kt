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

@file:Suppress("unused")

package net.javaru.iip.frc

import net.javaru.iip.frc.FrcPluginGlobals.FRC_IN_UNIT_TEST_MODE_KEY
import org.apache.commons.io.FilenameUtils
import org.junit.jupiter.api.Assertions.*
import java.io.File
import java.nio.file.Path


fun setInTestModeSystemProperty()
{
    System.setProperty(FRC_IN_UNIT_TEST_MODE_KEY, true.toString());
}

fun assertPathsEqual(expected: File?, actual: File?, message: String?)
{
    assertEquals(expected?.toString()?.normalizePath(), actual?.toString()?.normalizePath(), message)
}

fun assertPathsEqual(expected: File?, actual: File?)
{
    assertEquals(expected?.toString()?.normalizePath(), actual?.toString()?.normalizePath())
}

fun assertPathsEqual(expected: Path?, actual: Path?, message: String?)
{
    assertEquals(expected?.toString()?.normalizePath(), actual?.toString()?.normalizePath(), message)
}

fun assertPathsEqual(expected: Path?, actual: Path?)
{
    assertEquals(expected?.toString()?.normalizePath(), actual?.toString()?.normalizePath())
}

fun assertPathsEqual(expected: String?, actual: String?, message: String?)
{
    assertEquals(expected?.normalizePath(), actual?.normalizePath(), message)
}

fun assertPathsEqual(expected: String?, actual: String?)
{
    assertEquals(expected?.normalizePath(), actual?.normalizePath())
}

private fun String.normalizePath(): String = FilenameUtils.separatorsToWindows(this)
fun String.separatorsToWindows(): String = FilenameUtils.separatorsToWindows(this)
fun String.separatorsToUnix(): String = FilenameUtils.separatorsToUnix(this)
fun String.separatorsToSystem(): String = FilenameUtils.separatorsToSystem(this)
