/*
 * Copyright 2015-2018 the original author or authors
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

package net.javaru.iip.frc.wpilib.version;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.wpilib.WpiLibLibrariesUtils;



/**
 * A class that provides the current version of WPILib in its three forms: attached, downloaded, available for download. 
 * It also has convenience/utility methods for working with the versions. To create/construct, use the factory method
 * {@link WpiLibVersionStatus#getCurrentVersionStatus(Project) WpiLibVersionStatus#getCurrentVersionStatus(@Nullable Project)}.
 */
public class WpiLibVersionStatus
{
    @Nullable
    private final Project project;
    @Nullable
    private final WpiLibVersion attachedVersion;
    @Nullable
    private final WpiLibVersion downloadedVersion;
    @Nullable
    private final WpiLibVersion availableVersion;
    
    private final boolean wpiLibAttached;
    private final boolean wpiLibDownloaded;
    
    @NotNull
    private final String attachedVersionSummary;

    @NotNull
    private final String downloadedVersionSummary;

    @NotNull
    private final String availableVersionSummary;

    private final boolean availableVersionChecked;

    //TODO i18n
    
    // use getCurrentVersionStatus(Project)
    private WpiLibVersionStatus(@Nullable Project project,
                                boolean wpiLibAttached,
                                @Nullable WpiLibVersion attachedVersion,
                                boolean wpiLibDownloaded,
                                @Nullable WpiLibVersion downloadedVersion,
                                @Nullable WpiLibVersion availableVersion, 
                                boolean availableVersionChecked)
    {
        this.project = project;
        this.wpiLibAttached = wpiLibAttached;
        this.attachedVersion = attachedVersion;
        this.wpiLibDownloaded = wpiLibDownloaded;
        this.downloadedVersion = downloadedVersion;
        this.availableVersion = availableVersion;
        this.availableVersionChecked = availableVersionChecked;
        
        if (wpiLibAttached)
        {
            if (attachedVersion != null)
            {
                attachedVersionSummary = "Attached Version is: " + attachedVersion.getVersionString();
            }
            else
            {
                attachedVersionSummary = "Attached Version is undetermined";
            }
        }
        else
        {
            attachedVersionSummary = "WPILib is not attached";
        }    
    
        if (wpiLibDownloaded)
        {
            if (downloadedVersion != null)
            {
                downloadedVersionSummary = "Downloaded Version is: " + downloadedVersion.getVersionString();
            }
            else 
            {
                downloadedVersionSummary = "Downloaded Version is undetermined";
            }
        }
        else
        {
            downloadedVersionSummary = "WPILib is not downloaded to the system";
        }
    
        
        if (availableVersion != null)
        {
            availableVersionSummary = "Available for download: " + availableVersion.getVersionString();
        }
        else
        {
            availableVersionSummary = availableVersionChecked ? "Could not determine latest version available for download" : "Not Checked";
        }
    }


    public static WpiLibVersionStatus getCurrentVersionStatus(@Nullable Project project)
    {
        return getCurrentVersionStatus(project, true);
    }
    
    /**
     * Determines the current versions of the WPILib. If project is null, the attached version is not determined.
     * @param project the project, if any, to check the attached version for
     * @return the current versions of the WPILib
     */
    public static WpiLibVersionStatus getCurrentVersionStatus(@Nullable Project project, boolean checkLatestAvailable)
    {
        WpiLibVersion attachedVersion = null;
        WpiLibVersion downloadedVersion = null;
        WpiLibVersion availableVersion = null;
        boolean wpiLibAttached = false;
        boolean wpiLibDownloaded = false;
        if (project != null)
        {
            try
            {
                wpiLibAttached = WpiLibLibrariesUtils.isWpilibAttachedViaReadAction(project);
                if (wpiLibAttached) 
                {
                    attachedVersion = WpiLibLibrariesUtils.determineAttachedWpiLibVersionViaReadAction(project);
                }
            }
            catch (Exception ignore) {}
        }
        
        try
        {
            wpiLibDownloaded = WpiLibLibrariesUtils.isWpilibDownloadedToSystemViaReadAction();
            if (wpiLibDownloaded)
            {
                downloadedVersion = WpiLibLibrariesUtils.determineSystemAvailableWpiLibVersionViaReadAction();
            }
        }
        catch (Exception ignore) {}
        
        try
        {
            availableVersion = checkLatestAvailable ? WpiLibLibrariesUtils.determineAvailableWpiLibVersion() : null;
        }
        catch (Exception ignore) {}
        
        return new WpiLibVersionStatus(project, wpiLibAttached, attachedVersion, wpiLibDownloaded, downloadedVersion, availableVersion, checkLatestAvailable);
    }


    @Nullable
    public Project getProject() { return project; }


    @Nullable
    public WpiLibVersion getAttachedVersion() { return attachedVersion; }


    @Nullable
    public WpiLibVersion getDownloadedVersion() { return downloadedVersion; }


    @Nullable
    public WpiLibVersion getAvailableVersion() { return availableVersion; }


    public boolean isWpiLibAttached() { return wpiLibAttached; }


    public boolean isWpiLibDownloaded() { return wpiLibDownloaded; }
    
    
    public boolean isAvailableVersionDeterminable() { return availableVersion != null; }


    @NotNull
    public String getAttachedVersionSummary() { return attachedVersionSummary; }


    @NotNull
    public String getDownloadedVersionSummary() { return downloadedVersionSummary; }


    @NotNull
    public String getAvailableVersionSummary() { return availableVersionSummary; }


    public boolean isNewerVersionAvailableThanAttached()
    {
        final boolean isNewerAvailable = availableVersion != null && attachedVersion != null && availableVersion.isNewerThan(attachedVersion);
        Logger.getInstance(WpiLibVersionStatus.class).debug("[FRC] isNewerVersionAvailableThanAttached() = " + isNewerAvailable + " for avail of " + availableVersionSummary + " and attached of " + attachedVersionSummary );
        return isNewerAvailable;
    }
    
    
    public boolean isNewerVersionAvailableThanDownloaded()
    {
        return availableVersion != null && downloadedVersion != null && availableVersion.isNewerThan(downloadedVersion);
    }


    public boolean isNewerVersionDownloadedThanAttached()
    {
        return downloadedVersion != null && attachedVersion != null && downloadedVersion.isNewerThan(attachedVersion);
    }


    public boolean wasAvailableVersionChecked() { return availableVersionChecked; }


    @Override
    public String toString()
    {
        return getAttachedVersionSummary() +  "; " + getDownloadedVersionSummary() + "; " + getAvailableVersionSummary();
    }
}
