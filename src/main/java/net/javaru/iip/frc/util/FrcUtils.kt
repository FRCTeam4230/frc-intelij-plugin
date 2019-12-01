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

import com.intellij.openapi.diagnostic.Logger
import java.time.LocalDate

private object FrcUtils

private val LOG = Logger.getInstance(FrcUtils::class.java)

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


fun getCurrentBuildYear(): Int = LocalDate.now().year

/**
 * Returns the current build year, return the next build year if it is within `{days}` until that year. 
 * For example, if is December 20, 2019, and the default 14 days is used, this method will return 2020 
 * since it is within 14 days until the end of the year.
 */
fun getAdjustedBuildYear(days: Long = 14):Int = LocalDate.now().plusDays(days).year

/**
 * Gets the next build year.
 */
fun getPendingBuildYear(): Int = getCurrentBuildYear() + 1




