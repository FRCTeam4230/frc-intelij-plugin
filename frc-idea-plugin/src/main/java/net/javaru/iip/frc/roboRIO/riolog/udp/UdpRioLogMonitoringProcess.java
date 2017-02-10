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

package net.javaru.iip.frc.roboRIO.riolog.udp;

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

import net.javaru.iip.frc.roboRIO.riolog.RioLogMonitoringProcess;
import net.javaru.iip.frc.settings.FrcApplicationComponent;
import net.javaru.iip.frc.ui.notify.FrcNotifications;



public class UdpRioLogMonitoringProcess extends RioLogMonitoringProcess
{
    private static final Logger LOG = Logger.getInstance(UdpRioLogMonitoringProcess.class);

    private static final boolean USE_DEBUGGING_SERVER = BooleanUtils.toBoolean(System.getProperty(SIMULATED_LOG_SERVICE_ENABLED_PROP_KEY,
                                                                                                  Boolean.FALSE.toString()));


    public UdpRioLogMonitoringProcess(Runnable clearConsoleRunnable) throws IllegalStateException
    {
        super(clearConsoleRunnable);
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
            this(getSettings().getRioLogPort());
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
        

        @Override
        @NotNull
        protected String getStartingMonitoringMessage() {return "==Monitoring RioLog on port " + getPort() + "==";}


        protected DatagramSocket createSocket() throws IOException
        {
            LOG.debug("[FRC] Creating DatagramSocket with port " + getPort());
            DatagramSocket socket = new DatagramSocket(getPort());
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
            return getSettings().getRioLogPort();
        }
        else
        {
            return Integer.valueOf(System.getProperty(SIMULATED_LOG_SERVICE_PORT_PROP_KEY, Integer.toString(SIMULATED_LOG_SERVICE_PORT_DEFAULT)));
        }
    }
}
