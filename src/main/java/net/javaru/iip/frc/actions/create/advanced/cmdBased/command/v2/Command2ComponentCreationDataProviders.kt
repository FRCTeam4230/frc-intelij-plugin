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

package net.javaru.iip.frc.actions.create.advanced.cmdBased.command.v2

import com.google.common.collect.ImmutableList
import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.openapi.project.Project
import net.javaru.iip.frc.actions.create.advanced.BaseType
import net.javaru.iip.frc.actions.create.advanced.cmdBased.command.CommandCreationDataProvider
import net.javaru.iip.frc.templates.FrcFileTemplateGroupDescriptorFactory
import net.javaru.iip.frc.wpilib.getConfiguredProjectYearJustYear
import net.javaru.iip.frc.wpilib.WpiLibConstants

abstract class AbstractCommand2ComponentCreationDataProvider : CommandCreationDataProvider()
{
    override val componentVersion: Int = 2
    override val componentTypeSimpleName: String = "Command"
    override val componentTypeSimpleNames: List<String> by lazy { ImmutableList.of("Command", "Cmd") }
    override val baseType: BaseType = BaseType.InterfaceAndBaseClass
    override val topLevelClassFqName: String = WpiLibConstants.COMMAND_V2_INTERFACE_FQN
    // TODO - Need to get this to select either COMMAND_V2_BASE_FQN_2024Plus or COMMAND_V2_BASE_FQN_Pre2024 based on project frc Year… but that wil take major rework :( so that we have access to the project so we can use Project.getConfiguredProjectYearJustYear()
    override val typicalBaseClassFqName: String = WpiLibConstants.COMMAND_V2_BASE_FQN_2024Plus
    override val subsystemTopFqName: String = WpiLibConstants.SUBSYSTEM_V2_TOP_FQN

    fun getTopLevelClassFqName(frcProjectYear: Int): String
    {
        // TODO: This function is a workaround for Issue #150. We need to improve so that all Data Providers use a function like this for class names
        //       rather than a val.
        return if (frcProjectYear < 2024) WpiLibConstants.COMMAND_V2_BASE_FQN_Pre2024 else WpiLibConstants.COMMAND_V2_BASE_FQN_2024Plus
    }
}

object Command2JavaComponentCreationDataProvider : AbstractCommand2ComponentCreationDataProvider()
{
    override val fileTemplateDescriptor: FileTemplateDescriptor = FrcFileTemplateGroupDescriptorFactory.COMMAND2
}

object Command2KotlinComponentCreationDataProvider : AbstractCommand2ComponentCreationDataProvider()
{
    override val fileTemplateDescriptor: FileTemplateDescriptor = FrcFileTemplateGroupDescriptorFactory.COMMAND2_KOT
}