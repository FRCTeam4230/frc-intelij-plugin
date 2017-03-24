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

package net.javaru.iip.frc.util;

import java.nio.file.Files;
import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.JavadocOrderRootType;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;



public class LibraryUtils
{
    private static final Logger LOG = Logger.getInstance(LibraryUtils.class);


    public static void attachDirectoryBasedLibrary(@NotNull final Module module,
                                                   @NotNull final String libName,
                                                   @NotNull final Path dir)
    {
        ApplicationManager.getApplication().runWriteAction(() ->
                                                           {
                                                               try
                                                               {
                                                                   if (!Files.exists(dir))
                                                                   {
                                                                       Files.createDirectories(dir);
                                                                   }
                                                               }
                                                               catch (Throwable t) 
                                                               {
                                                                   LOG.info("[FRC] Could not create non-existing directory for attachment as library. This " 
                                                                            + "will result in directory not being attached as library. Target fir was '" 
                                                                            + dir + "'. Cause Summary: " + t.toString());
                                                               }
                                                               
                                                               final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);
                                                               final String urlString = VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, dir.toString());
                                                               final VirtualFile dirVirtualFile = VirtualFileManager.getInstance().findFileByUrl(urlString);
                                                               if (dirVirtualFile != null)
                                                               {
                                                                   final ModifiableRootModel modifiableModel = rootManager.getModifiableModel();
                                                                   final Library newLib = createDirLib(libName,
                                                                                                       dirVirtualFile,
                                                                                                       modifiableModel.getModuleLibraryTable());
                                                                   modifiableModel.commit();
                                                               }
                                                           });
    }


    public static Library createDirLib(@NotNull final String libName,
                                       @NotNull final VirtualFile dirVirtualFile,
                                       @NotNull final LibraryTable table)
    {
        Library library = table.getLibraryByName(libName);
        if (library == null)
        {
            library = table.createLibrary(libName);

            Library.ModifiableModel libraryModel = library.getModifiableModel();
            libraryModel.addJarDirectory(dirVirtualFile, true, OrderRootType.CLASSES);
            libraryModel.addJarDirectory(dirVirtualFile, true, OrderRootType.SOURCES);
            libraryModel.addJarDirectory(dirVirtualFile, true, JavadocOrderRootType.getInstance());
            libraryModel.commit();

        }
        return library;
    }


    public static void attachJarLibrary(@NotNull final Module module,
                                        @NotNull final String libName,
                                        @NotNull final String jarFilePath,
                                        @NotNull final String srcFilePath,
                                        @NotNull final String docFilePath)
    {
        ApplicationManager.getApplication().runWriteAction(() ->
                                                           {
                                                               final ModuleRootManager rootManager = ModuleRootManager.getInstance(module);

                                                               final String clzUrlString = VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, jarFilePath)
                                                                                           + JarFileSystem.JAR_SEPARATOR;
                                                               final String srcUrlString = VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, srcFilePath)
                                                                                           + JarFileSystem.JAR_SEPARATOR;
                                                               final String docUrlString = VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, docFilePath)
                                                                                           + JarFileSystem.JAR_SEPARATOR;

                                                               final VirtualFile jarVirtualFile = VirtualFileManager.getInstance().findFileByUrl(clzUrlString);
                                                               final VirtualFile srcVirtualFile = VirtualFileManager.getInstance().findFileByUrl(srcUrlString);
                                                               final VirtualFile docVirtualFile = VirtualFileManager.getInstance().findFileByUrl(docUrlString);

                                                               final ModifiableRootModel modifiableModel = rootManager.getModifiableModel();
                                                               final Library newLib = createJarLib(libName,
                                                                                                   modifiableModel.getModuleLibraryTable(),
                                                                                                   jarVirtualFile,
                                                                                                   srcVirtualFile,
                                                                                                   docVirtualFile);
                                                               modifiableModel.commit();
                                                           });
    }


    public static Library createJarLib(@NotNull final String libName,
                                 @NotNull final LibraryTable table,
                                 @Nullable final VirtualFile jarVirtualFile,
                                 @Nullable final VirtualFile srcVirtualFile,
                                 @Nullable final VirtualFile docVirtualFile)
    {
        Library library = table.getLibraryByName(libName);
        if (library == null)
        {
            library = table.createLibrary(libName);
            Library.ModifiableModel libraryModel = library.getModifiableModel();

            if (jarVirtualFile != null)
            {
                libraryModel.addRoot(jarVirtualFile, OrderRootType.CLASSES);
            }

            if (srcVirtualFile != null)
            {
                libraryModel.addRoot(srcVirtualFile, OrderRootType.SOURCES);
            }

            if (docVirtualFile != null)
            {
                libraryModel.addRoot(docVirtualFile, JavadocOrderRootType.getInstance());
            }

            libraryModel.commit();
        }
        return library;
    }
}
