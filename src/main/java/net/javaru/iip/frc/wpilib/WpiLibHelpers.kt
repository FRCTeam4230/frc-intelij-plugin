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

package net.javaru.iip.frc.wpilib

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.psi.PsiLiteralExpression
import com.intellij.util.io.exists
import com.intellij.util.io.isFile
import com.intellij.util.lang.JavaVersion
import net.javaru.iip.frc.FrcPluginGlobals
import net.javaru.iip.frc.i18n.FrcMessageKey
import net.javaru.iip.frc.notify.FrcNotificationType
import net.javaru.iip.frc.notify.FrcNotifications
import net.javaru.iip.frc.util.findClass
import net.javaru.iip.frc.util.warnWhenNotInTestMode
import net.javaru.iip.frc.wpilib.version.WpiLibVersion
import net.javaru.iip.frc.wpilib.version.WpiLibVersionImpl
import net.javaru.iip.frc.wpilib.version.firstRelease
import net.javaru.iip.frc.wpilib.version.minVersion
import org.apache.commons.lang3.SystemUtils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

object WpiLibHelpers
private val LOG = logger<WpiLibHelpers>()
/**
 * Determines the `projectYear` String used in the `wpilib_preferences.json` file. Typically, it is just the year such as `2020`, but it may be an alternate
 * value during the pre-releases, such as `Beta2020` or `Beta2020-2`. There doesn't appear to be any pattern to it as in the WPI repo, it
 * is a hard coded value in [https://github.com/wpilibsuite/vscode-wpilib/blob/master/vscode-wpilib/resources/gradle/java/.wpilib/wpilib_preferences.json]
 * and the change from `Beta2020` to `Beta2020-2` did not correlate to a WpiLib version/release.
 */
fun determineProjectYearStringForVersion(version: WpiLibVersion): String = if (version.isNewerThan(minVersion(2020)) && version.isOlderThan(firstRelease(2020)) && version.isBetaOrBetaPreview()) "Beta2020-2" else version.frcYear.toString()


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
 * Returns the root path for the WPI Lib installation. It does check if the directory exists, and if not its
 * missing is logged and the user is notified with a warn notification, but no other action is taken and the
 * Path is still returned.
 * @param forWpiLibVersion The WpiLibVersion to get the root path for
 */
fun getWpiLibRootPath(forWpiLibVersion: WpiLibVersion): Path = getWpiLibRootPath(forWpiLibVersion.frcYear)

/**
 * Returns the root path for the WPI Lib installation. It does check if the directory exists, and if not its
 * missing is logged and the user is notified with a warn notification, but no other action is taken and the
 * Path is still returned.
 * @param year The FRC season year
 */
fun getWpiLibRootPath(year: Int): Path
{
    // TODO: let's provide for user overriding of the install location See Issue #85: https://gitlab.com/Javaru/frc-intellij-idea-plugin/-/issues/85
    val wpiLibRootPath = getDefaultWpiLibRootPath(year)

    if (!wpiLibRootPath.exists() && FrcPluginGlobals.IS_NOT_IN_FRC_UNIT_TEST_MODE)
    {
        // TODO: when we implement above ability for user to override, prompt the user for the location here and set it
        LOG.warn("[FRC] The WPI Lib root was not found at its expected location of: $wpiLibRootPath")
        FrcNotifications.notify(FrcNotificationType.ACTIONABLE_WARN, FrcMessageKey.of("frc.wpilib.root.path.not.found.user.notification", wpiLibRootPath))
    }
    return wpiLibRootPath
}

/**
 * Returns the OS specific default/standard path for the wpilib installation, **but does not check if it exists**.
 * Generally, the [getWpiLibRootPath] should be preferred over this method.
 */
fun getDefaultWpiLibRootPath(forWpiLibVersion: WpiLibVersion): Path = getDefaultWpiLibRootPath(forWpiLibVersion.frcYear)

/**
 * Returns the OS specific default/standard path for the wpilib installation, **but does not check if it exists**.
 * Generally, the [getWpiLibRootPath] should be preferred over this method.
 */
fun getDefaultWpiLibRootPath(year: Int): Path
{
    //        From https://docs.wpilib.org/en/latest/docs/getting-started/getting-started-frc-control-system/wpilib-setup.html
    //        The installation directory has changed for 2020. In 2019 the software was installed to  ~\frcYYYY where ~ is C:\Users\Public on Windows and YYYY is the FRC year.
    //        In 2020 and later it is installed to  ~\wpilib\YYYY  This lessens clutter when multiple years software are installed.
    //        Regardless of whether All Users or Current User is chosen, the software is installed to C:\Users\Public\wpilib\YYYY where YYYY is the current FRC year.
    //            If you choose All Users, then shortcuts are installed to all users desktop and start menu and system environment variables are set.
    //            If Current User is chosen, then shortcuts and environment variables are set for only the current user.
    //
    // 2019  C:\Users\Public\frc${frcYear}          Mac & Linux: ~/frc${frcYear}
    // 2020+ C:\Users\Public\wpilib\${frcYear}      Mac & Linux: ~/wpilib/${frcYear}

    val basePath = if (SystemInfo.isWindows)
    {
        Paths.get(System.getenv("PUBLIC") ?: "C:\\Users\\Public").toAbsolutePath()
    }
    else
    {
        try
        {
            VfsUtil.getUserHomeDir()?.toNioPath()?.toAbsolutePath() ?: Paths.get("/").toAbsolutePath()
        }
        catch (t1: Throwable)
        {
            LOG.warnWhenNotInTestMode("[FRC] Could not determine WPI Lib path via VfUtils due to an exception. Will use Commons Lang SystemUtils.getUserHome() instead. Cause Summary: $t1")
            try
            {
                SystemUtils.getUserHome()?.toPath()?.toAbsolutePath() ?: Paths.get("/").toAbsolutePath()
            }
            catch (t2: Throwable)
            {
                LOG.warnWhenNotInTestMode("[FRC] Could not determine WPI Lib path via Commons Lang SystemUtils.getUserHome() due the exception: $t2")
                Paths.get("/")
            }
        }
    }

    return if (year <= 2019)
    {
        // technically 2018 and earlier is different. But at this point we can't deal with legacy anymore
        basePath.resolve("frc${year}")

    } else
    {
        basePath.resolve("wpilib").resolve("$year")
    }
}

/**
 * Returns the standard path for the wpilib JDK installation, **but does not check if it exists**.
 */
fun getWpiLibJdkHomePath(year: Int): Path = getWpiLibRootPath(year).resolve("jdk")

/**
 * Returns the standard path for the wpilib 'tools' directory, **but does not check if it exists**.
 */
fun getWpiLibToolsPath(year: Int): Path = getWpiLibRootPath(year).resolve("tools")

/**
 * Returns the standard path for the wpilib 'tools' directory, **but does not check if it exists**.
 */
fun getWpiLibToolsPath(forWpiLibVersion: WpiLibVersion): Path = getWpiLibRootPath(forWpiLibVersion).resolve("tools")

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
fun getWpiLibJdkReleaseFile(year: Int): Path = getWpiLibJdkHomePath(year).resolve("RELEASE")

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


/**
 * Gets the WPI Lib Version for the attached WPI Lib JAR **within a smart read action**, returning null if it cannot be determined (for example if the
 * WpiLib is not attached as a dependency/library).
 */
fun Project.getAttachedWpiLibVersionInSmartReadAction(): WpiLibVersion?
{
    var version: WpiLibVersion? = null
    DumbService.getInstance(this).runReadActionInSmartMode() {
        version = getAttachedWpiLibVersion()
    }
    return version
}

/**
 * Gets the WPI Lib Version for the attached WPI Lib JAR, returning null if it cannot be determined (for example if the
 * WpiLib is not attached as a dependency/library). **This action should be run in a `runReadActionInSmartMode` wrapping.**
 * @see [getAttachedWpiLibVersionInSmartReadAction]
 */
fun Project.getAttachedWpiLibVersion(): WpiLibVersion?
{
    val versionString = this.getAttachedWpiLibVersionString()
    return WpiLibVersionImpl.parseSafely(versionString)
}

/**
 * Gets the WPI Lib Version String for the attached WPI Lib JAR **within a smart read action**, returning null if it cannot be determined (for example if the
 * WpiLib is not attached as a dependency/library).
 * @see [getAttachedWpiLibVersionInSmartReadAction]
 */
fun Project.getAttachedWpiLibVersionStringInSmartReadAction(): String?
{
    var version: String? = null
    DumbService.getInstance(this).runReadActionInSmartMode() {
        version = this.getAttachedWpiLibVersionString()
    }
    return version
}

/**
 * Gets the WPI Lib Version String for the attached WPI Lib JAR returning null if it cannot be determined (for example if the
 * WpiLib is not attached as a dependency/library). **This action should be run in a `runReadActionInSmartMode` wrapping.**
 * @see [getAttachedWpiLibVersionStringInSmartReadAction]
 */
fun Project.getAttachedWpiLibVersionString(): String?
{
    val verClasses = findClass(this, WpiLibConstants.VERSION_CLASS_FQN)
    for (psiClass in verClasses)
    {
        val initializer = psiClass?.findFieldByName("Version", false)?.initializer
        if (initializer is PsiLiteralExpression)
        {
            val value = initializer.value
            if (value is String)
            {
                return value
            }
        }
    }
    return null
}