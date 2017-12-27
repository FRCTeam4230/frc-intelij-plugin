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

package net.javaru.iip.frc.components;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;

import net.javaru.iip.frc.facet.FrcFacet;



public class FrcModuleComponentImpl implements FrcModuleComponent
{
    private static final Logger LOG = Logger.getInstance(FrcModuleComponentImpl.class);

    @NotNull
    private final Module myModule;

    
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
    

    /** Do not call constructor directly. Use the static {@link #getInstance(Module)} method. */
    public FrcModuleComponentImpl(@NotNull Module myModule) {this.myModule = myModule;}
    

    @Nullable
    public static FrcApplicationComponent getInstance(@NotNull Module module)
    {
        return module.getComponent(FrcApplicationComponent.class);
    }


    @Override
    public boolean isThisModuleFrcFaceted()
    {
        return FrcFacet.isFrcFacetedModule(myModule);       
    }


    @SuppressWarnings("unused")
    @Contract("null -> false")
    public static boolean isFrcFacetedModule(@Nullable Module module) {return FrcFacet.isFrcFacetedModule(module);}


    // projectOpened() and projectClosed() are deprecated and thus not implemented
    

    @Override
    public void moduleAdded()
    {
        LOG.debug("[FRC] " + getClass().getSimpleName() + ".moduleAdded() called for module " + myModule);
        // We only want to update the RioLogConsole if the project is fully opened. In other words, this is a
        // case where the user is adding a module to an open project rather than this moduleAdded() method being 
        // called as part of the initial project loading when opening a project. In the latter case, the
        // RioLogConsoleProjectService.update() is called via the ProjectComponent.projectOpened() method
        if (myModule.getProject().isOpen())
        {
            //TODO - finish implmenting
            LOG.warn("[FRC] Still need to implement FrcModuleComponentImpl.moduleAdded()");
            //RioLogConsoleProjectService.update(myModule);
        }
    }


    @Override
    public void initComponent()
    {
        LOG.debug("[FRC] " + getClass().getSimpleName() + ".initComponent() called for module " + myModule);
    }


    @Override
    public void disposeComponent()
    {
        LOG.debug("[FRC] " + getClass().getSimpleName() + ".disposeComponent() called for module " + myModule);
        if (myModule.isDisposed())
        {
            //TODO - finish implmenting
            LOG.warn("[FRC] Still need to implement FrcModuleComponentImpl.disposeComponent()");
            //RioLogConsoleProjectService.update(myModule);
        }
    }

}
