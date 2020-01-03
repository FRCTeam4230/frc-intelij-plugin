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

package net.javaru.iip.frc.wpilib.legacy;

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

import net.javaru.iip.frc.i18n.FrcBundle;
import net.javaru.iip.frc.util.FrcClassUtilsKt;
import net.javaru.iip.frc.util.FrcFileUtils;
import net.javaru.iip.frc.wpilib.WpiLibConstants;
import net.javaru.iip.frc.wpilib.legacy.retrieval.LegacyWpiLibDownloader;
import net.javaru.iip.frc.wpilib.version.WpiLibVersion;
import net.javaru.iip.frc.wpilib.version.WpiLibVersionExtKt;
import net.javaru.iip.frc.wpilib.version.WpiLibVersionImpl;

import static net.javaru.iip.frc.util.FrcClassUtilsKt.isLibraryPresent;



public class LegacyWpiLibLibrariesUtils
{
    private static final Logger LOG = Logger.getInstance(LegacyWpiLibLibrariesUtils.class);
    
    // Network tables was completely rewritten for 2018. Jar changed from NetworkTables.jar to ntcore.jar
    public static final String NETWORK_TABLES_PRE_2018_CLASS_1 = "edu.wpi.first.wpilibj.networktables.NetworkTable";
    public static final String NETWORK_TABLES_PRE_2018_CLASS_2 = "edu.wpi.first.wpilibj.tables.ITable";
    public static final String NETWORK_TABLES_CLASS_1 = "edu.wpi.first.networktables.NetworkTable";
    public static final String NETWORK_TABLES_CLASS_2 = "edu.wpi.first.networktables.TableListener";


    /**
     * A convenience method to call {@code return LegacyWpiLibVersionStatus.getCurrentVersionStatus(project)}.
     * @return the current LegacyWpiLibVersionStatus 
     */
    public static LegacyWpiLibVersionStatus getCurrentWpiLibVersionStatus(@Nullable Project project)
    {
        return LegacyWpiLibVersionStatus.getCurrentVersionStatus(project);
    }
    
    
    private static boolean isLegacyWpilibAttached(@NotNull Project project)
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


    public static boolean isLegacyWpilibAttachedViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isLegacyWpilibAttached(project));
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
        final boolean wpilibPresent = isLegacyWpilibAttached(project);
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


    private static boolean isLegacyWpilibAttached(@NotNull Module module)
    {
        return isLibraryPresent(module, WpiLibConstants.ROBOT_BASE_FQN) ||
               isLibraryPresent(module, WpiLibConstants.ITERATIVE_ROBOT_FQN);
    }


    public static boolean isLegacyWpilibAttachedViaReadAction(@NotNull Module module)
    {
        return DumbService.getInstance(module.getProject()).runReadActionInSmartMode(() -> isLegacyWpilibAttached(module));
    }


    public static boolean isLegacyWpilibDownloadedToSystem()
    {
        try
        {
            return FrcFileUtils.directoryHasJars(LegacyWpiLibPaths.getJavaLibDir(), true);
        }
        catch (IOException e)
        {
            return false;
        }
    }


    public static boolean isLegacyWpilibDownloadedToSystemViaReadAction()
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) LegacyWpiLibLibrariesUtils::isLegacyWpilibDownloadedToSystem);
    }

    
    public static boolean isCommonDownloadedToSystem()
    {
        final Path commonSharedDir = LegacyWpiLibPaths.getCommonCurrentVersionSharedDir();
        final File[] files = commonSharedDir.toFile().listFiles();
        return files != null && files.length > 0;
    }
    
    public static boolean isCommonDownloadedToSystemViaReadAction()
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) LegacyWpiLibLibrariesUtils::isCommonDownloadedToSystem);
    }


    public static boolean is2018CommonRefreshNeeded()
    {
        final WpiLibVersion downloadedVersion = determineSystemAvailableLegacyWpiLibVersion();
        return downloadedVersion != null && WpiLibVersionExtKt.is2018Project(downloadedVersion) && !isCommonDownloadedToSystem();
    }

    public static boolean is2018CommonRefreshNeededViaReadAction()
    {
        return ApplicationManager.getApplication().runReadAction((Computable<Boolean>) LegacyWpiLibLibrariesUtils::is2018CommonRefreshNeeded);
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
        return isLegacyWpilibAttached(module) && isNetworkTablesPresent(module) && isOpenCvPresent(module) && isCsCorePresent(module);
    }


    public static boolean areAllPresentViaReadAction(@NotNull Module module)
    {
        return DumbService.getInstance(module.getProject()).runReadActionInSmartMode(() -> areAllPresent(module));
    }


    @Nullable
    public static Library findExistingLegacyWpilibJavaLibDirLibrary(@NotNull Module module)
    {
        final Path libDir = LegacyWpiLibPaths.getJavaLibDir();
        return findExistingDirBasedLibrary(module, libDir);
    }
    
    private static boolean isLegacyWpilibJavaLibDirAttached(@NotNull Project project)
    {
        final Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules)
        {
            if (findExistingLegacyWpilibJavaLibDirLibrary(module) != null)
            {
                return true;
            }
        }
        return false;
    }


    public static boolean isLegacyWpilibJavaLibDirAttachedViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isLegacyWpilibJavaLibDirAttached(project));
    }

    @Nullable
    public static Library findExistingUserLibDirLibrary(@NotNull Module module)
    {
        final Path userLibDir = LegacyWpiLibPaths.getUserLibDir();
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


    private static boolean isLegacyUserLibAttached(@NotNull Project project)
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
    public static boolean isLegacyUserLibAttachedViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isLegacyUserLibAttached(project));
    }


    private static boolean isLegacyUserLibNonEmptyAndNotAttached(@NotNull Project project)
    {
        try
        {
            return FrcFileUtils.directoryHasJars(LegacyWpiLibPaths.getUserLibDir(), true) && !isLegacyUserLibAttached(project);
        }
        catch (IOException e)
        {
            final String message = "An IOException occurred when checkin for user lib attachment. Cause Summary: " + e.toString();
            LOG.warn(message);
            throw new IllegalStateException(message, e);
        }
    }


    public static boolean isLegacyUserLibNonEmptyAndAttachedViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> isLegacyUserLibNonEmptyAndNotAttached(project));
    }


    @Nullable
    public static WpiLibVersion determineAvailableLegacyWpiLibVersion()
    {
        return LegacyWpiLibDownloader.getLatestVersionAvailable();
    }
    

    @Nullable
    public static WpiLibVersion determineAttachedLegacyWpiLibVersionViaReadAction(@NotNull Project project)
    {
        return DumbService.getInstance(project).runReadActionInSmartMode(() -> determineAttachedLegacyWpiLibVersion(project));
    }

    @Nullable
    public static WpiLibVersion determineAttachedLegacyWpiLibVersion(@NotNull Project project)
    {
        final String versionString = determineLegacyAttachedWpiLibVersionString(project);
        return extractWpiLibVersionFromVersionString(versionString);
    }
    
    
    /**
     * For non-legacy, see {@link net.javaru.iip.frc.wpilib.WpiLibHelpersKt#getAttachedWpiLibVersionStringInSmartReadAction(Project)}
     */
    public static String determineLegacyAttachedWpiLibVersionString(@NotNull Project project)
    {
        if (!isLegacyWpilibAttachedViaReadAction(project))
        {
            return FrcBundle.message("frc.wpilib.not.attached");
        }

        
        final PsiClass[] verClass = FrcClassUtilsKt.findClass(project, WpiLibConstants.VERSION_CLASS_FQN);
        
        if (verClass.length == 0)
        {
            return FrcBundle.message("frc.wpilib.version.unavailable", WpiLibConstants.VERSION_CLASS_FQN);
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
                    if (value instanceof String)
                    {
                        version = value.toString();
                        break;
                    }
                }
            }
        }
        
        return version != null ? version : FrcBundle.message("frc.wpilib.version.undetermined");
    }

 
    @Nullable
    public static WpiLibVersion determineSystemAvailableLegacyWpiLibVersionViaReadAction()
    {
        return ApplicationManager.getApplication().runReadAction((Computable<WpiLibVersion>) LegacyWpiLibLibrariesUtils::determineSystemAvailableLegacyWpiLibVersion);
    }
    
    @Nullable
    public static WpiLibVersion determineSystemAvailableLegacyWpiLibVersion()
    {
        final String versionString = determineSystemAvailableLegacyWpiLibVersionString();
        final WpiLibVersion version = extractWpiLibVersionFromVersionString(versionString);
        if (version != null) {LOG.debug("[FRC] WPILib version determined as " + version); }
        return version;
    }

    public static String determineSystemAvailableLegacyWpiLibVersionStringViaReadAction()
    {
        return ApplicationManager.getApplication().runReadAction((Computable<String>) LegacyWpiLibLibrariesUtils::determineSystemAvailableLegacyWpiLibVersionString);
    }
    
    public static String determineSystemAvailableLegacyWpiLibVersionString()
    {
        if (!isLegacyWpilibDownloadedToSystem())
        {
            return FrcBundle.message("frc.wpilib.version.second.half.msg.not.on.system");
        }
        else
        {
            try
            {
                final Path sourcesJar = LegacyWpiLibPaths.getJavaLibDir().resolve("WPILib-sources.jar");
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
                    final Path classesJar = LegacyWpiLibPaths.getJavaLibDir().resolve("WPILib.jar");
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
        return FrcBundle.message("frc.wpilib.version.second.half.msg.undetermined");
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
                return WpiLibVersionImpl.Companion.parse(versionString);
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
