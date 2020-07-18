/*
 * Copyright 2015-2020 the original author or authors
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

package net.javaru.iip.frc.actions.tools.legacy;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.notification.Notification;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.notify.FrcNotificationType;
import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.util.IndexUtils;
import net.javaru.iip.frc.wpilib.legacy.LegacyWpiLibLibrariesUtils;
import net.javaru.iip.frc.wpilib.legacy.retrieval.LegacyWpiLibDownloadFailedException;
import net.javaru.iip.frc.wpilib.legacy.retrieval.LegacyWpiLibDownloader;

import static net.javaru.iip.frc.components.FrcProjectComponentImpl.cancelLegacyWpiLibIsDownloadingNotifications;



public class DownloadLegacyWpiLibAction extends AbstractFrcToolsLegacyAction
{
    private static final Logger LOG = Logger.getInstance(DownloadLegacyWpiLibAction.class);

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
        downloadLatestInBackground(project, autoAttach, notifyOnCompletion, true, null, null);
    }
    
    public static void downloadLatestInBackground(@Nullable final Project project, final boolean autoAttach, final boolean notifyOnCompletion, boolean notifyOnFailure)
    {
        downloadLatestInBackground(project, autoAttach, notifyOnCompletion, notifyOnFailure, null, null);
    }
    
    public static void downloadLatestInBackground(@Nullable final Project project, 
                                                  final boolean autoAttach, 
                                                  final boolean notifyOnCompletion,
                                                  boolean notifyOnFailure,
                                                  @Nullable Runnable onSuccessAction,
                                                  @Nullable Runnable onFailAction)
    {
        
        // TODO: The isAttached concepts needs some rework:
        //    It needs to separate the use case of the default 'wpilib/java/lib' dir is attached as a project library (currently the only option)
        //    and the use case of the individual JARs attached. 
        //    The latter is complicated by the fact that JARs may be added, renamed and/or refactored, or removed from one year to the next 
        //    A possible solution is an all or nothing: autoManaged (with the possibility of an alternate dir location) and manually manged.
        
        new Backgroundable(project, "Downloading WPILib Update", false)
        {
            final boolean wasAttached = project != null && LegacyWpiLibLibrariesUtils.isLegacyWpilibJavaLibDirAttachedViaReadAction(project);
            boolean isAttached = wasAttached;
            

            @Override
            public void run(@NotNull ProgressIndicator indicator)
            {
                LOG.info("[FRC] Downloading latest WPILib...");
                LOG.debug("[FRC] wasAttached = " + wasAttached);
                LegacyWpiLibDownloader.downloadLatest();
                if (project != null)
                {
                    isAttached = LegacyWpiLibLibrariesUtils.isLegacyWpilibJavaLibDirAttachedViaReadAction(project);
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
                        AttachLegacyWpilibAction.attachWpiLib(project, !wasAttached, true);
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
                                       FrcNotifications.createNotification(FrcNotificationType.ACTIONABLE_INFO,
                                                                           "One or more WPILib JARs are not attached. <a href='attach'>Attach as library</a>",
                                                                            NOTIFICATIONS_SUBTITLE + " Completed Successfully",
                                                                           (theNotification, event) ->
                                                                            {
                                                                                if ("attach".equals(event.getDescription()))
                                                                                {
                                                                                    Logger.getInstance(DownloadLegacyWpiLibAction.class)
                                                                                          .debug("[FRC] Attaching WPILib library");
                                                                                    AttachLegacyWpilibAction.attachWpiLib(project, true, true);
                                                                                }
                                                                                theNotification.expire();
                                                                            })
                                       : null;
                    }
                }
                else
                {
                    notification = createNoActionSuccessNotification();
                }

                cancelLegacyWpiLibIsDownloadingNotifications(myProject);
                
                if (notification != null)
                {
                    notification.notify(myProject);
                }
                
                if (onSuccessAction != null)
                {
                    onSuccessAction.run();
                }
                
                // We reindex to catch the files that have changed
                IndexUtils.refreshAll(project);
            }


            @Override
            public void onThrowable(@NotNull Throwable error)
            {
                // A nice TODO: make the replacement of files a transaction with rollback if possible
                String content = "Cause: ";

                if (error instanceof LegacyWpiLibDownloadFailedException)
                {
                    content += (error.getCause() != null) ? error.getCause().toString() : error.getMessage();
                }
                else
                {
                    content += error.toString();
                }

                if (notifyOnFailure)
                {
                    FrcNotifications.notify(FrcNotificationType.ACTIONABLE_WARN,
                                            content,
                                             NOTIFICATIONS_SUBTITLE + " Failed",
                                            project);
                }
                
                if (onFailAction != null)
                {
                    onFailAction.run();
                }
                
                // We reindex to catch the files that have changed, which may have even have happened on a failure if it was a partial failure
                IndexUtils.refreshAll(project);
            }

        }.queue();
    }
    
    
    private static Notification createNoActionSuccessNotification()
    {
        return FrcNotifications.createNotification(FrcNotificationType.GENERAL_INFO,
                                                   "Download completed successfully.",
                                                   NOTIFICATIONS_SUBTITLE);
        
    }
}
