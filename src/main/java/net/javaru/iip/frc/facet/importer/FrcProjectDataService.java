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

package net.javaru.iip.frc.facet.importer;

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.externalSystem.JavaProjectData;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.externalSystem.model.DataNode;
import com.intellij.openapi.externalSystem.model.Key;
import com.intellij.openapi.externalSystem.model.project.ProjectData;
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider;
import com.intellij.openapi.externalSystem.service.project.manage.AbstractProjectDataService;
import com.intellij.openapi.project.Project;



public class FrcProjectDataService extends AbstractProjectDataService<JavaProjectData, Project> // implements ProjectDataService<JavaProjectData, Project>
{
    private static final Logger LOG = Logger.getInstance(FrcProjectDataService.class);
    
    /**
     * @return key of project data supported by the current manager
     */
    @NotNull
    @Override
    public Key<JavaProjectData> getTargetDataKey()
    {
        return JavaProjectData.KEY;
    }
    
    
    /**
     * It's assumed that given data nodes present at the ide when this method returns. I.e. the method should behave as below for
     * every of the given data nodes:
     * <pre>
     * <ul>
     *   <li>there is an existing project entity for the given data node and it has the same state. Do nothing for it then;</li>
     *   <li>
     *     there is an existing project entity for the given data node but it has different state (e.g. a module dependency
     *     is configured as 'exported' at the ide but not at external system). Reset the state to the external system's one then;
     *   </li>
     *   <li> there is no corresponding project entity at the ide side. Create it then; </li>
     * </ul>
     * </pre>
     * are created, updated or left as-is if they have the
     *  @param toImport
     *
     * @param projectData
     * @param project
     * @param modelsProvider
     */
    @Override
    public void importData(@NotNull Collection<? extends DataNode<JavaProjectData>> toImport,
                           @Nullable ProjectData projectData,
                           @NotNull Project project,
                           @NotNull IdeModifiableModelsProvider modelsProvider)
    {
        
        LOG.debug("[FRC] importData called");
    }
    
    
//    /**
//     * Compute orphan data.
//     *
//     * @param toImport
//     * @param projectData
//     * @param project
//     * @param modelsProvider
//     */
//    @NotNull
//    @Override
//    public Computable<Collection<Project>> computeOrphanData(@NotNull Collection<DataNode<JavaProjectData>> toImport,
//                                                             @NotNull ProjectData projectData,
//                                                             @NotNull Project project,
//                                                             @NotNull IdeModifiableModelsProvider modelsProvider)
//    {
//        //TODO: Write this 'computeOrphanData' implemented method in the 'FrcProjectDataService' class
//        throw new IllegalStateException("The 'computeOrphanData' method in the 'FrcProjectDataService' class is not yet implemented.");
//        //return null;
//    }
//    
//    
//    /**
//     * Asks to remove all given ide project entities.
//     * <p/>
//     * <b>Note:</b> as more than one {@link ProjectDataService} might be configured for a target entity type, there is a possible case
//     * that the entities have already been removed when this method is called. Then it's necessary to cleanup auxiliary data (if any)
//     * or just return otherwise.
//     *
//     * @param toRemove       project entities to remove
//     * @param toIgnore
//     * @param projectData
//     * @param project        target project
//     * @param modelsProvider
//     */
//    @Override
//    public void removeData(@NotNull Computable<Collection<Project>> toRemove,
//                           @NotNull Collection<DataNode<JavaProjectData>> toIgnore,
//                           @NotNull ProjectData projectData,
//                           @NotNull Project project,
//                           @NotNull IdeModifiableModelsProvider modelsProvider)
//    {
//        //TODO: Write this 'removeData' implemented method in the 'FrcProjectDataService' class
//        throw new IllegalStateException("The 'removeData' method in the 'FrcProjectDataService' class is not yet implemented.");
//        
//    }
}
