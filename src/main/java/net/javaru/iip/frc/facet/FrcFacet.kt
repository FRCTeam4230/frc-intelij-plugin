/*
 * Copyright 2015-2023 the original author or authors.
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
import com.intellij.facet.ModifiableFacetModel
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.externalSystem.service.project.IdeModelsProvider
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProviderImpl
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ExternalProjectSystemRegistry
import com.intellij.openapi.roots.ProjectModelExternalSource
import com.intellij.openapi.util.ThrowableComputable
import io.sentry.Attachment
import net.javaru.iip.frc.facet.FrcFacet.Companion.FACET_TYPE_ID
import net.javaru.iip.frc.services.FrcErrorReportSubmitter
import net.javaru.iip.frc.services.ReportableEvent
import net.javaru.iip.frc.wpilib.gradlePluginRepo.logger
import org.jetbrains.annotations.Contract
import java.io.ByteArrayOutputStream
import java.io.PrintWriter


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
    return getOrAddFrcFacetImpl(this, externalSystemId, modifiableModelsProvider, commitModel)
}

fun Module.getOrAddFrcFacet(externalSystemId: String? = null, modelsProvider: IdeModelsProvider, commitModel: Boolean = true): FrcFacet
{
    return if (modelsProvider is IdeModifiableModelsProvider)
    {
        getOrAddFrcFacetImpl(this, externalSystemId, modelsProvider, commitModel)
    }
    else
    {
        val modifiableModelsProvider = IdeModifiableModelsProviderImpl(this.project)
        getOrAddFrcFacetImpl(this, externalSystemId, modifiableModelsProvider, commitModel)
    }
}

@Suppress("unused")
fun Module.getOrAddFrcFacet(externalSystemId: String? = null, modifiableModelsProvider: IdeModifiableModelsProvider, commitModel: Boolean = true): FrcFacet =
    getOrAddFrcFacetImpl(this, externalSystemId, modifiableModelsProvider, commitModel)

private fun getOrAddFrcFacetImpl(
    module: Module,
    externalSystemId: String? = null,
    modelsProvider: IdeModifiableModelsProvider,
    commitModel: Boolean = true,
                                ): FrcFacet
{
    val facetModel = modelsProvider.getModifiableFacetModel(module)
    val frcFacet = FacetManager.getInstance(module).getFacetByType(FACET_TYPE_ID) ?:
        facetModel.findFacet(FACET_TYPE_ID, FrcFacetType.INSTANCE.defaultFacetName) ?: with(FrcFacetType.INSTANCE){
        logger.debug("[FRC] FrcFacet not found, creating and adding it. ")
        val createdFacet = this.createFacet(module, defaultFacetName, createDefaultConfiguration(), null)
        val externalSource = externalSystemId?.let { ExternalProjectSystemRegistry.getInstance().getSourceById(it) }
        val addResult1 = addFrcFacetViaExternalModelAPI(createdFacet, facetModel, externalSource)
        if (addResult1.error != null)
        {
            val addResult2 = addFrcFacetViaExternalModelAPI(createdFacet, facetModel, externalSource)
            if (addResult2.error != null)
            {
                // This fall back to the basic API is mostly in the vent the new/experimental API changes
                val addResult3  = addFrcFacetViaBasicAPI(createdFacet, facetModel)
                if (addResult3.error != null)
                {
                    logger.warn("[FRC] Could not add FrcFacet to module. Attempt 1 Cause: ${addResult1.error}; Attempt 2 Cause: ${addResult2.error}; Attempt 3 Cause: ${addResult3.error}")
                    val ex = IllegalStateException("Could not add FrcFacet to module").apply {
                        this.addSuppressed(addResult1.error)
                        this.addSuppressed(addResult2.error)
                        this.addSuppressed(addResult3.error)
                    }
                    val attachment = ByteArrayOutputStream().use {
                        PrintWriter(it).use { pw: PrintWriter ->
                            pw.println("Result from first call to new/experimental API:")
                            addResult1.error.printStackTrace(pw)
                            pw.println("Result from second call to new/experimental API:")
                            addResult2.error.printStackTrace(pw)
                            pw.println("Result from second call to old/basic API:")
                            addResult3.error.printStackTrace(pw)
                        }
                        Attachment(it.toByteArray(), "ModuleAddStackTraces.txt")
                    }
                    FrcErrorReportSubmitter.submitReportableEvent(
                        ReportableEvent(
                            "FrcFacet.getOrAddFrcFacet",
                            project = module.project,
                            messageOrAdditionalInfo = "Automated report for error in FrcFacet.getOrAddFrcFacet. See Attachment for stacktraces",
                            throwable = ex,
                            attachments = listOf(attachment)
                                       )
                                                                 )
                    throw ex

                }
            }
        }
        createdFacet
    }

    if (commitModel)
    {
        runWriteAction {
            if (!module.isDisposed)
            {
                facetModel.commit()
            }
        }
    }

    return frcFacet
}

private fun addFrcFacetViaExternalModelAPI(
    frcFacet: FrcFacet,
    facetModel: ModifiableFacetModel,
    externalSource: ProjectModelExternalSource?,
                                          ): AddFrcFacetResult
{
    val result = WriteAction.compute(ThrowableComputable {
        try
        {
            facetModel.addFacet(frcFacet, externalSource)
            AddFrcFacetResult(frcFacet, null)
        }
        catch (t: Throwable)
        {
            logger.warn("[FRC] Could not add FrcFacet to module via new API. Cause Summary: $t", t)
            AddFrcFacetResult(frcFacet, t)
        }
    })
    return result
}

private fun addFrcFacetViaBasicAPI(
    frcFacet: FrcFacet,
    facetModel: ModifiableFacetModel,
                                  ): AddFrcFacetResult
{
    val result = WriteAction.compute(ThrowableComputable {
        try
        {
            facetModel.addFacet(frcFacet)
            AddFrcFacetResult(frcFacet, null)
        }
        catch (t: Throwable)
        {
            logger.warn("[FRC] Could not add FrcFacet to module via new API. Cause Summary: $t", t)
            AddFrcFacetResult(frcFacet, t)
        }
    })
    return result
}


private data class AddFrcFacetResult(val frcFacet: FrcFacet?, val error: Throwable?)

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