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


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.facet.FacetManager;
import com.intellij.notification.Notification;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.libraries.Library;

import net.javaru.iip.frc.actions.tools.AbstractFrcToolsAction;
import net.javaru.iip.frc.facet.FrcFacet;
import net.javaru.iip.frc.facet.FrcFacetKt;
import net.javaru.iip.frc.notify.FrcNotificationType;
import net.javaru.iip.frc.notify.FrcNotifications;
import net.javaru.iip.frc.util.FrcProjectExtsKt;
import net.javaru.iip.frc.util.IndexUtils;
import net.javaru.iip.frc.util.LibDef;
import net.javaru.iip.frc.util.LibDefBuilder;
import net.javaru.iip.frc.util.LibDirType;
import net.javaru.iip.frc.util.LibraryUtilsKt;
import net.javaru.iip.frc.wpilib.legacy.LegacyWpiLibLibrariesUtils;
import net.javaru.iip.frc.wpilib.legacy.LegacyWpiLibPaths;




public class AttachLegacyUserLibDirAction extends AbstractFrcToolsAction
{
    private static final Logger LOG = Logger.getInstance(AttachLegacyUserLibDirAction.class);


    @Override
    public void update(AnActionEvent e)
    {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        e.getPresentation().setVisible(project != null &&
                                       !project.isDisposed() &&
                                       FrcFacetKt.isFrcFacetedProject(project) &&
                                       !FrcProjectExtsKt.isGradleProject(project)
                                       && /* TODO: Need to reverse this and check if it is an Ant Based Project once the isAntBasedFrcProject method is implemented*/
                                       !LegacyWpiLibLibrariesUtils.isLegacyUserLibAttachedViaReadAction(project));
    }


    @Override
    public void actionPerformed(AnActionEvent actionEvent)
    {
        final Project project = actionEvent.getProject();
        if (project != null)
        {
            attachUserLib(project, true);
        }
    }


    public static void attachUserLib(@NotNull Project project, final boolean notifyOnCompletion)
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
                    final Library existing = LegacyWpiLibLibrariesUtils.findExistingUserLibDirLibrary(module);
                    if (existing != null)
                    {
                        FrcNotifications.notify(FrcNotificationType.GENERAL_INFO,
                                                 "The FRC 'user/java/lib' directory is already attached via library '" + existing.getName() + "'. Indexes are being refreshed.",
                                                "User Lib Already Attached",
                                                project);
                    }
                    else
                    {
                        final LibDef libDef = new LibDefBuilder(module, "WPILib User Lib Directory")
                                .addDir(LegacyWpiLibPaths.getUserLibDir(), LibDirType.BIN, LibDirType.SRC, LibDirType.DOC)
                                .addDir(LegacyWpiLibPaths.getUserDocsDir(), LibDirType.DOC)
                                .build();
                        LibraryUtilsKt.attachDirectoryBasedLibrary(libDef);
                        if (notifyOnCompletion)
                        {
                            queueSuccessfulNotification(project);
                        }
                    }
                    IndexUtils.refreshAll(project);
                }
            }
        }
        catch (Exception e)
        {
            queueFailureNotification(project, e);
        }
    }


    private static void queueFailureNotification(@Nullable Project project, @Nullable Exception e)
    {
        Notifications.Bus.notify(createFailureNotification(e), project);
    }


    @NotNull
    private static Notification createFailureNotification(@Nullable Exception e)
    {


        if (e != null)
        {
            LOG.info("[FRC] Failed to attach User Lib Dir: " + e.toString(), e);
        }

        String cause = e != null ? "Cause: " + e.toString() : "";
        return FrcNotifications.createNotification(FrcNotificationType.GENERAL_WARN,
                                                    "Could not attach 'user/java/lib' dir as a library." + cause,
                                                   "WPILib");
    }


    private static void queueSuccessfulNotification(@Nullable Project project)
    {
        Notifications.Bus.notify(createSuccessNotification(), project);
    }


    @NotNull
    private static Notification createSuccessNotification()
    {
        return FrcNotifications.createNotification(FrcNotificationType.GENERAL_INFO,
                                                   "The 'user/java/lib' has been attached as a library. Indexes are being refreshed.",
                                                   "WPILib");
    }
}
