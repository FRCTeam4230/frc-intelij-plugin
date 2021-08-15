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

package net.javaru.iip.frc.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import net.javaru.iip.frc.wpilib.getTeamNumberConfiguredInWpiLibPreferencesFileAsBackgroundTask
import java.util.*

/**
 * A service that manages the configured Project team number, which is specified in the 
 * `wpilib_preferences.json` file. The service loads the team number from that preferences
 * file, and then monitors it for a change. In the event of a change, all registered 
 * change listeners are notified of the change.
 * 
 * Components can listen for changes to the Project Team Number by subscribing.
 * See [FrcProjectTeamNumberChangeListener] for more information and example
 * code.
 */
class FrcProjectTeamNumberService private constructor(val project:Project)
{
    private val logger = logger<FrcProjectTeamNumberService>()
    
    var teamNumber = FrcApplicationSettings.getInstance().teamNumber // default value, but then is set in the init block in the event it is different
        private set
                
    
    init
    {
        logger.debug {"[FRC] Initializing FrcProjectTeamNumberService for project $project"}
        project.getTeamNumberConfiguredInWpiLibPreferencesFileAsBackgroundTask { foundTeamNumber -> 
            teamNumber = foundTeamNumber
        }
        // Examples: com/intellij/openapi/externalSystem/service/project/manage/SourceFolderManagerImpl.kt:115
        //           schemeManager/SchemeManagerFactoryImpl.kt:133  along with  com.intellij.configurationStore.schemeManager.SchemeFileTracker
        // As noted in https://www.jetbrains.org/intellij/sdk/docs/basics/virtual_file_system.html#virtual-file-system-events
        //      "VFS listeners are application level and will receive events for changes happening in all the projects opened by the user. 
        //       You may need to filter out events that aren't relevant to your task (e.g., via ProjectFileIndex#isInContent())."
        // TODO: Commenting this out as a temp fix for issue #60, but need to resolve (keep in mind the line number in the stacktrace in the issue are off by 1 with this comment present
//        project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener{
//            override fun after(events: List<VFileEvent>)
//            {
//                events.forEach { event: VFileEvent ->
//                    val file = event.file
//                    if (file != null && ProjectFileIndex.getInstance(project).isInContent(file) && file.name == wpiLibPreferencesFileName)
//                    {
//                        val previousTeamNumber = teamNumber
//                        teamNumber = project.getTeamNumberConfiguredInWpiLibPreferencesFile()
//                        // teamNumberChangeDispatcher.multicaster.onTeamNumberChange(previousTeamNumber, teamNumber)
//                        project.messageBus.syncPublisher(PROJECT_TEAM_NUMBER_CHANGES).onTeamNumberChange(project, previousTeamNumber, teamNumber)
//                    }
//                }
//            }
//        })
    }
    
    companion object
    {
        @JvmStatic
        fun getInstance(project: Project) = project.service<FrcProjectTeamNumberService>()

        @JvmStatic
        val PROJECT_TEAM_NUMBER_CHANGES: Topic<FrcProjectTeamNumberChangeListener> = Topic("FRC Project Team Number Changes", FrcProjectTeamNumberChangeListener::class.java) 
    }
    
    
}



/**
 * A listener for Changed to the Project Team Number, typically caused by the user editing the `wpilib_preferences.json` file.
 * 
 * To register this listener:
 * ```
 *  // JAVA
 *  project.getMessageBus().connect().subscribe(FrcProjectTeamNumberService.getPROJECT_TEAM_NUMBER_CHANGES(), listener);
 *  // or in a disposable
 *  project.getMessageBus().connect(disposable).subscribe(FrcProjectTeamNumberService.getPROJECT_TEAM_NUMBER_CHANGES(), listener);
 *  
 *  // KOTLIN
 *  project.messageBus.connect().subscribe(FrcProjectTeamNumberService.PROJECT_TEAM_NUMBER_CHANGES, listener)
 * ```
 */
interface FrcProjectTeamNumberChangeListener: EventListener
{
    fun onTeamNumberChange(project: Project, previousTeamNumber: Int, newTeamNumber: Int)
}

/**
 * Returns the team number for a project as configured in the "wpilib_preferences.json" file, or the
 * application settings in the event the preferences file is not available, or properly configured.
 */
fun Project.getProjectTeamNumber():Int = FrcProjectTeamNumberService.getInstance(this).teamNumber