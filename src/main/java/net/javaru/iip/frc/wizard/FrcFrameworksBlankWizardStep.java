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
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.lang.JavaVersion;

import net.javaru.iip.frc.FrcPluginGlobals;
import net.javaru.iip.frc.util.FrcUtilsKt;
import net.javaru.iip.frc.util.TitleMessagePair;



public class FrcFrameworksBlankWizardStep extends ModuleWizardStep implements Disposable
{
    private static final Logger LOG = Logger.getInstance(FrcFrameworksBlankWizardStep.class);
    
    
    private JPanel myPanel;
    private JPanel frcLogoPanel;
    private JBLabel sdkNoticeLabel;
    private JBLabel invalidSdkSelectedLabel;
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
        initComponents();
        LOG.trace("[FRC] Exiting FrcFrameworksBlankWizardStep constructor");
    }
    
    
    private void initComponents()
    {
        updateInvalidSdkLabelVisibility();
        myBuilder.addSdkChangedListener(this::updateInvalidSdkLabelVisibility);
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
        if (!myBuilder.isSelectedSdkValid())
        {
            @Nullable
            final Sdk sdk = myBuilder.getSelectedSdk();
            // The build-in functionality opens a warning if there is no SDK selected. So we do not want to double prompt
            // So we just handle an invalid version & non-JDK
            if (sdk != null) 
            {
                JavaVersion configuredJavaVersion = null;
                if (sdk instanceof ProjectJdkImpl)
                {
                    configuredJavaVersion = JavaVersion.tryParse(sdk.getVersionString());
                }
        
                final TitleMessagePair titleMsgPair =
                        FrcUtilsKt.createInvalidJdkTitleMessagePair(FrcPluginGlobals.DEFAULT_MIN_REQUIRED_JAVA_VERSION,
                                                                    configuredJavaVersion,
                                                                    null);
                throw new ConfigurationException(titleMsgPair.getMessage(), titleMsgPair.getTitle());
            }
        }
        return true;
    }
    
    protected void updateInvalidSdkLabelVisibility()
    {
        invalidSdkSelectedLabel.setVisible(!myBuilder.isSelectedSdkValid());
    }
}
