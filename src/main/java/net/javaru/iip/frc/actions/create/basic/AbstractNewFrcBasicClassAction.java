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
    protected static final Icon DEFAULT_ICON = FrcIcons.FRC.FIRST_ICON_MEDIUM_16;


    @SuppressWarnings("unused")
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
        super(text, description, icon, inSourceOnly);
    }


    @Nullable
    protected PsiElement getNavigationElement(@NotNull PsiClass createdElement)
    {
        return createdElement.getNameIdentifier();
    }


    @Nullable
    protected PsiClass doCreate(PsiDirectory dir, String className, String templateName) throws IncorrectOperationException
    {
        return JavaDirectoryService.getInstance().createClass(dir, className, templateName, askForUndefinedVariables());
    }


    @Override
    public boolean startInWriteAction()
    {
        // During some testing, an "Assertion failed" was being thrown intermittently. This did not previously happen.
        // The Stacktrace:
        //        Assertion failed java.lang.Throwable:Assertion failed
        //          at com.intellij.openapi.diagnostic.Logger.assertTrue(Logger.java:172)
        //          at com.intellij.openapi.diagnostic.Logger.assertTrue(Logger.java:181)
        //          at com.intellij.psi.impl.file.JavaDirectoryServiceImpl.createClassFromTemplate(JavaDirectoryServiceImpl.java:129)
        //          at com.intellij.psi.impl.file.JavaDirectoryServiceImpl.createClass(JavaDirectoryServiceImpl.java:84)
        //          at com.intellij.psi.impl.file.JavaDirectoryServiceImpl.createClass(JavaDirectoryServiceImpl.java:76)
        //          at net.javaru.iip.frc.actions.create.basic.AbstractNewFrcBasicClassAction.doCreate(AbstractNewFrcBasicClassAction.java:74)
        // In JavaDirectoryServiceImpl.java:129 it checks:
        //      LOG.assertTrue(!ApplicationManager.getApplication().isWriteAccessAllowed());
        // this was added on 2016-12-19 via the commit https://github.com/JetBrains/intellij-community/commit/ff87813
        // So likely first appeared in v2017.1, or a late version of 2016.3.x
        // Based on this forum thread: https://intellij-support.jetbrains.com/hc/en-us/community/posts/115000431244-assertion-fail-in-LOG-assertTrue-ApplicationManager-getApplication-isWriteAccessAllowed-
        // We are overriding this method to return false. Although this seems counter to what the above fix was trying to solve.
        return false;
    }


    /**
     * Override if a template should NOT ask the user to define undefined variables. 
     * @return true by default
     */
    protected boolean askForUndefinedVariables()
    {
        return true;
    }
}
