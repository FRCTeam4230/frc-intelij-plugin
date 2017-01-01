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
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;

import icons.AntIcons;
import net.javaru.iip.frc.actions.create.ui.FrcWizardDialog;
import net.javaru.iip.frc.actions.create.ui.forms.CreateAntBuildPanelBean;
import net.javaru.iip.frc.actions.create.ui.forms.ModelPanelBean;

import static net.javaru.iip.frc.util.FrcBundle.message;



public class NewFrcAntBuildFileAction extends AbstractCreateTaskAction implements DumbAware
{
    private static final Icon ICON = AntIcons.AntInstallation;
    public static final String NAME = message("frc.new.build.ant.action.name");


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

        // A basic example
        // Messages.showInputDialog(project, "Enter Ant build File name", "New Ant Build File", ICON, "build.xml", validator);

        final DialogWrapper dialog = createDialog(project, directory);
        dialog.show();
        
        

        // TODO: NEEDS TO BE IMPLEMENTED
        // return validator.getCreatedElements();
        return new PsiElement[0];
    }

    
    protected DialogWrapper createDialog(@NotNull final Project project, PsiDirectory directory)
    {
        return createDialog(project, directory, null);
    }
    
    protected DialogWrapper createDialog(@NotNull final Project project, PsiDirectory directory, @Nullable String windowTitle)
    {
        if (windowTitle == null) {windowTitle = DEFAULT_WINDOW_TITLE;}
        
        final FrcWizardDialog dialog = createWizardDialog(project, directory);
        dialog.setTitle(windowTitle);
        return dialog;
    }


    @NotNull
    @Override
    protected ModelPanelBean createPanelBean(@NotNull final Project project, final PsiDirectory directory)
    {
        return new CreateAntBuildPanelBean();
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
    
    
    
}
