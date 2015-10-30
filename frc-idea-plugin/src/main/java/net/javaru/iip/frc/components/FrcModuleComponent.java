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

package net.javaru.iip.frc.components;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleComponent;

import net.javaru.iip.frc.ui.RioLogConsoleProjectService;



public class FrcModuleComponent implements ModuleComponent
{

    private static final Logger LOG = Logger.getInstance(FrcModuleComponent.class);
    @NotNull
    private final Module myModule;


    public FrcModuleComponent(@NotNull Module myModule) {this.myModule = myModule;}


    @Override
    public void projectOpened()
    {
        LOG.debug(getClass().getSimpleName() + ".projectOpened() called for" + myModule.getName());
    }


    @Override
    public void projectClosed()
    {
        LOG.debug(getClass().getSimpleName() + ".projectClosed() called for" + myModule.getName());
    }


    @Override
    public void moduleAdded()
    {
        LOG.debug(getClass().getSimpleName() + ".moduleAdded() called for" + myModule.getName());
        RioLogConsoleProjectService.update(myModule);
    }


    @Override
    public void initComponent()
    {
        LOG.debug(getClass().getSimpleName() + ".initComponent() called for" + myModule.getName());
    }


    @Override
    public void disposeComponent()
    {
        LOG.debug(getClass().getSimpleName() + ".disposeComponent()) called for" + myModule.getName());
        RioLogConsoleProjectService.update(myModule);
    }


    @NotNull
    @Override
    public String getComponentName()
    {
       return getClass().getSimpleName();
    }
}
