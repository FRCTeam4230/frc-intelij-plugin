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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Charsets;
import com.intellij.openapi.diagnostic.Logger;



public class RioLogMonitor extends Process implements AutoCloseable
{
    private static final Logger LOG = Logger.getInstance(RioLogMonitor.class);

    public final static int MAX_PACKET_SIZE = 65507;
    public static final int DEFAULT_RIO_LOG_PORT = 6666;

    private boolean enabled = true;

    private final int bufferSize; // in bytes
    private final int port;

    private PipedInputStream in;
    private PrintWriter writer;
    private Monitor monitor;


    public RioLogMonitor() throws IllegalStateException
    {
        //TODO: Ultimately  these values need to come from the settings (and should be validated there)
        this(MAX_PACKET_SIZE, DEFAULT_RIO_LOG_PORT);

    }

    private RioLogMonitor(int bufferSize, int port)
    {
        this.bufferSize = (bufferSize <=0 || bufferSize > MAX_PACKET_SIZE) ? MAX_PACKET_SIZE : bufferSize;
        if (port < 1 || port > 65_535)
        {
            throw new IllegalArgumentException("Specified port is no within the valid range");
        }

        this.port = port;
        restart();
    }

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
            writer = new PrintWriter(new OutputStreamWriter(new PipedOutputStream(in), Charsets.UTF_8));
        }
        catch (IOException e)
        {
            throw new IllegalStateException("Could not create necessary io streams.", e);
        }

        enabled  = true;
        //TODO: Change to new Monitor() once initial development is completed
        monitor = new MultiCastMonitor();
        Thread thread = new Thread(monitor);
        thread.setName("RioLogMonitor");
        thread.start();
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


    @Override
    public void destroy()
    {
        enabled = false;
        try
        {
            if (monitor != null)
            {
                while (monitor.isRunning)
                {
                    try {TimeUnit.MILLISECONDS.sleep(100);} catch (InterruptedException ignore) {}
                }
            }
        }
        catch (Exception ignore) {}
        try { if (in != null) {in.close();} } catch (Exception ignore) {}
        try { if (writer != null) {writer.close();} } catch (Exception ignore) {}
    }

    private class Monitor implements Runnable
    {
        protected boolean isRunning = true;

        @Override
        public void run()
        {
            //TODO: remove this once basic development work has completed
            writer.println("Monitoring RioLog on port " + port + "...");
            writer.flush();

            isRunning = true;
            byte[] buffer = new byte[bufferSize];
            try (DatagramSocket socket = new DatagramSocket(port))
            {
                socket.setReuseAddress(true);
                socket.setBroadcast(true);

                socket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(1)); // check every x seconds for shutdown
                while (enabled)
                {
                    DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
                    try
                    {
                        socket.receive(incomingPacket);
                        final String received = new String(incomingPacket.getData(), 0, incomingPacket.getLength());
                        writer.println(received);
                        writer.flush();
                    }
                    catch (SocketTimeoutException ignore)
                    {
                        if (!enabled)
                        {
                            isRunning = false;
                            writer.flush();
                            return;
                        }
                    }
                    catch (IOException e)
                    {
                        LOG.error("FRC: An IOException occurred while monitoring the RIO Log UDP output", e);
                        isRunning = false;
                        return;
                    }
                }
            }
            catch (SocketException e)
            {
                LOG.error("FRC: An SocketException occurred while monitoring the RIO Log UDP output", e);
                isRunning = false;
            }
            isRunning = false;
        }
    }



    private class MultiCastMonitor extends Monitor
    {
        @Override
        public void run()
        {

            writer.println("Monitoring RioLog on port " + port + "...");
            writer.flush();

            isRunning = true;
            byte[] buffer = new byte[bufferSize];
            try (MulticastSocket socket = new MulticastSocket(4446))
            {
                InetAddress address = InetAddress.getByName("230.0.0.1");
                socket.joinGroup(address);

                socket.setSoTimeout((int) TimeUnit.SECONDS.toMillis(1)); // check every x seconds for shutdown

                DatagramPacket incomingPacket;

                while (enabled)
                {
                    incomingPacket = new DatagramPacket(buffer, buffer.length);
                    try
                    {
                        socket.receive(incomingPacket);
                        final String received = new String(incomingPacket.getData(), 0, incomingPacket.getLength());
                        writer.println(received);
                        writer.flush();
                    }
                    catch (SocketTimeoutException ignore)
                    {
                        if (!enabled)
                        {
                            isRunning = false;
                            socket.leaveGroup(address);
                            writer.flush();
                            return;
                        }
                    }
                    catch (IOException e)
                    {
                        LOG.error("FRC: An IOException occurred while monitoring the RIO Log UDP output", e);
                        isRunning = false;
                        socket.leaveGroup(address);
                        return;
                    }
                }
            }
            catch (Exception e)
            {
                LOG.error("FRC: An Exception occurred while monitoring the RIO Log Mulitcast UDP output", e);
                isRunning = false;
            }
            isRunning = false;
        }


    }
}
