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
import com.intellij.psi.PsiField
import com.siyeh.ig.BaseInspection
import com.siyeh.ig.BaseInspectionVisitor
import com.siyeh.ig.InspectionGadgetsFix
import com.siyeh.ig.fixes.RenameFix
import net.javaru.iip.frc.i18n.FrcInspectionsBundle
import net.javaru.iip.frc.util.capitalize2
import net.javaru.iip.frc.util.decapitalize2


class HungarianNotationMemberVariablesInspection: BaseInspection(), CleanupLocalInspectionTool
{
    override fun getID(): String = "HungarianNotationMemberVariables"

    override fun buildErrorString(vararg infos: Any?): String = FrcInspectionsBundle.message("frc.inspections.hungarian.notation.member.variables.descriptor")

    override fun buildQuickFixesOnlyForOnTheFlyErrors(): Boolean = true

    override fun buildVisitor(): BaseInspectionVisitor
    {
        return object : BaseInspectionVisitor() {

            override fun visitField(field: PsiField)
            {
                super.visitField(field)
                if (field.isMNamedField())
                {
                    registerFieldError(field, field)
                }
            }

            fun PsiField.isMNamedField(): Boolean = this.name.startsWith("m_") && this.name.length > 2
        }
    }

    override fun buildFix(vararg infos: Any): InspectionGadgetsFix
    {
        val field = infos[0] as PsiField
        val targetName = field.createNewName()
        return RenameFix(targetName)
    }

    private fun PsiField.createNewName() : String
    {
        return if (containingClass?.isEnum == true)
            name.substring(2).capitalize2()
        else
            name.substring(2).decapitalize2()
    }
}
