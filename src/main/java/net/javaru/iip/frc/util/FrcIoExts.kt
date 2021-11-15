/*
 * Copyright 2015-2021 the original author or authors.
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

@file:Suppress("unused")

package net.javaru.iip.frc.util

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.intellij.openapi.diagnostic.logger
import com.intellij.util.io.createDirectories
import net.javaru.iip.frc.common.FrcPluginFileAlreadyExistsError
import net.javaru.iip.frc.common.FrcPluginIoError
import net.javaru.iip.frc.common.FrcPluginSourceFileNotFoundError
import java.io.Closeable
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.io.Writer
import java.net.ServerSocket
import java.net.Socket
import java.nio.channels.Selector
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/*

==== ALSO SEE: com.intellij.util.io.path.kt (in platform util-ex module) ====
In particular the Path.copy and Path.move extension functions

 */

private object FrcIoExts
private val logger = logger<FrcIoExts>()

///**
// * Returns the end path by removing the base path from the full path.
// * For example, give a full path of `/java/jdk8/docs/api` and a base path of `/java/jdk8`, this will return `docs/api`
// */
//fun getEndPath(fullPath: Path, basePath: Path): Path = fullPath.subpath(basePath.nameCount, fullPath.nameCount)

/**
 * Returns the end path by removing the base path from the full path (i.e. the receiver/this).
 * For example, give a full path (receiver) of `/java/jdk8/docs/api` and a base path of `/java/jdk8`, this will return `docs/api`
 */
fun Path.removeBasePath(basePath: Path): Path = this.subpath(basePath.nameCount, this.nameCount)

// ********************************************************
// For Path to VirtualFile function, see VirtualFilesExts
// ********************************************************

// TODO: For the closeQuietly function: Need to move JavaDocs examples to @sample tags (and clean them up a bit)
/**
 * Closes an `Reader` unconditionally.
 *
 *
 * Equivalent to [Reader.close], except any exceptions will be ignored.
 * This is typically used in finally blocks.
 *
 *
 * Example code:
 * ```
 * char[] data = new char[1024];
 * Reader in = null;
 * try {
 *     in = new FileReader("foo.txt");
 *     in.read(data);
 *     in.close(); //close errors are handled
 * } catch (Exception e) {
 *     // error handling
 * } finally {
 *     FrcIoExtsKt.closeQuietly(in);
 * }
 * ```
 *
 * @receiver the Reader to close, may be null or already closed
 *
 * @see Throwable.addSuppressed
 */
fun Reader.closeQuietly()
{
    (this as Closeable).closeQuietly()
}

/**
 * Closes an `Writer` unconditionally.
 *
 *
 * Equivalent to [Writer.close], except any exceptions will be ignored.
 * This is typically used in finally blocks.
 *
 *
 * Example code:
 * ```
 * Writer out = null;
 * try {
 *     out = new StringWriter();
 *     out.write("Hello World");
 *     out.close(); //close errors are handled
 * } catch (Exception e) {
 *     // error handling
 * } finally {
 *     FrcIoExtsKt.closeQuietly(out);
 * }
 * ```
 *
 * @receiver the Writer to close, may be null or already closed
 *
 * @see Throwable.addSuppressed
 */
fun Writer.closeQuietly()
{
    (this as Closeable).closeQuietly()
}

/**
 * Closes an `InputStream` unconditionally.
 *
 *
 * Equivalent to [InputStream.close], except any exceptions will be ignored.
 * This is typically used in finally blocks.
 *
 *
 * Example code:
 * ```
 * byte[] data = new byte[1024];
 * InputStream in = null;
 * try {
 *     in = new FileInputStream("foo.txt");
 *     in.read(data);
 *     in.close(); //close errors are handled
 * } catch (Exception e) {
 *     // error handling
 * } finally {
 *     FrcIoExtsKt.closeQuietly(in);
 * }
 * ```
 *
 * @param input the InputStream to close, may be null or already closed
 *
 * @see Throwable.addSuppressed
 */
fun closeQuietly(input: InputStream)
{
    (input as Closeable).closeQuietly()
}

/**
 * Closes an `OutputStream` unconditionally.
 *
 *
 * Equivalent to [OutputStream.close], except any exceptions will be ignored.
 * This is typically used in finally blocks.
 *
 *
 * Example code:
 * ```
 * byte[] data = "Hello, World".getBytes();
 *
 * OutputStream out = null;
 * try {
 *     out = new FileOutputStream("foo.txt");
 *     out.write(data);
 *     out.close(); //close errors are handled
 * } catch (IOException e) {
 *     // error handling
 * } finally {
 *     FrcIoExtsKt.closeQuietly(out);
 * }
 * ```
 *
 * @receiver the OutputStream to close, may be null or already closed
 *
 * @see Throwable.addSuppressed
 */
fun OutputStream.closeQuietly()
{
    (this as Closeable).closeQuietly()
}

/**
 * Closes a `Closeable` unconditionally.
 *
 *
 * Equivalent to [Closeable.close], except any exceptions will be ignored. This is typically used in
 * finally blocks.
 *
 *
 * Example code:
 *
 * ```
 * Closeable closeable = null;
 * try {
 *     closeable = new FileReader(&quot;foo.txt&quot;);
 * // process closeable
 *     closeable.close();
 * } catch (Exception e) {
 *     // error handling
 * } finally {
 *     FrcIoExtsKt.closeQuietly(closeable);
 * }
 * ```
 *
 *
 * Closing all streams:
 *
 * ```
 * try {
 *     return FrcIoExtsKt.copy(inputStream, outputStream);
 * } finally {
 *     FrcIoExtsKt.closeQuietly(inputStream);
 *     FrcIoExtsKt.closeQuietly(outputStream);
 * }
 * ```
 * 
 * @receiver the objects to close, may be null or already closed
 *
 * @see Throwable.addSuppressed
 */

fun Closeable?.closeQuietly()
{
    try
    {
        this?.close()
    }
    catch (ioe: IOException)
    {
        // ignore

    }
}

/**
 * Closes a `Closeable` unconditionally.
 *
 *
 * Equivalent to [Closeable.close], except any exceptions will be ignored.
 *
 *
 * This is typically used in finally blocks to ensure that the closeable is closed
 * even if an Exception was thrown before the normal close statement was reached.
 *
 * It should not be used to replace the close statement(s)
 * which should be present for the non-exceptional case.**
 *
 * It is only intended to simplify tidying up where normal processing has already failed
 * and reporting close failure as well is not necessary or useful.
 *
 *
 * Example code:
 *
 * ```
 * Closeable closeable = null;
 * try {
 *     closeable = new FileReader(&quot;foo.txt&quot;);
 *     // processing using the closeable; may throw an Exception
 *     closeable.close(); // Normal close - exceptions not ignored
 * } catch (Exception e) {
 *     // error handling
 * } finally {
 *     FrcIoExtsKt.closeQuietly(closeable); // In case normal close was skipped due to Exception**
 * }
 * ```
 *
 *
 * Closing all streams:
 *
 * ```
 * try {
 *     return FrcIoExtsKt.copy(inputStream, outputStream);
 * } finally {
 *     FrcIoExtsKt.closeQuietly(inputStream, outputStream);
 * }
 * ```
 *
 * @param closeables the objects to close, may be null or already closed
 *
 * @see .closeQuietly
 * @see Throwable.addSuppressed
 */
fun closeQuietly(vararg closeables: Closeable?)
{
    for (closeable in closeables)
    {
        closeable.closeQuietly()
    }
}

/**
 * Closes a `Socket` unconditionally.
 *
 *
 * Equivalent to [Socket.close], except any exceptions will be ignored.
 * This is typically used in finally blocks.
 *
 *
 * Example code:
 * ```
 * Socket socket = null;
 * try {
 *     socket = new Socket("http://www.foo.com/", 80);
 *     // process socket
 *     socket.close();
 * } catch (Exception e) {
 *     // error handling
 * } finally {
 *     FrcIoExtsKt.closeQuietly(socket);
 * }
 * ```
 *
 * @receiver the Socket to close, may be null or already closed
 *
 * @see Throwable.addSuppressed
 */
fun Socket?.closeQuietly()
{
    if (this != null)
    {
        try
        {
            close()
        }
        catch (ioe: IOException)
        {
            // ignored

        }
    }
}

/**
 * Closes a `Selector` unconditionally.
 *
 *
 * Equivalent to [Selector.close], except any exceptions will be ignored.
 * This is typically used in finally blocks.
 *
 *
 * Example code:
 * ```
 * Selector selector = null;
 * try {
 *     selector = Selector.open();
 * // process socket
 *
 * } catch (Exception e) {
 *     // error handling
 * } finally {
 *     FrcIoExtsKt.closeQuietly(selector);
 * }
 * ```
 *
 * @receiver the Selector to close, may be null or already closed
 *
 * @see Throwable.addSuppressed
 */
fun Selector?.closeQuietly()
{
    if (this != null)
    {
        try
        {
            close()
        }
        catch (ioe: IOException)
        {
            // ignored

        }
    }
}

/**
 * Closes a `ServerSocket` unconditionally.
 *
 *
 * Equivalent to [ServerSocket.close], except any exceptions will be ignored.
 * This is typically used in finally blocks.
 *
 *
 * Example code:
 * ```
 * ServerSocket socket = null;
 * try {
 *     socket = new ServerSocket();
 *     // process socket
 *     socket.close();
 * } catch (Exception e) {
 *     // error handling
 *} finally {
 *     FrcIoExtsKt.closeQuietly(socket);
 * }
 * ```
 *
 * @receiver the ServerSocket to close, may be null or already closed
 *
 * @see Throwable.addSuppressed
 */
fun ServerSocket?.closeQuietly()
{
    if (this != null)
    {
        try
        {
            close()
        }
        catch (ioe: IOException)
        {
            // ignored

        }
    }
}

/**
 * Convenience wrapper for [Files.copy] that creates parent directories before the move
 * and can set the [StandardCopyOption.REPLACE_EXISTING] option (set by default). If
 * `replaceExisting` is set to false and the file already exists, a
 * [java.nio.file.FileAlreadyExistsException] is thrown by the underlying operation
 * and wrapped in a [FrcPluginFileAlreadyExistsError].
 *
 * @return the target file or the Error that prevented completion.
 */
fun Path.copyTo(target: Path, replaceExisting: Boolean = true): Result<Path, FrcPluginIoError>
{
    return this.modifyOperation(target, createTargetParents = true) { theSource, theTarget ->
        if (replaceExisting)
            Files.copy(theSource, theTarget, StandardCopyOption.REPLACE_EXISTING)
        else
            Files.copy(theSource, theTarget)
        target
    }
}

/**
 * Convenience wrapper for [Files.move] that creates parent directories before the move
 * and can set the [StandardCopyOption.REPLACE_EXISTING] option (set by default). If
 * `replaceExisting` is set to false and the file already exists, a
 * [java.nio.file.FileAlreadyExistsException] is thrown by the underlying operation
 * and wrapped in a [FrcPluginFileAlreadyExistsError].
 *
 * @return the target file or the Error that prevented completion.
 */
fun Path.moveTo(target: Path, replaceExisting: Boolean = true): Result<Path, FrcPluginIoError>
{
    return this.modifyOperation(target, createTargetParents = true) { theSource, theTarget ->
        if (replaceExisting)
            Files.move(theSource, theTarget, StandardCopyOption.REPLACE_EXISTING)
        else
            Files.move(theSource, theTarget)
        target
    }
}

fun Path.modifyOperation(target: Path, createTargetParents: Boolean = true, operation: (source:Path, target: Path) -> Path): Result<Path, FrcPluginIoError>
{
    return try
    {
        if (createTargetParents) target.parent?.createDirectories()
        Ok(operation.invoke(this, target))
    }
    catch (t: Throwable)
    {
        logger.info("[FRC] Could NOT copy $this to $target. Reason: $t")
        val error: FrcPluginIoError = when (t)
        {
            is FileNotFoundException          -> FrcPluginSourceFileNotFoundError(t)
            is FileAlreadyExistsException -> FrcPluginFileAlreadyExistsError(t)
            else                      -> FrcPluginIoError(t)
        }
        Err(error)
    }
}