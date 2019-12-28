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

package net.javaru.iip.frc.actions.create.advanced;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.actions.CreateFileAction;
import com.intellij.ide.actions.ElementCreator;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.util.ExceptionUtil;
import com.intellij.util.IncorrectOperationException;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public abstract class ClassCreator
{
    private static final Logger LOG = Logger.getInstance(ClassCreator.class);
    
    
    @NotNull
    private final Project myProject;
    @NotNull
    private final Module myModule;

    private PsiClass[] createdClasses = null;

    

    protected ClassCreator(@NotNull Module module)
    {
        this.myModule = module;
        this.myProject = module.getProject();
    }
    
    
    @NotNull
    protected abstract String getClassTemplateName();
    
    
    PsiClass[] getCreatedClasses()
    {
        return createdClasses;
    }
    
    
    public boolean createClass(@NotNull String name,
                               @NotNull PsiDirectory directory,
                               @NotNull Map<String, String> additionalProperties)
    {
        return doCreateClass(() -> {
            final PsiClass psiClass = createSingleClass(name,
                                                        getClassTemplateName(),
                                                        directory,
                                                        additionalProperties);
            createdClasses = new PsiClass[] {psiClass};
            return true;
        });
        
        
    }
    
    
    /**
     * Executes the creation process in a WriteActionCommand. The supplied action should set the createdClasses field.
     * 
     * @return whether the class was created (which indicates whether the CreateFrcComponentDialog can be closed).
     */
    protected boolean doCreateClass(Callable<Boolean> action)
    {
        try
        {
            return WriteCommandAction.writeCommandAction(myProject)
                    .withName(message("frc.new.class.adv.action.name"))
                    .withUndoConfirmationPolicy(UndoConfirmationPolicy.REQUEST_CONFIRMATION)
                    .compute(action::call);
        }
        catch (Exception e)
        {
            handleException(e);
            return false;
        }
    }
    
    
    private void handleException(Throwable t)
    {
        LOG.info("[FRC] Exception when creating new class: "+ t.toString(), t);
        String errorMessage = ElementCreator.getErrorMessage(t);
        Messages.showMessageDialog(
                myProject, errorMessage, message("frc.new.class.adv.action.error.title"), Messages.getErrorIcon());
    }
    
    
    @Nullable
    public static String checkCanCreateClass(PsiDirectory directory, String name, String classTypeSimpleName)
    {
        PsiDirectory currentDir = directory;
        String packageName = StringUtil.getPackageName(name);
        if (!packageName.isEmpty())
        {
            for (String dir : packageName.split("\\."))
            {
                PsiDirectory childDir = currentDir.findSubdirectory(dir);
                if (childDir == null)
                {
                    return null;
                }
                currentDir = childDir;
            }
        }
        try
        {
            JavaDirectoryService.getInstance().checkCreateClass(currentDir, StringUtil.getShortName(name));
            return null;
        }
        catch (IncorrectOperationException e)
        {
            String exceptionMessage = ExceptionUtil.getMessage(e);
            return exceptionMessage != null 
                   ? message("frc.new.class.adv.validation.cantCreate.detailed", classTypeSimpleName, exceptionMessage) 
                   : message("frc.new.class.adv.validation.cantCreate.short", classTypeSimpleName );
        }
    }
    
    public static PsiClass createSingleClass(@NotNull String name,
                                             @NotNull String classTemplateName,
                                             @NotNull PsiDirectory directory)
    {
        return createSingleClass(name, classTemplateName, directory, Collections.emptyMap());
    }
    
    public static PsiClass createSingleClass(@NotNull String name,
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
