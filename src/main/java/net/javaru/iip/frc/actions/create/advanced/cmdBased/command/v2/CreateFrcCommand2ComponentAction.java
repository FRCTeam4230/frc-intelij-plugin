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

package net.javaru.iip.frc.actions.create.advanced.cmdBased.command.v2;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiDirectory;

import net.javaru.iip.frc.FrcIcons;
import net.javaru.iip.frc.actions.create.advanced.ClassCreator;
import net.javaru.iip.frc.actions.create.advanced.CreateFrcComponentDataProvider;
import net.javaru.iip.frc.actions.create.advanced.cmdBased.command.CreateCommandComponentDialog;
import net.javaru.iip.frc.actions.create.advanced.cmdBased.CreateFrcComponentDialog;
import net.javaru.iip.frc.actions.create.advanced.cmdBased.AbstractAdvancedNewFrcCmdBaseV2Action;

import static net.javaru.iip.frc.i18n.FrcBundle.message;



public class CreateFrcCommand2ComponentAction extends AbstractAdvancedNewFrcCmdBaseV2Action
{
    private static final Logger LOG = Logger.getInstance(CreateFrcCommand2ComponentAction.class);
    
    private static final Icon ICON = FrcIcons.Components.COMMAND;
    
    @NotNull
    private CreateFrcCommandV2DataProvider dataProvider = CreateFrcCommandV2DataProvider.INSTANCE;
    
    public CreateFrcCommand2ComponentAction()
    {
        super(message("frc.new.class.command.action.name"),
              message("frc.new.class.command.action.description"),
              ICON);
    }
    
    
    @Override
    protected CreateFrcComponentDialog constructCreateFrcComponentDialogInstance(@NotNull Module module,
                                                                                 @NotNull ClassCreator classCreator,
                                                                                 @NotNull PsiDirectory directory)
    {
        return new CreateCommandComponentDialog(module, classCreator, directory, dataProvider);
    }
    
    
    @Override
    protected ClassCreator constructClassCreatorInstance(@NotNull Module module)
    {
        return new ClassCreator(module, dataProvider);
    }
    
    
    @Override
    protected CreateFrcComponentDataProvider getDataProvider()
    {
        return dataProvider;
    }
}
