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

package net.javaru.iip.frc.actions.create.advanced.cmdBased.commandGroup.v2

import com.google.common.collect.ImmutableList
import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import net.javaru.iip.frc.actions.create.advanced.BaseType
import net.javaru.iip.frc.actions.create.advanced.cmdBased.command.CommandCreationDataProvider
import net.javaru.iip.frc.templates.FrcFileTemplateGroupDescriptorFactory
import net.javaru.iip.frc.wpilib.WpiLibConstants


object CommandGroup2ComponentCreationDataProvider : CommandCreationDataProvider()
{
    override val componentVersion: Int = 2
    override val componentTypeSimpleName: String = "CommandGroup"
    override val componentTypeSimpleNames: List<String> by lazy { ImmutableList.of("Command", "Cmd, CommandGroup", "CmdGroup", "CommandGrp", "CmdGrp") }
    override val baseType: BaseType = BaseType.InterfaceAndBaseClass
    override val topLevelClassFqName: String = WpiLibConstants.SEQUENTIAL_COMMAND_GROUP_V2_BASE_FQN
    override val typicalBaseClassFqName: String = WpiLibConstants.SEQUENTIAL_COMMAND_GROUP_V2_BASE_FQN
    override val additionalBaseClassFqNames: List<String> by lazy { ImmutableList.of<String>(
            "edu.wpi.first.wpilibj2.command.ParallelCommandGroup",
            "edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup",
            "edu.wpi.first.wpilibj2.command.ParallelRaceGroup"
                                                                                            ) }
    override val fileTemplateDescriptor: FileTemplateDescriptor = FrcFileTemplateGroupDescriptorFactory.COMMAND_GROUP2
    override val subsystemTopFqName: String = ""
    override val includeSubsystemChooser: Boolean = false

    override val showJavaDocForOverridesOption: Boolean = false
}