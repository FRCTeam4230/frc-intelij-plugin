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

package net.javaru.iip.frc.components;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetManagerAdapter;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;

import net.javaru.iip.frc.actions.tools.legacy.AttachLegacyUserLibDirAction;
import net.javaru.iip.frc.actions.tools.legacy.AttachLegacyWpilibAction;
import net.javaru.iip.frc.actions.tools.legacy.DownloadLegacyWpiLibAction;
import net.javaru.iip.frc.facet.FrcFacetKt;
import net.javaru.iip.frc.notify.FrcNotificationType;
import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.notify.FrcTeamNumberNotificationsKt;
import net.javaru.iip.frc.plugin.FrcPluginVersionManager;
import net.javaru.iip.frc.riolog.RioLogProjectService;
import net.javaru.iip.frc.riolog.udp.RioLogUdpSocketManagerApplicationService;
import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.settings.FrcProjectTeamNumberService;
import net.javaru.iip.frc.util.FrcProjectExtsKt;
import net.javaru.iip.frc.util.UriUtilsKt;
import net.javaru.iip.frc.wpilib.legacy.LegacyWpiLibLibrariesUtils;

import static net.javaru.iip.frc.FrcPluginGlobals.TEAM_NUM_NOTIFY_RUN_COUNT_PROJECT_LEVEL_NON_FRC_PROJECT;
import static net.javaru.iip.frc.actions.tools.legacy.DownloadLegacyWpiLibAction.NOTIFICATIONS_SUBTITLE;
import static net.javaru.iip.frc.components.FrcProjectComponentImpl.NotificationKey.*;



// TODO: Issue #45: This projectComponent has a lot of technical debt, and frankly has become an ugly mess. Let's clean it up. We can migrate to Kotlin at the same time.
// TODO: Issue #46: A lot of the notification work can be moved to a dedicated notification class/package.
// TODO: Issue #55: Migrate Plugin Components to Services, Extensions or Listeners as Components will become deprecated in the IntelliJ Plugin API
public class FrcProjectComponentImpl implements FrcProjectComponent
{
    private static final Logger LOG = Logger.getInstance(FrcProjectComponentImpl.class);
    
    @NotNull
    private final Project myProject;
    
    private final static Map<Project, Map<NotificationKey, Notification>> notificationsTracker = new HashMap<>();
    
    
    protected enum NotificationKey
    {ConfigureTeamNumberQuery, AttachWpiLibQuery, AttachUserLibQuery, DownloadNewWpiLibVersionQuery, LatestWpiLibIsBeingDownloaded}
    
    
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
        
        // IMPORTANT: Keep in mind the project is not yet fully initialized. 
        //            As such, some activities can not occur yet. Use:
        //               StartupManager.getInstance(myProject).registerPostStartupActivity(() -> someMethod(myProject));
        
        LOG.debug("[FRC] " + getClass().getSimpleName() + ".projectOpened() called for project " + myProject);
        
        registerMessageBusListeners();
        
        checkIssue8Refresh();
        
        //TODO the checkPluginUpdateStatus function needs to be rewritten, It currently is an empty function.
        FrcPluginVersionManager.INSTANCE.checkPluginUpdateStatus(myProject);
        StartupManager.getInstance(myProject).registerPostStartupActivity(this::runPostStartupActivities);
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
    public boolean isThisProjectFrcFaceted() { return FrcFacetKt.isFrcFacetedProject(myProject); }
    
    
    @Contract("null -> false")
    public static boolean isFrcFacetedProject(@Nullable Project project) {return FrcFacetKt.isFrcFacetedProject(project);}
    
    
    private void checkIssue8Refresh()
    {
        if (isFrcFacetedProject(myProject) && LegacyWpiLibLibrariesUtils.is2018CommonRefreshNeededViaReadAction())
        {
            final URI uri = UriUtilsKt.createUri("https://gitlab.com/Javaru/frc-intellij-idea-plugin/issues/8");
            final Notification refreshNotification = FrcNotifications.createNotification(FrcNotificationType.ACTIONABLE_INFO,
                                                                                          "In order to fully resolve "
                                                                                          + "<a href='" + uri
                                                                                          + "'>Issue #8: Cannot deploy code to roboRIO</a>, caused by a change "
                                                                                          + "to the 2018 WPILib, the previously downloaded WPILib and its associated tools needs to "
                                                                                          + "be refreshed. A fresh download has been started.",
                                                                                          NOTIFICATIONS_SUBTITLE + " Refresh Required",
                                                                                         NotificationListener.URL_OPENING_LISTENER);
            refreshNotification.notify(myProject);
            
            DownloadLegacyWpiLibAction.downloadLatestInBackground(myProject,
                                                                  true,
                                                                  false,
                                                                  true,
                                                                  () -> {
                                                                      refreshNotification.expire();
                                                                      FrcNotifications.notify(FrcNotificationType.GENERAL_INFO,
                                                                                              "Refresh of WPILib has completed",
                                                                                              NOTIFICATIONS_SUBTITLE,
                                                                                              myProject);
                
                                                                  },
                                                                  () -> {
                                                                      refreshNotification.expire();
                                                                      FrcNotifications.notify(FrcNotificationType.GENERAL_INFO,
                                                                                               "Refresh of WPILib failed. You will likely have problems deploying code to a 2018 roboRIO until "
                                                                                               + "a fresh copy of the WPILib is downloaded. Please verify your internet connection and download a "
                                                                                               + "fresh copy of the WPILIb and its tools via the menu: <em>Tools > FRC > Download Latest WPILib</em>",
                                                                                               NOTIFICATIONS_SUBTITLE + " Refresh Failed",
                                                                                              myProject);
                
                                                                  });
        }
    }
    
    
    private void runPostStartupActivities()
    {
        // TODO: Need some additional work to handle the variety of cases, especially a new version is available for download.
        
        boolean isFrcProject = isFrcFacetedProject(myProject);
        if (isFrcProject)
        {
            FrcProjectTeamNumberService.getInstance(myProject); // We need to initialize the registering of the VFS Change Listener so we can detect changes to the project team number
            RioLogProjectService.getInstance(myProject).activateTcp();
            notifyToConfigureTeamNumIfNecessary(myProject, true);
            checkProjectFrcStatus(myProject, true, false);
        }
    }
    
    
    public static void checkProjectFrcStatus(@NotNull Project project, boolean knownFacetedProject, boolean checkTeamNumConfigStatus)
    {
        // TODO add check for if a new version of WPILib is available
        DumbService.getInstance(project).runWhenSmart(() ->
                                                      {
                                                          if (checkTeamNumConfigStatus)
                                                          {
                                                              notifyToConfigureTeamNumIfNecessary(project, knownFacetedProject);
                                                          }
            
                                                          checkLegacyProjectLibraryAttachmentStatus(project, knownFacetedProject);
                                                      });
    }
    
    
    /**
     * Checks if a project is a legacy Ant based project, and if so, checks if the WPI Library and/or the User Library need to be attached.
     *
     * @param project             the project
     * @param knownFacetedProject if the project is already known to be an FrcFaceted project
     */
    private static void checkLegacyProjectLibraryAttachmentStatus(@NotNull Project project, boolean knownFacetedProject)
    {
        if ((knownFacetedProject || isFrcFacetedProject(project))
            && FrcProjectExtsKt.isAntBasedFrcProject(project)) /* TODO: The ant project check needs improvement. Right now it just checks it is not a gradle project  */
        {
            final Map<NotificationKey, Notification> notificationMap = getNotificationMapForProject(project);
            if (notificationMap.get(AttachWpiLibQuery) == null && !LegacyWpiLibLibrariesUtils.isLegacyWpilibAttachedViaReadAction(project))
            {
                if (LegacyWpiLibLibrariesUtils.isLegacyWpilibDownloadedToSystem())
                { queueAttachWpilibQueryNotification(project); }
                else
                { queueDownloadAndAttachWpilibNotification(project); }
            }
            
            if (notificationMap.get(AttachUserLibQuery) == null && !LegacyWpiLibLibrariesUtils.isLegacyUserLibAttachedViaReadAction(project))
            {
                queueMissingUserLibQueryNotification(project);
            }
        }
    }
    
    
    private static void notifyToConfigureTeamNumIfNecessary(@NotNull Project project, boolean knownFacetedProject)
    {
        final Map<NotificationKey, Notification> notificationMap = getNotificationMapForProject(project);
        final FrcApplicationSettings settings = FrcApplicationSettings.getInstance();
        
        final boolean shouldNotify = !settings.isTeamNumberConfigured()
                                     &&
                                     ((knownFacetedProject || isFrcFacetedProject(project))
                                      || settings.getPrc() <= TEAM_NUM_NOTIFY_RUN_COUNT_PROJECT_LEVEL_NON_FRC_PROJECT)
                                     &&
                                     notificationMap.get(ConfigureTeamNumberQuery) == null; //Don't publish multiple notifications for same project
        
        if (shouldNotify)
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("[FRC] Publishing 'configure team number' notification for Project '" + project + "'");
            }
            // Expire the application level notification to prevent duplicate notification in the event log
            FrcTeamNumberNotificationsKt.expireConfigureTeamNumberNotification(null);
            final Notification notification = FrcTeamNumberNotificationsKt.notifyAboutTeamNumberNeedingToBeConfigured(project, true, false);
            notificationMap.put(ConfigureTeamNumberQuery, notification);
        }
    }
    
    
    private static void queueDownloadAndAttachWpilibNotification(@NotNull Project project)
    {
        final Notification notification = FrcNotifications.notify(FrcNotificationType.ACTIONABLE_INFO,
                                                                  "Would you like to <a href='download'>download and attach</a> WPILib as a Module Library?",
                                                                  "WPILib Not Found on System",
                                                                  null,
                                                                  (theNotification, event) -> {
                                                                       if ("download".equals(event.getDescription()))
                                                                       {
                                                                           DownloadLegacyWpiLibAction.downloadLatestInBackground(project,
                                                                                                                                 true,
                                                                                                                                 true);
                                                                       }
                                                                   });
        getNotificationMapForProject(project).put(AttachWpiLibQuery, notification);
    }
    
    
    @SuppressWarnings("UnusedReturnValue")
    private static Notification queueAttachWpilibQueryNotification(@NotNull Project project)
    {
        final Notification notification = FrcNotifications.notify(FrcNotificationType.ACTIONABLE_INFO,
                                                                  "Would you like to <a href='attach'>attach</a> WPILib as a Module Library?",
                                                                  "WPILib not Attached",
                                                                  null,
                                                                  (theNotification, event) -> {
                                                                       theNotification.expire();
                                                                       if ("attach".equals(event.getDescription()))
                                                                       {
                                                                           AttachLegacyWpilibAction.attachWpiLib(project, false, true);
                                                                       }
                                                                   });
        getNotificationMapForProject(project).put(AttachWpiLibQuery, notification);
        return notification;
    }
    
    
    @SuppressWarnings("UnusedReturnValue")
    private static Notification queueMissingUserLibQueryNotification(@NotNull Project project)
    {
        final Notification notification = FrcNotifications.notify(FrcNotificationType.ACTIONABLE_INFO,
                                                                  "Would you like to <a href='attach'>attach</a> the User Lib directory as a Module Library?",
                                                                  "User Lib Directory Not Attached",
                                                                  null,
                                                                  (theNotification, event) ->
                                                                   {
                                                                       theNotification.expire();
                                                                       if ("attach".equals(event.getDescription()))
                                                                       {
                                                                           AttachLegacyUserLibDirAction.attachUserLib(project, false);
                                                                       }
                                                                   });
        getNotificationMapForProject(project).put(AttachUserLibQuery, notification);
        return notification;
    }
    
    
    public static void cancelLegacyWpiLibIsDownloadingNotifications(@NotNull Project project)
    {
        final Map<NotificationKey, Notification> notificationMap = getNotificationMapForProject(project);
        final Notification notification = notificationMap.remove(DownloadNewWpiLibVersionQuery);
        if (notification != null)
        {
            notification.expire();
        }
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
            if (FrcFacetKt.isFrcFacet(facet))
            {
                updateForFrcFacet(facet);
                checkProjectFrcStatus(facet.getModule().getProject(), true, true);
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
        
        
        private void updateForFrcFacet(@NotNull Facet<?> facet)
        {
            if (FrcFacetKt.isFrcFacet(facet))
            {
                RioLogProjectService.getInstance(facet.getModule().getProject()).update();
            }
        }
    }
}
