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

package net.javaru.iip.frc.actions.create.advanced.cmdBased.subsystem.v1;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiDirectory;

import net.javaru.iip.frc.actions.create.advanced.ClassCreator;
import net.javaru.iip.frc.actions.create.advanced.cmdBased.AbstractCmdBaseV1ComponentCreationAction;
import net.javaru.iip.frc.actions.create.advanced.cmdBased.FrcComponentCreationDialog;
import net.javaru.iip.frc.actions.create.advanced.cmdBased.subsystem.SubsystemComponentCreationDialog;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public class Subsystem1ComponentCreationAction extends AbstractCmdBaseV1ComponentCreationAction
{
    public Subsystem1ComponentCreationAction()
    {
        super(message("frc.new.class.subsystem.action.name"),
              message("frc.new.class.subsystem.action.description"),
              Subsystem1ComponentCreationDataProvider.INSTANCE);
    }
    
    
    @Override
    protected FrcComponentCreationDialog constructCreateFrcComponentDialogInstance(@NotNull Module module,
                                                                                   @NotNull ClassCreator classCreator,
                                                                                   @NotNull PsiDirectory directory)
    {
        return new SubsystemComponentCreationDialog(module, classCreator, directory, dataProvider);
    }
    
}
