/*
 * Copyright 2015-2020 the original author or authors
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

import com.esotericsoftware.minlog.Log
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifier
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiElementProcessor
import com.intellij.psi.search.PsiElementProcessorAdapter
import com.intellij.psi.search.searches.ClassInheritorsSearch
import net.javaru.iip.frc.i18n.FrcBundle
import org.jetbrains.annotations.Contract
import java.util.*
import javax.swing.JComponent

object FindClassUtils{}

private val LOG = Logger.getInstance(FindClassUtils::class.java)


@Contract("null, _ -> !null; !null, null -> !null")
fun findClass(project: Project?, fqn: String?): Array<PsiClass?>
{
    if (project == null || fqn == null)
    {
        return arrayOfNulls(0)
    }
    val scope = GlobalSearchScope.allScope(project)
    val facade = JavaPsiFacade.getInstance(project)
    return facade.findClasses(fqn, scope)
}

fun findClassAssumeOneIfAny(project: Project?, fqn: String?): PsiClass?
{
    val classes = findClass(project, fqn)
    return takeFirstPsiClass(classes, fqn)
}

@Contract("null, _ -> !null; !null, null -> !null")
fun findClass(module: Module?, fqn: String?): Array<PsiClass?>
{
    if (module == null || fqn == null)
    {
        return arrayOfNulls(0)
    }
    val scope = GlobalSearchScope.moduleScope(module)
    val facade = JavaPsiFacade.getInstance(module.project)
    return facade.findClasses(fqn, scope)
}

fun findClassAssumeOneIfAny(module: Module?, fqn: String?): PsiClass?
{
    val classes = findClass(module, fqn)
    return takeFirstPsiClass(classes, fqn)
}

private fun takeFirstPsiClass(classes: Array<PsiClass?>, fqn: String?): PsiClass?
{
    return if (classes.isEmpty())
    {
        null
    }
    else
    {
        if (classes.size > 1)
        {
            LOG.warn("[FRC] Multiple classes found for '$fqn' when it was assumed only a single instance would be found. Found instances: $classes")
        }
        classes[0]
    }
}

@Suppress("unused")
@Contract("null, _ -> false; !null, null -> false")
fun isLibraryPresent(project: Project?, keyClassFqn: String?): Boolean
{
    if (project == null || keyClassFqn == null) return false
    val possibleClasses = findClass(project, keyClassFqn)
    return possibleClasses.isNotEmpty()
}


@Suppress("unused")
@Contract("null, _ -> false; !null, null -> false")
fun isLibraryPresent(module: Module?, keyClassFqn: String?): Boolean
{
    if (module == null || keyClassFqn == null) return false
    val possibleClasses = findClass(module, keyClassFqn)
    return possibleClasses.isNotEmpty()
}


/* 
   ************************************************************************************
    The findImplementations methods, particularly the core worker method  
    findImplementationsForScope(PsiClass,GlobalSearchScope, JComponent) 
    are based on:
        com.intellij.codeInsight.daemon.impl.MarkerType.navigateToSubclassedClass()  
                                                        navigateToOverriddenMethod()
        com.intellij.refactoring.util.RefactoringHierarchyUtil._findImplementingClasses 
    ************************************************************************************
*/


/**
 * This method Should be run inside a `DumbService.runReadActionInSmartMode()` call.
 * Finds all the implementations of a class within a module. The base class, as identified
 * by the `classFQN`, will be looked for in the entire project. If better (i.e. more fine)
 * scoping is needed for which base class is used, use the overridden version that takes a
 * `GlobalSearchScope baseClassSearchScope` parameter, or the one that takes the
 * `PsiClass` rather than a String representing the class. The results are not in any
 * particular order. It is left up to the caller to sort as needed.
 *
 * @param classFQN            the Fully Qualified Name (FQN) of the class/interface to find inheritors of
 * @param module              the module to search within
 * @param includeDependencies whether inheritors in module dependencies should be included in the results
 * @param includeLibraries    whether inheritors in module libraries should be included in the results
 * @param parentComponent     the (optional) component which will be used to calculate the progress window ancestor
 *
 * @return the found implementations, in no particular order
 */
@Suppress("unused")
fun findImplementationsInModule(classFQN: String,
                                module: Module,
                                includeDependencies: Boolean,
                                includeLibraries: Boolean,
                                parentComponent: JComponent?): List<PsiClass>
{
    val searchScope = calculateModuleSearchScope(module, includeDependencies, includeLibraries)
    return findImplementationsForScope(module.project,
                                       classFQN,
                                       GlobalSearchScope.allScope(module.project),
                                       searchScope,
                                       parentComponent)
}

/**
 * This method Should be run inside a `DumbService.runReadActionInSmartMode()` call.
 * Finds all the implementations of a class within a module. The results are not in any
 * particular order. It is left up to the caller to sort as needed.
 *
 * @param psiClass            the class/interface to find inheritors of
 * @param module              the module to search within
 * @param includeDependencies whether inheritors in module dependencies should be included in the results
 * @param includeLibraries    whether inheritors in module libraries should be included in the results
 * @param parentComponent     the (optional) component which will be used to calculate the progress window ancestor
 *
 * @return the found implementations, in no particular order
 */
@Suppress("unused")
fun findImplementationsInModule(psiClass: PsiClass,
                                module: Module,
                                includeDependencies: Boolean,
                                includeLibraries: Boolean,
                                parentComponent: JComponent?): List<PsiClass>
{
    val searchScope = calculateModuleSearchScope(module, includeDependencies, includeLibraries)
    return findImplementationsForScope(psiClass, searchScope, parentComponent)
}

/**
 * This method Should be run inside a `DumbService.runReadActionInSmartMode()` call.
 * Finds all the implementations of a class within a global search scope. The results are not
 * in any particular order. It is left up to the caller to sort as needed.
 *
 * @param project                    The project that is being searched; however the entire project is not necessarily searched
 * as the search scope is defined by the `implementationsSearchScope` parameter
 * @param classFQN                   the Fully Qualified Name (FQN) of the class/interface to find inheritors of
 * @param baseClassSearchScope       the scope to search for the base class
 * @param implementationsSearchScope the scope to search inheritors in
 * @param parentComponent            the (optional) component which will be used to calculate the progress window ancestor
 *
 * @return the found implementations, in no particular order
 */
@Suppress("MemberVisibilityCanBePrivate")
fun findImplementationsForScope(project: Project,
                                classFQN: String,
                                baseClassSearchScope: GlobalSearchScope,
                                implementationsSearchScope: GlobalSearchScope,
                                parentComponent: JComponent?): List<PsiClass>
{
    val facade = JavaPsiFacade.getInstance(project)
    val possibleClasses = facade.findClasses(classFQN, baseClassSearchScope)
    if (possibleClasses.isEmpty())
    {
        Log.info("[FRC] Could not find base class/interface '" + classFQN + "' in project '" + project.name
                 + "' and therefore cannot look for implementations/subclasses")
    }
    val result: MutableSet<PsiClass> = HashSet()
    // We should only have one, but we still check all
    for (psiClass in possibleClasses)
    {
        result.addAll(findImplementationsForScope(psiClass, implementationsSearchScope, parentComponent))
    }
    return ArrayList(result)
}

/**
 * This method Should be run inside a `DumbService.runReadActionInSmartMode()` call.
 * Finds all the implementations of a class within a global search scope. The results are not
 * in any particular order. It is left up to the caller to sort as needed.
 *
 * @param psiClass                   the class/interface to find inheritors of
 * @param implementationsSearchScope the scope to search inheritors in
 * @param parentComponent            the (optional) component which will be used to calculate the progress window ancestor
 *
 * @return the found implementations, in no particular order
 */
@Suppress("MemberVisibilityCanBePrivate")
fun findImplementationsForScope(psiClass: PsiClass,
                                implementationsSearchScope: GlobalSearchScope,
                                parentComponent: JComponent?): List<PsiClass>
{ 
    // based on:
    //     com.intellij.codeInsight.daemon.impl.MarkerType.navigateToSubclassedClass()  
    //                                                     navigateToOverriddenMethod()
    //     com.intellij.refactoring.util.RefactoringHierarchyUtil._findImplementingClasses
    val inheritors: MutableList<PsiClass> = ArrayList()
    ProgressManager.getInstance()
        .runProcessWithProgressSynchronously(
                {
                    val query = ClassInheritorsSearch
                        .search(psiClass, implementationsSearchScope, true)
                    query
                        .forEach(PsiElementProcessorAdapter(PsiElementProcessor { psiClass ->
                            LOG
                                .trace("[FRC] Checking psiClass '" + psiClass.qualifiedName + "' of type " + psiClass.javaClass)
                            if (!psiClass.isInterface)
                            {
                                inheritors
                                    .add(psiClass)
                            }
                            true
                        }))
                }, FrcBundle.message("frc.util.findImplementations.progress.title", psiClass.name), true, psiClass.project, parentComponent)
    return inheritors
}

private fun calculateModuleSearchScope(module: Module,
                                       includeDependencies: Boolean,
                                       includeLibraries: Boolean): GlobalSearchScope
{
    return if (includeDependencies && includeLibraries)
    {
        GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module)
    }
    else if (includeDependencies)
    {
        GlobalSearchScope.moduleWithDependenciesScope(module)
    }
    else if (includeLibraries)
    {
        GlobalSearchScope.moduleWithLibrariesScope(module)
    }
    else
    {
        GlobalSearchScope.moduleScope(module)
    }
}

fun PsiClass.isAbstract(): Boolean
{
    // boolean isAbstract1 = psiClass.hasModifier(JvmModifier.ABSTRACT); // As of 2019-12-26 this is marked as experimental
    return this.hasModifierProperty(PsiModifier.ABSTRACT);
}

fun PsiClass.isInterfaceOrAbstract(): Boolean = this.isInterface || this.isAbstract()

fun PsiClass.isFinal(): Boolean =  this.hasModifierProperty(PsiModifier.FINAL)
fun PsiClass.isOpen(): Boolean =  !this.isFinal()

fun MutableList<PsiClass>.sortByName()
{
    this.sortWith(PsiClassNameComparator)
    
}

fun Collection<PsiClass>.sortedByName(): List<PsiClass>
{
    val mutableList = this.toMutableList()
    mutableList.sortByName()
    return mutableList
}

object PsiClassNameComparator: Comparator<PsiClass>
{
    override fun compare(o1: PsiClass?, o2: PsiClass?): Int
    {
        val name1 = if (o1?.name == null) "" else o1.name!!
        val name2 = if (o2?.name == null) "" else o2.name!!
        return name1.compareTo(name2)
    }
}
