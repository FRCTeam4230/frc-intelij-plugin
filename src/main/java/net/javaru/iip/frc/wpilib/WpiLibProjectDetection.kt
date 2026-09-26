/*
 * Copyright 2015-2026 the original author or authors.
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

package net.javaru.iip.frc.wpilib

import com.intellij.facet.FacetManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import net.javaru.iip.frc.facet.FrcFacet
import net.javaru.iip.frc.facet.getOrAddFrcFacet
import net.javaru.iip.frc.util.getMainModule
import org.jetbrains.plugins.gradle.settings.GradleSettings
import org.jetbrains.annotations.Contract

private object WpiLibProjectDetection
private val logger = logger<WpiLibProjectDetection>()

/*
 * A WPILib (i.e. FRC) project is detected solely by the presence of the `.wpilib/wpilib_preferences.json` file,
 * which the WPILib tooling creates in the root directory of every robot project. Detection only uses the VFS
 * (not indexes or the project model), so it works as soon as a project is opened (before the Gradle import)
 * and is safe to use in dumb mode.
 */

/** Determines if the project is a WPILib project, i.e. has a `.wpilib/wpilib_preferences.json` file in its root (or a module content root). */
@Contract("null -> false")
fun Project?.isWpiLibProject(): Boolean
{
    if (this == null || this.isDisposed || this.isDefault) return false
    return findWpiLibProjectRootDirs().isNotEmpty()
}

/** Determines if the module is part of a WPILib project, i.e. one of its content roots is in, or is, a WPILib project root directory. */
@Contract("null -> false")
fun Module?.isWpiLibModule(): Boolean
{
    if (this == null || this.isDisposed) return false
    val wpiLibRoots = project.findWpiLibProjectRootDirs()
    if (wpiLibRoots.isEmpty()) return false
    return ModuleRootManager.getInstance(this).contentRoots.any { contentRoot -> wpiLibRoots.any { VfsUtilCore.isAncestor(it, contentRoot, false) } }
}

/** Finds the directories, from the project directory and module content roots, that contain a `.wpilib/wpilib_preferences.json` file. */
fun Project.findWpiLibProjectRootDirs(): List<VirtualFile>
{
    if (isDisposed) return emptyList()
    val candidates = linkedSetOf<VirtualFile>()
    guessProjectDir()?.let { candidates.add(it) }
    ModuleManager.getInstance(this).modules.forEach { module -> candidates.addAll(ModuleRootManager.getInstance(module).contentRoots) }
    return candidates.filter { it.isValid && it.isDirectory && it.hasWpiLibProjectLayout() }
}

/** Checks if the directory contains a `.wpilib` directory with a `wpilib_preferences.json` file. */
fun VirtualFile.hasWpiLibProjectLayout(): Boolean =
    findChild(wpiLibDirName)?.takeIf { it.isDirectory }?.findChild(wpiLibPreferencesFileName)?.let { !it.isDirectory } == true

/** Determines if the path is, or is in, a `.wpilib` directory. Used to detect changes that may change a project's WPILib status. */
fun isWpiLibProjectLayoutPath(path: String): Boolean
{
    val normalized = path.replace('\\', '/')
    return normalized.endsWith("/$wpiLibDirName") || normalized.contains("/$wpiLibDirName/")
}

/**
 * Keeps the FRC facet in sync with the WPILib project detection: adds the facet to the project's main module (i.e. the Gradle
 * `.main` module, or the module containing the WPILib project root) if the project is a WPILib project, and removes FRC facets
 * if it is not. For Gradle projects, this should only be called once the Gradle import has completed. Must be called on the EDT.
 */
fun Project.syncFrcFacetWithWpiLibDetection()
{
    if (isDisposed) return
    val existingFacets = ModuleManager.getInstance(this).modules.mapNotNull { module ->
        FacetManager.getInstance(module).getFacetByType(FrcFacet.FACET_TYPE_ID)?.let { module to it }
    }
    if (isWpiLibProject())
    {
        if (existingFacets.isNotEmpty()) return
        // For Gradle projects (i.e. all WPILib projects) the facet is added to the main source set module once the Gradle import has
        // created it. Adding it before, or during, the import does not work as the import replaces the modules.
        val isGradleProject = GradleSettings.getInstance(this).linkedProjectsSettings.isNotEmpty()
        val module = getMainModule()
            ?: (if (isGradleProject) null else ModuleManager.getInstance(this).modules.firstOrNull { it.isWpiLibModule() })
            ?: return
        logger.info("[FRC] WPILib project detected. Adding the FRC facet to module '${module.name}' of project '$name'")
        module.getOrAddFrcFacet()
    }
    else if (existingFacets.isNotEmpty())
    {
        logger.info("[FRC] Project '$name' is no longer a WPILib project (no $wpiLibDirName/$wpiLibPreferencesFileName). Removing the FRC facet.")
        runWriteAction {
            existingFacets.forEach { (module, facet) ->
                if (!module.isDisposed)
                {
                    val model = FacetManager.getInstance(module).createModifiableModel()
                    model.removeFacet(facet)
                    model.commit()
                }
            }
        }
    }
}
