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

package net.javaru.iip.frc.actions.create.advanced.cmdBased.profiledPidSubsystem;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiDirectory;

import net.javaru.iip.frc.actions.create.advanced.ClassCreator;
import net.javaru.iip.frc.actions.create.advanced.CreateFrcComponentDataProvider;
import net.javaru.iip.frc.actions.create.advanced.cmdBased.subsystem.CreateSubsystemComponentDialog;



public class CreateProfiledPidSubsystemComponentDialog extends CreateSubsystemComponentDialog
{
    public CreateProfiledPidSubsystemComponentDialog(@NotNull Module module,
                                                     @NotNull ClassCreator classCreator,
                                                     @NotNull PsiDirectory directory,
                                                     @NotNull CreateFrcComponentDataProvider dataProvider)
    {
        super(module, classCreator, directory, dataProvider);
    }
    
    
    @Override
    protected boolean showTheIncludeJavaDocCheckbox()
    {
        return true;
    }
}
