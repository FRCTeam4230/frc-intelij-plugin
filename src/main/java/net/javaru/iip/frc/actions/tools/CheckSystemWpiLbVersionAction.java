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

import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.i18n.FrcMessageBundle;
import net.javaru.iip.frc.wpilib.version.WpiLibVersionStatus;



public class CheckSystemWpiLbVersionAction extends AbstractFrcToolsAction
{
    private static final Logger LOG = Logger.getInstance(CheckSystemWpiLbVersionAction.class);


    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final Project project = e.getProject();
        if (project == null)
        {
            return;
        }

        final WpiLibVersionStatus versionStatus = WpiLibVersionStatus.getCurrentVersionStatus(project);
        LOG.info("[FRC] WpiLib Version Status: " + versionStatus);

        //TODO: i18n


        final String title = FrcMessageBundle.message("frc.wpilib.version.dialog.title");
        @Nullable
        String yesText = null;
        
        final String indent = "&nbsp;&nbsp;&nbsp;&nbsp;"; 
        StringBuilder sb = new StringBuilder("<html>");
        sb.append("WPILib Version Status:<br>");


        // CASES
        //      1)  Attached and...
        //          a) a new version is available
        //          b) they have the latest
        //      2) Not Attached and...
        //          a) Is Downloaded and...
        //               i) a new version is available
        //              ii) the latest is downloaded
        //          b) Is NOT downloaded


        //TODO need to handle the case of a new version is downloaded than attached - what to do????
        
        if (versionStatus.isWpiLibAttached())
        {
            sb.append(indent).append(versionStatus.getAttachedVersionSummary()).append("<br>");

            if (versionStatus.isNewerVersionAvailableThanAttached() /*|| versionStatus.isNewerVersionAvailableThanDownloaded()*/)
            {
                
                // CASE 1a: Attached, but a newer version is available
                sb.append(indent).append(versionStatus.getAvailableVersionSummary()).append("<br>");
                sb.append("<br>");
                sb.append("Would you like to download the newer Version?<br>");
                yesText = "Download";
                
            }
            else
            {
                // CASE 1b: Attached, and it's the latest
                sb.append(indent).append("You have the latest available version.").append("<br>");
            }
            
        }
        else // WPILib is not attached 
        {
            if (versionStatus.isWpiLibDownloaded())
            {
                sb.append(indent).append(versionStatus.getDownloadedVersionSummary()).append(".<br>");
                sb.append(indent).append("But it is not attached as a Project Library.").append("<br>");
                
                if (versionStatus.isNewerVersionAvailableThanDownloaded())
                {
                    // CASE 2a, i) Not attached, downloaded, but a new versions is available.
                    sb.append(indent).append(versionStatus.getAvailableVersionSummary()).append("<br>");
                    sb.append("<br>");
                    sb.append("Would you like to download the newer version and attach it as a Project Library?<br>");
                    yesText = "Download & Attach";
                }
                else
                {
                    // // CASE 2a, ii) Not attached, downloaded and is the latest
                    sb.append(indent).append("This is the latest version available.<br>");
                    sb.append("<br>");
                    sb.append("Would you like to attach the already downloaded WPILib as a Project Library?<br>");
                    yesText = "Attach";
                    
                }
            }
            else 
            {
                // CASE 2b: is neither downloaded nor attached
                sb.append(indent).append(versionStatus.getDownloadedVersionSummary()).append("<br>");
                sb.append("<br>");
                sb.append("Would you like to download this version and attach it as a Project Library?<br>");
                yesText = "Download & Attach";
            }
        }

        sb.append("</html>");
        
        
        
       if (yesText != null)
       {
           final int answer = Messages.showYesNoDialog(project,
                                                       sb.toString(),
                                                       title,
                                                       yesText,
                                                       "Cancel",
                                                       null);

           if (Messages.YES == answer)
           {
               if ("Attach".equals(yesText))
               {
                   AttachWpilibAction.attachWpiLib(project, true, true);
               }
               else 
               {
                   DownloadWpiLibAction.downloadLatestInBackground(project, true, true);
               }
           }
       }
       else
       {
           Messages.showInfoMessage(project,
                                    sb.toString(),
                                    title);
       }
    }


    @Override
    public void update(AnActionEvent e)
    {
        //TODO: Make this action unavailable if a download is currently running in the background
        final Project project = e.getData(CommonDataKeys.PROJECT);
        e.getPresentation().setVisible(project != null 
                                       && !project.isDisposed() 
                                       && FrcFacet.isFrcFacetedProject(project) 
                                       //&& WpiLibLibrariesUtils.isWpilibAttachedViaReadAction(project)
        );
    }
}
