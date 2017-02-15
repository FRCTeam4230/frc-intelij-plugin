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

package net.javaru.iip.frc.roboRIO.riolog.ssh;

import org.junit.Test;
import com.intellij.openapi.diagnostic.Logger;

import static org.junit.Assert.*;



public class RioLogMonitoringProcessTest
{
    private static final Logger LOG = Logger.getInstance(RioLogMonitoringProcessTest.class);

    @Test
    public void testIsReachable() throws Exception
    {
        final SshRioLogMonitoringProcess process = new SshRioLogMonitoringProcess(() -> { });

        executeTest(process, "google.com");
//        executeTest(process, "192.168.50.250");
//        executeTest(process, "roboRIO-9999-FRC.local");
    }


    public void executeTest(SshRioLogMonitoringProcess process, String host)
    {
        final boolean reachable = process.isReachable(host);
        assertTrue("Host is not reachable: " + host, reachable);
    }
}
