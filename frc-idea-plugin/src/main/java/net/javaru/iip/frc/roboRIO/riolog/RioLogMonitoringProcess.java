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

package net.javaru.iip.frc.roboRIO.riolog;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.settings.FrcApplicationComponent;
import net.javaru.iip.frc.settings.FrcSettings;
import net.javaru.iip.frc.ui.notify.FrcNotifications;



@SuppressWarnings("WeakerAccess")
public abstract class RioLogMonitoringProcess extends Process
{
    private static final Logger LOG = Logger.getInstance(RioLogMonitoringProcess.class);

    public final static int MAX_PACKET_SIZE = 65507;

    /**
     * Set this System property to 'on' or 'true' to turn on the testing server that will send a constant output of
     * mock log messages to the rioLog port. Used for testing without being attached to a RoboRIO.
     */
    public static final String SIMULATED_LOG_SERVICE_PROP_KEY_BASE = "frc.simulated.log.service";
    public static final String SIMULATED_LOG_SERVICE_ENABLED_PROP_KEY = SIMULATED_LOG_SERVICE_PROP_KEY_BASE + ".enabled";
    public static final String SIMULATED_LOG_SERVICE_PORT_PROP_KEY = SIMULATED_LOG_SERVICE_PROP_KEY_BASE + ".port";
    public static final String SIMULATED_LOG_SERVICE_USE_CONFIGURED_PORT_PROP_KEY = SIMULATED_LOG_SERVICE_PROP_KEY_BASE + ".use.configured.port";
    public static final int SIMULATED_LOG_SERVICE_PORT_DEFAULT = 4248; //arbitrarily chosen port not listed at https://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers

    

    protected boolean enabled = true;

    private PipedInputStream in;
    protected PrintWriter consoleWriter;
    protected PrintWriter fileWriter;
    private MonitoringRunnable rioLogMonitor;


    protected final Runnable clearConsoleRunnable;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");


    protected RioLogMonitoringProcess(Runnable clearConsoleRunnable) throws IllegalStateException
    {
        this.clearConsoleRunnable = clearConsoleRunnable;
    }


    protected static FrcSettings getSettings() {return FrcApplicationComponent.getInstance().getState();}


    public void start()
    {
        try
        {
            in = new PipedInputStream();
            consoleWriter = new PrintWriter(new OutputStreamWriter(new PipedOutputStream(in), StandardCharsets.UTF_8), /*AutoFlush*/ true);

            if (getSettings().isLogToFile())
            {
                fileWriter = createFilePrintWriter();
            }

        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not create necessary io streams.", e);
        }

        try
        {
            enabled = true;
            rioLogMonitor = initMonitoringRunnable();
        }
        catch (Exception e)
        {
            enabled = false;
            LOG.warn("[FRC] Could not initialize riolog monitor. Cause Summary: " + e.toString(), e);
            Notifications.Bus.notify(new Notification(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP,
                                                      FrcNotifications.IconError,
                                                      FrcNotifications.Title,
                                                      "RioLog Initialization Failure",
                                                      "Could not initialize the RioLog socket monitor. See idea.log for more details.",
                                                      NotificationType.ERROR,
                                                      null
                                                      ));
        }
        
        
        if (enabled && rioLogMonitor != null)
        {
            Thread thread = new Thread(rioLogMonitor);
            thread.setName("RioLogMonitoringProcess");
            thread.start();
        }
    }


    @NotNull
    protected abstract MonitoringRunnable initMonitoringRunnable();
    


    public void stop()
    {
        destroy();
    }


    @SuppressWarnings("unused")
    public void restart()
    {
        stop();
        start();
    }


    public int getMonitoredPort()
    {
        return rioLogMonitor == null ? -1 : rioLogMonitor.getPort();
    }

    private PrintWriter createFilePrintWriter() throws IOException
    {
        final boolean append = getSettings().isLogFileAppend();
        Path outputFile = determineOutputFilePath();
        Files.createDirectories(outputFile.getParent());
        return new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile.toFile(), append), StandardCharsets.UTF_8), /*AutoFlush*/ true);
    }


    private Path determineOutputFilePath()
    {
        final Path directory = getSettings().getLogFileDirectory();
        final String baseName = getSettings().getLogFileBaseName();
        final String name = baseName.replace("${time}", dateFormat.format(new Date()));
        return directory.resolve(name);
    }


    protected void rollFileWriter() throws IOException
    {
        if (fileWriter != null)
        {
            fileWriter.flush();
            fileWriter.close();
            fileWriter = createFilePrintWriter();
        }
    }


    @Override
    public InputStream getInputStream() { return in; }


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
        while (enabled)
        {
            TimeUnit.MILLISECONDS.sleep(500);
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
        enabled = false;
        try
        {
            
            if (rioLogMonitor != null)
            {
                int count = 0;
                while (rioLogMonitor.isRunning() && count <  60 /* 60 * 50 = 3,000ms (3 seconds)*/)
                {
                    try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException ignore) {}
                    count++;
                }
            }
        }
        catch (Exception ignore) {}
        try { if (in != null) {in.close();} } catch (Exception ignore) {}
        try { if (consoleWriter != null) {consoleWriter.close();} } catch (Exception ignore) {}
        try { if (fileWriter != null) {fileWriter.close();} } catch (Exception ignore) {}
    }


    public boolean isEnabled() { return enabled; }

    protected interface MonitoringRunnable extends Runnable
    {
        int getPort();
        boolean isRunning();
        
    }
    
    protected abstract class AbstractMonitoringRunnable implements MonitoringRunnable
    {
        protected void processReceivedText(String received) throws IOException
        {
            if (getSettings().isClearOnRobotRestart() && RioLogUtils.isRestartNotification(received))
            {
                clearConsoleRunnable.run();
                rollFileWriter();
                logStartingMonitoring();
            }

            consoleWriter.print(received);
            if (addLineBreak())
            {
                consoleWriter.println();
            }
            consoleWriter.flush();
            if (fileWriter != null)
            {
                fileWriter.print(received);
                if (addLineBreak())
                {
                    fileWriter.println();
                }
                fileWriter.flush();
            }
        }


        protected void logStartingMonitoring()
        {
            consoleWriter.println(getStartingMonitoringMessage());
            consoleWriter.println();
            consoleWriter.flush();
            if (fileWriter != null)
            {
                fileWriter.println(getStartingMonitoringMessage());
                fileWriter.println();
                fileWriter.flush();
            }
        }


        @NotNull
        protected abstract String getStartingMonitoringMessage();

        protected boolean addLineBreak()
        {
            //TODO: add to settings
            return false;
        }
    }
}
