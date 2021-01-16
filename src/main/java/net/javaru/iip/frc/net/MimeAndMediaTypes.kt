/*
 * Copyright 2015-2021 the original author or authors
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

package net.javaru.iip.frc.net

import com.google.common.net.MediaType


/**
 * While the guava MediaType class is nice, it does a bit or parsing and concatenation on the toString call.
 * We  to reduce that overhead to a one time occurrence.
 */
@Suppress("UnstableApiUsage")
object MimeAndMediaTypes
{
    val APPLICATION_BZIP2: String by lazy { MediaType.BZIP2.toString() }
    val APPLICATION_GZIP: String by lazy { MediaType.GZIP.toString() }
    val APPLICATION_JSON_UTF_8: String by lazy { MediaType.JSON_UTF_8.toString() }
    val APPLICATION_PDF: String by lazy { MediaType.PDF.toString() }
    val APPLICATION_ZIP: String by lazy { MediaType.ZIP.toString() }
    val APPLICATION_XML_UTF_8: String by lazy { MediaType.APPLICATION_XML_UTF_8.toString() }
    val TEXT_HTML_UTF_8: String by lazy { MediaType.HTML_UTF_8.toString() }
    val TEXT_PLAIN_TEXT_UTF_8: String by lazy { MediaType.PLAIN_TEXT_UTF_8.toString() }
    val TEXT_XML_UTF_8: String by lazy { MediaType.XML_UTF_8.toString() }
}

fun main()
{

    println(MimeAndMediaTypes.APPLICATION_XML_UTF_8)
    println(MimeAndMediaTypes.APPLICATION_JSON_UTF_8)
    println(MimeAndMediaTypes.APPLICATION_BZIP2)
    println(MimeAndMediaTypes.APPLICATION_GZIP)
    println(MimeAndMediaTypes.APPLICATION_ZIP)
    println(MimeAndMediaTypes.APPLICATION_PDF)
    println(MimeAndMediaTypes.TEXT_XML_UTF_8)
    println(MimeAndMediaTypes.TEXT_HTML_UTF_8)
    println(MimeAndMediaTypes.TEXT_PLAIN_TEXT_UTF_8)
}