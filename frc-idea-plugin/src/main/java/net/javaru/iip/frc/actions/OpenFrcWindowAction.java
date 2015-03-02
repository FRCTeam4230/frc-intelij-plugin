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

import java.nio.charset.Charset;

import com.intellij.compiler.server.BuildManager;
import com.intellij.execution.process.BaseOSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import net.javaru.iip.frc.settings.FrcApplicationComponent;
import net.javaru.iip.frc.settings.FrcSettings;
import net.javaru.iip.frc.udp.RioLogMonitor;
import net.javaru.iip.frc.ui.AbstractRioLogContentExecutor;
import net.javaru.iip.frc.ui.RioLogFrcWindowContentExecutor;
import net.javaru.iip.frc.ui.RioLogRunWindowContentExecutor;



public class OpenFrcWindowAction extends DumbAwareAction
{
    private static final Logger LOG = Logger.getInstance(OpenFrcWindowAction.class);


    @Override
    public void actionPerformed(AnActionEvent actionEvent)
    {
        final Project project = actionEvent.getProject();
        monitorRioLog(project);

    }


    private void monitorRioLog(final Project project)
    {
        if (project != null)
        {
            final RioLogMonitor process = new RioLogMonitor();

            final ProcessHandler processHandler = new BaseOSProcessHandler(process, null, Charset.defaultCharset())
            {
                @Override
                public boolean isSilentlyDestroyOnClose()
                {
                    return true;
                }
            };

            try
            {
                processHandler.putUserDataIfAbsent(BuildManager.ALLOW_AUTOMAKE, true);
            }
            catch (NoClassDefFoundError ignore)
            {
                //php storm does not have it
            }


            final FrcSettings settings = FrcApplicationComponent.getInstance().getState();

            boolean useRunWindow = (settings != null && !settings.isUseFrcToolWindow());
            final AbstractRioLogContentExecutor contentExecutor = useRunWindow ?
                                                                  new RioLogRunWindowContentExecutor(project, processHandler) :
                                                                  new RioLogFrcWindowContentExecutor(project, processHandler);


            // see com/jetbrains/python/run/PythonTask.java:221 for example
            contentExecutor.withStop(
                processHandler::destroyProcess,
                () -> !processHandler.isProcessTerminated());
            contentExecutor.withRerun(
                () ->
                {
                    processHandler.destroyProcess();
                    processHandler.waitFor(2000L);
                    monitorRioLog(project);
                }
            );

            Disposer.register(project, contentExecutor);

            contentExecutor.run();

        }
    }
}
