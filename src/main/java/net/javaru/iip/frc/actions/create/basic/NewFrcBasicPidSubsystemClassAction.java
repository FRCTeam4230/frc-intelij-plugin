/*
 * Copyright 2015-2018 the original author or authors
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

package net.javaru.iip.frc.actions.create.basic;

import javax.swing.*;

import com.intellij.ide.actions.CreateFileFromTemplateDialog.Builder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;

import net.javaru.iip.frc.FrcIcons;
import net.javaru.iip.frc.templates.FrcFileTemplateGroupDescriptorFactory;

import static net.javaru.iip.frc.i18n.FrcMessageBundle.message;



public class NewFrcBasicPidSubsystemClassAction extends AbstractNewFrcBasicClassAction
{
    private static final Icon ICON = FrcIcons.Components.PID_SUBSYSTEM;


    protected NewFrcBasicPidSubsystemClassAction()
    {
        super(message("frc.new.class.pidSubsystem.action.name"),
              message("frc.new.class.pidSubsystem.action.description"),
              ICON);
    }


    @Override
    protected void buildDialog(Project project, PsiDirectory directory, Builder builder)
    {
        builder.setTitle(message("frc.new.class.pidSubsystem.action.name"))
               .addKind(message("frc.new.class.pidSubsystem.action.name"), ICON, FrcFileTemplateGroupDescriptorFactory.PID_SUBSYSTEM.getFileName());
    }


    @Override
    protected String getActionName(PsiDirectory directory, String newName, String templateName)
    {
        return message("frc.new.class.pidSubsystem.action.details", directory, newName);
    }


    @Override
    protected String getErrorTitle()
    {
        return message("frc.new.class.pidSubsystem.action.error");
    }
    
}