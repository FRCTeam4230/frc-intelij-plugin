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
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.task.ExternalSystemTaskId
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.jetbrains.plugins.gradle.service.project.data.ExternalProjectDataCache
import org.jetbrains.plugins.gradle.util.GradleConstants
import java.io.File
import java.nio.file.Path

// NOTE: AddGradleDslPluginActionHandler has example of modifying the gradle build file (for a groovy build file)

private val LOG = Logger.getInstance("#net.javaru.iip.frc.util.GradleUtils")

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
fun Project.reimportGradleProject(executionMode: ProgressExecutionMode = ProgressExecutionMode.IN_BACKGROUND_ASYNC,
                                  callback: ExternalProjectRefreshCallback? = null)
{
    //ImportModuleAction.doImport(this)  <-- this is for an initial import it looks like

    // The default call back (in the ImportSpecBuilder) calls ProjectDataManager.importData() on success.
    // This syncs IntelliJ IDEA's project structure (such as libraries)
    // So if we want to provide a call back, we need to wrap the passed in callback so that we are sure that importData() is called
    val ourCallback: ExternalProjectRefreshCallback = object : ExternalProjectRefreshCallback {
        override fun onSuccess(externalTaskId: ExternalSystemTaskId, externalProject: DataNode<ProjectData>?)
        {
            doProjectDataImport(externalProject)
            callback?.onSuccess(externalTaskId, externalProject)
        }

        override fun onSuccess(externalProject: DataNode<ProjectData>?)
        {
            doProjectDataImport(externalProject)
            callback?.onSuccess(externalProject)
        }

        override fun onFailure(externalTaskId: ExternalSystemTaskId, errorMessage: String, errorDetails: String?)
        {
            callback?.onFailure(externalTaskId, errorMessage, errorDetails)
        }

        override fun onFailure(errorMessage: String, errorDetails: String?)
        {
            callback?.onFailure(errorMessage, errorDetails)
        }

        fun doProjectDataImport(externalProject: DataNode<ProjectData>?)
        {
            if (externalProject != null)
            {
                val synchronous = executionMode == ProgressExecutionMode.MODAL_SYNC
                // We have to fully qualify the function so that there is not an imp0licit use of 'this'. Because we are in a Project extension function
                // an implicit `this` is added if we just type `service<ProjectDataManager>()` which results in Project.service<T> being called and
                // and not the ApplicationService service<T> function. Another word around would be to add a forwarding function outside this function.
                // and using it. For example:   inline fun <reified T : Any> appService() = service<T>()
                com.intellij.openapi.components.service<ProjectDataManager>().importData(externalProject, this@reimportGradleProject, synchronous)
            }
        }
    }

    // We save all documents because there is a possible case that there is an external system config file changed inside the ide.
    FileDocumentManager.getInstance().saveAllDocuments()
    // derived from looking at RefreshAllExternalProjectsAction, specifically when it calls ExternalSystemUtil.refreshProject
    ExternalSystemUtil.refreshProjects(ImportSpecBuilder(this, GradleConstants.SYSTEM_ID)
                                           .forceWhenUptodate(true)
                                           .use(executionMode)
                                           .callback(ourCallback)
                                      )

    // The ExternalSystemProjectTracker is newer, and is what the new small popup square (when you edit a build file) uses.
    // But it lacks a callback option
    //    val projectTracker = ExternalSystemProjectTracker.getInstance(this)
    //    projectTracker.scheduleProjectRefresh()
}