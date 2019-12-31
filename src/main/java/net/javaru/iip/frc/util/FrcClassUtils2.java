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

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiAssignmentExpression;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiNewExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.PsiTypeParameter;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.psi.search.searches.MethodReferencesSearch;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.siyeh.ig.psiutils.ControlFlowUtils;

import one.util.streamex.StreamEx;



// primary work methods extracted from  com.siyeh.ig.psiutils.ClassUtils in the InspectionGadgets/InspectionGadgetsAnalysis module of IntelliJ IDEA
// but since many were private, we needed to move them here. 
// (There are more in that other class)
public class FrcClassUtils2
{
    @SuppressWarnings("unused")
    @Nullable
    public static PsiField getSingletonField(@Nullable PsiClass aClass)
    {
        return (isSingleton(aClass)) ? getIfOneStaticSelfInstance(aClass) : null; 
    }
    
    public static PsiMethod getSingletonMethod(@Nullable PsiClass aClass)
    {
        if (!isSingleton(aClass)) {
            return null;
        }
        final List<PsiMethod> methods = Arrays.stream(aClass.getMethods())
                                           .filter(m -> !m.isConstructor() &&
                                                        m.hasModifierProperty(PsiModifier.PUBLIC) &&
                                                        !m.hasModifierProperty(PsiModifier.STATIC) &&
                                                        !m.hasParameters() &&
                                                        Objects.equals(aClass, PsiUtil.resolveClassInClassTypeOnly(m.getReturnType()))
                                                        
                                           ).collect(Collectors.toList());
        
        return methods.size() == 1 ? methods.get(0) : null;
    
    }
    
    
    @Contract("null -> false")
    public static boolean isSingleton(@Nullable PsiClass aClass)
    {
        if (aClass == null || aClass.isInterface() || aClass instanceof PsiTypeParameter || aClass instanceof PsiAnonymousClass)
        {
            return false;
        }
        if (aClass.isEnum())
        {
            if (!ControlFlowUtils.hasChildrenOfTypeCount(aClass, 1, PsiEnumConstant.class))
            {
                return false;
            }
            // has at least on accessible instance method
            return Arrays.stream(aClass.getMethods())
                         .anyMatch(m -> !m.isConstructor() && !m.hasModifierProperty(PsiModifier.PRIVATE) && !m.hasModifierProperty(PsiModifier.STATIC));
        }
        if (getIfOnlyInvisibleConstructors(aClass).length == 0)
        {
            return false;
        }
        final PsiField selfInstance = getIfOneStaticSelfInstance(aClass);
        return selfInstance != null && newOnlyAssignsToStaticSelfInstance(getIfOnlyInvisibleConstructors(aClass)[0], selfInstance);
    }
    
    
    @NotNull
    private static PsiMethod[] getIfOnlyInvisibleConstructors(PsiClass aClass)
    {
        final PsiMethod[] constructors = aClass.getConstructors();
        if (constructors.length == 0)
        {
            return PsiMethod.EMPTY_ARRAY;
        }
        for (final PsiMethod constructor : constructors)
        {
            if (constructor.hasModifierProperty(PsiModifier.PUBLIC))
            {
                return PsiMethod.EMPTY_ARRAY;
            }
            if (!constructor.hasModifierProperty(PsiModifier.PRIVATE) &&
                !constructor.hasModifierProperty(PsiModifier.PROTECTED))
            {
                return PsiMethod.EMPTY_ARRAY;
            }
        }
        return constructors;
    }
    
    
    private static boolean newOnlyAssignsToStaticSelfInstance(PsiMethod method, final PsiField field)
    {
        if (field instanceof LightElement) return true;
        final Query<PsiReference> search = MethodReferencesSearch.search(method, field.getUseScope(), false);
        final NewOnlyAssignedToFieldProcessor processor = new NewOnlyAssignedToFieldProcessor(field);
        search.forEach(processor);
        return processor.isNewOnlyAssignedToField();
    }
    
    
    @Nullable
    private static PsiField getIfOneStaticSelfInstance(PsiClass aClass)
    {
        Stream<PsiField> fieldStream = Arrays.stream(aClass.getFields());
        
        StreamEx<PsiField> enclosingClassFields =
                StreamEx.iterate(aClass.getContainingClass(), Objects::nonNull, PsiClass::getContainingClass).filter(Objects::nonNull)
                        .flatMap(c -> Stream.of(c.getFields()));
        fieldStream = Stream.concat(fieldStream, enclosingClassFields);
        
        fieldStream = Stream.concat(fieldStream,
                                    Arrays.stream(aClass.getInnerClasses())
                                          .filter(innerClass -> innerClass.hasModifierProperty(PsiModifier.STATIC))
                                          .flatMap(innerClass -> Arrays.stream(innerClass.getFields())));
        
        final List<PsiField> fields = fieldStream.filter(field -> resolveToSingletonField(aClass, field)).limit(2).collect(Collectors.toList());
        return fields.size() == 1 ? fields.get(0) : null;
    }
    
    
    private static boolean resolveToSingletonField(PsiClass aClass, PsiField field)
    {
        if (!field.hasModifierProperty(PsiModifier.STATIC))
        {
            return false;
        }
        final PsiClass targetClass = PsiUtil.resolveClassInClassTypeOnly(field.getType());
        PsiElement toCmp1 = aClass.isPhysical() ? aClass : aClass.getNavigationElement();
        PsiElement toCmp2 = targetClass == null || targetClass.isPhysical() ? targetClass : targetClass.getNavigationElement();
        return Objects.equals(toCmp1, toCmp2);
    }
    
    
    private static class NewOnlyAssignedToFieldProcessor implements Processor<PsiReference>
    {
        
        private boolean newOnlyAssignedToField = true;
        private final PsiField field;
    
    
        NewOnlyAssignedToFieldProcessor(PsiField field)
        {
            this.field = field;
        }
    
    
        @Override
        public boolean process(PsiReference reference)
        {
            final PsiElement element = reference.getElement();
            final PsiElement parent = element.getParent();
            if (!(parent instanceof PsiNewExpression))
            {
                newOnlyAssignedToField = false;
                return false;
            }
            final PsiElement grandParent = parent.getParent();
            if (field.equals(grandParent))
            {
                return true;
            }
            if (!(grandParent instanceof PsiAssignmentExpression))
            {
                newOnlyAssignedToField = false;
                return false;
            }
            final PsiAssignmentExpression assignmentExpression = (PsiAssignmentExpression) grandParent;
            final PsiExpression lhs = assignmentExpression.getLExpression();
            if (!(lhs instanceof PsiReferenceExpression))
            {
                newOnlyAssignedToField = false;
                return false;
            }
            final PsiReferenceExpression referenceExpression = (PsiReferenceExpression) lhs;
            final PsiElement target = referenceExpression.resolve();
            if (!field.equals(target))
            {
                newOnlyAssignedToField = false;
                return false;
            }
            return true;
        }
    
    
        public boolean isNewOnlyAssignedToField()
        {
            return newOnlyAssignedToField;
        }
    }
}
