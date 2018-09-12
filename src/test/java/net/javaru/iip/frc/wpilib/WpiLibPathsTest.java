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

package net.javaru.iip.frc.wpilib;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import com.intellij.util.ResourceUtil;

import static org.junit.jupiter.api.Assertions.*;



public class WpiLibPathsTest
{
    private final Path wpiLibDir = Paths.get("C:\\Users\\Dilbert\\wpilib");
    
    
    @Test
    public void getUserRootDir()
    {
        assertEquals("C:\\Users\\Dilbert\\wpilib\\user", WpiLibPaths.getUserRootDir(wpiLibDir).toString(), "Wrong dir path for getUserRootDir");
    }


    @Test
    public void getUserLibDir()
    {
        assertEquals("C:\\Users\\Dilbert\\wpilib\\user\\java\\lib", WpiLibPaths.getUserLibDir(wpiLibDir).toString(),
                                "Wrong dir path for getUserLibDir");
    }


    @Test
    public void getToolsDir()
    {
        assertEquals("C:\\Users\\Dilbert\\wpilib\\tools", WpiLibPaths.getToolsDir(wpiLibDir).toString(), "Wrong dir path for getToolsDir");
    }


    @Test
    public void getJavaDir()
    {
        assertEquals("C:\\Users\\Dilbert\\wpilib\\java", WpiLibPaths.getJavaDir(wpiLibDir).toString(), "Wrong dir path for getJavaDir");
    }


    @Test
    public void getJavaCurrentVersionDir()
    {
        assertEquals(
                "C:\\Users\\Dilbert\\wpilib\\java\\current",
                WpiLibPaths.getJavaCurrentDir(wpiLibDir, "current").toString(), "Wrong dir path for getJavaCurrentDir");
    }


    @Test
    public void getJavaCurrentVersionDir_readCurrentValue() throws Exception
    {
        final URL resource = ResourceUtil.getResource(WpiLibPathsTest.class, "wpiLib/mockLocalDir/wpilib", "wpilib.properties");
        final Path propFile = Paths.get(resource.toURI()).toAbsolutePath();
        Path mockWpiLibDir = propFile.getParent();

        System.out.println("mockWpiLibDir = " + mockWpiLibDir);


        assertEquals(
                mockWpiLibDir.resolve("java/alt-version-value").toString(),
                WpiLibPaths.getJavaCurrentDir(mockWpiLibDir).toString(), "Wrong dir path for getJavaCurrentDir reading properties file");

        //Test properties file does not exit
        mockWpiLibDir = Paths.get("/nonExistent/Mock/Path");
        assertEquals(
                mockWpiLibDir.resolve("java/current").toString(),
                WpiLibPaths.getJavaCurrentDir(mockWpiLibDir).toString(), "Wrong dir path for getJavaCurrentDir when properties file is not found");
    }


    @Test
    public void getJavaLibDir()
    {
        assertEquals("C:\\Users\\Dilbert\\wpilib\\java\\current\\lib", WpiLibPaths.getJavaLibDir(wpiLibDir).toString(),
                                "Wrong dir path for getJavaLibDir");
    }


    @Test
    public void getAntDir()
    {
        assertEquals("C:\\Users\\Dilbert\\wpilib\\java\\current\\ant", WpiLibPaths.getAntDir(wpiLibDir).toString(), "Wrong dir path for getAntDir");
    }


    @Test
    public void getJavadocDir()
    {
        assertEquals("C:\\Users\\Dilbert\\wpilib\\java\\current\\javadoc", WpiLibPaths.getJavadocDir(wpiLibDir).toString(),
                                "Wrong dir path for getJavadocDir");
    }


    @Test
    public void getWpilibPropertiesFile()
    {
        assertEquals(
                "C:\\Users\\Dilbert\\wpilib\\wpilib.properties",
                WpiLibPaths.getWpilibPropertiesFile(wpiLibDir).toString(), "Wrong file path for getWpilibPropertiesFile");
    }

}