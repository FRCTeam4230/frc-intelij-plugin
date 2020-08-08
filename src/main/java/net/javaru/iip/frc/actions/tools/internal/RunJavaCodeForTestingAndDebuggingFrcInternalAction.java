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

import org.jetbrains.annotations.NotNull;
import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;


// The Kotlin version is in FrcInternalActions.kt:  RunKotlinCodeForTestingAndDebuggingFrcInternalAction

public class RunJavaCodeForTestingAndDebuggingFrcInternalAction extends AbstractFrcInternalAction
{
    private static final Logger LOG = Logger.getInstance(RunJavaCodeForTestingAndDebuggingFrcInternalAction.class);
    
    
    @SuppressWarnings("unused")
    @Override
    public void actionPerformed(@NotNull AnActionEvent actionEvent)
    {
        final IdeView ideView = actionEvent.getData(LangDataKeys.IDE_VIEW);
        final Module module = actionEvent.getData(LangDataKeys.MODULE);
        final Project project = actionEvent.getProject();
        
        LOG.trace("[FRC] BREAKPOINT");
    
        // Put code here, but do N0T commit it
        
        
        
        
        
        LOG.trace("[FRC] BREAKPOINT");
    }
}
