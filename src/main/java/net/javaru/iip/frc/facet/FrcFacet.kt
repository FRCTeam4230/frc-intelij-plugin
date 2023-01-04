/*
 * Copyright 2015-2022 the original author or authors.
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

package net.javaru.iip.frc.facet

import com.google.common.collect.ImmutableList
import com.intellij.facet.Facet
import com.intellij.facet.FacetManager
import com.intellij.facet.FacetType
import com.intellij.facet.FacetTypeId
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.externalSystem.service.project.IdeModelsProvider
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProviderImpl
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ExternalProjectSystemRegistry
import net.javaru.iip.frc.facet.FrcFacet.Companion.FACET_TYPE_ID
import net.javaru.iip.frc.run.RunDebugConfigsCreationData
import net.javaru.iip.frc.run.createAllRunDebugConfigurations
import net.javaru.iip.frc.wpilib.gradlePluginRepo.logger
import org.jetbrains.annotations.Contract


class FrcFacet(facetType: FacetType<FrcFacet, FrcFacetConfiguration>,
               module: Module,
               name: String,
               configuration: FrcFacetConfiguration,
               underlyingFacet: Facet<*>?) : Facet<FrcFacetConfiguration>(facetType, module, name, configuration, underlyingFacet)
{
    companion object
    {
        internal val LOG = logger<FrcFacet>()

        private const val serialVersionUID: Long = 4400383714328255414L
        const val FACET_TYPE_ID_STRING = "FRC_FACET"
        val FACET_TYPE_ID = FacetTypeId<FrcFacet>(FACET_TYPE_ID_STRING)
        const val FACET_NAME = "FRC"
        //const val FULL_NAME = "FRC (FIRST Robotics Competition)"


        fun getInstance(module: Module): FrcFacet? = FacetManager.getInstance(module).getFacetByType(FACET_TYPE_ID)
    }
}

@Suppress("unused")
fun Module.getOrAddFrcFacet(externalSystemId: String? = null, commitModel: Boolean = true): FrcFacet
{
    val modifiableModelsProvider = IdeModifiableModelsProviderImpl(this.project)
    return getOrAddFrcFacetImpl(externalSystemId, modifiableModelsProvider, commitModel)
}

fun Module.getOrAddFrcFacet(externalSystemId: String? = null, modelsProvider: IdeModelsProvider, commitModel: Boolean = true): FrcFacet
{
    return if (modelsProvider is IdeModifiableModelsProvider)
    {
        getOrAddFrcFacetImpl(externalSystemId, modelsProvider, commitModel)
    }
    else
    {
        val modifiableModelsProvider = IdeModifiableModelsProviderImpl(this.project)
        getOrAddFrcFacetImpl(externalSystemId, modifiableModelsProvider, commitModel)
    }
}

@Suppress("unused")
fun Module.getOrAddFrcFacet(externalSystemId: String? = null, modifiableModelsProvider: IdeModifiableModelsProvider, commitModel: Boolean = true): FrcFacet =
    getOrAddFrcFacetImpl(externalSystemId, modifiableModelsProvider, commitModel)

private fun Module.getOrAddFrcFacetImpl(externalSystemId: String? = null,
                                        modelsProvider: IdeModifiableModelsProvider,
                                        commitModel: Boolean = true,
                                        isReattempt:Boolean = false
                                       ): FrcFacet
{
    val facetManager = FacetManager.getInstance(this)
    val frcFacet = facetManager.getFacetByType(FACET_TYPE_ID)
    if (frcFacet != null)
    {
        return frcFacet
    }

    // Based on Kotlin Plugin:  org.jetbrains.kotlin.idea.facet.FacetUtilsKt#getOrCreateFacet
    val facetModel = modelsProvider.getModifiableFacetModel(this)
    val facet = facetModel.findFacet(FACET_TYPE_ID, FrcFacetType.INSTANCE.defaultFacetName) ?: with(FrcFacetType.INSTANCE) {
        createFacet (this@getOrAddFrcFacetImpl, defaultFacetName, createDefaultConfiguration(), null)
    }.apply {
        val externalSource = externalSystemId?.let { ExternalProjectSystemRegistry.getInstance().getSourceById(it) }
        try
        {
            facetModel.addFacet(this, externalSource)
        }
        catch (e: com.intellij.workspaceModel.storage.impl.exceptions.SymbolicIdAlreadyExistsException)
        {
            // This was previously 'PersistentIdAlreadyExistsException', changed in v 2022.3
            if (!isReattempt)
            {
                this@getOrAddFrcFacetImpl.getOrAddFrcFacetImpl(externalSystemId, modelsProvider, commitModel, isReattempt = true)
            }
            else
            {
                logger.warn("[FRC] SymbolicIdAlreadyExistsException occurred on second attempt to add FrcFacet.", e)
            }
        }
    }

    if (commitModel) {
        runWriteAction {
            if (!this@getOrAddFrcFacetImpl.isDisposed) {
                facetModel.commit()
            }
        }
    }

    try {
        createAllRunDebugConfigurations(RunDebugConfigsCreationData.create(project))
    }
    catch (t: Throwable) {
        if (t is ProcessCanceledException)
            throw t
        else
            FrcFacet.LOG.info("[FRC] Could not create Run/Debu configurations when adding facet. Cause Summary: $t", t)
    }

    return facet
}

@Suppress("unused")
val allFrcFacetsForAllOpenProjects: ImmutableList<FrcFacet>
    get()
    {
        val openProjects = ProjectManager.getInstance().openProjects

        val listBuilder = ImmutableList.builder<FrcFacet>()

        for (openProject in openProjects)
        {
            if (!openProject.isDisposed)
            {
                listBuilder.addAll(openProject.getAllFrcFacetsForProject())
            }
        }
        return listBuilder.build()
    }

fun Project?.getAllFrcFacetsForProject(): ImmutableList<FrcFacet>
{
    if (this == null) return ImmutableList.of()

    val listBuilder = ImmutableList.builder<FrcFacet>()
    val modules = ModuleManager.getInstance(this).modules
    for (module in modules)
    {
        if (!module.isDisposed)
        {
            val frcFacets = FacetManager.getInstance(module).getFacetsByType(FACET_TYPE_ID)
            listBuilder.addAll(frcFacets)
        }
    }
    return listBuilder.build()
}

/** Returns all the modules in a project that have an `FrcFacet` attached to them. If the project is null, an empty list is returned.  */
@Suppress("unused")
fun Project?.getFrcFacetedModules(): List<Module>
{
    return if (this == null) emptyList()
    else ModuleManager.getInstance(this).modules.filter { it.isFrcFacetedModule() }
}

fun Facet<*>?.isFrcFacet(): Boolean = this is FrcFacet

@Contract("null -> false")
fun Module?.isFrcFacetedModule(): Boolean
{
    if (this == null || this.isDisposed)
    {
        return false
    }
    val frcFacet = FacetManager.getInstance(this).getFacetByType(FACET_TYPE_ID)
    return frcFacet != null
}

@Contract("null -> false")
fun Project?.isFrcFacetedProject(): Boolean
{
    if (this != null && !this.isDisposed)
    {
        val modules = ModuleManager.getInstance(this).modules
        for (module in modules)
        {
            if (module.isFrcFacetedModule())
            {
                return true
            }
        }
    }
    return false
}