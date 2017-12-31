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

package net.javaru.iip.frc.settings;

import javax.swing.*;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;

import net.javaru.iip.frc.i18n.FrcMessageBundle;
import net.javaru.iip.frc.riolog.RioLogProjectService;



// This class is registered in the plugin.xml as an <applicationConfigurable>
public class FrcApplicationSettingsConfigurable implements SearchableConfigurable 
{
    private static final Logger LOG = Logger.getInstance(FrcApplicationSettingsConfigurable.class);

    private FrcApplicationSettingsForm myForm;

    private static FrcApplicationSettingsConfigurable defaultInstance = new FrcApplicationSettingsConfigurable();
    

    @NotNull
    public static FrcApplicationSettingsConfigurable getInstance()
    {
        final FrcApplicationSettingsConfigurable instance = ApplicationManager.getApplication().getComponent(FrcApplicationSettingsConfigurable.class,
                                                                                                             defaultInstance);
        return instance != null ? instance : defaultInstance;
    }
    
    @Nls
    @Override
    public String getDisplayName()
    {
        return FrcMessageBundle.message("frc.ui.application.settings.display.name");
    }


    @Nullable
    @Override
    public String getHelpTopic() { return null; }


    @Nullable
    @Override
    public JComponent createComponent()
    {
        myForm = new FrcApplicationSettingsForm();
        return myForm.getRootPanel();
    }


    @Override
    public boolean isModified()
    {
        return myForm != null && myForm.isModified();
    }


    @Override
    public void apply() throws ConfigurationException
    {
        myForm.applyTo(FrcApplicationSettings.Settings.INSTANCE());
        RioLogProjectService.updateAllOpenProjects();
    }


    @Override
    public void reset() { myForm.load(FrcApplicationSettings.Settings.INSTANCE()); 
    }


    @Override
    public void disposeUIResources() { myForm = null; }


    @NotNull
    @Override
    public String getId() // Specified in SearchableConfigurable
    {
        return "frc.application.settings";
    }
}
