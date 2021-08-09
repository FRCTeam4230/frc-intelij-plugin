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

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl
import com.intellij.openapi.roots.ProjectRootManager
import net.javaru.iip.frc.facet.isFrcFacetedProject
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

/**
 * Convenience function for running a progress in the background. Per the [SDK Guide](https://www.jetbrains.org/intellij/sdk/docs/basics/architectural_overview/general_threading_rules.html)
 * callers should be prepared to catch and rethrow a `ProcessCanceledException`. "**This exception should never be logged**, it 
 * should be rethrown, and it’ll be handled in the infrastructure that started the process." 
 */
fun Project.runBackgroundTask(
        name: String,
        indeterminate: Boolean = true,
        cancellable: Boolean = false,
        background: PerformInBackgroundOption = PerformInBackgroundOption.ALWAYS_BACKGROUND,
        callback: (indicator: ProgressIndicator) -> Unit
                          )
{
    ProgressManager.getInstance().run(object : Task.Backgroundable(this, name, cancellable, background)
                                      {
                                          override fun run(indicator: ProgressIndicator)
                                          {
                                              if (indeterminate) indicator.isIndeterminate = true
                                              callback(indicator)
                                          }
                                      })
}

@Deprecated(message = "Use runBackgroundTask instead. If background is 'true', use PerformInBackgroundOption.ALWAYS_BACKGROUND, if 'false' use PerformInBackgroundOption.DEAF",
            replaceWith = ReplaceWith("Project.runBackgroundTask(name, indeterminate, cancellable, (if (background) PerformInBackgroundOption.ALWAYS_BACKGROUND else PerformInBackgroundOption.DEAF), callback)"),
            level = DeprecationLevel.WARNING)
fun Project.backgroundTask(
    name: String,
    indeterminate: Boolean = true,
    cancellable: Boolean = false,
    background: Boolean = false,
    callback: (indicator: ProgressIndicator) -> Unit
                          )
{
    val backgroundOption = if (background) PerformInBackgroundOption.ALWAYS_BACKGROUND else PerformInBackgroundOption.DEAF
    this.runBackgroundTask(name, indeterminate, cancellable, backgroundOption, callback)
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

fun Project.getProjectJdk(): ProjectJdkImpl?
{
    val sdk = getProjectSdk()
    return if (sdk == null) null else sdk as ProjectJdkImpl
}
fun Project.getProjectSdk(): Sdk? = ProjectRootManager.getInstance(this).projectSdk
fun Project.getProjectSdkHomePath(): String? = ProjectRootManager.getInstance(this).projectSdk?.homePath

fun getAllOpenFrcProjects(): Array<out Project>
{
    return getAllOpenProjects()
        .filter { it.isFrcFacetedProject() && it.isOpen && !it.isDisposed }
        .toTypedArray()
}

fun getAllOpenProjects(): Array<out Project>
{
    return ProjectManager
        .getInstance()
        .openProjects
        .filter { it.isOpen && !it.isDisposed }
        .toTypedArray()
}



/**
 * Finds a project to be used for tasks that require a Project. It will favor projects in this order:
 *
 * 1. FRC project with Focus
 * 2. Any FRC project
 * 3. Project with focus (non-FRC)
 * 4. Any project
 * 5. null
 */
fun findAnAnchorProject(): Project?
{
    val frcProjects = getAllOpenFrcProjects()
    return if (frcProjects.isNotEmpty())
    {
        frcProjects.findProjectWithFocus() ?: frcProjects[0]
    }
    else
    {
        val openProjects = getAllOpenProjects()
        if (openProjects.isNotEmpty())
        {
            openProjects.findProjectWithFocus() ?: openProjects[0]
        }
        else
        {
            null
        }

    }
}

/**
 * Looks for the project with focus, favoring FRC projects. If no FRC has focus,
 * it will check non FRC projects, unless `mustBeFrcProject` is set to `true`.
 */
fun findProjectWithFocus(mustBeFrcProject: Boolean = true): Project?
{
    val project: Project? = getAllOpenFrcProjects().findProjectWithFocus()

    return project ?: if (mustBeFrcProject)
    {
        project
    }
    else
    {
        ProjectManager.getInstance().openProjects.findProjectWithFocus()
    }
}




/** Returns the project with focus, or null is no project has focus, or there are no open projects. */
private fun Array<out Project>?.findProjectWithFocus(): Project?
{
    this?.forEach {
        if (it.isOpen && !it.isDisposed && it.findIdeFrameOrAlternateParentComponent()?.isFocusOwner == true)
        {
            return it
        }
    }
    return null
}