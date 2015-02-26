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
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.SimpleModificationTracker;

import net.javaru.iip.frc.FrcPluginGlobals;
import net.javaru.iip.frc.ui.FrcSettingsForm;



@State(
    name = FrcPluginGlobals.FRC_PLUGIN_COMPONENT_NAME,
    storages = {@Storage(id = FrcPluginGlobals.FRC_PLUGIN_COMPONENT_NAME, file = StoragePathMacros.APP_CONFIG + "/FRC/FrcPlugin.xml")})
public class FrcApplicationComponent extends SimpleModificationTracker implements Configurable,
                                                                                  ProjectComponent,
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
    public static FrcApplicationComponent getInstance() {return ApplicationManager.getApplication().getComponent(FrcApplicationComponent.class); }

    // ==== BaseComponent


    @Override
    public void initComponent()
    {
        //TODO: Write this 'initComponent' implemented method in the 'FrcApplicationComponent' class
    }


    @Override
    public void disposeComponent()
    {
        //TODO: Write this 'disposeComponent' implemented method in the 'FrcApplicationComponent' class
    }


    // ==== ExportableComponent


    @NotNull
    @Override
    public File[] getExportFiles()
    {
        //TODO; Need to verify this works / is correct
        return new File[] {PathManager.getOptionsFile("/FRC/FrcPlugin")};
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
            //TODO: may possibly need to set defaults here
        }
        return settings;
    }


    @Override
    public void loadState(FrcSettings frcSettings) { this.settings = frcSettings; }


    // ==== ProjectComponent


    @Override
    public void projectOpened()
    {
        //TODO: Write this 'projectOpened' implemented method in the 'FrcApplicationComponent' class
    }


    @Override
    public void projectClosed()
    {
        //TODO: Write this 'projectClosed' implemented method in the 'FrcApplicationComponent' class
    }

    // ==== NamedComponent


    @NotNull
    @Override
    public String getComponentName() { return FrcPluginGlobals.FRC_PLUGIN_COMPONENT_NAME; }

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
        //TODO: if we implement an service manager, we need to call FrcServiceManager.resetSettings()

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


    @Override
    public void disposeUIResources() { settingsForm = null; }


}
