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

import com.intellij.openapi.application.ApplicationManager;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
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
import com.intellij.util.ui.AsyncProcessIcon;

import net.javaru.iip.frc.i18n.FrcBundle;
import net.javaru.iip.frc.util.FrcJavaLangUtilsKt;
import net.javaru.iip.frc.util.FrcSystemConfigs.WizardAlwaysUpdateWpilibVersions;
import net.javaru.iip.frc.util.TitleMessagePair;
import net.javaru.iip.frc.wpilib.gradlePluginRepo.GradleRioMavenMetadataState;
import net.javaru.iip.frc.wpilib.version.WpiLibVersion;
import net.javaru.iip.frc.wpilib.version.WpiLibVersionFiltersKt;



public class FrcInitialCustomOptionsWizardStep extends ModuleWizardStep implements Disposable
{
    private static final Logger LOG = Logger.getInstance(FrcInitialCustomOptionsWizardStep.class);
    
    
    private JPanel myPanel;
    private JPanel frcLogoPanel;
    private JBLabel sdkNoticeLabel;
    private JBLabel invalidSdkSelectedLabel;
    private JPanel wpiLibVersionPanel;
    private JBLabel wpilibVersionSelectionLabel;
    private JComboBox<WpiLibVersion> wpilibVersionComboBox;
    private JPanel topCardPanel;
    private JPanel spinnerPanel;
    private AsyncProcessIcon asyncProcessIcon;
    private JBLabel spinnerLabel;
    @NotNull
    private final FrcModuleBuilder myBuilder;
    @NotNull
    private final WizardContext myContext;
    @Nullable
    private final Project myProjectOrNull;
    
    // NOTE: See org.jetbrains.idea.maven.wizards.MavenArchetypesStep for example of using the "loading spinner"
    public FrcInitialCustomOptionsWizardStep(@NotNull FrcModuleBuilder builder,
                                             @NotNull WizardContext context)
    {
        LOG.trace("[FRC] Entering FrcInitialCustomOptionsWizardStep constructor");
        this.myBuilder = builder;
        this.myContext = context;
        this.myProjectOrNull = context.getProject();
        initComponents();
        LOG.trace("[FRC] Exiting FrcInitialCustomOptionsWizardStep constructor");
    }
    
    
    private void initComponents()
    {
        updateDataModel();
        updateInvalidSdkLabel();
        myBuilder.addSdkChangedListener(() -> {
            updateDataModel();
            updateInvalidSdkLabel();
        });
        initCardPanel();
        initWpiLibVersionComboBox();
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
        LOG.trace("[FRC] Entering FrcInitialCustomOptionsWizardStep.updateDataModel()");
        final FrcProjectWizardData dataModel = myBuilder.getDataModel();
        WpiLibVersion wpilibVersion = (WpiLibVersion) wpilibVersionComboBox.getSelectedItem();
        if (wpilibVersion != null)
        {
            dataModel.setWpilibVersion(wpilibVersion);
            LOG.debug("[FRC] WpilibVersion set to '" + dataModel.getWpilibVersion() + "' on FrcProjectWizardData");
        }
        else
        {
            LOG.warn("[FRC] wpilibVersionComboBox returned null for the selected item.");
        }
        LOG.trace("[FRC] Exiting FrcInitialCustomOptionsWizardStep.updateDataModel()");
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
                        FrcJavaLangUtilsKt.createInvalidJdkTitleMessagePair(myBuilder.getRequiredJdkVersionForWpiLibVersion(),
                                                                            configuredJavaVersion,
                                                                            null);
                throw new ConfigurationException(titleMsgPair.getMessage(), titleMsgPair.getTitle());
            }
        }
        return true;
    }
    
    protected void updateInvalidSdkLabel()
    {
        invalidSdkSelectedLabel.setText(FrcBundle.message("frc.ui.wizard.sdkRequirement.invalidVersion.text.dynamic",
                                                          myBuilder.getDataModel().getFrcYearString(),
                                                          myBuilder.getRequiredJdkVersionForWpiLibVersion()));
        invalidSdkSelectedLabel.setVisible(!myBuilder.isSelectedSdkValid());
    }
    
    
    private void initWpiLibVersionComboBox()
    {
        //TODO: We need to 
        // ✔a) filter the list to remove the alphas, etc. 
        //  b) have UI option to only show the latest version for each year (on by default) (com/intellij/find/impl/FindPopupPanel.java:1625)
        //  c) have a UI option to show/hide betas (off by default)
        // ✔d) make modifications so that the FrcProjectWizardData.wpilibVersion defaults to the latest and then that the selected item (below) matches
    
        // Checking for newer versions is a network call, which must not be done on the EDT, so it is done on a pooled thread
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            final Duration maxAge = calcMaxAgeDurationToUse();
            final List<WpiLibVersion> versionList = GradleRioMavenMetadataState.getInstance(maxAge).getWpiLibMavenMetadata().getWpiLibVersionsDescending();
            SwingUtilities.invokeLater(() -> initWpiLibVersionComboBox(versionList));
        });
    }

    private void initWpiLibVersionComboBox(final List<WpiLibVersion> versionList)
    {
        // IMPORTANT: No PSI or VirtualFile work should be done inside the SwingUtilities.invokeLater block. It MUST be limited to UI work
        //            ApplicationManager.getApplication().invokeLater does not work here as it apparently waits until the wizard dialog is closed
        //            See the ModalityState class documentation for more information about using SwingUtilities.invokeLater
        {
            LOG.debug("[FRC] initWpiLibVersionComboBox invokeLater block is running");
            // Filter the list to include only releases, and the latest one if it is a release candidate or beta (for an unreleased version)
            final List<WpiLibVersion> filteredList = WpiLibVersionFiltersKt.filterToDefaultListing(versionList);
            final WpiLibVersion[] versions = filteredList.toArray(new WpiLibVersion[0]);
            wpilibVersionComboBox.setModel(new DefaultComboBoxModel<>(versions));
            int index = 0; // default to the first item in the list
            if (filteredList.isEmpty())
            {
                index = -1; // if by some rare chance the list is empty, we set to the -1 flag to say don't select anything.
            }
            else if (filteredList.size() >= 2 && filteredList.get(0).isPreRelease() && !filteredList.get(1).isPreRelease())
            {
                index = 1; // if the first item is a beta or RC we select the second item, i.e. the latest non beta/RC
            }
            wpilibVersionComboBox.setSelectedIndex(index);
            final CardLayout cardLayout = (CardLayout) topCardPanel.getLayout();
            cardLayout.show(topCardPanel, "wpilibVersionSelectionPanelCard");
            wpilibVersionComboBox.addItemListener(itemEvent -> {
                updateDataModel();
                updateInvalidSdkLabel();
            });
            LOG.debug("[FRC] initWpiLibVersionComboBox invokeLater block has completed");
        }
    }
    
    private void initCardPanel()
    {
        final CardLayout cardLayout = (CardLayout) topCardPanel.getLayout();
        cardLayout.show(topCardPanel, "spinnerPanelCard");
    }
    
    private Duration calcMaxAgeDurationToUse()
    {
        // TODO need an application setting to set the maxAge Duration to use, and also add a refresh button on the listing in the wizard
        final LocalDate now = LocalDate.now();
        
        // We want to update regularly during the initial build kickoff time period
        if (WizardAlwaysUpdateWpilibVersions.INSTANCE.getValue() ||
            (now.getMonth() == Month.DECEMBER && now.getDayOfMonth() > 20) ||
            (now.getMonth() == Month.JANUARY && now.getDayOfMonth() >= 16))
        {
            return Duration.ZERO;
        }
        else 
        {
            return Duration.ofMinutes(15);
        }
    }
    
    
    private void createUIComponents()
    {
        //asyncProcessIcon = new AsyncProcessIcon.Big(getClass() + ".loading");
        asyncProcessIcon = new AsyncProcessIcon.BigCentered(getClass() + ".loading");
    }
}
