/*
 * Copyright 2015-2019 the original author or authors
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

package net.javaru.iip.frc.actions.tools;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.facet.FrcFacetKt;
import net.javaru.iip.frc.util.FrcProjectExtsKt;



public abstract class AbstractFrcToolsLegacyAction extends AbstractFrcToolsAction
{
    @Override
    public void update(AnActionEvent e)
    {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        e.getPresentation().setVisible(project != null &&
                                       !project.isDisposed() &&
                                       !FrcProjectExtsKt.isGradleProject(project) && /* TODO: Need to reverse this and check if it is an Ant Based Project once the isAntBasedFrcProject method is implemented*/
                                       FrcFacetKt.isFrcFacetedProject(project));
    }
}
