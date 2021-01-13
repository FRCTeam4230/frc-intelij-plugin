/*
 * Copyright 2015-2021 the original author or authors
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

package net.javaru.iip.frc.wizard

import net.javaru.iip.frc.settings.FrcApplicationSettings
import net.javaru.iip.frc.wpilib.determineProjectYearStringForVersion
import net.javaru.iip.frc.wpilib.gradlePluginRepo.GradleRioMavenMetadataState
import net.javaru.iip.frc.wpilib.version.WpiLibVersion
import java.nio.file.Path
import java.nio.file.Paths

const val DEFAULT_BASE_PACKAGE = "frc.robot"
const val DEFAULT_ROBOT_CLASS_NAME = "Robot"

class FrcProjectWizardData(
        var teamNumber: Int = FrcApplicationSettings.getInstance().teamNumber,
        /** The simple name of the Main class (not to be confused with the (primary) Robot class). This is the simple class that has the `main()` method.*/
        var mainClassSimpleName: String = "Main",
        /** The simple name of the primary Robot class (not to be confused with the Main class). This is the class that extends one of the WPILib `RobotBase` classes.*/
        var robotClassSimpleName: String = DEFAULT_ROBOT_CLASS_NAME,  // if/whn we make settable, we need to change the template copying to rename the file!
        var basePackage: String = DEFAULT_BASE_PACKAGE,
        var wpilibVersion: WpiLibVersion = GradleRioMavenMetadataState.getInstance().wpiLibMavenMetadata.releaseAsWpiLibVersion,
        var frcWizardTemplateDefinition: FrcWizardTemplateDefinition = FrcWizard2019ProjectTemplateDefinition.CommandBased,
        var includeVsCodeConfigs: Boolean = true,
        var gitIgnoreConfiguration: GitIgnoreConfiguration = GitIgnoreConfiguration(true, generateFromSite = true),
        var includeJUnitSupport:Boolean = true,
        var junitOption: JUnitOption = JUnitOption.JUnit5,
        var junit5Version: String = "5.5.2"
                          )
{

    /**
     * The `projectYear` used in the `wpilib_preferences.json` file. Typically it is just the year such as `2020`, but it may be an alternate 
     * value during the beta releases, such as `Beta2020` or `Beta2020-2`. There doesn't appear to be any pattern to it as in the WPI repo, it 
     * is a hard coded value in [https://github.com/wpilibsuite/vscode-wpilib/blob/master/vscode-wpilib/resources/gradle/java/.wpilib/wpilib_preferences.json]
     * and the change from `Beta2020` to `Beta2020-2` did not correlate to a WpiLib version/release.
     */
    val projectYear: String
        get() = determineProjectYearStringForVersion(wpilibVersion)
                
    /** The project year, such as `2019` or `2020`, as a String. */
    val frcYear: String
        get() = wpilibVersion.frcYear.toString()
    
    val teamNumberString: String
        get() = teamNumber.toString()

    val basePackageAsDirString: String
        get() = basePackage.replace('.', '/')
    
    val basePackageAsDirPath: Path
        get() = Paths.get(basePackageAsDirString)
    
    /** The Fully Qualified (FQ) name of the Main class (not to be confused with the (primary) Robot class). This is the simple class that has the `main()` method.*/
    val mainClassFQ: String
        get() = if (basePackage.isEmpty()) mainClassSimpleName else "${basePackage}.${mainClassSimpleName}"
    /** The Fully Qualified (FQ) name of the primary Robot class (not to be confused with the Main class). This is the class that extends one of the WPILib `RobotBase` classes.*/
    val robotClassFQ: String
        get() = if (basePackage.isEmpty()) mainClassSimpleName else "${basePackage}.${mainClassSimpleName}"

    val gradleDistributionUrl: String
        get() {
            return when (wpilibVersion.frcYear)
            {
                2019 -> """https\://services.gradle.org/distributions/gradle-5.0-bin.zip"""
                2020 -> """https\://services.gradle.org/distributions/gradle-6.0.1-bin.zip"""
                else -> """https\://services.gradle.org/distributions/gradle-6.0.1-bin.zip"""
            }
        } 
    
    fun junitIncludeVintageSupport(): Boolean = includeJUnitSupport && (junitOption == JUnitOption.JUnit5withVintage)
    fun junitUseJUnitPlatform(): Boolean = includeJUnitSupport && (junitOption == JUnitOption.JUnit5 || junitOption == JUnitOption.JUnit5withVintage) 
    fun junitIsJUnit4Only(): Boolean = includeJUnitSupport && (junitOption == JUnitOption.JUnit4)
    
    override fun toString(): String
    {
        return "FrcProjectWizardData(teamNumber=$teamNumber, mainClassSimpleName='$mainClassSimpleName', robotClassSimpleName='$robotClassSimpleName', basePackage='$basePackage', wpilibVersion=$wpilibVersion, frcWizardTemplateDefinition=$frcWizardTemplateDefinition, includeVsCodeConfigs=$includeVsCodeConfigs, gitIgnoreConfiguration=$gitIgnoreConfiguration, includeJUnitSupport=$includeJUnitSupport, junitOption=$junitOption, junit5Version='$junit5Version')"
    }

    enum class JUnitOption() {JUnit5, JUnit5withVintage, JUnit4}
    
    
}

data class GitIgnoreConfiguration(
        var includeGitIgnoreFile: Boolean = true,
        var intellij: IdeConfigOption = IdeConfigOption.Share,
        var vscode: IdeConfigOption = IdeConfigOption.Share,
        var eclipse: IdeConfigOption = IdeConfigOption.Share,
        var netbeans: IdeConfigOption = IdeConfigOption.Share,
        var java: Boolean = true,
        var gradle: Boolean = true,
        var linux: Boolean = true,
        var macOS: Boolean = true,
        var windows: Boolean = true,
        /** The `.gitignore` file generated by wpilib has C++ in it. But that is more a case (I believe) that they use the same file for both Java and C++ based projects. So we default to `false` for this. */
        var cpp: Boolean = false,
        /** Additional templates names from the gitignore API site. See https://www.toptal.com/developers/gitignore/api/list (and/or https://www.toptal.com/developers/gitignore?templates) */
        var additionalGitignoreTemplates: MutableList<String> = mutableListOf(),
        var generateFromSite: Boolean = true
                                 )
{
    // since Java does not support named parameters, using the built-ion data class `copy()` function in Java 
    // requires us to pass in all properties, making it not very useful. So we have this function
    fun fullCopy(): GitIgnoreConfiguration = this.copy()
    
    companion object
    {
        /** Returns a `GitIgnoreConfiguration` instance with all properties set to their defaults. */
        fun defaultInstance(): GitIgnoreConfiguration = GitIgnoreConfiguration()
    }
}

enum class IdeConfigOption { Share, Ignore, NoEntry}
