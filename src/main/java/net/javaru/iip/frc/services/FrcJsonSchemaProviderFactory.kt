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

package net.javaru.iip.frc.services

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType
import com.jetbrains.jsonSchema.impl.JsonSchemaVersion
import net.javaru.iip.frc.FrcPluginGlobals.IS_IN_FRC_INTERNAL_MODE
import net.javaru.iip.frc.wpilib.wpiLibPreferencesFileName
import java.net.URL


//TODO: It would be nice for the schema's to monitor the schemas in wpilib source code.
//      We need to investigate if using a schema type of SchemaType.remoteSchema can help
//      And if we can use an HttpVirtualFile. We would need to compensate for times the user is off line.

class FrcJsonSchemaProviderFactory : JsonSchemaProviderFactory
{
    private val logger = logger<FrcJsonSchemaProviderFactory>()
    // For intro on writing the schema specification, see https://json-schema.org/learn/getting-started-step-by-step.html

    override fun getProviders(project: Project): MutableList<JsonSchemaFileProvider>
    {
        return mutableListOf(
            object : JsonSchemaFileProvider
            {
                override fun getName(): String = "WPILib Preferences"

                override fun isAvailable(file: VirtualFile): Boolean = file.nameSequence.endsWith(wpiLibPreferencesFileName)

                override fun getSchemaFile(): VirtualFile? = getSchemaFileImpl(WPILIB_PREFERENCES_ENHANCED_SCHEMA)

                override fun getSchemaType(): SchemaType = SchemaType.embeddedSchema

                // our enhanced Schema uses v7 the default one provided by wpilib does not state a version
                override fun getSchemaVersion(): JsonSchemaVersion = JsonSchemaVersion.SCHEMA_7
            },
            object : JsonSchemaFileProvider
            {
                override fun getName(): String = "WPILib Vendor JSON Definition"

                override fun isAvailable(file: VirtualFile): Boolean = file.isVendordepsJsonFile(project = null)

                override fun getSchemaFile(): VirtualFile? = getSchemaFileImpl(VENDORDEPS_PREFERENCES_MODIFIED_SCHEMA)

                override fun getSchemaType(): SchemaType = SchemaType.embeddedSchema

                // The provided default schema does not state a version, but it seems to conform to 7 ok
                override fun getSchemaVersion(): JsonSchemaVersion = JsonSchemaVersion.SCHEMA_7
            }

                            )
    }
    
    private fun getSchemaFileImpl(schemaResourcePath: String): VirtualFile?
    {
        val resourceUrl: URL? = javaClass.getResource(schemaResourcePath)
        return if (resourceUrl != null)
            VfsUtil.findFileByURL(resourceUrl)
        else
        {
            // This should be rare, and discovered at development time and via unit tests
            val msg = "[FRC] Could not find schema file as a resource for schemaResourcePath: $schemaResourcePath"
            if (IS_IN_FRC_INTERNAL_MODE)
                logger.error(msg)
            else
                logger.warn(msg)

            null
        }
    }

    @Suppress("unused")
    companion object
    {
        // For intro on writing the schema specification, see https://json-schema.org/learn/getting-started-step-by-step.html
        /** Our internal copy of the `wpilib.preferences` schema. Original source: https://github.com/wpilibsuite/vscode-wpilib/blob/main/vscode-wpilib/resources/wpilibschema.json */
        private const val WPILIB_PREFERENCES_SCHEMA = "/schemes/wpilibPreferencesDefault.schema.json"
        /** Internal copy of our enhanced version of the `wpilib.preferences` schema which defines types for the properties, and sets some validation rules. */
        private const val WPILIB_PREFERENCES_ENHANCED_SCHEMA = "/schemes/wpilibPreferencesEnhanced.schema.json" // Our enhanced version
        /** Our internal copy of the `vendordeps` schema. Original source: https://github.com/wpilibsuite/vscode-wpilib/blob/main/vscode-wpilib/resources/vendorschema.json */
        private const val VENDORDEPS_PREFERENCES_SCHEMA = "/schemes/vendordeps.schema.json"
        /** Modified copy of the vendordeps that makes a few properties optional (i.e. removed from required list) as none of the vendordeps files have the properties in them.  */
        private const val VENDORDEPS_PREFERENCES_MODIFIED_SCHEMA = "/schemes/vendordeps.modified.schema.json"
    }
}