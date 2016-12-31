/*
 * Copyright 2015-2016 Mark Vedder
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

package net.javaru.iip.frc.actions.create.build;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;

import icons.AntIcons;
import net.javaru.iip.frc.actions.create.TaskValidatorBase;
import net.javaru.iip.frc.actions.create.ui.FrcWizardDialog;
import net.javaru.iip.frc.actions.create.ui.forms.DevWorkPanel;

import static net.javaru.iip.frc.util.FrcBundle.message;



public class NewFrcAntBuildFileAction extends CreateElementActionBase implements DumbAware
{
    private static final Icon ICON = AntIcons.AntInstallation;
    public static final String NAME = message("frc.new.build.ant.action.name");
    public static final String DEFAULT_WINDOW_TITLE = message("frc.new.component.window.title");


    public NewFrcAntBuildFileAction()
    {
        super(NAME,
              message("frc.new.build.ant.action.description"),
              ICON);
    }


    @Override
    public boolean isDumbAware() { return NewFrcAntBuildFileAction.class.equals(getClass()); }


    @NotNull
    @Override
    protected PsiElement[] invokeDialog(Project project, PsiDirectory directory)
    {
        final MyInputValidator validator = new AntFilesInputValidator(project, directory);
        if (ApplicationManager.getApplication().isUnitTestMode())
        {
            try
            {
                return validator.create("test");
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
//            Messages.showInputDialog(project,
//                                     "Enter Ant build File name",
//                                     "New Ant Build File",
//                                     ICON,
//                                     "build.xml",
//                                     validator);

            final DialogWrapper dialog = createDialog(project);
            dialog.show();
            
        }
        
        return validator.getCreatedElements();
    }

    
    protected DialogWrapper createDialog(@NotNull final Project project)
    {
        return createDialog(project, null);
    }
    
    protected DialogWrapper createDialog(@NotNull final Project project, @Nullable String windowTitle)
    {
        if (windowTitle == null) {windowTitle = DEFAULT_WINDOW_TITLE;}
        
        final FrcWizardDialog dialog = createWizardDialog(project);
        dialog.setTitle(windowTitle);
        return dialog;
    }
    
    //TODO - define as abstract method in abstract super class
    protected FrcWizardDialog createWizardDialog(@NotNull final Project project)
    {
        final DevWorkPanel panel = new DevWorkPanel();
        
        return new FrcWizardDialog(project,
                                   panel.getMyRootPanel(),
                                   "Create Ant Build Files",
                                   "Creates FRC specific Ant 'build.xml' and 'build.properties' files.", 
                                   new TaskValidatorBase());
    }
    

    @NotNull
    @Override
    protected PsiElement[] create(String newName, PsiDirectory directory) throws Exception
    {
        //TODO: Write this 'create' implemented method in the 'NewFrcAntBuildFileAction' class
        throw new IllegalStateException("The 'create' method in the 'NewFrcAntBuildFileAction' class is not yet implemented.");
        //return new com.intellij.psi.PsiElement[0];
    }


    @Override
    protected String getErrorTitle()
    {
        return message("frc.new.build.ant.action.error");
    }


    @Override
    protected String getCommandName()
    {
        //TODO: Write this 'getCommandName' implemented method in the 'NewFrcAntBuildFileAction' class
        return NAME;
    }


    @Override
    protected String getActionName(PsiDirectory directory, String newName)
    {
        //TODO: Write this 'getActionName' implemented method in the 'NewFrcAntBuildFileAction' class
        return NAME;
    }
    
    
    protected class AntFilesInputValidator extends MyInputValidator
    {

        public AntFilesInputValidator(Project project, PsiDirectory directory)
        {
            super(project, directory);
        }
    }
}
