/*
 * Copyright 2015-2018 the original author or authors
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

package net.javaru.iip.frc.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbModeTask;
import com.intellij.openapi.project.DumbServiceImpl;
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
     *     <li>{@link #refreshProjectIndexes(Project)}  (if project is not null</li>
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
            refreshProjectIndexes(project);
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


    public static void refreshProjectIndexes(@NotNull Project project)
    {
        // See:  com.intellij.openapi.roots.impl.PushedFilePropertiesUpdaterImpl.scheduleDumbModeReindexingIfNeeded()
        //       com.intellij.openapi.roots.impl.ProjectRootManagerComponent.doUpdateOnRefresh()
        DumbServiceImpl dumbService = DumbServiceImpl.getInstance(project);
        DumbModeTask task = FileBasedIndexProjectHandler.createChangedFilesIndexingTask(project);
        if (task != null)
        {
            LOG.debug("[FRC] Queuing up Reindexing task");
            dumbService.queueTask(task);
        }
    }
}
