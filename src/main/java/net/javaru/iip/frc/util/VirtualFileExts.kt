/*
 * Copyright 2015-2019 the original author or authors
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

import com.intellij.application.options.CodeStyle
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.impl.LoadTextUtil
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import org.jdom2.filter.Filters
import org.jdom2.input.SAXBuilder
import org.jdom2.xpath.XPathFactory
import java.io.File
import java.nio.file.Path


private val LOG = Logger.getInstance("#net.javaru.iip.frc.util.VirtualFileExts")

private val AntDetectionXPathExpression = XPathFactory.instance().compile("//project/property[@file] | //bookstore/import[@file]", Filters.element())

fun VirtualFile.isWpiAntBuildFile(): Boolean
{
    var result = false
    try
    {
        this.inputStream.use { inputStream ->
            val document = SAXBuilder().build(inputStream)
            val elements = AntDetectionXPathExpression.evaluate(document)
            for (element in elements)
            {
                val attribute = element.getAttribute("file")
                var value: String? = attribute.getValue()
                if (value != null)
                {
                    value = value.toLowerCase()
                    if (value.contains("wpilib") || value.contains("wpi-lib"))
                    {
                        result = true
                        break
                    }
                }
            }
        }
    }
    catch (e: Exception)
    {
        LOG.warn("[FRC] an exception occurred when checking if a file is a WPI Ant Build File. Details: $e", e)
    }

    return result
}

fun VirtualFile.appendToFile(text: String)
{
    val lineSeparator = this.lineSeparator()
    val existingText = StringUtil.trimTrailing(VfsUtilCore.loadText(this))
    val content = (if (StringUtil.isNotEmpty(existingText)) existingText + lineSeparator else "") +
                  StringUtil.convertLineSeparators(text, lineSeparator)
    VfsUtil.saveText(this, content)
}


fun VirtualFile.lineSeparator(): String
{
    var lineSeparator = LoadTextUtil.detectLineSeparator(this, true)
    if (lineSeparator == null)
    {
        lineSeparator = CodeStyle.getDefaultSettings().lineSeparator
    }
    return lineSeparator!!
}

/**
 * @param parent the parent directory to create the file (or sub directories and file) in
 * @param path the path (or fileName) to create such as `foo.txt` or `src/main/java/App.java`
 */
private fun getOrCreateVfFile(parent: VirtualFile, path: Path): VirtualFile? = getOrCreateVfFile(parent.path, path.toString())

/**
 * @param parent the parent directory to create the file (or sub directories and file) in
 * @param path the path (or fileName) to create such as `foo.txt` or `src/main/java/App.java`
 */
private fun getOrCreateVfFile(parent: VirtualFile, path: String): VirtualFile? = getOrCreateVfFile(parent.path, path)

/**
 * @param parent the parent directory to create the file (or sub directories and file) in
 * @param path the path (or fileName) to create such as `foo.txt` or `src/main/java/App.java`
 */
private fun getOrCreateVfFile(parent: String, path: String): VirtualFile?
{
    val file = File(parent, path)
    FileUtilRt.createIfNotExists(file)
    return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
}





// ======= The following is copied from the JetBrains ktor plugin and generator. We might be able to replace with other code ========
//      ktor-init-tools\ktor-intellij-plugin\src\io\ktor\start\intellij\util\Utils.kt
//      ktor-init-tools\ktor-generator\src\commonMain\kotlin\io\ktor\start\util\FileMode.kt
//      ktor-init-tools\ktor-generator\src\commonMain\kotlin\io\ktor\start\util\charset.kt
//      ktor-init-tools\ktor-generator\src\commonMain\kotlin\io\ktor\start\util\baos.kt


//operator fun VirtualFile?.get(path: String?): VirtualFile?
//{
//    if (this == null || path == null || path == "" || path == ".") return this
//    val parts = path.split('/', limit = 2)
//    val firstName = parts[0]
//    val lastName = parts.getOrNull(1)
//    val child = this.findChild(firstName)
//    return if (lastName != null) child[lastName] else child
//}
//
//fun VirtualFile.myCreateFile(path: String, data: String, charset: Charset = UTF8, mode: FileMode = FileMode("0644")): VirtualFile =
//        myCreateFile(path, data.toByteArray(charset), mode)
//
//fun VirtualFile.myCreateFile(path: String, data: ByteArray, mode: FileMode = FileMode("0644")): VirtualFile
//{
//    val file = PathInfo(path)
//    val fileParent = file.parent
//    val dir = this.createDirectories(fileParent)
//    return runWriteAction {
//        dir.createChildData(null, file.name).apply {
//            setBinaryContent(data)
//            if (isInLocalFileSystem)
//            {
//                canonicalPath?.let { cp ->
//                    val lfile = File(cp)
//                    if (lfile.exists())
//                    {
//                        if (((mode.mode ushr 6) and 1) != 0)
//                        { // Executable bit on the user part
//                            lfile.setExecutable(true)
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//fun VirtualFile.createDirectories(path: String?): VirtualFile
//{
//    if (path == null) return this
//    return runWriteAction {
//        val parts = path.split('/', limit = 2)
//        val firstName = parts[0]
//        val lastName = parts.getOrNull(1)
//        val child = this.findChild(firstName) ?: this.createChildDirectory(null, firstName)
//        if (lastName != null) child.createDirectories(lastName) else child
//    }
//}
//
//
//class PathInfo(val path: String)
//{
//    val name: String get() = path.substringAfterLast('/', path)
//    val parent: String? get() = if (path.contains('/')) path.substringBeforeLast('/', "") else null
//}
//
//data class FileMode(val mode: Int)
//{
//    constructor(octalMode: String) : this(octalMode.toInt(8))
//}
//
//interface Charset
//{
//    fun decode(out: StringBuilder, src: ByteArray, start: Int, end: Int)
//    fun encode(out: ByteArrayOutputStream, src: CharSequence, start: Int, end: Int)
//}
//
//object UTF8 : Charset
//{
//    private fun createByte(codePoint: Int, shift: Int): Int = codePoint shr shift and 0x3F or 0x80
//
//    override fun decode(out: StringBuilder, src: ByteArray, start: Int, end: Int)
//    {
//        var i = start
//        while (i < end)
//        {
//            val c = src[i++].toInt() and 0xFF
//            when (c shr 4)
//            {
//                0, 1, 2, 3, 4, 5, 6, 7 ->
//                {
//                    // 0xxxxxxx
//                    out.append(c.toChar())
//                }
//                12, 13                 ->
//                {
//                    // 110x xxxx   10xx xxxx
//                    out.append((c and 0x1F shl 6 or (src[i++].toInt() and 0x3F)).toChar())
//                }
//                14                     ->
//                {
//                    // 1110 xxxx  10xx xxxx  10xx xxxx
//                    out.append((c and 0x0F shl 12 or (src[i++].toInt() and 0x3F shl 6) or (src[i++].toInt() and 0x3F)).toChar())
//                }
//                else                   -> error("Invalid UTF string")
//            }
//        }
//    }
//
//    override fun encode(out: ByteArrayOutputStream, src: CharSequence, start: Int, end: Int)
//    {
//        for (n in start until end)
//        {
//            val codePoint = src[n].toInt()
//
//            if (codePoint and 0x7F.inv() == 0)
//            { // 1-byte sequence
//                out.u8(codePoint)
//            }
//            else
//            {
//                when
//                {
//                    codePoint and 0x7FF.inv() == 0  -> // 2-byte sequence
//                        out.u8((codePoint shr 6 and 0x1F or 0xC0))
//                    codePoint and 0xFFFF.inv() == 0 ->
//                    { // 3-byte sequence
//                        out.u8((codePoint shr 12 and 0x0F or 0xE0))
//                        out.u8((createByte(codePoint, 6)))
//                    }
//                    codePoint and -0x200000 == 0    ->
//                    { // 4-byte sequence
//                        out.u8((codePoint shr 18 and 0x07 or 0xF0))
//                        out.u8((createByte(codePoint, 12)))
//                        out.u8((createByte(codePoint, 6)))
//                    }
//                }
//                out.u8((codePoint and 0x3F or 0x80))
//            }
//        }
//    }
//}
//
//object ASCII : Charset
//{
//    override fun decode(out: StringBuilder, src: ByteArray, start: Int, end: Int)
//    {
//        for (n in start until end) out.append(src[n].toChar())
//    }
//
//    override fun encode(out: ByteArrayOutputStream, src: CharSequence, start: Int, end: Int)
//    {
//        for (n in start until end) out.u8(src[n].toInt())
//    }
//}
//
//fun ByteArray.toString(charset: Charset): String =
//        StringBuilder().apply { charset.decode(this, this@toString, 0, this@toString.size) }.toString()
//
//fun String.toByteArray(charset: Charset): ByteArray =
//        buildByteArray { charset.encode(this, this@toByteArray, 0, this@toByteArray.length) }
//
//class ByteArrayOutputStream
//{
//    private var pos = 0
//    private var data = ByteArray(1024)
//    val size get() = pos
//
//    private fun ensure(count: Int): ByteArrayOutputStream
//    {
//        if (pos + count > data.size)
//        {
//            data = data.copyOf(max(pos + count, data.size * 2))
//        }
//        return this
//    }
//
//    private inline fun byte(v: Number) = run { data[pos++] = v.toByte() }
//    fun u8(v: Int) = ensure(1).apply { byte(v) }
//    fun u16_le(v: Int) = ensure(2).apply { byte(v shr 0); byte(v shr 8) }
//    fun u32_le(v: Int) = ensure(4).apply { byte(v shr 0); byte(v shr 8); byte(v shr 16); byte(v shr 24) }
//    fun bytes(data: ByteArray) = ensure(data.size).apply { for (n in 0 until data.size) byte(data[n]) }
//
//    fun toByteArray(): ByteArray
//    {
//        return data.copyOf(pos)
//    }
//
//    inline fun build(builder: ByteArrayOutputStream.() -> Unit): ByteArray
//    {
//        builder(this)
//        return toByteArray()
//    }
//}
//
//inline fun buildByteArray(builder: ByteArrayOutputStream.() -> Unit): ByteArray = ByteArrayOutputStream().build(builder)
