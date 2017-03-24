/*
 * Copyright 2015-2017 Mark Vedder
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.javaru.iip.frc.wpilib.attached;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import net.javaru.iip.frc.util.FrcFileUtils;
import net.javaru.iip.frc.wpilib.WpiLibPaths;

import static net.javaru.iip.frc.util.FindClassUtils.isLibraryPresent;



public class WpiLibrariesUtils
{

    private static final Logger LOG = Logger.getInstance(WpiLibrariesUtils.class);


    public static boolean isWpilibPresent(@NotNull Project project)
    {
        return isLibraryPresent(project, WpilibConstants.ROBOT_BASE_FQN) ||
               isLibraryPresent(project, WpilibConstants.ITERATIVE_ROBOT_FQN);
    }


    public static boolean isWpilibPresentViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isWpilibPresent(project));
    }


    public static boolean isCsCorePresent(@NotNull Project project)
    {
        return isLibraryPresent(project, "edu.wpi.cscore.VideoCamera") ||
               isLibraryPresent(project, "edu.wpi.cscore.CameraServerJNI");
    }


    public static boolean isCsCorePresentViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isCsCorePresent(project));
    }


    public static boolean isNetworkTablesPresent(@NotNull Project project)
    {
        return isLibraryPresent(project, "edu.wpi.first.wpilibj.networktables.NetworkTable") ||
               isLibraryPresent(project, "edu.wpi.first.wpilibj.tables.ITable");
    }


    public static boolean isNetworkTablesPresentViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isNetworkTablesPresent(project));
    }


    public static boolean isOpenCvPresent(@NotNull Project project)
    {
        return isLibraryPresent(project, "org.opencv.core.Core") ||
               isLibraryPresent(project, "org.opencv.video.Video") ||
               isLibraryPresent(project, "org.opencv.videoio.VideoCapture") ||
               isLibraryPresent(project, "org.opencv.objdetect.Objdetect");
    }


    public static boolean isOpenCvPresentViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isOpenCvPresent(project));
    }


    public static boolean areAllPresent(@NotNull Project project)
    {
        return isWpilibPresent(project) && isNetworkTablesPresent(project) && isOpenCvPresent(project) && isCsCorePresent(project);
    }


    public static boolean areAllPresentViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> areAllPresent(project));
    }


    public static boolean isWpilibPresent(@NotNull Module module)
    {
        return isLibraryPresent(module, WpilibConstants.ROBOT_BASE_FQN) ||
               isLibraryPresent(module, WpilibConstants.ITERATIVE_ROBOT_FQN);
    }


    public static boolean isWpilibPresentViaReadAction(@NotNull Module module)
    {
        return DumbService.getInstance(module.getProject()).runReadActionInSmartMode(() -> isWpilibPresent(module));
    }


    public static boolean isWpilibInstalledOnSystem()
    {
        try
        {
            return FrcFileUtils.directoryHasJars(WpiLibPaths.getJavaLibDir(), true);
        }
        catch (IOException e)
        {
            return false;
        }
    }


    public static boolean isWpilibInstalledOnSystemViaReadAction()
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) WpiLibrariesUtils::isWpilibInstalledOnSystem);
    }


    public static boolean isCsCorePresent(@NotNull Module module)
    {
        return isLibraryPresent(module, "edu.wpi.cscore.VideoCamera") ||
               isLibraryPresent(module, "edu.wpi.cscore.CameraServerJNI");
    }


    public static boolean isCsCorePresentViaReadAction(@NotNull Module module)
    {
        return DumbService.getInstance(module.getProject()).runReadActionInSmartMode(() -> isCsCorePresent(module));
    }


    public static boolean isNetworkTablesPresent(@NotNull Module module)
    {
        return isLibraryPresent(module, "edu.wpi.first.wpilibj.networktables.NetworkTable") ||
               isLibraryPresent(module, "edu.wpi.first.wpilibj.tables.ITable");
    }


    public static boolean isNetworkTablesPresentReadAction(@NotNull Module module)
    {
        return DumbService.getInstance(module.getProject()).runReadActionInSmartMode(() -> isNetworkTablesPresent(module));
    }


    public static boolean isOpenCvPresent(@NotNull Module module)
    {
        return isLibraryPresent(module, "org.opencv.core.Core") ||
               isLibraryPresent(module, "org.opencv.video.Video") ||
               isLibraryPresent(module, "org.opencv.videoio.VideoCapture") ||
               isLibraryPresent(module, "org.opencv.objdetect.Objdetect");
    }


    public static boolean isOpenCvPresentViaReadAction(@NotNull Module module)
    {
        return DumbService.getInstance(module.getProject()).runReadActionInSmartMode(() -> isOpenCvPresent(module));
    }


    public static boolean areAllPresent(@NotNull Module module)
    {
        return isWpilibPresent(module) && isNetworkTablesPresent(module) && isOpenCvPresent(module) && isCsCorePresent(module);
    }


    public static boolean areAllPresentViaReadAction(@NotNull Module module)
    {
        return DumbService.getInstance(module.getProject()).runReadActionInSmartMode(() -> areAllPresent(module));
    }


    @Nullable
    public static Library findExistingUserLibDirLibrary(@NotNull Module module)
    {
        final Path userLibDir = WpiLibPaths.getUserLibDir();

        // get the libraries on which it depends
        final LibraryTable libraryTable = ModuleRootManager.getInstance(module).getModifiableModel().getModuleLibraryTable();
        final Library[] libraries = libraryTable.getLibraries();
        for (Library library : libraries)
        {
            final String dirUrl = VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, userLibDir.toString());

            //Not sure why, but when testing, I had to replace back slashes as the dirUrl was file://C:\foo\bar which was not found, but file://C:/foo/bar was
            if (library.isJarDirectory(dirUrl.replace('\\', '/')) || library.isJarDirectory(dirUrl))
            {
                return library;
            }

            final VirtualFile[] libraryFiles = library.getFiles(OrderRootType.CLASSES);

            for (VirtualFile virtualFile : libraryFiles)
            {
                Path dir = Paths.get(virtualFile.getPresentableUrl());
                if (userLibDir.equals(dir) || dir.startsWith(userLibDir))
                {
                    return library;
                }
            }
        }

        return null;
    }


    public static boolean isUserLibAttached(@NotNull Project project)
    {
        final Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules)
        {
            if (findExistingUserLibDirLibrary(module) != null)
            {
                return true;
            }
        }
        return false;
    }


    public static boolean isUserLibAttachedViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isUserLibAttached(project));
    }


    public static boolean isUserLibNonEmptyAndNotAttached(@NotNull Project project)
    {
        try
        {
            return FrcFileUtils.directoryHasJars(WpiLibPaths.getUserLibDir(), true) && !isUserLibAttached(project);
        }
        catch (IOException e)
        {
            final String message = "An IOException occurred when checkin for user lib attachment. Cause Summary: " + e.toString();
            LOG.warn(message);
            throw new IllegalStateException(message, e);
        }
    }


    public static boolean isUserLibNonEmptyAndAttachedViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isUserLibNonEmptyAndNotAttached(project));
    }
}
