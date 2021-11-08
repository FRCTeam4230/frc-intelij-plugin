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

package net.javaru.iip.frc.wpilib.vendordeps

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.intellij.json.psi.JsonFile
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiTreeChangeEvent
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.containers.stream
import com.intellij.util.io.HttpRequests
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.i18n.FrcMessageKey
import net.javaru.iip.frc.notify.FrcNotificationType
import net.javaru.iip.frc.notify.FrcNotifications
import net.javaru.iip.frc.psi.FrcGeneralChangePsiTreeChangeListenerAdapter
import net.javaru.iip.frc.services.FrcApplicationDisposableService
import net.javaru.iip.frc.util.findCommonParentDir
import net.javaru.iip.frc.util.generateRandomTempPath
import net.javaru.iip.frc.util.getModules
import net.javaru.iip.frc.util.markGradleProjectAsNeedingReimport
import net.javaru.iip.frc.util.runBackgroundTask
import net.javaru.iip.frc.util.runReadActionInSmartMode
import net.javaru.iip.frc.util.toCommonSeparatorPath
import org.jetbrains.annotations.Contract
import java.net.URI
import java.nio.file.Path
import java.util.*
import kotlin.properties.Delegates

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
        logger.debug { "[FRC] Initializing VendordepsFileListener" }

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
                                       logger.debug { "[FRC] VendordepsFileListener.AFTER: Change detected to the 'vendordeps' file: ${virtualFile?.name}" }
                                       project.markGradleProjectAsNeedingReimport(scheduleForAutoReimport = true)
                                       updateVendordepsStatus()
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
                                       logger.debug {
                                           "[FRC] VendordepsFileListener.BEFORE: Change detected to the 'vendordeps' file: ${virtualFile.name}  io-file exists: ${
                                               VfsUtil.virtualToIoFile(virtualFile).exists()
                                           }"
                                       }
                                       project.markGradleProjectAsNeedingReimport(scheduleForAutoReimport = true)
                                       updateVendordepsStatus()
                                       break // we only want/need to do the import once in the event multiple files were changed.
                                   }
                               }
                           }
                           
                           fun updateVendordepsStatus()
                           {
                               project.runBackgroundTask("Update vendordeps status") {
                                   project.runReadActionInSmartMode {
                                       VendordepsService.getInstance(project).checkForDuplicates()
                                   }
                               }
                           }
                       })
    }
}

class VendordepsPsiTreeChangeListener(private val project: Project) : FrcGeneralChangePsiTreeChangeListenerAdapter()
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
fun PsiFile?.isVendordepsJsonFile(project: Project): Boolean = (this is JsonFile) && this.virtualFile.isVendordepsJsonFile(project)

/**
 * Determines if the `VirtualFile` is a `vendordeps.json` file, returning false if the `VirtualFile` is null.
 * If a project is provided (i.e. not null), then the file must exist within the project's content, and it must be an FRC Faceted project.
 */
@Contract("null,_ -> false")
fun VirtualFile?.isVendordepsJsonFile(project: Project?): Boolean
{
    if (this == null) return false
    val isWithinProject =
        if (project == null)
            true
        else
            project.isFrcFacetedProject() && ProjectFileIndex.getInstance(project).isInContent(this)
    return isWithinProject &&
        this.parent?.name == vendordepsDirName &&
        this.name.endsWith(".json", ignoreCase = true)
}

@Suppress("unused", "MemberVisibilityCanBePrivate")
class VendordepsListing(val vendordepsFileList: List<VendordepsFile>,
                        val vendordepsFileMap: Map<UUID, List<VendordepsFile>>,
                        val duplicateVendordepsMap: Map<UUID, List<VendordepsFile>>)
{
    fun hasDuplicates(): Boolean = duplicateVendordepsMap.isNotEmpty()
}


class VendordepsService private constructor(val project: Project)
{
    private val logger = logger<VendordepsService>()
    private var vendordepsListing by Delegates.notNull<VendordepsListing>()

    init
    {
        project.runReadActionInSmartMode{
            updateVendordepsListing(notifyOnDuplicates = true)
        }
    }
    
    companion object
    {
        @JvmStatic
        fun getInstance(project: Project) = project.service<VendordepsService>()
    }
    
    @Suppress("MemberVisibilityCanBePrivate")
    fun getLastKnownVendordepsListing(): VendordepsListing = vendordepsListing


    fun getAndUseVendordeps(notifyOnDuplicates: Boolean = false, callback: (VendordepsListing) -> Unit)
    {
        project.runReadActionInSmartMode {
            updateVendordepsListing(notifyOnDuplicates)
            callback.invoke(vendordepsListing)
        }
    }
    
    fun checkForDuplicates(notifyOnDuplicates: Boolean = true)
    {
        project.runReadActionInSmartMode {
            updateVendordepsListing(notifyOnDuplicates)
        }
    }

    private fun updateVendordepsListing(notifyOnDuplicates: Boolean)
    {
        val vendordepsDir = findVendordepsDir()
        logger.debug { "[FRC] $vendordepsDirName dir found at: ${vendordepsDir?.virtualFile?.path}" }
        val vendordepsFileList = mutableListOf<VendordepsFile>()
        vendordepsDir
            ?.children
            ?.stream()
            ?.filter { it != null && it is JsonFile }
            ?.map { it as JsonFile }
            ?.filter { it.isVendordepsJsonFile(project) }
            ?.forEach { jsonFile: JsonFile ->
                Vendordeps.parse(jsonFile).onSuccess { vendordeps: Vendordeps ->
                    vendordepsFileList.add(VendordepsFile(jsonFile, vendordeps))
                }.onFailure { t: Throwable ->
                    logger.info("[FRC] Could not parse file as Vendordeps. File: ${jsonFile.name} Error: $t")
                }
            }
        
        val vendordepsFileMap =
            vendordepsFileList.groupBy {
                it.vendordeps.uuid
            }

        val duplicateVendordepsMap =  vendordepsFileMap.filterValues {
                it.size > 1
            }.map {
                it.key to it.value.sorted()
            }.toMap()

        
        vendordepsListing = VendordepsListing(vendordepsFileList, vendordepsFileMap, duplicateVendordepsMap)

        if (notifyOnDuplicates && vendordepsListing.hasDuplicates())
        {

           notifyAboutDuplicates(vendordepsListing)
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun notifyAboutDuplicates(vendordepsListing: VendordepsListing)
    {
        if (vendordepsListing.hasDuplicates())
        {
            val msgBuilder = StringBuilder()
            vendordepsListing.duplicateVendordepsMap.forEach { entry: Map.Entry<UUID, List<VendordepsFile>> ->
                if (entry.value.size > 1)
                {
                    msgBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;\u2022 ${entry.value.first().vendordeps.name}:<br>")
                    entry.value.forEach { 
                        msgBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\u2043 version ${it.vendordeps.version} in ${it.jsonPsiFile.name}<br>")
                    }
                }
            }
            
            FrcNotifications.notify(FrcNotificationType.ACTIONABLE_WARN,
                                        contentKey = FrcMessageKey.of("frc.vendordeps.service.duplicate.content", msgBuilder.toString()),
                                        subTitleKey = FrcMessageKey.of("frc.vendordeps.service.duplicate.subtitle"),
                                        project = project,
                /* TODO ADD ACTION HANDLERS to allow user to fix the issue or ignore*/
                                       )
        }
    }

    fun findVendordepsDirAsNioPath(): Path? = findVendordepsDir()?.virtualFile?.toNioPath()


    /**
     * Finds the vendordeps directory for the project. Should be run only when the project is smart (and thus also
     * as a read action). For example:
     * ```
     * project.runWhenSmart {
     *     val dir = VendordepsVersionService.getInstance(it).findVendorDepsDir()
     *     notifyInfoBalloon("Vendordeps dir = ${dir?.virtualFile?.path ?: "NOT FOUND"}")
     * }
     * ```
     *
     * @return the vendordeps directory as a [PsiDirectory] or `null` if it does not exist, or cannot be found.
     */
    fun findVendordepsDir(): PsiDirectory?
    {
        // NOTES: The vendordeps directory can be overridden in GradleRIO via the Gradle Property 
        //        'gradlerio.vendordep.folder.path' ← Note the singular 'vendordep'
        //        But a stern warning about not overriding the values "unless you know what you are doing"
        //        gets logged on each build. See the WPIVendorDepsExtension class in GradleRIO project
        //        TODO: Enhance by seeing if we can read the Gradle property to get the value
        
        try
        {
            // Ideally, there is only a single vendordeps directory in the project root
            // But we have to allow for the possibility another vendordeps directory exists... perhaps a user accidentally created one elsewhere in the project
            var psiFsItems =
                FilenameIndex.getFilesByName(
                    project,
                    vendordepsDirName,
                    GlobalSearchScope.projectScope(project),
                    true
                                            )

            if (psiFsItems.isEmpty()) return null

            // 99% use case should be handled here
            if (psiFsItems.size == 1)
            {
                return if (psiFsItems[0] is PsiDirectory) psiFsItems[0] as PsiDirectory else null
            }

            // We have multiple found directories… try the obvious solution, the one in the project base dir
            // this should handle 99% of the remaining cases
            var foundDir: PsiDirectory? = null
            if (project.basePath != null)
            {
                psiFsItems.forEach {
                    if (it is PsiDirectory && project.basePath == it.parent?.virtualFile?.path)
                    {
                        foundDir = it
                        return@forEach
                    }
                }
            }

            if (foundDir != null) return foundDir

            // Final effort... let's narrow the search results first, and hunt for it.
            // We want to reduce the scope to non-source content
            val moduleScopes = project.getModules().asSequence().map { GlobalSearchScope.notScope(GlobalSearchScope.moduleScope(it)) }.toList()
            var finalScope = GlobalSearchScope.projectScope(project)
            moduleScopes.forEach {
                finalScope = finalScope.intersectWith(it)
            }
            psiFsItems =
                FilenameIndex.getFilesByName(
                    project,
                    vendordepsDirName,
                    finalScope,
                    true
                                            )

            if (psiFsItems.isEmpty()) return null
            if (psiFsItems.size == 1)
            {
                return if (psiFsItems[0] is PsiDirectory) psiFsItems[0] as PsiDirectory else null
            }

            val psiDirs = psiFsItems.filterIsInstance<PsiDirectory>().toList()
            val paths = psiDirs.mapNotNull { it.parentDirectory?.virtualFile?.path }
            val commonParentDir = findCommonParentDir(paths)
            psiDirs.forEach {
                if (it.parentDirectory?.virtualFile?.path?.toCommonSeparatorPath() == commonParentDir)
                {
                    foundDir = it
                    return@forEach
                }
            }
            return foundDir
        }
        catch (e: Throwable)
        {
            logger.warn("[FRC] An exception occurred when finding $vendordepsDirName directory. Cause summary: $e", e)
            return null
        }
    }


    /**
     * Downloads a vendordeps file from the specified URL to a (system) temp file. It ***does not*** install the file into the
     * vendordeps directory. Does so in a cancelable background process.
     */
    fun downloadVendordepToTempFileInBackground(project: Project, uri: URI, resultProcessor: (Result<Path, Exception>) -> Unit)
        = downloadVendordepToTempFileInBackground(project, uri.toString(), resultProcessor)

    /**
     * Downloads a vendordeps file from the specified URL to a (system) temp file. It ***does not*** install the file into the
     * vendordeps directory.
     */
    fun downloadVendordepToTempFileInBackground(project: Project, url: String, resultProcessor: (Result<Path, Exception>) -> Unit)
    {
        project.runBackgroundTask(
            "Download Vendordeps File",
            cancellable = true,
            background = PerformInBackgroundOption.ALWAYS_BACKGROUND) { indicator: ProgressIndicator ->
            val result = downloadVendordepsToTempFile(url, indicator)
            resultProcessor.invoke(result)
        }
    }

    /**
     * Downloads a vendordeps file from the specified URL to a (system) temp file. It ***does not*** install the file into the
     * vendordeps directory. This must nor be called from the EDT.
     */
    @JvmOverloads
    fun downloadVendordepsToTempFile(url: String, indicator: ProgressIndicator? = null): Result<Path, Exception>
    {
        return try
        {
            val outFile = generateRandomTempPath(deleteOnExit = true)
            HttpRequests.request(url).saveToFile(outFile, indicator)
            Ok(outFile)
        }
        catch (e: Exception)
        {
            Err(e)
        }
    }
}


// Pre IJ v2019.3, need to use StartupActivity rather than StartupActivity.Background (and change the plugin.xml element to match)
class VendordepsServicesStartupActivity : StartupActivity.Background
{
    override fun runActivity(project: Project)
    {
        if (project.isFrcFacetedProject())
        {
//            StartupManager.getInstance(project).runWhenProjectIsInitialized() {
            VendordepsFileListener.getInstance(project)
            VendordepsService.getInstance(project)
            
//            }
        }
    }
}
