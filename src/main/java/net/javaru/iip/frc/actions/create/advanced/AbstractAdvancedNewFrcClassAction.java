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

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateInDirectoryActionBase;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.application.WriteActionAware;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;

import net.javaru.iip.frc.facet.FrcFacetKt;


// Based on  org.jetbrains.idea.devkit.actions.service.NewServiceActionBase  and its inner classes in the 'devkit-core' IJ module

/**
 * Base class for creating a new Class with a custom dialog for getting necessary information for creation of the class.
 */
public abstract class AbstractAdvancedNewFrcClassAction extends CreateInDirectoryActionBase implements WriteActionAware
{
    private static final Logger LOG = Logger.getInstance(AbstractAdvancedNewFrcClassAction.class);
    
    
    protected AbstractAdvancedNewFrcClassAction(String text,
                                             String description,
                                             Icon icon)
    {
        super(text, description, icon);
    }
    
    
    protected abstract ClassCreator constructClassCreatorInstance(@NotNull Project project);
    
    protected abstract NewFrcClassDialog constructNewClassDialogInstance(@NotNull Project project,
                                                                         @NotNull ClassCreator classCreator,
                                                                         @NotNull PsiDirectory directory);
    
    
    
    @Override
    public void update(AnActionEvent e)
    {
        Module module = e.getData(LangDataKeys.MODULE);
        e.getPresentation().setEnabled(FrcFacetKt.isFrcFacetedModule(module) && shouldBeEnabledAdditionalCriteria(module));
    }
    
    
    @Override
    public final void actionPerformed(@NotNull AnActionEvent e)
    {
        IdeView view = e.getData(LangDataKeys.IDE_VIEW);
        Project project = e.getProject();
        if (view == null || project == null)
        {
            return;
        }
        PsiDirectory dir = view.getOrChooseDirectory();
        if (dir == null) return;
        
        ClassCreator classCreator = constructClassCreatorInstance(project);
        
        PsiClass[] createdClasses = invokeDialog(project, classCreator, dir);
        if (createdClasses == null)
        {
            return;
        }
        
        for (PsiClass createdClass : createdClasses)
        {
            view.selectElement(createdClass);
        }
    }
    
    
    @Nullable
    private PsiClass[] invokeDialog(@NotNull Project project, ClassCreator classCreator, PsiDirectory dir)
    {
        DialogWrapper dialog = constructNewClassDialogInstance(project, classCreator, dir);
        dialog.show();
        // When the user clicks OK, dialog.doOKAction() is called which in turn calls ClassCreator.createClass() which creates the class(es) 
        return classCreator.getCreatedClasses();
    }
    
    
    /**
     * Provides additional criteria when determining if the action should be enabled (in the menu).
     * Verification that the module is an FRC module is already done and does NOT need to occur in
     * implementations of this method.
     *
     * @return whether the action should be enabled.
     */
    protected boolean shouldBeEnabledAdditionalCriteria(@NotNull Module module)
    {
        return true;
    }
    
    @Override
    public boolean startInWriteAction()
    {
        // We will be showing a modal dialog, so we need to return false. 
        // We are then responsible for starting write actions properly 
        // by calling {@link Application#runWriteAction(Runnable)}.  
        return false;
    }
}
