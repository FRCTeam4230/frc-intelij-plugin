/*
 * Copyright 2015-2018 the original author or authors
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


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.facet.FacetManager;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.facet.FrcFacetKt;
import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.util.FrcFileUtils;
import net.javaru.iip.frc.util.FrcProjectExtsKt;
import net.javaru.iip.frc.util.IndexUtils;
import net.javaru.iip.frc.util.LibDef;
import net.javaru.iip.frc.util.LibDefBuilder;
import net.javaru.iip.frc.util.LibDirType;
import net.javaru.iip.frc.util.LibraryUtilsKt;
import net.javaru.iip.frc.wpilib.WpiLibLibrariesUtils;
import net.javaru.iip.frc.wpilib.WpiLibPaths;
import net.javaru.iip.frc.wpilib.retrieval.WpiLibDownloader;




public class AttachWpilibAction extends AbstractFrcToolsAction
{
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getInstance(AttachWpilibAction.class);


    @Override
    public void update(AnActionEvent e)
    {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        e.getPresentation().setVisible(project != null &&
                                       !project.isDisposed() &&
                                       FrcFacetKt.isFrcFacetedProject(project) &&
                                       !FrcProjectExtsKt.isGradleProject(project)  && /* TODO: Need to reverse this and check if it is an Ant Based Project once the isAntBasedFrcProject method is implemented*/
                                       !WpiLibLibrariesUtils.isWpilibAttachedViaReadAction(project));
    }


    @Override
    public void actionPerformed(AnActionEvent actionEvent)
    {
        final Project project = actionEvent.getProject();
        attachWpiLib(project, true, true);
    }


    public static void attachWpiLib(Project project, final boolean notifyOnCompletion, boolean doReindex)
    {
        if (project != null)
        {
            try
            {
                final Module[] modules = ModuleManager.getInstance(project).getModules();
                for (Module module : modules)
                {
                    final FacetManager facetManager = FacetManager.getInstance(module);
                    final FrcFacet frcFacet = facetManager.getFacetByType(FrcFacet.Companion.getFACET_TYPE_ID());
                    if (frcFacet != null)
                    {
                        //TODO: need to see if it is present as a Project library, and if so, attach that
                        if (WpiLibLibrariesUtils.isWpilibAttachedViaReadAction(module))
                        {
                            Notifications.Bus.notify(new Notification(FrcNotifications.Companion.getFRC_GENERAL_NOTIFICATION_GROUP().getDisplayId(),
                                                                      FrcNotifications.Companion.getIconInfo(),
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

                        if (!(Files.isDirectory(wpiJavaLibDir) && FrcFileUtils.directoryHasJars(wpiJavaLibDir, true)))
                        {
                            final int response = Messages.showYesNoCancelDialog(project,
                                                                                "The WPILib JARs were not found on your system. How do you wish to proceed?",
                                                                                "WPILib Not Found",
                                                                                "Download and Attach",
                                                                                "Attach Empty Directory",
                                                                                "Cancel Without Downloading or Attaching",
                                                                                Messages.getQuestionIcon());
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

                        LibDef libDef = new LibDefBuilder(module, "WPILib Libraries").addDir(wpiJavaLibDir, LibDirType.BIN, LibDirType.SRC, LibDirType.DOC).build();
                        LibraryUtilsKt.attachDirectoryBasedLibrary(libDef);
                        if (notifyOnCompletion)
                        {
                            queueSuccessfulNotification(project);
                        }
                    }
                }
                if (doReindex)
                {
                    IndexUtils.refreshAll(project);
                }
            }
            catch (Exception e)
            {
                queueFailureNotification(project, e);
            }
        }
    }


    private static void queueFailureNotification(@Nullable Project project, @Nullable Exception e)
    {
        Notifications.Bus.notify(createFailureNotification(e), project);
    }


    @NotNull
    private static Notification createFailureNotification(@Nullable Exception e)
    {


        if (e != null)
        {
            LOG.info("[FRC] Failed to attach WPILib Cause: " + e.toString(), e);
        }

        String cause = e != null ? " Cause: " + e.toString() : "";
        return new Notification(FrcNotifications.Companion.getFRC_GENERAL_NOTIFICATION_GROUP().getDisplayId(),
                                FrcNotifications.Companion.getIconWarn(),
                                FrcNotifications.Title,
                                "WPILib",
                                "Could not attach WPILib JARs as a library." + cause,
                                NotificationType.WARNING,
                                null
        );
    }


    private static void queueSuccessfulNotification(@Nullable Project project)
    {
        Notifications.Bus.notify(createSuccessNotification(), project);
    }


    @NotNull
    private static Notification createSuccessNotification()
    {
        return new Notification(FrcNotifications.Companion.getFRC_GENERAL_NOTIFICATION_GROUP().getDisplayId(),
                                FrcNotifications.Companion.getIconInfo(),
                                FrcNotifications.Title,
                                "WPILib",
                                "WPILib JARs have been attached as a library.",
                                NotificationType.INFORMATION,
                                null
        );
    }
}