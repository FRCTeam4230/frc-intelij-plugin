/*
 * Copyright 2015-2018 the original author or authors
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

package net.javaru.iip.frc.actions;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.ex.WindowManagerEx;

import net.javaru.iip.frc.FrcIcons.FRC;
import net.javaru.iip.frc.FrcPluginGlobals;
import net.javaru.iip.frc.components.FrcProjectComponentImpl;
import net.javaru.iip.frc.i18n.FrcBundle;
import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.settings.FrcTeamNumberKt;
import net.javaru.iip.frc.wpilib.WpiLibPaths;
import net.javaru.iip.frc.wpilib.retrieval.WpiLibDownloader;

import static net.javaru.iip.frc.FrcPluginGlobals.TEAM_NUM_NOTIFY_RUN_COUNT_APP_LEVEL;



public class ConfigureTeamNumberBasicAction extends AnAction
{
    @Override
    public void actionPerformed(AnActionEvent e)
    {
        @Nullable
        final Project project = e.getData(CommonDataKeys.PROJECT);
        openConfigureTeamNumberDialog(project);
    }


    public static void openConfigureTeamNumberDialog(@Nullable Project project)
    {
        IdeFrame ideFrame = WindowManagerEx.getInstanceEx().findFrameFor(project);

        final FrcApplicationSettings settings = FrcApplicationSettings.getInstance();

        final String teamNumString = Messages.showInputDialog(ideFrame.getComponent(),
                                                              FrcBundle.message("frc.ui.dialogs.enterTeamNumberPrompt"),
                                                              FrcPluginGlobals.FRC_PLUGIN_NAME,
                                                              FRC.FIRST_ICON_DIALOG_WINDOW,
                                                              (settings.isTeamNumberConfigured() ? "" + settings.getTeamNumber() : ""),
                                                              new InputValidator()
        {
            @Override
            public boolean checkInput(String inputString) { return FrcTeamNumberKt.isValidTeamNumber(inputString); }


            @Override
            public boolean canClose(String inputString) { return FrcTeamNumberKt.isValidTeamNumber(inputString); }
        });

        if (teamNumString != null && FrcTeamNumberKt.isValidTeamNumber(teamNumString))
        {
            settings.setTeamNumber(Integer.parseInt(teamNumString));
            performTeamNumberChangeUpdates();
            FrcNotifications.Companion.expireConfigureTeamNumberNotification(project);
        }
    }


    @Override
    public void update(AnActionEvent e)
    {
        super.update(e);
        e.getPresentation().setIcon(FRC.FIRST_ICON_MEDIUM_16);
        e.getPresentation().setVisible(shouldBeVisible(e));
    }
    
    protected boolean shouldBeVisible(AnActionEvent e)
    {
        final FrcApplicationSettings settings = FrcApplicationSettings.getInstance();
        return (!settings.isTeamNumberConfigured() && settings.getPrc() <= TEAM_NUM_NOTIFY_RUN_COUNT_APP_LEVEL);
    }
    
    //TODO Need to remove this legacy functionality
    @Deprecated
    public static void performTeamNumberChangeUpdates()
    {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try
            {
                WpiLibDownloader.updateOrCreateWpilibPropertiesFile();
            }
            catch (IOException e)
            {
                final Notification notification =
                    FrcNotifications.Companion.getFRC_ACTIONABLE_NOTIFICATION_GROUP()
                                              .createNotification("FRC",
                                            "Team Number Update Failure",
                                            "The '" + WpiLibPaths.getWpilibPropertiesFile() + "' file could not be updated with "
                                            + "the change to the team number. You will need to manually update the 'team-number' "
                                            + "property in the file in order for your robot deploys to work. Update Failure Cause: "
                                            + e.toString(),
                                            NotificationType.ERROR);
                final Project[] projects = ProjectManager.getInstance().getOpenProjects();
                for (Project project : projects)
                {
                    if (FrcProjectComponentImpl.isFrcFacetedProject(project))
                    {
                        Notifications.Bus.notify(notification, project);
                    }
                }
            }
        });
    }
}
