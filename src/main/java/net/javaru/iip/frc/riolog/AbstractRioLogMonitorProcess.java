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

package net.javaru.iip.frc.riolog;


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
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.output.NullOutputStream;
import org.jetbrains.annotations.NotNull;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.util.concurrency.Semaphore;

import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.settings.FrcApplicationSettings;



@SuppressWarnings("WeakerAccess")
public abstract class AbstractRioLogMonitorProcess extends Process
{
    private static final Logger LOG = Logger.getInstance(AbstractRioLogMonitorProcess.class);

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

    private final Semaphore myWaitSemaphore;


    protected boolean enabled = true;

    private PipedInputStream in;
    protected PrintWriter consoleWriter;
    protected PrintWriter fileWriter;
    private RioLogMonitoringRunnable rioLogMonitor;

    /**
     * Runnable that programmatically 'clicks' the clear button on the Executor window.
     */
    protected final Runnable clearConsoleRunnable;
    /**
     * Runnable that programmatically 'clicks' the stop button on the Executor window.
     * This runnable should <strong>not</strong> be called from within this class' 
     * {@link #stop()} method as that would cause an infinite loop since the 
     * stopRioLogRunnable calls this class's {@code stop()} method.
     */
    protected final Runnable stopRioLogRunnable;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");


    /**
     * 
     * @param clearConsoleRunnable Runnable that programmatically 'clicks' the clear button on the Executor window.
     * @param stopRioLogRunnable Runnable that programmatically 'clicks' the stop button on the Executor window.
     *                           
     * @throws IllegalStateException  If an initialization issue occurs
     */
    protected AbstractRioLogMonitorProcess(@NotNull Runnable clearConsoleRunnable, @NotNull Runnable stopRioLogRunnable) throws IllegalStateException
    {
        this.clearConsoleRunnable = clearConsoleRunnable;
        this.stopRioLogRunnable = stopRioLogRunnable;
        myWaitSemaphore = new Semaphore();
        myWaitSemaphore.down();
    }


    protected static FrcApplicationSettings getSettings() {return FrcApplicationSettings.Settings.INSTANCE(); }
    

    public void start()
    {
        try
        {
            in = new PipedInputStream();
            consoleWriter = new PrintWriter(new OutputStreamWriter(new PipedOutputStream(in), StandardCharsets.UTF_8), /*AutoFlush*/ true);

            if (getSettings().getLogNetConsoleToFile())
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
            myWaitSemaphore.up();
            LOG.warn("[FRC] Could not initialize riolog monitor. Cause Summary: " + e.toString(), e);
            Notifications.Bus.notify(new Notification(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP.getDisplayId(),
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
            thread.setName("RioLogMonitorProcess");
            thread.start();
        }
    }


    @NotNull
    protected abstract RioLogMonitoringRunnable initMonitoringRunnable();


    public void stop()
    {
        // Do not call the stopRioLogRunnable from the stop method. That runnable calls this stop method. So we'd get into an endless loop.
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


    private PrintWriter createFilePrintWriter()
    {
        try
        {
            final boolean append = getSettings().getLogNetConsoleToFileAppend();
            Path outputFile = determineOutputFilePath();
            Files.createDirectories(outputFile.getParent());
            return new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile.toFile(), append), StandardCharsets.UTF_8), /*AutoFlush*/ true);
        }
        catch (Exception e)
        {
            LOG.info("[FRC] Could not create PrintWriter for writing RiLog to file. Cause Summary: " + e.toString(), e);
            Notifications.Bus.notify(new Notification(FrcNotifications.FRC_GENERAL_NOTIFICATION_GROUP.getDisplayId(),
                                                      FrcNotifications.IconWarn,
                                                      FrcNotifications.Title,
                                                      "RioLog File Logging",
                                                      "Could not create writer to log RioLog to file. Cause:" + e.toString(),
                                                      NotificationType.WARNING,
                                                      null
            ));

            return new PrintWriter(new NullOutputStream());
        }
    }


    private Path determineOutputFilePath()
    {
        final Path directory = Paths.get(getSettings().getLogNetConsoleToFilePath());
        final String baseName = getSettings().getLogNetConsoleToFileBaseName();
        final String name = baseName.replace("${time}", dateFormat.format(new Date()));
        return directory.resolve(name);
    }


    protected void rollFileWriter()
    {
        if (fileWriter != null)
        {
            fileWriter.flush();
            fileWriter.close();
            fileWriter = createFilePrintWriter();
        }
    }


    protected void monitoringStopped()
    {
        // no op by default
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
        try
        {
            myWaitSemaphore.waitFor();
            return 0;
        }
        catch (ProcessCanceledException e)
        {
            return -1;
        }
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
                rioLogMonitor.stop();
            }
        }
        catch (Exception ignore) {}
        finally
        {
            myWaitSemaphore.up();
        }
        try { if (in != null) {in.close();} } catch (Exception ignore) {}
        try { if (consoleWriter != null) {consoleWriter.close();} } catch (Exception ignore) {}
        try { if (fileWriter != null) {fileWriter.close();} } catch (Exception ignore) {}
    }


    public boolean isEnabled() { return enabled; }


    protected interface RioLogMonitoringRunnable extends Runnable
    {
        int getPort();

        boolean isRunning();

        void stop();
        
        void logMessage(CharSequence message);
    }

    protected abstract class AbstractRioLogMonitoringRunnable implements RioLogMonitoringRunnable
    {
        protected final int port;
        protected boolean isRunning = true;


        protected AbstractRioLogMonitoringRunnable(int port) {this.port = port;}


        protected void processReceivedText(String received)
        {
            if (getSettings().getClearRioLogOnRobotRestart() && isRestartNotification(received))
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


        @Override
        public boolean isRunning() { return isRunning; }


        @Override
        public int getPort() { return port; }


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

        @Override
        public void logMessage(@NotNull CharSequence message)
        {
            final String msg = message.toString();
            consoleWriter.println(msg);
            consoleWriter.println();
            consoleWriter.flush();
            if (fileWriter != null)
            {
                fileWriter.println(msg);
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


        protected boolean isRestartNotification(String text)
        {
            if (getSettings().getUseRegexForRestartCheck())
            {
                //TODO: Make configurable in settings
                return RioLogGlobals.RIO_RESTART_REGEX_DEFAULT.matcher(text).find();
            }
            else
            {
                return text.contains("Launching") && text.contains("-jar") && text.contains("FRCUserProgram.jar");
            }
        }


        @Override
        public void stop()
        {
            isRunning = false;
        }
    }
}
