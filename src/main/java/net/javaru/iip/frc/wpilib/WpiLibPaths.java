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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;

import net.javaru.iip.frc.settings.FrcApplicationSettings;



public class WpiLibPaths
{
    private static final Logger LOG = Logger.getInstance(WpiLibPaths.class);
    
    public static final String DEFAULT_CURRENT_VERSION_NAME = "current";



    public static Path getWpiLibRootDir()
    {
        return FrcApplicationSettings.Settings.INSTANCE().getWpiLibDir();
    }


    public static Path getUserRootDir()
    {
        return getUserRootDir(getWpiLibRootDir());
    }


    public static Path getUserRootDir(Path wpiLibDir)
    {
        return wpiLibDir.resolve("user");
    }


    public static Path getUserLibDir()
    {
        return getUserLibDir(getWpiLibRootDir());
    }


    public static Path getUserLibDir(Path wpiLibDir)
    {
        // user/java/lib
        return getUserRootDir(wpiLibDir).resolve("java/lib");
    }


    public static Path getToolsDir()
    {
        return getToolsDir(getWpiLibRootDir());
    }


    public static Path getToolsDir(Path wpiLibDir)
    {
        return wpiLibDir.resolve("tools");
    }

    public static Path getCommonDir()
    {
        return getCommonDir(getWpiLibRootDir());
    }


    public static Path getCommonDir(Path wpiLibDir)
    {
        return wpiLibDir.resolve("common");
    }


    public static Path getCommonCurrentVersionDir()
    {
        return getCommonCurrentVersionDir(getWpiLibRootDir());
    }


    public static Path getCommonCurrentVersionDir(Path wpiLibDir)
    {
        return getCommonCurrentVersionDir(wpiLibDir, resolveCurrentVersionName(wpiLibDir));
    }


    protected static Path getCommonCurrentVersionDir(Path wpiLibDir, @NotNull String versionName)
    {
        return getCommonDir(wpiLibDir).resolve(versionName);
    }


    public static Path getCommonCurrentVersionSharedDir()
    {
        return getCommonCurrentVersionSharedDir(getWpiLibRootDir());
    }


    public static Path getCommonCurrentVersionSharedDir(Path wpiLibDir)
    {
        return getCommonCurrentVersionDir(wpiLibDir).resolve("lib/linux/athena/shared");
    }


    
    public static Path getJavaDir()
    {
        return getJavaDir(getWpiLibRootDir());
    }


    public static Path getJavaDir(Path wpiLibDir)
    {
        return wpiLibDir.resolve("java");
    }


    public static Path getJavaCurrentDir()
    {
        return getJavaCurrentDir(getWpiLibRootDir());
    }


    public static Path getJavaCurrentDir(Path wpiLibDir)
    {
        return getJavaCurrentDir(wpiLibDir, resolveCurrentVersionName(wpiLibDir));
    }


    protected static Path getJavaCurrentDir(Path wpiLibDir, @NotNull String versionName)
    {
        return getJavaDir(wpiLibDir).resolve(versionName);
    }


    protected static String resolveCurrentVersionName(Path wpiLibDir)
    {
        try
        {
            final Path propertiesFile = getWpilibPropertiesFile(wpiLibDir);
            if (Files.notExists(propertiesFile))
            {
                LOG.info(
                    "[FRC] wpilib.properties file does not exist at '" + propertiesFile + "'. Using default version of '" + DEFAULT_CURRENT_VERSION_NAME + "'");
                return DEFAULT_CURRENT_VERSION_NAME;
            }
            final String key = "version";
            final Properties defaultProperties = new Properties();
            defaultProperties.setProperty(key, DEFAULT_CURRENT_VERSION_NAME);
            final Properties properties = new Properties(defaultProperties);
            try (final InputStream in = Files.newInputStream(propertiesFile))
            {
                properties.load(in);
                return properties.getProperty(key, DEFAULT_CURRENT_VERSION_NAME);
            }
        }
        catch (Exception e)
        {
            LOG.warn("[FRC] An exception occurred when determining current version from wpilib.properties");
            return DEFAULT_CURRENT_VERSION_NAME;
        }
    }




    public static Path getJavaLibDir()
    {
        return getJavaLibDir(getWpiLibRootDir());
    }


    public static Path getJavaLibDir(Path wpiLibDir)
    {
        // java/current/lib
        return getJavaCurrentDir(wpiLibDir).resolve("lib");
    }


    public static Path getAntDir()
    {
        return getAntDir(getWpiLibRootDir());
    }


    public static Path getAntDir(Path wpiAntDir)
    {
        // java/current/ant
        return getJavaCurrentDir(wpiAntDir).resolve("ant");
    }


    public static Path getJavadocDir()
    {
        return getJavadocDir(getWpiLibRootDir());
    }


    public static Path getJavadocDir(Path wpiJavadocDir)
    {
        // java/current/javadoc
        return getJavaCurrentDir(wpiJavadocDir).resolve("javadoc");
    }


    public static Path getWpilibPropertiesFile()
    {
        return getWpilibPropertiesFile(getWpiLibRootDir());
    }


    public static Path getWpilibPropertiesFile(Path wpiLibDir)
    {
        return wpiLibDir.resolve("wpilib.properties");
    }

}
