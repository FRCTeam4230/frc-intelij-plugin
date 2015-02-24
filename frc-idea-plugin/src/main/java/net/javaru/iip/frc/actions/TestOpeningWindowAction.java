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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.intellij.compiler.server.BuildManager;
import com.intellij.execution.process.BaseOSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import net.javaru.iip.frc.io.TestLogDataGenerator;
import net.javaru.iip.frc.ui.AbstractRioLogContentExecutor;
import net.javaru.iip.frc.ui.RioLogFrcWindowContentExecutor;
import net.javaru.iip.frc.ui.RioLogRunWindowContentExecutor;



public class TestOpeningWindowAction extends DumbAwareAction
{
    private static final Logger LOG = Logger.getInstance(TestOpeningWindowAction.class);

    //TODO: put this in settings dialog and use from there
    private boolean useRunWindow = false;

    @Override
    public void actionPerformed(AnActionEvent actionEvent)
    {
        final Project project = actionEvent.getProject();
        if (project != null)
        {
            final FrcTempProcess process = new FrcTempProcess();

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

            final AbstractRioLogContentExecutor contentExecutor = useRunWindow ?
                                                            new RioLogRunWindowContentExecutor(project, processHandler) :
                                                            new RioLogFrcWindowContentExecutor(project, processHandler);


            // see com/jetbrains/python/run/PythonTask.java:221 for example
//            contentExecutor.withStop(
//                processHandler::destroyProcess,
//                () -> !processHandler.isProcessTerminated());
//            contentExecutor.withRerun(process::restart);

            Disposer.register(project, contentExecutor);

            contentExecutor.run();

        }

    }


    private class FrcTempProcess extends Process
    {
        protected volatile boolean running = true;
        private TestLogDataGenerator testLogDataGenerator = new TestLogDataGenerator();
        protected InputStream inputStream;


        public FrcTempProcess()
        {
            inputStream = testLogDataGenerator.getInputStream();
        }


        @Override
        public OutputStream getOutputStream()
        {
            return new OutputStream()
            {
                @Override
                public void write(int b) throws IOException
                {

                }
            };

        }


        @Override
        public InputStream getInputStream()
        {
           //return new ByteArrayInputStream("An Example InputStream with an ERROR word in it\nDEBUG Line 1\nDEBUG Line 2\nDEBUG line 3\nDEBUG line 4\nAn INFO Line\nAnother INFO line\nA WARN Line\nAnd a final INFO line".getBytes());
            return inputStream;
        }


        @Override
        public InputStream getErrorStream()
        {
            return new InputStream()
            {
                @Override
                public int read() throws IOException
                {
                    return 0;
                }
            };
        }


        @Override
        public int waitFor() throws InterruptedException
        {
            while (running)
            {
                Thread.sleep(1000);
            }
            return 0;
        }


        @Override
        public int exitValue()
        {
            return 0;
        }


        @Override
        public void destroy()
        {
            running = false;
            try { testLogDataGenerator.close(); } catch (Exception ignore) { } }


        public void restart()
        {
            testLogDataGenerator = new TestLogDataGenerator();
            running = true;
        }
    }



}
