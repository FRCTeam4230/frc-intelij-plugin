/*
 * Copyright 2015-2020 the original author or authors
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

package net.javaru.iip.frc.wpilib.services

import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.startup.StartupManager
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.i18n.FrcBundle.message
import net.javaru.iip.frc.notify.FrcNotifications
import net.javaru.iip.frc.notify.FrcNotifications.Companion.FRC_ACTIONABLE_NOTIFICATION_GROUP
import net.javaru.iip.frc.notify.FrcNotifications.Companion.FRC_GENERAL_NOTIFICATION_GROUP
import net.javaru.iip.frc.settings.FrcApplicationSettings
import net.javaru.iip.frc.wpilib.getAttachedWpiLibVersion
import net.javaru.iip.frc.wpilib.gradlePluginRepo.GradleRioMavenMetadataState
import net.javaru.iip.frc.wpilib.version.WpiLibVersion
import net.javaru.iip.frc.wpilib.version.filterToLatestForYear

// GitConflictsToolWindowManager is a good example of using the StartupActivity
// AcceptedLanguageLevelsSettings sows a class tha is both a StartupActivity and an application Service

// For IJ v2019.3+ we can/should use StartupActivity.Background (and change the plugin.xml element to <backgroundPostStartupActivity>)
class WpiLibVersionStartupActivity : StartupActivity
{
    override fun runActivity(project: Project)
    {
        if (FrcApplicationSettings.getInstance().checkWpiLibStatusOnProjectStartup && project.isFrcFacetedProject())
        {
            StartupManager.getInstance(project).runWhenProjectIsInitialized() {
                WpiLibVersionService.getInstance(project).checkWpiLibStatusAndAlertIfNeeded()
            }
        }
    }
}

class WpiLibVersionService private constructor(private val project: Project)
{
    private val LOG = Logger.getInstance(WpiLibVersionService::class.java)
    
    companion object
    {
        @JvmStatic
        fun getInstance(project: Project) = project.service<WpiLibVersionService>()
    }

    fun checkWpiLibStatusAndAlertIfNeeded(notifyIfNoUpdateAvailable: Boolean = false)
    {
        val versionStatus = getWpiLibVersionStatus()
        
        if (versionStatus != null)
        {
            if (versionStatus.updateAvailableForCurrentYear())
            {
                notifyNewerWpiLibVersionIsAvailable(versionStatus)
            }
            else
            {
                noUpdateAvailable()
            }
        }
        else
        {
            notifyUnableToCheckVersionStatus()
        }
        
    }
    
    private fun notifyNewerWpiLibVersionIsAvailable(versionStatus: WpiLibVersionStatus)
    {
        if (versionStatus.updateAvailableForCurrentYear())
        {
            val availVerString = versionStatus.latestAvailableForSameYear.versionString
            val currVerString = versionStatus.attachedVersion.versionString
            
            val subtitle = message("frc.notification.wpiLibVersionStatus.updateAvailable.subtitle", availVerString)
            val content = message("frc.notification.wpiLibVersionStatus.updateAvailable.content", availVerString, currVerString)
            
            // TODO need to implement update capability - and then move this to I18N into the above resource bundle string and update the below event handler
            //content.append("Would you like to update the Gradle build script to use the new version? <a href='makeUpdate'>Yes</a>  <a href='doNotUpdate'>No</a>")

            val notification = FRC_ACTIONABLE_NOTIFICATION_GROUP
                .createNotification(FrcNotifications.Title,
                                    subtitle,
                                    content,
                                    NotificationType.INFORMATION
                                   ) 
//                { theNotification: Notification, event: HyperlinkEvent ->
//                    theNotification.expire()
//                    if ("makeUpdate" == event.description)
//                    {
//                        TODO("Call Update Gradle Function once written")
//                    }
//                }
            notification.notify(project)
            
        }
    }

    private fun notifyUnableToCheckVersionStatus()
    {
        val content = message("frc.notification.wpiLibVersionStatus.unableToCheck.content")
        val notification = FRC_GENERAL_NOTIFICATION_GROUP
            .createNotification(FrcNotifications.Title,
                                null,
                                content,
                                NotificationType.INFORMATION)

        notification.notify(project)
    }
    
    private fun noUpdateAvailable()
    {
        val content = message("frc.notification.wpiLibVersionStatus.haveTheLatest.content")
        val notification = FRC_GENERAL_NOTIFICATION_GROUP
            .createNotification(FrcNotifications.Title,
                                null,
                                content,
                                NotificationType.INFORMATION)

        notification.notify(project)
    }
    
    
    // TODO - we only want to check if we have not done so recently
    fun getWpiLibVersionStatus(): WpiLibVersionStatus?
    {
        var versionStatus: WpiLibVersionStatus? = null
        DumbService.getInstance(project).runReadActionInSmartMode() {
            if (!project.isDisposed && project.isFrcFacetedProject())
            {
                val state = GradleRioMavenMetadataState.getInstance(true)
                val latestAvailableVersion = state.wpiLibMavenMetadata.latestAsWpiLibVersion

                val attachedVersion = project.getAttachedWpiLibVersion()
                if (attachedVersion != null)
                {
                    // TODO: we should check for a conflict between project year in the wpi_lib_preferences and the attached library? 
                    //       This should be added as inspection that runs on project startup, and anytime the preferences file changes
                    val projectYear = attachedVersion.frcYear

                    // TODO: if attached is null, we should check the Gradle file and see what is configured

                    val latestAvailableForSameYear = state.wpiLibMavenMetadata.wpiLibVersions.filterToLatestForYear(projectYear)
                    if (latestAvailableForSameYear != null)
                    {
                        versionStatus = WpiLibVersionStatus(attachedVersion, latestAvailableForSameYear, latestAvailableVersion)
                    }
                }
            }
        }
        
        return versionStatus
    }
}


data class WpiLibVersionStatus(val attachedVersion: WpiLibVersion,
                               val latestAvailableForSameYear: WpiLibVersion,
                               val latestAvailableVersion: WpiLibVersion?)
{
    fun updateAvailableForCurrentYear(): Boolean = latestAvailableForSameYear.isNewerThan(attachedVersion)
}
