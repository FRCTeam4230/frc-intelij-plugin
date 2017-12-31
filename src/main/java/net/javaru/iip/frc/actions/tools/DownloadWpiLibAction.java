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

import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.util.IndexUtils;
import net.javaru.iip.frc.wpilib.WpiLibLibrariesUtils;
import net.javaru.iip.frc.wpilib.retrieval.WpiLibDownloadFailedException;
import net.javaru.iip.frc.wpilib.retrieval.WpiLibDownloader;



public class DownloadWpiLibAction extends AbstractFrcToolsAction
{
    private static final Logger LOG = Logger.getInstance(DownloadWpiLibAction.class);

    public static final String NOTIFICATIONS_SUBTITLE = "WPILib Download";

    //TODO: Make this action unavailable if it is currently running in the background

    @Override
    public void actionPerformed(AnActionEvent actionEvent)
    {
        @Nullable
        final Project project = actionEvent.getProject();
        downloadLatestInBackground(project, false, true);
    }


    public static void downloadLatestInBackground(@Nullable final Project project, final boolean autoAttach, final boolean notifyOnCompletion)
    {
        
        // TODO: The isAttached concepts needs some rework:
        //    It needs to separate the use case of the default 'wpilib/java/lib' dir is attached as a project library (currently the only option)
        //    and the use case of the individual JARs attached. 
        //    The latter is complicated by the fact that JARs may be added, renamed and/or refactored, or removed from one year to the next 
        //    A possible solution is an all or nothing: autoManaged (with the possibility of an alternate dir location) and manually manged.
        
        new Backgroundable(project, "Downloading WPILib Update", false)
        {
            final boolean wasAttached = project != null && WpiLibLibrariesUtils.isWpilibJavaLibDirAttachedViaReadAction(project);
            boolean isAttached = wasAttached;
            

            @Override
            public void run(@NotNull ProgressIndicator indicator)
            {
                LOG.info("[FRC] Downloading latest WPILib...");
                LOG.debug("[FRC] wasAttached = " + wasAttached);
                WpiLibDownloader.downloadLatest();
                if (project != null)
                {
                    isAttached = WpiLibLibrariesUtils.isWpilibJavaLibDirAttachedViaReadAction(project);
                    LOG.debug("[FRC] After download. isAttached = " + isAttached);
                }
            }


            @Override
            public void onSuccess()
            {
                LOG.info("[FRC] Downloading latest WPILib completed successfully.");
                LOG.debug("[FRC] onSuccess() called for WPILib download. isAttached = " + isAttached + "  wasAttached = " + wasAttached);
                @Nullable
                final Notification notification;

                if (project != null && (wasAttached || !isAttached))
                {
                    if (autoAttach)
                    {
                        LOG.info("[FRC] Auto-attaching WPILib after download.");
                        AttachWpilibAction.attachWpiLib(project, !wasAttached, true);
                        notification = null;
                    }
                    else if (wasAttached)
                    {
                        IndexUtils.refreshAll(project);
                        notification = null;
                    }
                    else
                    {
                        notification = notifyOnCompletion ?
                                       new Notification(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP.getDisplayId(),
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
                                                                AttachWpilibAction.attachWpiLib(project, true, true);
                                                            }
                                                            theNotification.expire();
                                                        }
                                       )
                                       : null;
                    }
                }
                else
                {
                    notification = createNoActionSuccessNotification();
                }

                if (notification != null)
                {
                    Notifications.Bus.notify(notification, myProject);
                }
                // We reindex to catch the files that have changed
                IndexUtils.refreshAll(project);
            }


            @Override
            public void onThrowable(@NotNull Throwable error)
            {
                // A nice TODO: make the replacement of files a transaction with rollback if possible
                String content = "Cause: ";

                if (error instanceof WpiLibDownloadFailedException)
                {
                    content += (error.getCause() != null) ? error.getCause().toString() : error.getMessage();
                }
                else
                {
                    content += error.toString();
                }

                Notifications.Bus.notify(new Notification(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP.getDisplayId(),
                                                          FrcNotifications.IconWarn,
                                                          FrcNotifications.Title,
                                                          NOTIFICATIONS_SUBTITLE + " Failed",
                                                          content,
                                                          NotificationType.WARNING,
                                                          null
                ), project);
                // We reindex to catch the files that have changed, which may have even have happened on a failure if it was a partial failure
                IndexUtils.refreshAll(project);
            }

        }.queue();
    }


    private static Notification createNoActionSuccessNotification()
    {
        return new Notification(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP.getDisplayId(),
                                FrcNotifications.IconInfo,
                                FrcNotifications.Title,
                                NOTIFICATIONS_SUBTITLE,
                                "Download completed successfully.",
                                NotificationType.INFORMATION,
                                null
        );
    }
}
