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

package net.javaru.iip.frc.actions.create.advanced.cmdBased.profiledPidSubsystem.v2

import com.google.common.collect.ImmutableList
import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import net.javaru.iip.frc.actions.create.advanced.BaseType
import net.javaru.iip.frc.actions.create.advanced.FrcComponentCreationDataProvider
import net.javaru.iip.frc.templates.FrcFileTemplateGroupDescriptorFactory
import net.javaru.iip.frc.wpilib.WpiLibConstants


object ProfiledPidSubsystem2ComponentCreationDataProvider : FrcComponentCreationDataProvider()
{
    override val componentVersion: Int = 2
    override val componentTypeSimpleName: String = "ProfiledPIDSubsystem"
    override val componentTypeSimpleNames: List<String> by lazy {
        ImmutableList
            .of("Subsystem", "System", "SubSystem", "PIDSubsystem", "PidSubsystem", "PIDSubSystem", "PidSubSystem")
    }
    override val autoAppendSuffix: String = "PIDSubsystem"
    override val baseType: BaseType = BaseType.BaseClassOnly
    override val topLevelClassFqName: String = WpiLibConstants.PROFILED_PID_SUBSYSTEM_BASE_V2_FQN
    override val typicalBaseClassFqName: String = WpiLibConstants.PROFILED_PID_SUBSYSTEM_BASE_V2_FQN
    override val fileTemplateDescriptor: FileTemplateDescriptor = FrcFileTemplateGroupDescriptorFactory.PROFILED_PID_SUBSYSTEM2
}