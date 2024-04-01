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

package net.javaru.iip.frc.util

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.wpilib.services.WpiLibVersionService
import net.javaru.iip.frc.wpilib.getConfiguredProjectYearJustYear
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private object FrcUtils

//private val LOG = logger<FrcUtils>()

data class TitleMessagePair(val title:String, val message: String)

/**
 * Can execute code quietly such that no exception is thrown. Some examples to call a method:
 *
 *  * <tt>FrcUtils.executeQuietly(() -> socket.disconnect());</tt>
 *  * <tt>FrcUtils.executeQuietly(socket::disconnect);</tt>
 *
 * @param callable the callable to run
 */
fun executeQuietly(callable: Runnable)
{
    try
    {
        callable.run()
    }
    catch (ignore: Throwable)
    {
    }
}

/**
 * Gets the current FRC, adjusted by the specified number of days. For example, with the default adjustment or 30 days,
 * the FRC Year, or build year, is considered to run from December 1 to November 31. Thus, on December 3, 2019,
 * this would return a value of '2020'.
 */
fun getCurrentFrcSeason(daysAdjustment: Int = 30): Int = LocalDate.now().plus(daysAdjustment.toLong(), ChronoUnit.DAYS).year

/**
 * Determines the FRC Year using the following sources in order until a non-null value is found:
 * 1. The frc year of the attached WpiLib (see [WpiLibVersionService.frcYear])
 * 2. The `projectYear` as specified in `wpilib_preferences.json` (see [getConfiguredProjectYearJustYear] in `WpiLibPreferences.kt`)
 * 3. The current Frc Year (based on the current date) as returned by [getCurrentFrcSeason]
 *
 *  **Due to the third step, the year returned may not be accurate. But that would indicate a massive misconfiguration of the project.**
 */
fun Project.getFrcYearNonNull() : Int
{
    return this.runReadActionInSmartModeWithReturn {
        this.service<WpiLibVersionService>().frcYear ?: this.getConfiguredProjectYearJustYear() ?: getCurrentFrcSeason()
    }
}



