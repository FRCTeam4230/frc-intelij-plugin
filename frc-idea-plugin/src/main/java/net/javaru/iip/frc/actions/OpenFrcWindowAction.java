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

package net.javaru.iip.frc.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.ui.RioLogConsoleProjectService;



public class OpenFrcWindowAction extends DumbAwareAction
{
    private static final Logger LOG = Logger.getInstance(OpenFrcWindowAction.class);


    @Override
    public void actionPerformed(AnActionEvent actionEvent)
    {
        final Project project = actionEvent.getProject();
        if (project != null)
        {
            final RioLogConsoleProjectService rioLogConsoleProjectService = ServiceManager.getService(project, RioLogConsoleProjectService.class);
            rioLogConsoleProjectService.openRioLogConsole();
        }
    }
}
