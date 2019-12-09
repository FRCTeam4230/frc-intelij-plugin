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

package net.javaru.iip.frc.wpilib.jsonSchema

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType
import net.javaru.iip.frc.wpilib.wpiLibPreferencesFileName
import java.util.*


class WpiLibJsonSchemaProviderFactory: JsonSchemaProviderFactory
{
    override fun getProviders(project: Project): MutableList<JsonSchemaFileProvider>
    {
        return Collections.singletonList(object: JsonSchemaFileProvider {
            override fun getName(): String = "WPI Lib Preferences"

            override fun isAvailable(file: VirtualFile): Boolean = file.nameSequence.endsWith(wpiLibPreferencesFileName)

            // TODO - it would be nice to monitor https://github.com/wpilibsuite/vscode-wpilib/blob/master/vscode-wpilib/resources/wpilibschema.json for changes
            //        Do we need to change the SchemaType in the below method to SchemaType.remoteSchema
            override fun getSchemaFile(): VirtualFile? = VfsUtil.findFileByURL(javaClass.getResource(WPILIB_PREFERENCES_SCHEMA))

            override fun getSchemaType(): SchemaType = SchemaType.embeddedSchema
        })
    }

    // For intro on writing the schema specification, see https://json-schema.org/learn/getting-started-step-by-step.html
    companion object {
//        private const val WPILIB_PREFERENCES_SCHEMA = "/schemes/wpilibPreferencesDefault.schema.json"
        private const val WPILIB_PREFERENCES_SCHEMA = "/schemes/wpilibPreferencesEnhanced.schema.json"
    }
}