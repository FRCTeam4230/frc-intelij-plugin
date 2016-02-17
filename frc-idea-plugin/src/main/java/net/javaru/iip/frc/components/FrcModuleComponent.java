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

    /*
        LIFECYCLE METHODS CALL ORDER 
            When Project is opened
                A. [ProjectComponent.initComponent()]

                1. ModuleComponent.initComponent()
                    • Called for each module in the project. All are called sequentially before next method is called
                2. ModuleComponent.moduleAdded()
                    • Called for each module in the project. All are called sequentially before next method is called
                3. ModuleComponent.projectOpened()
                    • Called for each module in the project. All are called sequentially\
                B. [ProjectComponent.projectOpened()]
                    
                == User Doing Work in Project ==
                
                4. ModuleComponent.projectClosed()
                    • Called for each module in the project. All are called sequentially
                5. ModuleComponent.disposeComponent()
                    • Called for each module in the project. All are called sequentially
                
            When a Module is Added
                1. initComponent()
                    • Called as soon as the module is added in Project Structure, even before Apply or OK is clicked
                2. moduleAdded()
                    • Called when Apply/OK is clicked in Project Structure
        
     */

    @Override
    public void initComponent()
    {
        LOG.debug(getClass().getSimpleName() + ".initComponent() called for" + myModule.getName());
    }


    @Override
    public void moduleAdded()
    {
        LOG.debug(getClass().getSimpleName() + ".moduleAdded() called for" + myModule.getName());
        RioLogConsoleProjectService.update(myModule);
    }


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
    public void disposeComponent()
    {
        LOG.debug(getClass().getSimpleName() + ".disposeComponent()) called for" + myModule.getName());
        if (!myModule.isDisposed())
        {
            RioLogConsoleProjectService.update(myModule);
        }
    }


    @NotNull
    @Override
    public String getComponentName()
    {
       return getClass().getSimpleName();
    }
}
