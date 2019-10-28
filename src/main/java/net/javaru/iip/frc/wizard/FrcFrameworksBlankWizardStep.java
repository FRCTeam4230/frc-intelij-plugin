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

package net.javaru.iip.frc.wizard;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;



public class FrcFrameworksBlankWizardStep extends ModuleWizardStep implements Disposable
{
    private static final Logger LOG = Logger.getInstance(FrcFrameworksBlankWizardStep.class);
    
    
    private JPanel myPanel;
    @NotNull
    private final FrcModuleBuilder myBuilder;
    @NotNull
    private final WizardContext myContext;
    @Nullable
    private final Project myProjectOrNull;
    
    public FrcFrameworksBlankWizardStep(@NotNull FrcModuleBuilder builder,
                                        @NotNull WizardContext context)
    {
        LOG.trace("[FRC] Entering FrcFrameworksBlankWizardStep constructor");
        this.myBuilder = builder;
        this.myContext = context;
        this.myProjectOrNull = context.getProject();
        LOG.trace("[FRC] Exiting FrcFrameworksBlankWizardStep constructor");
    }
    
    
    @Override
    public void dispose()
    {
    }
    
    @Override
    public JComponent getComponent() { return myPanel; }
    
    
    /** Commits data from UI into ModuleBuilder and WizardContext */
    @Override
    public void updateDataModel()
    {
                
    }
    
    
    @Override
    public void disposeUIResources()
    {
        Disposer.dispose(this);
    }
    
    
    @Override
    public boolean validate() throws ConfigurationException
    {
        // TODO: We need to get the min version programmatically. We can get it from C:\Users\Public\frc${frcYear}\jdk\release
        
        // TODO: Implement validation properly
        //  The validation does not work because the below Validate method tries to get the SDK from the wizardContext
        //  However, that returns null because this validate method is called prior to the updateDataModel() method which sets the SDK as the first step is exited
        //  I have a question posted on how we might be able to do this:
        //     https://intellij-support.jetbrains.com/hc/en-us/community/posts/360006464099-Valdating-slected-Project-SDK-version-on-the-first-wizard-page
    
        return true;
//        return FrcUtilsKt.validateMinimumJavaVersion(myBuilder,
//                                                     myContext,
//                                                     11);
    }
    
}
