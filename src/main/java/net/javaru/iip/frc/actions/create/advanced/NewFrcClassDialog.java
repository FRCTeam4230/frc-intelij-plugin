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

package net.javaru.iip.frc.actions.create.advanced;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.text.JTextComponent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiNameHelper;

import net.javaru.iip.frc.util.FrcClassUtilsKt;
import net.javaru.iip.frc.util.PsiClassNameComparator;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public abstract class NewFrcClassDialog extends DialogWrapper
{
    private static final Logger LOG = Logger.getInstance(NewFrcClassDialog.class);
    
    @NotNull
    protected final Project myProject;
    @NotNull
    protected final Module myModule;
    @NotNull
    protected final ClassCreator myClassCreator;
    @NotNull
    protected final PsiDirectory myDirectory;
    
    @NotNull
    protected final NewFrcClassDataProvider myDataProvider;
    
    
    /**
     * <strong style="font-color: red;">Implmenting classes must call <tt>init()</tt> at the end of their constructors.</strong>
     * @param module the module
     * @param classCreator the ClassCreator to use
     * @param directory The PsiDirectory the class the action was called on.
     */
    protected NewFrcClassDialog(@NotNull Module module,
                                @NotNull ClassCreator classCreator,
                                @NotNull PsiDirectory directory,
                                @NotNull NewFrcClassDataProvider dataProvider)
    {
        super(module.getProject());
        this.myModule = module;
        this.myProject = module.getProject();
        this.myClassCreator = classCreator;
        this.myDirectory = directory;
        this.myDataProvider = dataProvider;
    }


    @NotNull
    @Override
    protected List<ValidationInfo> doValidateAll()
    {
        final Builder<ValidationInfo> results = ImmutableList.builder();

        boolean nameIsValid = isProposedClassNameValid();
        @Nullable
        String createClassErrorMessage = ClassCreator.checkCanCreateClass(myDirectory, 
                                                                          getNewClassNameField().getText(), 
                                                                          myDataProvider.getClassTypeSimpleName());
        
        if (!nameIsValid)
        {
            results.add(new ValidationInfo(message("frc.new.class.adv.validation.invalidName", myDataProvider.getClassTypeSimpleName()), 
                                           getNewClassNameField()));
        }
       
        if (createClassErrorMessage != null)
        {
            results.add(new ValidationInfo(createClassErrorMessage, getNewClassNameField()));
        }
    
        results.addAll(doAdditionalValidation());
        // Everything is valid
        return results.build();
    }


    /**
     * A method subclasses can override to do additional validation specific t the class type being created.
     * @return List<ValidationInfo> of invalid fields, or an empty list if no errors found.
     */
    protected List<ValidationInfo> doAdditionalValidation()
    {
        return Collections.emptyList();
    }
    
    
    @NotNull
    protected abstract JTextComponent getNewClassNameField();
    
    /**
     * Implementations should return a map of additional properties to pass into the Velocity Template. If none,
     * return an empty Map.
     * 
     * @return a map of additional properties to pass into the Velocity Template
     */
    protected abstract Map<String, String> getAdditionalProperties();
    
    
    /**
     * Returns the desired name for the new class. Implmenting classes can override if
     * they want to dynamically create the class name, such as by auto appending a suffix like 
     * 'Command' or 'Subsystem' to the name the user inputs. Overriding implementations should be 
     * sure a trimmed value is returned. By default, this method will returned the trimmed value 
     * of the Class Name Field as returned by the {@link #getNewClassNameField()} method.
     * 
     * @return the desired name for the new class
     */
    protected String getNewClassName()
    {
        return getNewClassNameField().getText().trim();
    }

    protected boolean isProposedClassNameValid()
    {
        String text = getNewClassNameField().getText();
        return text.length() > 0 && PsiNameHelper.getInstance(myProject).isQualifiedName(text);
    }

    @Override
    protected void doOKAction()
    {
        LOG.trace("[FRC] doOKAction called");
        
        if (!getOKAction().isEnabled())
        {
            return;
        }
    
        Map<String, String> additionalProperties = getAdditionalProperties();
        
        if (myClassCreator.createClass(getNewClassName().trim(),
                                       myDirectory,
                                       additionalProperties))
        {
            close(OK_EXIT_CODE);
        }
    }
    
    
    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent()
    {
        return getNewClassNameField();
    }
    
    
    @Override
    public String getTitle()
    {
        return message("frc.new.class.adv.general.dialog.title", myDataProvider.getClassTypeSimpleName());
    }
    
    
    protected List<PsiClass> getExtendableClasses()
    {
        final ImmutableList.Builder<PsiClass> classes = ImmutableList.builder();
        
        final String typicalBaseClassFqName = myDataProvider.getTypicalBaseClassFqName();
        final String topLevelClassFqName = myDataProvider.getTopLevelClassFqName();
        
        
        final PsiClass[] foundBaseClasses = FrcClassUtilsKt.findClass(myProject, typicalBaseClassFqName);
        classes.add(foundBaseClasses);
        
        final PsiClass[] topLevelClasses;
        
        if (!topLevelClassFqName.equals(typicalBaseClassFqName))
        {
            topLevelClasses = FrcClassUtilsKt.findClass(myProject, topLevelClassFqName);
            classes.add(topLevelClasses);
        }
        else
        {
            topLevelClasses = foundBaseClasses;
        }
        
        
        // We should only find the one interface, but the easiest way to handle is to iterate over them.
        for (PsiClass foundInterface : topLevelClasses)
        {
            List<PsiClass> projectImpls = findProjectBaseImpls(foundInterface);
            classes.addAll(projectImpls);
        }
        
        return classes.build();
    }
    
    
    /**
     * Returns an immutable List of all 'base' implementations of the provided top level class, This
     * includes all interfaces and abstract classes that implement or extend the provided top level
     * class, as well as any classes that have a "Base Class" name, as returned by 
     * {@link #isBaseClass(PsiClass)}.
     * 
     * @param topLevelClassFqName the top level interface or (abstract) class name to find an implementation for.
     */
    @NotNull
    protected List<PsiClass> findProjectBaseImpls(String topLevelClassFqName)
    {
        final List<PsiClass> implementations = FrcClassUtilsKt.findImplementationsInModule(topLevelClassFqName,
                                                                                           myModule,
                                                                                           false,
                                                                                           false,
                                                                                           null);
        return doFindProjectBaseImplsProcessing(implementations);
    }
    
    /**
     * Returns an immutable List of all 'base' implementations of the provided top level class, This
     * includes all interfaces and abstract classes that implement or extend the provided top level
     * class, as well as any classes that have a "Base Class" name, as returned by 
     * {@link #isBaseClass(PsiClass)}.
     * 
     * @param topLevelClass the top level interface or (abstract) class name to find an implementation for.
     */
    @NotNull
    protected List<PsiClass> findProjectBaseImpls(PsiClass topLevelClass)
    {
        final List<PsiClass> implementations = FrcClassUtilsKt.findImplementationsInModule(topLevelClass,
                                                                                           myModule,
                                                                                           false,
                                                                                           false,
                                                                                           null);
        return doFindProjectBaseImplsProcessing(implementations);                       
    }
    
    
    @NotNull
    protected List<PsiClass> doFindProjectBaseImplsProcessing(List<PsiClass> foundImplementations)
    {
        return foundImplementations.stream()
                                   .filter(this::isBaseClass)
                                   .sorted(PsiClassNameComparator.INSTANCE)
                                   .collect(Collectors.toList());
    }
    
    /**
     * Determines if a PsiClass is a 'Base Class', returning true if the PsiClass is an interface, is abstract, or
     * has a 'Base Name' ending such as 'CommandBase' or 'BaseCommand' in the case of a Command. Just having 'Base'
     * in the name however does not make it a 'Base Class'. 
     */
    protected boolean isBaseClass(PsiClass psiClass)
    {
        @Nullable
        final String name = psiClass.getName();
        
        boolean isBaseClass = false;
        if (name != null)
        {
            final Set<String> baseNames = myDataProvider.getClassTypeBaseNames();
            isBaseClass = baseNames.stream().anyMatch(name::endsWith);
        }
        
        return (isBaseClass || FrcClassUtilsKt.isInterfaceOrAbstract(psiClass)) && FrcClassUtilsKt.isOpen(psiClass);
    }
}
