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
package net.javaru.iip.frc.actions.create.advanced.cmdBased.pidSubsystem.v1

import com.intellij.openapi.module.Module
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDirectory
import net.javaru.iip.frc.actions.create.advanced.ClassCreator
import net.javaru.iip.frc.actions.create.advanced.cmdBased.AbstractCmdBaseV1ComponentCreationAction
import net.javaru.iip.frc.actions.create.advanced.cmdBased.FrcComponentCreationDialog
import net.javaru.iip.frc.actions.create.advanced.cmdBased.subsystem.SubsystemComponentCreationDialog

class PidSubsystem1ComponentCreationAction :
    AbstractCmdBaseV1ComponentCreationAction(PidSubsystem1ComponentCreationDataProvider)
{
    override fun constructCreateFrcComponentDialogInstance(module: Module, classCreator: ClassCreator<PsiClass>, directory: PsiDirectory): FrcComponentCreationDialog
    {
        return SubsystemComponentCreationDialog(module, classCreator, directory, dataProvider)
    }
}