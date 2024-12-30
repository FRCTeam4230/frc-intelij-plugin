/*
 * Copyright 2015-2024 the original author or authors.
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

package net.javaru.iip.frc.templates

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString


object TempyCopy
{
    val templateDir = Path.of("P:\\dev\\proj\\javaru\\intellij-idea-plugins\\IntelliFRC\\code\\FRC\\src\\main\\resources\\frc-wizard-templates")
    @JvmStatic
    fun main(args: Array<String>)
    {
        copyGitKeepFiles()
        
    }
    
    fun copyGitKeepFiles()
    {
        val y2024 = templateDir.resolve("2024")
        val y2025 = templateDir.resolve("2025")
        y2024.toFile().walk().filter {
            it.isFile && it.name == ".gitkeep"
        }.forEach {
          val dest = Path.of(it.toPath().absolutePathString().replace("2024", "2025"))
            println("Will copy $it to $dest")
            it.copyTo(dest.toFile(), overwrite = true)
        }
    }
    
    fun copyKotlinCodeDirs() {
        val y2024 = templateDir.resolve("2024")
        val y2025 = templateDir.resolve("2025")
        //y2024.toFile().copyRecursively(y2025.toFile(), overwrite = true)
        y2024.toFile().walk().filter {
            val asPath = it.toPath()
            asPath.fileName.toString() == "kotlin-code" && asPath.parent.parent.fileName.toString() != "commandBase" && asPath.parent.parent.fileName.toString() != "-DEFAULT-FILES-"
        }.forEach {
            val asPath = it.toPath()
            val templateName = asPath.parent.parent.fileName.toString()
            val dest = y2025.resolve(templateName).resolve(asPath.parent.fileName.toString()).resolve(asPath.fileName.toString())
            Files.createDirectories(dest)
            it.copyRecursively(dest.toFile(), overwrite = true)
        }
    }
}