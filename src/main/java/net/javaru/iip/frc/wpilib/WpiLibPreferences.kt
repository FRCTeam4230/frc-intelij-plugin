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

import com.intellij.openapi.project.Project
import net.javaru.iip.frc.settings.FrcApplicationSettings
import java.time.temporal.ChronoUnit

//A Data class representation of the WpiLibPreferences JSON file
data class WpiLibPreferences(var enableCppIntellisense: Boolean = false,
                             var currentLanguage: String = "Java",
                             var projectYear: Int = java.time.LocalDateTime.now().plus(15, ChronoUnit.DAYS).year, /* For initialization, we assume from ~ December 15 on we want to use the next year*/
                             var teamNumber: Int = FrcApplicationSettings.getInstance().teamNumber
                             )


fun readWpiLibPreferences(project: Project): WpiLibPreferences
{
//    if (project == ProjectManager.getInstance().defaultProject)
//    {
        return WpiLibPreferences()
//    }
//    
//    ApplicationManager.getApplication().runReadAction {
//        var basePath: Path? = null;
//        val basePathString = project.basePath
//        
//        if (basePathString != null)
//        {
//            basePath = Paths.get(basePathString)
//        }
//        
//        if (basePath == null || !basePath.exists())
//        {
//            val projectDir = project.guessProjectDir()
//            if (projectDir != null)
//        }
//    }
}