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

import net.javaru.iip.frc.actions.tools.AttachUserLibDirAction;
import net.javaru.iip.frc.actions.tools.AttachWpilibAction;
import net.javaru.iip.frc.actions.tools.DownloadWpiLibAction;
import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.riolog.udp.RioLogUdpSocketManagerApplicationService;
import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.wpilib.WpiLibLibrariesUtils;

import static net.javaru.iip.frc.FrcPluginGlobals.TEAM_NUM_NOTIFY_RUN_COUNT_PROJECT_LEVEL;
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
        LOG.debug("[FRC] " + getClass().getSimpleName() + ".projectOpened() called for project " + myProject);
        registerMessageBusListeners();
        
        //TODO: Still need to copy stuff over from original - primarily RioLog update
        // RioLogConsoleProjectService.update(myProject);
        
        StartupManager.getInstance(myProject).registerPostStartupActivity(() -> notifyToConfigureTeamNumIfNecessary(myProject));
    }


    @Override
    public void projectClosed()
    {
        LOG.debug("[FRC] " + getClass().getSimpleName() + ".projectClosed() called for project " + myProject);
        final Map<NotificationKey, Notification> notificationMap = notificationsTracker.remove(myProject);
        notificationMap.values().forEach(Notification::expire);
        notificationMap.clear();
        ServiceManager.getService(RioLogUdpSocketManagerApplicationService.class).deregister(myProject);
        
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


    public static void checkProjectFrcStatus(@NotNull Project project, boolean knownFacetedProject)
    {
        DumbService.getInstance(project).runWhenSmart(() ->
                                                      {
                                                          final Map<NotificationKey, Notification> notificationMap = getNotificationMapForProject(project);
                                                          notifyToConfigureTeamNumIfNecessary(project);

                                                          if (knownFacetedProject || isFrcFacetedProject(project))
                                                          {
                                                              if (notificationMap.get(WpilibAttached) == null && !WpiLibLibrariesUtils.isWpilibPresent(project))
                                                              {
                                                                  final Notification notification = 
                                                                  (WpiLibLibrariesUtils.isWpilibInstalledOnSystem())
                                                                      ? queueAttachWpilibNotification(project)
                                                                      : queueDownloadAndAttachWpilibNotification(project);
                                                                  notificationMap.put(WpilibAttached, notification);
                                                              }

                                                              if (notificationMap.get(UserLibAttached) == null && !WpiLibLibrariesUtils.isUserLibAttached(project))
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
                                     (isFrcFacetedProject(project) || settings.getPrc() <= TEAM_NUM_NOTIFY_RUN_COUNT_PROJECT_LEVEL)
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
        final Notification notification = new Notification(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP,
                                                           FrcNotifications.IconInfo,
                                                           FrcNotifications.Title,
                                                           "WPILib Not Found on System",
                                                           "Would you like to <a href='download'>download and attach</a> WPILib?",
                                                           NotificationType.INFORMATION,
                                                           (theNotification, event) ->
                                                           {
                                                               theNotification.expire();
                                                               if ("download".equals(event.getDescription()))
                                                               {
                                                                   DownloadWpiLibAction.downloadLatestInBackground(project, true);
                                                               }
                                                           }
        );
        Notifications.Bus.notify(notification, null);
        return notification;
    }


    private static Notification queueAttachWpilibNotification(@NotNull Project project)
    {
        final Notification notification = new Notification(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP,
                                                           FrcNotifications.IconInfo,
                                                           FrcNotifications.Title,
                                                           "WPILib not Attached",
                                                           "Would you like to <a href='attach'>attach</a> WPILib as a library?",
                                                           NotificationType.INFORMATION,
                                                           (theNotification, event) ->
                                                           {
                                                               theNotification.expire();
                                                               if ("attach".equals(event.getDescription()))
                                                               {
                                                                   AttachWpilibAction.attachWpiLib(project, false);
                                                               }
                                                           }
        );
        Notifications.Bus.notify(notification, null);
        return notification;
    }


    private static Notification queueMissingUserLibNotification(@NotNull Project project)
    {
        final Notification notification = new Notification(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP,
                                                           FrcNotifications.IconInfo,
                                                           FrcNotifications.Title,
                                                           "User Lib Directory Not Attached",
                                                           "Would you like to <a href='attach'>attach</a> the User Lib directory as a Library?",
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
                //TODO need to implement once RioLogConsoleProjectService is reworked
                //RioLogConsoleProjectService.update(facet);
                LOG.info("TODO: need to FrcProjectComponentImpl.implement updateForFrcFacet(Facet) once RioLogConsoleProjectService is reworked");
            }
        }
    }
}
