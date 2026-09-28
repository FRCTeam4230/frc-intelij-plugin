/*
 * Copyright 2015-2022 the original author or authors.
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

package net.javaru.iip.frc.inspections

import com.intellij.codeInspection.CleanupLocalInspectionTool
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiField
import com.intellij.psi.PsiModifier
import com.intellij.util.text.NameUtilCore
import com.siyeh.ig.BaseInspection
import com.siyeh.ig.BaseInspectionVisitor
import com.siyeh.ig.fixes.RenameFix
import net.javaru.iip.frc.i18n.FrcInspectionsBundle
import net.javaru.iip.frc.util.capitalize2
import java.util.*


class HungarianNotationConstantsInspection: BaseInspection(), CleanupLocalInspectionTool
{
    override fun getID(): String = "HungarianNotationConstants"

    override fun buildErrorString(vararg infos: Any?): String = FrcInspectionsBundle.message("frc.inspections.hungarian.notation.k.constants.descriptor")

    override fun buildQuickFixesOnlyForOnTheFlyErrors(): Boolean = true

    override fun buildVisitor(): BaseInspectionVisitor
    {
        return object : BaseInspectionVisitor() {

            override fun visitField(field: PsiField)
            {
                super.visitField(field)
                if (field.hasModifierProperty(PsiModifier.STATIC)
                    && field.hasModifierProperty(PsiModifier.FINAL)
                    && field.isKNamedConstant())
                {
                    registerFieldError(field, field)
                }
            }

            fun PsiField.isKNamedConstant(): Boolean = this.name.startsWith('k') && this.name.length > 1 && this.name[1].isUpperCase()
        }
    }

    override fun buildFix(vararg infos: Any): LocalQuickFix
    {
        val field = infos[0] as PsiField
        val targetName = field.createNewName()
        return RenameFix(targetName)
    }

    private fun PsiField.createNewName() : String
    {
        return if (containingClass?.isEnum == true) {
            name.substring(1).capitalize2()
        }
        else {
            // Based on com.intellij.refactoring.rename.JavaNameSuggestionProvider#suggestProperlyCasedName() : if (kind == VariableKind.STATIC_FINAL_FIELD) clause
            NameUtilCore.splitNameIntoWordList(name.substring(1)).joinToString("_") { StringUtil.toUpperCase(it) }
        }
    }
}
