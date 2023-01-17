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

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.run.createLaunchShuffleboardRunConfiguration
import net.javaru.iip.frc.run.createLaunchSmartDashboardRunConfiguration
import net.javaru.iip.frc.wpilib.getAttachedWpiLibVersion
import net.javaru.iip.frc.wpilib.getWpiLibToolsPath

abstract class AbstractCreateRunConfigurationAction(): AnAction()
{
    override fun actionPerformed(e: AnActionEvent)
    {
        val project = e.getData(CommonDataKeys.PROJECT)
        if (project != null)
        {
            DumbService.getInstance(project).runReadActionInSmartMode() {
                doCreation(project, e)
            }
        }
    }

    abstract fun doCreation(project: Project, e: AnActionEvent)
}

class CreateLaunchShuffleboardRunConfigurationAction() : AbstractCreateRunConfigurationAction()
{
    override fun doCreation(project: Project, e: AnActionEvent)
    {
        project.getAttachedWpiLibVersion()?.also {
            createLaunchShuffleboardRunConfiguration (project, it, setAsSelected = true)
        }
    }
}

class CreateLaunchSmartDashboardRunConfigurationAction() : AbstractCreateRunConfigurationAction()
{
    override fun doCreation(project: Project, e: AnActionEvent)
    {
        project.getAttachedWpiLibVersion()?.also {
            createLaunchSmartDashboardRunConfiguration(project, it, setAsSelected = true)
        }
    }
}