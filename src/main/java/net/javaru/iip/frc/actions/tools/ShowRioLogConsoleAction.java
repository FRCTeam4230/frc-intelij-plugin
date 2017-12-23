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

package net.javaru.iip.frc.actions.tools;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;




public class ShowRioLogConsoleAction extends AbstractFrcToolsAction implements DumbAware
{
    private static final Logger LOG = Logger.getInstance(ShowRioLogConsoleAction.class);
    @Override
    public void actionPerformed(AnActionEvent actionEvent)
    {
        final Project project = actionEvent.getProject();
        if (project != null)
        {
            //TODO: implement once RioLogConsoleProjectService is reworked - or delete this action and replace with a show FRC tool window depending on how things change
            LOG.warn("[FRC] Need to implement ShowRioLogConsoleAction.actionPerformed() once RioLog Service is reworked ");
//            RioLogConsoleProjectService.update(project);
//            RioLogConsoleProjectService.activateNow(project);
        }
    }
}
