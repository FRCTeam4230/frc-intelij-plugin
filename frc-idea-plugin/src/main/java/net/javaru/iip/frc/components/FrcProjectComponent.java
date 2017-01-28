/*
 * Copyright 2015 Mark Vedder
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


import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.facet.Facet;
import com.intellij.facet.FacetManager;
import com.intellij.facet.FacetManagerAdapter;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;

import net.javaru.iip.frc.actions.tools.AttachUserLibDirAction;
import net.javaru.iip.frc.actions.tools.AttachWpilibAction;
import net.javaru.iip.frc.actions.tools.DownloadWpiLibAction;
import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.settings.FrcApplicationComponent;
import net.javaru.iip.frc.ui.RioLogConsoleProjectService;
import net.javaru.iip.frc.ui.notify.FrcNotifications;
import net.javaru.iip.frc.wpilib.attached.WpiLibrariesUtils;



public class FrcProjectComponent implements ProjectComponent
{
    private static final Logger LOG = Logger.getInstance(FrcProjectComponent.class);

    @NotNull
    private final Project myProject;
    
    private final List<Notification> startUpNotifications = new ArrayList<>();


    public FrcProjectComponent(@NotNull Project project)
    {
        this.myProject = project;
    }


    @Override
    public void projectOpened()
    {
        registerMessageBusListeners();
        RioLogConsoleProjectService.update(myProject);
        
        // For example, see com.intellij.framework.detection.impl.FrameworkDetectionManager#projectOpened

        StartupManager.getInstance(myProject).registerPostStartupActivity(() ->
                                                                        {
                                                                            if (isFrcFacetedProject(myProject))
                                                                            {
                                                                                checkProjectFrcStatus(myProject);
                                                                            }
                                                                        });
        
    }


    public static void checkProjectFrcStatus(@NotNull Project project)
    {
        if (FrcApplicationComponent.getInstance().getState().getTeamNumber() <= 0)
        {
//            if (!FrcApplicationComponent.getInstance().showTeamNumConfigNotification(project))
            {
                FrcApplicationComponent.getInstance().expireTeamNumConfigNotification(project);
                final Notification notification = FrcNotifications.notifyAboutTeamNumberNeedingToBeConfigured(project);
//                    startUpNotifications.add(notification);
            }
        }

        if (!WpiLibrariesUtils.isWpilibPresent(project))
        {
            if (WpiLibrariesUtils.isWpilibInstalledOnSystem())
            {
                final Notification notification = queueAttachWpilibNotification(project);
//                        startUpNotifications.add(notification);
            }
            else
            {
                final Notification notification = queueDownloadAndAttachWpilibNotification(project);
//                        startUpNotifications.add(notification);
            }

        }

//            if (WpiLibrariesUtils.isUserLibNonEmptyAndNotAttached(project))
        if (!WpiLibrariesUtils.isUserLibAttached(project))
        {
            final Notification notification = queueMissingUserLibNotification(project);
//                    startUpNotifications.add(notification);
        }
    }


    @Override
    public void projectClosed() 
    {
        for (Notification notification : startUpNotifications)
        {
            if (!notification.isExpired())
            {
                notification.expire();
            }
        }    
    }


    @Override
    public void initComponent() { /* no op */ }


    @Override
    public void disposeComponent() { /* no op */ }


    @NotNull
    @Override
    public String getComponentName() { return getClass().getSimpleName(); }


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
            updateForFrcFacet(facet);
            checkProjectFrcStatus(facet.getModule().getProject());
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
            if (facet instanceof FrcFacet)
            {
                RioLogConsoleProjectService.update(facet);
            }
        }
    }

    public static boolean isFrcFacetedProject(@Nullable Project project)
    {
        if (project != null)
        {
            final Module[] modules = ModuleManager.getInstance(project).getModules();
            for (Module module : modules)
            {
                if (FrcModuleComponent.isFrcFacetedModule(module)) {return true;}
            }
        }
        return false;
    }
}
