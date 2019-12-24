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

package net.javaru.iip.frc.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.*;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.esotericsoftware.minlog.Log;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.search.PsiElementProcessorAdapter;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.util.Query;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public class FindClassUtils
{
    private static final Logger LOG = Logger.getInstance(FindClassUtils.class);
    
    
    @NotNull
    @Contract("null, _ -> !null; !null, null -> !null")
    public static PsiClass[] findClass(Project project, String fqn)
    {
        if (project == null || fqn == null) {return new PsiClass[0]; }
        
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        PsiClass[] possibleClasses = facade.findClasses(fqn, scope);
        return possibleClasses;
    }
    
    
    @NotNull
    @Contract("null, _ -> !null; !null, null -> !null")
    public static PsiClass[] findClass(Module module, String fqn)
    {
        if (module == null || fqn == null) {return new PsiClass[0]; }
        GlobalSearchScope scope = GlobalSearchScope.moduleScope(module);
        JavaPsiFacade facade = JavaPsiFacade.getInstance(module.getProject());
        PsiClass[] possibleClasses = facade.findClasses(fqn, scope);
        return possibleClasses;
    }
    
    
    @Contract("null, _ -> false; !null, null -> false")
    public static boolean isLibraryPresent(@Nullable Project project, @Nullable String keyClassFqn)
    {
        if (project == null || keyClassFqn == null) return false;
        final PsiClass[] possibleClasses = findClass(project, keyClassFqn);
        return possibleClasses.length > 0;
    }
    
    
    @Contract("null, _ -> false; !null, null -> false")
    public static boolean isLibraryPresent(@Nullable Module module, @Nullable String keyClassFqn)
    {
        if (module == null || keyClassFqn == null) return false;
        final PsiClass[] possibleClasses = findClass(module, keyClassFqn);
        return possibleClasses.length > 0;
    }
    
    
    /* 
       ************************************************************************************
        The indImplementations methods, particularly the core worker method  
        findImplementationsForScope(PsiClass,GlobalSearchScope, JComponent) 
        are based on:
            com.intellij.codeInsight.daemon.impl.MarkerType.navigateToSubclassedClass()  
                                                            navigateToOverriddenMethod()
            com.intellij.refactoring.util.RefactoringHierarchyUtil._findImplementingClasses 
        ************************************************************************************
    */
    
    
    /**
     * This method Should be run inside a {@code DumbService.runReadActionInSmartMode()} call.
     * Finds all the implementations of a class within a module. The base class, as identified
     * by the {@code classFQN}, will be looked for in the entire project. If better (i.e. more fine)
     * scoping is needed for which base class is used, use the overridden version that takes a
     * {@code  GlobalSearchScope baseClassSearchScope} parameter, or the one that takes the
     * {@code PsiClass} rather than a String representing the class. The results are not in any
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
    @NotNull
    public static List<PsiClass> findImplementationsInModule(@NotNull String classFQN,
                                                             @NotNull Module module,
                                                             boolean includeDependencies,
                                                             boolean includeLibraries,
                                                             @Nullable JComponent parentComponent)
    {
        final GlobalSearchScope searchScope = calculateModuleSearchScope(module, includeDependencies, includeLibraries);
        
        return findImplementationsForScope(module.getProject(),
                                           classFQN,
                                           GlobalSearchScope.allScope(module.getProject()),
                                           searchScope,
                                           parentComponent);
    }
    
    
    /**
     * This method Should be run inside a {@code DumbService.runReadActionInSmartMode()} call.
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
    @NotNull
    public static List<PsiClass> findImplementationsInModule(@NotNull PsiClass psiClass,
                                                             @NotNull Module module,
                                                             boolean includeDependencies,
                                                             boolean includeLibraries,
                                                             @Nullable JComponent parentComponent)
    {
        final GlobalSearchScope searchScope = calculateModuleSearchScope(module, includeDependencies, includeLibraries);
        return findImplementationsForScope(psiClass, searchScope, parentComponent);
    }
    
    
    /**
     * This method Should be run inside a {@code DumbService.runReadActionInSmartMode()} call.
     * Finds all the implementations of a class within a global search scope. The results are not
     * in any particular order. It is left up to the caller to sort as needed.
     *
     * @param project                    The project that is being searched; however the entire project is not necessarily searched
     *                                   as the search scope is defined by the {@code implementationsSearchScope} parameter
     * @param classFQN                   the Fully Qualified Name (FQN) of the class/interface to find inheritors of
     * @param baseClassSearchScope       the scope to search for the base class
     * @param implementationsSearchScope the scope to search inheritors in
     * @param parentComponent            the (optional) component which will be used to calculate the progress window ancestor
     *
     * @return the found implementations, in no particular order
     */
    @NotNull
    public static List<PsiClass> findImplementationsForScope(@NotNull Project project,
                                                             @NotNull String classFQN,
                                                             @NotNull GlobalSearchScope baseClassSearchScope,
                                                             @NotNull GlobalSearchScope implementationsSearchScope,
                                                             @Nullable JComponent parentComponent)
    {
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        PsiClass[] possibleClasses = facade.findClasses(classFQN, baseClassSearchScope);
        
        if (possibleClasses.length == 0)
        {
            Log.info("[FRC] Could not find base class/interface '" + classFQN + "' in project '" + project.getName()
                     + "' and therefore cannot look for implementations/subclasses");
        }
        
        final Set<PsiClass> result = new HashSet<>();
        // We should only have one, but we still check all
        for (PsiClass psiClass : possibleClasses)
        {
            result.addAll(findImplementationsForScope(psiClass, implementationsSearchScope, parentComponent));
        }
        return new ArrayList<>(result);
    }
    
    
    /**
     * This method Should be run inside a {@code DumbService.runReadActionInSmartMode()} call.
     * Finds all the implementations of a class within a global search scope. The results are not
     * in any particular order. It is left up to the caller to sort as needed.
     *
     * @param psiClass                   the class/interface to find inheritors of
     * @param implementationsSearchScope the scope to search inheritors in
     * @param parentComponent            the (optional) component which will be used to calculate the progress window ancestor
     *
     * @return the found implementations, in no particular order
     */
    public static List<PsiClass> findImplementationsForScope(@NotNull PsiClass psiClass,
                                                             @NotNull GlobalSearchScope implementationsSearchScope,
                                                             @Nullable JComponent parentComponent)
    {
        // based on:
        //     com.intellij.codeInsight.daemon.impl.MarkerType.navigateToSubclassedClass()  
        //                                                     navigateToOverriddenMethod()
        //     com.intellij.refactoring.util.RefactoringHierarchyUtil._findImplementingClasses
        final List<PsiClass> inheritors = new ArrayList<>();
        
        
        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            final Query<PsiClass> query = ClassInheritorsSearch.search(psiClass, implementationsSearchScope, true);
            query.forEach(new PsiElementProcessorAdapter<>(new PsiElementProcessor<PsiClass>()
            {
                @Override
                public boolean execute(@NotNull PsiClass psiClass)
                {
                    LOG.trace("[FRC] Checking psiClass '" + psiClass.getQualifiedName() + "' of type " + psiClass.getClass());
                    if (!psiClass.isInterface())
                    {
                        inheritors.add(psiClass);
                    }
                    return true;
                }
            }));
            
        }, message("frc.util.findImplementations.progress.title", psiClass.getName()), true, psiClass.getProject(), parentComponent);
        
        return inheritors;
    }
    
    
/*
    public static List<NavigatablePsiElement> findImplementations3(@NotNull PsiClass psiClass,
                                                                   @NotNull GlobalSearchScope implementationsSearchScope,
                                                                   @Nullable JComponent parentComponent)
    {
        final List<NavigatablePsiElement> inheritors = new ArrayList<>();
        
        final PsiElementProcessor.FindElement<PsiClass> collectProcessor = new PsiElementProcessor.FindElement<>();
        final PsiElementProcessor.FindElement<PsiFunctionalExpression> collectExprProcessor = new PsiElementProcessor.FindElement<>();
        
        if (!ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> {
            final Query<PsiClass> query = ClassInheritorsSearch.search(psiClass, implementationsSearchScope, true);
            query.forEach(new PsiElementProcessorAdapter<>(collectProcessor));
            if (collectProcessor.getFoundElement() == null)
            {
                FunctionalExpressionSearch.search(psiClass).forEach(new PsiElementProcessorAdapter<>(collectExprProcessor));
            }
        }, message("frc.util.findImplementations.progress.title", psiClass.getName()), true, psiClass.getProject(), parentComponent))
        {
            return inheritors;
        }
        
        ContainerUtil.addIfNotNull(inheritors, collectProcessor.getFoundElement());
        ContainerUtil.addIfNotNull(inheritors, collectExprProcessor.getFoundElement());
        return inheritors;
    }
*/


//    /**
//     * In general, try to use overloaded version of this method that either takes a module or GlobalSearchScope as
//     * both provided more control in terms of the scope to search.
//     */
//    public static Set<PsiClass> findImplementations(@NotNull String classOrInterfaceFqn, @NotNull Project project)
//    {
//        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
//        return findImplementations(project,
//                                   classOrInterfaceFqn,
//                                   scope,
//                                   scope);
//    }
//    
//    
//    public static Set<PsiClass> findImplementations(@NotNull String classOrInterfaceFqn,
//                                                    @NotNull Module module,
//                                                    boolean includeDependencies,
//                                                    boolean includeLibraries)
//    {
//        final GlobalSearchScope searchScope = calculateModuleSearchScope(module, includeDependencies, includeLibraries);
//        
//        return findImplementations(module.getProject(),
//                                   classOrInterfaceFqn,
//                                   GlobalSearchScope.allScope(module.getProject()),
//                                   searchScope);
//    }
//    
//    
//    public static Set<PsiClass> findImplementations(@NotNull Project project,
//                                                    @NotNull String classOrInterfaceFqn,
//                                                    @NotNull GlobalSearchScope baseClassSearchScope,
//                                                    @NotNull GlobalSearchScope implementationsSearchScope)
//    {
//        
//        
//        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
//        PsiClass[] possibleClasses = facade.findClasses(classOrInterfaceFqn, baseClassSearchScope);
//        
//        if (possibleClasses.length == 0)
//        {
//            Log.info("[FRC] Could not find base class/interface '" + classOrInterfaceFqn + "' in project '" + project.getName()
//                     + "' and therefore cannot look for implementations/subclasses");
//        }
//        
//        final Set<PsiClass> result = new HashSet<>();
//        // We should only have one, but we still check all
//        for (PsiClass psiClass : possibleClasses)
//        {
//            result.addAll(findImplementations(project, psiClass, implementationsSearchScope));
//        }
//        return result;
//    }
//    
//    
//    public static Set<PsiClass> findImplementations(@NotNull Project project,
//                                                    @NotNull PsiClass baseClass,
//                                                    @NotNull GlobalSearchScope implementationsSearchScope)
//    {
//        final Set<PsiClass> result = new HashSet<>();
//        _findImplementations(baseClass, implementationsSearchScope, new HashSet<>(), result);
//        boolean classesRemoved = true;
//        while (classesRemoved)
//        {
//            classesRemoved = false;
//loop1:
//            for (Iterator<PsiClass> iterator = result.iterator(); iterator.hasNext(); )
//            {
//                final PsiClass psiClass = iterator.next();
//                for (final PsiClass aClass : result)
//                {
//                    if (psiClass.isInheritor(aClass, true))
//                    {
//                        iterator.remove();
//                        classesRemoved = true;
//                        break loop1;
//                    }
//                }
//            }
//        }
//        return result;
//    }
//    
//    
//
//    private static void _findImplementations(@NotNull PsiClass theSuper,
//                                             @NotNull GlobalSearchScope scope,
//                                             @NotNull final Set<? super PsiClass> visited,
//                                             @NotNull final Collection<? super PsiClass> result)
//    {
//        
//        
//        visited.add(theSuper);
//        final Query<PsiClass> psiClasses = ClassInheritorsSearch.search(theSuper, scope, false);
//        psiClasses.forEach(new PsiElementProcessorAdapter<>(new PsiElementProcessor<PsiClass>()
//        {
//            
//            @Override
//            public boolean execute(@NotNull PsiClass psiClass)
//            {
//                LOG.trace("[FRC] Checking psiClass '" + psiClass.getQualifiedName() + "' of type " + psiClass.getClass());
//                if (!psiClass.isInterface())
//                {
//                    result.add(psiClass);
//                }
//                return true;
//            }
//        }));
//    }
//    
    
    
    @NotNull
    private static GlobalSearchScope calculateModuleSearchScope(@NotNull Module module,
                                                                boolean includeDependencies,
                                                                boolean includeLibraries)
    {
        final GlobalSearchScope searchScope;
        if (includeDependencies && includeLibraries)
        {
            searchScope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
        }
        else if (includeDependencies)
        {
            searchScope = GlobalSearchScope.moduleWithDependenciesScope(module);
        }
        else if (includeLibraries)
        {
            searchScope = GlobalSearchScope.moduleWithLibrariesScope(module);
        }
        else
        {
            searchScope = GlobalSearchScope.moduleScope(module);
        }
        return searchScope;
    }

//    public static boolean isLibrarySourcePresent(@Nullable Project project, @Nullable String keyClassFqn)
//    {
//        // TODO: Need to determine how to implement this
//    }
}
