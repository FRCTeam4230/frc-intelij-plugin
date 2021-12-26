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

package net.javaru.iip.frc.actions.create.advanced.cmdBased.trigger.v2

import com.google.common.collect.ImmutableList
import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import net.javaru.iip.frc.actions.create.advanced.BaseType
import net.javaru.iip.frc.actions.create.advanced.cmdBased.trigger.TriggerCreationDataProvider
import net.javaru.iip.frc.templates.FrcFileTemplateGroupDescriptorFactory
import net.javaru.iip.frc.wpilib.WpiLibConstants


object Trigger2ComponentCreationDataProvider : TriggerCreationDataProvider()
{
    override val componentVersion: Int = 2
    override val componentTypeSimpleName: String = "Trigger"
    override val componentTypeSimpleNames: List<String> by lazy { ImmutableList.of("Trigger", "Button", "Btn") }
    override val baseType: BaseType = BaseType.BaseClassOnly
    override val topLevelClassFqName: String = ""
    override val typicalBaseClassFqName: String = WpiLibConstants.TRIGGER_V2_BASE_FQN
    override val fileTemplateDescriptor: FileTemplateDescriptor = FrcFileTemplateGroupDescriptorFactory.TRIGGER2
}