/*
 * Copyright 2015-2021 the original author or authors
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

import com.intellij.openapi.diagnostic.logger
import org.intellij.lang.annotations.Language
import org.jdom2.Document
import org.jdom2.JDOMException
import org.jdom2.input.SAXBuilder
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

private object XmlUtils
private val logger = logger<XmlUtils>()

/**
 * Converts a XML String to a JDOM2 [Document]. Will throw a `JDOMException` if the conversion
 * fails. For a non throwing alternative, see [toXmlDocumentOrNull].
 */
@Throws(IOException::class, JDOMException::class)
fun @receiver:Language("XML") String.toXmlDocument(): Document
{
    val charset = determineXmlEncoding(this)
    return ByteArrayInputStream(this.toByteArray(charset)).use { inputStream ->
        SAXBuilder().build(inputStream)
    }
}

/**
 * Converts a XML String to a JDOM2 [Document]. Will return `null` if the
 * conversion fails (or the receiver XML String is null).
 */
fun @receiver:Language("XML") String?.toXmlDocumentOrNull(): Document?
{
    return try
    {
        this?.toXmlDocument()
    }
    catch (t: Throwable)
    {
        logger.warn("[FRC] Could not convert xml string to a XML Document. Cause: $t")
        null
    }
}

/** Converts an InputStream of XML data to a JDOM2 [Document]. **/
@Throws(IOException::class, JDOMException::class)
fun InputStream.toXmlDocument(charset: Charset = StandardCharsets.UTF_8): Document = SAXBuilder().build(InputStreamReader(this, charset))


