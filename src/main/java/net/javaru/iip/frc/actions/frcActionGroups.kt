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

package net.javaru.iip.frc.actions


import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.facet.isFrcFacetedProject


abstract class AbstractFrcActionGroup : DefaultActionGroup()
{
    override fun update(e: AnActionEvent)
    {
        val project = e.getData(CommonDataKeys.PROJECT)
        e.presentation.isVisible =
            project != null &&
            !project.isDisposed &&
            project.isFrcFacetedProject()
            && additionalVisibilityCriteria(project, e)
    }

    /** Override to add additional visibility criteria beyond the project not being null, not disposed, and an FRC project. */
    open fun additionalVisibilityCriteria(project: Project, event: AnActionEvent): Boolean = true

}

class FrcRioLogActionGroup : AbstractFrcActionGroup()
class FrcCreateComponentsActionGroup : AbstractFrcActionGroup()
class FrcToolsActionGroup : AbstractFrcActionGroup()
