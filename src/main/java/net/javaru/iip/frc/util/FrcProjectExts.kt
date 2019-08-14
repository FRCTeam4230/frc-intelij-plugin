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

package net.javaru.iip.frc.util

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.plugins.gradle.settings.GradleSettings


// Note: There are also some Project Extension functions in FrcFacet.kt

private val LOG = Logger.getInstance("#net.javaru.iip.frc.util.ProjectExts")

fun Project?.isAntBasedFrcProject(): Boolean
{
    return if (this == null) 
        false
    else 
    {
        
        // TODO - Need to implement this feature
//        try
//        {
//            val modules = ModuleManager.getInstance(this).modules
//            for (module in modules)
//            {
//                val file: VirtualFile =  module.
//            }
//        }
//        catch(e:Exception)
//        {
//            LOG.warn("An exception occurred when checking if a project in is an Ant Based FRC Project. Cause Summary: $e", e)
//            return false
//        }
        return false
    }
}

fun Project?.isGradleProject(): Boolean
{
    return try
    {
        // copied from   org/jetbrains/plugins/gradle/execution/GradleConsoleFilterProvider.java:59
        // Need to determine if there is a better way to do this.
        // I've asked here: https://intellij-support.jetbrains.com/hc/en-us/community/posts/360004424640-Determine-if-a-project-is-a-Gradle-Project
        if (this == null) false else !GradleSettings.getInstance(this).linkedProjectsSettings.isEmpty()
    }
    catch (e: Exception)
    {
        LOG.warn("An exception occurred when checking if a project in is a Gradle Based Project. Cause Summary: $e", e)
        false
    }
}