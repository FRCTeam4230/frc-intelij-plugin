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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiNameHelper;

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
    
    
    /**
     * <strong style="font-color: red;">Implmenting classes must call <tt>init()</tt> at the end of their constructors.</strong>
     * @param module the module
     * @param classCreator the ClassCreator to use
     * @param directory The PsiDirectory the class the action was called on.
     */
    protected NewFrcClassDialog(@NotNull Module module,
                                @NotNull ClassCreator classCreator,
                                @NotNull PsiDirectory directory)
    {
        super(module.getProject());
        this.myModule = module;
        this.myProject = module.getProject();
        this.myClassCreator = classCreator;
        this.myDirectory = directory;
    }


    @NotNull
    @Override
    protected List<ValidationInfo> doValidateAll()
    {
        final Builder<ValidationInfo> results = ImmutableList.builder();

        boolean nameIsValid = isProposedClassNameValid();
        @Nullable
        String createClassErrorMessage = ClassCreator.checkCanCreateClass(myDirectory, getNewClassNameField().getText(), getClassTypeSimpleName());
        
        if (!nameIsValid)
        {
            results.add(new ValidationInfo(message("frc.new.class.adv.validation.invalidName", getClassTypeSimpleName()), getNewClassNameField()));
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
    
    /**
     * Returns the simple name of the class type being created for use in Dialogs and error messages. For example: 'Command", 'Subsystem', etc.
     * @return the simple name of the class type being created for use in Dialogs and error messages. For example: 'Command", 'Subsystem', etc.
     */
    @NotNull
    protected abstract String getClassTypeSimpleName();


    protected abstract JTextComponent getNewClassNameField();
    
    //protected abstract Icon
    
    
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
    
        //TODO - need to get the actual additional properties we need.
        Map<String, String> additionalProperties = new HashMap<>();
        additionalProperties.put("tempSubSystemName", "ExampleSubsystem");
        
        if (myClassCreator.createClass(getNewClassName().trim(),
                                       myDirectory,
                                       additionalProperties))
        {
            close(OK_EXIT_CODE);
        }
        // TODO Finish Me
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
        return message("frc.new.class.adv.general.dialog.title", getClassTypeSimpleName());
    }
    
    
}
