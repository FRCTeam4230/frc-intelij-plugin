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
import net.javaru.iip.frc.wpilib.version.WpiLibVersionStatus;



public class CheckSystemWpiLbVersionAction extends AbstractFrcToolsAction
{
    private static final Logger LOG = Logger.getInstance(CheckSystemWpiLbVersionAction.class);


    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final Project project = e.getProject();

        final WpiLibVersionStatus versionStatus = WpiLibVersionStatus.getCurrentStatus(project);
        LOG.info("[FRC] WpiLib Version Status: " + versionStatus);

        //TODO: i18n
        
        final String indent = "&nbsp;&nbsp;&nbsp;&nbsp;"; 
        StringBuilder sb = new StringBuilder("<html>");
        sb.append("WPILib Version Status:<br>");
        sb.append(indent).append(versionStatus.getAttachedVersionSummary()).append("<br>");
        if (!versionStatus.isWpiLibAttached() || versionStatus.isNewerVersionDownloadedThanAttached())
        {
            sb.append(indent).append(versionStatus.getDownloadedVersionSummary()).append("<br>");
        }
        if (versionStatus.isNewerVersionAvailableThanAttached() || versionStatus.isNewerVersionAvailableThanDownloaded())
        {
            // This logic will likely need to be split
            sb.append(indent).append(versionStatus.getAvailableVersionSummary()).append("<br>");
        }
        else
        {
            sb.append(indent).append("You have the latest available version.").append("<br>");
        }
        sb.append("</html>");
        //TODO add logic so if not attached, there is an option to do such
        //TODO add logic so if new version is installed than attached - what to do????
        //TODO add logic so if newer version is available, there is an option to download it
        
        Messages.showInfoMessage(project,
                                 sb.toString(),
                                 FrcMessageBundle.message("frc.wpilib.version.dialog.title"));
    }


    @Override
    public void update(AnActionEvent e)
    {
        //TODO: Make this action unavailable if a download is currently running in the background
        final Project project = e.getData(CommonDataKeys.PROJECT);
        e.getPresentation().setVisible(project != null &&
                                       !project.isDisposed() &&
                                       FrcFacet.isFrcFacetedProject(project) &&
                                       WpiLibLibrariesUtils.isWpilibAttachedViaReadAction(project));
    }
}
