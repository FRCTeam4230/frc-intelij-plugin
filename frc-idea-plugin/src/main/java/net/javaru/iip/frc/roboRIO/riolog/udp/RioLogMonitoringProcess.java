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

package net.javaru.iip.frc.roboRIO.riolog.udp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import javax.swing.*;

import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.ex.WindowManagerEx;

import net.javaru.iip.frc.roboRIO.riolog.RioLogUtils;
import net.javaru.iip.frc.settings.FrcApplicationComponent;
import net.javaru.iip.frc.settings.FrcSettings;
import net.javaru.iip.frc.ui.notify.FrcNotifications;



public class RioLogMonitoringProcess extends Process
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

    private static final boolean USE_DEBUGGING_SERVER = BooleanUtils.toBoolean(System.getProperty(SIMULATED_LOG_SERVICE_ENABLED_PROP_KEY,
                                                                                                  Boolean.FALSE.toString()));


    private boolean enabled = true;

    private PipedInputStream in;
    private PrintWriter consoleWriter;
    private PrintWriter fileWriter;
    private RioLogMonitor rioLogMonitor;


    private final Runnable clearConsoleRunnable;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");


    public RioLogMonitoringProcess(Runnable clearConsoleRunnable) throws IllegalStateException
    {
        this.clearConsoleRunnable = clearConsoleRunnable;
    }


    private static FrcSettings getSettings() {return FrcApplicationComponent.getInstance().getState();}


    public void restart()
    {
        stop();
        start();
    }


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
            if (USE_DEBUGGING_SERVER)
            {
                rioLogMonitor = new TestingRioLogMonitor();
                LOG.warn(String.format("[FRC] System Property '%s is set to 'true'. Using '%s' for monitoring on port '%d'.",
                                       SIMULATED_LOG_SERVICE_PROP_KEY_BASE,
                                       rioLogMonitor.getClass().getSimpleName(),
                                       rioLogMonitor.port));
            }
            else
            {
                rioLogMonitor = new RioLogMonitor();
                LOG.info(String.format("[FRC] Using '%s' for monitoring on port '%d'.",
                                       rioLogMonitor.getClass().getSimpleName(),
                                       rioLogMonitor.port));
            }
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


    public void stop()
    {
        destroy();
    }

    
    public int getMonitoredPort()
    {
        return rioLogMonitor == null ? -1 : rioLogMonitor.port;
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


    private void rollFileWriter() throws IOException
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
                while (rioLogMonitor.isRunning && count <  60 /* 60 * 50 = 3,000ms (3 seconds)*/)
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


    private class RioLogMonitor implements Runnable
    {
        protected boolean isRunning = true;

        protected final int port;


        protected RioLogMonitor()
        {
            this(getSettings().getRioLogPort());
        }


        public RioLogMonitor(int port)
        {
            this.port = port;
        }


        @Override
        public void run()
        {
            logStartingMonitoring();

            isRunning = true;
            byte[] buffer = new byte[MAX_PACKET_SIZE];
            try (DatagramSocket socket = createSocket())
            {
                socket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(1)); // check every x seconds for shutdown

                DatagramPacket incomingPacket;

                while (enabled)
                {
                    incomingPacket = new DatagramPacket(buffer, buffer.length);
                    try
                    {
                        socket.receive(incomingPacket);
                        final String received = new String(incomingPacket.getData(), 0, incomingPacket.getLength());


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
                    catch (SocketTimeoutException ignore)
                    {
                        if (!enabled)
                        {
                            isRunning = false;
                            consoleWriter.flush();
                            if (fileWriter != null)
                            {
                                fileWriter.flush();
                            }
                            cleanUpSocket(socket);
                            return;
                        }
                    }
                    catch (IOException e)
                    {
                        LOG.warn("[FRC] An IOException occurred while monitoring the RIO Log UDP output", e);
                        isRunning = false;
                        cleanUpSocket(socket);
                        return;
                    }
                }
            }
            catch (BindException e)
            {
                final String msg = "Could not bind to the RioLog port. This is likely due to a second IDEA window with an "
                                   + "FRC project being open. It is a known limitation that only one FRC project can be " 
                                   + "opened at a time. A fix for all FRC projects to share the port is planned for a " 
                                   + "future release.";
                LOG.warn(msg + " Cause Summary: " + e.toString(), e);
                publishBindWarning(msg);
                isRunning = false;
            }
            catch (Exception e)
            {
                LOG.warn("[FRC] An Exception occurred while monitoring the RIO Log UDP output", e);
                isRunning = false;
            }
            //This sets isRunning to false after the while(enabled) loop exits so the destroy method knows its ok to exit
            isRunning = false;
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
        protected String getStartingMonitoringMessage() {return "==Monitoring RioLog on port " + port + "==";}


        protected DatagramSocket createSocket() throws IOException
        {
            LOG.debug("[FRC] Creating DatagramSocket with port " + port);
            DatagramSocket socket = new DatagramSocket(port);
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            return socket;
        }


        protected void cleanUpSocket(DatagramSocket socket) throws IOException
        {
            //no op - here mostly for the testing version of this class
        }


        protected boolean addLineBreak()
        {
            //TODO: add to settings
            return false;
        }
        
        
        protected void publishBindWarning(String msg)
        {
            final Notification notification = new Notification(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP,
                                                               FrcNotifications.IconWarn,
                                                               FrcNotifications.Title,
                                                               "RIOLog Monitor",
                                                               /*"Cannot bind to the RIOLOg port as it is already in use.<br>" 
                                                               + "Likely there in another IDEA window opened with an FRC project.<br>" 
                                                               + "It is a known issue that only FRC project can be opened at a time.<br>" 
                                                               + "I hope to address it in near future release."*/
                                                               msg,
                                                               NotificationType.WARNING,
                                                               (theNotification, event) ->
                                                               {
                                                                   if ("configure".equals(event.getDescription()))
                                                                   {
                                                                       final Configurable configurable = FrcApplicationComponent.getInstance();
                                                                       IdeFrame ideFrame = WindowManagerEx.getInstanceEx().findFrameFor(null);
                                                                       ShowSettingsUtil.getInstance().editConfigurable((JFrame) ideFrame, configurable);
                                                                   }
                                                                   theNotification.expire();
                                                               }
            );
            Notifications.Bus.notify(notification, null);
        }
    }


    private class TestingRioLogMonitor extends RioLogMonitor
    {
        private final InetAddress groupAddress;
        


        public TestingRioLogMonitor()
        {
            super(determineTestPort());
            
            try
            {
                groupAddress = InetAddress.getByName("230.0.0.1");
            }
            catch (UnknownHostException e)
            {
                final String message = "Cannot create group InetAddress due to an exception.";
                LOG.warn("[FRC] " + message);
                throw new IllegalStateException(message, e);
            }
        }


        @Override
        protected DatagramSocket createSocket() throws IOException
        {
            MulticastSocket socket = new MulticastSocket(port);
            socket.joinGroup(groupAddress);
            return socket;
        }


        @Override
        protected void cleanUpSocket(DatagramSocket socket) throws IOException
        {
            if (socket instanceof MulticastSocket)
            {
                ((MulticastSocket) socket).leaveGroup(groupAddress);
            }
        }


        @NotNull
        @Override
        protected String getStartingMonitoringMessage() { return "«««Monitoring *SIMULATED* RioLog on port " + port + "»»»"; }


        @Override
        protected boolean addLineBreak()
        {
            return true;
        }


    }


    private static int determineTestPort()
    {
        boolean useConfiguredPort = BooleanUtils.toBoolean(System.getProperty(SIMULATED_LOG_SERVICE_USE_CONFIGURED_PORT_PROP_KEY, "false"));
        if (useConfiguredPort)
        {
            return getSettings().getRioLogPort();
        }
        else
        {
            return Integer.valueOf(System.getProperty(SIMULATED_LOG_SERVICE_PORT_PROP_KEY, Integer.toString(SIMULATED_LOG_SERVICE_PORT_DEFAULT)));
        }
    }
}
