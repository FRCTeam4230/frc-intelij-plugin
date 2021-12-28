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

import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.openapi.module.Module
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement

class KotlinClassCreator(module: Module, dataProvider: FrcComponentCreationDataProvider) : AbstractClassCreator<PsiElement>(module, dataProvider)
{
    override fun createSingleClassImpl(normalizedName: String, classTemplateName: String, normalizedDirectory: PsiDirectory, additionalProperties: Map<String, String>): PsiElement
    {
        val template = FileTemplateManager.getInstance(normalizedDirectory.project).getInternalTemplate(classTemplateName)
        return FileTemplateUtil.createFromTemplate(template, normalizedName, additionalProperties.toProperties(), normalizedDirectory)
    }
}