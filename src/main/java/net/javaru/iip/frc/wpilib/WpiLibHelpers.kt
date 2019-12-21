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

package net.javaru.iip.frc.wpilib

import com.intellij.util.io.isFile
import com.intellij.util.lang.JavaVersion
import net.javaru.iip.frc.wpilib.version.WpiLibVersion
import net.javaru.iip.frc.wpilib.version.firstRelease
import net.javaru.iip.frc.wpilib.version.minVersion
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/**
 * The `projectYear` used in the `wpilib_preferences.json` file. Typically it is just the year such as `2020`, but it may be an alternate
 * value during the beta releases, such as `Beta2020` or `Beta2020-2`. There doesn't appear to be any pattern to it as in the WPI repo, it
 * is a hard coded value in [https://github.com/wpilibsuite/vscode-wpilib/blob/master/vscode-wpilib/resources/gradle/java/.wpilib/wpilib_preferences.json]
 * and the change from `Beta2020` to `Beta2020-2` did not correlate to a WpiLib version/release.
 */
fun WpiLibVersion.projectYearFor(): String = if (this.isNewerThan(minVersion(2020)) && this.isOlderThan(firstRelease(2020)) && this.isBetaOrBetaPreview()) "Beta2020-2" else major.toString()


//        JAVA_VERSION key in 
//            2019: C:\Users\Public\frc${frcYear}\jdk\release
//            2020+ C:\Users\Public\wpilib\${frcYear}\jdk\release 
//        From https://docs.wpilib.org/en/latest/docs/getting-started/getting-started-frc-control-system/wpilib-setup.html
//        The installation directory has changed for 2020. In 2019 the software was installed to  ~\frcYYYY where ~ is C:\Users\Public on Windows and YYYY is the FRC year. 
//        In 2020 and later it is installed to  ~\wpilib\YYYY  This lessens clutter when multiple years software are installed.
//        Regardless of whether All Users or Current User is chosen, the software is installed to C:\Users\Public\wpilib\YYYY where YYYY is the current FRC year. 
//            If you choose All Users, then shortcuts are installed to all users desktop and start menu and system environment variables are set. 
//            If Current User is chosen, then shortcuts and environment variables are set for only the current user.

/**
 * Returns the standard path for the wpilib installation, **but does not check if it exists**.
 */
fun getWpiLibRoot(year: Int): Path
{
    //        From https://docs.wpilib.org/en/latest/docs/getting-started/getting-started-frc-control-system/wpilib-setup.html
    //        The installation directory has changed for 2020. In 2019 the software was installed to  ~\frcYYYY where ~ is C:\Users\Public on Windows and YYYY is the FRC year. 
    //        In 2020 and later it is installed to  ~\wpilib\YYYY  This lessens clutter when multiple years software are installed.
    //        Regardless of whether All Users or Current User is chosen, the software is installed to C:\Users\Public\wpilib\YYYY where YYYY is the current FRC year. 
    //            If you choose All Users, then shortcuts are installed to all users desktop and start menu and system environment variables are set. 
    //            If Current User is chosen, then shortcuts and environment variables are set for only the current user.
    //
    // 2019  C:\Users\Public\frc${frcYear}\jdk
    // 2020+ C:\Users\Public\wpilib\${frcYear}\jdk 

    // technically 2018 and earlier is different. But at this point we can't deal with legacy anymore
    val publicRoot = Paths.get(System.getenv("PUBLIC") ?: "C:\\Users\\Public")
    return if (year <= 2019)
    {
        publicRoot.resolve("frc${year}")

    }
    else
    {
        publicRoot.resolve("wpilib").resolve ("$year")
    }
}

/**
 * Returns the standard path for the wpilib JDK installation, **but does not check if it exists**.
 */
fun getWpiLibJdkHome(year: Int): Path = getWpiLibRoot(year).resolve("jdk")

/**
 * Returns the standard path for the Java `RELEASE` file for the wpilib JDK installation, **but does not check if it exists**.
 * Some example content:
 * 
 * **Oracle JDK 11 AND OpenJDK 11**
 * ```
 * IMPLEMENTOR="Oracle Corporation"
 * IMPLEMENTOR_VERSION="18.9"
 * JAVA_VERSION="11.0.1"
 * JAVA_VERSION_DATE="2018-10-16"
 * MODULES="java.base java.compiler . . ."
 * OS_ARCH="x86_64"
 * OS_NAME="Windows"
 * SOURCE=".:8513ac27b651"
 * ```
 * 
 * **Amazon Corretto 8**
 * ```
 * JAVA_VERSION="1.8.0_222"
 * OS_NAME="Windows"
 * OS_VERSION="5.2"
 * OS_ARCH="amd64"
 * SOURCE=""
 * ```
 * 
 * **Amazon Corretto 11**
 * ```
 * IMPLEMENTOR="Amazon.com Inc."
 * IMPLEMENTOR_VERSION="Corretto-11.0.5.10.1"
 * JAVA_VERSION="11.0.5"
 * JAVA_VERSION_DATE="2019-10-15"
 * MODULES="java.base java.compiler . . ."
 * OS_ARCH="x86_64"
 * OS_NAME="Windows"
 * SOURCE=""
 * ```
 */
fun getWpiLibJdkReleaseFile(year: Int): Path = getWpiLibJdkHome(year).resolve("RELEASE")

/**
 * Returns the value of the `JAVA_VERSION` property of the JDK `RELEASE` file, or null if the file 
 * does not exist, or does nto contain the `JAVA_VERSION property.
 * Example values:
 *  - 11.0.2
 *  - 11.0.5
 *  - 1.8.0_222
 *  - `null`
 */
fun getWpiLibJdkReleaseJavaVersionString(year: Int): String?
{
    val releaseFile = getWpiLibJdkReleaseFile(year)
    return if (releaseFile.isFile())
    {
        val properties = Properties()
        properties.load(Files.newBufferedReader(releaseFile))
        val value = properties.getProperty("JAVA_VERSION", null)
        // We need to strip off surrounding quotes
        value?.removeSurrounding("\"")
    }
    else
    {
        null
    }
}

fun getWpiLibJdkReleaseJavaVersion(year: Int): JavaVersion? = JavaVersion.tryParse(getWpiLibJdkReleaseJavaVersionString(year))
fun getWpiLibJdkReleaseJavaFeatureVersion(year: Int): Int? = JavaVersion.tryParse(getWpiLibJdkReleaseJavaVersionString(year))?.feature