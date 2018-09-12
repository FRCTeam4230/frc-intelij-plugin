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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;



public class FrcFileUtilNormalizeFileNameParameterizedTest
{

    @ParameterizedTest
    @MethodSource("data")
   void testTestNormalizeFileNameExtension(String param, String expected)
   {
       final String actual = FrcFileUtils.normalizeFileNameExtension(param, "xml");
       assertEquals(expected, actual, "Wrong value for " + param);
   }


    static Iterable<Arguments> data()
    {
        return Arrays.asList(
               Arguments.of("build", "build.xml"),
               Arguments.of("build.xml", "build.xml"),
               Arguments.of("build.XML", "build.xml"),
               Arguments.of("build.Xml", "build.xml"),
               Arguments.of("build.foo", "build.foo.xml"),
               Arguments.of("build.foo.xml", "build.foo.xml"),
               Arguments.of(null, null),
               Arguments.of("build ", "build.xml"),
               Arguments.of(" build", "build.xml"),
               Arguments.of(" build ", "build.xml"),
               Arguments.of(" build.xml ", "build.xml")
                );
    }
}