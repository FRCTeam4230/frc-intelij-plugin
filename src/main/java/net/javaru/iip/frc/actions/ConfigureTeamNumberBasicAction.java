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

package net.javaru.iip.frc.actions;

import java.awt.*;
import java.io.IOException;
import javax.swing.*;

import org.jetbrains.annotations.NotNull;
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

import net.javaru.iip.frc.FrcIcons.FRC;
import net.javaru.iip.frc.FrcPluginGlobals;
import net.javaru.iip.frc.components.FrcProjectComponentImpl;
import net.javaru.iip.frc.i18n.FrcBundle;
import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.settings.FrcTeamNumberKt;
import net.javaru.iip.frc.util.FrcUiUtilsKt;
import net.javaru.iip.frc.wpilib.legacy.LegacyWpiLibPaths;
import net.javaru.iip.frc.wpilib.legacy.retrieval.LegacyWpiLibDownloader;

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
        
    
        final InputValidator validator = new InputValidator()
        {
            @Override
            public boolean checkInput(String inputString) { return FrcTeamNumberKt.isValidTeamNumber(inputString); }
    
    
            @Override
            public boolean canClose(String inputString) { return FrcTeamNumberKt.isValidTeamNumber(inputString); }
        };
    
        final FrcApplicationSettings settings = FrcApplicationSettings.getInstance();
        
        @Nullable
        final Component parentComponent = FrcUiUtilsKt.findIdeFrameOrAlternateParentComponent(project);
        final String message = FrcBundle.message("frc.ui.dialogs.enterTeamNumberPrompt");
        final String title = FrcPluginGlobals.FRC_PLUGIN_NAME;
        final Icon icon = FRC.FIRST_ICON_DIALOG_WINDOW;
        final String initialValue = settings.isTeamNumberConfigured() ? "" + settings.getTeamNumber() : "";
        
        final String teamNumString;
        if (parentComponent != null)
        {
            
            teamNumString = Messages.showInputDialog(parentComponent,
                                                     message,
                                                     title,
                                                     icon,
                                                     initialValue,
                                                     validator);
        }
        else
        {
            teamNumString = Messages.showInputDialog(message,
                                                     title,
                                                     icon,
                                                     initialValue,
                                                     validator);
        }
        

        if (teamNumString != null && FrcTeamNumberKt.isValidTeamNumber(teamNumString))
        {
            settings.setTeamNumber(Integer.parseInt(teamNumString));
            performTeamNumberChangeUpdates();
            FrcNotifications.Companion.expireConfigureTeamNumberNotification(project);
        }
    }


    @Override
    public void update(@NotNull AnActionEvent e)
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
                LegacyWpiLibDownloader.updateOrCreateWpilibPropertiesFile();
            }
            catch (IOException e)
            {
                final Notification notification =
                    FrcNotifications.Companion.getFRC_ACTIONABLE_NOTIFICATION_GROUP()
                                              .createNotification("FRC",
                                            "Team Number Update Failure",
                                                                  "The '" + LegacyWpiLibPaths.getWpilibPropertiesFile() + "' file could not be updated with "
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
