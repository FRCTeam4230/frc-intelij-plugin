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

package net.javaru.iip.frc.settings

import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.messages.Topic
import net.javaru.iip.frc.wpilib.getConfiguredTeamNumber
import net.javaru.iip.frc.wpilib.wpiLibPreferencesFileName
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
    private val LOG = Logger.getInstance(FrcProjectTeamNumberService::class.java)
    
    
    
    var teamNumber = UN_CONFIGURED_TEAM_NUMBER // default value, but then is set in the init block
        private set
                
    
    init
    {
        LOG.debug("[FRC] Initializing FrcProjectTeamNumberService for project $project")
        teamNumber = getConfiguredTeamNumber(project)
        
        // Examples: com/intellij/openapi/externalSystem/service/project/manage/SourceFolderManagerImpl.kt:115
        //           schemeManager/SchemeManagerFactoryImpl.kt:133  along with  com.intellij.configurationStore.schemeManager.SchemeFileTracker
        // As noted in https://www.jetbrains.org/intellij/sdk/docs/basics/virtual_file_system.html#virtual-file-system-events
        //      "VFS listeners are application level and will receive events for changes happening in all the projects opened by the user. 
        //       You may need to filter out events that aren't relevant to your task (e.g., via ProjectFileIndex#isInContent())."
        project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener{
            override fun after(events: List<VFileEvent>)
            {
                events.forEach { event: VFileEvent ->
                    val file = event.file
                    if (file != null && ProjectFileIndex.getInstance(project).isInContent(file) && file.name == wpiLibPreferencesFileName)
                    {
                        val previousTeamNumber = teamNumber
                        teamNumber = getConfiguredTeamNumber(project)
                        // teamNumberChangeDispatcher.multicaster.onTeamNumberChange(previousTeamNumber, teamNumber)
                        project.messageBus.syncPublisher(PROJECT_TEAM_NUMBER_CHANGES).onTeamNumberChange(project, previousTeamNumber, teamNumber)
                    }
                }
            }
        })
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