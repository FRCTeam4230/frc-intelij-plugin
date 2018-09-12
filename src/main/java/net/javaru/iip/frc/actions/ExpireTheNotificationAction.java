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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.notify.FrcNotifications;



public class ExpireTheNotificationAction extends AnAction
{
    private static final Logger LOG = Logger.getInstance(ExpireTheNotificationAction.class);


    @Override
    public void actionPerformed(AnActionEvent e)
    {
        FrcNotifications.expireConfigureTeamNumberNotification(e.getData(CommonDataKeys.PROJECT));
    }


    @Override
    public void update(AnActionEvent e)
    {
        super.update(e);
    }
}
