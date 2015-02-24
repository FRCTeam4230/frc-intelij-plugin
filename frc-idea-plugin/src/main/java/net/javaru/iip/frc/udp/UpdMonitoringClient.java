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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;



public class UpdMonitoringClient
{
    public final static int MAX_PACKET_SIZE = 65507;
    public static final int DEFAULT_RIO_LOG_PORT = 6666;
    private volatile boolean isShutDown = false;
    private final int bufferSize; // in bytes
    private final int port;


    public UpdMonitoringClient()
    {
        this.port = DEFAULT_RIO_LOG_PORT;
        this.bufferSize = MAX_PACKET_SIZE;
    }


    public void execute()
    {
        byte[] buffer = new byte[bufferSize];
        try (DatagramSocket socket = new DatagramSocket(port))
        {
            socket.setSoTimeout(10000); // check every x seconds for shutdown
            while (!isShutDown)
            {
                DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
                try
                {
                    socket.receive(incomingPacket);
                    final String received = new String(incomingPacket.getData(), 0, incomingPacket.getLength());
                    //TODO - send received message to console/toolWindow
                }
                catch (SocketTimeoutException ignore)
                {
                    if (isShutDown) {return;}
                }
                catch (IOException ex)
                {
                    // TODO: LOG AND/OR HANDLE
                }
            } // end while
        }
        catch (SocketException ex)
        {
            // TODO: LOG AND/OR HANDLE
            //logger.log(Level.SEVERE, "Could not bind to port: " + port, ex);
        }
    }
}
