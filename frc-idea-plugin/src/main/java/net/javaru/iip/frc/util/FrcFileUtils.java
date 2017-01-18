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

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.io.FileUtilRt;



public final class FrcFileUtils
{

    private FrcFileUtils() { }


    /**
     * @param name     the name of the file, either with or without the extension.
     * @param fileType the file type of file the name is for
     *
     * @return the file name with the proper extension.
     */
    //TODO: move to abstract class
    @Contract("null, _ -> null; !null, _ -> !null")
    public static String normalizeFileNameExtension(@Nullable String name, @Nullable FileType fileType)
    {
        return fileType != null ? normalizeFileNameExtension(name, fileType.getDefaultExtension())
                                : normalizeFileNameExtension(name, (String) null);
    }


    /**
     * @param name      the name of the file, either with or without the extension.
     * @param extension the extension to normalize to without the dot prefix. For example: 'txt', 'xml', 'java', etc.
     *
     * @return the file name with the proper extension.
     */
    @Contract("null, _ -> null; !null, _ -> !null")
    public static String normalizeFileNameExtension(@Nullable String name, @Nullable String extension)
    {
        // Possibilities:
        //   0) null values
        //   1) correct file ext (incl case)
        //   2) correct file ext, wrong case
        //   3) no file ext, or not correct one

        // Case 0:
        if (StringUtils.isBlank(name)) { return null;}
        name = name.trim();
        if (extension == null) {return name;}


        final String actualExt = FileUtilRt.getExtension(name);
        //Case 1
        if (actualExt.equals(extension)) {return name;}
        final String justName = FilenameUtils.removeExtension(name);
        //Case 2
        if (actualExt.equalsIgnoreCase(extension)) {return justName + '.' + extension;}
        // Case 3
        return name + '.' + extension;
    }


    public static boolean isDirectoryEmpty(@NotNull Path directory) throws IOException
    {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory))
        {
            return !dirStream.iterator().hasNext();
        }
    }
    
    public static boolean doesDirectoryHaveJars(@NotNull final Path directory, final boolean recursive) throws IOException
    {
        final AtomicBoolean foundJar = new AtomicBoolean(false);
        
        class MyFileVisitor extends SimpleFileVisitor<Path>
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
            {
                if (file.getFileName().toString().toLowerCase().endsWith(".jar"))
                {
                    foundJar.set(true);
                    return FileVisitResult.TERMINATE;
                }
                else 
                {
                    return FileVisitResult.CONTINUE;
                }
            }


            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException
            {
                return recursive ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
            }
        }
        final MyFileVisitor fileVisitor = new MyFileVisitor();
        Files.walkFileTree(directory, fileVisitor);
        return foundJar.get();
    }
}
