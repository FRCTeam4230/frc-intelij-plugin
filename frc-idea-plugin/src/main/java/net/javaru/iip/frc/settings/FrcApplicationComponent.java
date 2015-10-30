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

package net.javaru.iip.frc.settings;

import java.io.File;
import javax.swing.*;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.components.ExportableApplicationComponent;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.SimpleModificationTracker;

import net.javaru.iip.frc.FrcPluginGlobals;
import net.javaru.iip.frc.ui.FrcSettingsForm;
import net.javaru.iip.frc.ui.RioLogConsoleProjectService;



// @State persistence documentation: http://www.jetbrains.org/intellij/sdk/docs/basics/persisting_state_of_components.html
@State(
        name = FrcPluginGlobals.FRC_PLUGIN_BASE_NAME,
        storages = {@Storage(id = FrcPluginGlobals.FRC_PLUGIN_BASE_NAME, file = StoragePathMacros.APP_CONFIG + "frc.xml")}
    )
public class FrcApplicationComponent extends SimpleModificationTracker implements Configurable,
                                                                                  ApplicationComponent,
                                                                                  ExportableApplicationComponent,
                                                                                  PersistentStateComponent<FrcSettings>,
                                                                                  ModificationTracker
{

    private static final Logger LOG = Logger.getInstance(FrcApplicationComponent.class);

    private FrcSettings settings;
    private FrcSettingsForm settingsForm;


    public FrcApplicationComponent() { }

    @NotNull
    public static FrcApplicationComponent getInstance() 
    {
        final FrcApplicationComponent component = ApplicationManager.getApplication().getComponent(FrcApplicationComponent.class);
        return component != null ? component : new FrcApplicationComponent();
    }

    // ==== BaseComponent


    @Override
    public void initComponent()
    {
        LOG.debug("[FRC] FrcApplicationComponent.initComponent() has been called");
    }


    @Override
    public void disposeComponent()
    {
        LOG.debug("[FRC] FrcApplicationComponent.initComponent() has been called");
    }


    // ==== ExportableComponent


    @NotNull
    @Override
    public File[] getExportFiles()
    {
        return new File[] {PathManager.getOptionsFile("frc")};
    }


    @NotNull
    @Override
    public String getPresentableName()
    {
        return FrcPluginGlobals.FRC_PLUGIN_DISPLAY_NAME;
    }


    // ==== PersistentStateComponent


    @Nullable
    @Override
    public FrcSettings getState()
    {
        if (settings == null)
        {
            settings = new FrcSettings();
        }
        return settings;
    }


    @Override
    public void loadState(FrcSettings frcSettings) { this.settings = frcSettings; }



    // ==== NamedComponent


    @NotNull
    @Override
    public String getComponentName() { return getClass().getSimpleName(); }

    // ==== Configurable


    @Nls
    @Override
    public String getDisplayName() { return FrcPluginGlobals.FRC_PLUGIN_DISPLAY_NAME; }


    @Nullable
    @Override
    public String getHelpTopic() { return null; }


    // ==== UnnamedConfigurable


    @Nullable
    @Override
    public JComponent createComponent()
    {
        if (settingsForm == null)
        {
            settingsForm = new FrcSettingsForm(getState());
        }
        return settingsForm.getRootComponent();
    }


    @Override
    public boolean isModified()
    {
        return settingsForm != null && settingsForm.isSettingsModified(settings);
    }


    @Override
    public void apply() throws ConfigurationException
    {
        final FrcSettings newSettings = settingsForm.getFrcSettings();
        this.settings = newSettings.clone();
        updateRioLogConsole();
    }


    @Override
    public void reset()
    {
        if (settingsForm != null)
        {
            final FrcSettings clone = settings.clone();
            settingsForm.importFrom(clone);
        }
    }

    protected void updateRioLogConsole()
    {
        final Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : openProjects)
        {
            RioLogConsoleProjectService.update(project);
        }
    }

    @Override
    public void disposeUIResources() { settingsForm = null; }


}
