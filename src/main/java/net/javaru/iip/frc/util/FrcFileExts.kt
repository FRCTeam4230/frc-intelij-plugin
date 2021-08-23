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


import org.apache.commons.io.FilenameUtils
import kotlin.streams.toList


fun findCommonParentDir(thePaths: Collection<String>): String
{
    val paths = thePaths.stream().map { it.toCommonSeparatorPath() }.toList()
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

