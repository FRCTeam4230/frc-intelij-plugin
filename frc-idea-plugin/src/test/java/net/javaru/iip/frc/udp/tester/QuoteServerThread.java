/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.javaru.iip.frc.udp.tester;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Charsets.UTF_8;



//Modification of http://docs.oracle.com/javase/tutorial/networking/datagrams/examples/QuoteServerThread.java
@SuppressWarnings({"UseOfSystemOutOrSystemErr", "CallToPrintStackTrace"})
public class QuoteServerThread extends Thread
{
    protected DatagramSocket socket = null;
//    protected BufferedReader in = null;

    public static final int PORT = 4445; //arbitrarily chosen
    private final ImmutableList<String> quotes;

    private int currentIndex = 0;


    public QuoteServerThread() throws Exception
    {
        this("QuoteServerThread");
    }


    public QuoteServerThread(String name) throws Exception
    {
        super(name);


        socket = new DatagramSocket(PORT);
//        try
        {
            final URL resource = QuoteServerThread.class.getClassLoader().getResource("testMulticastServer/one-liners.txt");
            if (resource == null)
            {
                throw new IllegalStateException("Could not find 'testMulticastServer/one-liners.txt' resource file on the classpath.");
            }
            @SuppressWarnings("ConstantConditions")
            final Path inputFile = Paths.get(resource.toURI());
            quotes = ImmutableList.copyOf(Files.readAllLines(inputFile, UTF_8));
//            in = new BufferedReader(new FileReader(inputFile.toFile()));
        }
//        catch (Exception e)
//        {
//            logger.warn("Could not open quote file. Cause: '{}' Serving time instead.", e.toString());
//        }
    }


    @Override
    public void run()
    {
        while (!socket.isClosed())
        {
            try
            {
                byte[] buf = new byte[256];

                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                // figure out response
//                final String dString = (in == null) ? new Date().toString() : getNextQuote();
                final String dString = getNextQuote();
                buf = dString.getBytes(UTF_8);

                // send the response to the client at "address" and "port"
                InetAddress address = packet.getAddress();
                int port = packet.getPort();
                packet = new DatagramPacket(buf, buf.length, address, port);
                socket.send(packet);
            }
            catch (IOException e)
            {
                e.printStackTrace();
                throw new RuntimeException("We crashed and burned.", e);
            }
        }
        socket.close();
    }


    protected String getNextQuote()
    {
        if (quotes == null || quotes.isEmpty())
        {
            return new Date().toString();
        }

        final String quote = quotes.get(currentIndex);
        if (++currentIndex == quotes.size())
        {
            currentIndex = 0;
            System.out.println("Cycling back to start of quote list");
        }
        return quote;
    }



}
