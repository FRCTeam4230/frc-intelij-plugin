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

package net.javaru.iip.frc.facet.importer

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.model.DataNode
import com.intellij.openapi.externalSystem.model.Key
import com.intellij.openapi.externalSystem.model.ProjectKeys
import com.intellij.openapi.externalSystem.model.project.LibraryDependencyData
import com.intellij.openapi.externalSystem.model.project.LibraryPathType
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.service.project.IdeModelsProvider
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.externalSystem.service.project.manage.AbstractProjectDataService
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.util.containers.ContainerUtil
import net.javaru.iip.frc.facet.getOrAddFrcFacet

// https://intellij-support.jetbrains.com/hc/en-us/community/posts/207379795/comments/207661099

// @Order(ExternalSystemConstants.UNORDERED)
class FrcDependencyDataService : AbstractProjectDataService<LibraryDependencyData, Module>()
{
    companion object
    {
        private val LOG = Logger.getInstance(FrcDependencyDataService::class.java)
    }

    override fun getTargetDataKey(): Key<LibraryDependencyData>
    {
        return ProjectKeys.LIBRARY_DEPENDENCY
    }


    override fun importData(toImport: Collection<DataNode<LibraryDependencyData>>,
                            projectData: ProjectData?,
                            project: Project,
                            modelsProvider: IdeModifiableModelsProvider)
    {
        LOG.debug("[FRC] toImport reached in FrcDependencyDataService")
        
        // Gradle: edu.wpi.first.wpilibj:wpilibj-java:2019.4.12                     <-- Has the base Robot super classes and the primary Robot classes 
        // Gradle: edu.wpi.first.wpiutil:wpiutil-java:2019.4.1                      <-- 2 Runtime based classes
        // Gradle: edu.wpi.first.hal:hal-java:2019.4.1                              <-- Hardware Abstraction Layer (HAL) that has the lower level stuff
        // Gradle: edu.wpi.first.cscore:cscore-java:2019.4.1
        // Gradle: edu.wpi.first.cameraserver:cameraserver-java:2019.4.1
        // Gradle: edu.wpi.first.thirdparty.frc2019.opencv:opencv-java:3.4.4-4
        // Gradle: edu.wpi.first.ntcore:ntcore-java:2019.4.1
        
        
//        toImport.forEach { dataNode: DataNode<LibraryDependencyData> ->
//            val externalName = dataNode.data.externalName
//            if (externalName.contains("edu.wpi.first.wpilibj:wpilibj"))
//            {
//                val paths = dataNode.data.target.getPaths(LibraryPathType.BINARY)
//                val path = ContainerUtil.getFirstItem(paths)
//                if (path != null)
//                {
//                    val module = modelsProvider.findIdeModule(dataNode.data.ownerModule)
//                    module?.getOrAddFrcFacet(projectData?.owner.toString())
//                }
//            }
//        }
    }

    override fun postProcess(toImport: MutableCollection<DataNode<LibraryDependencyData>>, projectData: ProjectData?, project: Project, modelsProvider: IdeModifiableModelsProvider)
    {
        LOG.debug("[FRC] toImport reached in postProcess")
    }

    override fun onSuccessImport(imported: MutableCollection<DataNode<LibraryDependencyData>>, projectData: ProjectData?, project: Project, modelsProvider: IdeModelsProvider)
    {
        LOG.debug("[FRC] toImport reached in onSuccessImport")
        imported.forEach { dataNode: DataNode<LibraryDependencyData> ->
            val externalName = dataNode.data.externalName
            if (externalName.contains("edu.wpi.first.wpilibj:wpilibj"))
            {
                val paths = dataNode.data.target.getPaths(LibraryPathType.BINARY)
                val path = ContainerUtil.getFirstItem(paths)
                if (path != null)
                {
                    val module = modelsProvider.findIdeModule(dataNode.data.ownerModule)
                    if (module?.name?.contains(".main") == true)
                    {
                        // only add to the main module. In Gradle projects, there is a team123robot.main and team123robot.test module
                        module.getOrAddFrcFacet(projectData?.owner.toString())
                    }
                }
            }
        }
    }
}
