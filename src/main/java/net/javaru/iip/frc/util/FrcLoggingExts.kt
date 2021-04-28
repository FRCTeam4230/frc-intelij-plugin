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

import net.javaru.iip.frc.FrcPluginGlobals
import net.javaru.iip.frc.FrcPluginGlobals.IS_IN_FRC_UNIT_TEST_MODE

/**
 * If `IS_IN_FRC_UNIT_TEST_MODE` is true, the supplied message is always logged at the DEBUG level (assuming DEBUG is enabled for the logger).
 * If we are not `IS_IN_FRC_UNIT_TEST_MODE`, the message is logged at the WARN level, unless we are `IS_IN_FRC_INTERNAL_MODE` in which case
 * it is logged at the ERROR level in order to call the developer's attention to the issue.
 */
@JvmOverloads
fun com.intellij.openapi.diagnostic.Logger.asserted(t: Throwable? = null, message: ()-> String)
{
    if (IS_IN_FRC_UNIT_TEST_MODE && this.isDebugEnabled) this.debug(message.invoke(), t) else if (shouldAssert) this.error(message.invoke(), t) else this.warn(message.invoke(), t)
}

/**
 * If `IS_IN_FRC_UNIT_TEST_MODE` is true, the supplied message is always logged at the DEBUG level (assuming DEBUG is enabled for the logger).
 * If we are not `IS_IN_FRC_UNIT_TEST_MODE`, the message is logged at the WARN level, unless we are `IS_IN_FRC_INTERNAL_MODE` in which case
 * it is logged at the ERROR level in order to call the developer's attention to the issue.
 */
@JvmOverloads
fun com.intellij.openapi.diagnostic.Logger.asserted(message: String, t: Throwable? = null)
{
    if (IS_IN_FRC_UNIT_TEST_MODE) this.debug(message, t) else if (shouldAssert) this.error(message, t) else this.warn(message, t)
}

val shouldAssert: Boolean by lazy {
    if (IS_IN_FRC_UNIT_TEST_MODE)
        false
    else
        FrcPluginGlobals.IS_IN_FRC_INTERNAL_MODE
}

@JvmOverloads
fun com.intellij.openapi.diagnostic.Logger.infoWhenNotInTestMode(message: String, t: Throwable? = null)
{
    if (IS_IN_FRC_UNIT_TEST_MODE) this.debug(message, t) else this.info(message, t)
}
@JvmOverloads
fun com.intellij.openapi.diagnostic.Logger.warnWhenNotInTestMode(message: String, t: Throwable? = null)
{
    if (IS_IN_FRC_UNIT_TEST_MODE) this.debug(message, t) else this.warn(message, t)
}
@JvmOverloads
fun com.intellij.openapi.diagnostic.Logger.errorWhenNotInTestMode(message: String, t: Throwable? = null)
{
    if (IS_IN_FRC_UNIT_TEST_MODE) this.debug(message, t) else this.error(message, t)
}
