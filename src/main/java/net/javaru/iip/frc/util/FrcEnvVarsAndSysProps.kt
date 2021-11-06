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

import com.intellij.openapi.diagnostic.logger
import org.apache.commons.lang3.BooleanUtils


@Suppress("unused") private object FrcEnvVarsAndSysProps
@Suppress("unused") private object FrcSystemPropertyUtils
@Suppress("unused") private object FrcEnvironmentVariables
@Suppress("unused") private object FrcEnvironmentVariableUtils

@Suppress("unused")
private val LOG = logger<FrcEnvVarsAndSysProps>()

fun getBooleanSystemProperty(key: String, default: Boolean = false): Boolean = BooleanUtils.toBoolean(System.getProperty(key, default.toString()))


sealed interface FrcSystemConfig<T>
{
    val key: String

    val default: T

    val value: T
}

object FrcSystemConfigs
{
    object ErrorReportSubmitterUseQa : FrcSystemConfig<Boolean>
    {
        override val key: String = "frc.error.report.submitter.use.qa"
        override val default: Boolean = false
        override val value: Boolean = getBooleanSystemProperty(key, default)
    }
}
