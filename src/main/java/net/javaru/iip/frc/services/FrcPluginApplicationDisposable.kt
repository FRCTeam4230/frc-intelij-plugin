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

package net.javaru.iip.frc.services

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace


/**
 * An Application Service that we can use as an Application level parent disposable.
 *
 * This can easily be obtained via [net.javaru.iip.frc.util.getProjectParentDisposable] and/or
 * [net.javaru.iip.frc.util.getParentDisposable] in `FrcPluginUtils.kt`.
 *
 * See https://jetbrains.org/intellij/sdk/docs/basics/disposers.html#choosing-a-disposable-parent
 *
 * "For resources required for the entire lifetime of a plugin, use an application or project level service."
 *
 * For a project level disposable service, use [FrcPluginProjectDisposable].
 */
class FrcPluginApplicationDisposable: Disposable
{
    private val logger = logger<FrcPluginApplicationDisposable>()

    var isDisposed = false
        private set


    companion object
    {
        @JvmStatic
        fun getInstance() = service<FrcPluginApplicationDisposable>()
    }

    override fun dispose()
    {
        try
        {
            isDisposed = true
            logger.trace { "[FRC] FrcPluginApplicationDisposable.dispose() called." }
        }
        catch (ignore: Exception) { }
    }
}