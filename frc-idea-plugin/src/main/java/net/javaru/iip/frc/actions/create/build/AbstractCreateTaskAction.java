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

package net.javaru.iip.frc.actions.create.build;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import com.intellij.ide.actions.CreateElementActionBase;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;

import net.javaru.iip.frc.actions.create.ui.FrcWizardDialog;
import net.javaru.iip.frc.actions.create.ui.forms.ModelPanelBean;

import static net.javaru.iip.frc.util.FrcBundle.message;



public abstract class AbstractCreateTaskAction extends CreateElementActionBase
{
    private static final Logger LOG = Logger.getInstance(AbstractCreateTaskAction.class);
    
    public static final String DEFAULT_WINDOW_TITLE = message("frc.new.component.window.title");


    public AbstractCreateTaskAction(String text, String description, Icon icon) {super(text, description, icon);}


    protected FrcWizardDialog createWizardDialog(@NotNull final Project project, PsiDirectory directory)
    {
        ModelPanelBean panelBean = createPanelBean(project, directory);
        

        return new FrcWizardDialog(project,
                                   panelBean,
                                   "Create Ant Build Files",
                                   "Creates FRC specific Ant 'build.xml' and 'build.properties' files.");
    }


    


    @NotNull
    protected abstract ModelPanelBean createPanelBean(@NotNull final Project project, final PsiDirectory directory);
}
