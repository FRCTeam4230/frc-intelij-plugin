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

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import icons.FrcIcons.FRC
import net.javaru.iip.frc.FrcPluginGlobals
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.net.FrcPseudoRestService
import net.javaru.iip.frc.notify.FrcNotificationType
import net.javaru.iip.frc.notify.FrcNotifications
import net.javaru.iip.frc.notify.FrcNotifications.createNotification
import net.javaru.iip.frc.notify.FrcNotifications.notifyBalloonAllOpenProjects
import javax.swing.Icon

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

/**
 * An action that will purposefully cause an exception for testing purposes.
 */
class LogAnErrorAction : AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        LOG.info("[FRC] logging a simulated error message for testing exception handling")
        LOG.error("[FRC] Sample error logging for testing exception handling", RuntimeException("Sample exception for testing exception handling"))
    }

    companion object
    {
        private val LOG = Logger.getInstance(LogAnErrorAction::class.java)
    }
}

/**
 * An action that will purposefully cause an exception for testing purposes.
 */
class CauseAnExceptionAction : AbstractFrcInternalAction()
{
    override fun actionPerformed(actionEvent: AnActionEvent)
    {
        LOG.info("[FRC] Throwing simulated exception for testing exception handling")
        throw RuntimeException("Sample exception for testing exception handling")
    }

    companion object
    {
        private val LOG = Logger.getInstance(CauseAnExceptionAction::class.java)
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

        LOG.trace("[FRC] BREAKPOINT")

        try
        {
            LOG.trace("[FRC] BREAKPOINT")

            // Put code here, but do N0T commit it - paying attention to import statements




            LOG.trace("[FRC] BREAKPOINT")
        }
        catch (t: Throwable)
        {
            // We log as an error so we can more easily grab the stacktrace from the exception reporter
            LOG.error("[FRC] Exception: $t", t)
        }

        LOG.trace("[FRC] BREAKPOINT")
    }

    companion object
    {
        private val LOG = Logger.getInstance(CauseAnExceptionAction::class.java)
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
        val project = actionEvent.project
        if (project == null)
        {
            FrcNotifications.notifyAllOpenProjects(notifyFrcProjectsOnly = false) {
                createNotification(
                    FrcNotificationType.ACTIONABLE_INFO_WITH_FRC_ICON,
                    "Project was null on the ActionEvent. Need a Project in order to fetch the resource on a read action thread",
                    subTitle = "Fetch REST Service Failure"
                                  )
            }
        }
        else
        {
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