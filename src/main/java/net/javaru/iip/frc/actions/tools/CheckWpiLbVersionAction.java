/*
 * Copyright 2015-2017 Mark Vedder
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.javaru.iip.frc.actions.tools;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.i18n.FrcMessageBundle;
import net.javaru.iip.frc.wpilib.WpiLibLibrariesUtils;
import net.javaru.iip.frc.wpilib.version.DetermineWpiLibVersion;



public class CheckWpiLbVersionAction extends AbstractFrcToolsAction
{
    private static final Logger LOG = Logger.getInstance(CheckWpiLbVersionAction.class);


    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final Project project = e.getProject();
        if (project != null)
        {
            final String version = DetermineWpiLibVersion.determineVersion(project);
            LOG.info("[FRC] Attached WpiLib Version: " + version);
            Messages.showInfoMessage(project, 
                                     FrcMessageBundle.message("frc.wpilib.version.attached.dialog.message", version), 
                                     FrcMessageBundle.message("frc.wpilib.version.attached.dialog.title"));
        }
    }


    @Override
    public void update(AnActionEvent e)
    {
        //TODO: Make this action unavailable if a download is currently running in the background
        final Project project = e.getData(CommonDataKeys.PROJECT);
        e.getPresentation().setVisible(project != null &&
                                       !project.isDisposed() &&
                                       FrcFacet.isFrcFacetedProject(project) &&
                                       WpiLibLibrariesUtils.isWpilibPresentViaReadAction(project));
    }
}
