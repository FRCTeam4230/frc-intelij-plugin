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

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;



public class UnzipUtils
{
    private static final int EOF = -1;
    private static final int BUFFER_SIZE = 4096;


    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     *
     * @param zipFilePath        the path to the ZIP File
     * @param destDir            the destination directory
     * @param deleteDestDirFirst id the destination directory should be deleted before
     *                           extraction to ensure a clean copy of the zip file contents
     *
     * @throws IOException if an I/O error occurs
     */
    public static void unzip(Path zipFilePath, Path destDir, boolean deleteDestDirFirst) throws IOException
    {
        final InputStream inputStream = new FileInputStream(zipFilePath.toFile());
        unzip(inputStream, destDir, deleteDestDirFirst);
    }


    /**
     * Extracts a zip file specified by the zipFilePath to a directory specified by
     * destDirectory (will be created if does not exists)
     *
     * @param inputStream        an inputStream for the ZIP File; it will be closed
     * @param destDir            the destination directory
     * @param deleteDestDirFirst id the destination directory should be deleted before
     *                           extraction to ensure a clean copy of the zip file contents
     *
     * @throws IOException if an I/O error occurs
     */
    public static void unzip(InputStream inputStream, Path destDir, boolean deleteDestDirFirst) throws IOException
    {
        try (ZipInputStream zipIn = new ZipInputStream(inputStream))
        {
            if (deleteDestDirFirst)
            {
                FileUtils.deleteDirectory(destDir.toFile());
            }
            Files.createDirectories(destDir);
            

            ZipEntry entry;
            // iterates over entries in the zip file
            while ((entry = zipIn.getNextEntry()) != null)
            {
                Path filePath = destDir.resolve(entry.getName());
                if (!entry.isDirectory())
                {
                    // if the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                }
                else
                {
                    // if the entry is a directory, make the directory
                    Files.createDirectories(filePath);
                }
                zipIn.closeEntry();
            }
            zipIn.close();
        }
    }


    private static void extractFile(ZipInputStream zipIn, Path filePath) throws IOException
    {
        BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(filePath));
        byte[] bytesInBuffer = new byte[BUFFER_SIZE];

        int readBytes;
        while ((readBytes = zipIn.read(bytesInBuffer)) != EOF)
        {
            bos.write(bytesInBuffer, 0, readBytes);
        }
        bos.close();
    }
}
