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

package net.javaru.iip.frc.util

import com.intellij.lang.java.lexer.JavaLexer
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.LanguageLevelProjectExtension
import com.intellij.openapi.util.text.StringUtil
import com.intellij.pom.java.LanguageLevel
import com.intellij.psi.PsiNameHelper
import com.intellij.util.lang.JavaVersion
import net.javaru.iip.frc.FrcPluginGlobals


fun getDefaultFrcPsiNameHelper(): FrcPsiNameHelper = FrcPsiNameHelper(FrcPluginGlobals.DEFAULT_MIN_REQUIRED_LANGUAGE_LEVEL)

// Based on PsiNameHelperImpl
//    that impl either requires a Project (which we don't necessarily have) or checks against LanguageLevel.LATEST
class FrcPsiNameHelper(_languageLevel: LanguageLevel?) : PsiNameHelper()
{
    private val languageLevel: LanguageLevel
    
    constructor(javaVersion: JavaVersion?) : this(javaVersion.extractLanguageLevel())
    
    constructor(project: Project): this(LanguageLevelProjectExtension.getInstance(project).languageLevel)

    init
    {
        if (_languageLevel == null)
        {
            //this.languageLevel = LanguageLevel.HIGHEST;
            this.languageLevel = FrcPluginGlobals.DEFAULT_MIN_REQUIRED_LANGUAGE_LEVEL
            LOG.debug("Supplied language level was null. Will use " + this.languageLevel.toString() + " as the language level")
        }
        else
        {
            this.languageLevel = _languageLevel
        }
    }

    companion object
    {
        private val LOG = Logger.getInstance(FrcPsiNameHelper::class.java)
    }

    /**
     * Checks if the specified text is a Java identifier, using the language level of the project
     * with which the name helper is associated to filter out keywords.
     *
     * @param text the text to check.
     *
     * @return true if the text is an identifier, false otherwise
     */
    override fun isIdentifier(text: String?): Boolean
    {
        return isIdentifier(text, languageLevel)
    }

    /**
     * Checks if the specified text is a Java identifier, using the specified language level
     * with which the name helper is associated to filter out keywords.
     *
     * @param text          the text to check.
     * @param languageLevel to check text against. For instance 'assert' or 'enum' might or might not be identifiers depending on language level
     *
     * @return true if the text is an identifier, false otherwise
     */
    override fun isIdentifier(text: String?, languageLevel: LanguageLevel): Boolean
    {
        return text != null && StringUtil.isJavaIdentifier(text) && !JavaLexer.isKeyword(text, languageLevel)
    }

    /**
     * Checks if the specified text is a Java keyword, using the language level of the project
     * with which the name helper is associated.
     *
     * @param text the text to check.
     *
     * @return true if the text is a keyword, false otherwise
     */
    override fun isKeyword(text: String?): Boolean
    {
        return text != null && JavaLexer.isKeyword(text, languageLevel)
    }

    /**
     * Checks if the specified string is a qualified name (sequence of identifiers separated by
     * periods).
     *
     * @param text the text to check.
     *
     * @return true if the text is a qualified name, false otherwise.
     */
    override fun isQualifiedName(text: String?): Boolean
    {
        if (text == null) return false
        var index = 0
        while (true)
        {
            var index1 = text.indexOf('.', index)
            if (index1 < 0) index1 = text.length
            if (!isIdentifier(text.substring(index, index1))) return false
            if (index1 == text.length) return true
            index = index1 + 1
        }
    }
}