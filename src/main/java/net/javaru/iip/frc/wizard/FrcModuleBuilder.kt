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

package net.javaru.iip.frc.wizard

import com.intellij.ide.util.projectWizard.JavaModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleBuilderListener
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.project.ProjectId
import com.intellij.openapi.module.JavaModuleType
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.util.Disposer
import net.javaru.iip.frc.FrcIcons.FRC
import javax.swing.Icon

class FrcModuleBuilder : JavaModuleBuilder(), ModuleBuilderListener
{
    private var myWizardContext: WizardContext? = null
    private var myParentProject: ProjectData? = null
    private val myInheritGroupId = false
    private val myInheritVersion = false
    private var myProjectId: ProjectId? = null
    private val rootProjectPath: String? = null
    private val myUseKotlinDSL = false
    val config = FrcModuleConfig()
    override fun getParentGroup(): String
    {
        return JavaModuleType.BUILD_TOOLS_GROUP
    }

    override fun getWeight(): Int
    {
        return BUILD_SYSTEM_WEIGHT
    }

    override fun getModuleType(): ModuleType<*>?
    {
        return FrcModuleType.getInstance() /*return StdModuleTypes.JAVA;*/
    }

    override fun moduleCreated(module: Module)
    {
        // This method is from the ModuleBuilderListener

        LOG.debug("[FRC] FrcModuleBuilder.moduleCreated() called with module: " + module.name + " at " + module.moduleFilePath)
        // Module Configuration work could be done here

    }

    override fun getPresentableName(): String
    {
        // The default in super is: return getModuleTypeName();
        // This is the name that appears (on the left) in the initial new project dialog where all the possible project types/options are shown  

        return "FRC Robot Project"
    }

    // The icon used (on the left) in the initial new project dialog where all the possible project types/options are shown
    override fun getNodeIcon(): Icon
    {
        return FRC.FIRST_ICON_MEDIUM_16
    }

    override fun getGroupName(): String
    {
        return MODULE_BUILDER_GROUP_NAME
    }

    @Throws(ConfigurationException::class)
    override fun setupRootModel(modifiableRootModel: ModifiableRootModel)
    {
        LOG.debug("[FRC] FrcModuleBuilder.setupRootModel() called")
        super.setupRootModel(modifiableRootModel)
    }

    override fun createWizardSteps(wizardContext: WizardContext, modulesProvider: ModulesProvider): Array<ModuleWizardStep>
    {
        myWizardContext = wizardContext
        return arrayOf(FrcModuleWizardStep(this, wizardContext))
    }

    fun setParentProject(parentProject: ProjectData?)
    {
        myParentProject = parentProject
    }

    var projectId: ProjectId?
        get() = myProjectId
        set(projectId)
        {
            myProjectId = projectId
        }

    /**
     * Custom UI to be shown on the first wizard page
     *
     * @param context
     * @param parentDisposable
     */
    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep?
    {
        //TODO: Write this 'getCustomOptionsStep' overridden method
        //     This determines the potential frameworks  that can be selected (like kotlin, groovy, Thymeleaf, Ruby, yada yada yada
        //     Notice that when setProviders is called  "java" is set for the "preselected" parameter    In IDEA project: service/project/wizard/GradleFrameworksWizardStep.java:99 as well as  service/project/wizard/GradleFrameworksWizardStep.java:91 for the Kotlin DSL
        //     Others are dynamically loaded via extension point definitions as far as I can tell.
        //     So this is likely where we will want to put Kotlin 
        //     It looks like these ultimately get defined/configured via an extension is the plugin.xml
        //     For example with Gradle, there is:
        //           <frameworkSupport implementation="org.jetbrains.plugins.gradle.frameworkSupport.GradleGroovyFrameworkSupportProvider"/>
        //     in the gradle-groovy-integration.xml file.
        //     in turn that file is defined as an optional depends in the gradle-java-integration.xml file when defining "org.intellij.groovy" as an (optional) dependency
        //     For Java, I would want it to be a required provider rather than an optional that is preselected. Not sure if I need to "add" it behind the scenes or not.

        LOG.trace("[FRC] FrcModuleBuilder.getCustomOptionsStep() called")
        /*return super.getCustomOptionsStep(context, parentDisposable);*/


        //final FrcFrameworksWizardStep step = new FrcFrameworksWizardStep(context, this, config);


        val step = FrcFrameworksBlankWizardStep()
        Disposer.register(parentDisposable, step)
        return step
    }

    companion object
    {
        private val LOG = Logger.getInstance(FrcModuleBuilder::class.java)
    }
}