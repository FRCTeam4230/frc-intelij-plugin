/*
 * Copyright 2015-2021 the original author or authors.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.javaru.iip.frc.actions.create.simple.cmdBased.v2;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import com.intellij.ide.actions.CreateFileFromTemplateDialog.Builder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;

import icons.FrcIcons;
import net.javaru.iip.frc.templates.FrcFileTemplateGroupDescriptorFactory;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public class SimpleNewFrcCommand2ClassAction extends AbstractSimpleNewFrcCmdBasedVersion2ClassAction
{
    private static final Logger LOG = Logger.getInstance(SimpleNewFrcCommand2ClassAction.class);
    
    private static final Icon ICON = FrcIcons.Components.COMMAND;
    
    
    protected SimpleNewFrcCommand2ClassAction()
    {
        super(ICON);
    }
    
    
    @Override
    protected void buildDialog(Project project, PsiDirectory directory, Builder builder)
    {
        builder.setTitle(message("frc.simple.new.class.command.action.title"))
               .addKind(message("frc.simple.new.class.command.action.title"), ICON, FrcFileTemplateGroupDescriptorFactory.COMMAND2.getFileName());
    }
    
    
    @Override
    protected String getActionName(PsiDirectory directory, @NotNull String newName, String templateName)
    {
        return message("frc.simple.new.class.command.action.details", directory, newName);
    }
    
    
    @NotNull
    @Override
    protected String getErrorTitle()
    {
        return message("frc.simple.new.class.command.action.error");
    }
}
