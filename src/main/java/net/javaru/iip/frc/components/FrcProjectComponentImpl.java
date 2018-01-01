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

package net.javaru.iip.frc.components;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetManagerAdapter;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;

import net.javaru.iip.frc.actions.tools.AttachUserLibDirAction;
import net.javaru.iip.frc.actions.tools.AttachWpilibAction;
import net.javaru.iip.frc.actions.tools.DownloadWpiLibAction;
import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.riolog.RioLogProjectService;
import net.javaru.iip.frc.riolog.udp.RioLogUdpSocketManagerApplicationService;
import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.util.FrcFileUtils;
import net.javaru.iip.frc.wpilib.WpiLibLibrariesUtils;
import net.javaru.iip.frc.wpilib.version.WpiLibVersionStatus;

import static net.javaru.iip.frc.FrcPluginGlobals.TEAM_NUM_NOTIFY_RUN_COUNT_PROJECT_LEVEL_NON_FRC_PROJECT;
import static net.javaru.iip.frc.components.FrcProjectComponentImpl.NotificationKey.*;



public class FrcProjectComponentImpl implements FrcProjectComponent
{
    private static final Logger LOG = Logger.getInstance(FrcProjectComponentImpl.class);

    @NotNull
    private final Project myProject;
        

    private final static Map<Project, Map<NotificationKey, Notification>> notificationsTracker =  new HashMap<>(); 
    
    
    protected enum NotificationKey {TeamNumConfigured, WpilibAttached, UserLibAttached}

    /** Do not call constructor directly. Use the static {@link #getInstance(Project)} method. */
    public FrcProjectComponentImpl(@NotNull Project project)
    {
        this.myProject = project;
    }
    
    @Nullable
    public static FrcProjectComponent getInstance(@NotNull Project project)
    {
        return project.getComponent(FrcProjectComponent.class);
    }
        
    
    @Override
    public void projectOpened()
    {
        final FrcApplicationSettings appSettings = FrcApplicationSettings.Settings.INSTANCE();
        
        LOG.debug("[FRC] " + getClass().getSimpleName() + ".projectOpened() called for project " + myProject);
        LOG.debug("[FRC] wpiLibDir: " + appSettings.getWpiLibDir());
        
        registerMessageBusListeners();
        
        
        
        final boolean isTemplateFirstOpen = templateCreationCleanup();

        if (isTemplateFirstOpen || appSettings.getCheckForNewWpiLibVersionOnProjectOpen())
        {
            final WpiLibVersionStatus versionStatus = WpiLibVersionStatus.getCurrentVersionStatus(myProject);
            if (!isTemplateFirstOpen)
            {
                LOG.debug("[FRC] On project WpiLib Version Status: " + versionStatus);
                if (versionStatus.isNewerVersionAvailableThanAttached())
                {
                    
                    if (appSettings.getAutoDownloadNewWpiLibVersions())
                    {
                        // TODO Prompt user if they would like to download new version.
                    }
                    else
                    {
                        // TODO auto download and then notify user
                    }
                    
                }
                else
                {
                    // TODO auto attach wpilib and user dir if fresh project
                    
                    // Possibilities
                    //    1) WpiLib is not downloaded
                    //       a) can be downloaded                           - ask permission and download it, auto attach
                    //       b) cannot be downloaded                        - notify - possibly link to how to manually download
                    //    2) WpiLib is downloaded (may already be attached, unlikely, but check just in case)
                    //       a) Has the latest version                      - auto attach 
                    //       b) need a newer version                        - ask permission and download it, auto attach
                    //       c) Can not determine what latest version is    - we can still attach, but need to notify
                    //
                    //  1a and 2b are the same, except for a different message in the notification.
                    //  1b and 2c are just notifications
                    //  2a is the simplest use case


                    LOG.debug("[FRC] This is first open for new project from template. WpiLib Status: " + versionStatus);
                }
            }
        }
        
        
        // TODO: See if we need to call RioLogProjectService.update(myProject) in any way. FrcModuleComponentImpl.moduleAdded(), which we need if someone adds a module to an existing project, and moduleAdded is called during a project opening
        // RioLogProjectService.update(myProject);

        // For example, see com.intellij.framework.detection.impl.FrameworkDetectionManager#projectOpened
        StartupManager.getInstance(myProject).registerPostStartupActivity(() -> RioLogProjectService.activateUdpNow(myProject));
        StartupManager.getInstance(myProject).registerPostStartupActivity(() -> notifyToConfigureTeamNumIfNecessary(myProject));
        StartupManager.getInstance(myProject).registerPostStartupActivity(() -> checkProjectFrcStatus(myProject, false));
    }


    @Override
    public void projectClosed()
    {
        LOG.debug("[FRC] " + getClass().getSimpleName() + ".projectClosed() called for project " + myProject);
        clearNotificationsTrackerMap();
        ServiceManager.getService(RioLogUdpSocketManagerApplicationService.class).deregister(myProject);
    }


    public void clearNotificationsTrackerMap()
    {
        try
        {
            final Map<NotificationKey, Notification> notificationMap = notificationsTracker.remove(myProject);
            if (notificationMap != null && !notificationMap.isEmpty())
            {
                notificationMap.values().forEach(Notification::expire);
                notificationMap.clear();
            }
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] An exception occurred when clearing the notifications tracker map for project '" + myProject + "'. Cause: " + e.toString());
        }
    }


    @Override
    public void initComponent()
    {
        LOG.debug("[FRC] " + getClass().getSimpleName() + ".initComponent() called for project " + myProject);
    }


    @Override
    public void disposeComponent()
    {
        LOG.debug("[FRC] " + getClass().getSimpleName() + ".disposeComponent() called for project " + myProject);
    }


    @Override
    public boolean isThisProjectFrcFaceted() { return FrcFacet.isFrcFacetedProject(myProject); }

    public static boolean isFrcFacetedProject(@Nullable Project project) {return FrcFacet.isFrcFacetedProject(project);}


    private boolean templateCreationCleanup()
    {
        boolean isFreshTemplateProject = false;
        try
        {
            final VirtualFile projectFile = myProject.getProjectFile();
            if (projectFile != null)
            {
                final VirtualFile ideaDir = projectFile.getParent();
                final VirtualFile projectTemplateFile = ideaDir.findChild("project-template.xml");
                if (projectTemplateFile != null && projectTemplateFile.exists())
                {
                    isFreshTemplateProject = true;
                    FrcFileUtils.deleteSafely(projectTemplateFile, this);
                    // This is assuming a standard template was used...
                    final VirtualFile srcDir = myProject.getBaseDir().findChild("src");
                    if (srcDir != null && srcDir.exists())
                    {
                        final VirtualFile frcDir = srcDir.findChild("frc");
                        if (frcDir != null && frcDir.exists())
                        {
                            final VirtualFile teamDir = frcDir.findChild("team0000");
                            FrcFileUtils.deleteSafelyIfEmpty(teamDir, this);
                            FrcFileUtils.deleteSafelyIfEmpty(frcDir, this);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] Could not check for and/or cleanup template files. Cause: " + e.toString(), e);
        }

        return isFreshTemplateProject;
    }
    
    public static void checkProjectFrcStatus(@NotNull Project project, boolean knownFacetedProject)
    {
        DumbService.getInstance(project).runWhenSmart(() ->
                                                      {
                                                          final Map<NotificationKey, Notification> notificationMap = getNotificationMapForProject(project);
                                                          notifyToConfigureTeamNumIfNecessary(project);

                                                          if (knownFacetedProject || isFrcFacetedProject(project))
                                                          {
                                                              if (notificationMap.get(WpilibAttached) == null && !WpiLibLibrariesUtils.isWpilibAttachedViaReadAction(project))
                                                              {
                                                                  final Notification notification = 
                                                                  (WpiLibLibrariesUtils.isWpilibDownloadedToSystem())
                                                                      ? queueAttachWpilibNotification(project)
                                                                      : queueDownloadAndAttachWpilibNotification(project);
                                                                  notificationMap.put(WpilibAttached, notification);
                                                              }

                                                              if (notificationMap.get(UserLibAttached) == null && !WpiLibLibrariesUtils.isUserLibAttachedViaReadAction(project))
                                                              {
                                                                  final Notification notification = queueMissingUserLibNotification(project);
                                                                  notificationMap.put(UserLibAttached, notification);
                                                              }
                                                          }
                                                      });
    }


    private static void notifyToConfigureTeamNumIfNecessary(@NotNull Project project)
    {
        final Map<NotificationKey, Notification> notificationMap = getNotificationMapForProject(project);
        final FrcApplicationSettings settings = FrcApplicationSettings.Settings.INSTANCE();

        final boolean shouldNotify = !settings.isTeamNumberConfigured()
                                     &&
                                     (isFrcFacetedProject(project) || settings.getPrc() <= TEAM_NUM_NOTIFY_RUN_COUNT_PROJECT_LEVEL_NON_FRC_PROJECT)
                                     &&
                                     notificationMap.get(TeamNumConfigured) == null; //Don't publish multiple notifications for same project

        if (shouldNotify)
        {
            if (LOG.isDebugEnabled())
            { 
                LOG.debug("[FRC] Publishing 'configure team number' notification for Project '" + project + "'"); }
            // Expire the application level notification to prevent duplicate notification in the event log
            FrcNotifications.expireConfigureTeamNumberNotification(null);
            final Notification notification = FrcNotifications.notifyAboutTeamNumberNeedingToBeConfigured( project, true);
            notificationMap.put(TeamNumConfigured, notification);
        }
    }

    private static Notification queueDownloadAndAttachWpilibNotification(@NotNull Project project)
    {
        final Notification notification = new Notification(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP.getDisplayId(),
                                                           FrcNotifications.IconInfo,
                                                           FrcNotifications.Title,
                                                           "WPILib Not Found on System",
                                                           "Would you like to <a href='download'>download and attach</a> WPILib as a Module Library?",
                                                           NotificationType.INFORMATION,
                                                           (theNotification, event) ->
                                                           {
                                                               theNotification.expire();
                                                               if ("download".equals(event.getDescription()))
                                                               {
                                                                   DownloadWpiLibAction.downloadLatestInBackground(project, true, true);
                                                               }
                                                           }
        );
        Notifications.Bus.notify(notification, null);
        return notification;
    }


    private static Notification queueAttachWpilibNotification(@NotNull Project project)
    {
        final Notification notification = new Notification(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP.getDisplayId(),
                                                           FrcNotifications.IconInfo,
                                                           FrcNotifications.Title,
                                                           "WPILib not Attached",
                                                           "Would you like to <a href='attach'>attach</a> WPILib as a Module Library?",
                                                           NotificationType.INFORMATION,
                                                           (theNotification, event) ->
                                                           {
                                                               theNotification.expire();
                                                               if ("attach".equals(event.getDescription()))
                                                               {
                                                                   AttachWpilibAction.attachWpiLib(project, false, true);
                                                               }
                                                           }
        );
        Notifications.Bus.notify(notification, null);
        return notification;
    }


    private static Notification queueMissingUserLibNotification(@NotNull Project project)
    {
        final Notification notification = new Notification(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP.getDisplayId(),
                                                           FrcNotifications.IconInfo,
                                                           FrcNotifications.Title,
                                                           "User Lib Directory Not Attached",
                                                           "Would you like to <a href='attach'>attach</a> the User Lib directory as a Module Library?",
                                                           NotificationType.INFORMATION,
                                                           (theNotification, event) ->
                                                           {
                                                               theNotification.expire();
                                                               if ("attach".equals(event.getDescription()))
                                                               {
                                                                   AttachUserLibDirAction.attachUserLib(project, false);
                                                               }

                                                           }
        );
        Notifications.Bus.notify(notification, null);
        return notification;
    }


    @SuppressWarnings("unused")
    private Map<NotificationKey, Notification> getNotificationsMap()
    {
        return getNotificationMapForProject(myProject);
    }


    private static Map<NotificationKey, Notification> getNotificationMapForProject(@NotNull Project project)
    {
        return notificationsTracker.computeIfAbsent(project, (proj) -> new HashMap<>());
    }


    private static void registerMessageBusListeners()
    {
        try
        {
            ApplicationManager.getApplication().getMessageBus().connect().subscribe(FacetManager.FACETS_TOPIC, new FrcFacetManagerListener());
        }
        catch (IllegalStateException e)
        {
            LOG.info("[FRC] Already Subscribed for FACETS_TOPIC: " + e.toString(), e);
        }
    }

    public static class FrcFacetManagerListener extends FacetManagerAdapter
    {
        @Override
        public void facetAdded(@NotNull Facet facet)
        {
            if (FrcFacet.isFrcFacet(facet))
            {
                updateForFrcFacet(facet);
                checkProjectFrcStatus(facet.getModule().getProject(), true);
            }
        }


        @Override
        public void facetConfigurationChanged(@NotNull Facet facet)
        {
            updateForFrcFacet(facet);
        }


        @Override
        public void facetRemoved(@NotNull Facet facet)
        {
            updateForFrcFacet(facet);
        }


        private void updateForFrcFacet(@NotNull Facet facet)
        {
            if (FrcFacet.isFrcFacet(facet))
            {
                RioLogProjectService.update(facet);
            }
        }
    }
}
