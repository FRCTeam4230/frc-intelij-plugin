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
package net.javaru.iip.frc.actions.tools.internal

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import icons.FrcIcons
import icons.FrcIcons.FRC
import net.javaru.iip.frc.FrcPluginGlobals
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.net.FrcPseudoRestService
import net.javaru.iip.frc.notify.FrcNotificationType
import net.javaru.iip.frc.notify.FrcNotifications
import net.javaru.iip.frc.notify.FrcNotifications.createNotification
import net.javaru.iip.frc.notify.FrcNotifications.notify
import net.javaru.iip.frc.notify.FrcNotifications.notifyBalloonAllOpenProjects
import net.javaru.iip.frc.run.createAllRunDebugConfigurations
import net.javaru.iip.frc.services.FrcGradleService
import net.javaru.iip.frc.ui.internal.PlaceholderTextFieldPaddingDemoFormDialogWrapper
import net.javaru.iip.frc.util.markGradleProjectAsNeedingReimport
import net.javaru.iip.frc.util.runWhenSmart
import net.javaru.iip.frc.wizard.FrcProjectWizardData
import net.javaru.iip.frc.wpilib.vendordeps.VendordepsListing
import net.javaru.iip.frc.wpilib.vendordeps.VendordepsManagementDialogWrapper
import net.javaru.iip.frc.wpilib.vendordeps.VendordepsService
import java.nio.file.Path
import javax.swing.Icon



class FrcInternalVendordepsActionsGroup : FrcInternalActionsGroup()

private object FrcInternalActions

private val logger = logger<FrcInternalActions>()

abstract class AbstractFrcInternalAction : AnAction
{
    protected constructor()

    @Suppress("unused")
    protected constructor(text: String?) : super(text)

    @Suppress("unused")
    protected constructor(text: String?,
                          description: String?,
                          icon: Icon?) : super(text, description, icon)

    override fun update(e: AnActionEvent)
    {
        val project = e.project
        e.presentation.isVisible = project != null &&
                                   !project.isDisposed &&
                                   showForProject(project) &&
                                   additionalIsVisibleChecks(project, e)
    }

    @Suppress("UNUSED_PARAMETER")
    protected fun additionalIsVisibleChecks(project: Project, e: AnActionEvent): Boolean = true

    @Suppress("MemberVisibilityCanBePrivate")
    protected fun showOnlyForFrcProjects(): Boolean = false

    private fun showForProject(project: Project): Boolean
    {
        return if (showOnlyForFrcProjects())
        {
            project.isFrcFacetedProject()
        }
        else
        {
            true
        }
    }
}

private fun executeIfProjectNotNull(actionEvent: AnActionEvent, actionName: String = "", function: (project: Project) -> Unit)
{
    val project = actionEvent.project
    if (project == null)
        notifyOfFailureDueToNullProject(actionName)
    else
        function(project)
}

private fun notifyOfFailureDueToNullProject(actionName: String = "")
{
    FrcNotifications.notifyAllOpenProjects(notifyFrcProjectsOnly = false) {
        createFailedActionDueToNullProjectNotification(actionName)
    }
}

private fun createFailedActionDueToNullProjectNotification(actionName: String = "") = createNotification(
    FrcNotificationType.ACTIONABLE_INFO_WITH_FRC_ICON,
    "Cannot run $actionName Action because the project was null on the ActionEvent.",
    subTitle = "$actionName Action Failed".trim()
                                                                                                        )


open class FrcInternalActionsGroup : DefaultActionGroup()
{
    override fun update(e: AnActionEvent)
    {
        val project = e.project
        e.presentation.isVisible = project != null &&
                                   !project.isDisposed &&
                                   FrcPluginGlobals.IS_IN_FRC_INTERNAL_MODE
    }
}

/** An action that will purposefully cause an exception for testing purposes. */
class LogAnErrorAction : AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        logger.info("[FRC] logging a simulated error message for testing exception handling")
        logger.error("[FRC] Test Error; Please Ignore. Sample error logging for testing: ${randomString()}")
    }
}

class RunKotlinCodeForTestingAndDebuggingFrcInternalAction : AbstractFrcInternalAction()
{
    @Suppress("UNUSED_VARIABLE")
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        val ideView = actionEvent.getData(LangDataKeys.IDE_VIEW)
        val module =  actionEvent.getData(LangDataKeys.MODULE)
        val project =  actionEvent.project

        logger.trace {"[FRC] BREAKPOINT"}

        try
        {
            logger.trace {"[FRC] BREAKPOINT"}

            // Put code here, but do N0T commit it - paying attention to import statements




            logger.trace {"[FRC] BREAKPOINT"}
        }
        catch (t: Throwable)
        {
            // We log as an error so that we can more easily grab the stacktrace from the exception reporter
            logger.error("[FRC] Exception: $t", t)
        }

        logger.trace {"[FRC] BREAKPOINT"}
    }
}

class FetchPredefinedRestResource: AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        object : Task.Modal(actionEvent.project, "Checking REST Service", false)
        {
            override fun run(indicator: ProgressIndicator)
            {
                val resourcePath = "license.txt"
                val resource = FrcPseudoRestService.getResource(resourcePath) ?: "Was Null (i.e. not found)"
                notifyBalloonAllOpenProjects(notifyFrcProjectsOnly = true) {
                    createNotification(FrcNotificationType.ACTIONABLE_INFO_WITH_FRC_ICON,
                                       "<html><h2>The following was retrieved from '$resourcePath'</h2><br/><pre>$resource</pre></html>")
                }
            }
        }.queue()
    }
}

class FetchSpecifiedRestResource: AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        executeIfProjectNotNull(actionEvent, actionName = "Fetch REST Service") { project: Project ->
            DumbService.getInstance(project).runReadActionInSmartMode<Boolean> {
                val defaultResourcePath = "dynamic-notifications/eol.json"
                val message =
                    """<html><b>Resource to fetch?<b><br>
                    |Relative to the <tt>src/main/resources</tt> dir on the <tt>rest-v1/</tt> branch <b>without</b> leading slash.<br>
                    |Examples:<br>
                    |&nbsp;&nbsp;&nbsp;&nbsp;copyright.txt<br>
                    |&nbsp;&nbsp;&nbsp;&nbsp;licenses/wpilib-license.txt<br>
                    |&nbsp;&nbsp;&nbsp;&nbsp;dynamic-notifications/eol.json<br>
                    |</html>""".trimMargin()
                val resourcePath = Messages.showInputDialog(project, message, FrcPluginGlobals.FRC_PLUGIN_NAME, FRC.FIRST_ICON_DIALOG_WINDOW, defaultResourcePath, null) ?: defaultResourcePath
                object : Task.Modal(project, "Checking REST Service", false)
                {
                    override fun run(indicator: ProgressIndicator)
                    {

                        val resource = FrcPseudoRestService.getResource(resourcePath) ?: "Was Null (i.e. not found)"
                        notifyBalloonAllOpenProjects(notifyFrcProjectsOnly = true) {
                            createNotification(
                                FrcNotificationType.ACTIONABLE_INFO_WITH_FRC_ICON,
                                "<html><h2>The following was retrieved from '$resourcePath'</h2><br/><pre>$resource</pre></html>"
                                              )
                        }
                    }
                }.queue()
                true
            }
        }
    }
}

class CheckIncludeDesktopSupportSetting : AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        executeIfProjectNotNull(actionEvent, actionName = "Fetch REST Service") { project: Project ->
            val result = FrcGradleService.getInstance(project).isIncludeDesktopSupport()
            notify(FrcNotificationType.ACTIONABLE_INFO, "includeDesktopSupport: $result")
        }
    }
}

class CreateRunConfigurationsFrcInternalAction: AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        executeIfProjectNotNull(actionEvent, "Create Run Configs") {
            val data = FrcProjectWizardData()
            createAllRunDebugConfigurations(it, data)
        }
    }
}

class MarkGradleProjectDirtyInternalAction: AbstractFrcInternalAction()
{
    override fun actionPerformed(e: AnActionEvent)
    {
        val project = e.getData(CommonDataKeys.PROJECT)
        project?.markGradleProjectAsNeedingReimport()
    }
}

class FindVendordepsDirFrcInternalAction: AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent) {
        executeIfProjectNotNull(actionEvent, "Find Vendordeps dir") {
            it.runWhenSmart {
                val dir = VendordepsService.getInstance(it).findVendordepsDir()
                FrcNotifications.notifyInfoBalloon("Vendordeps dir = ${dir?.virtualFile?.path ?: "NOT FOUND"}")
            }
        }
    }
}

class GetVendordepsListingFrcInternalAction: AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent) {
        executeIfProjectNotNull(actionEvent, "Find Vendordeps dir") {
            it.runWhenSmart {
                VendordepsService.getInstance(it).getAndUseVendordeps(notifyOnDuplicates = true) { listing: VendordepsListing ->
                    val sb = StringBuilder()
                    sb.append("<html><h3>Vendordeps:</h3><ol>")
                    listing.vendordepsFileList.forEach { item ->
                        sb.append("<li>$item</li>")
                    }
                    sb.append("</ol></html>")

                    FrcNotifications.notifyInfoBalloon(sb.toString())
                }
            }
        }
    }
}

class ShowPlaceholderTextFieldPaddingDemoDialog: AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        executeIfProjectNotNull(actionEvent, "Display Placeholder Padding Demo Dialog") {
            PlaceholderTextFieldPaddingDemoFormDialogWrapper(it).showAndGet()
        }
    }
}

class ShowVendordepsManagementDialog: AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        executeIfProjectNotNull(actionEvent, "Display Vendordeps Management Dialog") {
            VendordepsManagementDialogWrapper(it).showAndGet()
        }
    }
}

class CreateTempFile: AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        val project:Project? = actionEvent.project
        val tempFile = net.javaru.iip.frc.util.createRandomTempFile(".txt")
        Messages.showMessageDialog(project, "Temp File: $tempFile", "Temp File Created", null)

    }
}

class DownloadVendorDeps: AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        executeIfProjectNotNull(actionEvent, "Display Vendordeps Management Dialog") { project: Project ->
            val url = Messages.showInputDialog(project,
                                               "Enter Vendordeps URL",
                                               "Download Vendordeps",
                                               FrcIcons.FileAndDirTypes.VendordepsDir,
                                               "https://devsite.ctr-electronics.com/maven/release/com/ctre/phoenix/Phoenix-latest.json",
                                               object : InputValidator
                                               {
                                                   override fun checkInput(inputString: String?): Boolean = inputString?.isNotBlank() ?: false
                                                   override fun canClose(inputString: String?): Boolean = inputString?.isNotBlank() ?: false
                                               })!!
            VendordepsService.getInstance(project).downloadVendordepToTempFileInBackground(project, url) { result: Result<Path, Exception> ->

                result.onSuccess {
                    notify(
                        FrcNotificationType.ACTIONABLE_INFO_WITH_FRC_ICON,
                        "Downloaded to: $it",
                        project = project
                          )
                }.onFailure {
                    notify(
                        FrcNotificationType.ACTIONABLE_ERROR,
                        "Could not download Vendordeps file, Reason: ${it.message}",
                        project = project
                          )
                }

            }

        }
    }
}