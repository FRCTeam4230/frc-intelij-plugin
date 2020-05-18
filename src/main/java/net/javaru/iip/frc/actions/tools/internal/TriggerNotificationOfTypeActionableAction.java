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

package net.javaru.iip.frc.actions.tools.internal;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.notify.FrcNotificationType;
import net.javaru.iip.frc.notify.FrcNotifications;



/**
 * An action that will purposefully cause an exception for testing purposes. 
 */
public class TriggerNotificationOfTypeActionableAction extends AbstractFrcInternalAction
{
    private static final Logger LOG = Logger.getInstance(TriggerNotificationOfTypeActionableAction.class);


    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent)
    {
        LOG.info("[FRC] Making a sample FRC Notification");
        final Project project = actionEvent.getData(CommonDataKeys.PROJECT);
        FrcNotifications.Companion.notify(FrcNotificationType.ACTIONABLE_INFO, 
                                          "This is a test FRC Actionable notification",
                                          "My Sub-title",
                                          project);
    }

}
