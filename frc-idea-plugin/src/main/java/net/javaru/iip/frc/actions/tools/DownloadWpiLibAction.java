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
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.ui.notify.FrcNotifications;
import net.javaru.iip.frc.wpilib.attached.WpiLibrariesUtils;
import net.javaru.iip.frc.wpilib.retrieval.WpiLibDownloadFailedException;
import net.javaru.iip.frc.wpilib.retrieval.WpiLibDownloader;



public class DownloadWpiLibAction extends AbstractFrcToolsAction
{

    public static final String NOTIFICATIONS_SUBTITLE = "WPILib Download";


    @Override
    public void actionPerformed(AnActionEvent actionEvent)
    {
        @Nullable
        final Project project = actionEvent.getProject();
        new Backgroundable(project, "Downloading WPILib Update", false)
        {
            boolean areAttached = false;

            @Override
            public void run(@NotNull ProgressIndicator indicator)
            {
                WpiLibDownloader.downloadLatest();
                areAttached = WpiLibrariesUtils.areAllPresentViaReadAction(project);
            }


            @Override
            public void onSuccess()
            {

                final Notification notification;

                if (!areAttached && project != null)
                {
                    notification = new Notification(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP,
                                                    FrcNotifications.IconInfo,
                                                    FrcNotifications.Title,
                                                    NOTIFICATIONS_SUBTITLE + " Completed Successfully",
                                                    "One or more WPILib JARs are not attached. <a href='attach'>Attach as library</a>",
                                                    NotificationType.INFORMATION,
                                                    (theNotification, event) ->
                                                    {
                                                        if ("attach".equals(event.getDescription()))
                                                        {
                                                            Logger.getInstance(DownloadWpiLibAction.class).debug("[FRC] Attaching WPILib library");
                                                            AttachWpilibAction.attachWpiLib(project);
                                                        }
                                                        theNotification.expire();
                                                    }
                    );
                }
                else
                {
                    notification = createNoActionSuccessNotification();
                }

                Notifications.Bus.notify(notification, myProject);

            }


            @Override
            public void onError(@NotNull Exception error)
            {

                String content = "Cause: " ;

                if (error instanceof WpiLibDownloadFailedException)
                {
                    content += (error.getCause() != null) ? error.getCause().toString() : error.getMessage();
                }
                else
                {
                    content += error.toString();
                }

                Notifications.Bus.notify(new Notification(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP,
                                                          FrcNotifications.IconWarn,
                                                          FrcNotifications.Title,
                                                          NOTIFICATIONS_SUBTITLE + " Failed",
                                                          content,
                                                          NotificationType.WARNING,
                                                          null
                ), project);
            }

        }.queue();
    }


    private Notification createNoActionSuccessNotification()
    {
        return new Notification(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP,
                                FrcNotifications.IconInfo,
                                FrcNotifications.Title,
                                NOTIFICATIONS_SUBTITLE,
                                "Download completed successfully.",
                                NotificationType.INFORMATION,
                                null
        );
    }
}
