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

import com.intellij.openapi.externalSystem.importing.ImportSpecBuilder
import com.intellij.openapi.externalSystem.service.execution.ProgressExecutionMode
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import org.jetbrains.plugins.gradle.service.project.data.ExternalProjectDataCache
import org.jetbrains.plugins.gradle.util.GradleConstants

// NOTE: AddGradleDslPluginActionHandler has example of modifying the gradle build file (for a groovy build file)

fun Project.getGradleBuildVirtualFile(): VirtualFile?
{
    val cache = ExternalProjectDataCache.getInstance(this)
    val rootExternalProject = cache.getRootExternalProject(this.basePath!!)
    val buildFile = rootExternalProject?.buildFile
    return buildFile?.findVirtualFile(true)
}

fun Project.getGradleBuildPsiFile(): PsiFile? = this.getGradleBuildVirtualFile()?.findPsiFile(this)


fun Project.importGradleProject()
{
    // TODO: When a change is made to only support IDEA v2910.3 or greater, we can use the linkAndRefreshGradleProject from it. Note that it is marked experimental in the EAP version
    // /* v2019.3 */ org.jetbrains.plugins.gradle.service.project.open.linkAndRefreshGradleProject(this.basePath!!, this)
    /* v2019.2 */ org.jetbrains.plugins.gradle.service.project.open.importProject(this.basePath!!, this)
}

fun Module.importGradleProject() = this.project.importGradleProject()

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