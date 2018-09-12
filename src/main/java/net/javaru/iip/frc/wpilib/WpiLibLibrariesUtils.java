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

package net.javaru.iip.frc.wpilib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.impl.compiled.ClassFileDecompiler;

import net.javaru.iip.frc.i18n.FrcMessageBundle;
import net.javaru.iip.frc.util.FindClassUtils;
import net.javaru.iip.frc.util.FrcFileUtils;
import net.javaru.iip.frc.wpilib.retrieval.WpiLibDownloader;
import net.javaru.iip.frc.wpilib.version.WpiLibVersion;
import net.javaru.iip.frc.wpilib.version.WpiLibVersionImpl;
import net.javaru.iip.frc.wpilib.version.WpiLibVersionStatus;
import net.javaru.iip.frc.wpilib.version.WpiLibVersions;

import static net.javaru.iip.frc.util.FindClassUtils.isLibraryPresent;



public class WpiLibLibrariesUtils
{
    private static final Logger LOG = Logger.getInstance(WpiLibLibrariesUtils.class);
    
    // Network tables was completely rewritten for 2018. Jar changed from NetworkTables.jar to ntcore.jar
    public static final String NETWORK_TABLES_PRE_2018_CLASS_1 = "edu.wpi.first.wpilibj.networktables.NetworkTable";
    public static final String NETWORK_TABLES_PRE_2018_CLASS_2 = "edu.wpi.first.wpilibj.tables.ITable";
    public static final String NETWORK_TABLES_CLASS_1 = "edu.wpi.first.networktables.NetworkTable";
    public static final String NETWORK_TABLES_CLASS_2 = "edu.wpi.first.networktables.TableListener";


    /**
     * A convenience method to call {@code return WpiLibVersionStatus.getCurrentVersionStatus(project)}.
     * @return the current WpiLibVersionStatus 
     */
    public static WpiLibVersionStatus getCurrentWpiLibVersionStatus(@Nullable Project project)
    {
        return WpiLibVersionStatus.getCurrentVersionStatus(project);
    }
    
    
    private static boolean isWpilibAttached(@NotNull Project project)
    {
        //noinspection SimplifiableIfStatement
        if (project.isInitialized())
        {
            return isLibraryPresent(project, WpiLibConstants.ROBOT_BASE_FQN) ||
                   isLibraryPresent(project, WpiLibConstants.ITERATIVE_ROBOT_FQN) ||
                   isLibraryPresent(project, WpiLibConstants.VERSION_CLASS_FQN);
        }
        else
        {
            // TODO - I'd rather this thrown an exception to notify the caller that the project is not yet ready... or possible use a nullable Boolean.
            return false;
        }
    }


    public static boolean isWpilibAttachedViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isWpilibAttached(project));
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
        final boolean wpilibPresent = isWpilibAttached(project);
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


    private static boolean isWpilibAttached(@NotNull Module module)
    {
        return isLibraryPresent(module, WpiLibConstants.ROBOT_BASE_FQN) ||
               isLibraryPresent(module, WpiLibConstants.ITERATIVE_ROBOT_FQN);
    }


    public static boolean isWpilibAttachedViaReadAction(@NotNull Module module)
    {
        return DumbService.getInstance(module.getProject()).runReadActionInSmartMode(() -> isWpilibAttached(module));
    }


    public static boolean isWpilibDownloadedToSystem()
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


    public static boolean isWpilibDownloadedToSystemViaReadAction()
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) WpiLibLibrariesUtils::isWpilibDownloadedToSystem);
    }

    
    public static boolean isCommonDownloadedToSystem()
    {
        final Path commonSharedDir = WpiLibPaths.getCommonCurrentVersionSharedDir();
        final File[] files = commonSharedDir.toFile().listFiles();
        return files != null && files.length > 0;
    }
    
    public static boolean isCommonDownloadedToSystemViaReadAction()
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) WpiLibLibrariesUtils::isCommonDownloadedToSystem);
    }


    public static boolean is2018CommonRefreshNeeded()
    {
        final WpiLibVersion downloadedVersion = determineSystemAvailableWpiLibVersion();
        return downloadedVersion != null && WpiLibVersions.is2018OrLater(downloadedVersion) && !isCommonDownloadedToSystem();
    }

    public static boolean is2018CommonRefreshNeededViaReadAction()
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) WpiLibLibrariesUtils::is2018CommonRefreshNeeded);
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
        return isWpilibAttached(module) && isNetworkTablesPresent(module) && isOpenCvPresent(module) && isCsCorePresent(module);
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
        final ModifiableRootModel modifiableRootModel = ModuleRootManager.getInstance(module).getModifiableModel();
               
        // TODO: see if this needs to be changed so it is wrapped in ModuleRootModificationUtil.updateModel() described in javadoc for modifiableRootModel.dispose()
        try
        {
            final LibraryTable libraryTable = modifiableRootModel.getModuleLibraryTable();
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
        finally
        {
            if (!modifiableRootModel.isDisposed())
            {
                modifiableRootModel.dispose();
            }
        }
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


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
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


    @Nullable
    public static WpiLibVersion determineAvailableWpiLibVersion()
    {
        return WpiLibDownloader.getLatestVersionAvailable();
    }
    

    @Nullable
    public static WpiLibVersion determineAttachedWpiLibVersionViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> determineAttachedWpiLibVersion(project));
    }

    @Nullable
    public static WpiLibVersion determineAttachedWpiLibVersion(@NotNull Project project)
    {
        final String versionString = determineAttachedWpiLibVersionString(project);
        return extractWpiLibVersionFromVersionString(versionString);
    }

    public static String determineAttachedWpiLibVersionString(@NotNull Project project)
    {
        if (!isWpilibAttachedViaReadAction(project))
        {
            return FrcMessageBundle.message("frc.wpilib.not.attached");
        }

        
        final PsiClass[] verClass = FindClassUtils.findClass(project, WpiLibConstants.VERSION_CLASS_FQN);
        
        if (verClass.length == 0)
        {
            return FrcMessageBundle.message("frc.wpilib.version.unavailable", WpiLibConstants.VERSION_CLASS_FQN);
        }

        String version = null;
        for (PsiClass aClass : verClass)
        {
            @Nullable
            final PsiField versionField = aClass.findFieldByName("Version", false);
            if (versionField != null)
            {
                final PsiExpression initializer = versionField.getInitializer();
                
                if (initializer instanceof PsiLiteralExpression)
                {
                    Object value = ((PsiLiteralExpression) initializer).getValue();
                    if (value != null && value instanceof String)
                    {
                        version = value.toString();
                        break;
                    }
                }
            }
        }
        
        return version != null ? version : FrcMessageBundle.message("frc.wpilib.version.undetermined");
    }

 
    @Nullable
    public static WpiLibVersion determineSystemAvailableWpiLibVersionViaReadAction()
    {
        return ApplicationManager.getApplication().runReadAction((Computable<WpiLibVersion>) WpiLibLibrariesUtils::determineSystemAvailableWpiLibVersion);
    }
    
    @Nullable
    public static WpiLibVersion determineSystemAvailableWpiLibVersion()
    {
        final String versionString = determineSystemAvailableWpiLibVersionString();
        final WpiLibVersion version = extractWpiLibVersionFromVersionString(versionString);
        if (version != null) {LOG.debug("[FRC] WPILib version determined as " + version); }
        return version;
    }

    public static String determineSystemAvailableWpiLibVersionStringViaReadAction()
    {
        return ApplicationManager.getApplication().runReadAction((Computable<String>) WpiLibLibrariesUtils::determineSystemAvailableWpiLibVersionString);
    }
    
    public static String determineSystemAvailableWpiLibVersionString()
    {
        if (!isWpilibDownloadedToSystem())
        {
            return FrcMessageBundle.message("frc.wpilib.version.second.half.msg.not.on.system");
        }
        else
        {
            try
            {
                final Path sourcesJar = WpiLibPaths.getJavaLibDir().resolve("WPILib-sources.jar");
                if (Files.isReadable(sourcesJar))
                {
                    try (JarFile jarFile = new JarFile(sourcesJar.toFile()))
                    {
                        final ZipEntry entry = jarFile.getEntry("edu/wpi/first/wpilibj/util/WPILibVersion.java");
                        if (entry != null)
                        {
                            try (InputStream inputStream = jarFile.getInputStream(entry))
                            {
                                final List<String> lines = IOUtils.readLines(inputStream, StandardCharsets.UTF_8);
                                for (String line : lines)
                                {
                                    if (line.toLowerCase().contains("string version"))
                                    {
                                        final int begin = line.indexOf('"') + 1;
                                        final int end = line.indexOf('"', begin);
                                        final String version = line.substring(begin, end);
                                        LOG.debug("[FRC] WPILib version extracted from WPILib-sources.jar as " + version);
                                        return version;
                                    }
                                }
                                
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        LOG.debug("[FRC] Could not extract WPILib version from source JAR. Cause: " + e.toString());
                    }
                }
                else
                {
                    LOG.debug("[FRC] Could not extract WPILib version from source JAR as the file is not readable: " + sourcesJar);
                    // We don't have the sources JAR
                    final Path classesJar = WpiLibPaths.getJavaLibDir().resolve("WPILib.jar");
                    if (Files.isReadable(classesJar))
                    {
                        final Path tempFile = Files.createTempFile("WpiLibVersion", ".class");
                        try
                        {
                            
                            try (JarFile jarFile = new JarFile(classesJar.toFile()))
                            {
                                final ZipEntry entry = jarFile.getEntry("edu/wpi/first/wpilibj/util/WPILibVersion.class");
                                if (entry != null)
                                {
                                    
                                    try (InputStream inputStream = jarFile.getInputStream(entry))
                                    {
                                        FileUtils.copyToFile(inputStream, tempFile.toFile());
                                    }
                                }
                            }

                            final VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(tempFile.toUri().toString());
                            if (virtualFile == null)
                            {
                                LOG.debug("[FRC] Could not extract WPILib version from classes JAR. Could not create virtualFile to extracted class file at " + classesJar);
                            }
                            else 
                            {
                                final String text = new ClassFileDecompiler().decompile(virtualFile).toString();
                                final int begin = text.indexOf('"') + 1;
                                final int end = text.indexOf('"', begin);
                                final String version = text.substring(begin, end);
                                LOG.debug("[FRC] WPILib version extracted from the WPILibVersion.class file in WPILib.jar as " + version);
                                return version;
                            }
                            
                        }
                        catch (Exception e)
                        {
                            LOG.debug("[FRC] Could not extract WPILib version from classes JAR. Cause: " + e.toString());
                        }
                        finally
                        {
                            FrcFileUtils.deleteFileSafely(tempFile);
                        }
                    }
                    
                    
                }
            }
            catch (Exception e)
            {
                LOG.debug("[FRC] Could not extract WPILib version. Cause: " + e.toString());
            }

        }
        return FrcMessageBundle.message("frc.wpilib.version.second.half.msg.undetermined");
    }


    @Nullable
    public static WpiLibVersion extractWpiLibVersionFromVersionString(String versionString)
    {
        try
        {
            if (StringUtils.isBlank(versionString))
            {
                return null;
            }
            else if (Character.isDigit(versionString.toCharArray()[0]))
            {
                return WpiLibVersionImpl.parse(versionString);
            }
            else
            {
                return null;
            }
        }
        catch (Exception ignore)
        {
            return null;
        }
    }
    
}
