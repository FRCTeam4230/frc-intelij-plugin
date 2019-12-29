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

package net.javaru.iip.frc.actions.create.advanced.cmdBased.subsystem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.ImmutableMap.Builder;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;

import net.javaru.iip.frc.actions.create.advanced.ClassCreator;
import net.javaru.iip.frc.actions.create.advanced.CreateFrcComponentDataProvider;
import net.javaru.iip.frc.actions.create.advanced.cmdBased.CreateFrcComponentDialog;
import net.javaru.iip.frc.util.FrcClassUtilsKt;



public class CreateSubsystemComponentDialog extends CreateFrcComponentDialog
{
    private static final String INCLUDE_ADD_CHILD_MESSAGE = "includeChildMessage";
    
    public CreateSubsystemComponentDialog(@NotNull Module module,
                                          @NotNull ClassCreator classCreator,
                                          @NotNull PsiDirectory directory,
                                          @NotNull CreateFrcComponentDataProvider dataProvider)
    {
        super(module, classCreator, directory, dataProvider);
    }
    
    
    @Override
    protected void addComponentSpecificProperties(@NotNull Builder<String, String> props,
                                                  @Nullable PsiClass baseClass,
                                                  @NotNull String targetPackageName,
                                                  @NotNull String newClassName)
    {
        if (myDataProvider.getComponentVersion() >= 2 && baseClass != null)
        {
            final PsiClass subsystemBaseClass = FrcClassUtilsKt.findClassAssumeOneIfAny(myProject, myDataProvider.getTypicalBaseClassFqName());
            boolean includeChildMessage =  subsystemBaseClass != null && (baseClass.equals(subsystemBaseClass) ||  baseClass.isInheritor(subsystemBaseClass, true));
            props.put(INCLUDE_ADD_CHILD_MESSAGE, Boolean.toString(includeChildMessage));
        }
    }
    
    @Override
    protected boolean showTheIncludeJavaDocCheckbox()
    {
        return false;
    }
}
