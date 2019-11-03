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

package net.javaru.iip.frc.wizard

import net.javaru.iip.frc.settings.FrcApplicationSettings
import net.javaru.iip.frc.wpilib.version.WpiLibVersion
import net.javaru.iip.frc.wpilib.version.WpiLibVersionImpl
import java.nio.file.Path
import java.nio.file.Paths

const val DEFAULT_BASE_PACKAGE = "frc.robot"
const val DEFAULT_ROBOT_CLASS_NAME = "Robot"

class FrcProjectWizardData(
        var teamNumber: Int = FrcApplicationSettings.INSTANCE().teamNumber,
        /** The simple name of the Main class (not to be confused with the (primary) Robot class). This is the simple class that has the `main()` method.*/
        var mainClassSimpleName: String = "Main",
        /** The simple name of the primary Robot class (not to be confused with the Main class). This is the class that extends one of the WPILib `RobotBase` classes.*/
        var robotClassSimpleName: String = DEFAULT_ROBOT_CLASS_NAME,  // if/whn we make settable, we need to change the template copying to rename the file!
        var basePackage: String = DEFAULT_BASE_PACKAGE,
        var wpilibVersion: WpiLibVersion = WpiLibVersionImpl.parse("2019.4.1"), // TODO Need to set the default dynamically. See notes in FrcTemplateSelectionWizardStep.updateDataModel() 
        var gradleDistributionUrl: String = "https\\://services.gradle.org/distributions/gradle-5.0-bin.zip",
        var frcYear: String = wpilibVersion.major.toString(),
        var frcWizardTemplateDefinition: FrcWizardTemplateDefinition = FrcWizardProjectTemplateDefinition.CommandBased,
        var includeVsCodeConfigs: Boolean = true,
        var doDirectGradleImport: Boolean = true  // Possible put in a default value the application settings
                          )
{
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
}