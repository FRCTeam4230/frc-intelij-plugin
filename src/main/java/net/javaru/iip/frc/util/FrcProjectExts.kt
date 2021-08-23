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

import com.intellij.ide.projectView.ProjectView
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.Computable
import net.javaru.iip.frc.facet.isFrcFacetedProject
import org.jetbrains.plugins.gradle.settings.GradleSettings


// Note: There are also some Project Extension functions in FrcFacet.kt

private val LOG = Logger.getInstance("#net.javaru.iip.frc.util.ProjectExts")


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

/** Convenience Extension function for [DumbService.runWhenSmart]. */
fun Project.runWhenSmart(action:() -> Unit) = DumbService.getInstance(this).runWhenSmart{action()}

/** Convenience Extension function for [DumbService.runReadActionInSmartMode]. */
fun Project.runReadActionInSmartMode(action:() -> Unit) = DumbService.getInstance(this).runReadActionInSmartMode{action()}

/** Convenience Extension function for [DumbService.runReadActionInSmartMode]. */
fun <T> Project.runReadActionInSmartMode(computable: Computable<T>): T = DumbService.getInstance(this).runReadActionInSmartMode(computable)

/**
 * Convenience function for running a process in the background. Per the [SDK Guide](https://plugins.jetbrains.com/docs/intellij/general-threading-rules.html#background-processes-and-processcanceledexception)
 * callers should be prepared to catch and rethrow a `ProcessCanceledException`. "**This exception should never be logged**, it 
 * should be rethrown, and it’ll be handled in the infrastructure that started the process." 
 * 
 */
fun Project.runBackgroundTask(
    name: String,
    indeterminate: Boolean = true,
    cancellable: Boolean = false,
    background: PerformInBackgroundOption = PerformInBackgroundOption.ALWAYS_BACKGROUND,
    action: (indicator: ProgressIndicator) -> Unit
                          )
{
    ProgressManager.getInstance().run(object : Task.Backgroundable(this, name, cancellable, background)
                                      {
                                          override fun run(indicator: ProgressIndicator)
                                          {
                                              if (indeterminate) indicator.isIndeterminate = true
                                              action(indicator)
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
 * Refreshes the [ProjectView] (i.e. the Project tool window ) **for all open FRC projects**.
 * @see refreshProjectView
 */
fun refreshProjectViewsForAllFrcProjects()
{
    try
    {
        val openProjects: Array<out Project> = getAllOpenProjects()
        for (project in openProjects)
        {
            project.refreshProjectView()
        }
    }
    catch (e: Throwable)
    {
        LOG.warn("[FRC] An exception occurred when attempting to refresh project views for FRC open projects. Cause summary: $e", e)
    }
}

/**
 * Refreshes the [ProjectView] (i.e. the Project tool window ) for the receiver project.
 * @see refreshProjectViewsForAllFrcProjects
 */
fun Project.refreshProjectView()
{
    try
    {
        val view = ProjectView.getInstance(this)
        if (view != null)
        {
            view.refresh()
            view.currentProjectViewPane.updateFromRoot(true)
        }
    }
    catch (e: Throwable)
    {
        LOG.warn("[FRC] An exception occurred when attempting to refresh project view for project '${this}' Cause summary: $e", e)
    }
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