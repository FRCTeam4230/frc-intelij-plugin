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

package net.javaru.iip.frc.services

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.util.getGradleBuildPsiFile
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement
import org.jetbrains.plugins.groovy.lang.psi.GroovyRecursiveElementVisitor
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral


// Keep an eye on:  com.intellij.externalSystem.DependencyModifierService
// It's experimental, but allows you to modify the build model such as adding a dependency
// It does not (yet) support modifying a Plugin version. But JetBrains seems to indicate
// that that is possibly planned:
// https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010674120-Programatically-Update-Plugin-Version-in-Gradle-Build-File

class FrcGradleService private constructor(val project: Project)
{
    private val LOG = logger<FrcGradleService>()
    companion object
    {
        fun getInstance(project: Project): FrcGradleService = project.service()
    }

    fun isIncludeDesktopSupport(): Boolean?
    {
        var result: Boolean? = null
        try
        {
            val psiFile = project.getGradleBuildPsiFile()
            if (psiFile == null)
            {
                LOG.info("[FRC] Could not find gradle project file to look up includeDesktopSupport setting.")
            }
            else
            {
                when (psiFile)
                {
                    is GroovyFile -> {
                        psiFile.accept(
                            object : GroovyRecursiveElementVisitor()
                            {
                                override fun visitElement(element: GroovyPsiElement)
                                {
                                    super.visitElement(element)
                                    if (element is GrVariable && element.name == "includeDesktopSupport")
                                    {
                                        val literal = element.children.asSequence().filter { it is GrLiteral }.firstOrNull()
                                        result = literal?.firstChild?.text?.toBoolean()
                                    }
                                }
                            })
                    }
                    else -> {
                        LOG.info("[FRC] non groovy gradle files is not yet supported. Unable to determine if includeDesktopSupport setting.")
                    }
                }
            }



        }
        catch (e: Exception)
        {
            LOG.info("[FRC] an exception occurred when checking includeDesktopSupport setting: $e")
        }
        return result
    }
}