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
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.gradle.settings.GradleSettings


// Note: There are also some Project Extension functions in FrcFacet.kt

private val LOG = Logger.getInstance("#net.javaru.iip.frc.util.ProjectExts")

fun Project?.isAntBasedFrcProject(): Boolean
{
    return if (this == null)
        false
    else
        // TODO: This works for now since there are only two possibilities: Legacy Ant or GradleRIO. But that may change some day
        //       We should probably check if there is an build file present. But that is super low priority for now.
        !this.isGradleProject()
}

fun Project?.isGradleProject(): Boolean
{
    return try
    {
        // copied from   org/jetbrains/plugins/gradle/execution/GradleConsoleFilterProvider.java:59
        // When asked if this was ok methodology on forums: https://intellij-support.jetbrains.com/hc/en-us/community/posts/360004424640-Determine-if-a-project-is-a-Gradle-Project
        // The response was yes that that works, as would
        //     ExternalSystemApiUtil.isExternalSystemAwareModule(GradleConstants.SYSTEM_ID, module)
        if (this == null) false else !GradleSettings.getInstance(this).linkedProjectsSettings.isEmpty()
    }
    catch (e: Exception)
    {
        LOG.warn("An exception occurred when checking if a project in is a Gradle Based Project. Cause Summary: $e", e)
        false
    }
}

fun Project.backgroundTask(
        name: String,
        indeterminate: Boolean = true,
        cancellable: Boolean = false,
        background: Boolean = false,
        callback: (indicator: ProgressIndicator) -> Unit
                          )
{
    ProgressManager.getInstance().run(object : Task.Backgroundable(this, name, cancellable, { background })
                                      {
                                          override fun shouldStartInBackground() = background

                                          override fun run(indicator: ProgressIndicator)
                                          {
                                              try
                                              {
                                                  if (indeterminate) indicator.isIndeterminate = true
                                                  callback(indicator)
                                              }
                                              catch (e: Throwable)
                                              {
                                                  e.printStackTrace()
                                                  throw e
                                              }
                                          }
                                      })
}

/**
 * Convenience extension that returns the modules for a project, returning an empty array if the project is `null`.
 * It simply safely calls `ModuleManager.getInstance(project).modules`
 */
fun Project?.getModules(): Array<Module>
{
    val modules = this?.let { ModuleManager.getInstance(it).modules }
    return modules ?: emptyArray()
}

/** Returns the 'main' Gradle module, i.e. `projectName.main` , or null if it cannot be found. */
fun Project?.getMainModule(): Module? = this.getModules().firstOrNull { module -> module.name.endsWith(".main") }
/** Returns the 'test' Gradle module, i.e. `projectName.test` , or null if it cannot be found. */
fun Project?.getTestModule(): Module? = this.getModules().firstOrNull { module -> module.name.endsWith(".test") }