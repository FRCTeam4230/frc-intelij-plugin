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

package net.javaru.iip.frc.riolog.tcp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.riolog.AbstractRioLogMonitorProcess;



public class TcpRioLogMonitorProcess extends AbstractRioLogMonitorProcess
{
    private static final Logger LOG = Logger.getInstance(TcpRioLogMonitorProcess.class);

    private final BlockingQueue<String> riologQueue = new ArrayBlockingQueue<>(2_048);

    /**
     * @param clearConsoleRunnable Runnable that programmatically 'clicks' the clear button on the Executor window.
     * @param stopRioLogRunnable   Runnable that programmatically 'clicks' the stop button on the Executor window.
     *
     * @throws IllegalStateException If an initialization issue occurs
     */
    public TcpRioLogMonitorProcess(@NotNull Runnable clearConsoleRunnable, @NotNull Runnable stopRioLogRunnable) throws IllegalStateException
    {
        super(clearConsoleRunnable, stopRioLogRunnable);
    }


    @NotNull
    @Override
    protected RioLogMonitoringRunnable initMonitoringRunnable()

    {
        RioLogMonitoringRunnable rioLogMonitor;
//        if (USE_DEBUGGING_SERVER)
//        {
//            rioLogMonitor = new TestingTcpRioLogMonitor();
//            LOG.warn(String.format("[FRC] System Property '%s is set to 'true'. Using '%s' for monitoring on port '%d'.",
//                                   SIMULATED_LOG_SERVICE_PROP_KEY_BASE,
//                                   rioLogMonitor.getClass().getSimpleName(),
//                                   rioLogMonitor.getPort()));
//        }
//        else
        {
            rioLogMonitor = new TcpRioLogMonitoringRunnable();
            final String message = rioLogMonitor.getPort() != null 
                                   ? String.format("[FRC] Using '%s' for monitoring on port '%d'.",
                                                rioLogMonitor.getClass().getSimpleName(),
                                                rioLogMonitor.getPort()) 
                                   : String.format("[FRC] Using '%s' for monitoring.",
                                                   rioLogMonitor.getClass().getSimpleName());
            LOG.info(message);
        }
        return rioLogMonitor;
    }

    private class TcpRioLogMonitoringRunnable extends AbstractRioLogMonitoringRunnable
    {


        private Thread listenerThread;

        protected TcpRioLogMonitoringRunnable()
        {
            super(null);
        }
        


        @Override
        public void run()
        {

            if (!getSettings().isTeamNumberConfigured())
            {
                consoleWriter.println();
                consoleWriter.println("==========================================================================================================");
                consoleWriter.println("==  YOUR TEAM NUMBER IS NOT CONFIGURED.                                                                 ==");
                consoleWriter.println("==  To RIOLog monitoring, please configure your Team Number in Settings > Languages & Frameworks > FRC  ==");
                consoleWriter.println("==========================================================================================================");
                isRunning = false;
                enabled = false;
            }
            else
            {
                logStartingMonitoring();
                isRunning = true;

                final TcpRioLogListener rioLogListener = new TcpRioLogListener();
                
                
                listenerThread = new Thread(rioLogListener);
                listenerThread.setDaemon(true);
                listenerThread.setName("TcpRioLogListener");
                listenerThread.start();
                
                while (listenerThread.isAlive()) 
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
                        LOG.debug("[FRC] InterruptedException in TcpRioLogMonitoringRunnable.run(). Calling 'stop().");
                        // TODO: We can look at deprecating these and removing by migrating everything over to thread interrupts 
                        isRunning = false;
                        enabled = false;
                        try
                        {
                            if (!listenerThread.isInterrupted()) { listenerThread.interrupt(); }
                        }
                        catch (Exception ignore) {}
                        
                        stop();
                    }
                }
            }
        }


        @Override
        public void stop()
        {
            LOG.debug("[FRC] stopping TCP listener");
            super.stop();
            if (listenerThread != null && listenerThread.isAlive() && !listenerThread.isInterrupted())
            {
                listenerThread.interrupt();
            }
        }


        //TODO - should this be removed?
        @Override
        @NotNull
        protected String getStartingMonitoringMessage() {return "Connecting to roboRIO...";}

    }


    private static byte[] emptyFrame = new byte[] {0, 0};
    // TODO - an we come up with a better name
    class TcpRioLogListener implements Runnable
    {
        private Socket socket;
        private Thread sender;
        private boolean autoReconnect = true;
        private final AtomicBoolean cleanup = new AtomicBoolean(false);
        private final AtomicBoolean reconnect = new AtomicBoolean(false);
        private final AtomicBoolean discard = new AtomicBoolean(false);
        private final AtomicBoolean paused = new AtomicBoolean(false);
        private final AtomicBoolean showWarning = new AtomicBoolean(true);
        private final AtomicBoolean showPrint = new AtomicBoolean(true);
        private Consumer<Boolean> connectedCallback = null;
        private final Lock lock = new ReentrantLock();
        private final Condition wakeupListener = lock.newCondition();        
        
        @Override
        public void run()
        {
            try
            {
                ByteBuffer data = ByteBuffer.allocate(65536);
                DataInputStream in = null;

                while (!Thread.currentThread().isInterrupted() && !cleanup.get()) // TODO determine what cleanup is used for // FIX BEFORE COMMIT 
                {
                    if (in == null || reconnect.getAndSet(false))
                    {
                        if (LOG.isTraceEnabled())
                        { LOG.trace("[FRC] in was null or reconnect was true."); }
                        
                        lock.lock();
                        try
                        {
                            while (!autoReconnect)
                            {
                                wakeupListener.await();
                            }
                        }
                        catch (InterruptedException e)
                        {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        finally
                        {
                            lock.unlock();
                        }
                        //logger.log("starting reconnect");
                        in = null;
                        
                        in = connect();
                        if (in == null)
                        {
                            continue;
                        }
                    }

                    // 
                    if (cleanup.get())
                    {
                        break;
                    }

                    int tag = -1;
                    try
                    {
                        tag = readSegment(data, in);
                    }
                    catch (IOException e)
                    {
                        LOG.warn("[FRC] RIOLog TCP socket disconnected during read: " + e.toString());
                        lock.lock();
                        Consumer<Boolean> connCb = connectedCallback;
                        lock.unlock();
                        if (connCb != null)
                        {
                            connCb.accept(Boolean.FALSE);
                        }
                        setSocket(null);
                        in = null;
                        continue;
                    }

                    handleSegment(tag, data);
                }
                LOG.debug("[FRC] exiting TcpRioLogListener.run()");
            }
            finally
            {
                closeSocket();
            }
        }

//
//        public String getConnectionInfo()
//        {
//            return  socket != null ? socket.getInetAddress().toString() : "<no info>";
//        }
        
        /**
         * Read a tagged segment into a byte buffer.
         * segmentData must have a capacity of at least 65535.
         * Returns tag, or -1 on error.
         */
        private /*static*/ int readSegment(ByteBuffer segmentData, DataInputStream inputStream) throws IOException
        {
            // read 2-byte length.  Ignore zero length frames
            int len;
            do
            {
                len = inputStream.readUnsignedShort();
            } while (len == 0);

            // read 1-byte tag
            int tag = inputStream.readUnsignedByte();
            //logger.log("got segment len=" + len + " tag=" + tag);

            // subtract 1 for tag
            len -= 1;

            segmentData.clear();
            segmentData.limit(len);
            byte[] data = segmentData.array();

            int bytesRead = 0;
            while (bytesRead < len)
            {
                int nRead = inputStream.read(data, bytesRead, len - bytesRead);
                if (nRead < 0)
                {
                    return -1;
                }
                bytesRead += nRead;
            }
            //logger.log("finished reading segment");
            return tag;
        }
        
        private void handleSegment(int tag, ByteBuffer data)
        {
            if (LOG.isTraceEnabled())
            { LOG.trace("[FRC] handleSegment called: tag=" + tag + "  data:" + StandardCharsets.UTF_8.decode(data).toString()); }
            
            if (discard.get())
            {
                return;
            }

            // TODO: Enhance to use the tag to stylize the data based on it
            // tag == 11    Error or warning
            // tag == 12    Standard message
            // other tags are ignored.... we may need to implement this
            if (tag == 11 || tag == 12)
            {
                try
                {
                    riologQueue.put(StandardCharsets.UTF_8.decode(data).toString());
                }
                catch (InterruptedException e)
                {
                    Thread.currentThread().interrupt();
                }
                catch (Exception e)
                {
                    LOG.warn("[FRC] An exception occurred when handling segment. Cause Summary: " + e.toString(), e);
                }
            }
            else
            {
                try
                {
                    LOG.debug("[FRC] ignoring tag of '" + tag + "' with data: " + StandardCharsets.UTF_8.decode(data).toString());
                }
                catch (Exception ignore)
                {
                    LOG.debug("[FRC] ignoring tag of '" + tag + "'");
                }
            }
            
        }
        
        private void setSocket(Socket socket)
        {
            lock.lock();
            this.socket = socket;
            lock.unlock();
        }

        private DataInputStream connect()
        {
            LOG.debug("[FRC] Connecting to TCp RioLog Monitoring");
            Socket mySocket;
            try
            {
                mySocket = TcpRioSocketConnectorApplicationService.getInstance().connect();
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
                return null;
            }

            if (mySocket == null)
            {
                return null;
            }
            setSocket(mySocket);
            DataInputStream in;
            OutputStream out;
            try
            {
                mySocket.setTcpNoDelay(true);  // for keep alives
                in = new DataInputStream(mySocket.getInputStream());
                out = mySocket.getOutputStream();
            }
            catch (IOException e)
            {
                closeSocket();
                return null;
            }
            lock.lock();
            Consumer<Boolean> connCb = connectedCallback;
            lock.unlock();
            if (connCb != null)
            {
                connCb.accept(Boolean.TRUE);
            }
            try
            {
                riologQueue.put(">>>Connected to roboRIO<<<");
            }
            catch (InterruptedException ignore) {}
            LOG.info("[FRC] RIOLog TCP socket connected");

            // kick off keep alive thread
            if (sender == null)
            {
                sender = new Thread(() -> {
                    while (!Thread.interrupted() && !cleanup.get())
                    {
                        try
                        {
                            TimeUnit.SECONDS.sleep(2);
                            out.write(emptyFrame);
                            out.flush();
                        }
                        catch (InterruptedException e)
                        {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        catch (IOException e)
                        {
                            LOG.debug("[FRC] failed to send keep alive, reconnecting");
                            reconnect();
                            break;
                        }
                    }
                }, "RioConsoleSender");
                sender.setDaemon(true);
                sender.start();
            }
            return in;
        }


        public void stop()
        {
            // We should be able to remove this method
            cleanup.set(true);
            closeSocket();
            Thread.currentThread().interrupt();
        }


        public void reconnect()
        {
            reconnect.set(true);
            closeSocket();
        }

        public void closeSocket()
        {
            LOG.info("[FRC] Closing RIOLog TCP socket");
            lock.lock();
            Socket s = socket;
            socket = null;
            lock.unlock();
            try
            {
                if (s != null)
                {
                    s.close();
                }
            }
            catch (IOException ignore) {}
        }
    }
}
