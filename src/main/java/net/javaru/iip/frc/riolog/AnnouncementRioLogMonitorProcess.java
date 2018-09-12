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

package net.javaru.iip.frc.riolog;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;



public class AnnouncementRioLogMonitorProcess extends AbstractRioLogMonitorProcess
{
    private static final Logger LOG = Logger.getInstance(AnnouncementRioLogMonitorProcess.class);


    private final CharSequence message;


    /**
     * @param clearConsoleRunnable Runnable that programmatically 'clicks' the clear button on the Executor window.
     * @param stopRioLogRunnable   Runnable that programmatically 'clicks' the stop button on the Executor window.
     * @param message              The message to display on the RioLog console                            
     *
     * @throws IllegalStateException If an initialization issue occurs
     */
    public AnnouncementRioLogMonitorProcess(@NotNull Runnable clearConsoleRunnable, @NotNull Runnable stopRioLogRunnable, @NotNull CharSequence message) throws IllegalStateException
    {
        super(clearConsoleRunnable,stopRioLogRunnable);
        this.message = message;
    }


    @NotNull
    @Override
    protected RioLogMonitoringRunnable initMonitoringRunnable()
    {
        return new AnnouncementRioLogMonitoringRunnable();
    }
    
    class AnnouncementRioLogMonitoringRunnable extends AbstractRioLogMonitoringRunnable
    {

        protected AnnouncementRioLogMonitoringRunnable()
        {
            super(0);
        }


        @NotNull
        @Override
        protected String getStartingMonitoringMessage()
        {
            return "";
        }


        @Override
        public void run()
        {
            logMessage(message);
            stopRioLogRunnable.run();
        }
    }
}
