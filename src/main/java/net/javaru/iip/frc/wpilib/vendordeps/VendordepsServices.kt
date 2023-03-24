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

package net.javaru.iip.frc.wpilib.vendordeps

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.intellij.json.psi.JsonFile
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.progress.PerformInBackgroundOption
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.startup.StartupManager
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
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.io.HttpRequests
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.i18n.FrcBundle.message
import net.javaru.iip.frc.i18n.FrcMessageKey
import net.javaru.iip.frc.isUnitTestMode
import net.javaru.iip.frc.notify.FrcNotifyType
import net.javaru.iip.frc.psi.FrcGeneralChangePsiTreeChangeListenerAdapter
import net.javaru.iip.frc.services.FrcErrorReportSubmitter
import net.javaru.iip.frc.services.FrcPluginProjectDisposable
import net.javaru.iip.frc.util.findCommonParentDir
import net.javaru.iip.frc.util.findPsiDirectory
import net.javaru.iip.frc.util.generateRandomTempPath
import net.javaru.iip.frc.util.getModules
import net.javaru.iip.frc.util.markGradleProjectAsNeedingReimport
import net.javaru.iip.frc.util.runBackgroundTask
import net.javaru.iip.frc.util.runNonBlockingReadActionInSmartMode
import net.javaru.iip.frc.util.toCommonSeparatorPath
import org.jetbrains.annotations.Contract
import java.net.URI
import java.nio.file.Path
import java.util.*
import java.util.concurrent.Callable

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

        PsiManager.getInstance(project).addPsiTreeChangeListener(VendordepsPsiTreeChangeListener(project), FrcPluginProjectDisposable.getInstance(project))
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
                                   // The below runs in a non-blocking read action in smart mode
                                   VendordepsService.getInstance(project).updateVendordepsListing()
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
data class VendordepsProjectFilesListing(val vendordepsProjectFileList: List<VendordepsProjectFile>,
                                    val vendordepsProjectFileMap: Map<UUID, List<VendordepsProjectFile>>,
                                    val duplicateVendordepsMap: Map<UUID, List<VendordepsProjectFile>>,
                                    val invalidVendordepsFileList: List<InvalidVendordepsProjectFile>)
{
    fun hasDuplicates(): Boolean = duplicateVendordepsMap.isNotEmpty()

    fun hasInvalidFiles(): Boolean = invalidVendordepsFileList.isNotEmpty()

    val duplicatesBulletedListing:String =  run {
        val msgBuilder = StringBuilder ()
        val via = message("frc.vendordeps.service.specified.via")
        duplicateVendordepsMap.forEach { entry: Map.Entry<UUID, List<VendordepsProjectFile>> ->
            if (entry.value.size > 1)
            {
                msgBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;\u2022 ${entry.value.first().vendordeps.name}:<br>")
                entry.value.forEach {
                    msgBuilder.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;\u2043 v${it.vendordeps.version.asText} $via ${it.jsonPsiFile.name}<br>")
                }
            }
        }
        msgBuilder.toString()
    }
}


class VendordepsService private constructor(val project: Project): Disposable
{
    private val logger = logger<VendordepsService>()
    // We initialize to an empty listing, but it gets properly initialized in the init method once the project is in smart mode
    @Suppress("MemberVisibilityCanBePrivate")
    var vendordepsProjectFilesListing = VendordepsProjectFilesListing(emptyList(), emptyMap(), emptyMap(), emptyList())
        private set
    
    var isDisposed = false
        private set
    
    init
    {
        logger.debug{"[FRC] Scheduling VendordepsService initialization for project: $project"}
        StartupManager.getInstance(project).runAfterOpened {
            logger.debug { "[FRC] Scheduling VendordepsService read action for project: $project" }
            project.runNonBlockingReadActionInSmartMode {
                logger.debug { "[FRC] VendordepsService initializing for project: $project" }
                updateVendordepsListing()
                notifyAboutDuplicatesIfAny()
            }
        }
    }
    
    companion object
    {
        @JvmStatic
        fun getInstance(project: Project) = project.service<VendordepsService>()
    }

   

    /**
     * Runs, via a non-blocking read action in smart mode, a process to update and then
     * use the Vendordeps list. Note that the callback is run in the same read action.
     * As such as UI work, including notifications, should not take place. If UI work is
     * necessary, use the uiContinuationCallback.
     * **Typically, rather than using this function, you can just use the
     * [vendordepsProjectFilesListing] property as that should be updated anytime there is
     * a change to the `vendordeps` directory.**
     */
    fun <R> updateAndUseVendordepsList(notifyOnDuplicates: Boolean = true, callback: (VendordepsProjectFilesListing) -> R, uiContinuationCallback: (R, VendordepsProjectFilesListing) -> Unit)
    {
        var result: R? = null
        project.runNonBlockingReadActionInSmartMode(
            {
                updateVendordepsListing()
                result = callback(vendordepsProjectFilesListing)
            }, {
                if (notifyOnDuplicates) {
                    notifyAboutDuplicatesIfAny()
                }
                uiContinuationCallback(result!!, vendordepsProjectFilesListing)
            })
    }


    /** 
     * Updates the [vendordepsProjectFilesListing] with the bulk of the work being done in a 
     * non-blocking read action in smart mode, and the actual (quick) updated of the 
     * [vendordepsProjectFilesListing] property and the notification of duplicates (if applicable)
     * in a write action.
     * 
     * @param notifyOnDuplicates if a notification should occur if there are duplicates
     */
    fun updateVendordepsListing(notifyOnDuplicates: Boolean = true)
    {
        if (isUnitTestMode()) {
            vendordepsProjectFilesListing = updateVendordepsListingWork()
            if (notifyOnDuplicates) { notifyAboutDuplicatesIfAny() }
        }
        else {
            ReadAction
                .nonBlocking(Callable { updateVendordepsListingWork() })
                .inSmartMode(project)
                .expireWith(this)
                .finishOnUiThread(ModalityState.NON_MODAL) { result ->
                    vendordepsProjectFilesListing = result
                    if (notifyOnDuplicates) { notifyAboutDuplicatesIfAny() }
                }
                // Common Executor examples are
                //      com.intellij.util.concurrency.NonUrgentExecutor.getInstance()
                //      AppExecutorUtil.getAppExecutorService()
                //      com.intellij.util.concurrency.BoundedTaskExecutor
                .submit(AppExecutorUtil.getAppExecutorService())
//            .submit(AppExecutorUtil.createBoundedApplicationPoolExecutor("Read Action", AppExecutorUtil.getAppExecutorService(), 1, disposable))
        }
    }
    
    /** Updates the current [vendordepsProjectFilesListing]. This should be run in a nonblocking read action. */
    private fun updateVendordepsListingWork(): VendordepsProjectFilesListing
    {
        // ** No write actions allowed. This function is designed to be run in a read action. **
        logger.debug{ "[FRC] Updating Vendordeps Listing for project $project" }
        val pluginProjectDisposable = FrcPluginProjectDisposable.getInstance(project)
        fun hasBeenDisposed() = (project.isDisposed || pluginProjectDisposable.isDisposed || this.isDisposed)
        if (hasBeenDisposed()) return vendordepsProjectFilesListing
        val vendordepsDir = findVendordepsDir()
        if (hasBeenDisposed()) return vendordepsProjectFilesListing
        logger.debug { "[FRC] $vendordepsDirName dir found at: ${vendordepsDir?.virtualFile?.path}" }
        val vendordepsProjectFileList = mutableListOf<VendordepsProjectFile>()
        val invalidVendordepsFileList = mutableListOf<InvalidVendordepsProjectFile>()
        vendordepsDir
            ?.children
            ?.asSequence()
            ?.filter { it is JsonFile }
            ?.map { it as JsonFile }
            ?.filter { it.isVendordepsJsonFile(project) }
            ?.forEach { jsonFile: JsonFile ->
                // Check if we need to "break" out early
                if (hasBeenDisposed()) return vendordepsProjectFilesListing
                Vendordeps.parse(jsonFile).onSuccess { vendordeps: Vendordeps ->
                    vendordepsProjectFileList.add(VendordepsProjectFile(jsonFile, vendordeps))
                }.onFailure { t: Throwable ->
                    //FrcErrorReportSubmitter.submitVendordepsParsingError(project, VendordepsParsingException(jsonFile, t))
                    logger.info("[FRC] Could not parse file as Vendordeps. File: ${jsonFile.name} Error: $t", t)
                    invalidVendordepsFileList.add(InvalidVendordepsProjectFile(jsonFile))
                }
            }

        if (hasBeenDisposed()) return vendordepsProjectFilesListing
        val vendordepsProjectFileMap =
            vendordepsProjectFileList.groupBy {
                it.vendordeps.uuid
            }

        val duplicateVendordepsMap =  vendordepsProjectFileMap.filterValues {
                it.size > 1
            }.map {
                it.key to it.value.sorted()
            }.toMap()

        return VendordepsProjectFilesListing(vendordepsProjectFileList,
                                             vendordepsProjectFileMap,
                                             duplicateVendordepsMap,
                                             invalidVendordepsFileList)
    }

    /**
     * Checks if the current (i.e. last updated) Vendordeps List has any duplicates. If so, it
     * notifies the user. The check itself is a simple checks for an empty `duplicateVendordepsMap`
     * property on the supplied `VendordepsProjectFilesListing`. If not empty, a message is created
     * and the notification is made. As such, no file (or read) activity occurs.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun notifyAboutDuplicatesIfAny() = notifyAboutDuplicatesIfAny(this.vendordepsProjectFilesListing)

    /**
     * Checks the supplied Vendordeps  List has any duplicates. If so, it notifies the user.
     * The check itself is a simple checks for an empty `duplicateVendordepsMap` property on
     * the supplied `VendordepsProjectFilesListing`. If not empty, a message is created and
     * the notification is made. As such, no file (or read) activity occurs.
     *
     * @param vendordepsProjectFilesListing the listing of vendordeps files
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun notifyAboutDuplicatesIfAny(vendordepsProjectFilesListing: VendordepsProjectFilesListing)
    {
        if (vendordepsProjectFilesListing.hasDuplicates())
        {
            /* TODO ADD ACTION HANDLERS to allow user to fix the issue or ignore*/
            FrcNotifyType.ACTIONABLE_ERROR
                .withContent(FrcMessageKey.of("frc.vendordeps.service.duplicate.content", vendordepsProjectFilesListing.duplicatesBulletedListing))
                .withFrcPrefixedTitle(FrcMessageKey.of("frc.vendordeps.service.duplicate.subtitle"))
                .notify(project)
        }

    }

    fun findVendordepsDirNonBlocking(action: (directory: PsiDirectory?) -> Unit) {
        project.runNonBlockingReadActionInSmartMode {
            val dir = findVendordepsDir()
            action(dir)
        }
    }
    
    /**
     * Finds the vendordeps directory for the project. Should be run only when the project is smart
     * and only as a non-blocking read action.
     * ```
     * project.runNonBlockingReadActionInSmartMode {
     *     val dir = VendordepsVersionService.getInstance(it).findVendorDepsDir()
     *     notifyInfoBalloon("Vendordeps dir = ${dir?.virtualFile?.path ?: "NOT FOUND"}")
     * }
     * ```
     *
     * @return the vendordeps directory as a [PsiDirectory] or `null` if it does not exist, or cannot be found.
     */
    private fun findVendordepsDir(): PsiDirectory?
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
            var virtualFiles = FilenameIndex.getVirtualFilesByName(vendordepsDirName, true, GlobalSearchScope.projectScope(project)).filter {
                    it.isDirectory
                }

            if (virtualFiles.isEmpty()) return null

            // 99% use case should be handled here
            if (virtualFiles.size == 1)
            {
                return virtualFiles.first().findPsiDirectory(project)
            }

            // We have multiple found directories… try the obvious solution, the one in the project base dir
            // this should handle 99% of the remaining cases
            if (project.basePath != null)
            {
                val psiDir = virtualFiles.firstOrNull {
                    it.parent.path == project.basePath
                }?.findPsiDirectory(project)
                if (psiDir != null) return psiDir
            }

            // Final effort... let's narrow the search results first, and hunt for it.
            // We want to reduce the scope to non-source content
            val moduleScopes = project.getModules().asSequence().map { GlobalSearchScope.notScope(GlobalSearchScope.moduleScope(it)) }.toList()
            var finalScope = GlobalSearchScope.projectScope(project)
            moduleScopes.forEach {
                finalScope = finalScope.intersectWith(it)
            }
            virtualFiles = FilenameIndex.getVirtualFilesByName(vendordepsDirName, true, finalScope).filter {
                it.isDirectory
            }

            if (virtualFiles.isEmpty()) return null
            if (virtualFiles.size == 1)
            {
                return virtualFiles.first().findPsiDirectory(project)
            }

            val paths = virtualFiles.mapNotNull { it.parent?.path }
            val commonParentDir = findCommonParentDir(paths)
            return virtualFiles.firstOrNull {
                it.parent?.path?.toCommonSeparatorPath() == commonParentDir
            } as PsiDirectory?
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
    @Suppress("unused")
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
            background = PerformInBackgroundOption.ALWAYS_BACKGROUND
                                 ) { indicator: ProgressIndicator ->
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

    override fun dispose()
    {
        isDisposed = true
        logger.trace { "[FRC] dispose() called for ${this::class.java.simpleName}" }
    }
}

class VendordepsParsingException(val jsonFile: JsonFile, cause: Throwable) : RuntimeException("Could not parse vendordeps file ${jsonFile.name}", cause)
{
    companion object
    {
        private const val serialVersionUID: Long = -4068721612655173680L
    }

}

// Pre IJ v2019.3, need to use StartupActivity rather than StartupActivity.Background (and change the plugin.xml element to match)
// StartupActivity.Background was deprecated in v2023.1. It recommends to use 'ProjectActivity' interface (with the <postStartupActivity> element)
// Also see  com.intellij.openapi.startup.StartupManager and com.intellij.ide.util.RunOnceUtil
// <postStartupActivity implementation="com.intellij.ide.bookmark.BookmarksStartupActivity"/>
class VendordepsServicesStartupActivity : ProjectActivity
{
    private val logger = logger<VendordepsServicesStartupActivity>()
    override suspend fun execute(project: Project)
    {
        if (project.isFrcFacetedProject())
        {
            logger.debug{ "[FRC] Scheduling Vendordeps Services Startup Activities for project: $project" }
            StartupManager.getInstance(project).runAfterOpened {
                logger.debug { "[FRC] Running Vendordeps Services Startup Activities for project: $project" }
                VendordepsFileListener.getInstance(project)
                VendordepsService.getInstance(project)
            }
        }
    }
}
