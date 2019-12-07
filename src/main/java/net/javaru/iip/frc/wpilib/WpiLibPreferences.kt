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

package net.javaru.iip.frc.wpilib

import com.esotericsoftware.minlog.Log
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonNumberLiteral
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.json.psi.JsonValue
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScopesCore
import net.javaru.iip.frc.util.getModules

private object WpiLibPreferencesFunctions
private val LOG = Logger.getInstance(WpiLibPreferencesFunctions::class.java)

const val wpiLibDirName = ".wpilib"
const val wpiLibPreferencesFileName: String = "wpilib_preferences.json"
const val teamNumberPropertyName = "teamNumber"
const val projectYearPropertyName = "projectYear"
//const val currentLanguagePropertyName = "currentLanguage"
//const val enableCppIntellisensePropertyName = "enableCppIntellisense"


fun findLikelyWpiLibPreferencesPsiFileAsJsonFile(project: Project): JsonFile?
{
    return findLikelyWpiLibPreferencesPsiFile(project) as JsonFile
}

fun findLikelyWpiLibPreferencesPsiFile(project: Project): PsiFile?
{
    val psiFiles = findWpiLibPreferencesPsiFiles(project) { module ->  !(module.name.endsWith("-test") || module.name.endsWith("-main"))}
    return if (psiFiles.isEmpty())
    {
        null
    }
    else
    {
        // For 99.9% of the cases we should only have a single found file. 
        // But for the case of multiple files, we just go with the first one.
        if (psiFiles.size > 1)
        {
            val vfs = psiFiles.map{ it.virtualFile}
            LOG.warn("Multiple $wpiLibPreferencesFileName files found. We default to using first one. Files found: $vfs")
        }
        psiFiles[0]
    }
}

fun findWpiLibPreferencesPsiFiles(project: Project, filter: (module: Module) -> Boolean = { _ -> true}): List<PsiFile>
{
    if (project.isDefault)
    {
        //Do we need handle better? Make a notification?
        return emptyList()
    }

    /*
        Due to the way Gradle configures a project, a FRC project has two or three modules
            0 = {com.intellij.openapi.module.impl.ModuleImpl@32218} "Module: 'my-robot'"
            1 = {com.intellij.openapi.module.impl.ModuleImpl@32219} "Module: 'my-robot.test'"
            2 = {com.intellij.openapi.module.impl.ModuleImpl@32220} "Module: 'my-robot.main'"
        The file we want will be in 'my-robot' since it is in the base directory
        If present, the following will also be found:
             src/main/.wpilib/wpilib_preferences.json
             src/test/.wpilib/wpilib_preferences.json
        But that would be highly unlikely.
        Note, any such files in either of the `resources` directory are not found.
     */
    val foundFiles = mutableListOf<PsiFile>()

    try
    {
        DumbService.getInstance(project).runReadActionInSmartMode {
            val modules = project.getModules()
            modules.filter { filter.invoke(it) }.forEach { module: Module ->
                foundFiles.addAll(findWpiLibPreferencesPsiFiles(module))
            }
        }
    }
    catch (e: IndexNotReadyException)
    {
        // As far as I know, we should not get this since we are running the read action in Smart Mode. But better safe than sorry.
        LOG.info("[FRC] IndexNotReadyException when searching for $wpiLibPreferencesFileName files for project ${project}.")
        // TODO: make index not ready notification?
    }
    catch (e: Exception)
    {
        LOG.warn("[FRC] An exception occurred when finding $wpiLibPreferencesFileName files for project ${project}. Cause Summary: $e", e)
    }

    return foundFiles
}

fun findWpiLibPreferencesPsiFiles(module: Module): List<PsiFile>
{
    val foundFiles = mutableListOf<PsiFile>()
    
    val project = module.project

    try
    {
        DumbService.getInstance(project).runReadActionInSmartMode {
            val psiManager = PsiManager.getInstance(project)
            val rootManager = module.rootManager
            val contentRoots = rootManager.contentRoots
            contentRoots.forEach { contentRoot: VirtualFile? ->
                if (contentRoot != null)
                {
                    Log.trace("[FRC] content Root Found: ${contentRoot.path}")
                    val psiDirectory = psiManager.findDirectory(contentRoot)
                    if (psiDirectory != null)
                    {
                        val contentRootDirScope = GlobalSearchScopesCore.directoryScope(psiDirectory, false)
                        val wpiLibDirs = FilenameIndex.getFilesByName(project, wpiLibDirName, contentRootDirScope, true)
                        wpiLibDirs.forEach { wpiLibDirPsiFileSysItem: PsiFileSystemItem? ->
                            if (wpiLibDirPsiFileSysItem != null && wpiLibDirPsiFileSysItem is PsiDirectory)
                            {
                                val wpiLibDirScope = GlobalSearchScopesCore.directoryScope(wpiLibDirPsiFileSysItem, false)
                                val files = FilenameIndex.getFilesByName(project, wpiLibPreferencesFileName, wpiLibDirScope)
                                if (LOG.isTraceEnabled)
                                {
                                    files.forEach { LOG.trace("[FRC] Found: ${it.virtualFile.path}") }
                                }
                                foundFiles.addAll(files)
                            }
                        }
                    }
                }
            }
        }
    }
    catch (e: IndexNotReadyException)
    {
        // As far as I know, we should not get this since we are running the read action in Smart Mode. But better safe than sorry.
        LOG.info("[FRC] IndexNotReadyException when searching for $wpiLibPreferencesFileName files for module $module")
        // TODO: make index not ready notification?
    }
    catch (e: Exception)
    {
        LOG.warn("[FRC] An exception occurred when finding $wpiLibPreferencesFileName files for module ${module}. Cause Summary: $e", e)
    }
    
    return foundFiles
}

/**
 * Returns the configured teamNumber in the `wpilib_preferences.json` file, or -1 if it cannot be found.
 */
fun getConfiguredTeamNumber(project: Project): Int
{
    val jsonFile = findLikelyWpiLibPreferencesPsiFileAsJsonFile(project)
    return if (jsonFile == null) -1 else getConfiguredTeamNumber(jsonFile)
}

/**
 * Returns the configured teamNumber in the `wpilib_preferences.json` file, or -1 if it cannot be found.
 */
fun getConfiguredTeamNumber(wpiLibPreferences: JsonFile): Int = getIntPropertyValue(wpiLibPreferences, teamNumberPropertyName) ?: -1


fun getConfiguredProjectYear(project: Project): String?
{
    val jsonFile = findLikelyWpiLibPreferencesPsiFileAsJsonFile(project)
    return if (jsonFile == null) null else getConfiguredProjectYear(jsonFile)
}

fun getConfiguredProjectYear(wpiLibPreferences: JsonFile): String? = getStringPropertyValue(wpiLibPreferences, projectYearPropertyName)


@Suppress("SameParameterValue")
private fun getIntPropertyValue(wpiLibPreferences: JsonFile, propertyName: String): Int?
{
    return runReadAction {
        val jsonValue = getJsonValue(wpiLibPreferences, propertyName)
        val jsonNumberLiteral = jsonValue as JsonNumberLiteral?
        jsonNumberLiteral?.value?.toInt()
    }
}


@Suppress("SameParameterValue")
private fun getStringPropertyValue(wpiLibPreferences: JsonFile, propertyName: String): String?
{
    return runReadAction {
        val jsonValue = getJsonValue(wpiLibPreferences, propertyName)
        val jsonStringLiteral = jsonValue as JsonStringLiteral?
        jsonStringLiteral?.value
    }
}

private fun getJsonValue(wpiLibPreferences: JsonFile, propertyName: String): JsonValue?
{
    return runReadAction {
        val toplevelValue = wpiLibPreferences.topLevelValue
        val jsonObject = toplevelValue as JsonObject?
        val jsonProperty = jsonObject?.findProperty(propertyName)
        jsonProperty?.value
    }
}
