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

import com.intellij.ide.impl.ProjectUtil
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import net.javaru.iip.frc.notify.FrcNotificationType
import net.javaru.iip.frc.notify.FrcNotifications
import org.jetbrains.plugins.gradle.service.project.data.ExternalProjectDataCache
import org.jetbrains.plugins.gradle.service.project.open.attachGradleProjectAndRefresh
import org.jetbrains.plugins.gradle.service.project.open.setupGradleSettings
import org.jetbrains.plugins.gradle.settings.GradleProjectSettings
import org.jetbrains.plugins.gradle.util.GradleConstants

// NOTE: AddGradleDslPluginActionHandler has example of modifying the gradle build file (for a groovy build file)

private val LOG = Logger.getInstance("#net.javaru.iip.frc.util.GradleUtils")

fun Project.getGradleBuildVirtualFile(): VirtualFile?
{
    val cache = ExternalProjectDataCache.getInstance(this)
    val rootExternalProject = cache.getRootExternalProject(this.basePath!!)
    val buildFile = rootExternalProject?.buildFile
    return buildFile?.findVirtualFile(true)
}

fun Project.getGradleBuildPsiFile(): PsiFile? = this.getGradleBuildVirtualFile()?.findPsiFile(this)

/**
 * Imports a new gradle project. In most cases this needs to wrapped in a write action:
 * ```
 * ApplicationManager.getApplication().runWriteAction() {
 *     module.importNewGradleProject()
 * }
 * ```
 * This wraps the IntelliJ IDEA API call so that some logistics can be handled in a single place.
 */
fun Module.importNewGradleProject() = this.project.importNewGradleProject()

/**
 * Imports a new gradle project. In most cases this needs to wrapped in a write action:
 * ```
 * ApplicationManager.getApplication().runWriteAction() {
 *     project.importNewGradleProject()
 * }
 * ```
 * This wraps the IntelliJ IDEA API call so that some logistics can be handled in a single place.
 */
fun Project.importNewGradleProject()
{
    // Version 2019.2 has the experimental API function: org.jetbrains.plugins.gradle.service.project.open.importProject(this.basePath!!, this)
    // Version 2019.3 has the new API function:          org.jetbrains.plugins.gradle.service.project.open.linkAndRefreshGradleProject(this.basePath!!, this)
    //
    // Eventually, once we stop supporting v2019.2, we can just call the linkAndRefreshGradleProject directly
    /* 2019.2  */  org.jetbrains.plugins.gradle.service.project.open.importProject(this.basePath!!, this)
    /* 2019.3+ */  //org.jetbrains.plugins.gradle.service.project.open.linkAndRefreshGradleProject(this.basePath!!, this)
    
}


private fun Project.importGradleProject20192()
{
    // This function mimics hat is done in the function:
    //         org.jetbrains.plugins.gradle.service.project.open.importProject(this.basePath!!, this)
    // found in v2019.2, but removed in v2019.3 and replaced with  linkAndRefreshGradleProject(this.basePath!!, this)
    // We may want to experiment using this method in 102.2 so as not get the "experimental API use" warning.
    val projectDirectory: String? = this.basePath
    if (projectDirectory == null)
    {
        LOG.warn("[FRC] baseDir was null for project. Cannot import project: $this")
    }
    else
    {
        // Taken from org.jetbrains.plugins.gradle.service.project.open.importProject(String, Project) which is marked experimental in v2019.2
        LOG.info("[FRC] Importing project at $projectDirectory")
        val projectSdk = ProjectRootManager.getInstance(this).projectSdk
        val gradleProjectSettings = GradleProjectSettings()
        setupGradleSettings(gradleProjectSettings, projectDirectory, this, projectSdk)
        attachGradleProjectAndRefresh(gradleProjectSettings, this)
        ProjectUtil.updateLastProjectLocation(projectDirectory)
        this.save()
    }
}

private fun Project.importGradleProject20193()
{
    // This function mimics the functionality of the function:
    //       org.jetbrains.plugins.gradle.service.project.open.linkAndRefreshGradleProject(this.basePath!!, this)
    // found in v2019.3 and greater, but can be called in v2019.2. However, there is no guarantee that some of the helper
    // methods/functions called are the same in 2010.2 and 2019.3 Need to do some testing and experimenting. 
    // Once we no longer support v2019.2, we can basically eliminate this method and just call 
    // the API method directly so as to have any new functionality or bug fixes
    val projectFilePath: String? = this.basePath
    if (projectFilePath == null)
    {
        LOG.warn("[FRC] baseDir was null for project. Cannot import project: $this")
        notifyCannotAutoImport()
    }
    else
    {
        val localFileSystem = LocalFileSystem.getInstance()
        val projectFile = localFileSystem.refreshAndFindFileByPath(projectFilePath)
        if (projectFile == null)
        {
            val shortPath = FileUtil.getLocationRelativeToUserHome(FileUtil.toSystemDependentName(projectFilePath), false)
            LOG.warn("[FRC] Cannot auto import project. Gradle script file '$shortPath' was not found. ")
            notifyCannotAutoImport()
        }
        else
        {
            val projectDirectory = getProjectDirectory(projectFile)
            val projectSdk = ProjectRootManager.getInstance(this).projectSdk
            val gradleProjectSettings = GradleProjectSettings()
            setupGradleSettings(gradleProjectSettings, projectDirectory.path, this, projectSdk)
            attachGradleProjectAndRefresh(gradleProjectSettings, this)
        }
        
    }
}

private fun notifyCannotAutoImport()
{
    FrcNotifications.notify(FrcNotificationType.ACTIONABLE_ERROR, "Cannot auto import the Gradle file to the project. You will need to manually import it.")
}

private fun getProjectDirectory(file: VirtualFile): VirtualFile
{
    if (!file.isDirectory) return file.parent
    return file
}


@JvmOverloads
fun Project.reimportGradleProject(callback: ExternalProjectRefreshCallback? = null)
{
    // derived from looking at RefreshAllExternalProjectsAction, specifically when it calls ExternalSystemUtil.refreshProjects
    ExternalSystemUtil.refreshProjects(ImportSpecBuilder(this, GradleConstants.SYSTEM_ID)
                                           .forceWhenUptodate(true)
                                           .use(ProgressExecutionMode.IN_BACKGROUND_ASYNC)
                                           .callback(callback)
                                      )
}