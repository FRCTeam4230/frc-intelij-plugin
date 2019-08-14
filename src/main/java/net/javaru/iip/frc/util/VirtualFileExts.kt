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

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import org.jdom2.filter.Filters
import org.jdom2.input.SAXBuilder
import org.jdom2.xpath.XPathFactory


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