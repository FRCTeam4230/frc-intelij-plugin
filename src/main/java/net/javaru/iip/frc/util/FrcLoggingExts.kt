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
