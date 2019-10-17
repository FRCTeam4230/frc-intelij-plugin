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

import java.awt.*;
import java.util.Collections;
import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.ide.util.newProjectWizard.AddSupportForFrameworksPanel;
import com.intellij.ide.util.newProjectWizard.impl.FrameworkSupportModelBase;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainer;
import com.intellij.openapi.roots.ui.configuration.projectRoot.LibrariesContainerFactory;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.components.JBLabel;



public class FrcFrameworksWizardStep extends ModuleWizardStep implements Disposable
{
    private static final Logger LOG = Logger.getInstance(FrcFrameworksWizardStep.class);
    
    
    private JPanel myPanel;
    private JPanel myOptionsPanel;
    private JPanel myFrameworksPanelPlaceholder;
    private final AddSupportForFrameworksPanel myFrameworksPanel;
    private JCheckBox kdslCheckBox;
    private JBLabel myFrameworksLabel;
    
    
    public FrcFrameworksWizardStep(WizardContext context, final FrcModuleBuilder builder)
    {
        @Nullable Project project = context.getProject();
        final LibrariesContainer container = LibrariesContainerFactory.createContainer(context.getProject());
    
        FrameworkSupportModelBase model = new FrameworkSupportModelBase(project, builder, container)
        {
            @NotNull
            @Override
            public String getBaseDirectoryForLibrariesPath()
            {
                return StringUtil.notNullize(builder.getContentEntryPath());
            }
        };
    
        myFrameworksPanel = new AddSupportForFrameworksPanel(Collections.emptyList(), model, true, null);
    
        setFrcFrameworkSupportProviders();
    
        Disposer.register(this, myFrameworksPanel);
        myFrameworksPanelPlaceholder.add(myFrameworksPanel.getMainPanel());
    
        ModuleBuilder.ModuleConfigurationUpdater configurationUpdater = new ModuleBuilder.ModuleConfigurationUpdater()
        {
            @Override
            public void update(@NotNull Module module, @NotNull ModifiableRootModel rootModel)
            {
                myFrameworksPanel.addSupport(module, rootModel);
            }
        };
        builder.addModuleConfigurationUpdater(configurationUpdater);
    
        ((CardLayout) myOptionsPanel.getLayout()).show(myOptionsPanel, "frameworks card");
    
        // TODO: Remove when Kotlin DSL support is added.
        kdslCheckBox.setEnabled(false);
        // kdslCheckBox.addChangeListener();
        
    }
    
    
    private void setFrcFrameworkSupportProviders()
    {
//        List<FrameworkSupportInModuleProvider> providers = new ArrayList<>();
//        Collections.addAll(providers, FrcFrameworkSupportProvider.EP_NAME.getExtensions());
//        // TODO: Ideally, I do not think Java should be "optional" even if it is selected by default. This may just be a case of not using/including the FrcJavaFrameworkSupportProvider as I think it is only used to add to the gradle script as needed
//        myFrameworksPanel.setProviders(providers, Collections.emptySet(), Collections.singleton("java" /* <-- this is FrcJavaFrameworkSupportProvider.ID*/ ));
//    
    }
    
    
    @Override
    public JComponent getComponent() { return myPanel; }
    
    
    /** Commits data from UI into ModuleBuilder and WizardContext */
    @Override
    public void updateDataModel()
    {
    }
    
    
    @Override
    public void dispose()
    {
    }
    
    
    @Override
    public void disposeUIResources()
    {
        Disposer.dispose(this);
    }
}
