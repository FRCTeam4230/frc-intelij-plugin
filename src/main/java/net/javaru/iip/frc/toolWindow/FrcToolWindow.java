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

package net.javaru.iip.frc.toolWindow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.ContentManager;



public class FrcToolWindow
{
    private static final Logger LOG = Logger.getInstance(FrcToolWindow.class);

    @NotNull
    private final Project myProject;
    @Nullable
    private ToolWindow myToolWindow;
    @Nullable
    private ContentManager myContentManager;

    public FrcToolWindow(@NotNull Project project) {this.myProject = project;}


    public void initToolWindow(@NotNull ToolWindow toolWindow)
    {
        this.myToolWindow = toolWindow;
        this.myContentManager = toolWindow.getContentManager();
    }
}
