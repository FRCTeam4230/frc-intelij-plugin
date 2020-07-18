/*
 * Copyright 2015-2020 the original author or authors
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

package net.javaru.iip.frc.actions.tools.legacy;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.facet.FacetManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task.Backgroundable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import net.javaru.iip.frc.actions.tools.AbstractFrcToolsAction;
import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.facet.FrcFacetKt;
import net.javaru.iip.frc.notify.FrcNotificationType;
import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.util.FrcFileUtils;
import net.javaru.iip.frc.util.FrcProjectExtsKt;
import net.javaru.iip.frc.util.IndexUtils;
import net.javaru.iip.frc.util.LibDef;
import net.javaru.iip.frc.util.LibDefBuilder;
import net.javaru.iip.frc.util.LibDirType;
import net.javaru.iip.frc.util.LibraryUtilsKt;
import net.javaru.iip.frc.wpilib.legacy.LegacyWpiLibLibrariesUtils;
import net.javaru.iip.frc.wpilib.legacy.LegacyWpiLibPaths;
import net.javaru.iip.frc.wpilib.legacy.retrieval.LegacyWpiLibDownloader;




public class AttachLegacyWpilibAction extends AbstractFrcToolsAction
{
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getInstance(AttachLegacyWpilibAction.class);


    @Override
    public void update(AnActionEvent e)
    {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        e.getPresentation().setVisible(project != null &&
                                       !project.isDisposed() &&
                                       FrcFacetKt.isFrcFacetedProject(project) &&
                                       !FrcProjectExtsKt.isGradleProject(project)  && /* TODO: Need to reverse this and check if it is an Ant Based Project once the isAntBasedFrcProject method is implemented*/
                                       !LegacyWpiLibLibrariesUtils.isLegacyWpilibAttachedViaReadAction(project));
    }


    @Override
    public void actionPerformed(AnActionEvent actionEvent)
    {
        final Project project = actionEvent.getProject();
        attachWpiLib(project, true, true);
    }


    public static void attachWpiLib(Project project, final boolean notifyOnCompletion, boolean doReindex)
    {
        if (project != null)
        {
            try
            {
                final Module[] modules = ModuleManager.getInstance(project).getModules();
                for (Module module : modules)
                {
                    final FacetManager facetManager = FacetManager.getInstance(module);
                    final FrcFacet frcFacet = facetManager.getFacetByType(FrcFacet.Companion.getFACET_TYPE_ID());
                    if (frcFacet != null)
                    {
                        //TODO: need to see if it is present as a Project library, and if so, attach that
                        if (LegacyWpiLibLibrariesUtils.isLegacyWpilibAttachedViaReadAction(module))
                        {
                            FrcNotifications.notify(FrcNotificationType.GENERAL_WARN,
                                                    "WPILib is already attached as a library",
                                                    "WPILib",
                                                    project);
                            return;
                        }


                        final Path wpiJavaLibDir = LegacyWpiLibPaths.getJavaLibDir();
                        try
                        {
                            Files.createDirectories(wpiJavaLibDir);
                        }
                        catch (IOException e)
                        {
                            LOG.debug("[FRC] Could not create wpiJavaDir. Cause summary: " + e.toString(), e);
                        }

                        if (!(Files.isDirectory(wpiJavaLibDir) && FrcFileUtils.directoryHasJars(wpiJavaLibDir, true)))
                        {
                            final int response = Messages.showYesNoCancelDialog(project,
                                                                                "The WPILib JARs were not found on your system. How do you wish to proceed?",
                                                                                "WPILib Not Found",
                                                                                "Download and Attach",
                                                                                "Attach Empty Directory",
                                                                                "Cancel Without Downloading or Attaching",
                                                                                Messages.getQuestionIcon());
                            if (response == Messages.YES)
                            {
                                new Backgroundable(project, "Downloading WPILib Update", false)
                                {
                                    @Override
                                    public void run(@NotNull ProgressIndicator indicator)
                                    {
                                        LegacyWpiLibDownloader.downloadLatest();
                                    }
                                }.queue();
                            }
                            else if (response == Messages.CANCEL)
                            {
                                return;
                            }
                        }

                        LibDef libDef = new LibDefBuilder(module, "WPILib Libraries").addDir(wpiJavaLibDir, LibDirType.BIN, LibDirType.SRC, LibDirType.DOC).build();
                        LibraryUtilsKt.attachDirectoryBasedLibrary(libDef);
                        if (notifyOnCompletion)
                        {
                            makeSuccessfulNotification(project);
                        }
                    }
                }
                if (doReindex)
                {
                    IndexUtils.refreshAll(project);
                }
            }
            catch (Exception e)
            {
                makeFailureNotification(project, e);
            }
        }
    }
    
    
    private static void makeFailureNotification(@Nullable Project project, @Nullable Exception e)
    {
        
        
        if (e != null)
        {
            LOG.info("[FRC] Failed to attach WPILib Cause: " + e.toString(), e);
        }
        
        String cause = e != null ? " Cause: " + e.toString() : "";
        FrcNotifications.notify(FrcNotificationType.GENERAL_WARN,
                                 "Could not attach WPILib JARs as a library." + cause,
                                "WPILib",
                                project);
    }
    
    
    private static void makeSuccessfulNotification(@Nullable Project project)
    {
        FrcNotifications.notify(FrcNotificationType.GENERAL_INFO,
                                "WPILib JARs have been attached as a library.",
                                "WPILib",
                                project);
    }
}