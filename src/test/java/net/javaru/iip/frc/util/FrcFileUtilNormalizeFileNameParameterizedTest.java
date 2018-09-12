/*
 * Copyright 2015-2018 the original author or authors
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

package net.javaru.iip.frc.util;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.*;

import static org.junit.Assert.*;



@RunWith(Parameterized.class)
public class FrcFileUtilNormalizeFileNameParameterizedTest
{

    private final String param;
    private final String expected;


    public FrcFileUtilNormalizeFileNameParameterizedTest(String param, String expected)
    {
        this.param = param;
        this.expected = expected;
    }


    @Parameters
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][] {
             /*   0 */ {"build", "build.xml"},
             /*   1 */ {"build.xml", "build.xml"},
             /*   2 */ {"build.XML", "build.xml"},
             /*   3 */ {"build.Xml", "build.xml"},
             /*   4 */ {"build.foo", "build.foo.xml"},
             /*   5 */ {"build.foo.xml", "build.foo.xml"},
             /*   6 */ {null, null},
             /*   7 */ {"build ", "build.xml"},
             /*   8 */ {" build", "build.xml"},
             /*   9 */ {" build ", "build.xml"},
             /*  10 */ {" build.xml ", "build.xml"},
             });
    }


    @Test
    public void testNormalizeFileNameExtension()
    {
        final String actual = FrcFileUtils.normalizeFileNameExtension(param, "xml");
        assertEquals("Wrong value for " + param, expected, actual);
    }
}