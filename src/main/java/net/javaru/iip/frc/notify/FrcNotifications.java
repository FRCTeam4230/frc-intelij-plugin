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

package net.javaru.iip.frc.notify;

import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.icons.AllIcons;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.notification.NotificationsConfiguration;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.actions.ConfigureTeamNumberBasicAction;
import net.javaru.iip.frc.i18n.FrcMessageBundle;
import net.javaru.iip.frc.settings.FrcApplicationSettings;



/**
 * Also see {@link net.javaru.iip.frc.components.FrcProjectComponentImpl} for some notification methods.
 */
// NOTE: This class is registered in the plugin.xml as a ApplicationComponent
public class FrcNotifications implements FrcNotificationsApplicationComponent
{
    private static final Logger LOG = Logger.getInstance(FrcNotifications.class);


    public static final NotificationGroup FRC_GENERAL_NOTIFICATION_GROUP = new NotificationGroup(FrcMessageBundle.message("frc.notifications.group.name.general"),
                                                                                                 NotificationDisplayType.BALLOON,
                                                                                                 true);

    public static final NotificationGroup FRC_ACTIONABLE_NOTIFICATION_GROUP = new NotificationGroup(FrcMessageBundle.message("frc.notifications.group.name.actionable"),

                                                                                                    NotificationDisplayType.STICKY_BALLOON,
                                                                                                    true);

    public static final Icon IconInfo = AllIcons.General.BalloonInformation;
    public static final Icon IconWarn = AllIcons.General.BalloonWarning;
    public static final Icon IconError = AllIcons.General.BalloonError;

    public static final String Title = "FRC";


    private static final Map<Project, Notification> configureTeamNumberNotifications = new HashMap<>();

    private FrcNotifications() 
    {
        LOG.debug("[FRC] Registering FRC Notification Groups");
        NotificationsConfiguration.getNotificationsConfiguration().register(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP.getDisplayId(),
                                                                            NotificationDisplayType.BALLOON,
                                                                            true);
        NotificationsConfiguration.getNotificationsConfiguration().register(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP.getDisplayId(),
                                                                            NotificationDisplayType.STICKY_BALLOON,
                                                                            true);
    }


    @Override
    public void initComponent()
    {
        // no op
    }


    @Override
    public void disposeComponent()
    {
        // no op
    }


    @NotNull
    @Override
    public String getComponentName()
    {
        return getClass().getSimpleName();
    }


    //TODO: Move to FrcProjectComponentImpl and then move the notificationsMap placement into this method
    @SuppressWarnings("UnusedReturnValue")
    public static Notification notifyAboutTeamNumberNeedingToBeConfigured(@Nullable Project project, boolean useSticky)
    {
        
        //See com/intellij/ide/plugins/PluginManager.java:177 for an example

        Notification notification = configureTeamNumberNotifications.get(project);

        if (notification == null || notification.isExpired())
        {
            notification = createConfigureTeamNotification(project, useSticky);
            // This makes the notification title & subtitle appear in bold in the Event Log window
            notification.setImportant(true);
            configureTeamNumberNotifications.put(project, notification);
            Notifications.Bus.notify(notification, project);
        }
        return notification;
    }


    @NotNull
    private static Notification createConfigureTeamNotification(@Nullable Project project, boolean useSticky)
    {
        final NotificationGroup notificationGroup = useSticky ? FRC_ACTIONABLE_NOTIFICATION_GROUP : FRC_GENERAL_NOTIFICATION_GROUP;
        return new Notification(notificationGroup.getDisplayId(),
                                FrcNotifications.IconInfo,
                                FrcNotifications.Title,
                                "Configuration Needed",
                                "Please <a href='configure'>configure</a> your FRC Team Number.",
                                NotificationType.INFORMATION,
                                (theNotification, event) ->
                                                           {
                                                               if ("configure".equals(event.getDescription()))
                                                               {
//                                                                       final Configurable configurable = FrcApplicationSettingsConfigurable.getInstance();
//                                                                       IdeFrame ideFrame = WindowManagerEx.getInstanceEx().findFrameFor(project);
//                                                                       ShowSettingsUtil.getInstance().editConfigurable((JFrame) ideFrame, configurable);
                                                                   ConfigureTeamNumberBasicAction.openConfigureTeamNumberDialog(project);
                                                               }

                                                               if (FrcApplicationSettings.Settings.INSTANCE().isTeamNumberConfigured())
                                                               {
                                                                   theNotification.expire();
                                                               }
                                                           }
        );

        // THIS IS AN ALTERNATIVE WAY TO CREATE A NOTIFICATION FROM THE NotificationGroup CLASS
//        return notificationGroup.createNotification(FrcNotifications.Title,
//                                                    "Configuration Needed",
//                                                    "Please <a href='configure'>configure</a> your FRC Team Number.",
//                                                    NotificationType.INFORMATION,
//                                                    (theNotification, event) ->
//                                                    {
//                                                        if ("configure".equals(event.getDescription()))
//                                                        {
////                                                                       final Configurable configurable = FrcApplicationSettingsConfigurable.getInstance();
////                                                                       IdeFrame ideFrame = WindowManagerEx.getInstanceEx().findFrameFor(project);
////                                                                       ShowSettingsUtil.getInstance().editConfigurable((JFrame) ideFrame, configurable);
//                                                            ConfigureTeamNumberBasicAction.openConfigureTeamNumberDialog(project);
//                                                        }
//
//                                                        if (FrcApplicationSettings.Settings.INSTANCE().isTeamNumberConfigured())
//                                                        {
//                                                            theNotification.expire();
//                                                        }
//                                                    }
//        );
    }


    public static void expireConfigureTeamNumberNotification(@Nullable Project project)
    {
        final Notification notification = configureTeamNumberNotifications.get(project);
        if (notification != null)
        {
            notification.expire();
        }
    }
    
}
