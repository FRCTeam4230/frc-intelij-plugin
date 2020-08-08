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
package net.javaru.iip.frc.actions.tools.internal

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.facet.isFrcFacetedProject
import javax.swing.Icon
import org.apache.commons.lang3.BooleanUtils


val IS_IN_FRC_INTERNAL_MODE = BooleanUtils.toBoolean(System.getProperty("frc.is.internal", "false"))



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
                                   IS_IN_FRC_INTERNAL_MODE
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

            // Put code here, but do N0T commit it




            LOG.trace("[FRC] BREAKPOINT")
        }
        catch (t: Throwable)
        {
            LOG.warn("[FRC] Exception: $t", t)
        }

        LOG.trace("[FRC] BREAKPOINT")
    }

    companion object
    {
        private val LOG = Logger.getInstance(CauseAnExceptionAction::class.java)
    }
}
