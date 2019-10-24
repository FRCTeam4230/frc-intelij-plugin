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

import net.javaru.iip.frc.wpilib.version.WpiLibVersion
import net.javaru.iip.frc.wpilib.version.WpiLibVersionImpl


class FrcProjectWizardData(
                    var robotMainClassFQ: String = "frc.robot.Main",
                    var wpilibVersion: WpiLibVersion = WpiLibVersionImpl.parse("2019.4.1"),
                    var gradleDistributionUrl: String = "https\\://services.gradle.org/distributions/gradle-5.0-bin.zip",
                    var frcYear: String = wpilibVersion.major.toString()
                          )
{
    
}