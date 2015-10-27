/*
 * Copyright 2015 Mark Vedder
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

package net.javaru.iip.frc.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;

import net.javaru.iip.frc.settings.FrcApplicationComponent;
import net.javaru.iip.frc.settings.FrcSettings;



public class RioLogConsoleProjectService 
{
    
    private final Project myProject;

    public RioLogConsoleProjectService(Project myProject)
    {
        this.myProject = myProject;
    }
    
    public void openRioLogConsole()
    {
        if (myProject != null)
        {
            final FrcSettings settings = FrcApplicationComponent.getInstance().getState();
            boolean useRunWindow = (settings != null && !settings.isUseFrcToolWindow());
            final AbstractRioLogContentExecutor contentExecutor = useRunWindow ?
                                                                  new RioLogRunWindowContentExecutor(myProject, true) :
                                                                  new RioLogFrcWindowContentExecutor(myProject, true);
            Disposer.register(myProject, contentExecutor);
            contentExecutor.run();
        }
    }
}
