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

package net.javaru.iip.frc.riolog.udp;

import java.io.IOException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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

import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.riolog.AbstractRioLogMonitorProcess;
import net.javaru.iip.frc.settings.FrcApplicationSettingsConfigurable;



public class UdpRioLogMonitorProcess extends AbstractRioLogMonitorProcess
{
    private static final Logger LOG = Logger.getInstance(UdpRioLogMonitorProcess.class);

    private static final boolean USE_DEBUGGING_SERVER = BooleanUtils.toBoolean(System.getProperty(SIMULATED_LOG_SERVICE_ENABLED_PROP_KEY,
                                                                                                  Boolean.FALSE.toString()));

    private boolean isFirstBindAttempt = true;

    /**
     * @param clearConsoleRunnable Runnable that programmatically 'clicks' the clear button on the Executor window.
     * @param stopRioLogRunnable   Runnable that programmatically 'clicks' the stop button on the Executor window.
     *
     * @throws IllegalStateException If an initialization issue occurs
     */
    public UdpRioLogMonitorProcess(@NotNull Runnable clearConsoleRunnable, @NotNull Runnable stopRioLogRunnable) throws IllegalStateException
    {
        super(clearConsoleRunnable, stopRioLogRunnable);
    }


    @NotNull
    @Override
    protected MonitoringRunnable initMonitoringRunnable()

    {
        MonitoringRunnable rioLogMonitor;
        if (USE_DEBUGGING_SERVER)
        {
            rioLogMonitor = new TestingUdpRioLogMonitor();
            LOG.warn(String.format("[FRC] System Property '%s is set to 'true'. Using '%s' for monitoring on port '%d'.",
                                   SIMULATED_LOG_SERVICE_PROP_KEY_BASE,
                                   rioLogMonitor.getClass().getSimpleName(),
                                   rioLogMonitor.getPort()));
        }
        else
        {
            rioLogMonitor = new UdpRioLogMonitor();
            LOG.info(String.format("[FRC] Using '%s' for monitoring on port '%d'.",
                                   rioLogMonitor.getClass().getSimpleName(),
                                   rioLogMonitor.getPort()));
        }
        return rioLogMonitor;
    }


    private class UdpRioLogMonitor extends AbstractMonitoringRunnable
    {


        protected UdpRioLogMonitor()
        {
            this(getSettings().getRioLogUdpPort());
        }


        public UdpRioLogMonitor(int port)
        {
            super(port);
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
                        processReceivedText(received);
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
                final String msg = "Could not bind to the RioLog port. This can occur if the port is already bound to in another " 
                                   + "instance of IntelliJ IDEA with an FRC project open, or by another tool, such as Eclipse. "
                                   + "You will need to stop the RioLog monitoring in the other tool and then then reattempt to start monitoring.";
                LOG.warn(msg + " Cause Summary: " + e.toString(), e);
                consoleWriter.println();
                consoleWriter.println("==Could not bind to the RioLog UDP port.==");
                consoleWriter.println("This can occur if the port is already bound to in another instance of IntelliJ IDEA with an FRC project open, or by another tool, such as Eclipse.");
                consoleWriter.println("You will need to stop the monitoring in the other tool and then reattempt to start monitoring.");
                consoleWriter.println("Since this UDP based RIOLog monitoring is no longer used (since 2018), and this is considered legacy feature, there is no plan to resolve this minor shortcoming of only having a single monitor running at a time.");
                consoleWriter.println();
                consoleWriter.flush();
                // publishBindWarning(msg);
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


        @Override
        @NotNull
        protected String getStartingMonitoringMessage() {return "==Monitoring RioLog on port " + getPort() + "==";}


        protected DatagramSocket createSocket() throws IOException
        {
            // We try to create the socket doing a retry after 1 second on the first BindException
            LOG.debug("[FRC] Creating DatagramSocket with port " + getPort());
            DatagramSocket socket;
            try
            {
                socket = new DatagramSocket(getPort());
            }
            catch (BindException e)
            {
                try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException ignore) {}
                socket = new DatagramSocket(getPort());
            }
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            return socket;
        }


        protected void cleanUpSocket(DatagramSocket socket) throws IOException
        {
            //no op - here mostly for the testing version of this class
        }


        protected void publishBindWarning(String msg)
        {
            final Notification notification = new Notification(FrcNotifications.FRC_ACTIONABLE_NOTIFICATION_GROUP.getDisplayId(),
                                                               FrcNotifications.IconWarn,
                                                               FrcNotifications.Title,
                                                               "RIOLog Monitor",
                                                               msg,
                                                               NotificationType.WARNING,
                                                               (theNotification, event) ->
                                                               {
                                                                   if ("configure".equals(event.getDescription()))
                                                                   {
                                                                       final Configurable configurable = FrcApplicationSettingsConfigurable.getInstance();
                                                                       IdeFrame ideFrame = WindowManagerEx.getInstanceEx().findFrameFor(null);
                                                                       ShowSettingsUtil.getInstance().editConfigurable((JFrame) ideFrame, configurable);
                                                                   }
                                                                   theNotification.expire();
                                                               }
            );
            Notifications.Bus.notify(notification, null);
        }
    }


    private class TestingUdpRioLogMonitor extends UdpRioLogMonitor
    {
        private final InetAddress groupAddress;


        public TestingUdpRioLogMonitor()
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
            MulticastSocket socket = new MulticastSocket(getPort());
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
        protected String getStartingMonitoringMessage() { return "«««Monitoring *SIMULATED* RioLog on port " + getPort() + "»»»"; }


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
            return getSettings().getRioLogUdpPort();
        }
        else
        {
            return Integer.valueOf(System.getProperty(SIMULATED_LOG_SERVICE_PORT_PROP_KEY, Integer.toString(SIMULATED_LOG_SERVICE_PORT_DEFAULT)));
        }
    }
}
