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

package net.javaru.iip.frc.udp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.NotNull;
import com.google.common.base.Charsets;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.settings.FrcApplicationComponent;
import net.javaru.iip.frc.settings.FrcSettings;



public class RioLogMonitor extends Process implements AutoCloseable
{
    private static final Logger LOG = Logger.getInstance(RioLogMonitor.class);

    public final static int MAX_PACKET_SIZE = 65507;

    private boolean enabled = true;

    private PipedInputStream in;
    private PrintWriter consoleWriter;
    private PrintWriter fileWriter;
    private MonitorRunnable monitorRunnable;


    private Runnable clearConsoleRunnable;

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");


    public RioLogMonitor() throws IllegalStateException
    {
        restart();
    }


    private static FrcSettings getSettings() {return FrcApplicationComponent.getInstance().getState();}



    public void restart()
    {
        try
        {
            close();
        }
        catch (Exception e)
        {
            final String msg = "Could not stop the current monitoring thread. Cause Details: " + e.toString();
            throw new IllegalStateException(msg);
        }


        try
        {
            in = new PipedInputStream();
            consoleWriter = new PrintWriter(new OutputStreamWriter(new PipedOutputStream(in), Charsets.UTF_8), /*AutoFlush*/ true);

            if (getSettings().isLogToFile())
            {
                fileWriter = createFilePrintWriter();
            }

        }
        catch (Exception e)
        {
            throw new IllegalStateException("Could not create necessary io streams.", e);
        }

        enabled  = true;
        //TODO: Change to new MonitorRunnable() once initial development is completed
//        monitorRunnable = new MonitorRunnable();
        monitorRunnable = new MultiCastTestingMonitor();
        Thread thread = new Thread(monitorRunnable);
        thread.setName("RioLogMonitor");
        thread.start();
    }


    private PrintWriter createFilePrintWriter() throws IOException
    {
        final boolean append = getSettings().isLogFileAppend();
        Path outputFile = determineOutputFilePath();
        Files.createDirectories(outputFile.getParent());
        return new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile.toFile(), append), Charset.forName("UTF-8")), /*AutoFlush*/ true);
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
    public void close() throws Exception
    {
        destroy();
    }


    public void setClearConsoleRunnable(Runnable clearConsoleRunnable)
    {
        this.clearConsoleRunnable = clearConsoleRunnable;
    }


    @Override
    public void destroy()
    {
        enabled = false;
        try
        {
            if (monitorRunnable != null)
            {
                while (monitorRunnable.isRunning)
                {
                    try {TimeUnit.MILLISECONDS.sleep(100);} catch (InterruptedException ignore) {}
                }
            }
        }
        catch (Exception ignore) {}
        try { if (in != null) {in.close();} } catch (Exception ignore) {}
        try { if (consoleWriter != null) {consoleWriter.close();} } catch (Exception ignore) {}
        try { if (fileWriter != null) {fileWriter.close();} } catch (Exception ignore) {}
    }

    private class MonitorRunnable implements Runnable
    {
        protected boolean isRunning = true;

        protected final int port = getSettings().getRioLogPort();

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

//                        if (getSettings().isClearOnRobotRestart() && getSettings().getRioRestartRegex().matcher(received).find())
                        if (getSettings().isClearOnRobotRestart() && received.startsWith("\u2794"))
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
                        LOG.error("FRC: An IOException occurred while monitoring the RIO Log UDP output", e);
                        isRunning = false;
                        cleanUpSocket(socket);
                        return;
                    }
                }
            }
            catch (Exception e)
            {
                LOG.error("FRC: An Exception occurred while monitoring the RIO Log UDP output", e);
                isRunning = false;
            }
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
        protected String getStartingMonitoringMessage() {return "«««Monitoring RioLog on port " + port + "»»»";}


        protected DatagramSocket createSocket() throws IOException
        {
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
    }



    private class MultiCastTestingMonitor extends MonitorRunnable
    {
        private final InetAddress groupAddress;
        private final int multicastPort = 4446;


        public MultiCastTestingMonitor()
        {
            try
            {
                groupAddress = InetAddress.getByName("230.0.0.1");
            }
            catch (UnknownHostException e)
            {
                throw new IllegalStateException("Cannot create group InetAddress due to an exception.", e);
            }
        }


        @Override
        protected DatagramSocket createSocket() throws IOException
        {
            MulticastSocket socket = new MulticastSocket(multicastPort);
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
        protected String getStartingMonitoringMessage() { return "«««Monitoring *SIMULATED* RioLog on port " + multicastPort + "»»»"; }


        @Override
        protected boolean addLineBreak()
        {
            return true;
        }
    }
}
