/*
 * Copyright 2015-2016 Mark Vedder
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.javaru.iip.frc.actions.create.basic;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.actions.JavaCreateTemplateInPackageAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

import net.javaru.iip.frc.FrcIcons;



public abstract class AbstractNewFrcBasicClassAction extends JavaCreateTemplateInPackageAction<PsiClass> implements DumbAware 
{
    protected static final Icon DEFAULT_ICON = FrcIcons.FIRST_ICON_MEDIUM_16;

    protected AbstractNewFrcBasicClassAction(String text)
    {
        this(text, DEFAULT_ICON);
    }
    
    protected AbstractNewFrcBasicClassAction(String text, Icon icon)
    {
        this(text, text, icon, true);
    }
    
    
    protected AbstractNewFrcBasicClassAction(String text, String description, Icon icon)
    {
        this(text, description, icon, true);    
    }
    
    protected AbstractNewFrcBasicClassAction(String text, String description, Icon icon, boolean inSourceOnly)
    {
        super(text, description, icon, true);
    }


    @Nullable
    protected PsiElement getNavigationElement(@NotNull PsiClass createdElement)
    {
        return createdElement.getNameIdentifier();
    }


    @Nullable
    protected PsiClass doCreate(PsiDirectory dir, String className, String templateName) throws IncorrectOperationException
    {
        return JavaDirectoryService.getInstance().createClass(dir, className, templateName, true);
    }
}
