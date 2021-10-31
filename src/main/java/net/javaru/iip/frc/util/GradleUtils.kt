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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.externalSystem.ExternalSystemManager
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectId
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectTracker
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.jetbrains.plugins.gradle.service.project.data.ExternalProjectDataCache
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.io.File
import java.nio.file.Path

// NOTE: AddGradleDslPluginActionHandler has example of modifying the gradle build file (for a groovy build file)

// Keep an eye on:  com.intellij.externalSystem.DependencyModifierService
// It's experimental, but allows you to modify the build model such as adding a dependency
// It does not (yet) support modifying a Plugin version. But JetBrains seems to indicate
// that that is possibly planned:
// https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010674120-Programatically-Update-Plugin-Version-in-Gradle-Build-File

private object GradleUtils

private val logger = logger<GradleUtils>()

fun Project.getGradleBuildIoFile(): File?
{
    if (this.basePath == null) return null
    val cache = ExternalProjectDataCache.getInstance(this)
    val rootExternalProject = cache.getRootExternalProject(this.basePath!!)
    return rootExternalProject?.buildFile
}

fun Project.getGradleBuildNIoPath(): Path? = this.getGradleBuildIoFile()?.toPath()

fun Project.getGradleBuildVirtualFile(): VirtualFile? = this.getGradleBuildIoFile()?.findVirtualFile(true)

fun Project.getGradleBuildPsiFile(): PsiFile? = this.getGradleBuildVirtualFile()?.findPsiFile(this)

///**
// * Imports a new gradle project. In most cases this needs to wrapped in a write action:
// * ```
// * ApplicationManager.getApplication().runWriteAction() {
// *     module.importNewGradleProject()
// * }
// * ```
// * This wraps the IntelliJ IDEA API call so that some logistics can be handled in a single place.
// */
//fun Module.importNewGradleProject() = this.project.importNewGradleProject()
//
///**
// * Imports a new gradle project. In most cases this needs to wrapped in a write action:
// * ```
// * ApplicationManager.getApplication().runWriteAction() {
// *     project.importNewGradleProject()
// * }
// * ```
// * This wraps the IntelliJ IDEA API call so that some logistics can be handled in a single place.
// */
//fun Project.importNewGradleProject()
//{
//    // Version 2019.2 has the experimental API function: org.jetbrains.plugins.gradle.service.project.open.importProject(this.basePath!!, this)
//    // Version 2019.3 has the new API function:          org.jetbrains.plugins.gradle.service.project.open.linkAndRefreshGradleProject(this.basePath!!, this)
//    //
//    // Eventually, once we stop supporting v2019.2, we can just call the linkAndRefreshGradleProject directly
//    /* 2019.2  */  //org.jetbrains.plugins.gradle.service.project.open.importProject(this.basePath!!, this)
//    /* 2019.3+ */  org.jetbrains.plugins.gradle.service.project.open.linkAndRefreshGradleProject(this.basePath!!, this)
//
//}

@JvmOverloads
fun Project.reimportGradleProject(callback: ExternalProjectRefreshCallback? = null)
{
    // Should we instead use:
    //ImportModuleAction.doImport(this)

    // derived from looking at RefreshAllExternalProjectsAction, specifically when it calls ExternalSystemUtil.refreshProjects
    ExternalSystemUtil.refreshProjects(
        ImportSpecBuilder(this, GradleConstants.SYSTEM_ID)
            .use(ProgressExecutionMode.IN_BACKGROUND_ASYNC)
            .callback(callback))
}

fun Project.markGradleProjectAsNeedingReimport(scheduleForAutoReimport: Boolean = false)
{
    val projectTracker = ExternalSystemProjectTracker.getInstance(this)
    val projectSettings = this.findAllProjectSettings()
    logger.trace{"[FRC] Marking Gradle for project '${this.name}' as dirty. scheduleForAutoReimport = $scheduleForAutoReimport"}
    
    // The externalProjectsWatcher.markDirty method auto imports, which we don't want. So we basically duplicate its functionality here
    //val externalProjectsManager = ExternalProjectsManagerImpl.getInstance(this)
    //projectSettings.forEach {
    //    externalProjectsManager.externalProjectsWatcher.markDirty(it.externalProjectPath)
    //}
    
    ApplicationManager.getApplication().invokeLater(
        {
            projectSettings.forEach {
                projectTracker.markDirty(it)
            }
            // Note, in the externalProjectsWatcher.markDirty implementation, it also iterates over
            //       contributors. However, the gradle plugin does not implement the ExternalSystemProjectsWatcherImpl.Contributor
            //       extension point (only maven does) so it would be an empty list
            if (scheduleForAutoReimport) 
                projectTracker.scheduleProjectRefresh() 
            else 
                projectTracker.scheduleChangeProcessing()
        }, this.disposed)
        
    

}

private fun Project.findAllProjectSettings(): List<ExternalSystemProjectId>
{
    val list: MutableList<ExternalSystemProjectId> = ArrayList()
    ExternalSystemManager.EP_NAME.forEachExtensionSafe { manager: ExternalSystemManager<*, *, *, *, *> ->
        val systemId = manager.systemId
        val linkedProjectsSettings = manager.settingsProvider.`fun`(this).linkedProjectsSettings
        for (settings in linkedProjectsSettings)
        {
            val externalProjectPath = settings.externalProjectPath ?: continue
            list.add(ExternalSystemProjectId(systemId, externalProjectPath))
        }
    }
    return list
}

/** Reimports the Gradle project by scheduling it via the internal ProjectTracker */
fun Project.scheduleGradleReimport() = this.markGradleProjectAsNeedingReimport(scheduleForAutoReimport = true)
