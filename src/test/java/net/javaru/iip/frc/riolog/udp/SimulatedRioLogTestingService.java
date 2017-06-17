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
import java.net.SocketException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableList;

import net.javaru.iip.frc.riolog.RioLogGlobals;

import static com.google.common.base.Charsets.UTF_8;
import static net.javaru.iip.frc.riolog.udp.RioLogUdpSocketManagerApplicationService.SIMULATED_LOG_SERVICE_MULTICAST_HOST;
import static net.javaru.iip.frc.riolog.udp.RioLogUdpSocketManagerApplicationService.SystemPropertyKeys.SIMULATED_LOG_SERVICE_PORT_PROP_KEY;
import static net.javaru.iip.frc.riolog.udp.RioLogUdpSocketManagerApplicationService.SystemPropertyKeys.SIMULATED_LOG_SERVICE_PROP_KEY_BASE;


/**
 * <p>
 * A testing utility that will send simulated roboRIO Log messages via UDP (a.k.a. Multicast Sever). To use when testing/debugging the plugin,
 * run this class and then set the system property (i.e. {@code -D} switch) {@value RioLogUdpSocketManagerApplicationService.SystemPropertyKeys#SIMULATED_LOG_SERVICE_ENABLED_PROP_KEY}
 * to "<tt>true</tt>" or "<tt>on</tt>" in the <em>VM Options</em> field of the <strong>Plugin</strong> Run/Debug Configuration. Launch
 * the plugin Run/Debug configuration.
 * </p>
 * <p>
 * The following options can be set via system properties (i.e. {@code -D} switches) in the Run/Debug Configuration of <storng>this</storng>
 * class:
 * <ul>
 * <li>{@value #SIMULATED_RESTART_FREQUENCY_PROP_KEY} : [int] - sets the frequency (i.e. after every <em>n</em><sup>th</sup>
 * message) that a startup message is sent. Default: <tt>10</tt></li>
 * <li>{@value #MESSAGE_TYPE_PROP_KEY} : [{@link MessageType} enum] - sets the message type. Default: {@code DatedNumberedLines}.</li>
 * <li>{@value #PAUSE_DURATION_PROP_KEY} : [long] - sets the duration of the pause between messages. Default: <tt>2</tt></li>
 * <li>{@value #PAUSE_TIMEUNIT_PROP_KEY} : [{@link TimeUnit} enum] - sets the TimeUnit of the pause between messages. Default: <tt>SECONDS</tt></li>
 * <li>
 * {@value RioLogUdpSocketManagerApplicationService.SystemPropertyKeys#SIMULATED_LOG_SERVICE_PORT_PROP_KEY} : [int] - sets the multicast port the service "broadcasts" the simulated log messages
 * on.
 * <em>If setting this value, you must <strong>set it on both Run/Debug Configurations</strong>; that the one for this class and
 * the one for the Plugin. This should need to be set only if the default value conflicts with another service on your system.</em>
 * </li>
 * <li>
 * {@value #SOCKET_PORT_PROP_KEY} : [int] - sets the port used when creating the Socket. This should only have to be changed if the default value
 * conflicts with another service on your system. This port only needs to be set in the Run/Debug configuration of this class (and not on
 * the one for the Plugin). Default: {@value #DEFAULT_SOCKET_PORT}
 * </li>
 * </ul>
 * </p>
 */
//Based on example at http://docs.oracle.com/javase/tutorial/networking/datagrams/examples/QuoteServerThread.java
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToPrintStackTrace", "ClassWithoutLogger"})
public class SimulatedRioLogTestingService extends Thread implements Runnable
{
    //frc.simulated.log.service.message.type
    public static final String MESSAGE_TYPE_PROP_KEY = SIMULATED_LOG_SERVICE_PROP_KEY_BASE + ".message.type";

    //frc.simulated.log.service.simulated.restart.frequency
    public static final String SIMULATED_RESTART_FREQUENCY_PROP_KEY =
       SIMULATED_LOG_SERVICE_PROP_KEY_BASE + ".simulated.restart.frequency";

    //frc.simulated.log.service.simulated.exception.frequency
    public static final String SIMULATED_EXCEPTION_FREQUENCY_PROP_KEY =
       SIMULATED_LOG_SERVICE_PROP_KEY_BASE + ".simulated.exception.frequency";

    public static final String PAUSE_DURATION_PROP_KEY =SIMULATED_LOG_SERVICE_PROP_KEY_BASE + ".pause.duration";

    public static final String PAUSE_TIMEUNIT_PROP_KEY =SIMULATED_LOG_SERVICE_PROP_KEY_BASE + ".pause.timeunit";

    public static final String SOCKET_PORT_PROP_KEY =SIMULATED_LOG_SERVICE_PROP_KEY_BASE + ".socket.port";
    public static final int DEFAULT_SOCKET_PORT = RioLogUdpSocketManagerApplicationService.SIMULATED_LOG_SERVICE_PORT_DEFAULT + 1;


    protected int socketPort = DEFAULT_SOCKET_PORT; //default arbitrarily chosen
    private int multicastPort = RioLogUdpSocketManagerApplicationService.SIMULATED_LOG_SERVICE_PORT_DEFAULT; // i.e. the "Broadcast" Port 

    protected DatagramSocket socket = null;

    protected ImmutableList<String> quotes;
    protected MessageType messageType = MessageType.DatedNumberedLines;

    protected int currentIndex = 0;
    protected int messageCount = 0;
    protected final NumberFormat intFormatter = new DecimalFormat("#,###");
    @SuppressWarnings("SpellCheckingInspection")
    protected static final DateFormat dateFormatter = new SimpleDateFormat("EEEE d MMMM yyyy kk:mm:ss zzzz");
    protected int simulatedRestartFrequency;
    protected int simulatedExceptionFrequency;
    protected long pauseDuration = 2L;
    protected TimeUnit pauseTimeUnit = TimeUnit.SECONDS;


    public enum MessageType
    {
        Quotes,
        Timestamps,
        NumberedLines,
        DatedNumberedLines
        //TODO - add a simulated log messages option
    }


    public static void main(String[] args) throws Exception
    {
        new SimulatedRioLogTestingService().start();
    }


    public SimulatedRioLogTestingService() throws Exception
    {
        this("SimulatedRioLogTestingService");
    }


    public SimulatedRioLogTestingService(String name) throws Exception
    {
        super(name);
        System.out.println();
        System.out.println("===============================================================================================");
        initPorts();
        initPauseTime();
        initMessageType();
        initSimulatedRestartFrequency();
        initSimulatedExceptionFrequency();
        initQuotesList();
        System.out.println("===============================================================================================");

        try
        {
            socket = new DatagramSocket(socketPort);
        }
        catch (SocketException e)
        {
            if (e instanceof BindException && "Address already in use: Cannot bind".equals(e.getMessage()))
            {
                System.err.println();
                System.err.println("ERROR: " + e.toString());
                System.err.println("Is another instance already running???");
                System.err.println("If this issue persists, and another instance of this " + getClass().getSimpleName() + " "
                                   + "is not running, consider setting an alternate port. See the class level JavaDoc for details.");
                System.exit(-1);
            }
        }
        System.out.println();
        System.out.println("««« Broadcasting Simulated roboRIO Log messages on port " + multicastPort + " »»»");
        System.out.println();
    }


    protected void initQuotesList() throws URISyntaxException, IOException
    {
        if (messageType == MessageType.Quotes)
        {
            final URL resource = SimulatedRioLogTestingService.class.getClassLoader().getResource("testMulticastServer/one-liners.txt");
            if (resource == null)
            {
                throw new IllegalStateException("Could not find 'testMulticastServer/one-liners.txt' resource file on the classpath.");
            }
            @SuppressWarnings("ConstantConditions")
            final Path inputFile = Paths.get(resource.toURI());
            quotes = ImmutableList.copyOf(Files.readAllLines(inputFile, UTF_8));
        }
    }


    protected void initPorts()
    {
        try
        {
            multicastPort = determineSimulatedLogPort();
        }
        catch (Exception e)
        {
            final String msg = "Could not set multicast port due to an exception: " + e.toString();
            System.err.println(msg);
            throw new RuntimeException(msg, e);
        }

        try
        {
            socketPort = Integer.valueOf(System.getProperty(SOCKET_PORT_PROP_KEY, Integer.toString(socketPort)));
        }
        catch (Exception e)
        {
            final String msg = "Could not set socket port due to an exception: " + e.toString();
            System.err.println(msg);
            throw new RuntimeException(msg, e);
        }
        System.out.println(">>> INIT: Socket port set to:    " + socketPort);
        System.out.println(">>> INIT: Multicast port set to: "  + multicastPort + " (i.e. the 'broadcast' port)");
        System.out.println(">>> INIT: - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
    }


    static int determineSimulatedLogPort()
    {
        try
        {
            return Integer.valueOf(System.getProperty(SIMULATED_LOG_SERVICE_PORT_PROP_KEY,
                                                      Integer.toString(RioLogUdpSocketManagerApplicationService.SIMULATED_LOG_SERVICE_PORT_DEFAULT)));
        }
        catch (Exception e)
        {
            final String message = "Could not determine the port for the simulated log service due to an exception: " + e.toString();
            System.err.println("[FRC] " + message);
            throw new IllegalArgumentException(message, e);
        }
    }


    protected void initMessageType()
    {
        final String msgTypeProp = System.getProperty(MESSAGE_TYPE_PROP_KEY, this.messageType.name());
        for (MessageType type : MessageType.values())
        {
            if (type.name().toLowerCase().equals(msgTypeProp.toLowerCase()))
            {
                messageType = type;
                break;
            }
        }
        System.out.println(">>> INIT: Message Type set to: " + messageType);
    }


    protected void initSimulatedRestartFrequency()
    {
        try
        {
            this.simulatedRestartFrequency = Integer.valueOf(System.getProperty(SIMULATED_RESTART_FREQUENCY_PROP_KEY, "10"));
        }
        catch (NumberFormatException e)
        {
            this.simulatedRestartFrequency = 20;
            System.err.printf("Could not parse as an integer '%s' system property value of '%s'. Setting to a default value of %d.%n",
                              SIMULATED_RESTART_FREQUENCY_PROP_KEY,
                              System.getProperty(SIMULATED_RESTART_FREQUENCY_PROP_KEY),
                              this.simulatedRestartFrequency);
        }
        System.out.println(">>> INIT: Simulated Restart Frequency set to:   send startup message after every " + ordinal(simulatedRestartFrequency) + " message");
    }


    protected void initSimulatedExceptionFrequency()
    {
        try
        {
            this.simulatedExceptionFrequency = Integer.valueOf(System.getProperty(SIMULATED_EXCEPTION_FREQUENCY_PROP_KEY, "10"));
        }
        catch (NumberFormatException e)
        {
            this.simulatedExceptionFrequency = Integer.MAX_VALUE;
            System.err.printf("Could not parse as an integer '%s' system property value of '%s'. Setting to a default value of %d.%n",
                              SIMULATED_EXCEPTION_FREQUENCY_PROP_KEY,
                              System.getProperty(SIMULATED_EXCEPTION_FREQUENCY_PROP_KEY),
                              this.simulatedExceptionFrequency);
        }
        System.out.println(
            ">>> INIT: Simulated Exception Frequency set to: send exception message after every " + ordinal(simulatedExceptionFrequency) + " message");
    }


    protected void initPauseTime()
    {
        try
        {
            pauseTimeUnit = TimeUnit.valueOf(System.getProperty(PAUSE_TIMEUNIT_PROP_KEY, this.pauseTimeUnit.name()).toUpperCase());
        }
        catch (IllegalArgumentException | NullPointerException e)
        {
            final String msg = String.format("System property '%s' value of '%s' could not be converted to a TimeUnit.",
                                             PAUSE_TIMEUNIT_PROP_KEY, System.getProperty(PAUSE_TIMEUNIT_PROP_KEY));
            System.err.println(msg);
            throw new RuntimeException(msg, e);
        }

        try
        {
            pauseDuration = Long.valueOf(System.getProperty(PAUSE_DURATION_PROP_KEY, Long.toString(pauseDuration)));
        }
        catch (NumberFormatException e)
        {
            final String msg = String.format("System property '%s' value of '%s' could not be converted to a Long.",
                                             PAUSE_DURATION_PROP_KEY, System.getProperty(PAUSE_DURATION_PROP_KEY));
            System.err.println(msg);
            throw new RuntimeException(msg, e);
        }
        System.out.println(">>> INIT: Pause Time set to:   " + pauseDuration + " " + pauseTimeUnit.name().toLowerCase());
    }


    @Override
    public void run()
    {
        while (!socket.isClosed())
        {
            try
            {
                messageCount++;
                if (messageCount % simulatedRestartFrequency == 0)
                {
                    sendMessage(RioLogGlobals.ROBO_RIO_STARTUP_LOG_MSG);
                }
                if (messageCount % simulatedExceptionFrequency == 0)
                {
                    sendMessage(createSimulatedExceptionMessage());
                }
                sendMessage(getNextMessage());
                pause();

            }
            catch (IOException e)
            {
                e.printStackTrace();
                throw new RuntimeException("We crashed and burned.", e);
            }
        }
        socket.close();
    }


    @Override
    public synchronized void start()
    {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try
            {
                System.out.println("JVM Shutdown detected. Closing socket...");
                socket.close();
                System.out.println("Socket closed.");
            }
            catch (Exception e)
            {
                System.out.println("An exception occurred when closing the socket. Details: " + e.toString());
            }

        }));
        super.start();
    }


    protected void pause()
    {
        try
        {
            pauseTimeUnit.sleep(pauseDuration);
            pauseDuration = 2;
        }
        catch (InterruptedException ignore) {}
    }


    protected void sendMessage(String message) throws IOException
    {
        final byte[] buf = message.getBytes(UTF_8);

        // send it
        InetAddress group = InetAddress.getByName(SIMULATED_LOG_SERVICE_MULTICAST_HOST);
        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, multicastPort);
        System.out.println("Sending Message: " + message);
        socket.send(packet);
    }


    protected String getNextMessage()
    {
        switch (messageType)
        {
            case Quotes:
                return getNextQuote();
            case Timestamps:
                return getNextTimeStamp();
            case NumberedLines:
                return getNextNumberedLine();
            case DatedNumberedLines:
            default:
                return getNextTimeStamp() + " : " + getNextNumberedLine();
        }
    }


    protected String getNextQuote()
    {
        final String quote = quotes.get(currentIndex);
        if (++currentIndex == quotes.size())
        {
            currentIndex = 0;
            System.out.println("Cycling back to start of quote list");
        }
        return quote;
    }


    protected String getNextTimeStamp()
    {
        return dateFormatter.format(new Date());
    }


    protected String getNextNumberedLine()
    {
        return "Simulated log message # " + intFormatter.format(messageCount);
    }


    protected static String createSimulatedExceptionMessage()
    {
        return "SIMULATED exception Log Message with stack trace.\n" +
               "com.example.foo.bar.SimulatedException: This is a SIMULATED exception (with simulated StackTrace) for testing purposes.\n" +
               "\tat java.lang.String.isEmpty(String.java:623)\n" +
               "\tat java.lang.String.getChars(String.java:807)\n" +
               "\tat java.util.concurrent.TimeUnit.timedWait(TimeUit.java:346)\n" +
               "\tat Main.main(Main.java:11)";


    }


    protected static String ordinal(int i)
    {
        int mod100 = i % 100;
        int mod10 = i % 10;
        if (mod10 == 1 && mod100 != 11)
        {
            return i + "st";
        }
        else if (mod10 == 2 && mod100 != 12)
        {
            return i + "nd";
        }
        else if (mod10 == 3 && mod100 != 13)
        {
            return i + "rd";
        }
        else
        {
            return i + "th";
        }
    }
}


//class SimulatedException extends RuntimeException
//{
//    private static final long serialVersionUID = 2497082517883523110L;
//
//
//    public SimulatedException()
//    {
//        this("This is a SIMULATED Exception for testing purposes");
//    }
//
//
//    public SimulatedException(String message)
//    {
//        super(message);
//    }
//
//
//    public SimulatedException(String message, Throwable cause)
//    {
//        super(message, cause);
//    }
//}