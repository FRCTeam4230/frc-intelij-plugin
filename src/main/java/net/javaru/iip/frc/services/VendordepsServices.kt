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
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeEvent
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.psi.FrcGeneralChangePsiTreeChangeListenerAdapter
import net.javaru.iip.frc.util.markGradleProjectAsNeedingReimport
import org.jetbrains.annotations.Contract

const val vendordepsDirName = "vendordeps"

class VendordepsFileListener private constructor(val project: Project)
{
    private val logger = logger<VendordepsFileListener>()

    companion object
    {
        @JvmStatic
        fun getInstance(project: Project) = project.service<VendordepsFileListener>()
    }
    
    init
    {
        logger.debug{"[FRC] Initializing VendordepsFileListener"}

        PsiManager.getInstance(project).addPsiTreeChangeListener(VendordepsPsiTreeChangeListener(project), FrcApplicationDisposableService.getInstance()) 
        //  NOTE: A BulkFileListener is only notified during write actions. So we are not notified 
        //        of the changes until a save or delete occurs, No notification occurs while editing.
        //        The above PsiTreeChangeListener notifies us about edit changes
        project.messageBus.connect()
            .subscribe(VirtualFileManager.VFS_CHANGES,
                       object : BulkFileListener
                       {
                           override fun after(events: List<VFileEvent>)
                           {
                               //logger.trace {"[FRC] VendordepsFileListener.AFTER called with ${events.size} events"}
                               for (event in events)
                               {
                                   val virtualFile = event.file
                                   if (virtualFile.isVendordepsJsonFile(project))
                                   {
                                       logger.debug{"[FRC] VendordepsFileListener.AFTER: Change detected to the 'vendordeps' file: ${virtualFile?.name}"}
                                       project.markGradleProjectAsNeedingReimport(scheduleForAutoReimport = true)
                                       break // we only want/need to do the import once in the event multiple files were changed.
                                   }
                               }
                           }

                           override fun before(events: MutableList<out VFileEvent>)
                           {
                               //logger.trace {"[FRC] VendordepsFileListener.BEFORE called with ${events.size} events"}
                               for (event in events)
                               {
                                   val virtualFile = event.file
                                   // We only want to react to deletions in the before method. Note: Although there is a VirtualFile.exists 
                                   // method, we don't want to use it as it reports true since it is the state of the file before the deletion
                                   if (virtualFile.isVendordepsJsonFile(project) && !VfsUtil.virtualToIoFile(virtualFile!!).exists()) 
                                   {
                                       logger.debug{"[FRC] VendordepsFileListener.BEFORE: Change detected to the 'vendordeps' file: ${virtualFile.name}  io-file exists: ${VfsUtil.virtualToIoFile(virtualFile).exists()}"}
                                       project.markGradleProjectAsNeedingReimport(scheduleForAutoReimport = true)
                                       break // we only want/need to do the import once in the event multiple files were changed.
                                   }
                               }
                           }
                       })
    }
}

class VendordepsPsiTreeChangeListener(private val project: Project): FrcGeneralChangePsiTreeChangeListenerAdapter()
{
    override fun handleChange(event: PsiTreeChangeEvent)
    {
        if (event.isVendordepsEvent())
        {
            project.markGradleProjectAsNeedingReimport(scheduleForAutoReimport = false)
        }
    }

    private fun PsiTreeChangeEvent.isVendordepsEvent() =
        //file?.containingDirectory?.name == vendordepsDirName && file?.name?.endsWith(".json", ignoreCase = true) == true
        file.isVendordepsJsonFile(project)
}

@Contract("null,_ -> false")
fun PsiFile?.isVendordepsJsonFile(project: Project) : Boolean = this?.virtualFile.isVendordepsJsonFile(project)

/**
 * Determines if the `VirtualFile` is a `vendordeps.json` file, returning false if the `VirtualFile` is null.
 * If a project is provided (i.e. not null), then the file must exist within the project's content and it must be an FRC Faceted project.
 */
@Contract("null,_ -> false")
fun VirtualFile?.isVendordepsJsonFile(project: Project?) : Boolean
{
    if (this == null) return false
    val isWithinProject = 
        if (project == null) 
            true 
        else 
            project.isFrcFacetedProject() &&  ProjectFileIndex.getInstance(project).isInContent(this)
    return isWithinProject && 
        this.parent.name == vendordepsDirName && 
        this.name.endsWith(".json", ignoreCase = true)
}

// Pre IJ v2019.3, need to use StartupActivity rather than StartupActivity.Background (and change the plugin.xml element to match)
class VendordepsFileListenerStartupActivity : StartupActivity.Background
{
    override fun runActivity(project: Project)
    {
        if (project.isFrcFacetedProject())
        {
//            StartupManager.getInstance(project).runWhenProjectIsInitialized() {
            VendordepsFileListener.getInstance(project)
//            }
        }
    }
}
