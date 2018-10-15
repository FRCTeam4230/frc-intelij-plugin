/*
 * Copyright 2015-2018 the original author or authors
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

import com.intellij.openapi.module.Module
import java.nio.file.Path


@Suppress("DataClassPrivateConstructor")
data class LibDef internal constructor(val module: Module, val libName: String, val binDirs: Set<Path>, val srcDirs: Set<Path>, val docDirs: Set<Path>)

class LibDefBuilder(val module: Module, val libName: String)
{
    private val binDirs: MutableSet<Path> = HashSet()
    private val srcDirs: MutableSet<Path> = HashSet()
    private val docDirs: MutableSet<Path> = HashSet()
    
    fun addDir(path: Path, vararg addAs: LibDirType): LibDefBuilder
    {
        for (dirType in addAs)
        {
            when (dirType)
            {
                LibDirType.BIN -> binDirs.add(path)
                LibDirType.DOC -> docDirs.add(path)
                LibDirType.SRC -> srcDirs.add(path)
            }
        }
        return this
    }
    
    fun build() : LibDef = LibDef(module, libName, binDirs, srcDirs, docDirs)
}

enum class LibDirType
{
    BIN,
    DOC,
    SRC
}