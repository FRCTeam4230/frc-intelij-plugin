/*
 * Copyright 2015-2020 the original author or authors
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

package net.javaru.iip.frc.services

import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.project.ExternalProjectRefreshCallback
import com.intellij.openapi.externalSystem.service.project.ProjectDataManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.i18n.FrcBundle.message
import net.javaru.iip.frc.notify.FrcNotificationType
import net.javaru.iip.frc.notify.FrcNotifications
import net.javaru.iip.frc.util.asDate
import net.javaru.iip.frc.util.reimportGradleProject
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.atomic.AtomicReference


class VendordepsFileListener private constructor(val project: Project)
{
    private val LOG = Logger.getInstance(VendordepsFileListener::class.java)

    private val timer = Timer("VendordepsFileListener do Gradle Reimport Timer")
    private var taskRef: AtomicReference<GradleReimportTask> = AtomicReference()

    companion object
    {
        @JvmStatic
        fun getInstance(project: Project) = project.service<VendordepsFileListener>()
    }
    
    init
    {
        LOG.info("[FRC] Initializing VendordepsFileListener")
        project.messageBus.connect()
            .subscribe(VirtualFileManager.VFS_CHANGES,
                       object : BulkFileListener
                       {
                           override fun after(events: List<VFileEvent>)
                           {
                               //LOG.trace("[FRC] VendordepsFileListener.AFTER called with ${events.size} events")
                               for (event in events)
                               {
                                   val file = event.file
                                   if (file != null &&
                                       ProjectFileIndex.getInstance(project).isInContent(file) &&
                                       file.parent.name == "vendordeps" &&
                                       file.name.endsWith(".json", ignoreCase = true))
                                   {
                                       // For now we react to any change to any json file  in the the directory
                                       LOG.debug("[FRC] VendordepsFileListener.AFTER: Change detected to the 'vendordeps' file: ${file.name}")
                                       scheduleGradleReimport()
                                       break // we only want/need to do the import once in the event multiple files were changed.
                                   }
                               }
                           }

                           override fun before(events: MutableList<out VFileEvent>)
                           {
                               //LOG.trace("[FRC] VendordepsFileListener.BEFORE called with ${events.size} events")
                               for (event in events)
                               { 
                                   val file = event.file
                                   if (file != null &&
                                       ProjectFileIndex.getInstance(project).isInContent(file) &&
                                       file.parent.name == "vendordeps" &&
                                       file.name.endsWith(".json", ignoreCase = true) &&
                                       !VfsUtil.virtualToIoFile(file).exists()) // We only want to react to deletions in the before method. Note: Although there is a VirtualFile.exists method, we dont want to use it as it reports true since it is the state of the file before the deletion
                                   {
                                       LOG.debug("[FRC] VendordepsFileListenerBEFORE: Change detected to the 'vendordeps' file: ${file.name}  io-file exists: ${VfsUtil.virtualToIoFile(file).exists()}")
                                       scheduleGradleReimport()
                                       break // we only want/need to do the import once in the event multiple files were changed.
                                   }
                               }
                           }

                           // If two files are added to the vendordeps directory, sometimes they come through as two calls to after, not as two events in a single call.
                           // If we direct called the reimport code in the after function, two re-imports get started in rapid succession, resulting in
                           // two "successfully applied vendordeps changes" messages... not a good user experience
                           // So we delay the import ny a second. If another one come along, we cancel the first and reschedule a new one
                           
                           private fun scheduleGradleReimport()
                           {
                               val previousTask: GradleReimportTask? = taskRef.get()
                               
                               if (previousTask != null && !previousTask.importHasStarted)
                               {
                                   previousTask.cancel()
                                   LOG.debug("[FRC] canceled previously scheduled GradleReimportTask.")
                               }
                               LOG.debug("[FRC] scheduling GradleReimportTask.")
                               val task = GradleReimportTask(project)
                               taskRef.set(task)
                               timer.schedule(task, LocalDateTime.now().plusSeconds(1).asDate())
                           }
                       })
    }

    
    
}

// For IJ v2019.3+ we can/should use StartupActivity.Background (and change the plugin.xml element to <backgroundPostStartupActivity>)
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

class GradleReimportTask(private val project: Project): TimerTask()
{
    var importHasStarted = false
    private val LOG = Logger.getInstance(GradleReimportTask::class.java)
    
    override fun run()
    {
        importHasStarted = true
        LOG.debug("[FRC] Starting GradleReimportTask")
        doGradleReimport()
    }

    private fun doGradleReimport(recursiveCount: Int = 0)
    {
        project.reimportGradleProject(object : ExternalProjectRefreshCallback
                                      {
                                          val subtitle = message("frc.notification.vendordeps.reimport.title")

                                          override fun onSuccess(externalProject: DataNode<ProjectData>?)
                                          {
                                              if (externalProject != null)
                                              {
                                                  // This first line is taken from the Default callback. Not sure we need it. But it can't hurt
                                                  ServiceManager.getService(ProjectDataManager::class.java)
                                                      .importData(externalProject, project, false)

                                                  FrcNotifications
                                                      .notify(FrcNotificationType.GENERAL_INFO,
                                                              message("frc.notification.vendordeps.reimport.success"),
                                                              subtitle,
                                                              project)
                                              }
                                          }

                                          override fun onFailure(errorMessage: String, errorDetails: String?)
                                          {
                                              if (errorMessage.startsWith("Another 'Reimport project' task is currently running"))
                                              {
                                                  // Since the import suspends indexing, we can wait until we are smart again 
                                                  // and then do the reimport to make sure the vendordeps changes where taken
                                                  // but we only do this a few times to prevent an infinite loop
                                                  if (recursiveCount <= 3)
                                                  {
                                                      DumbService.getInstance(project)
                                                          .runWhenSmart() { doGradleReimport(recursiveCount + 1) }
                                                  }
//                                                                         else if (recursiveCount == 4)
//                                                                         {
//                                                                             FrcNotifications
//                                                                                 .notify(FrcNotificationType.ACTIONABLE_WARN,
//                                                                                         message("frc.notification.vendordeps.reimport.retry"),
//                                                                                         subtitle,
//                                                                                         project)
//                                                                         }
                                              }
                                              else
                                              {
                                                  FrcNotifications
                                                      .notify(FrcNotificationType.ACTIONABLE_ERROR,
                                                              message("frc.notification.vendordeps.reimport.failure", errorMessage),
                                                              subtitle,
                                                              project)
                                              }

                                          }
                                      })
    }

}