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
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import net.javaru.iip.frc.FrcIcons.FRC
import net.javaru.iip.frc.freemarker.FM_TEMPLATE_EXT
import net.javaru.iip.frc.freemarker.freemarkerConfiguration
import net.javaru.iip.frc.util.getPluginResourceAsStream
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import javax.swing.Icon

class FrcModuleBuilder : JavaModuleBuilder(), ModuleBuilderListener
{
    private val defaultFilesResourceBase = Paths.get("frc-wizard-templates/default-files/")
    private val gradleGroovyDslSubPath = Paths.get("gradle/groovy-dsl")
    private val gradleGroovyDslResourceBase = defaultFilesResourceBase.resolve(gradleGroovyDslSubPath)
    private val gradleKotlinDslSubPath = Paths.get("gradle/kotlin-dsl")
    private val gradleKotlinDslResourceBase = defaultFilesResourceBase.resolve(gradleKotlinDslSubPath)
    private val gradleWrapperSubPath = Paths.get("gradle/gradle-wrapper")
    private val gradleWrapperResourceBase = defaultFilesResourceBase.resolve(gradleWrapperSubPath)
    private val configsSubPath = "configs"
    private val configsResourceBase = defaultFilesResourceBase.resolve(configsSubPath)
    private val commonCodeSubPath = Paths.get("code/common-code")
    private val commonCodeResourceBase = defaultFilesResourceBase.resolve(commonCodeSubPath)
    private val javaCodeSubPath = Paths.get("code/java-code")
    private val javaCodeResourceBase = defaultFilesResourceBase.resolve(javaCodeSubPath)    
    private val kotlinCodeSubPath = Paths.get("code/kotlin-code")
    private val kotlinCodeResourceBase = defaultFilesResourceBase.resolve(kotlinCodeSubPath)
    
    private val fmConfig = freemarkerConfiguration(this, "/")
    
    private var myWizardContext: WizardContext? = null
    private var myParentProject: ProjectData? = null
//    private val myInheritGroupId = false
//    private val myInheritVersion = false
    private var myProjectId: ProjectId? = null
    private var rootProjectPath: String? = null
    
    
    private val myUseKotlinDSL = false
    private val myIncludeVsCodeConfigs = true
    private val myShowGradleConfig = true;
    
    //TODO: this needs to be created from the wizard step form
    val dataModel = FrcProjectWizardData()
    
    override fun getGroupName(): String = MODULE_BUILDER_GROUP_NAME 
    override fun getParentGroup(): String = JavaModuleType.JAVA_GROUP // This is the top group in the New Project Wizard, and for now it makes sense to be part of it

    /**
     * This is the name that appears (on the left) in the initial new project dialog where all the possible project/module types/options are shown.
     * The default in super is: `return getModuleTypeName()`.
     */
    override fun getPresentableName(): String = "FRC Robot Project"

    // The icon used (on the left) in the initial new project dialog where all the possible project types/options are shown
    override fun getNodeIcon(): Icon = FRC.FIRST_ICON_MEDIUM_16
    
    /**
     * Value that determines where in the list (within the Group) the module shows.
     * Higher numbers appear at the top, lower numbers at the bottom.
     * The items in IntelliJ IDEA Community are from top to bottom `Java`, `JavaFX`, `Android`, `IntelliJ Platform Plugin` 
     * The items in IntelliJ IDEA Ultimate are from top to bottom: (Dependent upon plugins installed) `Java`, `flexmark-java extension`, `Java Enterprise`, `JBoss`, `Spring`, JavaFX`, `Android`, `IntelliJ Platform Plugin`
     * We'll use 0 and get placed aty the bottom which I think is more consistent in the long run.
     */
    override fun getWeight(): Int = 0
    override fun getModuleType(): ModuleType<*>? = FrcModuleType.getInstance() /* = StdModuleTypes.JAVA;*/
    
    
    override fun moduleCreated(module: Module)
    {
        // This method is from the ModuleBuilderListener
        LOG.trace("[FRC] FrcModuleBuilder.moduleCreated() called with module: " + module.name + " at " + module.moduleFilePath)
        // Module Configuration work could be done here

    }

    override fun setupModule(module: Module?)
    {
        // This implementation is heavily based on the impl in GradleModelBuilder, along with a bit from  the KtorModuleBuilder impl in the JetBrains ktor plugin
        LOG.trace("[FRC] FrcModuleBuilder.setupModule() called")
        super.setupModule(module) // this will call (our overridden) setupRootModel method
        
        //assert(rootProjectPath != null) { "project root path was null" }
        
    }

    @Throws(ConfigurationException::class)
    override fun setupRootModel(rootModel: ModifiableRootModel)
    {
        // This implementation is heavily based on the impl in AbstractGradleModuleBuilder (v2019.3+, was previously GradleModelBuilder), along with a bit from the KtorModuleBuilder impl in the JetBrains ktor plugin
        // This method gets called by the setupModule method
        LOG.trace("[FRC] FrcModuleBuilder.setupRootModel() called")

        /*
            We need to setup the following:
            A) The following are typically identical between templates
                1) Gradle 
                    - nice to have would be to add dependencies such as logging
                    - Files:
                        a) build.gradle     (or build.gradle.kts)       
                            - potentially modifiable
                            - will need to replace robot main class if we allow for alternate base package
                        b) settings.gradle  (or settings.gradle.kts)
                            - need to set frcYear (i.e. 2019, 2020, etc)
                            - set public folder?
                        c) gradlew
                        d) gradlew.bat
                        e) gradle/wrapper/gradle-wrapper.jar
                        f) gradle/wrapper/gradle-wrapper.properties
                2) .vscode
                    - this is a nice to have
                    - Files
                        a) .vscode/launch.json
                        b) .vscode/settings.json
                3) .wpilib
                    - Will have replacements for team number and project year
                    - Files:
                        a) wpilib_preferences.json
                    
            B) Template Specific files:
                1) src/main/deploy/example.txt
                    - Same across all projects
                2) Main.java
                    - typically does not change per project
                    - A future nice to have would be to allow for a different "Robot" class name which would require this to be
                3) Robot.java
                    - differs per template
                4) Other Java classes and packages
            
            
        */
        

        val modelContentRootDir = createAndGetRoot() ?: return
        rootModel.addContentEntry(modelContentRootDir)

        // This is a to  do comment in GradleModuleBuilder that this sdk work should be moved to generic ModuleBuilder
        if (myJdk != null) rootModel.sdk = myJdk else rootModel.inheritSdk()

        val project = rootModel.project

        rootProjectPath = if (myParentProject != null)
        {
            myParentProject!!.linkedExternalProjectPath
        }
        else
        {
            FileUtil.toCanonicalPath(if (myWizardContext!!.isCreatingNewProject) project.basePath else modelContentRootDir.path)
        }
        assert(rootProjectPath != null) { "rootProjectPath is null"}

        copyResourceToModuleRoot(modelContentRootDir, gradleGroovyDslResourceBase, Paths.get("build.gradle.ftl"))
        copyResourceToModuleRoot(modelContentRootDir, gradleGroovyDslResourceBase, Paths.get("settings.gradle.ftl"))
        copyResourceToModuleRoot(modelContentRootDir, gradleWrapperResourceBase, Paths.get("gradlew"))
        copyResourceToModuleRoot(modelContentRootDir, gradleWrapperResourceBase, Paths.get("gradlew.bat"))
        copyResourceToModuleRoot(modelContentRootDir, gradleWrapperResourceBase, Paths.get("gradle/wrapper/gradle-wrapper.jar"))
        copyResourceToModuleRoot(modelContentRootDir, gradleWrapperResourceBase, Paths.get("gradle/wrapper/gradle-wrapper.properties.ftl"))

        copyResourceToModuleRoot(modelContentRootDir, configsResourceBase, Paths.get(".wpilib/wpilib_preferences.json.ftl"))
        if (myIncludeVsCodeConfigs)
        {
            copyResourceToModuleRoot(modelContentRootDir, configsResourceBase, Paths.get(".vscode/launch.json.ftl"))
            copyResourceToModuleRoot(modelContentRootDir, configsResourceBase, Paths.get(".vscode/settings.json.ftl"))
        }

        copyResourceToModuleRoot(modelContentRootDir, commonCodeResourceBase, Paths.get("src/main/deploy/example.txt.ftl"))
        copyResourceToModuleRoot(modelContentRootDir, javaCodeResourceBase, Paths.get("src/main/java/base-package/Main.java.ftl"))
        
        modelContentRootDir.refresh(false, true)
        
        LOG.trace("FrcModuleBuilder.setupRootModel() completed")
    }


    private fun copyResourceToModuleRoot(modelContentRootDir: VirtualFile, resourceBase: Path, resourceRelativePath: Path): VirtualFile?
    {
        return if (resourceRelativePath.fileName.toString().endsWith(FM_TEMPLATE_EXT))
            doCopyFreemarkerTemplateToModuleRoot(modelContentRootDir, resourceBase, resourceRelativePath)
        else
            doCopyResourceToModuleRoot(modelContentRootDir, resourceBase, resourceRelativePath)
    }


    private fun doCopyResourceToModuleRoot(modelContentRootDir: VirtualFile, resourceBase: Path, resourceRelativePath: Path): VirtualFile?
    {
        val resourcePath = resourceBase.resolve(resourceRelativePath)
        try
        {
            val pluginResourceInputStream = getPluginResourceAsStream(resourcePath)
            if (pluginResourceInputStream == null)
            {
                LOG.warn("[FRC] Could not find resource '$resourcePath'")
                return null
            }

            val target = resolveTargetPath(modelContentRootDir, resourceRelativePath)
            Files.createDirectories(target.parent)
            val file = target.toFile()
            FileUtils.copyInputStreamToFile(pluginResourceInputStream, file)
            return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
        }
        catch (e: Exception)
        {
            LOG.warn("[FRC] Could not copy resource '$resourcePath' to the module root dir '$modelContentRootDir' due to an exception. Cause Summary: $e", e)
            return null
        }
    }

    private fun doCopyFreemarkerTemplateToModuleRoot(modelContentRootDir: VirtualFile, resourceBase: Path, templateRelativePath: Path): VirtualFile?
    {
        val templatePath = resourceBase.resolve(templateRelativePath)
        try
        {
            val targetRelativePath = templateRelativePath.resolveSibling(templatePath.fileName.toString().removeSuffix(FM_TEMPLATE_EXT))
            val target = resolveTargetPath(modelContentRootDir, targetRelativePath)
            val template = fmConfig.getTemplate(FilenameUtils.separatorsToUnix(templatePath.toString()))

            Files.createDirectories(target.parent)
            Files.newBufferedWriter(target, Charsets.UTF_8).use {
                val environment = template.createProcessingEnvironment(hashMapOf("data" to dataModel), it)
                environment.outputEncoding = Charsets.UTF_8.toString()
                environment.locale = Locale.ENGLISH
                environment.process()
            }
            return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(target.toFile())
        }
        catch(e: Exception)
        {
            LOG.warn("[FRC] Could not process resource template '$templatePath' for the module root dir '$modelContentRootDir' due to an exception. Cause Summary: $e", e)
            return null
        }
    }

    private fun resolveTargetPath(modelContentRootDir: VirtualFile, targetRelativePath: Path): Path
    {
        val target = VfsUtil.virtualToIoFile(modelContentRootDir).toPath().resolve(targetRelativePath)
        return normalizeTargetPathWithPackageDir(target)
    }

    private fun normalizeTargetPathWithPackageDir(target: Path): Path
    {
        val basePackage = "base-package/"
        val pathString = FilenameUtils.separatorsToUnix(target.toString())
        return if (pathString.contains(basePackage))
        {
            val normalizedPathString: String = if (dataModel.basePackage.isEmpty())
            {
                val start = pathString.indexOf(basePackage)
                val end = start + basePackage.length
                pathString.removeRange(start, end)
            }
            else
            {
                pathString.replace(basePackage, "${FilenameUtils.separatorsToUnix(dataModel.basePackageAsDirString)}/")
            }

            Paths.get(normalizedPathString)
        }
        else
        {
            target
        }
    }
    

    private fun createAndGetRoot(): VirtualFile?
    {
        val path = contentEntryPath?.let { FileUtil.toSystemIndependentName(it) } ?: return null
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(File(path).apply { mkdirs() }.absolutePath)
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
