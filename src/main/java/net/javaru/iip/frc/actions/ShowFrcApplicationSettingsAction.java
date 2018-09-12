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

import javax.swing.*;

import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.ex.WindowManagerEx;

import net.javaru.iip.frc.settings.FrcApplicationSettingsConfigurable;



public class ShowFrcApplicationSettingsAction extends AnAction
{
    private static final Logger LOG = Logger.getInstance(ShowFrcApplicationSettingsAction.class);


    @Override
    public void actionPerformed(AnActionEvent e)
    {
        final Configurable configurable = FrcApplicationSettingsConfigurable.getInstance();
        @Nullable
        final Project project = e.getData(CommonDataKeys.PROJECT);
        IdeFrame ideFrame = WindowManagerEx.getInstanceEx().findFrameFor(project);
        ShowSettingsUtil.getInstance().editConfigurable((JFrame) ideFrame, configurable);
    }


    @Override
    public void update(AnActionEvent e)
    {
        super.update(e);
//        e.getPresentation().setIcon(FrcIcons.FIRST_ICON_MEDIUM_16);
//        final FrcApplicationSettings settings = FrcApplicationSettings.Settings.INSTANCE();
//        e.getPresentation().setVisible(!settings.isTeamNumberConfigured() && settings.getRunCount() <= 5);
    }
}
