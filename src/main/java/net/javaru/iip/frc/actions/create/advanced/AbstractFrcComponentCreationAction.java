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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.IdeView;
import com.intellij.ide.actions.CreateInDirectoryActionBase;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.WriteActionAware;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;

import net.javaru.iip.frc.actions.create.advanced.cmdBased.FrcComponentCreationDialog;
import net.javaru.iip.frc.facet.FrcFacetKt;


// Based on  org.jetbrains.idea.devkit.actions.service.NewServiceActionBase  and its inner classes in the 'devkit-core' IJ module

/**
 * Base class for creating a new Class with a custom dialog for getting necessary information for creation of the class.
 */
public abstract class AbstractFrcComponentCreationAction extends CreateInDirectoryActionBase implements WriteActionAware
{
    private static final Logger LOG = Logger.getInstance(AbstractFrcComponentCreationAction.class);
    
    @NotNull
    protected final FrcComponentCreationDataProvider dataProvider;
    
    protected AbstractFrcComponentCreationAction(@NotNull String text,
                                                 @NotNull String description,
                                                 @NotNull FrcComponentCreationDataProvider dataProvider)
    {
        super(text, description, dataProvider.getIcon());
        this.dataProvider = dataProvider;
    }
    
    
    
    
    protected abstract FrcComponentCreationDialog constructCreateFrcComponentDialogInstance(@NotNull Module module,
                                                                                            @NotNull ClassCreator classCreator,
                                                                                            @NotNull PsiDirectory directory);
    
    
    protected ClassCreator constructClassCreatorInstance(@NotNull Module module)
    {
        return new ClassCreator(module, dataProvider);
    }
    
    @NotNull
    protected FrcComponentCreationDataProvider getDataProvider()
    {
        return dataProvider;
    }
    
    
    @Override
    public void update(AnActionEvent e)
    {
        final Module module = e.getData(LangDataKeys.MODULE);
        final Presentation presentation = e.getPresentation();
        
        // Note, on the Presentation, there is a "setEnabled() and a "setEnabledAndVisible() method. 
        // If you set just the first, the action shows in the menu, but dithered our.
        presentation.setEnabledAndVisible(FrcFacetKt.isFrcFacetedModule(module) && shouldBeEnabledAdditionalCriteria(module));
    }
    
    
    @Override
    public final void actionPerformed(@NotNull AnActionEvent e)
    {
        final IdeView view = e.getData(LangDataKeys.IDE_VIEW);
        final Project project = e.getProject();
        final Module module = e.getData(LangDataKeys.MODULE);
        if (view == null || project == null || module == null)
        {
            return;
        }
        PsiDirectory dir = view.getOrChooseDirectory();
        if (dir == null) return;
        
        ClassCreator classCreator = constructClassCreatorInstance(module);
        
        PsiClass[] createdClasses = invokeDialog(module, classCreator, dir);
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
    private PsiClass[] invokeDialog(@NotNull Module module, ClassCreator classCreator, PsiDirectory dir)
    {
        DialogWrapper dialog = constructCreateFrcComponentDialogInstance(module, classCreator, dir);
        dialog.show();
        // When the user clicks OK, dialog.doOKAction() is called, which in turn calls ClassCreator.createClass() which creates the class(es) 
        return classCreator.getCreatedClasses();
    }
    
    
    /**
     * Provides additional criteria when determining if the action should be enabled (in the menu).
     * Verification that the module is an FRC module is already done and does NOT need to occur in
     * implementations of this method.
     * 
     * @param module the module that the action was activated for (remember the project is available via the module.getProject() method)
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
