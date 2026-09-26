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

import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.wpilib.vendordeps.vendordepsDirName
import org.jetbrains.annotations.Contract

private val gradleBuildFileNames = listOf("build.gradle", "build.gradle.kts")

/**
 * Determines if the project is a WPILib robot project. That is, if it has the FRC facet, or if its root directory
 * (or a module content root) contains a Gradle build file, a `.wpilib` directory, and a `vendordeps` directory.
 * Unlike the facet check, which requires the Gradle import to have completed, the file based check works as soon as the
 * project is opened. It uses only the VFS (not indexes), so it is safe to call in dumb mode.
 */
@Contract("null -> false")
fun Project?.isWpiLibProject(): Boolean
{
    if (this == null || this.isDisposed || this.isDefault) return false
    if (this.isFrcFacetedProject()) return true
    return findWpiLibProjectRootDirs().isNotEmpty()
}

/** Finds the directories, from the project directory and module content roots, that have the layout of a WPILib robot project. */
fun Project.findWpiLibProjectRootDirs(): List<VirtualFile>
{
    val candidates = linkedSetOf<VirtualFile>()
    guessProjectDir()?.let { candidates.add(it) }
    ModuleManager.getInstance(this).modules.forEach { module -> candidates.addAll(ModuleRootManager.getInstance(module).contentRoots) }
    return candidates.filter { it.isValid && it.isDirectory && it.hasWpiLibProjectLayout() }
}

/** Checks if the directory contains a Gradle build file, a `.wpilib` directory, and a `vendordeps` directory. */
fun VirtualFile.hasWpiLibProjectLayout(): Boolean =
    gradleBuildFileNames.any { findChild(it)?.isDirectory == false } &&
        findChild(wpiLibDirName)?.isDirectory == true &&
        findChild(vendordepsDirName)?.isDirectory == true
