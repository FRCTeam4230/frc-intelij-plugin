/*
 * Copyright 2015-2019 the original author or authors
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

package net.javaru.iip.frc.riolog.tcp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.riolog.AbstractRioLogMonitorProcess;
import net.javaru.iip.frc.riolog.tcp.message.ErrorMessage;
import net.javaru.iip.frc.riolog.tcp.message.InfoMessage;
import net.javaru.iip.frc.riolog.tcp.message.Message;
import net.javaru.iip.frc.riolog.tcp.message.MessageToStringRenderer;
import net.javaru.iip.frc.settings.FrcProjectTeamNumberService;



public class TcpRioLogMonitorProcess extends AbstractRioLogMonitorProcess
{
    private static final Logger LOG = Logger.getInstance(TcpRioLogMonitorProcess.class);

    private static final byte[] emptyFrame = new byte[] {0, 0};
    
    private final BlockingQueue<String> riologQueue = new ArrayBlockingQueue<>(2_048);

    
    
    /**
     *
     * @param project the project the monitoring process is running for
     * @param clearConsoleRunnable Runnable that programmatically 'clicks' the clear button on the Executor window.
     * @param stopRioLogRunnable   Runnable that programmatically 'clicks' the stop button on the Executor window.
     *
     * @throws IllegalStateException If an initialization issue occurs
     */
    public TcpRioLogMonitorProcess(@NotNull  Project project, @NotNull Runnable clearConsoleRunnable, @NotNull Runnable stopRioLogRunnable) throws IllegalStateException
    {
        super(project, clearConsoleRunnable, stopRioLogRunnable);
    }


    @NotNull
    @Override
    protected RioLogMonitoringRunnable initMonitoringRunnable()
    {
        final RioLogMonitoringRunnable rioLogMonitor = new TcpRioLogMonitoringRunnable();
        final String message = String.format("[FRC] Using '%s' for monitoring.", rioLogMonitor.getClass().getSimpleName());
        LOG.info(message);
        return rioLogMonitor;
    }

    private class TcpRioLogMonitoringRunnable extends AbstractRioLogMonitoringRunnable
    {
        private Thread listenerThread;

        protected TcpRioLogMonitoringRunnable()
        {
            super();
        }

        @Override
        public void run()
        {
            boolean teamNumberIsSet = FrcProjectTeamNumberService.getInstance(project).getTeamNumber() > 0 || getSettings().isTeamNumberConfigured();
            if (!teamNumberIsSet)
            {
                consoleWriter.println();
                consoleWriter.println("==============================================================================================================");
                consoleWriter.println("==  YOUR TEAM NUMBER IS NOT CONFIGURED.                                                                     ==");
                consoleWriter.println("==  To use RIOLog monitoring, please configure your Team Number in Settings > Languages & Frameworks > FRC  ==");
                consoleWriter.println("==============================================================================================================");
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
                
                while (listenerThread.isAlive() && !listenerThread.isInterrupted()) 
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
                        // TODO: As noted in AbstractRioLogMonitoringRunnable, we want to get rid of these flags in favor of thread interruption monitoring
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
        protected boolean addLineBreak()
        {
            return true;
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

        
        @Override
        @NotNull
        protected String getStartingMonitoringMessage() {return "Connecting to roboRIO...";}

    }


    
    
    class TcpRioLogListener implements Runnable
    {
        /* 
            "Based on" (well mostly directly ported over from) the RioConsole class
            https://github.com/wpilibsuite/riolog/blob/master/src/main/java/netconsole2/RioConsole.java
            https://github.com/wpilibsuite/EclipsePlugins/blob/master/edu.wpi.first.wpilib.plugins.riolog/src/netconsole2/RioConsole.java
        */
        
        private Socket socket;
        private Thread sender;
        private boolean autoReconnect = true;
        private final AtomicBoolean cleanup = new AtomicBoolean(false);
        private final AtomicBoolean reconnect = new AtomicBoolean(false);
        // TODO add capability (i.e. toolbar button) to set verbosity level and thus the showWarning and showInfo booleans
        private final AtomicBoolean showWarning = new AtomicBoolean(true); 
        private final AtomicBoolean showInfo = new AtomicBoolean(true);
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

                while (!Thread.currentThread().isInterrupted() && !cleanup.get())
                {
                    if (in == null || reconnect.getAndSet(false))
                    {
                        if (LOG.isTraceEnabled()) { LOG.trace("[FRC] in was null or reconnect was true."); }
                        
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
                            LOG.debug("[FRC] Received InterruptedException from wakeupListener.await() in TcpRioLogListener.run(). Breaking out of run loop ");
                            Thread.currentThread().interrupt();
                            break;
                        }
                        finally
                        {
                            lock.unlock();
                        }
                        
                        LOG.trace("[FRC] starting TCP reconnect");
                        in = connect();
                        if (in == null)
                        {
                            LOG.trace("[FRC] Did not reconnect.");
                            continue;
                        }
                    }

                    if (cleanup.get())
                    {
                        break;
                    }

                    @SuppressWarnings("UnusedAssignment")
                    int tag = -1;
                    try
                    {
                        tag = readSegment(data, in);
                    }
                    catch (IOException e)
                    {
                        LOG.warn("[FRC] RIOLog TCP socket disconnected (or other IOException) during read: " + e.toString());
                        lock.lock();
                        Consumer<Boolean> connCb;
                        try
                        {
                            connCb = connectedCallback;
                        }
                        finally
                        {
                            lock.unlock(); 
                        }
                        
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
                LOG.debug("[FRC] exiting TcpRioLogListener.run() loop");
                // closeSocket() called within stop() which is called in finally block
            }
            finally
            {
                stop();
            }
        }

        
        /**
         * Read a tagged segment into a byte buffer.
         * segmentData must have a capacity of at least 65535.
         * Returns tag, or -1 on error.
         */
        private int readSegment(ByteBuffer segmentData, DataInputStream inputStream) throws IOException
        {
            LOG.trace("[FRC] entering readSegment");
            // read 2-byte length.  Ignore zero length frames
            int len;
            do
            {
                // TODO: We can remove these wrapping logging calls once the read issue is resolved
                LOG.trace("[FRC] reading from the inputStream");
                len = inputStream.readUnsignedShort();
                LOG.trace("[FRC] inputStream read has completed len = " + len);
            } while (len == 0);

            // read 1-byte tag
            int tag = inputStream.readUnsignedByte();
            LOG.trace("[FRC] got segment len=" + len + " tag=" + tag);

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
            LOG.trace("[FRC] finished reading segment. returning with tag=" + tag);
            return tag;
        }
        
        private void handleSegment(int tag, ByteBuffer data)
        {
            // The data sent is not direct Strings to display as is, but "Messages" that need to be (for the lat of a better term) 
            // parsed. Then those Messages need to be rendered to get the actual content to display on the console.
            try
            {
                if (LOG.isTraceEnabled())
                { LOG.trace("[FRC] handleSegment called: tag=" + tag); }

                Message message;
                if (tag == 11)
                {  // Error or warning
                    message = new ErrorMessage(data);
                    if (message.getType() == Message.Type.kWarning && !showWarning.get())
                    {
                        return;
                    }
                }
                else if (tag == 12 && showInfo.get())
                {  // Console
                    message = new InfoMessage(data);
                }
                else
                {
                    LOG.trace("Ignoring tag " + tag);
                    return;  // Ignore other tags
                }


                try
                {
                    // The data sent is not direct Strings to display as is, but "Messages" that need to be (for the lat of a better term) 
                    // parsed. Then those Messages need to be rendered to get the actual content to display on the console.
                    // TODO: let's use the renderToString(Message, Message.RenderOptions()) overload to allow us to pass in custom renderOptions in order to turn on and off the timestamp, errorCode, errorLocation, errorCallstack, warningLocation, warningCallstack
                    final String messageContent = MessageToStringRenderer.renderToString(message);
                    riologQueue.put(messageContent);
                }
                catch (InterruptedException e)
                {
                    LOG.debug("[FRC] InterruptedException during riologQueue.put(). Interrupting listener thread");
                    Thread.currentThread().interrupt();
                }
            }
            catch (Exception e)
            {
                LOG.warn("[FRC] An exception occurred when handling segment. Cause Summary: " + e.toString(), e);
                Thread.currentThread().interrupt();
            }


//            // tag == 11    Error or warning
//            // tag == 12    Standard message
//            // other tags are ignored
//            if (tag == 11 || tag == 12)
//            {
//                try
//                {
//                    final String value = StandardCharsets.UTF_8.decode(data).toString();
//                    if (LOG.isTraceEnabled()) { LOG.trace("[FRC] Queueing Console data: " + value); }
//                    riologQueue.put(value);
//                }
//                catch (InterruptedException e)
//                {
//                    LOG.debug("[FRC] InterruptedException during riologQueue.put(). Interrupting listener thread");
//                    Thread.currentThread().interrupt();
//                }
//                catch (Exception e)
//                {
//                    LOG.warn("[FRC] An exception occurred when handling segment. Cause Summary: " + e.toString(), e);
//                    Thread.currentThread().interrupt();
//                }
//            }
//            else
//            {
//                LOG.debug("[FRC] ignoring tag of '" + tag);
//            }
            
        }
        
        private void setSocket(Socket socket)
        {
            lock.lock();
            try
            {
                this.socket = socket;
            }
            finally
            {
                lock.unlock();
            }
        }

        private DataInputStream connect()
        {
            LOG.debug("[FRC] Connecting to TCP RioLog Monitoring");
            Socket mySocket;
            try
            {
                mySocket = TcpRioSocketConnectorProjectService.getInstance(project).connect();
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
            Consumer<Boolean> connCb;
            try
            {
                connCb = connectedCallback;
            }
            finally
            {
                lock.unlock(); 
            }
            
            if (connCb != null)
            {
                connCb.accept(Boolean.TRUE);
            }
            try
            {
                String connectionInfo = "";
                try
                {
                    String host = mySocket.getInetAddress().toString();
                    if (host.startsWith("/")) { host = StringUtils.removeStartIgnoreCase(host, "/"); }
                    connectionInfo = " via " + host + " on port " + mySocket.getPort();
                }
                catch (Exception ignore) {}
                    
                riologQueue.put(">>>Connected to roboRIO" + connectionInfo + "<<<");
                riologQueue.put("");
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


        @Override
        protected void finalize()
        {
            stop();
        }
        
        public void stop()
        {
            cleanup.set(true);
            TcpRioSocketConnectorProjectService.getInstance(project).stopConnectionAttempts();
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
            Socket s;
            try
            {
                s = socket;
                socket = null;
            }
            finally
            {
                lock.unlock();
            }
            
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
