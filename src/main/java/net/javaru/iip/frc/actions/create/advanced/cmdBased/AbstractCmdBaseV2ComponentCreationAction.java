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

package net.javaru.iip.frc.actions.create.advanced.cmdBased;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;

import net.javaru.iip.frc.actions.create.advanced.AbstractFrcComponentCreationAction;
import net.javaru.iip.frc.actions.create.advanced.FrcComponentCreationDataProvider;
import net.javaru.iip.frc.wpilib.WpiibLibraryUtilsKt;



public abstract class AbstractCmdBaseV2ComponentCreationAction extends AbstractFrcComponentCreationAction
{
    private static final Logger LOG = Logger.getInstance(AbstractCmdBaseV2ComponentCreationAction.class);
    
    
    protected AbstractCmdBaseV2ComponentCreationAction(String text, String description, FrcComponentCreationDataProvider dataProvider)
    {
        super(text, description, dataProvider);
    }
    
    
    @Override
    protected boolean shouldBeEnabledAdditionalCriteria(@NotNull Module module)
    {
        return WpiibLibraryUtilsKt.isVersion2CommandBaseLibAttached(module);
    }
}
