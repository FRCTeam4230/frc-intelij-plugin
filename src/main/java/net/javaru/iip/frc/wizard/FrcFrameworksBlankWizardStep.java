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

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;



public class FrcFrameworksBlankWizardStep extends ModuleWizardStep implements Disposable
{
    private static final Logger LOG = Logger.getInstance(FrcFrameworksBlankWizardStep.class);
    
    
    private JPanel myPanel;
    
    
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
}
