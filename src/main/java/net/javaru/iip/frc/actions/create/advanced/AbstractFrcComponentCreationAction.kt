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
package net.javaru.iip.frc.actions.create.advanced

import com.intellij.ide.actions.CreateInDirectoryActionBase
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.WriteActionAware
import com.intellij.openapi.module.Module
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiDirectory
import net.javaru.iip.frc.actions.create.advanced.cmdBased.FrcComponentCreationDialog
import net.javaru.iip.frc.facet.isFrcFacetedModule

// Based on  org.jetbrains.idea.devkit.actions.service.NewServiceActionBase  and its inner classes in the 'devkit-core' IJ module
/**
 * Base class for creating a new Class with a custom dialog for getting necessary information for creation of the class.
 */
abstract class AbstractFrcComponentCreationAction protected constructor(
    text: String,
    description: String,
    protected val dataProvider: FrcComponentCreationDataProvider
                                                                       ) : CreateInDirectoryActionBase(text, description, dataProvider.icon), WriteActionAware
{
    protected abstract fun constructCreateFrcComponentDialogInstance(
        module: Module,
        classCreator: ClassCreator,
        directory: PsiDirectory
                                                                    ): FrcComponentCreationDialog

    protected fun constructClassCreatorInstance(module: Module): ClassCreator
    {
        return ClassCreator(module, dataProvider)
    }

    override fun update(e: AnActionEvent)
    {
        val module = e.getData(LangDataKeys.MODULE)
        val presentation = e.presentation

        // Note, on the Presentation, there is a "setEnabled() and a "setEnabledAndVisible() method. 
        // If you set just the first, the action shows in the menu, but dithered our.
        presentation.isEnabledAndVisible = module.isFrcFacetedModule() && shouldBeEnabledAdditionalCriteria(module!!, e)
    }

    override fun actionPerformed(e: AnActionEvent)
    {
        val view = e.getData(LangDataKeys.IDE_VIEW)
        val project = e.project
        val module = e.getData(LangDataKeys.MODULE)
        if (view == null || project == null || module == null)
        {
            return
        }
        val dir = view.orChooseDirectory ?: return
        val classCreator = constructClassCreatorInstance(module)
        val createdClasses = invokeDialog(module, classCreator, dir) ?: return
        for (createdClass in createdClasses)
        {
            view.selectElement(createdClass)
        }
    }

    private fun invokeDialog(module: Module, classCreator: ClassCreator, dir: PsiDirectory): Array<PsiClass>
    {
        val dialog: DialogWrapper = constructCreateFrcComponentDialogInstance(module, classCreator, dir)
        dialog.show()
        // When the user clicks OK, dialog.doOKAction() is called, which in turn calls ClassCreator.createClass() which creates the class(es) 
        return classCreator.createdClasses
    }

    /**
     * Provides additional criteria when determining if the action should be enabled (in the menu).
     * Verification that the module is an FRC module is already done and does NOT need to occur in
     * implementations of this method.
     *
     * @param module the module that the action was activated for as retrieved form the action event via
     *               `e.getData(LangDataKeys.MODULE)` (Reminder: the project is available via the `module.getProject()` method)
     * @param e      the original action event
     *
     * @return whether the action should be enabled.
     */
    protected open fun shouldBeEnabledAdditionalCriteria(module: Module, e: AnActionEvent): Boolean
    {
        return true
    }

    override fun startInWriteAction(): Boolean
    {
        // We will be showing a modal dialog, so we need to return false. 
        // We are then responsible for starting write actions properly 
        // by calling {@link Application#runWriteAction(Runnable)}.  
        return false
    }
}