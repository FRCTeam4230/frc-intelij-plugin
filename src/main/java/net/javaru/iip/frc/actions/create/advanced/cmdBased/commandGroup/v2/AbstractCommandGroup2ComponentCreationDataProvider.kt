/*
 * Copyright 2015-2022 the original author or authors.
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

package net.javaru.iip.frc.actions.create.advanced.cmdBased.commandGroup.v2

import com.google.common.collect.ImmutableList
import net.javaru.iip.frc.actions.create.advanced.BaseType
import net.javaru.iip.frc.actions.create.advanced.cmdBased.command.CommandCreationDataProvider
import net.javaru.iip.frc.wpilib.WpiLibConstants


abstract class AbstractCommandGroup2ComponentCreationDataProvider : CommandCreationDataProvider()
{
    override val componentVersion: Int = 2
    override val componentTypeSimpleName: String = "CommandGroup"
    override val componentTypeSimpleNames: List<String> by lazy { ImmutableList.of("Command", "Cmd, CommandGroup", "CmdGroup", "CommandGrp", "CmdGrp") }
    override val baseType: BaseType = BaseType.InterfaceAndBaseClass
    override val topLevelClassFqName: String = WpiLibConstants.SEQUENTIAL_COMMAND_GROUP_V2_BASE_FQN
    override val typicalBaseClassFqName: String = WpiLibConstants.SEQUENTIAL_COMMAND_GROUP_V2_BASE_FQN
    override val additionalBaseClassFqNames: List<String> by lazy {
        ImmutableList.of(
            "edu.wpi.first.wpilibj2.command.ParallelCommandGroup",
            "edu.wpi.first.wpilibj2.command.ParallelDeadlineGroup",
            "edu.wpi.first.wpilibj2.command.ParallelRaceGroup"
                                )
    }
    override val subsystemTopFqName: String = ""
    override val includeSubsystemChooser: Boolean = false
    override val showJavaDocForOverridesOption: Boolean = false
}