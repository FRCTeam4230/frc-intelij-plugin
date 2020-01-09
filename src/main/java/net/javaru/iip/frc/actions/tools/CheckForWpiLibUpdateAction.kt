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

package net.javaru.iip.frc.actions.tools

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.util.isGradleProject
import net.javaru.iip.frc.wpilib.services.WpiLibVersionService
import java.time.Duration


class CheckForWpiLibUpdateAction: AbstractFrcToolsAction()
{
    override fun actionPerformed(e: AnActionEvent)
    {
        val project = e.project ?: return
        WpiLibVersionService.getInstance(project).checkWpiLibStatusAndAlertIfNeeded(true, Duration.ofSeconds(0))
    }

    override fun additionalIsVisibleChecks(project: Project, e: AnActionEvent): Boolean = project.isGradleProject()
}