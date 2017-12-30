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

package net.javaru.iip.frc.wpilib.version;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import net.javaru.iip.frc.wpilib.WpiLibLibrariesUtils;



public class WpiLibVersionStatus
{
    private static final Logger LOG = Logger.getInstance(WpiLibVersionStatus.class);

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
    
    @Nonnull
    private final String attachedVersionSummary;

    @Nonnull
    private final String downloadedVersionSummary;

    @Nonnull
    private final String availableVersionSummary;


    //TODO i18n
    
    private WpiLibVersionStatus(@Nullable Project project,
                                boolean wpiLibAttached, 
                                @Nullable WpiLibVersion attachedVersion,
                                boolean wpiLibDownloaded, 
                                @Nullable WpiLibVersion downloadedVersion,
                                @Nullable WpiLibVersion availableVersion)
    {
        this.project = project;
        this.wpiLibAttached = wpiLibAttached;
        this.attachedVersion = attachedVersion;
        this.wpiLibDownloaded = wpiLibDownloaded;
        this.downloadedVersion = downloadedVersion;
        this.availableVersion = availableVersion;

        
        if (wpiLibAttached)
        {
            if (attachedVersion != null)
            {
                attachedVersionSummary = "Attached Version is: " + attachedVersion.getVersion();
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
                downloadedVersionSummary = "Downloaded Version is: " + downloadedVersion.getVersion();
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
            availableVersionSummary = "Available for download: " + availableVersion.getVersion();
        }
        else
        {
            availableVersionSummary = "Could not determine latest version available for download";
        }
    }


    /**
     * Determines the current versions of the WPILib. If project is null, the attached version is not determined.
     * @param project the project, if any, to check the attached version for
     * @return the current versions of the WPILib
     */
    public static WpiLibVersionStatus getCurrentStatus(@Nullable Project project)
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
            catch (Exception ignore) {};
        }
        
        try
        {
            wpiLibDownloaded = WpiLibLibrariesUtils.isWpilibDownloadedToSystemViaReadAction();
            if (wpiLibDownloaded)
            {
                downloadedVersion = WpiLibLibrariesUtils.determineSystemAvailableWpiLibVersionViaReadAction();
            }
        }
        catch (Exception ignore) {};
        
        try
        {
            availableVersion = WpiLibLibrariesUtils.determineAvailableWpiLibVersion();
        }
        catch (Exception ignore) {};
        
        return new WpiLibVersionStatus(project, wpiLibAttached, attachedVersion, wpiLibDownloaded, downloadedVersion, availableVersion);
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


    @Nonnull
    public String getAttachedVersionSummary() { return attachedVersionSummary; }


    @Nonnull
    public String getDownloadedVersionSummary() { return downloadedVersionSummary; }


    @Nonnull
    public String getAvailableVersionSummary() { return availableVersionSummary; }


    public boolean isNewerVersionAvailableThanAttached()
    {
        return availableVersion != null && attachedVersion != null && availableVersion.isNewThan(attachedVersion);
    }
    
    
    public boolean isNewerVersionAvailableThanDownloaded()
    {
        return availableVersion != null && downloadedVersion != null && availableVersion.isNewThan(downloadedVersion);
    }


    public boolean isNewerVersionDownloadedThanAttached()
    {
        return downloadedVersion != null && attachedVersion != null && downloadedVersion.isNewThan(attachedVersion);
    }


    @Override
    public String toString()
    {
        return getAttachedVersionSummary() +  "; " + getDownloadedVersionSummary() + "; " + getAvailableVersionSummary();
    }
}
