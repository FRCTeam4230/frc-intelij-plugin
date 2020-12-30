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

import com.asarkar.semver.SemVer
import com.intellij.ide.plugins.PluginManager
import com.intellij.ide.plugins.cl.PluginClassLoader
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import icons.FrcIcons
import net.javaru.iip.frc.FrcPluginGlobals.FRC_PLUGIN_ID_STRING
import net.javaru.iip.frc.services.FrcApplicationDisposableService
import net.javaru.iip.frc.services.FrcProjectLifecycleService
import org.apache.commons.io.FilenameUtils
import java.io.InputStream
import java.net.URL
import java.nio.file.Path

private object FrcPluginUtils
private val LOG = logger<FrcPluginUtils>()

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

/**
 * Returns the full version, including the Idea Version, of the running FRC plugin. For example: `1.4.0-2020.3`
 * In the rare event the Plugin Version cannot be determined, `null` is returned.
 * @see getFrcPluginFullVersion
 * @see getFrcPluginCoreVersionString
 * @see getFrcPluginCoreVersion
 */
fun getFrcPluginFullVersionString(): String?
{
    val pluginId = PluginId.getId(FRC_PLUGIN_ID_STRING)
    val pluginDescriptor = PluginManager.getPlugin(pluginId)
    return pluginDescriptor?.version
}

/**
 * Returns the full version, including the Idea Version, of the running FRC plugin. For example: `1.4.0-2020.3`
 * In the rare event the Plugin Version cannot be determined, `null` is returned.
 * Note that the supported IDEA version is parsed as the 'preReleaseVersion` within the `SemVer`.
 * @see getFrcPluginFullVersionString
 * @see getFrcPluginCoreVersionString
 * @see getFrcPluginCoreVersion
 */
fun getFrcPluginFullVersion(): SemVer? = getFrcPluginFullVersionString().toSemVer()

/**
 * Returns the core version, *without* the Idea Version, of the running FRC plugin. For example: `1.4.0`
 * In the rare event the Plugin Version cannot be determined, `null` is returned.
 * @see getFrcPluginCoreVersion
 * @see getFrcPluginFullVersionString
 * @see getFrcPluginFullVersion
 */
fun getFrcPluginCoreVersionString(): String?
{
    return getFrcPluginFullVersionString()?.substringBefore('-')
}

/**
 * Returns the core version, *without* the Idea Version, of the running FRC plugin. For example: `1.4.0`
 * In the rare event the Plugin Version cannot be determined, `null` is returned.
 * @see getFrcPluginCoreVersionString
 * @see getFrcPluginFullVersionString
 * @see getFrcPluginFullVersion
 */
fun getFrcPluginCoreVersion(): SemVer? = getFrcPluginCoreVersionString().toSemVer()

private fun String?.toSemVer(): SemVer?
{
    // We're using SemVer from com.asarkar:jsemver but it should be noted IDEA has a built in SemVer in com.intellij.util.text - but it's less robust than the library one
    return try
    {
        if (this == null) null else SemVer.parse(this)
    } catch (e: Exception)
    {
        LOG.warn("Could not parse '$this' to a Semantic Version. Cause Summary: $e", e)
        null
    }
}