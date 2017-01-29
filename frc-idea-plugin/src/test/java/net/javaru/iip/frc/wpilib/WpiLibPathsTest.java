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

package net.javaru.iip.frc.wpilib;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import com.intellij.util.ResourceUtil;

import static org.junit.Assert.*;



public class WpiLibPathsTest
{
    
    private final Path wpiLibDir = Paths.get("C:\\Users\\Dilbert\\wpilib");
    
    
    @Test
    public void getUserRootDir() throws Exception
    {
        assertEquals("Wrong dir path for getUserRootDir", "C:\\Users\\Dilbert\\wpilib\\user", WpiLibPaths.getUserRootDir(wpiLibDir).toString());
    }


    @Test
    public void getUserLibDir() throws Exception
    {
        assertEquals("Wrong dir path for getUserLibDir", "C:\\Users\\Dilbert\\wpilib\\user\\java\\lib", WpiLibPaths.getUserLibDir(wpiLibDir).toString());
    }


    @Test
    public void getToolsDir() throws Exception
    {
        assertEquals("Wrong dir path for getToolsDir", "C:\\Users\\Dilbert\\wpilib\\tools", WpiLibPaths.getToolsDir(wpiLibDir).toString());
    }


    @Test
    public void getJavaDir() throws Exception
    {
        assertEquals("Wrong dir path for getJavaDir", "C:\\Users\\Dilbert\\wpilib\\java", WpiLibPaths.getJavaDir(wpiLibDir).toString());
    }


    @Test
    public void getJavaCurrentVersionDir() throws Exception
    {
        assertEquals("Wrong dir path for getJavaCurrentDir", "C:\\Users\\Dilbert\\wpilib\\java\\current", WpiLibPaths.getJavaCurrentDir(wpiLibDir, "current").toString());
    }


    @Test
    public void getJavaCurrentVersionDir_readCurrentValue() throws Exception
    {
        final URL resource = ResourceUtil.getResource(WpiLibPathsTest.class, "wpiLib/mockLocalDir/wpilib", "wpilib.properties");
        final Path propFile = Paths.get(resource.toURI()).toAbsolutePath();
        Path mockWpiLibDir = propFile.getParent();

        System.out.println("mockWpiLibDir = " + mockWpiLibDir);


        assertEquals("Wrong dir path for getJavaCurrentDir reading properties file", mockWpiLibDir.resolve("java/alt-version-value").toString(), WpiLibPaths.getJavaCurrentDir(mockWpiLibDir).toString());
        
        //Test properties file does not exit
        mockWpiLibDir = Paths.get("/nonExistent/Mock/Path");
        assertEquals("Wrong dir path for getJavaCurrentDir when properties file is not found", mockWpiLibDir.resolve("java/current").toString(), WpiLibPaths.getJavaCurrentDir(mockWpiLibDir).toString());
    }


    @Test
    public void getJavaLibDir() throws Exception
    {
        assertEquals("Wrong dir path for getJavaLibDir", "C:\\Users\\Dilbert\\wpilib\\java\\current\\lib", WpiLibPaths.getJavaLibDir(wpiLibDir).toString());
    }

    
    public void getAntDir() throws Exception
    {
        assertEquals("Wrong dir path for getAntDir", "C:\\Users\\Dilbert\\wpilib\\java\\current\\ant", WpiLibPaths.getAntDir(wpiLibDir).toString());
    }

    public void getJavadocDir() throws Exception
    {
        assertEquals("Wrong dir path for getJavadocDir", "C:\\Users\\Dilbert\\wpilib\\java\\current\\javadoc", WpiLibPaths.getJavadocDir(wpiLibDir).toString());
    }


    @Test
    public void getWpilibPropertiesFile() throws Exception
    {
        assertEquals("Wrong file path for getWpilibPropertiesFile", "C:\\Users\\Dilbert\\wpilib\\wpilib.properties", WpiLibPaths.getWpilibPropertiesFile(wpiLibDir).toString());
    }

}