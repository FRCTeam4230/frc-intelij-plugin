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

package net.javaru.iip.frc.util

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.JavadocOrderRootType
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.OrderRootType
import com.intellij.openapi.roots.libraries.Library
import com.intellij.openapi.roots.libraries.LibraryTable
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import java.nio.file.Files
import java.nio.file.Path


    private val LOG = Logger.getInstance("#net.javaru.iip.frc.util.LibraryUtils")



    fun attachDirectoryBasedLibrary(libDef: LibDef)
    {
        ApplicationManager.getApplication().runWriteAction {
            
            LOG.debug("[FRC] Attaching Library: $libDef")
            val rootManager = ModuleRootManager.getInstance(libDef.module)
            val modifiableRootModel = rootManager.modifiableModel
            val libraryTable = modifiableRootModel.moduleLibraryTable
            
            var library = libraryTable.getLibraryByName(libDef.libName)
            if (library == null)
            {
                LOG.info("[FRC] Library '${libDef.libName} does not exist and will be created.")
                library = libraryTable.createLibrary(libDef.libName)
            }
            else
            {
                LOG.info("[FRC] Library '${libDef.libName} already exists. Will modify/update it.")
                val libraryModifiableModel = library.modifiableModel
                libraryModifiableModel.getUrls(OrderRootType.CLASSES).forEach { libraryModifiableModel.removeRoot(it, OrderRootType.CLASSES) }
                libraryModifiableModel.getUrls(OrderRootType.SOURCES).forEach { libraryModifiableModel.removeRoot(it, OrderRootType.SOURCES) }
                libraryModifiableModel.getUrls(JavadocOrderRootType.getInstance()).forEach { libraryModifiableModel.removeRoot(it, JavadocOrderRootType.getInstance()) }
            }
            
            val libraryModifiableModel = library.modifiableModel
            libDef.binDirs.forEach { attach(it, libraryModifiableModel, LibDirType.BIN) }
            libDef.srcDirs.forEach { attach(it, libraryModifiableModel, LibDirType.SRC) }
            libDef.docDirs.forEach { attach(it, libraryModifiableModel, LibDirType.DOC) }
            
            libraryModifiableModel.commit()
            modifiableRootModel.commit()
            LOG.debug("[FRC] Library attach competed for '${libDef.libName}")
        }
    }

    private fun createDir(path: Path)
    {
        try
        {
            if (!Files.exists(path))
            {
                Files.createDirectories(path)
            }
        }
        catch (t: Throwable)
        {
            LOG.info("[FRC] Could not create non-existing directory for attachment as library. This "
                     + "will result in directory not being attached as library. Target dir was '"
                     + path + "'. Cause Summary: " + t.toString())
        }
    }
    
    
    private fun attach(dir: Path, libraryModel: Library.ModifiableModel, type: LibDirType)
    {
        createDir(dir)
        if (Files.isDirectory(dir))
        {
            val urlString = VirtualFileManager.constructUrl(LocalFileSystem.PROTOCOL, dir.toString())
            val dirVirtualFile = VirtualFileManager.getInstance().findFileByUrl(urlString)
            if (dirVirtualFile != null)
            {
                val orderRootType =
                        when(type)
                        {
                            LibDirType.BIN -> OrderRootType.CLASSES
                            LibDirType.SRC -> OrderRootType.SOURCES
                            LibDirType.DOC -> JavadocOrderRootType.getInstance()
                        }
                libraryModel.addJarDirectory(dirVirtualFile, false, orderRootType)
            }
        }
    }


    
    fun attachJarLibrary(module: Module,
                         libName: String,
                         jarFilePath: String,
                         srcFilePath: String,
                         docFilePath: String)
    {
        ApplicationManager.getApplication().runWriteAction {
            val rootManager = ModuleRootManager.getInstance(module)

            val clzUrlString = VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, jarFilePath) + JarFileSystem.JAR_SEPARATOR
            val srcUrlString = VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, srcFilePath) + JarFileSystem.JAR_SEPARATOR
            val docUrlString = VirtualFileManager.constructUrl(JarFileSystem.PROTOCOL, docFilePath) + JarFileSystem.JAR_SEPARATOR

            val jarVirtualFile = VirtualFileManager.getInstance().findFileByUrl(clzUrlString)
            val srcVirtualFile = VirtualFileManager.getInstance().findFileByUrl(srcUrlString)
            val docVirtualFile = VirtualFileManager.getInstance().findFileByUrl(docUrlString)

            val modifiableModel = rootManager.modifiableModel
            /*val newLib =*/ createJarLib(libName,
                                      modifiableModel.moduleLibraryTable,
                                      jarVirtualFile,
                                      srcVirtualFile,
                                      docVirtualFile)
            modifiableModel.commit()
        }
    }


    fun createJarLib(libName: String,
                     table: LibraryTable,
                     jarVirtualFile: VirtualFile?,
                     srcVirtualFile: VirtualFile?,
                     docVirtualFile: VirtualFile?): Library
    {
        var library = table.getLibraryByName(libName)
        if (library == null)
        {
            library = table.createLibrary(libName)
            val libraryModel = library.modifiableModel

            if (jarVirtualFile != null)
            {
                libraryModel.addRoot(jarVirtualFile, OrderRootType.CLASSES)
            }

            if (srcVirtualFile != null)
            {
                libraryModel.addRoot(srcVirtualFile, OrderRootType.SOURCES)
            }

            if (docVirtualFile != null)
            {
                libraryModel.addRoot(docVirtualFile, JavadocOrderRootType.getInstance())
            }

            libraryModel.commit()
        }
        return library
    }

