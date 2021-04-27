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

package net.javaru.iip.frc.util;

import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.indexing.FileBasedIndexProjectHandler;



public class IndexUtils
{
    private static final Logger LOG = Logger.getInstance(IndexUtils.class);


    /**
     * Calls, in sequence:
     * <ol>
     *     <li>{@link FileBasedIndexProjectHandler#scheduleReindexingInDumbMode(Project)}  (if project is not null)</li>
     *     <li>{@link #refreshFileSystem()}</li>
     *     <li>{@link #refreshVirtualFileSystem()}</li>
     *     
     * </ol>
     * @param project the project to refresh the project indexes for
     */
    public static void refreshAll(@Nullable Project project)
    {
        if (project != null)
        {
            // Refresh project indexes
            // See:  com.intellij.openapi.roots.impl.PushedFilePropertiesUpdaterImpl.scheduleDumbModeReindexingIfNeeded()
            //       com.intellij.openapi.roots.impl.ProjectRootManagerComponent.doUpdateOnRefresh()
            // Although those examples used the now deprecated createChangedFilesIndexingTask() and then queued the task in the DumbService
            // As of 2021.1, there is the new scheduleReindexingInDumbMode() method that does all that for you
            FileBasedIndexProjectHandler.scheduleReindexingInDumbMode(project);
        }
        refreshFileSystem();
        refreshVirtualFileSystem();
    }


    public static void refreshVirtualFileSystem()
    {
        LOG.debug("[FRC] Refreshing virtual files");
        VirtualFileManager.getInstance().asyncRefresh(null);
    }
    
    public static void refreshFileSystem()
    {
        LOG.debug("[FRC] Refreshing file System");
        LocalFileSystem.getInstance().refresh(true);  //TODO look at the See also in the Javadoc
    }
}
