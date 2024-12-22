/*
 * Copyright 2015-2024 the original author or authors.
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

package net.javaru.iip.frc.facet.framework.detector

import com.intellij.framework.detection.DetectedFrameworkDescription
import com.intellij.framework.detection.FileContentPattern
import com.intellij.framework.detection.FrameworkDetectionContext
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.java.library.JavaLibraryUtil
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.patterns.ElementPattern
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.util.indexing.FileContent
import net.javaru.iip.frc.facet.FrcFacetConfiguration
import net.javaru.iip.frc.run.createAllRunDebugConfigurations
import net.javaru.iip.frc.util.tryQuietlyIf
import net.javaru.iip.frc.wpilib.WpiLibConstants.*
import net.javaru.iip.frc.wpilib.WpiLibHelpers


abstract class AbstractWpiLibLibraryFrcFrameworkDetector(detectorId: String, detectorVersion: Int) : FrcAbstractFrameworkDetector(detectorId, detectorVersion)
{
    private val logger = logger<WpiLibHelpers>()

    
    
    override fun createSuitableFilePattern(): ElementPattern<FileContent> = FileContentPattern.fileContent()

    /**
     * Override this method if only one facet of this type are allowed in a single module
     *
     * @param files files accepted by detector's filter
     *
     * @return configuration for detected facet
     */
    override fun createConfiguration(files: Collection<VirtualFile>): FrcFacetConfiguration?
    {
        logger.debug("[FRC] createConfiguration(files) called in RobotSubclassFrcFrameworkDetector")
        return facetType.createDefaultConfiguration()
    }

    override fun detect(newFiles: MutableCollection<out VirtualFile>, context: FrameworkDetectionContext): MutableList<out DetectedFrameworkDescription>
    {
        val project = context.project
        logger.info("[FRC] Checking if project '${project?.name ?: "<null-project>"} is an FRC project")

        // Going to disable subclass checking for now. Library detection should work fine
        val detectedFrameworks = detectViaWpiLibPresence(project, newFiles, context)/*.let {
            // Check for Robot Subclasses
            if (it.isEmpty()) detectViaRobotSubclass(project, newFiles, context) else it
        }*/
        
        tryQuietlyIf( { detectedFrameworks.isNotEmpty()} ) { 
            logger.debug("[FRC] Creating FRC run/debug Configurations for detected FRC framework")
            createAllRunDebugConfigurations(project)
        }
        
        if (detectedFrameworks.isNotEmpty())
        {
            logger.info("[FRC] FRC Framework detected for  project '${project?.name ?: "<null-project>"}.")
        }
        else
        {
            logger.info("[FRC] FRC Framework NOT detected for  project '${project?.name ?: "<null-project>"}.")
        }
        return detectedFrameworks
    }

    @Suppress("UnstableApiUsage")
    private fun detectViaWpiLibPresence(
        project: Project?,
        newFiles: MutableCollection<out VirtualFile>,
        context: FrameworkDetectionContext
                                       ): MutableList<out DetectedFrameworkDescription>
    {
        logger.debug("[FRC] Checking if project '${project?.name ?: "<null-project>"} has WPI Lib libraries or classes on the classpath.")
        if (project == null) return mutableListOf()
        return try
        {
            if (
                // https://plugins.jetbrains.com/docs/intellij/psi-cookbook.html#how-do-i-check-the-presence-of-a-jvm-library
                JavaLibraryUtil.hasAnyLibraryJar(
                    project, listOf(
                        WPILIB_JAVA_MAVEN_COORDINATES,
                        WPILIB_HAL_JAVA_MAVEN_COORDINATES,
                        WPILIB_WPIUTIL_JAVA_MAVEN_COORDINATES,
                        WPILIB_XRP_MAVEN_COORDINATES
                                   )
                                                ) ||
                JavaLibraryUtil.hasLibraryClass(project, ROBOT_BASE_FQN) ||
                JavaLibraryUtil.hasLibraryClass(project, TIMED_ROBOT_FQN) ||
                JavaLibraryUtil.hasLibraryClass(project, SAMPLE_ROBOT_FQN)
            )
            {
                logger.info("[FRC] WPILib libraries or classes detected. Creating FRC Framework Description")
                val files = createFilesList(newFiles)
                context.createDetectedFacetDescriptions(this, files)
            }
            else
            {
                mutableListOf()
            }
        }
        catch (e: ProcessCanceledException)
        {
            throw e
        }
        catch (e: Exception)
        {
            logger.warn("[FRC] An exception occurred while checking for WPILib libraries or classes for Framework Detection. Cause: $e", e)
            mutableListOf()
        }
    }

    private fun createFilesList(newFiles: MutableCollection<out VirtualFile>): MutableCollection<out VirtualFile>
    {
        // 1 Check for a Robot class
        val robotFile = newFiles.firstOrNull { it.name.equals("Robot.java", ignoreCase = true) || it.name.equals("Robot.kt", ignoreCase = true) }
        if (robotFile != null) return mutableListOf(robotFile)

        // 2 Get a List of files with the name Robot in it; if non found, just return the full list
        val robotFiles = newFiles.filter { it.name.contains("robot", ignoreCase = true) }
        if (robotFiles.isEmpty()) return newFiles
        
        // 3 Filter out RobotContainer class
        val robotFilesNonContainer =
                robotFiles.filter { !it.name.equals("RobotContainer.java", ignoreCase = true) && !it.name.equals("RobotContainer.kt", ignoreCase = true) }
        
        // Return the first class from the non container list, unless it's empty, then return the first from the original robot files list
        val file = robotFilesNonContainer.firstOrNull() ?: robotFiles.firstOrNull()
        return if (file != null) mutableListOf(file) else newFiles
    }

    private fun detectViaRobotSubclass(
        project: Project?,
        newFiles: MutableCollection<out VirtualFile>,
        context: FrameworkDetectionContext
                                   ): MutableList<out DetectedFrameworkDescription>
    {
        try
        {
            logger.debug("[FRC] Checking if project '${project?.name ?: "<null-project>"} has any class that implements an FRC Robot class.")
            
            val foundFiles = detectRobotClasses(project, newFiles, context)
            if (foundFiles.isNotEmpty()) {
                logger.info("[FRC] Subclass of an FRC Robot class found. Creating FRC Framework Description")
            }
            return if (foundFiles.isEmpty()) mutableListOf() else context.createDetectedFacetDescriptions(this, foundFiles)
        }
        catch (e: ProcessCanceledException)
        {
            throw e
        }
        catch (e: Exception)
        {
            logger.warn("[FRC] An exception occurred when checking for FRC Framework. Cause Summary: $e", e)
            return mutableListOf()
        }
    }
    
    private fun detectRobotClasses(
        project: Project?,
        newFiles: Collection<VirtualFile>,
        @Suppress("UNUSED_PARAMETER") context: FrameworkDetectionContext): List<VirtualFile>
    {
        val foundFiles: MutableList<VirtualFile> = ArrayList(newFiles.size)

        if (project != null)
        {
            for (virtualFile in newFiles)
            {
                val psiFile = PsiManager.getInstance(project).findFile(virtualFile)
                if (psiFile is PsiJavaFile)
                {
                    if (checkIfRobotClass(psiFile, project))
                    {
                        foundFiles.add(virtualFile)
                        // Once we find a file, we can break out of the loop to improve performance.
                        break
                    }
                }
            }
        }
        else
        {
            for (virtualFile in newFiles)
            {
                if (checkIfExtendsRobot(virtualFile))
                {
                    foundFiles.add(virtualFile)
                    // Once we find a file, we can break out of the loop to improve performance.
                    break
                }
            }
        }

        return foundFiles
    }
    
     private fun checkIfExtendsRobot(virtualFile: VirtualFile?): Boolean
    {
        try
        {
            if (virtualFile == null)
            {
                return false
            }
            val content = String(virtualFile.contentsToByteArray())
            val matcher = EXTENDS_A_ROBOT_REGEX.matcher(content)
            return matcher.find()
        }
        catch (e: ProcessCanceledException)
        {
            throw e
        }
        catch (e: Exception)
        {
            logger.warn("[FRC] An exception occurred when checking if VirtualFile '" + virtualFile!!.path + " extends Robot'. Cause Summary: " + e)
            return false
        }
    }


    private fun checkIfRobotClass(psiFile: PsiJavaFile?, project: Project): Boolean
    {
        try
        {
            if (psiFile == null || project.isDisposed)
            {
                return false
            }
            val classes = psiFile.classes
            if (logger.isTraceEnabled)
            {
                logger.trace("[FRC] FrameworkDetector checking psiFile '" + psiFile.name + "'. Its classes are: " + classes.contentToString())
            }


            // This will only work if the WpiLib classes are on the classpath (i.e. added as a library)
            return hasFrcSuperClass(classes, project)
        }
        catch (e: ProcessCanceledException)
        {
            throw e
        }
        catch (e: Exception)
        {
            logger.warn(
                ("[FRC] An exception occurred when checking if PsiJavaFile file '$psiFile' ('" + psiFile!!
                    .virtualFile.path
                        + "') is a robot class. Cause Summary: " + e), e
                    )
            return false
        }
    }


    /**
     * Checks to see if any classes in a group is a subclass of one of the WPI Lib Robot classes. The code
     * does traverse all the way up the inheritance hierarchy; so a class does not directly have
     * to extend one of the Robt classes to be 'discovered'.
     * **This will only work if the WpiLib classes are on the classpath (i.e. added as a library).**
     *
     * @param classes the classes to check
     *
     * @param project the project
     * @return true if one of the classes extends an FRC robot class
     */
    private fun hasFrcSuperClass(classes: Array<PsiClass>, project: Project): Boolean
    {
        for (psiClass in classes)
        {
            if (project.isDisposed)
            {
                if (logger.isDebugEnabled)
                {
                    logger.debug("[FRC] FrameworkDetector psiClass '" + psiClass.qualifiedName + "' does NOT have an FRC super class")
                }
                return false
            }
            if (hasFrcSuperClass(psiClass, project, 0))
            {
                if (logger.isDebugEnabled)
                {
                    logger.debug("[FRC] FrameworkDetector psiClass '" + psiClass.qualifiedName + "' DOES have an FRC super class")
                }
                return true
            }
        }
        return false
    }


    /**
     * Checks to see if a class is a subclass of one of the WPI Lib Robot classes. The code
     * does traverse all the way up the inheritance hierarchy; so a class does not directly have
     * to extend one of the Robt classes to be 'discovered'.
     * **This will only work if the WpiLib classes are on the classpath (i.e. added as a library).**
     *
     * @param psiClass       the class to check
     * @param project        the containing project
     * @param recursionCount the number of times the method has been called. Outside calls should set to zero
     *
     * @return true if the class extends an FRC robot class
     */
    private fun hasFrcSuperClass(psiClass: PsiClass?, project: Project, recursionCount: Int): Boolean
    {
        try
        {
            if (psiClass == null || project.isDisposed)
            {
                return false
            }

            if (ROBOT_SUPER_CLASSES_FQN.contains((psiClass.qualifiedName)))
            {
                if (logger.isTraceEnabled)
                {
                    logger.trace("[FRC] FrameworkDetector psiClass '" + psiClass.qualifiedName + "' IS an FRC/WpiLib base robot class. Returning true hasFrcSuperClass().")
                }
                return true
            }
            else if (extendsFrcRobot(psiClass))
            {
                if (logger.isTraceEnabled)
                {
                    logger.trace("[FRC] FrameworkDetector psiClass '" + psiClass.qualifiedName + "' extends an FRC/WpiLib base robot class. Returning true for hasFrcSuperClass.")
                }
                return true
            }
            else
            {
                val superClass = psiClass.superClass
                if (superClass == null || "java.lang.Object" == superClass.qualifiedName)
                {
                    if (logger.isTraceEnabled)
                    {
                        logger.trace("[FRC] FrameworkDetector psiClass '" + psiClass.qualifiedName + "' is not, nor does it extend, an FRC/WpiLib base robot class. It does not have a Superclass (other than java.lang.Object). Returning false for hasFrcSuperClass() for this class.")
                    }
                    return false
                }
                if (logger.isTraceEnabled)
                {
                    logger.trace("[FRC] FrameworkDetector psiClass '" + psiClass.qualifiedName + "' is not, nor does it extend, an FRC/WpiLib base robot class. Checking it super class " + superClass)
                }
                // Issue 16: A Stackoverflow occurred of well over 1000 calls to the below recursive call.
                //           Not sure what class caused it. But to prevent the issue, we limit the traversal
                //           of super classes to a depth of 16, which is way more than is ever likely for a robot project
                val newCount = recursionCount + 1
                return (newCount <= 16) && hasFrcSuperClass(superClass, project, recursionCount)
            }
        }
        catch (e: ProcessCanceledException)
        {
            throw e
        }
        catch (e: Exception)
        {
            logger.warn(
                ("[FRC] An exception occurred when checking for FRC Super Class on psiClass '" + psiClass!!.qualifiedName + "'. Cause Summary: "
                        + e), e
                    )
            return false
        }
    }


    private fun extendsFrcRobot(psiClass: PsiClass?): Boolean
    {
        if (psiClass == null)
        {
            return false
        }

        val extendsListTypes = psiClass.extendsListTypes
        for (extendsListType in extendsListTypes)
        {
            val extendsClassName = extendsListType.className
            if (ROBOT_SUPER_CLASSES_NAMES.contains(extendsClassName))
            {
                return true
            }
        }
        return false
    }
    
}
