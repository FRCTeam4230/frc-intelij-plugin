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

package net.javaru.iip.frc.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.nio.charset.Charset
import java.util.stream.Stream

internal class FrcIoExtsKtTest
{

    @ParameterizedTest
    @MethodSource
    fun determineXmlEncoding(xml: String, expectedCharset: Charset)
    {
        assertEquals(expectedCharset, determineXmlEncoding(xml))
    }


    @Suppress("unused")
    companion object
    {
        @JvmStatic
        fun determineXmlEncoding(): Stream<Arguments>
        {
            return Stream.of(
                // Since the default, on failures, is to return UTF-8, we favor testing with Charsets other than UTF-8
                Arguments.of("""<?xml version='1.0' encoding='US-ASCII' standalone='yes'?>""", Charsets.US_ASCII),
                Arguments.of("""<?xml version='1.0' encoding='us-ascii' standalone='yes'?>""", Charsets.US_ASCII),
                Arguments.of("""<?xml version='1.0' encoding='ISO-8859-1' standalone='yes'?>""", Charsets.ISO_8859_1),
                Arguments.of("""<?xml version='1.0' encoding='UTF-8' standalone='yes'?>""", Charsets.UTF_8),
                Arguments.of("""<?xml version='1.0' encoding='UTF-16' standalone='yes'?>""", Charsets.UTF_16),
                Arguments.of("""<?xml version='1.0' encoding="US-ASCII" standalone='yes'?>""", Charsets.US_ASCII),
                Arguments.of("""<?xml version="1.0" encoding="US-ASCII" standalone="yes"?>""", Charsets.US_ASCII),
                Arguments.of("""<?xml encoding="US-ASCII" version="1.0" standalone="yes"?>""", Charsets.US_ASCII),
                Arguments.of("""<?xml encoding = "US-ASCII" version="1.0" standalone="yes"?>""", Charsets.US_ASCII),
                Arguments.of("""<?  xml version='1.0' encoding='US-ASCII' standalone='yes'?>""", Charsets.US_ASCII),
                Arguments.of("""<?xml version="1.0" standalone="yes" encoding="US-ASCII" ?>""", Charsets.US_ASCII),
                Arguments.of("""     <?xml version="1.0" encoding="US-ASCII" standalone="yes"?>    """, Charsets.US_ASCII),
                Arguments.of("""<?xml version="1.0" ?>""", Charsets.UTF_8),
                Arguments.of("""<root></root>""", Charsets.UTF_8),
                            )
        }
    }
}