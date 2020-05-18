/*
 * Copyright 2015-2020 the original author or authors
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

package net.javaru.iip.frc.actions.tools.internal;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.facet.FrcFacetKt;



public abstract class AbstractFrcInternalAction extends AnAction
{
    private static final Logger LOG = Logger.getInstance(AbstractFrcInternalAction.class);
    
    
    protected AbstractFrcInternalAction()
    {
    }
    
    
    protected AbstractFrcInternalAction(@Nullable String text)
    {
        super(text);
    }
    
    
    protected AbstractFrcInternalAction(@Nullable String text,
                                     @Nullable String description,
                                     @Nullable Icon icon)
    {
        super(text, description, icon);
    }
    
    @Override
    public void update(AnActionEvent e)
    {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        e.getPresentation().setVisible(project != null &&
                                       !project.isDisposed() &&
                                       showForProject(project) &&
                                       additionalIsVisibleChecks(project, e));
    }
    
    protected boolean additionalIsVisibleChecks(@NotNull Project project, @NotNull AnActionEvent e)
    {
        return true;
    }
    
    protected boolean showOnlyForFrcProjects()
    {
        return false;
    }
    
    private boolean showForProject(@NotNull Project project)
    {
        if (showOnlyForFrcProjects())
        {
            return FrcFacetKt.isFrcFacetedProject(project);
        }
        else
        {
            return true;
        }
    }
}
