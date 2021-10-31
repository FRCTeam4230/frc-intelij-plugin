/*
 * Copyright 2015-2021 the original author or authors.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.javaru.iip.frc.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class FrcFileExtsKtTest
{
    @ParameterizedTest
    @MethodSource
    fun testFindCommonParentDir(paths: List<String>, expected: String)
    {
        assertEquals(expected, findCommonParentDir(paths))
    }

    @Suppress("unused")
    companion object
    {
        @JvmStatic
        fun testFindCommonParentDir(): Stream<Arguments>
        {
            return Stream.of(
                Arguments.of(
                    listOf(
                        "C:\\dev\\projects\\project1\\src\\main\\java\\App.java",
                        "C:\\dev\\projects\\project1\\src\\main\\Info.txt",
                        "C:\\dev\\projects\\project1\\src\\main\\resources\\data.txt",
                        "C:\\dev\\projects\\project1\\src\\main\\resources\\data\\example-input\\foo.txt",
                        "C:\\dev\\projects\\project1\\src\\test\\resources",
                          ),
                    "C:/dev/projects/project1/src"
                            ),
                Arguments.of(
                    listOf(
                        "C:/dev/projects/project2/src/main/java/App.java",
                        "C:/dev/projects/project2/src/main/Info.txt",
                        "C:/dev/projects/project2/src/main/resources/data.txt",
                        "C:/dev/projects/project2/src/main/resources/data/example-input/foo.txt",
                        "C:/dev/projects/project2/src/test/resources",
                          ),
                    "C:/dev/projects/project2/src"
                            ),
                Arguments.of(
                    listOf(
                        "/dev/projects/project3/src/main/java/App.java",
                        "/dev/projects/project3/src/main/Info.txt",
                        "/dev/projects/project3/src/main/resources/data.txt",
                        "/dev/projects/project3/src/main/resources/data/example-input/foo.txt",
                        "/dev/projects/project3/src/test/resources",
                          ),
                    "/dev/projects/project3/src"
                            ),
                Arguments.of(
                    listOf(
                        "/dev/projects/project4/src/main/java/App.java",
                        "/dev/projects/project4/src/main/Info.txt",
                        "/dev/projects/project4/src/",
                        "/dev/projects/project4/src/main/resources/data.txt",
                        "/dev/projects/project4/src/main/resources/data/example-input/foo.txt",
                        "/dev/projects/project4/src/test/resources",
                          ),
                    "/dev/projects/project4/src"
                            ),
                Arguments.of(
                    listOf(
                        "/dev/projects/project5/src/main/java/App.java",
                        "/dev/projects/project5/src/main/Info.txt",
                        "/dev/projects/project5/src/",
                        "/dev/projects/project5/src",
                        "/dev/projects/project5/src/main/resources/data.txt",
                        "/dev/projects/project5/src/main/resources/data/example-input/foo.txt",
                        "/dev/projects/project5/src/test/resources",
                          ),
                    "/dev/projects/project5/src"
                            ),
                Arguments.of(
                    listOf(
                        "/dev/projects/project6/src",
                          ),
                    "/dev/projects/project6/src"
                            ),

                )

        }
    }
}