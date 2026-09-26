/*
 * Copyright 2015-2026 the original author or authors.
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

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

internal class WpiLibProjectDetectionTest
{
    @ParameterizedTest
    @CsvSource(
        "C:/Robotics/MyRobot/.wpilib/wpilib_preferences.json, true",
        """C:\Robotics\MyRobot\.wpilib\wpilib_preferences.json, true""",
        "/home/me/robot/.wpilib, true",
        "/home/me/robot/.wpilib/other.json, true",
        "/home/me/robot/src/main/java/Robot.java, false",
        "/home/me/robot/vendordeps/REVLib.json, false",
        "/home/me/robot/not.wpilib/wpilib_preferences.json, false",
              )
    fun `detects paths that may change a project's WPILib status`(path: String, expected: Boolean)
    {
        assertEquals(expected, isWpiLibProjectLayoutPath(path))
    }
}
