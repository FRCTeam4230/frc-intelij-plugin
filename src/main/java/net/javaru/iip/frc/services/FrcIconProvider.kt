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

import com.intellij.icons.AllIcons
import com.intellij.ide.FileIconProvider
import com.intellij.ide.IconProvider
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.PsiManager
import icons.FrcIcons
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.settings.FrcApplicationSettings
import org.apache.commons.io.FilenameUtils
import java.util.*
import javax.swing.Icon

// extends    com.intellij.ide.IconProvider                             to override icons in Project view
// implements com.intellij.ide.FileIconProvider                         to override icons in Project view
// implements com.intellij.openapi.vcs.changes.FilePathIconProvider     to override icons in VCS (Git, etc.) views
//
class FrcIconProvider: IconProvider(), FileIconProvider, DumbAware
{
    private val LOG = logger<FrcIconProvider>()


    // From IconProvider
    override fun getIcon(element: PsiElement, flags: Int): Icon?
    {
        try
        {
            if (!(FrcApplicationSettings.getInstance().provideCustomFileIcons && element.project.isFrcFacetedProject())) return null

            if (element is PsiDirectory)
            {
                val vFile = element.virtualFile


                return when (vFile.name)
                {
                    ".idea" ->  FrcIcons.FileAndDirTypes.IdeaDir
                    ".run" ->   FrcIcons.FileAndDirTypes.RunDir
                    ".vscode" -> FrcIcons.FileAndDirTypes.VsCodeDir // FrcIcons.FileAndDirTypes.VSCode
                    ".wpilib" -> FrcIcons.FileAndDirTypes.WpiLibDir // FrcIcons.WpiLib.WPI_LIB_16
                    "vendordeps" ->  FrcIcons.FileAndDirTypes.VendordepsDir// AllIcons.Nodes.PpLibFolder  //AllIcons.General.ProjectStructure
                    else -> null
                }
            }
            else if (element is PsiFile)
            {
                val vFile = element.virtualFile
                val baseName = FilenameUtils.removeExtension(vFile.name).lowercase(Locale.getDefault())
                return when (baseName)
                {
                    "wpilib-license" -> FrcIcons.FileAndDirTypes.License
                    "license" -> FrcIcons.FileAndDirTypes.License
                    else -> null
                }
            }
        }
        catch (t: Throwable)
        {
            LOG.info("[FRC] An exception occurred in the FrcIconProvider.getIcon(element: PsiElement, flags: Int) method from IconProvider: $t", t)
        }

        return null
    }

    // From FilePathIconProvider for VCS views
    /*override fun getIcon(filePath: FilePath, project: Project?): Icon?
    {
        try
        {
            if (project != null)
            {
                val file = filePath.virtualFile ?: return null

                val psiFileSystemItem: PsiFileSystemItem? = if (file.isDirectory)
                {
                    PsiManager.getInstance(project).findDirectory(file)
                }
                else
                {
                    PsiManager.getInstance(project).findFile(file)
                }
                if (psiFileSystemItem != null)
                {
                    return getIcon(psiFileSystemItem, 0  flags are ignored )
                }
            }
        }
        catch (t: Throwable)
        {
            LOG.info("[FRC] An exception occurred in the FrcIconProvider.getIcon(filePath: FilePath, project: Project?) method fromFilePathIconProvider: $t", t)
        }
        return null
    }*/

    // From FileIconProvider
    override fun getIcon(file: VirtualFile, flags: Int, project: Project?): Icon?
    {
        try
        {
            if (project != null)
            {
                val psiFileSystemItem: PsiFileSystemItem? = if (file.isDirectory)
                {
                    PsiManager.getInstance(project).findDirectory(file)
                }
                else
                {
                    PsiManager.getInstance(project).findFile(file)
                }

                if (psiFileSystemItem != null)
                {
                    return getIcon(psiFileSystemItem, flags)
                }
            }
        }
        catch (t: Throwable)
        {
            LOG.info("[FRC] An exception occurred in the FrcIconProvider.getIcon(file: VirtualFile, flags: Int, project: Project?) method from FileIconProvider: $t", t)
        }
        return null
    }

}