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

package net.javaru.iip.frc.actions.create.simple;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.actions.JavaCreateTemplateInPackageAction;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

import net.javaru.iip.frc.FrcIcons;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



/**
 * Base class for creating a new Class that uses the default dialog(s) for getting necessary information for creation of the class.
 */
public abstract class AbstractSimpleNewFrcClassAction extends JavaCreateTemplateInPackageAction<PsiClass> implements DumbAware
{
    protected static final Icon DEFAULT_ICON = FrcIcons.FRC.FIRST_ICON_MEDIUM_16;


    @SuppressWarnings("unused")
    protected AbstractSimpleNewFrcClassAction(String text)
    {
        this(text, DEFAULT_ICON);
    }


    protected AbstractSimpleNewFrcClassAction(String text, Icon icon)
    {
        this(text, text, icon, true);
    }


    protected AbstractSimpleNewFrcClassAction(String text, String description, Icon icon)
    {
        this(text, description, icon, true);
    }


    protected AbstractSimpleNewFrcClassAction(String text, String description, Icon icon, boolean inSourceOnly)
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
    protected boolean isAvailable(DataContext dataContext)
    {
        final Project project = CommonDataKeys.PROJECT.getData(dataContext);
        return super.isAvailable(dataContext) && project != null && notInDumbMode(project) && shouldBeEnabledAdditionalCriteria(project);
    }
    
    
    /**
     * Indicates if the {@link #isAvailable(DataContext)} checks needs to check that the IDE is nor in dumb mode (i,e. that
     * indexes are being updated) because the availability check, usually done by {@link #shouldBeEnabledAdditionalCriteria(Project)},
     * runs checks, such as what libraries are attached, during its operation. Default it {@code true}.
     * @return if the {@link #isAvailable(DataContext)} checks needs to check that the IDE is nor in dumb mode 
     */
    protected boolean needsIndexesToCheckEnabling()
    {
        return true;
    }
    
    protected boolean notInDumbMode(@NotNull Project project)
    {
        if (needsIndexesToCheckEnabling() && DumbService.isDumb(project))
        {
            DumbService.getInstance(project).showDumbModeNotification(message("frc.new.class.adv.general.inDumbMode"));
            return false;
        }
        return true;
    }
    
    /**
     * Provides additional criteria when determining if the action should be enabled (in the menu).
     * Verification that the module is an FRC module is already done and does NOT need to occur in
     * implementations of this method.
     * 
     * Also see {@link #needsIndexesToCheckEnabling()}
     *
     * @return whether the action should be enabled.
     */
    protected boolean shouldBeEnabledAdditionalCriteria(@NotNull Project project)
    {
        return true;
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
        //          at net.javaru.iip.frc.actions.create.simple.AbstractSimpleNewFrcClassAction.doCreate(AbstractSimpleNewFrcClassAction.java:74)
        // In JavaDirectoryServiceImpl.java:129 it checks:
        //      LOG.assertTrue(!ApplicationManager.getApplication().isWriteAccessAllowed());
        // this was added on 2016-12-19 via the commit https://github.com/JetBrains/intellij-community/commit/ff87813
        // So likely first appeared in v2017.1, or a late version of 2016.3.x
        // Based on this forum thread: https://intellij-support.jetbrains.com/hc/en-us/community/posts/115000431244-assertion-fail-in-LOG-assertTrue-ApplicationManager-getApplication-isWriteAccessAllowed-
        // We are overriding this method to return false. Although this seems counter to what the above fix was trying to solve.
        // I believe it is due to the Javadoc comment which states to return false if presenting a modal dialog box, and then the
        // action itself is responsible for starting write action when needed
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
