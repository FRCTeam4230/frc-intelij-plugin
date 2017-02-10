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

package net.javaru.iip.frc.roboRIO.riolog.ssh;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.roboRIO.riolog.RioLogMonitoringProcess;



public class SshRioLogMonitoringProcess extends RioLogMonitoringProcess
{
    private static final Logger LOG = Logger.getInstance(SshRioLogMonitoringProcess.class);


    public SshRioLogMonitoringProcess(Runnable clearConsoleRunnable) throws IllegalStateException
    {
        super(clearConsoleRunnable);
    }


    @NotNull
    @Override
    protected MonitoringRunnable initMonitoringRunnable()
    {
        //Long term to do - make port configurable
        return new SshRioLogMonitor(22);
    }


    private class SshRioLogMonitor extends AbstractMonitoringRunnable
    {
        
        protected SshRioLogMonitor(int port)
        {
            super(port);
        }


        @NotNull
        @Override
        protected String getStartingMonitoringMessage()
        {
            return "==Monitoring RioLog via SSH tail==";
        }


        @Override
        public void run()
        {
            logStartingMonitoring();
            isRunning = true;

            while (enabled)
            {
                processReceivedText("Simulated SSH tail content " + new Date() + "\n");
                try {TimeUnit.SECONDS.sleep(5);} catch (InterruptedException ignore) {}
            }

        }
    }
}
