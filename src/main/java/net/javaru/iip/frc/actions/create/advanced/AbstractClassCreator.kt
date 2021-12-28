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

import net.javaru.iip.frc.i18n.FrcBundle.message
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiDirectory
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.ide.actions.ElementCreator
import com.intellij.psi.JavaDirectoryService
import com.intellij.util.IncorrectOperationException
import com.intellij.ide.actions.CreateFileAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.text.StringUtil
import com.intellij.util.ExceptionUtil
import java.lang.Exception
import java.util.concurrent.Callable

abstract class AbstractClassCreator<T : PsiElement> protected constructor(
    module: Module,
    dataProvider: FrcComponentCreationDataProvider
                                                                          ) : ClassCreator<T>
{
    protected val myProject: Project
    protected val myModule: Module
    protected val dataProvider: FrcComponentCreationDataProvider
    protected var createdClassesInternal = emptyList<T>()

    override val createdClasses: List<T>
        get() = createdClassesInternal

    init
    {
        myProject = module.project
        myModule = module
        this.dataProvider = dataProvider
    }

    override fun createClass(name: String, directory: PsiDirectory, additionalProperties: Map<String, String>): Boolean
    {
        return doCreateClass {
            val psiClass = createSingleClass(
                name,
                dataProvider.fileTemplateName,
                directory,
                additionalProperties
                                            )
            // A null class really should only happen in extreme cases, such as an exception occurring somewhere along the line
            createdClassesInternal = psiClass?.let { listOf(it) } ?: emptyList()
            true
        }
    }

    /**
     * Executes the creation process in a WriteActionCommand. The supplied action should set the createdClasses field.
     *
     * @return whether the class was created (which indicates whether the FrcComponentCreationDialog can be closed).
     */
    protected fun doCreateClass(action: Callable<Boolean>): Boolean
    {
        return try
        {
            WriteCommandAction.writeCommandAction(myProject)
                .withName(message("frc.new.class.adv.action.name"))
                .withUndoConfirmationPolicy(UndoConfirmationPolicy.REQUEST_CONFIRMATION)
                .compute<Boolean, Exception> { action.call() }
        }
        catch (e: Exception)
        {
            handleException(e)
            false
        }
    }

    protected fun handleException(t: Throwable)
    {
        LOG.info("[FRC] Exception when creating new class: $t", t)
        val errorMessage = ElementCreator.getErrorMessage(t)
        Messages.showMessageDialog(
            myProject, errorMessage, message("frc.new.class.adv.action.error.title"), Messages.getErrorIcon()
                                  )
    }

    override fun checkCanCreateClass(directory: PsiDirectory, name: String, classTypeSimpleName: String): String?
    {
        var currentDir = directory
        val packageName = StringUtil.getPackageName(name)
        if (!packageName.isEmpty())
        {
            for (dir in packageName.split("\\.").toTypedArray())
            {
                val childDir = currentDir.findSubdirectory(dir) ?: return null
                currentDir = childDir
            }
        }
        return try
        {
            JavaDirectoryService.getInstance().checkCreateClass(currentDir, StringUtil.getShortName(name))
            null
        }
        catch (e: IncorrectOperationException)
        {
            val exceptionMessage = ExceptionUtil.getMessage(e)
            if (exceptionMessage != null) message(
                "frc.new.class.adv.validation.cantCreate.detailed",
                classTypeSimpleName,
                exceptionMessage
                                                 )
            else message("frc.new.class.adv.validation.cantCreate.short", classTypeSimpleName)
        }
    }

    override fun createSingleClass(name: String, classTemplateName: String, directory: PsiDirectory): T?
    {
        return createSingleClass(name, classTemplateName, directory, emptyMap())
    }

    override fun createSingleClass(name: String, classTemplateName: String, directory: PsiDirectory, additionalProperties: Map<String, String>): T?
    {
        var normalizedName = name
        var normalizedirectory = directory
        if (normalizedName.contains("."))
        {
            val names = normalizedName.split("\\.").toTypedArray()
            for (i in 0 until names.size - 1)
            {
                normalizedirectory = CreateFileAction.findOrCreateSubdirectory(normalizedirectory, names[i])
            }
            normalizedName = names[names.size - 1]
        }
        return createSingleClassImpl(normalizedName, classTemplateName, normalizedirectory, additionalProperties)
    }

    protected abstract fun createSingleClassImpl(normalizedName: String, classTemplateName: String, normalizedDirectory: PsiDirectory, additionalProperties: Map<String, String>): T?

    companion object
    {
        protected val LOG = Logger.getInstance(AbstractClassCreator::class.java)
    }
}