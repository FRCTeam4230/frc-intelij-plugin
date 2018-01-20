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

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;



public abstract class AbstractProcessStream implements ProcessStream,
                                                       Closeable
{
    private static final Logger LOG = Logger.getInstance(AbstractProcessStream.class);

    public enum State
    {
        FIRST_RUN, STOPPED, SUSPENDED, RUNNING
    }

    private State state;

    private PipedInputStream inputStream;


    private OutputStream outputStream;
    private BufferedReader reader;

    private ExecutorService executorService;
    private StoppableRunnable queueProcessor;
    private Future<?> queueProcessorFuture;

    private InputProcessor inputProcessor;
    private Future<?> lineReadingFuture;

    private final BlockingQueue<String> streamQueue = new ArrayBlockingQueue<>(2_048);


    protected AbstractProcessStream()
    {
        init();
    }


    @Override
    public void start()
    {
        if (state != State.RUNNING)
        {
            if (outputStream == null)
            {
                init();
            }
            executorService = Executors.newFixedThreadPool(2);

            queueProcessor = getQueueProcessor();
            queueProcessorFuture = executorService.submit(queueProcessor);

            inputProcessor = new InputProcessor();
            lineReadingFuture = executorService.submit(inputProcessor);
            state = State.RUNNING;
        }
    }


    @NotNull
    protected StoppableRunnable getQueueProcessor()
    {
        return new QueueProcessor();
    }


    private void init()
    {
        try
        {
            inputStream = new PipedInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            outputStream = new WrappedOutputStream(inputStream);
        }
        catch (IOException e)
        {
            LOG.warn("[FRC] An exception occurred when creating the streams. Cause Summary: " + e.toString(), e);
        }
    }


    private class WrappedOutputStream extends FilterOutputStream
    {
        public WrappedOutputStream(PipedInputStream inputStream) throws IOException
        {
            super(new PipedOutputStream(inputStream));
        }


        @Override
        public void close() throws IOException
        {
            state = State.SUSPENDED;
            try
            {
                out.flush();
                super.close();
            }
            finally
            {
                IOUtils.closeQuietly(inputStream);
                outputStream = null;

                // While I believe we have fully prevented it, in previous versions there was
                // a deadlock when trying to close the reader. So we are doing it in a separate
                // thread with a timeout just in case.
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<Void> future = executor.submit(() ->
                                                      {
                                                          LOG.debug("[FRC] Closing reader");
                                                          IOUtils.closeQuietly(reader);
                                                          LOG.debug("[FRC] Reader has been closed");
                                                          return null;
                                                      });

                try
                {
                    future.get(2, TimeUnit.SECONDS);
                    LOG.debug("[FRC] Reader closed cleanly");
                }

                catch (TimeoutException e)
                {
                    LOG.debug("[FRC] Reader closing attempt timed out");
                    future.cancel(true);
                }
                catch (Exception ignore) { }
            }
        }
    }

    protected interface StoppableRunnable extends Runnable
    {
        void stop();
    }

    protected abstract class AbstractStoppableRunnable implements StoppableRunnable
    {
        protected boolean isRunning = true;


        @Override
        public void stop() { isRunning = false; }
    }

    protected class InputProcessor extends AbstractStoppableRunnable
    {
        @Override
        public void run()
        {
            LOG.debug("[FRC] Starting InputProcessor");
            while (isRunning)
            {
                if (state == State.RUNNING)
                {
                    try
                    {
                        String line = null;
                        if (reader != null && inputStream != null)
                        {
                            try
                            {
                                line = reader.readLine();
                            }
                            catch (InterruptedIOException ignore)
                            {
                                // We may get this as part of the shutdown process. So we are
                                // only going to log is it happens while we are in a RUNNING state.
                                if (state == State.RUNNING)
                                {
                                    LOG.info("[FRC] An unexpected InterruptedIOException occurred.");
                                }
                                else
                                {
                                    LOG.debug("[FRC] >>>>Still got an InterruptedIOException");
                                }
                            }
                        }

                        if (line != null)
                        {
                            streamQueue.offer(line + "\n");
                        }
                        else
                        {
                            LOG.trace("looped");
                        }
                    }
                    catch (Exception e)
                    {
                        LOG.warn("[FRC] An Exception occurred in InputProcessor.run(): " + e.toString(), e);
                    }
                }
                else
                {
                    LOG.trace("[FRC] State is not RUNNING, but " + state);
                }
            }
        }
    }


    protected class QueueProcessor extends AbstractStoppableRunnable
    {
        @Override
        public void run()
        {
            LOG.debug("[FRC] Starting QueueProcessor");
            while (isRunning)
            {
                try
                {
                    final String value = streamQueue.poll(1, TimeUnit.SECONDS);
                    if (value != null)
                    {
                        processLine(value);
                    }
                }
                catch (InterruptedException e)
                {
                    LOG.debug("[FRC] InterruptedException in QueueProcessor.run(). Setting isRunning to 'false'.");
                    isRunning = false;
                }
            }
        }
    }


    protected abstract void processLine(String line);


    @Override
    public OutputStream getOutputStream()
    {
        if (state == State.SUSPENDED)
        {
            init();
            state = State.RUNNING;
        }
        return outputStream;
    }


    public void stop()
    {
        state = State.STOPPED;
        IOUtils.closeQuietly(outputStream);
        stopRunnable(inputProcessor, lineReadingFuture);
        stopRunnable(queueProcessor, queueProcessorFuture);

        if (executorService != null)
        {
            executorService.shutdownNow();
        }
    }


    @Override
    public void close() throws IOException
    {
        stop();
    }


    public void stopRunnable(StoppableRunnable runnable, Future<?> future)
    {
        if (runnable != null)
        {
            runnable.stop();
            if (future != null)
            {
                int count = 0;
                while (!future.isDone() && count++ < 20)
                {
                    try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException ignore) {}
                }

                if (!future.isDone())
                {
                    try
                    {
                        future.cancel(true);
                    }
                    catch (Exception ignore) {}
                }
            }
        }
    }

}
