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

package net.javaru.iip.frc.util

import java.time.Duration
import java.time.LocalDate

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
 * Gets the current FRC, adjusted by the specified Duration. For example, with the default adjustment or 30 days,
 * the FRC Year, or build year, is considered to run from December 1 to November 31. Thus on December 3, 2019,
 * this would return a value of '2020'.
 */
fun getCurrentFrcYear(adjustment: Duration = Duration.ofDays(30)): Int = LocalDate.now().plus(adjustment).year

/**
 * Returns the current build year, return the next build year if it is within the duration until that year.
 * For example, if is December 20, 2019, and  14 days is used, this method will return 2020
 * since it is within 14 days until the end of the year.
 */
@Deprecated("Use getCurrentFrcYear(0 instead", replaceWith = ReplaceWith("getCurrentFrcYear(adjustment)"))
fun getCurrentBuildYear(adjustment: Duration = Duration.ofDays(30)): Int = getCurrentFrcYear(adjustment)





