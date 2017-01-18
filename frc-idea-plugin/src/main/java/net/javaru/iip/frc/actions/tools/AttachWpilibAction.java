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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.facet.FacetManager;
import com.intellij.icons.AllIcons.General;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.ui.notify.FrcNotifications;
import net.javaru.iip.frc.util.FrcFileUtils;
import net.javaru.iip.frc.util.LibraryUtils;
import net.javaru.iip.frc.wpilib.WpiLibPaths;
import net.javaru.iip.frc.wpilib.attached.WpiLibrariesUtils;
import net.javaru.iip.frc.wpilib.retrieval.WpiLibDownloader;


public class AttachWpilibAction extends AbstractFrcToolsAction
{
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getInstance(AttachWpilibAction.class);


    @Override
    public void actionPerformed(AnActionEvent actionEvent)
    {
        final Project project = actionEvent.getProject();
        if (project != null)
        {
            try
            {
                final Module[] modules = ModuleManager.getInstance(project).getModules();
                for (Module module : modules)
                {
                    final FacetManager facetManager = FacetManager.getInstance(module);
                    final FrcFacet frcFacet = facetManager.getFacetByType(FrcFacet.FACET_TYPE_ID);
                    if (frcFacet != null)
                    {
                        //TODO: need to see if it is present as a Project library, and if so, attach that
                        if (WpiLibrariesUtils.isWpilibPresent(module))
                        {
                            Notifications.Bus.notify(new Notification(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP,
                                                                      FrcNotifications.IconInfo,
                                                                      FrcNotifications.Title,
                                                                      "WPILib",
                                                                      "WPILib is already attached as a library",
                                                                      NotificationType.WARNING,
                                                                      null
                            ), project);
                            return;
                        }
                        
                        
                        final Path wpiJavaLibDir = WpiLibPaths.getJavaLibDir();
                        try
                        {
                            Files.createDirectories(wpiJavaLibDir);
                        }
                        catch (IOException e)
                        {
                            LOG.debug("[FRC] Could not create wpiJavaDir. Cause summary: " + e.toString(), e);
                        }

                        if (!(Files.isDirectory(wpiJavaLibDir) && FrcFileUtils.doesDirectoryHaveJars(wpiJavaLibDir, true)))
                        {
                            final int response = Messages.showYesNoCancelDialog(project,
                                                                                "The WPILib JARs were not found on your system. How do you wish to proceed?",
                                                                                "WPILib Not Found",
                                                                                "Download and Attach",
                                                                                "Attach Empty Directory",
                                                                                "Cancel Without Downloading or Attaching",
                                                                                General.QuestionDialog);
                            if (response == Messages.YES)
                            {
                                new Backgroundable(project, "Downloading WPILib Update", false)
                                {
                                    @Override
                                    public void run(@NotNull ProgressIndicator indicator)
                                    {
                                        WpiLibDownloader.downloadLatest();
                                    }
                                }.queue();
                            }
                            else if (response == Messages.CANCEL)
                            {
                                return;
                            }
                        }

                        LibraryUtils.attachDirectoryBasedLibrary(module, "WPILib Libraries", wpiJavaLibDir.toString());
                        queueSuccessfulNotification(project);
                    }
                }
            }
            catch (Exception e)
            {
                queueFailureNotification(project, e);
            }
        }
    }

    
    private void queueFailureNotification(@Nullable Project project, @Nullable Exception e)
    {
        Notifications.Bus.notify(createFailureNotification(e), project);
    }


    @NotNull
    private Notification createFailureNotification(@Nullable Exception e)
    {


        if (e != null)
        {
            LOG.info("[FRC] Failed to attach WPILib Cause: " + e.toString(), e);
        }

        String cause = e != null ? "Cause: " + e.toString() : "";
        return new Notification(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP,
                                FrcNotifications.IconWarn,
                                FrcNotifications.Title,
                                "WPILib",
                                "Could not attach WPILib JARs as a library." + cause,
                                NotificationType.WARNING,
                                null
        );
    }


    private void queueSuccessfulNotification(@Nullable Project project)
    {
        Notifications.Bus.notify(createSuccessNotification(), project);
    }


    @NotNull
    private Notification createSuccessNotification()
    {
        return new Notification(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP,
                                FrcNotifications.IconInfo,
                                FrcNotifications.Title,
                                "WPILib",
                                "WPILib JARs have been attached as a library",
                                NotificationType.INFORMATION,
                                null
        );
    }
}
