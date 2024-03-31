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
package net.javaru.iip.frc.actions.tools

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.facet.isFrcFacetedProject
import javax.swing.Icon

abstract class AbstractFrcToolsAction : AnAction
{
    protected constructor()

    @Suppress("unused")
    protected constructor(text: String?) : super(text)

    @Suppress("unused")
    protected constructor(icon: Icon?) : super(icon)

    @Suppress("unused")
    protected constructor(text: String?, description: String?, icon: Icon?) : super(text, description, icon)

    override fun update(e: AnActionEvent)
    {
        val project = e.getData(CommonDataKeys.PROJECT)
        e.presentation.isVisible = project != null && !project.isDisposed && project.isFrcFacetedProject() && additionalIsVisibleChecks(project, e)
    }

    /**
     * Overrode to impose additional visibility checks. Return `true` to make the action visible.
     */
    protected open fun additionalIsVisibleChecks(project: Project, e: AnActionEvent): Boolean
    {
        return true
    }

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT
}