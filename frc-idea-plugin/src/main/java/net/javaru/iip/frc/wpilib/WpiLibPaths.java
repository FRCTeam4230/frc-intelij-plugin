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

import java.nio.file.Path;

import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.settings.FrcApplicationComponent;
import net.javaru.iip.frc.settings.FrcSettings;



public class WpiLibPaths
{
    private static final Logger LOG = Logger.getInstance(WpiLibPaths.class);


    public static Path getUserLibDir()
    {
        FrcSettings settings = FrcApplicationComponent.getInstance().getState();
        final Path wpiLibDir = settings.getWpiLibDir();
        return getUserLibDir(wpiLibDir);
    }


    public static Path getUserLibDir(Path wpiLibDir)
    {
        final Path userLib = wpiLibDir.resolve("user/java/lib");
        return userLib;
    }
    
    
    public static Path getUserRootDir()
    {
        FrcSettings settings = FrcApplicationComponent.getInstance().getState();
        final Path wpiLibDir = settings.getWpiLibDir();
        return getUserRootDir(wpiLibDir);
    }


    public static Path getUserRootDir(Path wpiLibDir)
    {
        final Path userRoot = wpiLibDir.resolve("user");
        return userRoot;
    }
    
    
    
    public static Path getToolsDir()
    {
        FrcSettings settings = FrcApplicationComponent.getInstance().getState();
        final Path wpiLibDir = settings.getWpiLibDir();
        return getToolsDir(wpiLibDir);
    }


    public static Path getToolsDir(Path wpiLibDir)
    {
        final Path userLib = wpiLibDir.resolve("tools");
        return userLib;
    }
    
    public static Path getJavaDir()
    {
        FrcSettings settings = FrcApplicationComponent.getInstance().getState();
        final Path wpiLibDir = settings.getWpiLibDir();
        return getJavaDir(wpiLibDir);
    }


    public static Path getJavaDir(Path wpiLibDir)
    {
        final Path userLib = wpiLibDir.resolve("java");
        return userLib;
    }


    public static Path getJavaLibDir()
    {
        FrcSettings settings = FrcApplicationComponent.getInstance().getState();
        final Path wpiLibDir = settings.getWpiLibDir();
        return getJavaLibDir(wpiLibDir);
    }


    public static Path getJavaLibDir(Path wpiLibDir)
    {
        final Path javaLib = wpiLibDir.resolve("java/lib");
        return javaLib;
    }


    public static Path getWpilibPropertiesFile()
    {
        FrcSettings settings = FrcApplicationComponent.getInstance().getState();
        final Path wpiLibDir = settings.getWpiLibDir();
        return getWpilibPropertiesFile(wpiLibDir);
    }


    public static Path getWpilibPropertiesFile(Path wpiLibDir)
    {
        final Path wpiPropertiesFile = wpiLibDir.resolve("wpilib.properties");
        return wpiPropertiesFile;
    }


}
