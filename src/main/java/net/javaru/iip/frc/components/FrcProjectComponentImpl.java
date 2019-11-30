/*
 * Copyright 2015-2019 the original author or authors
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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetManagerAdapter;
import com.intellij.ide.browsers.BrowserLauncherImpl;
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
import net.javaru.iip.frc.facet.FrcFacetKt;
import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.plugin.FrcPluginVersionManager;
import net.javaru.iip.frc.riolog.RioLogProjectService;
import net.javaru.iip.frc.riolog.udp.RioLogUdpSocketManagerApplicationService;
import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.util.FrcFileUtils;
import net.javaru.iip.frc.util.FrcProjectExtsKt;
import net.javaru.iip.frc.util.UriUtils;
import net.javaru.iip.frc.wpilib.WpiLibLibrariesUtils;
import net.javaru.iip.frc.wpilib.legacy.LegacyWpiLibVersionStatus;
import net.javaru.iip.frc.wpilib.version.WpiLibVersion;

import static net.javaru.iip.frc.FrcPluginGlobals.TEAM_NUM_NOTIFY_RUN_COUNT_PROJECT_LEVEL_NON_FRC_PROJECT;
import static net.javaru.iip.frc.actions.tools.DownloadWpiLibAction.NOTIFICATIONS_SUBTITLE;
import static net.javaru.iip.frc.components.FrcProjectComponentImpl.NotificationKey.*;


// TODO: This projectComponent has become an ugly mess. Let's clean it up. A lot of the notification work can be moved to a dedicated notification class.

public class FrcProjectComponentImpl implements FrcProjectComponent
{
    private static final Logger LOG = Logger.getInstance(FrcProjectComponentImpl.class);

    @NotNull
    private final Project myProject;
        

    private final static Map<Project, Map<NotificationKey, Notification>> notificationsTracker =  new HashMap<>(); 
    
    
    protected enum NotificationKey {ConfigureTeamNumberQuery, AttachWpiLibQuery, AttachUserLibQuery, DownloadNewWpiLibVersionQuery, LatestWpiLibIsBeingDownloaded}

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
        
        final boolean isTemplateFirstOpen = frcFreshTemplateProjectCheckAndCleanup();

        // TODO: See if we need to call RioLogProjectService.update(myProject) in any way. FrcModuleComponentImpl.moduleAdded(), which we need if someone adds a module to an existing project, and moduleAdded is called during a project opening
        // RioLogProjectService.update(myProject);

        // For example, see com.intellij.framework.detection.impl.FrameworkDetectionManager#projectOpened
        StartupManager.getInstance(myProject).registerPostStartupActivity(() -> this.runPostStartupActivities(isTemplateFirstOpen));
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

    public static boolean isFrcFacetedProject(@Nullable Project project) {return FrcFacetKt.isFrcFacetedProject(project);}


    private boolean frcFreshTemplateProjectCheckAndCleanup()
    {
        if (FrcProjectExtsKt.isGradleProject(myProject)) return false;
        
        boolean isFreshTemplateProject = false;
        try
        {
            final VirtualFile projectFile = myProject.getProjectFile();
            if (projectFile != null)
            {
                final VirtualFile ideaDir = projectFile.getParent();
                final VirtualFile projectTemplateFile = ideaDir.findChild("project-template.xml");
                isFreshTemplateProject = isFrcProjectTemplate(projectTemplateFile);
                if (isFreshTemplateProject)
                {
                    FrcFileUtils.deleteSafely(projectTemplateFile, this);
                    // This is assuming a standard template was used...
                    //final VirtualFile srcDir = myProject.getBaseDir().findChild("src");
                    final VirtualFile projectDir = com.intellij.openapi.project.ProjectUtil.guessProjectDir(myProject);
                    if (projectDir == null) { return false; } // Not much else we can do... and besides, this is functionality for the old Ant based project templates and can be removed at some point
                    final VirtualFile srcDir = projectDir.findChild("src");
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


    private void checkIssue8Refresh()
    {
        if (isFrcFacetedProject(myProject) && WpiLibLibrariesUtils.is2018CommonRefreshNeededViaReadAction())
        {

            final Notification refreshNotification = new Notification(FrcNotifications.Companion.getFRC_ACTIONABLE_NOTIFICATION_GROUP().getDisplayId(),
                                                                      FrcNotifications.Companion.getIconInfo(),
                                                                      FrcNotifications.Title,
                                                                      NOTIFICATIONS_SUBTITLE + " Refresh Required",
                                                                      "In order to fully resolve "
                                                                      + "<a href='openIssue'>Issue #8: Cannot deploy code to roboRIO</a>, "
                                                                      + "caused by a change to the 2018 WPILib, the previously downloaded WPILib and its associated tools needs "
                                                                      + "to be refreshed. A fresh download has been started.",
                                                                      NotificationType.INFORMATION,
                                                                      (notification, event) -> {
                                                                          if ("openIssue".equals(event.getDescription()))
                                                                          {
                                                                              BrowserLauncherImpl.getInstance().browse(UriUtils.createUri("https://gitlab.com/Javaru/frc-intellij-idea-plugin/issues/8"));
                                                                          }
                                                                      }
            );
            Notifications.Bus.notify(refreshNotification, myProject);

            DownloadWpiLibAction.downloadLatestInBackground(myProject,
                                                            true,
                                                            false,
                                                            true,
                                                            () -> {
                                                                refreshNotification.expire();
                                                                final Notification refreshCompletedNotification = new Notification(FrcNotifications.Companion.getFRC_GENERAL_NOTIFICATION_GROUP()
                                                                                                                                                             .getDisplayId(),
                                                                                                                                   FrcNotifications.Companion.getIconInfo(),
                                                                                                                                   FrcNotifications.Title,
                                                                                                                                   NOTIFICATIONS_SUBTITLE,
                                                                                                                                   "Refresh of WPILib has completed",
                                                                                                                                   NotificationType.INFORMATION,
                                                                                                                                   null
                                                                );
                                                                Notifications.Bus.notify(refreshCompletedNotification, myProject);

                                                            },
                                                            () -> {
                                                                refreshNotification.expire();
                                                                final Notification refreshFailedNotification = new Notification(FrcNotifications.Companion.getFRC_GENERAL_NOTIFICATION_GROUP()
                                                                                                                                                          .getDisplayId(),
                                                                                                                                FrcNotifications.Companion.getIconWarn(),
                                                                                                                                FrcNotifications.Title,
                                                                                                                                NOTIFICATIONS_SUBTITLE
                                                                                                                                + " Refresh Failed",
                                                                                                                                "Refresh of WPILib failed. You will likely have problems deploying code to a 2018 roboRIO until "
                                                                                                                                + "a fresh copy of the WPILib is downloaded. Please verify your internet connection and download a "
                                                                                                                                + "fresh copy of the WPILIb and its tools via the menu: <em>Tools > FRC > Download Latest WPILib</em>",
                                                                                                                                NotificationType.INFORMATION,
                                                                                                                                null
                                                                );
                                                                Notifications.Bus.notify(refreshFailedNotification, myProject);

                                                            });
        }
    }
    
    private boolean isFrcProjectTemplate(@Nullable VirtualFile projectTemplateFile)
    {
        if (projectTemplateFile != null && projectTemplateFile.exists())
        {
            try (final InputStream inputStream = projectTemplateFile.getInputStream())
            {
                Document document = new SAXBuilder().build(inputStream);
                final XPathFactory xPathFactory = XPathFactory.instance();
                XPathExpression<Element> expression = xPathFactory.compile("/template/input-field", Filters.element());
                final Element inputFieldElement = expression.evaluateFirst(document);
                if (inputFieldElement != null)
                {
                    final String defaultValue = inputFieldElement.getAttributeValue("default");
                    if (defaultValue != null && defaultValue.toLowerCase().contains("frc"))
                    {
                        return true;
                    }
                    // check the icon as a secondary check
                    expression = xPathFactory.compile("/template/icon-path", Filters.element());
                    final Element iconElement = expression.evaluateFirst(document);
                    if (iconElement != null)
                    {
                        final String iconPath = iconElement.getValue();
                        return iconPath != null && iconPath.toLowerCase().contains("/icons/first/first_icon"); 
                    }
                }
            }
            catch (Exception e)
            {
                LOG.warn("Could not determine isFrcProjectTemplate due to an Exception: " + e.toString(), e);
            }
        }
        return false;
    }
    
    private void runPostStartupActivities(final boolean isFreshFrcTemplateProject)
    {
        // TODO: Need some additional work to handle the variety of cases, especially a new version is available for download.
        
        boolean isFrcProject = isFreshFrcTemplateProject || isFrcFacetedProject(myProject);
        if (isFrcProject)
        {
            LOG.debug("[FRC] isFreshFrcTemplateProject = " + isFreshFrcTemplateProject);

            RioLogProjectService.getInstance(myProject).activateTcp();
            notifyToConfigureTeamNumIfNecessary(myProject, true);
    
            if (FrcProjectExtsKt.isAntBasedFrcProject(myProject))
            {
                checkLegacyWpiLibraryStatus(isFreshFrcTemplateProject);
            }
            else
            {
                // TODO - check gradle RIO version status
            }
        }
    }
    
    
    private void checkLegacyWpiLibraryStatus(boolean isFreshFrcTemplateProject)
    {
        final LegacyWpiLibVersionStatus versionStatus = LegacyWpiLibVersionStatus.getCurrentVersionStatus(myProject, true);
        
        if (isFreshFrcTemplateProject)
        {
            if (!versionStatus.isWpiLibDownloaded() || versionStatus.isNewerVersionAvailableThanDownloaded())
            {
                if (!versionStatus.isWpiLibDownloaded())
                {
                    queueLatestWpiLibVersionIsBeingDownloadedNotification(myProject);
                }
                DownloadWpiLibAction.downloadLatestInBackground(myProject,
                                                                true,
                                                                false);
            }
            else
            {
                AttachWpilibAction.attachWpiLib(myProject, false, true);
            }
            AttachUserLibDirAction.attachUserLib(myProject, false);
            
        }
        else
        {
            checkProjectFrcStatus(myProject, true, false);
        }
    }
    
    
    public static void checkProjectFrcStatus(@NotNull Project project, boolean knownFacetedProject, boolean checkTeamNumConfigStatus)
    {
        // TODO add check for if a new version of WPILib is available
        DumbService.getInstance(project).runWhenSmart(() ->
                                                      {
                                                          final Map<NotificationKey, Notification> notificationMap = getNotificationMapForProject(project);
                                                          if (checkTeamNumConfigStatus)
                                                          {
                                                              notifyToConfigureTeamNumIfNecessary(project, knownFacetedProject);
                                                          }

                                                          if (knownFacetedProject || isFrcFacetedProject(project))
                                                          {
                                                              if (!FrcProjectExtsKt.isGradleProject(project) && 
                                                                  /* TODO: The ant project check always returns false at this time as a quick fix the breaking change.   */
                                                                   FrcProjectExtsKt.isAntBasedFrcProject(project))
                                                              {
                                                                  if (notificationMap.get(AttachWpiLibQuery) == null && !WpiLibLibrariesUtils.isWpilibAttachedViaReadAction(project))
                                                                  {
                                                                      
                                                                      if (WpiLibLibrariesUtils.isWpilibDownloadedToSystem())
                                                                          { queueAttachWpilibQueryNotification(project); }
                                                                      else 
                                                                          { queueDownloadAndAttachWpilibNotification(project); }
                                                                  }
        
                                                                  if (notificationMap.get(AttachUserLibQuery) == null && !WpiLibLibrariesUtils.isUserLibAttachedViaReadAction(project))
                                                                  {
                                                                      queueMissingUserLibQueryNotification(project);
                                                                  }
                                                              }
                                                          }
                                                      });
    }


    private static void notifyToConfigureTeamNumIfNecessary(@NotNull Project project, boolean knownFacetedProject)
    {
        final Map<NotificationKey, Notification> notificationMap = getNotificationMapForProject(project);
        final FrcApplicationSettings settings = FrcApplicationSettings.getInstance();

        final boolean shouldNotify = !settings.isTeamNumberConfigured()
                                     &&
                                     ((knownFacetedProject || isFrcFacetedProject(project)) || settings.getPrc() <= TEAM_NUM_NOTIFY_RUN_COUNT_PROJECT_LEVEL_NON_FRC_PROJECT)
                                     &&
                                     notificationMap.get(ConfigureTeamNumberQuery) == null; //Don't publish multiple notifications for same project

        if (shouldNotify)
        {
            if (LOG.isDebugEnabled())
            { 
                LOG.debug("[FRC] Publishing 'configure team number' notification for Project '" + project + "'"); }
            // Expire the application level notification to prevent duplicate notification in the event log
            FrcNotifications.Companion.expireConfigureTeamNumberNotification(null);
            final Notification notification = FrcNotifications.Companion.notifyAboutTeamNumberNeedingToBeConfigured(project, true, false);
            notificationMap.put(ConfigureTeamNumberQuery, notification);
        }
    }

    public static Notification queueNewerWpiLibVersionIsAvailable(@NotNull Project project,
                                                                  @NotNull LegacyWpiLibVersionStatus versionStatus)
    {
        return queueNewerWpiLibVersionIsAvailable(project, versionStatus.getAttachedVersion(), versionStatus.getAvailableVersion());    
    }


    public static Notification queueNewerWpiLibVersionIsAvailable(@NotNull Project project,
                                                                  @Nullable WpiLibVersion attachedVersion,
                                                                  @Nullable WpiLibVersion availableVersion)
    {

        StringBuilder content = new StringBuilder("A newer version of the WPILib is available for download.<br>");
        if (attachedVersion != null && availableVersion != null)
        {
            content.append("Current Version: ")
                   .append(attachedVersion.getVersionString())
                   .append(" Available Version: ")
                   .append(availableVersion.getVersionString())
                   .append("<br>");
        }
        content.append("Would you like to download the new version? <a href='download'>Yes</a>  <a href='doNotDownload'>No</a>");
        final Notification notification = FrcNotifications.Companion.getFRC_ACTIONABLE_NOTIFICATION_GROUP()
                                                                    .createNotification(FrcNotifications.Title,
                                                                                        "New WPI Lib Available",
                                                                                        content.toString(),
                                                                                        NotificationType.INFORMATION,
                                                                                        (theNotification, event) ->
                                {
                                    theNotification.expire();
                                    if ("download".equals(event.getDescription()))
                                    {
                                        DownloadWpiLibAction.downloadLatestInBackground(
                                            project,
                                            true,
                                            true);
                                    }
                                }
            );
        Notifications.Bus.notify(notification, project);
        getNotificationMapForProject(project).put(DownloadNewWpiLibVersionQuery, notification);
        return notification;
    }
    
    private static Notification queueDownloadAndAttachWpilibNotification(@NotNull Project project)
    {
        final Notification notification = new Notification(FrcNotifications.Companion.getFRC_ACTIONABLE_NOTIFICATION_GROUP().getDisplayId(),
                                                           FrcNotifications.Companion.getIconInfo(),
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
        getNotificationMapForProject(project).put(AttachWpiLibQuery, notification);
        return notification;
    }


    @SuppressWarnings("UnusedReturnValue")
    private static Notification queueAttachWpilibQueryNotification(@NotNull Project project)
    {
        final Notification notification = new Notification(FrcNotifications.Companion.getFRC_ACTIONABLE_NOTIFICATION_GROUP().getDisplayId(),
                                                           FrcNotifications.Companion.getIconInfo(),
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
        getNotificationMapForProject(project).put(AttachWpiLibQuery, notification);
        return notification;
    }


    @SuppressWarnings("UnusedReturnValue")
    private static Notification queueMissingUserLibQueryNotification(@NotNull Project project)
    {
        final Notification notification = new Notification(FrcNotifications.Companion.getFRC_ACTIONABLE_NOTIFICATION_GROUP().getDisplayId(),
                                                           FrcNotifications.Companion.getIconInfo(),
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
        getNotificationMapForProject(project).put(AttachUserLibQuery, notification);
        return notification;
    }


    @SuppressWarnings("UnusedReturnValue")
    public static Notification queueLatestWpiLibVersionIsBeingDownloadedNotification(@Nullable Project project)
    {
        final Notification notification = FrcNotifications.Companion.getFRC_ACTIONABLE_NOTIFICATION_GROUP()
                                                                    .createNotification(FrcNotifications.Title,
                                                                                        "WPILib",
                                                                                        "The latest version of the WPILib is being downloaded.",
                                                                                        NotificationType.INFORMATION,
                                                                                        null);
        Notifications.Bus.notify(notification, null);
        if (project != null)
        {
            getNotificationMapForProject(project).put(DownloadNewWpiLibVersionQuery, notification);
        }
        return notification;
    }

    
    public static void cancelWpiLibIsDownloadingNotifications(@NotNull Project project)
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


        private void updateForFrcFacet(@NotNull Facet facet)
        { 
            if (FrcFacetKt.isFrcFacet(facet))
            {
                RioLogProjectService.getInstance(facet.getModule().getProject()).update();
            }
        }
    }
}
