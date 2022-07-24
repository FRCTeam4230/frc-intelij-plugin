/*
 * Copyright 2015-2022 the original author or authors.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.javaru.iip.frc.util


import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import net.javaru.iip.frc.FrcPluginGlobals.IS_IN_FRC_INTERNAL_MODE
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.RandomStringUtils
import java.io.IOException
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.util.*

private object FrcFileExts

private val logger = logger<FrcFileExts>()

fun findCommonParentDir(thePaths: Collection<String>): String
{
    val paths = thePaths.map { it.toCommonSeparatorPath() }
    val separator = "/"
    if (paths.isEmpty()) return ""
    if (paths.size == 1) return paths[0]

    val splits = paths[0].toCommonSeparatorPath().split(separator)
    val n = splits.size
    val paths2 = paths.drop(1)
    var k = 0
    var common = ""
    while (true)
    {
        val prevCommon = common
        common += if (k == 0) splits[0] else separator + splits[k]
        if (!paths2.all { it.startsWith(common + separator) || it == common }) return prevCommon
        if (++k == n) return common
    }
}

fun String?.toCommonSeparatorPath(): String = FilenameUtils.separatorsToUnix(this) ?: ""

/** 
 * Normalize a path by converting backslashes to forward slashes, and removing double slashes inside the path
 * (without affecting windows network paths). It is a convenience method that calls IDEA's `FileUtil.normalize(String)`
 */
@Suppress("unused", "RemoveRedundantQualifierName")
fun String.normalizePath(): String = com.intellij.openapi.util.io.FileUtil.normalize(this)


/**
 * Creates a named temp directory within the FRC temp directory (which itself is inside
 * the system temp dir), and then creates it so that it is ready for use.
 *  @param name the name of the temp directory
 *  @param deleteOnExit whether to delete the directory on exit. Default is `false`
 *                      See the [Path.deleteOnExit] extension function docs for important
 *                      information on how deleting a directory on exit works
 * @see generateRandomTempPath
 * @see createRandomTempFile
 * @see createRandomTempDir
 */
fun createNamedTempDir(name: String, deleteOnExit: Boolean = false): Path
{
    val dir = getFrcPluginTempDirectory().resolve(name)
    Files.createDirectories(dir)
    if (deleteOnExit) {dir.deleteOnExit()}
    logger.trace { "[FRC] Created temp directory: $dir" }
    return dir.toAbsolutePath()
}


/**
 * Uses the [generateRandomTempPath] function to generate a path for a random
 * temp directory, within the FRC temp directory (which itself is inside
 * the system temp dir), and then creates it so that it is ready for use.
 *
 * @param deleteOnExit if `true` (the default) the temp directory will be marked for deletion on exit.
 *                     See the [Path.deleteOnExit] extension function docs for important
 *                     information on how deleting a directory on exit works
 * @see generateRandomTempPath
 * @see createRandomTempFile
 * @see createNamedTempDir
 */
fun createRandomTempDir(deleteOnExit: Boolean = true): Path
{
    val file = generateRandomTempPath(null, deleteOnExit)
    Files.createDirectories(file)
    logger.trace { "[FRC] Created temp directory: $file" }
    return file.toAbsolutePath()
}

/**
 * Uses the [generateRandomTempPath] function to generate a path for a random temp file,
 * and then creates it so that it is ready for use.
 *
 * @param extension the file extension, either with or without the leading dot. If null/blank, no extension is used.
 *                  Thus, for a path for use for a temp dir, this will generally be set to null.
 * @param deleteOnExit if `true` (the default) the temp file will be marked for deletion on exit.
 *                     See the [Path.deleteOnExit] extension function docs for important
 *                     information on how deleting a directory on exit works
 * @see generateRandomTempPath
 * @see createRandomTempDir
 * @see createNamedTempDir
 */
fun createRandomTempFile(extension: String? = null, deleteOnExit: Boolean = true): Path
{
    val file = generateRandomTempPath(extension, deleteOnExit)
    Files.createFile(file)
    logger.trace { "[FRC] Created temp file: $file" }
    return  file.toAbsolutePath()
}

/**
 * Generates a **path for** a random temp file or directory, but does **not** create the file or directory.
 * The parent directory, and FRC IntelliJ IDEA Plugin directory within the system temp directory, will exist.
 *
 * @param extension the file extension, either with or without the leading dot. If null/blank, no extension is used.
 *                  Thus, for a path for use for a temp dir, this will generally be set to null.
 * @param deleteOnExit if `true` (the default) the temp file/dir will be marked for deletion on exit.
 *                     See the [Path.deleteOnExit] extension function docs for important
 *                     information on how deleting a directory on exit works
 * @see createRandomTempFile
 * @see createRandomTempDir
 * @see createNamedTempDir
 */
@JvmOverloads
fun generateRandomTempPath(extension: String? = null, deleteOnExit: Boolean = true): Path
{
    val tmpDir = getFrcPluginTempDirectory()
    val ext = if (extension == null) "" else if (extension.startsWith(".")) extension else ".${extension}"
    var path = tmpDir.resolve("frc-iip-${UUID.randomUUID()}$ext")
    var tries = 0
    while (Files.exists(path) && tries++ < 15)
    {
        path = when
        {
            // Progressively add to the randomness of the name. Although it is doubtful we'd ever need such
            // extreme measures since the chances of a duplicated UUID, let alone two in a row, are so high.
            tries < 5  -> tmpDir.resolve("frc-iip-${UUID.randomUUID()}$ext")
            tries < 10 -> tmpDir.resolve("frc-iip-${UUID.randomUUID()}--${RandomStringUtils.randomAlphanumeric(10)}$ext")
            else       -> tmpDir.resolve("frc-iip-${UUID.randomUUID()}--${RandomStringUtils.randomAlphanumeric(10)}--${UUID.randomUUID()}$ext")
        }
    }
    // While the odds of this occurring are absolutely astronomical, we must account for it.
    if (Files.exists(path)) throw IOException("Couldn't generate unique random path for temp file.")

    path =  path.normalize()
    if (deleteOnExit)
        path.deleteOnExit()
    return path
}

/**
 * Gets (and creates if necessary) the FRC IntelliJ IDEA Plugin temp directory (which
 * is created with the system's temp directory). **One of the other create temp file/dir
 * functions should be favored over using this function.**
 *
 * @see generateRandomTempPath
 * @see createRandomTempFile
 * @see createRandomTempDir
 * @see createNamedTempDir
 */
fun getFrcPluginTempDirectory(): Path
{
    // We use our own subdirectory to make testing and debugging easier, and to reduce
    // clutter in the system temp directory.
    // When in "internal" mode on Windows, we prefix it with a special character so that it is
    // easier to find in the temp directory as it should always be near the top.
    val prefix = if (IS_IN_FRC_INTERNAL_MODE && SystemInfo.isWindows) "#" else ""
    val tmpDir = try
    {
        Path.of(FileUtil.getTempDirectory())
            .resolve("${prefix}FRC-IntelliJ-IDEA-Plugin")
    }
    catch (ignore: InvalidPathException)
    {
        // Just in case (although the prefix character is known to work on Windows)
        Path.of(FileUtil.getTempDirectory())
            .resolve("FRC-IntelliJ-IDEA-Plugin")
    }

    if (!Files.exists(tmpDir)) Files.createDirectories(tmpDir)
    return tmpDir
}

/**
 * Marks a path to be deleted on JVM exit, as described in [java.io.File.deleteOnExit].
 * Files (or directories) are deleted in the reverse order that they are registered. This
 * has implications for the deletion of directories. A directory marked delete on exit is
 * deleted iff the directory is empty. This can be either by the files being deleted
 * programmatically (before the JVM goes to exit), or all files being marked
 * delete on exit AFTER the directory is marked delete on exit.
 */
fun Path.deleteOnExit() = this.toFile().deleteOnExit()
