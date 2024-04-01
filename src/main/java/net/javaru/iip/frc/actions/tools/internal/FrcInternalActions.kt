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
@file:Suppress("PropertyName")

package net.javaru.iip.frc.actions.tools.internal

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.intellij.icons.AllIcons
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
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.InputValidator
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiDirectory
import com.intellij.ui.components.JBScrollPane
import icons.FrcIcons
import icons.FrcIcons.FRC
import net.javaru.iip.frc.FrcPluginGlobals
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.net.FrcPseudoRestService
import net.javaru.iip.frc.notify.FrcNotifyType
import net.javaru.iip.frc.run.RunDebugConfigsCreationData
import net.javaru.iip.frc.run.createAllRunDebugConfigurations
import net.javaru.iip.frc.services.FrcGradleService
import net.javaru.iip.frc.settings.FrcApplicationSettings
import net.javaru.iip.frc.ui.internal.PlaceholderTextFieldPaddingDemoFormDialogWrapper
import net.javaru.iip.frc.util.getCurrentFrcSeason
import net.javaru.iip.frc.util.markGradleProjectAsNeedingReimport
import net.javaru.iip.frc.util.reimportGradleProject
import net.javaru.iip.frc.util.runWhenSmart
import net.javaru.iip.frc.wpilib.getTeamNumberConfiguredInWpiLibPreferencesFileAsBackgroundTask
import net.javaru.iip.frc.wpilib.vendordeps.VendordepsManagementDialogWrapper
import net.javaru.iip.frc.wpilib.vendordeps.VendordepsProjectFilesListing
import net.javaru.iip.frc.wpilib.vendordeps.VendordepsService
import java.awt.Component
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.lang.reflect.Modifier
import java.nio.file.Path
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants
import kotlin.math.max

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

class FrcInternalUiActionsGroup : FrcInternalActionsGroup()
class FrcInternalRunCodeActionsGroup : FrcInternalActionsGroup()
class FrcInternalNetAndRestActionsGroup : FrcInternalActionsGroup()
class FrcInternalIoActionsGroup : FrcInternalActionsGroup()
class FrcInternalFrcPluginRelatedActionsGroup : FrcInternalActionsGroup()
class FrcInternalVendordepsActionsGroup : FrcInternalActionsGroup()
class FrcInternalGradleActionsGroup : FrcInternalActionsGroup()
class FrcInternalExceptionsActionsGroup : FrcInternalActionsGroup()

private object FrcInternalActions

@Suppress("unused")
private val logger = logger<FrcInternalActions>()

abstract class AbstractFrcInternalAction : AnAction
{
    protected val log = logger<AbstractFrcInternalAction>()

    @Suppress("unused")
    protected constructor()

    @Suppress("unused")
    protected constructor(icon: Icon?): super(icon)

    @Suppress("unused")
    protected constructor(text: String?) : super(text)

    @Suppress("unused")
    protected constructor(text: String?, icon: Icon?) : super(text, null, icon)

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
    protected open fun additionalIsVisibleChecks(project: Project, e: AnActionEvent): Boolean = true

    @Suppress("MemberVisibilityCanBePrivate")
    protected open fun showOnlyForFrcProjects(): Boolean = false

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
    FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON.builder()
        .withContent("Cannot run $actionName Action because the project was null on the ActionEvent.")
        .withFrcPrefixedTitle("$actionName Action Failed".trim())
        .notifyAllProject(notifyFrcProjectsOnly = false)
}




/** An action that will purposefully cause an exception for testing purposes. */
class LogAnErrorAction : AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        logger.info("[FRC] logging a simulated error message for testing exception handling")
        logger.error("[FRC] Test Error; Please Ignore. Sample error logging for testing: ${randomString()}")
    }

    companion object
    {
        private val logger = logger<LogAnErrorAction>()
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

    companion object
    {
        private val logger = logger<CauseAnExceptionAction>()
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
                FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON.builder()
                    .withContent("<html><h2>The following was retrieved from '$resourcePath'</h2><br/><pre>$resource</pre></html>")
                    .withFrcTitle()
                    .notifyAllProjectsViaBalloon(notifyFrcProjectsOnly = true)
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
                        FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON
                            .withContent("<html><h2>The following was retrieved from '$resourcePath'</h2><br/><pre>$resource</pre></html>")
                            .notifyAllProjectsViaBalloon(notifyFrcProjectsOnly = true)
                    }
                }.queue()
                true
            }
        }
    }
}

class CheckRobotProjectTypeInfo : AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        executeIfProjectNotNull(actionEvent, actionName = "Check RobotProjectTypeInfo") { project: Project ->
            val result = FrcGradleService.getRobotProjectTypeInfo(project)
            logger.info("RobotProjectTypeInfo: $result")
            FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON
                .withContent("RobotProjectTypeInfo: $result")
                .notify(project)
        }
    }
}

class CheckIncludeDesktopSupportSetting : AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        executeIfProjectNotNull(actionEvent, actionName = "Check Include Desktop Support Setting") { project: Project ->
            val result = FrcGradleService.getInstance(project).isIncludeDesktopSupport()
            FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON
                .withContent("includeDesktopSupport: $result")
                .notify(project)
        }
    }
}

class CheckGradleHasRoborioDeployTarget : AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        executeIfProjectNotNull(actionEvent, actionName = "Check Gradle Has Roborio Deploy Target") { project: Project ->
            val result = FrcGradleService.getInstance(project).hasRoborioDeployTarget()
            FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON
                .withContent("CheckGradleHasRoborioDeployTarget: $result")
                .notify(project)
        }
    }
}

class CreateRunConfigurationsFrcInternalAction: AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        executeIfProjectNotNull(actionEvent, "Create Run Configs") { project ->
            createAllRunDebugConfigurations(RunDebugConfigsCreationData.create(project))
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

class ReimportGradleProjectInternalAction: AbstractFrcInternalAction(AllIcons.Actions.Refresh)
{
    override fun actionPerformed(e: AnActionEvent)
    {
        val project = e.getData(CommonDataKeys.PROJECT)
        project?.reimportGradleProject()
    }
}

class FindVendordepsDirFrcInternalAction: AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent) {
        executeIfProjectNotNull(actionEvent, "Find Vendordeps dir") {
            VendordepsService.getInstance(it).findVendordepsDirNonBlocking { dir: PsiDirectory? ->
                FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON.withContent("Vendordeps dir = ${dir?.virtualFile?.path ?: "NOT FOUND"}").notifyViaBalloon(it)
            }
        }
    }
}

class CallGetTeamNumberConfiguredInWpiLibPreferencesFileAction: AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        executeIfProjectNotNull(actionEvent, "Get Team Number From WpiLib Preferences File") { project: Project ->
            project.getTeamNumberConfiguredInWpiLibPreferencesFileAsBackgroundTask {
                FrcNotifyType.ACTIONABLE_INFO
                    .withContent("Team number in wpilib_preferences.json is: $it")
                    .withFrcPrefixedTitle("Project team number")
                    .notifyViaBalloon(project)
            }
        }
    }
}

abstract class AbstractDisplayVendordepsListingFrcInternalAction : AbstractFrcInternalAction
{
    @Suppress("unused")
    protected constructor()

    @Suppress("unused")
    protected constructor(icon: Icon?) : super(icon)

    @Suppress("unused")
    protected constructor(text: String?) : super(text)

    @Suppress("unused")
    protected constructor(
        text: String?,
        description: String?,
        icon: Icon?
                         ) : super(text, description, icon)
    protected fun runUpdateCheck(project: Project)
    {
        VendordepsService.getInstance(project).updateAndUseVendordepsList(
            notifyOnDuplicates = true,
            { listing: VendordepsProjectFilesListing ->
            val sb = StringBuilder()
            sb.append("<html><h3>Vendordeps:</h3><ol>")
            listing.vendordepsProjectFileList.forEach { item ->
                sb.append("<li>$item</li>")
            }
            sb.append("</ol>")
            if (listing.hasInvalidFiles())
            {
                sb.append("<h3>Invalid Files:</h3><ol>")
                listing.invalidVendordepsFileList.forEach {invalid ->
                    sb.append("<li>${invalid.jsonPsiFile.name} (Lib name: ${invalid.data.libName})</li>")
                }
                sb.append("</ol>")
            }
            sb.append("</html>")
            sb.toString()

        }) { message, _ ->
            FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON.withContent(message).notifyViaBalloon(project)
        }
    }
}

class DisplayVendordepsListingFrcInternalAction : AbstractDisplayVendordepsListingFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        executeIfProjectNotNull(actionEvent, "Update Vendordeps Listing") {
            runUpdateCheck(it)
        }
    }
}

//class DisplayVendordepsListingWrappedInBackgroundTaskFrcInternalAction : AbstractDisplayVendordepsListingFrcInternalAction("List Vendordeps Wrapped in Background Task")
//{
//    override fun actionPerformed(actionEvent: AnActionEvent)
//    {
//        executeIfProjectNotNull(actionEvent, "Update Vendordeps Listing") {
//            // THIS CAUSES AN THREADING EXCEPTION:
//            //    Access is allowed from write thread only.
//            // Since runUpdateCheck uses runNonBlockingReadActionInSmartMode
//            it.runBackgroundTask("Display Vendordeps List") { _ ->
//                runUpdateCheck(it)
//            }
//        }
//    }
//}

class VendordepsCheckForDuplicatesInternalAction : AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        executeIfProjectNotNull(actionEvent, "Find Vendordeps dir") {
            it.runWhenSmart {
                VendordepsService.getInstance(it).updateVendordepsListing(true)
            }
        }
    }
}
class VendordepsCheckForDuplicatesNoNotificationInternalAction : AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        executeIfProjectNotNull(actionEvent, "Find Vendordeps dir") {
            it.runWhenSmart {
                VendordepsService.getInstance(it).updateVendordepsListing(false)
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
            val year = getCurrentFrcSeason()
            val ctreUrl = if (year >= 2022) "https://maven.ctr-electronics.com/release/com/ctre/phoenix/Phoenix-frc$year-latest.json" else "https://devsite.ctr-electronics.com/maven/release/com/ctre/phoenix/Phoenix-latest.json" 
            val url = Messages.showInputDialog(project,
                                               "Enter Vendordeps URL",
                                               "Download Vendordeps",
                                               FrcIcons.FileAndDirTypes.VendordepsDir,
                                               ctreUrl, 
                                               object : InputValidator
                                               {
                                                   override fun checkInput(inputString: String?): Boolean = inputString?.isNotBlank() ?: false
                                                   override fun canClose(inputString: String?): Boolean = inputString?.isNotBlank() ?: false
                                               })!!
            VendordepsService.getInstance(project).downloadVendordepToTempFileInBackground(project, url) { result: Result<Path, Exception> ->
                result.onSuccess {
                    FrcNotifyType.ACTIONABLE_INFO_WITH_FRC_ICON
                        .withContent("Downloaded to: $it")
                        .notify(project)
                }.onFailure {
                    FrcNotifyType.ACTIONABLE_ERROR
                        .withContent("Could not download Vendordeps file, Reason: ${it.message}")
                        .notify(project)
                }
            }

        }
    }
}

class ShowFrcApplicationSettings : AbstractFrcInternalAction(FRC.FIRST_ICON_MEDIUM_16)
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        val settingsString = FrcApplicationSettings.getInstance().toString()
        log.info("[FRC] $settingsString")
        Messages.showMessageDialog(
            actionEvent.project,
            settingsString,
            "Frc Application Settings",
            FRC.FIRST_ICON_MEDIUM_16
                                  )
    }
}

class LoadAllIconsInternalAction: AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        logger.info("[FRC] Loading all icons...")
        val loadedIcons = mutableListOf<Component>()
        processClass(FrcIcons::class.java, loadedIcons)


        val outer = JPanel(GridBagLayout())
        val inner = JPanel(GridBagLayout())
        val scroll = JBScrollPane(inner)
        outer.add(scroll, GridBagConstraints())

        val gc = GridBagConstraints()
        loadedIcons.forEach {
            gc.gridx++
            if (gc.gridx % 3 == 0)
            {
                gc.gridy++
                gc.gridx = 0
            }
            inner.add(it, gc)
        }
        val builder = DialogBuilder(actionEvent.project)
        builder.setTitle("Loaded FRC Icons")
        builder.setCenterPanel(outer)
        builder.show()
    }

    private fun processClass(clazz: Class<*>, loadedIcons: MutableList<Component>)
    {
        logger.info("[FRC] Checking Class: ${clazz.name}")
        clazz.declaredFields.forEach {
            if (Modifier.isStatic(it.modifiers) && javax.swing.Icon::class.java.isAssignableFrom(it.type) && it.name != "NOT_FOUND_ICON")
            {
                logger.info("[FRC] Loading ${it.name}")
                val icon = it.get(null) as Icon
                if (icon.iconHeight <= 24 && icon.iconWidth <= 24 && !it.name.contains("_ICO_"))
                {
                    val label = JLabel("${clazz.name}.${it.name}", icon, SwingConstants.LEFT)
                    val size = Dimension(400, max(18, icon.iconHeight + 4))
                    label.minimumSize = size
                    label.preferredSize = size
                    loadedIcons.add(label)
                }
                logger.info("[FRC] ${it.name} loaded")
            }
            else
            {
                logger.info("[FRC] Ignoring ${it.name}")
            }
        }
        logger.info("[FRC] Completed Class: ${clazz.name}")
        clazz.classes.forEach {
           processClass(it, loadedIcons)
        }
    }

}
