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

package net.javaru.iip.frc.wpilib;

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

import static net.javaru.iip.frc.util.FindClassUtils.isLibraryPresent;



public class WpiLibLibrariesUtils
{
    private static final Logger LOG = Logger.getInstance(WpiLibLibrariesUtils.class);
    
    // Network tables was completely rewritten for 2018. Jar changed from NetworkTables.jar to ntcore.jar
    public static final String NETWORK_TABLES_PRE_2018_CLASS_1 = "edu.wpi.first.wpilibj.networktables.NetworkTable";
    public static final String NETWORK_TABLES_PRE_2018_CLASS_2 = "edu.wpi.first.wpilibj.tables.ITable";
    public static final String NETWORK_TABLES_CLASS_1 = "edu.wpi.first.networktables.NetworkTable";
    public static final String NETWORK_TABLES_CLASS_2 = "edu.wpi.first.networktables.TableListener";


    private static boolean isWpilibPresent(@NotNull Project project)
    {
        return isLibraryPresent(project, WpiLibConstants.ROBOT_BASE_FQN) ||
               isLibraryPresent(project, WpiLibConstants.ITERATIVE_ROBOT_FQN) ||
               isLibraryPresent(project, WpiLibConstants.VERSION_CLASS_FQN);
    }


    public static boolean isWpilibPresentViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isWpilibPresent(project));
    }


    private static boolean isCsCorePresent(@NotNull Project project)
    {
        return isLibraryPresent(project, "edu.wpi.cscore.VideoCamera") ||
               isLibraryPresent(project, "edu.wpi.cscore.CameraServerJNI");
    }


    public static boolean isCsCorePresentViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isCsCorePresent(project));
    }


    private static boolean isNetworkTablesPresent(@NotNull Project project)
    {
        return isLibraryPresent(project, NETWORK_TABLES_CLASS_1) ||
               isLibraryPresent(project, NETWORK_TABLES_CLASS_2) ||
               isLibraryPresent(project, NETWORK_TABLES_PRE_2018_CLASS_1) ||
               isLibraryPresent(project, NETWORK_TABLES_PRE_2018_CLASS_2);
    }


    public static boolean isNetworkTablesPresentViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isNetworkTablesPresent(project));
    }

    private static boolean isWpiUtilsPresent(@NotNull Project project)
    {
        // wpiutil.jar was added in v2018 and has only a single class
        return isLibraryPresent(project, "du.wpi.first.wpiutil.RuntimeDetector");
    }


    public static boolean isWpiUtilsPresentViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isWpiUtilsPresent(project));
    }


    private static boolean isOpenCvPresent(@NotNull Project project)
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


    private static boolean areAllPresent(@NotNull Project project)
    {
        final boolean wpilibPresent = isWpilibPresent(project);
        final boolean networkTablesPresent = isNetworkTablesPresent(project);
        final boolean openCvPresent = isOpenCvPresent(project);
        final boolean csCorePresent = isCsCorePresent(project);
        LOG.debug("On areAllPresent check: wpilibPresent=" + wpilibPresent + "; networkTablesPresent=" 
                  + networkTablesPresent + "; openCvPresent=" + openCvPresent +  "; csCorePresent=" + csCorePresent );
        return wpilibPresent && networkTablesPresent && openCvPresent && csCorePresent;
    }


    public static boolean areAllPresentViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> areAllPresent(project));
    }


    private static boolean isWpilibPresent(@NotNull Module module)
    {
        return isLibraryPresent(module, WpiLibConstants.ROBOT_BASE_FQN) ||
               isLibraryPresent(module, WpiLibConstants.ITERATIVE_ROBOT_FQN);
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
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) WpiLibLibrariesUtils::isWpilibInstalledOnSystem);
    }


    private static boolean isCsCorePresent(@NotNull Module module)
    {
        return isLibraryPresent(module, "edu.wpi.cscore.VideoCamera") ||
               isLibraryPresent(module, "edu.wpi.cscore.CameraServerJNI");
    }


    public static boolean isCsCorePresentViaReadAction(@NotNull Module module)
    {
        return DumbService.getInstance(module.getProject()).runReadActionInSmartMode(() -> isCsCorePresent(module));
    }


    private static boolean isNetworkTablesPresent(@NotNull Module module)
    {
        return isLibraryPresent(module, NETWORK_TABLES_CLASS_1) ||
               isLibraryPresent(module, NETWORK_TABLES_CLASS_2) ||
               isLibraryPresent(module, NETWORK_TABLES_PRE_2018_CLASS_1) ||
               isLibraryPresent(module, NETWORK_TABLES_PRE_2018_CLASS_2);
    }


    public static boolean isNetworkTablesPresentReadAction(@NotNull Module module)
    {
        return DumbService.getInstance(module.getProject()).runReadActionInSmartMode(() -> isNetworkTablesPresent(module));
    }


    private static boolean isOpenCvPresent(@NotNull Module module)
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


    private static boolean isWpiUtilPresent(@NotNull Module module)
    {
        // wpiutil.jar was added in v2018 and has only a single class
        return isLibraryPresent(module, "du.wpi.first.wpiutil.RuntimeDetector") ;
    }


    public static boolean isWpiUtilPresentReadAction(@NotNull Module module)
    {
        return DumbService.getInstance(module.getProject()).runReadActionInSmartMode(() -> isWpiUtilPresent(module));
    }

    private static boolean areAllPresent(@NotNull Module module)
    {
        // wpiutil.jar was added in v2018 and has only a single class - not check on it for now
        return isWpilibPresent(module) && isNetworkTablesPresent(module) && isOpenCvPresent(module) && isCsCorePresent(module);
    }


    public static boolean areAllPresentViaReadAction(@NotNull Module module)
    {
        return DumbService.getInstance(module.getProject()).runReadActionInSmartMode(() -> areAllPresent(module));
    }


    @Nullable
    public static Library findExistingWpilibJavaLibDirLibrary(@NotNull Module module)
    {
        final Path libDir = WpiLibPaths.getJavaLibDir();
        return findExistingDirBasedLibrary(module, libDir);
    }
    
    private static boolean isWpilibJavaLibDirAttached(@NotNull Project project)
    {
        final Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules)
        {
            if (findExistingWpilibJavaLibDirLibrary(module) != null)
            {
                return true;
            }
        }
        return false;
    }


    public static boolean isWpilibJavaLibDirAttachedViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isWpilibJavaLibDirAttached(project));
    }

    @Nullable
    public static Library findExistingUserLibDirLibrary(@NotNull Module module)
    {
        final Path userLibDir = WpiLibPaths.getUserLibDir();
        return findExistingDirBasedLibrary(module, userLibDir);
    }


    @Nullable
    public static Library findExistingDirBasedLibrary(@NotNull Module module, Path libDir)
    {
        // get the libraries for the module
        final LibraryTable libraryTable = ModuleRootManager.getInstance(module).getModifiableModel().getModuleLibraryTable();
        final Library[] libraries = libraryTable.getLibraries();
        for (Library library : libraries)
        {
            final String dirUrl = VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, libDir.toString());

            //Not sure why, but when testing, I had to replace back slashes as the dirUrl was file://C:\foo\bar which was not found, but file://C:/foo/bar was
            if (library.isJarDirectory(dirUrl.replace('\\', '/')) || library.isJarDirectory(dirUrl))
            {
                return library;
            }

            final VirtualFile[] libraryFiles = library.getFiles(OrderRootType.CLASSES);

            for (VirtualFile virtualFile : libraryFiles)
            {
                Path dir = Paths.get(virtualFile.getPresentableUrl());
                if (libDir.equals(dir) || dir.startsWith(libDir))
                {
                    return library;
                }
            }
        }

        return null;
    }


    private static boolean isUserLibAttached(@NotNull Project project)
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


    private static boolean isUserLibNonEmptyAndNotAttached(@NotNull Project project)
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
