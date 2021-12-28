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

package net.javaru.iip.frc.actions.create.advanced;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import com.intellij.ide.actions.CreateFileAction;
import com.intellij.openapi.module.Module;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;



public class JavaClassCreator extends AbstractClassCreator<PsiClass>
{
    public JavaClassCreator(@NotNull Module module, @NotNull FrcComponentCreationDataProvider dataProvider)
    {
        super(module, dataProvider);
    }
    
    
    @Override
    public PsiClass createSingleClass(@NotNull String name,
                                      @NotNull String classTemplateName,
                                      @NotNull PsiDirectory directory,
                                      @NotNull Map<String, String> additionalProperties)
    {
        if (name.contains("."))
        {
            String[] names = name.split("\\.");
            for (int i = 0; i < names.length - 1; i++)
            {
                directory = CreateFileAction.findOrCreateSubdirectory(directory, names[i]);
            }
            name = names[names.length - 1];
        }
    
        return JavaDirectoryService.getInstance().createClass(directory, name, classTemplateName, false, additionalProperties);
    }
}
