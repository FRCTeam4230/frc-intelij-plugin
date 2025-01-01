/*
 * Copyright 2015-2021 the original author or authors.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.javaru.iip.frc.services

import com.intellij.ProjectTopics
import com.intellij.facet.Facet
import com.intellij.facet.FacetManager
import com.intellij.facet.FacetManagerListener
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.diagnostic.trace
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.ModuleListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ModuleRootListener
import com.intellij.openapi.startup.ProjectActivity
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.messages.MessageBusConnection
import net.javaru.iip.frc.facet.isFrcFacet
import net.javaru.iip.frc.facet.isFrcFacetedModule
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.notify.FrcNotificationsTracker
import net.javaru.iip.frc.notify.notifyToConfigureTeamNumIfNecessary
import net.javaru.iip.frc.riolog.RioLogProjectService
import net.javaru.iip.frc.riolog.udp.RioLogUdpSocketManagerApplicationService
import net.javaru.iip.frc.riolog.ui.FrcRioLogToolWindowExecutor
import net.javaru.iip.frc.settings.FrcProjectTeamNumberService


// Do NOT make a StartupActivity.Background since we want indexing to be complete
class FrcProjectLifecycleService private constructor(val project: Project) : ModuleRootListener,
                                                                             ModuleListener,
                                                                             Disposable,
                                                                             FacetManagerListener
{
    private val logger = logger<FrcProjectLifecycleService>()


    companion object
    {
        @Suppress("RemoveExplicitTypeArguments")
        @JvmStatic
        fun getInstance(project: Project): FrcProjectLifecycleService = project.service<FrcProjectLifecycleService>()
    }

    internal fun registerListeners()
    {
        logger.trace {"[FRC] FrcProjectLifecycleService.registerListeners() called for project '$project'"}
        val connection: MessageBusConnection = project.messageBus.connect()
        connection.subscribe(ProjectTopics.MODULES, this)
        connection.subscribe(ProjectTopics.PROJECT_ROOTS, this)
        connection.subscribe(FacetManager.FACETS_TOPIC, this)
    }



    internal fun runFrcProjectStartupActivities(project: Project)
    {
        // Projects should be fully initialized at this point (per JavaDoc in StartupActivity)
        if (project.isOpen && project.isFrcFacetedProject() && !project.isDisposed)
        {
            // FYI: The FrcPluginVersionManagerStartupActivity also does some notification work

            //RioLogProjectService.getInstance(project).activateTcp()
            RioLogProjectService.getInstance(project).update()

            // TODO Move into the RioLogProjectService so the update method takes a setting if we should open or not
            val includeDesktopSupport = ReadAction.compute<Boolean, Throwable> {
                FrcGradleService.getInstance(project).isIncludeDesktopSupport() ?: false
            }
            logger.info("[FRC] includeDesktopSupport for project '$project' is: $includeDesktopSupport ")
            if (includeDesktopSupport)
            {
                invokeLater {
                    // Issue #114 - AlreadyDisposedException was happening when then runnable was invoked.
                    if (project.isOpen && !project.isDisposed) {
                        try
                        {
                            val toolWindow = ToolWindowManager.getInstance(project).getToolWindow(FrcRioLogToolWindowExecutor.FRC_RIO_LOG_TOOL_WINDOW_ID)
                            if (toolWindow != null && !toolWindow.isDisposed)
                            {
                                toolWindow.hide()
                            }
                        }
                        catch (e: com.intellij.serviceContainer.AlreadyDisposedException)
                        {
                            logger.warn("[FRC] Cannot access FRC Tool Window as it is already disposed: $e", e)
                        }
                    }
                }
            }

            // initialize the registering of the VFS Change Listener so we can detect changes to the project team number
            FrcProjectTeamNumberService.getInstance(project)

            notifyToConfigureTeamNumIfNecessary(project, true)

            checkProjectFrcStatus(project, knownFacetedProject = true, checkTeamNumConfigStatus = false)
        }
    }

    override fun dispose() // This is basically our projectClosed() method
    {
        logger.trace {"[FRC] FrcProjectLifecycleService.dispose() called for project '$project'"}
        FrcNotificationsTracker.clearAllForProject(project)
        service<RioLogUdpSocketManagerApplicationService>().deregister(project)
    }

    override fun modulesAdded(project: Project, modules: MutableList<out Module>)
    {
        logger.trace {"[FRC] FrcProjectLifecycleService.moduleAdded() called for modules '$modules' on project '$project'"}
        // We only want to update the RioLogConsole if the project is fully opened. In other words, this is a
        // case where the user is adding a module to an open project rather than this moduleAdded() method being
        // called as part of the initial project loading when opening a project. In the latter case, the
        // RioLogProjectService.update() is called via the ProjectComponent.projectOpened() method
        // We only want to update the RioLogConsole if the project is fully opened. In other words, this is a
        // case where the user is adding a module to an open project rather than this moduleAdded() method being
        // called as part of the initial project loading when opening a project. In the latter case, the
        // RioLogProjectService.update() is called via the ProjectComponent.projectOpened() method
        modules.forEach {
            if (it.project.isOpen && it.isFrcFacetedModule() && !it.isDisposed && !it.project.isDisposed)
            {
                RioLogProjectService.getInstance(it.project).update()
            }
        }
    }

    override fun beforeModuleRemoved(project: Project, module: Module)
    {
        logger.trace {"[FRC] FrcProjectLifecycleService.beforeModuleRemoved() called for module '$module'"}
        if (module.isFrcFacetedModule() && !module.isDisposed)
        {
            RioLogProjectService.getInstance(module.project).update()
        }
    }

    override fun moduleRemoved(project: Project, module: Module)
    {
        logger.debug {"[FRC] FrcProjectLifecycleService.disposeComponent() called for module '$module'"}
        if (module.isFrcFacetedModule() && !module.isDisposed)
        {
            RioLogProjectService.getInstance(module.project).update()
        }
    }

    override fun facetAdded(facet: Facet<*>)
    {
        if (facet.isFrcFacet())
        {
            val theProject = facet.module.project
            RioLogProjectService.getInstance(theProject).update()
            checkProjectFrcStatus(theProject, knownFacetedProject = true, checkTeamNumConfigStatus = true)
        }
    }


    override fun facetConfigurationChanged(facet: Facet<*>) = updateForFrcFacet(facet)


    override fun facetRemoved(facet: Facet<*>) = updateForFrcFacet(facet)


    private fun updateForFrcFacet(facet: Facet<*>)
    {
        if (facet.isFrcFacet())
        {
            RioLogProjectService.getInstance(facet.module.project).update()
        }
    }

    fun checkProjectFrcStatus(project: Project, knownFacetedProject: Boolean, checkTeamNumConfigStatus: Boolean)
    {
        DumbService.getInstance(project).runWhenSmart {
            if (checkTeamNumConfigStatus)
            {
                notifyToConfigureTeamNumIfNecessary(project, knownFacetedProject)
            }
        }
    }

}

class FrcProjectLifecycleServiceStartupActivity : ProjectActivity
{
    override suspend fun execute(project: Project)
    {
        val service = FrcProjectLifecycleService.getInstance(project)
        service.registerListeners()

        // Handle opening of existing FRC projects
        service.runFrcProjectStartupActivities(project)
    }
}