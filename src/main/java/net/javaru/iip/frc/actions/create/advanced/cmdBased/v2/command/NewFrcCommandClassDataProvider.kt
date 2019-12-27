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

package net.javaru.iip.frc.actions.create.advanced.cmdBased.v2.command

import net.javaru.iip.frc.actions.create.advanced.NewFrcClassDataProvider
import net.javaru.iip.frc.wpilib.WpiLibConstants


object NewFrcCommandClassDataProvider : NewFrcClassDataProvider()
{
    override val classTypeSimpleName: String = "Command"
    override val classTypeSimpleNames: List<String> by lazy { listOf("Command", "Cmd") }
    override val topLevelClassFqName: String = WpiLibConstants.COMMAND_V2_INTERFACE_FQN
    override val typicalBaseClassFqName: String = WpiLibConstants.COMMAND_V2_BASE_FQN
}