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

package net.javaru.iip.frc.riolog.ssh;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.Iterators;
import com.intellij.openapi.diagnostic.Logger;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import net.javaru.iip.frc.riolog.AbstractRioLogMonitorProcess;
import net.javaru.iip.frc.settings.FrcRoboRioSettings;
import net.javaru.iip.frc.settings.FrcSshSettings;



public class SshRioLogMonitorProcess extends AbstractRioLogMonitorProcess
{
    private static final Logger LOG = Logger.getInstance(SshRioLogMonitorProcess.class);

    //TODO - make these configurable in the settings
    private static final int IS_REACHABLE_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(5);
    private static final int CONNECTION_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(5);


    public SshRioLogMonitorProcess(Runnable clearConsoleRunnable) throws IllegalStateException
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


    private final BlockingQueue<String> riologQueue = new ArrayBlockingQueue<>(2_048);


    @Override
    protected void monitoringStopped()
    {
        super.monitoringStopped();
        riologQueue.clear();
    }


    boolean isReachable(String host)
    {
        if (StringUtils.isBlank(host))
        {
            return false;
        }

        final InetAddress address;
        try
        {
            address = InetAddress.getByName(host);
            if (address == null)
            {
                logToConsole("Host '" + host + "' is unreachable (Invalid INetAddress)");
                return false;
            }
        }
        catch (final UnknownHostException e)
        {
            logToConsole("Host '" + host + "' is unreachable (Unknown Host)");
            return false;
        }
        catch (Exception e)
        {
            logToConsole("Host '" + host + "' is unreachable (Invalid INetAddress)");
            return false;
        }


        try
        {
            boolean reachable = address.isReachable(IS_REACHABLE_TIMEOUT);
            if (!reachable)
            {
                logToConsole("Host '" + host + "' is unreachable (Unknown Host)");
            }
            return reachable;
        }
        catch (Exception e)
        {
            logToConsole("Host '" + host + "' is unreachable (" + e.toString() + ")");
            return false;
        }
    }


    private void logToConsoleConnectionError(Exception e)
    {
        String cause = e.getCause() == null ? e.toString() : e.getCause().toString();
        logToConsole("Could not connect. (" + cause + ")");
    }


    private void logToConsole(String msg)
    {
        riologQueue.offer("SSH Tail >> " + msg + "\n");
    }


    @NotNull
    public static String getExperimentalFeatureMessage()
    {
        return "\n\n" +
               "************************************************************************************************************************\n" +
               "** This is an Experimental Feature. Please report issues at https://gitlab.com/Javaru/frc-intellij-idea-plugin/issues **\n" +
               "************************************************************************************************************************\n";
    }

    private class SshRioLogMonitor extends AbstractMonitoringRunnable
    {
        SshRioLogMonitor(int port)
        {
            super(port);
        }


        @NotNull
        @Override
        protected String getStartingMonitoringMessage()
        {
            return getExperimentalFeatureMessage();
        }


        @Override
        public void run()
        {
            if (!getSettings().isTeamNumberConfigured())
            {
                consoleWriter.println();
                consoleWriter.println("========================================================================================================");
                consoleWriter.println("==  YOUR TEAM NUMBER IS NOT CONFIGURED.                                                               ==");
                consoleWriter.println("==  To use SSH tailing, please configure your Team Number in Settings > Languages & Frameworks > FRC  ==");
                consoleWriter.println("========================================================================================================");
                isRunning = false;
                enabled = false;
            }
            else
            {
                logStartingMonitoring();
                isRunning = true;

                JSchSShTailer tailer = new JSchSShTailer();
                tailer.start();

                while (enabled)
                {
                    try
                    {
                        final String receivedText = riologQueue.poll(1, TimeUnit.SECONDS);
                        if (receivedText != null)
                        {
                            processReceivedText(receivedText);
                        }
                    }
                    catch (InterruptedException e)
                    {
                        LOG.debug("[FRC] InterruptedException in SshRioLogMonitor.run(). Calling 'stop().");
                        isRunning = false;
                        enabled = false;
                        stop();
                    }
                }
                tailer.stop();
            }
        }
    }


    class MyProcessStream extends AbstractProcessStream
    {
        @Override
        protected void processLine(String line)
        {
            riologQueue.offer(line);
        }
    }


    class JSchSShTailer
    {
        private AtomicBoolean running = new AtomicBoolean(false);

        private final ConnectionConfig connectionConfig = new ConnectionConfig();
        private final ProcessStream stdProcessStream = new MyProcessStream();
        private final ProcessStream errProcessStream = stdProcessStream;
        private ExecutorService executorService;
        private Future<?> runningFuture;


        void run()
        {
            LOG.debug("[FRC] Starting SSH TailRunnable...");
            executorService = Executors.newSingleThreadExecutor();
            runningFuture = executorService.submit(new TailRunnable());
        }


        void start()
        {
            stdProcessStream.start();
            if (!stdProcessStream.equals(errProcessStream))
            {
                errProcessStream.start();
            }
            LOG.debug("[FRC] JSchSShTailer.start() called");
            if (!running.getAndSet(true))
            {
                run();
            }
        }


        void stop()
        {
            LOG.debug("[FRC] JSchSShTailer.stop() called");
            this.running.set(false);

            //Block briefly until shutdown is complete...
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<Void> future = executor.submit(() ->
                                                  {
                                                      while (isRunning())
                                                      {
                                                          try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException ignore) {}
                                                      }
                                                      return null;
                                                  });

            try
            {
                future.get(3, TimeUnit.SECONDS);
            }
            catch (TimeoutException e)
            {
                future.cancel(true);
            }
            catch (Exception ignore) { }


            stdProcessStream.stop();
            if (!errProcessStream.equals(stdProcessStream))
            {
                errProcessStream.stop();
            }
        }


        boolean isRunning() { return running.get() && runningFuture != null && !runningFuture.isDone(); }


        private class TailRunnable implements Runnable
        {
            private Channel channel;

            private Session session;

            @Nullable
            private String lastKnownGoodHost;


            @Override
            public void run()
            {
                LOG.debug("[FRC] TailRunnable.run() running");
                while (running.get())
                {
                    if (!isConnectedFully())
                    {
                        do
                        {
                            connect();
                            if (!isConnectedFully())
                            {
                                logToConsole("Unable to connect to roboRIO via SSH. Will attempt reconnect in 20 seconds...\n");
                                //This is the connection retry interval - TODO make the retry interval configurable
                                try {TimeUnit.SECONDS.sleep(20);} catch (InterruptedException ignore) {}
                            }
                        } while (running.get() && !isConnectedFully());
                    }

                    //Prevent the main loop from being too tight - we're basically just seeing if we have lost connection.
                    try {TimeUnit.SECONDS.sleep(10);} catch (InterruptedException ignore) {}
                }

                LOG.debug("[FRC] Shutting down the SSH tailer by disconnecting ssh channel and session. [JSchSShTailer.running = " + running.get() + ")");
                disconnectFully();
                LOG.debug("[FRC] Tailer  shut down complete");
            }


            private void connect()
            {
                //disconnect any previously existing sessions/channels
                disconnectFully();
                this.session = null;
                this.channel = null;

                if (lastKnownGoodHost != null)
                {
                    LOG.debug("[FRC] Attempting to connect to lastKnownGoodHost of: " + lastKnownGoodHost);
                    connect(lastKnownGoodHost, false);
                }

                if (!isConnectedFully())
                {
                    // TODO - make connection order configurable
                    final Iterator<String> hosts = Iterators.forArray(FrcRoboRioSettings.Settings.INSTANCE().getRoboRioHost_mDNS(),
                                                                      FrcRoboRioSettings.Settings.INSTANCE().getRoboRioHost_DNS(),
                                                                      FrcRoboRioSettings.Settings.INSTANCE().getRoboRioHost_USB(),
                                                                      FrcRoboRioSettings.Settings.INSTANCE().getRoboRioHost_IP());

                    while (!isConnectedFully() && hosts.hasNext())
                    {
                        connect(hosts.next(), true);
                    }
                }
            }


            private void connect(String host, boolean skipIfIsLastKnownGoodHost)
            {
                this.session = null;
                this.channel = null;
                if (host == null)
                {
                    return;
                }

                if (skipIfIsLastKnownGoodHost && StringUtils.equals(host, this.lastKnownGoodHost))
                {
                    LOG.debug("[FRC] Skipping host '" + host + "' as it was the lastKnownGoodHost and already checked");
                }
                else
                {
                    this.session = connectSession(host);
                    if (this.session != null)
                    {
                        logToConsole("Attempting to create channel...");
                        this.channel = connectChannel();
                        if (this.channel == null)
                        {
                            this.session = null;
                        }
                        else
                        {
                            LOG.debug("[FRC] Setting lastKnownGoodHost to: " + host);
                            lastKnownGoodHost = host;
                        }
                    }
                }
            }


            @Nullable
            private Session connectSession(String host)
            {
                if (host == null)
                {
                    return null;
                }
                if (LOG.isDebugEnabled())
                {
                    LOG.debug(String.format("[FRC] Initializing Session: username = '%s';  host = '%s';  Tail command: %s",
                                            connectionConfig.getUsername(),
                                            host,
                                            connectionConfig.getTailCommand()));
                }

                logToConsole("Attempting to connect to roboRIO via '" + connectionConfig.getUsername() + "@" + host + "'");

                if (!isReachable(host))
                {
                    return null;
                }

                try
                {

                    final JSch jsch = new JSch();
                    session = jsch.getSession(connectionConfig.getUsername(), host, connectionConfig.getPort());
                    session.setUserInfo(connectionConfig);

                    if (!connectionConfig.useStrictHostKeyChecking())
                    {
                        session.setConfig("StrictHostKeyChecking", "no");
                    }
                    else
                    {
                        session.setConfig("StrictHostKeyChecking", "yes");
                        jsch.setKnownHosts(connectionConfig.getKnownHostsFilePath().toString());
                    }
                }
                catch (JSchException e)
                {
                    // jsch.getSession - throws if username or host is invalid
                    // jsch.setKnownHosts - throws if file is invalid
                    logToConsoleConnectionError(e);
                    LOG.debug("[FRC] An exception occurred when attempting to create a session: " + e.toString(), e);
                    session = null;
                }

                if (session != null)
                {
                    try
                    {
                        LOG.debug("[FRC] Connecting session");
                        session.connect(CONNECTION_TIMEOUT);
                        return (isSessionValidAndConnected()) ? session : null;
                    }
                    catch (JSchException e)
                    {
                        logToConsoleConnectionError(e);
                        LOG.debug("[FRC] An exception occurred when connecting the session: " + e.toString(), e);
                    }
                }
                return null;
            }


            @Nullable
            private Channel connectChannel()
            {
                Channel channel = null;
                try
                {
                    if (isSessionValidAndConnected())
                    {
                        LOG.debug("[FRC] Connecting channel");
                        channel = session.openChannel("exec");
                        final ChannelExec channelExec = (ChannelExec) channel;
                        channelExec.setCommand(connectionConfig.getTailCommand());

                        channel.setInputStream(null);

                        channelExec.setErrStream(errProcessStream.getOutputStream());
                        channelExec.setOutputStream(stdProcessStream.getOutputStream());

                        LOG.debug("[FRC] Calling channel.connect()");
                        channelExec.connect(CONNECTION_TIMEOUT);
                        if (!channel.isConnected())
                        {
                            logToConsole("Could not connect channel");
                            channel = null;
                        }
                    }
                    else
                    {
                        LOG.debug("[FRC] Session is not connected. (Will not attempt to connect the channel.)");
                    }
                }
                catch (Exception e)
                {
                    logToConsoleConnectionError(e);
                    LOG.debug("An exception occurred when connecting the channel. Cause Summary: " + e.toString(), e);
                }
                return channel;
            }


            private void disconnectFully()
            {
                if (channel != null)
                {
                    try
                    {
                        channel.disconnect();
                    }
                    catch (Exception ignore) {}
                }

                if (session != null)
                {
                    try
                    {
                        session.disconnect();
                    }
                    catch (Exception ignore) {}
                }
            }


            @SuppressWarnings("BooleanMethodIsAlwaysInverted")
            private boolean isConnectedFully()
            {
                return isSessionValidAndConnected() && isChannelValidAndConnected();
            }


            private boolean isChannelValidAndConnected()
            {
                return channel != null && channel.isConnected() && !channel.isClosed();
            }


            private boolean isSessionValidAndConnected()
            {
                return session != null && session.isConnected();
            }
        }
    }


    //TODO Allow for custom settings
    public class ConnectionConfig implements UserInfo
    {
        private final boolean useStrictHostKeyChecking = false;
        private final int port = 22;
        private Path knownHostsFilePath = Paths.get(System.getProperty("user.home", "C:\\Users\\Public"))
                                               .resolve(".ssh")
                                               .resolve("known_hosts")
                                               .toAbsolutePath();


        boolean useStrictHostKeyChecking() { return useStrictHostKeyChecking; }


        Path getKnownHostsFilePath() { return knownHostsFilePath; }


        int getPort() { return port; }


        String getUsername() { return FrcSshSettings.Settings.INSTANCE().getSshUsername(); }


        String getTailCommand() { return FrcSshSettings.Settings.INSTANCE().getSshTailCommand(); }


        @Override
        public String getPassphrase()
        {
            LOG.debug("[FRC] UserInfo.getPassphrase() called");
            return "";
        }


        @Override
        public String getPassword()
        {
            LOG.debug("[FRC] UserInfo.getPassword() called");
            return FrcSshSettings.Settings.INSTANCE().getSshPassword();
        }


        @Override
        public boolean promptPassword(String message)
        {
            LOG.debug("[FRC] UserInfo.promptPassword() called with a message of '" + message + "'");
            //riologQueue.offer(message);
            return true;
        }


        @Override
        public boolean promptPassphrase(String message)
        {
            LOG.debug("[FRC] UserInfo.promptPassphrase() called with a message of '" + message + "'");
            //riologQueue.offer(message);
            return true;
        }


        @Override
        public boolean promptYesNo(String message)
        {
            LOG.debug("[FRC] UserInfo.promptYesNo() called with a message of '" + message + "'");
            //riologQueue.offer(message);
            return true;
        }


        @Override
        public void showMessage(String message)
        {
            LOG.debug("[FRC] UserInfo.showMessage() called with a message of:\n" + message);
            riologQueue.offer(message);
        }
    }
}
