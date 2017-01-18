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

import java.nio.file.Path;
import java.nio.file.Paths;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;

import net.javaru.iip.frc.wpilib.WpiLibPaths;

import static net.javaru.iip.frc.util.FindClassUtils.isLibraryPresent;



public class WpiLibrariesUtils
{

    private static final Logger LOG = Logger.getInstance(WpiLibrariesUtils.class);
    
    @Contract("null -> false")
    public static boolean isWpilibPresent(@Nullable Project project)
    {
        return isLibraryPresent(project, WpilibConstants.ROBOT_BASE_FQN) ||
               isLibraryPresent(project, WpilibConstants.ITERATIVE_ROBOT_FQN);
    }

    @Contract("null -> false")
    public static boolean isWpilibPresentViaReadAction(@Nullable Project project)
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> isWpilibPresent(project));
    }


    @Contract("null -> false")
    public static boolean isCsCorePresent(@Nullable Project project)
    {
        return isLibraryPresent(project, "edu.wpi.cscore.VideoCamera") ||
               isLibraryPresent(project, "edu.wpi.cscore.CameraServerJNI");
    }


    @Contract("null -> false")
    public static boolean isCsCorePresentViaReadAction(@Nullable Project project)
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> isCsCorePresent(project));
    }

    @Contract("null -> false")
    public static boolean isNetworkTablesPresent(@Nullable Project project)
    {
        return isLibraryPresent(project, "edu.wpi.first.wpilibj.networktables.NetworkTable") ||
               isLibraryPresent(project, "edu.wpi.first.wpilibj.tables.ITable");
    }


    @Contract("null -> false")
    public static boolean sNetworkTablesPresentViaReadAction(@Nullable Project project)
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> isNetworkTablesPresent(project));
    }

    @Contract("null -> false")
    public static boolean isOpenCvPresent(@Nullable Project project)
    {
        return isLibraryPresent(project, "org.opencv.core.Core") ||
               isLibraryPresent(project, "org.opencv.video.Video") ||
               isLibraryPresent(project, "org.opencv.videoio.VideoCapture") ||
               isLibraryPresent(project, "org.opencv.objdetect.Objdetect");
    }


    @Contract("null -> false")
    public static boolean isOpenCvPresentViaReadAction(@Nullable Project project)
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> isOpenCvPresent(project));
    }

    public static boolean areAllPresent(@Nullable Project project)
    {
        return isWpilibPresent(project) && isNetworkTablesPresent(project) && isOpenCvPresent(project) && isCsCorePresent(project);
    }


    @Contract("null -> false")
    public static boolean areAllPresentViaReadAction(@Nullable Project project)
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> areAllPresent(project));
    }

    @Contract("null -> false")
    public static boolean isWpilibPresent(@Nullable Module module)
    {
        return isLibraryPresent(module, WpilibConstants.ROBOT_BASE_FQN) ||
               isLibraryPresent(module, WpilibConstants.ITERATIVE_ROBOT_FQN);
    }


    @Contract("null -> false")
    public static boolean isWpilibPresentViaReadAction(@Nullable Module module)
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> isWpilibPresent(module));
    }

    @Contract("null -> false")
    public static boolean isCsCorePresent(@Nullable Module module)
    {
        return isLibraryPresent(module, "edu.wpi.cscore.VideoCamera") ||
               isLibraryPresent(module, "edu.wpi.cscore.CameraServerJNI");
    }


    @Contract("null -> false")
    public static boolean isCsCorePresentViaReadAction(@Nullable Module module)
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> isCsCorePresent(module));
    }

    @Contract("null -> false")
    public static boolean isNetworkTablesPresent(@Nullable Module module)
    {
        return isLibraryPresent(module, "edu.wpi.first.wpilibj.networktables.NetworkTable") ||
               isLibraryPresent(module, "edu.wpi.first.wpilibj.tables.ITable");
    }


    @Contract("null -> false")
    public static boolean isNetworkTablesPresentReadAction(@Nullable Module module)
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> isNetworkTablesPresent(module));
    }

    @Contract("null -> false")
    public static boolean isOpenCvPresent(@Nullable Module module)
    {
        return isLibraryPresent(module, "org.opencv.core.Core") ||
               isLibraryPresent(module, "org.opencv.video.Video") ||
               isLibraryPresent(module, "org.opencv.videoio.VideoCapture") ||
               isLibraryPresent(module, "org.opencv.objdetect.Objdetect");
    }


    @Contract("null -> false")
    public static boolean isOpenCvPresentViaReadAction(@Nullable Module module)
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> isOpenCvPresent(module));
    }

    public static boolean areAllPresent(@Nullable Module module)
    {
        return isWpilibPresent(module) && isNetworkTablesPresent(module) && isOpenCvPresent(module) && isCsCorePresent(module);
    }


    @Contract("null -> false")
    public static boolean areAllPresentViaReadAction(@Nullable Module module)
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) () -> areAllPresent(module));
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
}
