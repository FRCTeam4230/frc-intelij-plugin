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

package net.javaru.iip.frc.riolog.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.settings.FrcApplicationSettings;
import net.javaru.iip.frc.util.FrcIoExtsKt;
import net.javaru.iip.frc.util.FrcUtilsKt;

import static net.javaru.iip.frc.riolog.udp.RioLogUdpSocketManagerApplicationService.SystemPropertyKeys.SIMULATED_LOG_SERVICE_ENABLED_PROP_KEY;
import static net.javaru.iip.frc.riolog.udp.RioLogUdpSocketManagerApplicationService.SystemPropertyKeys.SIMULATED_LOG_SERVICE_PORT_PROP_KEY;



public class RioLogUdpSocketManagerApplicationService
{
    private static final Logger LOG = Logger.getInstance(RioLogUdpSocketManagerApplicationService.class);

    // For now, we support a single application wide port; as a future enhancement we can add different ports per project
    private static Set<Project> registeredProjects = new HashSet<>();

    public final static int MAX_PACKET_SIZE =  8 * 1024; // Most platforms have an 8K limit  // Absolute Max is 65507;


    public static final int SIMULATED_LOG_SERVICE_PORT_DEFAULT = 4248; //arbitrarily chosen port not listed at https://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers

    public static final String SIMULATED_LOG_SERVICE_MULTICAST_HOST = "230.0.0.1";

    private static final boolean USE_SIMULATED_LOG_SERVICE = BooleanUtils.toBoolean(System.getProperty(SIMULATED_LOG_SERVICE_ENABLED_PROP_KEY, Boolean.FALSE.toString()));

    public static class SystemPropertyKeys
    {
        public static final String SIMULATED_LOG_SERVICE_PROP_KEY_BASE = "frc.simulated.log.service";
        /**
         * Set this System property to 'on' or 'true' to turn on the testing server that will send a constant output of
         * mock log messages to the rioLog port. Used for testing without being attached to a RoboRIO.
         */
        public static final String SIMULATED_LOG_SERVICE_ENABLED_PROP_KEY = SIMULATED_LOG_SERVICE_PROP_KEY_BASE + ".enabled";
        public static final String SIMULATED_LOG_SERVICE_PORT_PROP_KEY = SIMULATED_LOG_SERVICE_PROP_KEY_BASE + ".port";
        public static final String SIMULATED_LOG_SERVICE_USE_CONFIGURED_PORT_PROP_KEY = SIMULATED_LOG_SERVICE_PROP_KEY_BASE + ".use.configured.port";
    }
    @Nullable
    private static DatagramSocket socket;

    @Nullable
    private static InetAddress simulatedServiceGroupAddress;
       
    public DatagramSocket getSocket(@NotNull Project project) throws IOException
    {
        return getSocket(project, FrcApplicationSettings.getInstance().getRioLogUdpPort());
    }

    
    private DatagramSocket getSocket(@NotNull Project project, int port) throws IOException
    {
        registeredProjects.add(project);
        if (socket == null)
        {
            socket = createSocket(port);
        }
        return socket;
    }
    
    public void deregister(@NotNull Project project)
    {
        registeredProjects.remove(project);
        if (registeredProjects.isEmpty())
        {
            cleanUpSocket();
        }
    }


    private void cleanUpSocket()
    {
        try
        {
            if (socket != null)
            {
                if (socket instanceof MulticastSocket)
                {
                    ((MulticastSocket) socket).leaveGroup(getSimulatedServiceGroupAddress());
                }
                FrcUtilsKt.executeQuietly(socket::disconnect);
                FrcIoExtsKt.closeQuietly(socket);
            }
        }
        catch (Exception e)
        {
            LOG.debug("[FRC] An exception occurred when cleaning up DatagramSocket");
        }
    }


    public static InetAddress getSimulatedServiceGroupAddress() throws UnknownHostException
    {
        if (simulatedServiceGroupAddress == null)
        {
            simulatedServiceGroupAddress = InetAddress.getByName(SIMULATED_LOG_SERVICE_MULTICAST_HOST);
        }
        return simulatedServiceGroupAddress;
    }

    protected DatagramSocket createSocket(int port) throws IOException
    {
        if(USE_SIMULATED_LOG_SERVICE)
        {
            MulticastSocket socket = new MulticastSocket(
                Integer.valueOf(System.getProperty(SIMULATED_LOG_SERVICE_PORT_PROP_KEY, Integer.toString(SIMULATED_LOG_SERVICE_PORT_DEFAULT))));
            socket.joinGroup(getSimulatedServiceGroupAddress());
            return socket;
        }
        else 
        {
            LOG.debug("[FRC] Creating DatagramSocket with port " + port);
            DatagramSocket socket = new DatagramSocket(port);
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
            return socket;
        }
        
    }
}
