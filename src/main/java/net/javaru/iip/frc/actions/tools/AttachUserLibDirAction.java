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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;

import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.util.IndexUtils;
import net.javaru.iip.frc.util.LibraryUtils;
import net.javaru.iip.frc.wpilib.WpiLibLibrariesUtils;
import net.javaru.iip.frc.wpilib.WpiLibPaths;




public class AttachUserLibDirAction extends AbstractFrcToolsAction
{
    private static final Logger LOG = Logger.getInstance(AttachUserLibDirAction.class);


    @Override
    public void update(AnActionEvent e)
    {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        e.getPresentation().setVisible(project != null &&
                                       !project.isDisposed() &&
                                       FrcFacet.isFrcFacetedProject(project) &&
                                       !WpiLibLibrariesUtils.isUserLibAttachedViaReadAction(project));
    }


    @Override
    public void actionPerformed(AnActionEvent actionEvent)
    {
        final Project project = actionEvent.getProject();
        if (project != null)
        {
            attachUserLib(project, true);
        }
    }


    public static void attachUserLib(@NotNull Project project, final boolean notifyOnCompletion)
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
                    final Library existing = WpiLibLibrariesUtils.findExistingUserLibDirLibrary(module);
                    if (existing != null)
                    {
                        Notifications.Bus.notify(new Notification(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP.getDisplayId(),
                                                                  FrcNotifications.IconInfo,
                                                                  FrcNotifications.Title,
                                                                  "User Lib Already Attached",
                                                                  "The FRC 'user/java/lib' directory is already attached via library '" + existing.getName() 
                                                                  + "'. Indexes are being refreshed." ,
                                                                  NotificationType.INFORMATION,
                                                                  null
                        ), project);
                    }
                    else
                    {
                        LibraryUtils.attachDirectoryBasedLibrary(module, "WPILib User Lib Directory", WpiLibPaths.getUserLibDir());
                        if (notifyOnCompletion)
                        {
                            queueSuccessfulNotification(project);
                        }
                    }
                    IndexUtils.refreshAll(project);
                }
            }
        }
        catch (Exception e)
        {
            queueFailureNotification(project, e);
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
            LOG.info("[FRC] Failed to attach User Lib Dir: " + e.toString(), e);
        }

        String cause = e != null ? "Cause: " + e.toString() : "";
        return new Notification(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP.getDisplayId(),
                                FrcNotifications.IconWarn,
                                FrcNotifications.Title,
                                "WPILib",
                                "Could not attach 'user/java/lib' dir as a library." + cause,
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
        return new Notification(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP.getDisplayId(),
                                FrcNotifications.IconInfo,
                                FrcNotifications.Title,
                                "WPILib",
                                "The 'user/java/lib' has been attached as a library. Indexes are being refreshed.",
                                NotificationType.INFORMATION,
                                null
        );
    }
}
