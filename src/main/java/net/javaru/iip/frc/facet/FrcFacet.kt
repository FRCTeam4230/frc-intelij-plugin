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

package net.javaru.iip.frc.facet

import com.google.common.collect.ImmutableList
import com.intellij.facet.Facet
import com.intellij.facet.FacetManager
import com.intellij.facet.FacetType
import com.intellij.facet.FacetTypeId
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.runWriteAction
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProviderImpl
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.roots.ExternalProjectSystemRegistry
import net.javaru.iip.frc.facet.FrcFacet.Companion.FACET_NAME
import net.javaru.iip.frc.facet.FrcFacet.Companion.FACET_TYPE_ID
import org.jetbrains.annotations.Contract


class FrcFacet(facetType: FacetType<FrcFacet, FrcFacetConfiguration>,
               module: Module,
               name: String,
               configuration: FrcFacetConfiguration,
               underlyingFacet: Facet<*>?) : Facet<FrcFacetConfiguration>(facetType, module, name, configuration, underlyingFacet)
{
    companion object
    {
        //private val LOG = Logger.getInstance(FrcFacet::class.java)

        const val FACET_TYPE_ID_STRING = "FRC_FACET"
        val FACET_TYPE_ID = FacetTypeId<FrcFacet>(FACET_TYPE_ID_STRING)
        const val FACET_NAME = "FRC"
        //const val FULL_NAME = "FRC (FIRST Robotics Competition)"


        fun getInstance(module: Module): FrcFacet? = FacetManager.getInstance(module).getFacetByType(FACET_TYPE_ID)
    }
}

fun Module.getOrAddFrcFacet(externalSystemId: String? = null, commitModel: Boolean = true): FrcFacet
{
    // Based on Kotlin Plugin:  org.jetbrains.kotlin.idea.facet.FacetUtilsKt#getOrCreateFacet 
    val modelsProvider = IdeModifiableModelsProviderImpl(this.project)
    val facetModel = modelsProvider.getModifiableFacetModel(this)
    val facet = facetModel.findFacet(FACET_TYPE_ID, FACET_NAME) ?: with(FrcFacetType.instance) {
        createFacet(this@getOrAddFrcFacet, FACET_NAME, createDefaultConfiguration(), null)
    }.apply {
        val externalSource = externalSystemId?.let{ ExternalProjectSystemRegistry.getInstance().getSourceById(it) }
        facetModel.addFacet(this, externalSource)
    }

    if (commitModel)
    {
        ApplicationManager.getApplication().invokeLater{
            runWriteAction {
                facetModel.commit()
            }
        }
    }
    return facet
}

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