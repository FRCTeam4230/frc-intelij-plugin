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

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.intellij.facet.FacetManager
import com.intellij.ide.projectView.ProjectView
import com.intellij.idea.IdeaLogger
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProcessCanceledException
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
import com.intellij.psi.impl.source.tree.injected.changesHandler.debug
import com.intellij.util.concurrency.AppExecutorUtil
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.isUnitTestMode
import net.javaru.iip.frc.services.FrcErrorReportSubmitter
import net.javaru.iip.frc.services.FrcPluginProjectDisposable
import net.javaru.iip.frc.services.ReportableEvent
import org.jetbrains.annotations.Contract
import org.jetbrains.plugins.gradle.settings.GradleSettings
import java.util.concurrent.Callable


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
fun Project.runWhenSmart(action:() -> Unit)
{
    if (isUnitTestMode()) {
        action()
    }
    else if (!this.isDisposed) {
        DumbService.getInstance(this).runWhenSmart {
            if (this.isDisposed)
                LOG.debug { "[FRC] action will not run as project is disposed." }
            else
                action()
        }
    }
}

/** Convenience Extension function for [DumbService.runReadActionInSmartMode]. */
fun Project.runReadActionInSmartMode(action:() -> Unit)
{
    if (isUnitTestMode())
    {
       action()
    }
    else if (!this.isDisposed) {
        DumbService.getInstance(this).runReadActionInSmartMode {
            if (this.isDisposed)
                LOG.debug { "[FRC] read action will not run as project is disposed." }
            else
                action()
        }
    }
}

/** Convenience Extension function for [DumbService.runReadActionInSmartMode]. */
fun <T> Project.runReadActionInSmartMode(computable: Computable<T>): T = DumbService.getInstance(this).runReadActionInSmartMode(computable)


inline fun <R> Project.runNonBlockingReadActionInSmartMode(crossinline action: () -> R, crossinline uiContinuation: (R) -> Unit)
{
    // Based on org/jetbrains/kotlin/idea/util/nonblocking.kt:12
    if (isUnitTestMode()) {
        val result = action()
        uiContinuation(result)
    }
    else {
        val disposable = FrcPluginProjectDisposable.getInstance(this)
        ReadAction.nonBlocking(Callable { action() })
            .inSmartMode(this)
            .expireWith(disposable)
            .finishOnUiThread(ModalityState.current()) { result ->
                uiContinuation(result)
            }
            // Common Executor examples are
            //      com.intellij.util.concurrency.NonUrgentExecutor.getInstance()
            //      AppExecutorUtil.getAppExecutorService()
            //      com.intellij.util.concurrency.BoundedTaskExecutor
            .submit(AppExecutorUtil.getAppExecutorService())
//            .submit(AppExecutorUtil.createBoundedApplicationPoolExecutor("Read Action", AppExecutorUtil.getAppExecutorService(), 1, disposable))
    }
}

// ExecutorService executor = AppExecutorUtil.createBoundedApplicationPoolExecutor("Document Commit Pool", AppExecutorUtil.getAppExecutorService(), 1, this);
// ExecutorService executor = SequentialTaskExecutor.createSequentialApplicationPoolExecutor("Json Vfs Updater Executor");

inline fun Project.runNonBlockingReadActionInSmartMode(crossinline action: () -> Unit)
{
    if (isUnitTestMode()) {
        action()
    }
    else {
        val disposable = FrcPluginProjectDisposable.getInstance(this)
        ReadAction.nonBlocking(Callable { action() })
            .inSmartMode(this)
            .expireWith(disposable)
            // Common Executor examples are
            //      com.intellij.util.concurrency.NonUrgentExecutor.getInstance()
            //      AppExecutorUtil.getAppExecutorService()
            //      com.intellij.util.concurrency.BoundedTaskExecutor
            .submit(AppExecutorUtil.getAppExecutorService())
//            .submit(AppExecutorUtil.createBoundedApplicationPoolExecutor("Read Action", AppExecutorUtil.getAppExecutorService(), 1, disposable))
    }
}

/**
 * Convenience function for running a process in the background. Per the [SDK Guide](https://plugins.jetbrains.com/docs/intellij/general-threading-rules.html#background-processes-and-processcanceledexception)
 * callers should be prepared to catch and rethrow a `ProcessCanceledException`. "**This exception should never be logged**, it 
 * should be rethrown, and it’ll be handled in the infrastructure that started the process." 
 * Keywords: runInBackground, runTaskInBackground
 */
fun Project.runBackgroundTask(
    name: String,
    indeterminate: Boolean = true,
    cancellable: Boolean = false,
    background: PerformInBackgroundOption = PerformInBackgroundOption.ALWAYS_BACKGROUND,
    debuggingData: Map<String, String?>? = null,
    action: (indicator: ProgressIndicator) -> Unit
                          )
{
    ProgressManager.getInstance().run(object : Task.Backgroundable(this, name, cancellable, background)
                                      {
                                          override fun run(indicator: ProgressIndicator)
                                          {
                                              if (indeterminate) indicator.isIndeterminate = true
                                              try
                                              {
                                                  action(indicator)
                                              }
                                              catch (pce: ProcessCanceledException)
                                              {
                                                  throw pce
                                              }
                                              catch (t: Throwable)
                                              {
                                                  try
                                                  {
                                                      FrcErrorReportSubmitter.submitReportableEvent(ReportableEvent(
                                                          correlationId = "Task: $name",
                                                          project = this@runBackgroundTask,
                                                          throwable = t,
                                                          lastActionId = IdeaLogger.ourLastActionId,
                                                          additionalData = debuggingData))
                                                  }
                                                  catch (t: Throwable)
                                                  {
                                                      LOG.info("[FRC] Could not submit reportable event for runBackgroundTask. Cause: $t")
                                                  }
                                              }
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
    this.runBackgroundTask(name, indeterminate, cancellable, backgroundOption, action = callback)
}

fun Project?.runSafely(correlationId: String, debuggingData: Map<String, Any?>? = null, action: () -> Unit)
{
    try
    {
        action.invoke()
    }
    catch (t: Throwable)
    {
        try
        {
            FrcErrorReportSubmitter.submitReportableEvent(
                ReportableEvent(
                    correlationId = "Task: $correlationId",
                    project = this,
                    throwable = t,
                    lastActionId = IdeaLogger.ourLastActionId,
                    additionalData = debuggingData
                               )
                                                             )
        }
        catch (t: Throwable)
        {
            LOG.info("[FRC] Could not submit reportable event for runSafely. Cause: $t")
        }
    }
}

fun <R> Project?.runSafelyWithResult(correlationId: String, debuggingData: Map<String, String?>? = null, action: () ->R): Result<R, Throwable>
{
    return try
    {
        Ok(action.invoke())
    }
    catch (t: Throwable)
    {
        try
        {
            FrcErrorReportSubmitter.submitReportableEvent(
                ReportableEvent(
                    correlationId = "Task: $correlationId",
                    project = this,
                    throwable = t,
                    lastActionId = IdeaLogger.ourLastActionId,
                    additionalData = debuggingData
                               ))
        }
        catch (t: Throwable)
        {
            LOG.info("[FRC] Could not submit reportable event for runSafelyWithResult. Cause: $t")
        }
        Err(t)
    }
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

@Contract("null -> false")
fun Module?.isKotlinFacetedModule(): Boolean
{
    if (this == null || this.isDisposed)
    {
        return false
    }
    // We would need to add Kotlin as a dependency to access the KotlinFacet or KotlinFacetType
    // For now, we don't want to do that.
//    val kotlinFacet = FacetManager.getInstance(this).getFacetByType(KotlinFacetType.TYPE_ID)

    // Kotlin Facet Name = "Kotlin"
    //              ID =   "kotlin-language"
    val kotlinFacet = FacetManager
        .getInstance(this)
        .allFacets
        .asSequence().firstOrNull { it.name == "Kotlin" }
    return kotlinFacet != null
}

@Contract("null -> false")
fun Project?.iKotlinFacetedProject(): Boolean
{
    if (this != null && !this.isDisposed)
    {
        val modules = ModuleManager.getInstance(this).modules
        for (module in modules)
        {
            if (module.isKotlinFacetedModule())
            {
                return true
            }
        }
    }
    return false
}