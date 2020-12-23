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

import com.intellij.ide.plugins.cl.PluginClassLoader
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import icons.FrcIcons
import net.javaru.iip.frc.services.FrcApplicationDisposableService
import net.javaru.iip.frc.services.FrcProjectLifecycleService
import org.apache.commons.io.FilenameUtils
import java.io.InputStream
import java.net.URL
import java.nio.file.Path

private object FrcPluginUtils
private val LOG = Logger.getInstance(FrcPluginUtils::class.java)

inline fun invokeLater(crossinline func: () -> Unit)
{
    if (ApplicationManager.getApplication().isDispatchThread)
    {
        func()
    }
    else
    {
        ApplicationManager.getApplication().invokeLater({ func() }, ModalityState.defaultModalityState())
    }
}

@JvmOverloads
fun getPluginClassloader(clazz: Class<*> = FrcIcons::class.java): PluginClassLoader = clazz.classLoader as PluginClassLoader

@WillNotThrowException
fun getPluginResource(path: Path): URL? = getPluginResource(path.toString())

@WillNotThrowException
fun getPluginResource(path: String): URL?
{
    return try
    {
        getPluginClassloader().getResource(FilenameUtils.separatorsToUnix(path))
    }
    catch (e: Exception)
    {
        LOG.warn("Could not getPluginResource (as URL) for '$path' due to an exception: $e", e)
        null
    }
}

@WillNotThrowException
fun getPluginResourceAsStream(path: Path): InputStream? = getPluginResourceAsStream(path.toString())

@WillNotThrowException
fun getPluginResourceAsStream(path: String): InputStream?
{
    return try
    {
        getPluginClassloader().getResourceAsStream(FilenameUtils.separatorsToUnix(path))
    }
    catch (e: Exception)
    {
        LOG.warn("Could not getPluginResourceAsStream for '$path' due to an exception: $e", e)
        null
    }
}

@WillNotThrowException
fun getPluginResourceAsText(resourcePath: String): String?
{
    val inputStream = getPluginResourceAsStream(resourcePath)
    return if (inputStream == null)
    {
        LOG.warn("[FRC] Could not find resource: $resourcePath")
        null
    }
    else
    {
        try
        {
            inputStream.bufferedReader().use { it.readText() }
        }
        catch (t: Throwable)
        {
            LOG.warn("[FRC] An exception occurred when trying to read resource '$resourcePath'. Cause Summary: $t", t)
            null
        }
    }
}

fun Project?.getParentDisposable(): Disposable
{
    return if (this != null) FrcProjectLifecycleService.getInstance(this) else getApplicationParentDisposable()
}

fun getApplicationParentDisposable(): Disposable = FrcApplicationDisposableService.getInstance()


