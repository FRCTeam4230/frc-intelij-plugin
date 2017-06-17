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

package net.javaru.iip.frc.actions;

import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.ex.WindowManagerEx;

import net.javaru.iip.frc.FrcIcons;
import net.javaru.iip.frc.FrcPluginGlobals;
import net.javaru.iip.frc.i18n.FrcMessageBundle;
import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.settings.FrcApplicationSettings;

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

        final FrcApplicationSettings settings = FrcApplicationSettings.Settings.INSTANCE();

        final String teamNumString = Messages.showInputDialog(ideFrame.getComponent(),
                                                              FrcMessageBundle.message("frc.ui.dialogs.enterTeamNumberPrompt"),
                                                              FrcPluginGlobals.FRC_PLUGIN_NAME,
                                                              FrcIcons.FIRST_ICON_DIALOG_WINDOW,
                                                              (settings.isTeamNumberConfigured() ? "" + settings.getTeamNumber() : ""),
                                                              new InputValidator()
        {
            @Override
            public boolean checkInput(String inputString) { return FrcApplicationSettings.Settings.isValidTeamNumber(inputString); }


            @Override
            public boolean canClose(String inputString) { return FrcApplicationSettings.Settings.isValidTeamNumber(inputString); }
        });

        if (teamNumString != null && FrcApplicationSettings.Settings.isValidTeamNumber(teamNumString))
        {
            settings.setTeamNumber(Integer.parseInt(teamNumString));
            FrcNotifications.expireConfigureTeamNumberNotification(project);
        }
    }


    @Override
    public void update(AnActionEvent e)
    {
        super.update(e);
        e.getPresentation().setIcon(FrcIcons.FIRST_ICON_MEDIUM_16);
        e.getPresentation().setVisible(shouldBeVisible(e));
    }
    
    protected boolean shouldBeVisible(AnActionEvent e)
    {
        final FrcApplicationSettings settings = FrcApplicationSettings.Settings.INSTANCE();
        return (!settings.isTeamNumberConfigured() && settings.getPrc() <= TEAM_NUM_NOTIFY_RUN_COUNT_APP_LEVEL);
    }
}
