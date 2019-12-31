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

@file:Suppress("unused")

package net.javaru.iip.frc.wpilib

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.util.isLibraryPresent


enum class CommandLibraryVersioning 
{
    Neither, Version1, Version2, Both;
    
    fun isVersion1Present(): Boolean = this == Version1 || this == Both
    fun isVersion2Present(): Boolean = this == Version2 || this == Both
}

fun getCommandBasedLibraryStatus(module: Module?): CommandLibraryVersioning = getCommandBasedLibraryStatus(module?.project)

fun getCommandBasedLibraryStatus(project: Project?): CommandLibraryVersioning
{
    val v1Present = isLibraryPresent(project, WpiLibConstants.COMMAND_BASED_VERSION_CHECK_V1_FQN)
    val v2Present = isLibraryPresent(project, WpiLibConstants.COMMAND_BASED_VERSION_CHECK_V2_FQN)
    if (v1Present && !v2Present) return CommandLibraryVersioning.Version1
    if (v2Present && !v1Present) return CommandLibraryVersioning.Version2
    if (v1Present && v2Present) return CommandLibraryVersioning.Both
    return CommandLibraryVersioning.Neither
}

fun isVersion1CommandBaseLibAttached(module: Module?): Boolean = isVersion1CommandBaseLibAttached(module?.project)
fun isVersion1CommandBaseLibAttached(project: Project?): Boolean = getCommandBasedLibraryStatus(project).isVersion1Present()
fun isVersion2CommandBaseLibAttached(module: Module?): Boolean = isVersion2CommandBaseLibAttached(module?.project)
fun isVersion2CommandBaseLibAttached(project: Project?): Boolean = getCommandBasedLibraryStatus(project).isVersion2Present()
