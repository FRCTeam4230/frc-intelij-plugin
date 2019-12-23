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

package net.javaru.iip.frc.actions.create.simple.cmdBased.v1;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.actions.create.simple.AbstractSimpleNewFrcClassAction;
import net.javaru.iip.frc.wpilib.WpiibLibraryUtilsKt;



public abstract class AbstractSimpleNewFrcCmdBasedVersion1ClassAction extends AbstractSimpleNewFrcClassAction
{
    private static final Logger LOG = Logger.getInstance(AbstractSimpleNewFrcCmdBasedVersion1ClassAction.class);
    
    
    @SuppressWarnings("unused")
    protected AbstractSimpleNewFrcCmdBasedVersion1ClassAction(String text)
    {
        this(text, DEFAULT_ICON);
    }
    
    
    protected AbstractSimpleNewFrcCmdBasedVersion1ClassAction(String text, Icon icon)
    {
        this(text, text, icon, true);
    }
    
    
    protected AbstractSimpleNewFrcCmdBasedVersion1ClassAction(String text, String description, Icon icon)
    {
        this(text, description, icon, true);
    }
    
    
    protected AbstractSimpleNewFrcCmdBasedVersion1ClassAction(String text, String description, Icon icon, boolean inSourceOnly)
    {
        super(text, description, icon, inSourceOnly);
    }
    
    
    @Override
    protected boolean shouldBeEnabledAdditionalCriteria(@NotNull Project project)
    {
        return WpiibLibraryUtilsKt.isVersion1CommandBaseLibAttached(project);
    }
}
