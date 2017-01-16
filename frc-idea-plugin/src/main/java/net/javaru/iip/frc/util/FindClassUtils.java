/*
 * Copyright 2015-2017 Mark Vedder
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.javaru.iip.frc.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;



public class FindClassUtils
{
    private static final Logger LOG = Logger.getInstance(FindClassUtils.class);


    @NotNull
    @Contract("null, _ -> !null; !null, null -> !null")
    public static PsiClass[] findClass(Project project, String fqn)
    {
        if (project == null || fqn == null)  {return new PsiClass[0]; }
        
        GlobalSearchScope allScope = GlobalSearchScope.allScope(project);
        JavaPsiFacade facade = JavaPsiFacade.getInstance(project);
        PsiClass[] possibleClasses = facade.findClasses(fqn, allScope);
        return possibleClasses;
    }
    


    @Contract("null, _ -> false; !null, null -> false")
    public static boolean isLibraryPresent(@Nullable Project project, @Nullable String keyClassFqn)
    {
        if (project == null || keyClassFqn == null) return false;
        final PsiClass[] possibleClasses = findClass(project, keyClassFqn);
        return possibleClasses.length > 0;
    }


//    public static boolean isLibrarySourcePresent(@Nullable Project project, @Nullable String keyClassFqn)
//    {
//        // TODO: Need to determine how to implement this
//    }
}
