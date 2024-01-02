/*
 * Copyright 2015-2022 the original author or authors.
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
import net.javaru.iip.frc.wizard.FrcProjectWizardData
import org.jetbrains.plugins.groovy.lang.psi.GroovyFile
import org.jetbrains.plugins.groovy.lang.psi.GroovyPsiElement
import org.jetbrains.plugins.groovy.lang.psi.GroovyRecursiveElementVisitor
import org.jetbrains.plugins.groovy.lang.psi.api.statements.GrVariable
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.literals.GrLiteral


// Keep an eye on:  com.intellij.externalSystem.DependencyModifierService
// It's experimental, but allows you to modify the build model such as adding a dependency
// It does not (yet) support modifying a Plugin version. But JetBrains seems to indicate
// that that is possibly planned:
// https://intellij-support.jetbrains.com/hc/en-us/community/posts/360010674120-Programatically-Update-Plugin-Version-in-Gradle-Build-File

class FrcGradleService private constructor(val project: Project)
{
    private val logger = logger<FrcGradleService>()
    companion object
    {
        fun getInstance(project: Project): FrcGradleService = project.service()

        fun getRobotProjectTypeInfo(project: Project) = getInstance(project).getRobotProjectTypeInfo()

        fun getRobotProjectTypeInfo(frcProjectWizardData: FrcProjectWizardData) = RobotProjectTypeInfo(frcProjectWizardData)
    }

    fun getRobotProjectTypeInfo(): RobotProjectTypeInfo
    {
        // To reduce the parsing work done, and improve performance, we set these intelligently
        val gradleService = FrcGradleService.getInstance(project)
        val rio = gradleService.hasRoborioDeployTarget()
        val xrp = if (rio == true) false else gradleService.isXrpProject()
        val romi = if (rio == true || xrp == true) false else gradleService.isRomiProject()
        val desk = gradleService.isIncludeDesktopSupport()
        return RobotProjectTypeInfo(rio, romi, xrp, desk)
    }

    fun isIncludeDesktopSupport(): Boolean?
    {
        var result: Boolean? = null
        try
        {
            val psiFile = project.getGradleBuildPsiFile()
            if (psiFile == null)
            {
                logger.info("[FRC] Could not find gradle project file to look up includeDesktopSupport setting.")
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
                        logger.info("[FRC] non groovy gradle files is not yet supported. Cloud not lookup includeDesktopSupport setting.")
                    }
                }
            }
        }
        catch (e: Exception)
        {
            logger.info("[FRC] an exception occurred when checking includeDesktopSupport setting: $e")
        }
        return result
    }

    fun hasRoborioDeployTarget(): Boolean? = gradleFileHasTextReferenceOf("deploy.targets.roborio", useContains = false)

    /** Indicates a project is a non-roboRIO project. That is, it is (likely) either a Romi or XRP project. */
    @Suppress("unused")
    fun isNonRoboRioProject(): Boolean? = hasRoborioDeployTarget()?.not()

    fun isXrpProject(): Boolean? = gradleFileHasTextReferenceOf("addXRPClient()", true)

    fun isRomiProject(): Boolean? = gradleFileHasTextReferenceOf("10.0.0.2", true)

    private fun gradleFileHasTextReferenceOf(targetText: String, useContains: Boolean): Boolean?
    {
        var result: Boolean? = null
        try
        {
            val psiFile = project.getGradleBuildPsiFile()
            if (psiFile == null)
            {
                logger.info("[FRC] Could not find gradle project file to look up hasRoborioDeployTarget setting.")
            }
            else
            {
                when (psiFile)
                {
                    is GroovyFile ->
                    {
                        result = false
                        psiFile.accept(
                            object : GroovyRecursiveElementVisitor()
                            {
                                override fun visitReferenceExpression(referenceExpression: GrReferenceExpression)
                                {
                                    super.visitReferenceExpression(referenceExpression)

                                    if (useContains)
                                    {
                                        if (referenceExpression.text.contains(targetText))
                                        {
                                            result = true
                                        }
                                    }
                                    else
                                    {
                                        if (referenceExpression.text == targetText)
                                        {
                                            result = true
                                        }
                                    }

                                }
                            }
                                      )
                    }
                    else          ->
                    {
                        logger.info("[FRC] non groovy gradle files is not yet supported. Unable to determine if includeDesktopSupport setting.")
                    }
                }
            }
        }
        catch (e: Exception)
        {
            logger.info("[FRC] an exception occurred when checking includeDesktopSupport setting: $e")
            result = null
        }
        return result
    }

    /**
     * To get an instance:
     *  - `FrcGradleService.getRobotProjectTypeInfo(project)`
     *  - `FrcGradleService.getInstance(project).getRobotProjectTypeInfo()`
     */
    data class RobotProjectTypeInfo  constructor(
        val isRoboRIOProject: Boolean?,
        val isRomiProject: Boolean?,
        val isXrpProject: Boolean?,
        val hasIncludeDesktopSupport: Boolean?
                                                  )
    {
        constructor(frcProjectWizardData: FrcProjectWizardData): this(frcProjectWizardData.isRoboRioRobotTemplate,
                                                                      frcProjectWizardData.isRomiTemplate,
                                                                      frcProjectWizardData.isXrpTemplate,
                                                                      frcProjectWizardData.enableDesktopSupport,
                                                                     )

        companion object
        {
            fun create(frcProjectWizardData: FrcProjectWizardData): RobotProjectTypeInfo = RobotProjectTypeInfo(frcProjectWizardData)
        }
    }
}


