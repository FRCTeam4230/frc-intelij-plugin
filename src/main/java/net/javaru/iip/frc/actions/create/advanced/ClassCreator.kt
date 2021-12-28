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

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiDirectory

interface ClassCreator<T : PsiElement>
{
    val createdClasses: List<T>

    fun createClass(name: String, directory: PsiDirectory, additionalProperties: Map<String, String>): Boolean

    fun checkCanCreateClass(directory: PsiDirectory, name: String, classTypeSimpleName: String): String?

    fun createSingleClass(name: String, classTemplateName: String, directory: PsiDirectory): T?

    fun createSingleClass(name: String, classTemplateName: String, directory: PsiDirectory, additionalProperties: Map<String, String>): T?
}