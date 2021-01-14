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

package net.javaru.iip.frc.wizard

import com.intellij.ide.SaveAndSyncHandler
import com.intellij.ide.actions.ImportModuleAction
import com.intellij.ide.util.projectWizard.JavaModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleBuilderListener
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SdkSettingsStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.project.ProjectId
import com.intellij.openapi.module.JavaModuleType
import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.StdModuleTypes
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.projectRoots.SdkTypeId
import com.intellij.openapi.projectRoots.impl.JavaSdkImpl
import com.intellij.openapi.projectRoots.impl.ProjectJdkImpl
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.util.Condition
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.projectImport.ProjectImportProvider
import com.intellij.util.containers.ContainerUtil
import com.intellij.util.containers.stream
import com.intellij.util.io.HttpRequests
import freemarker.template.Template
import icons.FrcIcons.FRC
import net.javaru.iip.frc.FrcPluginGlobals.DEFAULT_MIN_REQUIRED_JAVA_VERSION
import net.javaru.iip.frc.freemarker.FM_TEMPLATE_EXT_WITH_DOT
import net.javaru.iip.frc.freemarker.freemarkerConfiguration
import net.javaru.iip.frc.i18n.FrcBundle.message
import net.javaru.iip.frc.run.createDebuggingRunConfiguration
import net.javaru.iip.frc.run.createGradleRunConfiguration
import net.javaru.iip.frc.settings.FrcApplicationSettings
import net.javaru.iip.frc.settings.RoboRioAddressType
import net.javaru.iip.frc.util.asPluginResourceUrl
import net.javaru.iip.frc.util.asPluginResourceVF
import net.javaru.iip.frc.util.get
import net.javaru.iip.frc.util.getPluginResourceAsStream
import net.javaru.iip.frc.util.invokeLater
import net.javaru.iip.frc.util.isValidJavaVersion
import net.javaru.iip.frc.util.isValidJdk
import net.javaru.iip.frc.util.reader
import net.javaru.iip.frc.util.removeBasePath
import net.javaru.iip.frc.util.runBackgroundTask
import net.javaru.iip.frc.util.toCommaDelimitedString
import net.javaru.iip.frc.wpilib.version.WpiLibVersion
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import javax.swing.Icon
import kotlin.NoSuchElementException

class FrcModuleBuilder : JavaModuleBuilder(), ModuleBuilderListener
{
    private val mySdkChangedListeners: MutableList<Runnable> = ContainerUtil.createLockFreeCopyOnWriteList()
    private val fmConfiguration = freemarkerConfiguration(this, "/")
    /** 
     * Tracks the configured SDK since the `myJdk` property (in the super class `ModuleBuilder`) and the value in `WizardContext.getProjectJdk()` 
     * is not set until we pass the initial step. 
     * We need to certain to keep this updated based on activities. A null value indicates not only that an SDK has not been selected,
     * but more likely a valid one (Type * Version) is not available in the listing.
     */
    var selectedSdk: Sdk? = null
        set(sdk) 
        {
            LOG.trace("[FRC] selectedSdk setter called with value of '$sdk'  Previous value was '$field'")
            if (field != sdk)
            {
                field = sdk
                for (runnable in mySdkChangedListeners)
                {
                    runnable.run()
                }
            }
        }
    
    private var myWizardContext: WizardContext? = null
    private var myParentProject: ProjectData? = null
//    private val myInheritGroupId = false
//    private val myInheritVersion = false
    private var myProjectId: ProjectId? = null
    private var rootProjectPath: String? = null
    
    
//    private val myUseKotlinDSL = false
//    private val myShowGradleConfig = true
    
    val dataModel = FrcProjectWizardData()

    // TODO: Can we make this an interface and an ExtensionPoint?
    @Suppress("MemberVisibilityCanBePrivate")
    class TemplatePaths(val version: WpiLibVersion)
    {
        /** The base templates directory. For example `frc-wizard-templates/2020` for FRC year 2020.
         * In the event the directory of a year is not present, it will fallback to the latest year available. */
        val frcWizardTemplatesBaseDirPath: Path

        /** The `frc-wizard-templates` base templates directory as identified as  by the constant [frcWizardTemplatesDirName]. */
        val frcWizardTemplatesBaseDirVf: VirtualFile

        init
        {
            val frcWizardTemplatesDirPath = Paths.get(frcWizardTemplatesDirName)
            val frcWizardTemplatesDirVf = frcWizardTemplatesDirPath.asPluginResourceVF()

            if (frcWizardTemplatesDirVf == null)
            {
                val msg = "Cannot find FRC Wizard Templates Base Dir '$frcWizardTemplatesDirName' as plugin resource."
                LOG.warn("[FRC] $msg")
                throw ConfigurationException(msg)
            }


            var baseDirForWpiLibVersionPath = frcWizardTemplatesDirPath.resolve(version.frcYear.toString())
            var baseDirForWpiLibVersionVf = baseDirForWpiLibVersionPath.asPluginResourceVF()

            @Suppress("ThrowableNotThrown")
            if (baseDirForWpiLibVersionVf == null)
            {
                LOG.warn("[FRC] Cannot find FRC Wizard Templates Base Dir as resource for year ${version.frcYear}. Will use templates from latest available year.")
                val lastAvailableYear =
                    frcWizardTemplatesDirVf
                        .children
                        .stream()
                        .filter { it.isDirectory }
                        .map { it.name.toIntOrNull() }
                        .filter { it != null }
                        .mapToInt { it!! }
                        .max()
                        .orElseThrow { NoSuchElementException("No FRC child FRC year directories found in the FRC Wizard Templates directory '$frcWizardTemplatesDirName'") }

                baseDirForWpiLibVersionPath = frcWizardTemplatesDirPath.resolve(lastAvailableYear.toString())
                baseDirForWpiLibVersionVf = baseDirForWpiLibVersionPath.asPluginResourceVF()
            }

            if (baseDirForWpiLibVersionVf == null)
            {
                val msg = "Cannot find FRC Wizard Templates Base Dir (as plugin resource) for WPI Lib version '$version', nor can a template directory for any previous versions be found."
                LOG.warn("[FRC] $msg")
                throw ConfigurationException(msg)
            }

            frcWizardTemplatesBaseDirPath = baseDirForWpiLibVersionPath
            frcWizardTemplatesBaseDirVf = baseDirForWpiLibVersionVf
        }

        val defaultFilesResourceBasePath: Path = frcWizardTemplatesBaseDirPath.resolve(defaultFilesDirName)
        val gradleGroovyDslResourceBasePath: Path = defaultFilesResourceBasePath.resolve(gradleGroovyDslSubPath)
        val gradleKotlinDslResourceBasePath: Path = defaultFilesResourceBasePath.resolve(gradleKotlinDslSubPath)
        val gradleWrapperResourceBasePath: Path = defaultFilesResourceBasePath.resolve(gradleWrapperSubPath)
        val configsResourceBasePath: Path = defaultFilesResourceBasePath.resolve(configsSubPath)
        val extrasResourceBasePath: Path = defaultFilesResourceBasePath.resolve(extrasSubPath)
        val vsCodeConfigsResourceBasePath: Path = extrasResourceBasePath.resolve(vsCodeConfigsSubPath)
        val commonCodeResourceBasePath: Path = defaultFilesResourceBasePath.resolve(commonCodeSubPath)
        val javaCodeResourceBasePath: Path = defaultFilesResourceBasePath.resolve(javaCodeSubPath)
        val kotlinCodeResourceBasePath: Path = defaultFilesResourceBasePath.resolve(kotlinCodeSubPath)

        companion object
        {
            const val frcWizardTemplatesDirName = "frc-wizard-templates"
            const val defaultFilesDirName = "default-files"

            val gradleGroovyDslSubPath: Path = Paths.get("gradle/groovy-dsl")
            val gradleKotlinDslSubPath: Path = Paths.get("gradle/kotlin-dsl")
            val gradleWrapperSubPath: Path = Paths.get("gradle/gradle-wrapper")
            val configsSubPath: Path = Paths.get("configs")
            val extrasSubPath: Path = Paths.get("extras")
            val vsCodeConfigsSubPath: Path = Paths.get("vs-code-configs")
            val commonCodeSubPath: Path = Paths.get("code/common-code")
            val javaCodeSubPath: Path = Paths.get("code/java-code")
            val kotlinCodeSubPath: Path = Paths.get("code/kotlin-code")
        }
    }
    
    

    
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
    override fun getModuleType(): ModuleType<*>? = StdModuleTypes.JAVA
    override fun getBuilderId(): String? = javaClass.name // This is critical since the default is to return the ModuleType's ID, which then results in the Java Wizard steps being used as our createWizardSteps() is never called.

    override fun createProject(name: String?, path: String?): Project?
    {
        LOG.trace("[FRC] FrcModuleBuilder.createProject() called with name: $name and path: $path")
        val project = super.createProject(name, path)
        LOG.trace("[FRC] FrcModuleBuilder.createProject() completed. project: $project")
        return project
    }


    override fun moduleCreated(module: Module)
    {
        // This method is from the ModuleBuilderListener
        LOG.trace("[FRC] FrcModuleBuilder.moduleCreated() called and completed for module: ${module.name} at ${module.moduleFilePath}")
        // Module Configuration work could be done here
    }

    override fun setupModule(module: Module?)
    {
        // This implementation is heavily based on the impl in GradleModelBuilder, along with a bit from  the KtorModuleBuilder impl in the JetBrains ktor plugin
        LOG.trace("[FRC] FrcModuleBuilder.setupModule() called for module: ${module?.name} at ${module?.moduleFilePath}")
        super.setupModule(module) // this will call (our overridden) setupRootModel method
        LOG.trace("[FRC] FrcModuleBuilder.setupModule() completed for module: ${module?.name} at ${module?.moduleFilePath}")
    }


    @Throws(ConfigurationException::class)
    override fun setupRootModel(rootModel: ModifiableRootModel)
    {
        // This implementation is heavily based on the impl in AbstractGradleModuleBuilder (v2019.3+, was previously GradleModelBuilder), along with a bit from the KtorModuleBuilder impl in the JetBrains ktor plugin
        // This method gets called by the setupModule method
        LOG.debug("[FRC] FrcModuleBuilder.setupRootModel() called with template: '${dataModel.frcWizardTemplateDefinition.displayName}' from dir '${dataModel.frcWizardTemplateDefinition.templateResourcesDirName()}'")
        LOG.trace("[FRC] Data Model: $dataModel")

        val modelContentRootDir = createAndGetRoot() ?: return
        val project = rootModel.project
        //val module = rootModel.module
        rootModel.addContentEntry(modelContentRootDir)
        if (myJdk != null) rootModel.sdk = myJdk else rootModel.inheritSdk()

        project.runBackgroundTask("Setting Up FRC Project") { progress ->
            progress.text = "Processing templates"
            rootProjectPath = myParentProject?.linkedExternalProjectPath ?: FileUtil.toCanonicalPath(if (myWizardContext!!.isCreatingNewProject) project.basePath else modelContentRootDir.path)
            assert(rootProjectPath != null) { "rootProjectPath is null" }

            copyTemplateFilesToProject(modelContentRootDir)

            progress.text = "Configuring project model"
            modelContentRootDir.refresh(false, true)

            if (FrcApplicationSettings.getInstance().enableGradleImportUponNewProjectCreation)
            {
                importGradleProject(modelContentRootDir, project)
            }

            progress.text = "Done."
        }

        StartupManager.getInstance(project).runWhenProjectIsInitialized {
            createRunConfigurations(project)
        }

        LOG.trace("[FRC] FrcModuleBuilder.setupRootModel() completed")
    }


    override fun commitModule(project: Project, model: ModifiableModuleModel?): Module?
    {
        LOG.trace("[FRC] FrcModuleBuilder.moduleCreated() called for project: ${project.name} for moduleModel $model")
        val module = super.commitModule(project, model)
        LOG.trace("[FRC] FrcModuleBuilder.moduleCreated() completed for project: ${project.name} for moduleModel $model")
        return module
    }


    private fun copyTemplateFilesToProject(modelContentRootDir: VirtualFile)
    {
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

        val wpilibVersion = dataModel.wpilibVersion
        //TODO Need to enhance the calls to the defaults check if the template has overridden any of the default files
        val paths = TemplatePaths(wpilibVersion)

        paths.frcWizardTemplatesBaseDirPath.asPluginResourceVF()?.refresh(false, true)
        copyAllResourcesToModuleRoot(modelContentRootDir, paths.gradleGroovyDslResourceBasePath)
        copyAllResourcesToModuleRoot(modelContentRootDir, paths.gradleWrapperResourceBasePath)


        val wpilibCommandsJsonFilter: (VirtualFile) -> Boolean =
            when (dataModel.frcWizardTemplateDefinition.commandVersion)
            {
                1    -> { virtualFile -> !virtualFile.name.contains("WPILibNewCommands") }    // reject New so we keep Old
                2    -> { virtualFile -> !virtualFile.name.contains("WPILibOldCommands") }     // reject Old so we keep New
                else -> { virtualFile -> !virtualFile.name.contains("WPILibNewCommands") && !virtualFile.name.contains("WPILibOldCommands") } // reject both
            }
        copyAllResourcesToModuleRoot(modelContentRootDir, paths.configsResourceBasePath, wpilibCommandsJsonFilter)

        copyAllResourcesToModuleRoot(modelContentRootDir, paths.commonCodeResourceBasePath)
        copyAllResourcesToModuleRoot(modelContentRootDir, paths.javaCodeResourceBasePath)
        if (dataModel.includeVsCodeConfigs)
        {
            copyAllResourcesToModuleRoot(modelContentRootDir, paths.vsCodeConfigsResourceBasePath)
        }

        if (dataModel.gitIgnoreConfiguration.includeGitIgnoreFile)
        {
            try
            {
                val gitIgnoreContent = generateGitIgnoreFileContent(dataModel.gitIgnoreConfiguration)
                val file = Paths.get(modelContentRootDir.path).resolve(".gitignore")
                Files.newBufferedWriter(file, Charsets.UTF_8).use { it.write(gitIgnoreContent) }
            } catch (e: Exception)
            {
                LOG.warn("[FRC] Unable to create .gitignore file due to an exception: $e", e)
            }
        }

        val selectedTemplateResourceBase = paths.frcWizardTemplatesBaseDirPath.resolve(dataModel.frcWizardTemplateDefinition.templateResourcesDirName()).resolve(TemplatePaths.javaCodeSubPath)
        copyAllResourcesToModuleRoot(modelContentRootDir, selectedTemplateResourceBase)
    }


    private fun importGradleProject(modelContentRootDir: VirtualFile, project: Project)
    {
        // Primarily copied from KtorModuleBuilder in Ktor plugin
        val buildGradle = modelContentRootDir["build.gradle.kts"] ?: modelContentRootDir["build.gradle"]
        if (buildGradle != null)
        {
            LOG.trace("[FRC] preparing for gradle import")
            invokeLater {
                LOG.trace("[FRC] gradle import lambda starting")
                val provider = ProjectImportProvider.PROJECT_IMPORT_PROVIDER.extensions
                    .firstOrNull { it.canImport(buildGradle, project) }
                    ?: return@invokeLater

                val wizard = ImportModuleAction.createImportWizard(project, null, buildGradle, provider)

                if (wizard != null && (wizard.stepCount <= 0 || wizard.showAndGet()))
                {
                    ImportModuleAction.createFromWizard(project, wizard)
                }
                LOG.trace("[FRC] gradle import lambda finished")
            }
            LOG.trace("[FRC] gradle import prep completed")
        } else
        {
            LOG.info("[FRC] gradle import not executed as build.gradle/build.gradle.kts was not found ")
        }
    }


    private fun createRunConfigurations(project: Project)
    {
        LOG.trace("[FRC] Creating run configurations")
        val debugModeArgument = "-PdebugMode=true"
        createGradleRunConfiguration(project, message("frc.wizard.run.configuration.buildAndDeploy.name"), listOf("deploy"), setAsSelected = true)
        createGradleRunConfiguration(project, message("frc.wizard.run.configuration.buildAndDeployForDebug.name"), listOf("deploy"), arguments = debugModeArgument)
        createGradleRunConfiguration(project, message("frc.wizard.run.configuration.build.name"), listOf("build"))
        createGradleRunConfiguration(project, message("frc.wizard.run.configuration.cleanBuildAndDeploy.name"), listOf("clean", "deploy"))
        createGradleRunConfiguration(project, message("frc.wizard.run.configuration.cleanBuildAndDeployForDebug.name"), listOf("clean", "deploy"), arguments = debugModeArgument)
        createGradleRunConfiguration(project, message("frc.wizard.run.configuration.cleanBuild.name"), listOf("clean", "build"))
        createDebuggingRunConfiguration(project, dataModel.teamNumber, RoboRioAddressType.IP)
        createDebuggingRunConfiguration(project, dataModel.teamNumber, RoboRioAddressType.USB)
        // We need to do a Save here or the run config files are not created, which then causes all sorts of issues (to say the least)
        //TODO: Look at switching to SaveAndSyncHandler.getInstance().scheduleSave(task: SaveTask, forceExecuteImmediately: Boolean) once it is no longer marked internal. See SaveAllAction for use example
        SaveAndSyncHandler.getInstance().scheduleProjectSave(project)
        LOG.trace("[FRC] Completed run configurations")
    }

    private fun copyAllResourcesToModuleRoot(modelContentRootDir: VirtualFile, resourceDirBase: Path, keepFilter: (VirtualFile) -> Boolean = { true })
    {
        val pluginResourceDirUrl = resourceDirBase.asPluginResourceUrl()
        LOG.debug("[FRC] pluginResourceDir URL = $pluginResourceDirUrl")

        if (pluginResourceDirUrl == null)
        {
            LOG.warn("[FRC] Could  not find resourceDir '$resourceDirBase' for 'copy all template files' operation.")
        }
        else
        {
            val srcFqBaseDir = VfsUtil.findFileByURL(pluginResourceDirUrl)
            if (srcFqBaseDir == null)
            {
                LOG.debug("[FRC] Could not convert URL '$pluginResourceDirUrl' to a VirtualFile for 'copy all wizard template files' operation.")
            }
            else
            {
                VfsUtil.collectChildrenRecursively(srcFqBaseDir)
                    .filter { !it.isDirectory }
                    .filter { keepFilter.invoke(it) }
                    .forEach {
                    val resourceRelativePath = Paths.get(it.toString().removePrefix("$srcFqBaseDir")).removeBasePath(Paths.get("/"))
                    // We refresh to resolve an issue where the source file is cached by IntelliJ IDEA, even after a restart, and we get the old content
                    // We probably only need to do this when testing since a new plugin release would have a new jar file name... and IntelliJ *should*
                    // see that as a new file as its full path would be different. But since this is in the new project wizard, and thus is only used
                    // occasionally, the slight performance hit is worth the complete assurance of using the most recent file
                    it.refresh(false, true)
                    if (resourceRelativePath.fileName.toString().endsWith(FM_TEMPLATE_EXT_WITH_DOT))
                        copyFreemarkerTemplate(modelContentRootDir, it, srcFqBaseDir)
                    else
                        copyNonTemplateFile(modelContentRootDir, it, srcFqBaseDir)
                }
            }
        }
    }

    
    private fun copyNonTemplateFile(modelContentRootDir: VirtualFile, srcFqVf: VirtualFile, srcFqBaseDir: VirtualFile): VirtualFile?
    {
        return try
        {
            val endPath = Paths.get(srcFqVf.toString().removePrefix("$srcFqBaseDir")).removeBasePath(Paths.get("/"))
            val target = resolveTargetPath(modelContentRootDir, endPath)
            Files.createDirectories(target.parent)
            val file = target.toFile()

            FileUtils.copyInputStreamToFile(srcFqVf.inputStream, file)
            LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
        } catch (e: Exception)
        {
            LOG.warn("[FRC] Could not copy new project wizard file '$srcFqVf' to new project root '${modelContentRootDir}' exception: $e", e)
            null
        }
    }
    
    
    private fun copyFreemarkerTemplate(modelContentRootDir: VirtualFile, srcFqVf: VirtualFile, srcFqBaseDir: VirtualFile)
    {
        try
        {
            srcFqBaseDir.refresh(false, false)
            val srcEndPath = Paths.get(srcFqVf.toString().removePrefix("$srcFqBaseDir")).removeBasePath(Paths.get("/"))
            val fmTemplateName = srcEndPath.fileName.toString()
            val targetEndPath = srcEndPath.resolveSibling(fmTemplateName.removeSuffix(FM_TEMPLATE_EXT_WITH_DOT))
            val target = resolveTargetPath(modelContentRootDir, targetEndPath)
            // name ==> The path of the template file relatively to the (virtual) directory that you use to store the templates
            val name = FilenameUtils.separatorsToUnix(srcEndPath.toString())!!
            val template = Template(name, srcFqVf.reader(), fmConfiguration)
            processFreemarkerTemplate(template, target)
        }
        catch (e: Exception)
        {
            LOG.warn("[FRC] Could not copy new project wizard template file '$srcFqVf' to new project root '${modelContentRootDir}' exception: $e", e)
        }
    }

    
    private fun processFreemarkerTemplate(fmTemplate: Template, target: Path): VirtualFile?
    {
        try
        {
            Files.createDirectories(target.parent)
            Files.newBufferedWriter(target, Charsets.UTF_8).use {
                val environment = fmTemplate.createProcessingEnvironment(hashMapOf("data" to dataModel), it)
                environment.outputEncoding = Charsets.UTF_8.toString()
                environment.locale = Locale.ENGLISH
                environment.process()
            }
            return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(target.toFile())
        }
        catch (e: Exception)
        {
            LOG.warn("'[FRC] Could not process new project wizard freemarker template '${fmTemplate.sourceName}' to destination '$target' due to the exception: $e", e)
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


    private fun generateGitIgnoreFileContent(config: GitIgnoreConfiguration): String
    {
        val (templates: MutableList<String>, manualEntries: StringBuilder) = extractGitIgnoreTemplates(config)
        val content = StringBuilder()
        
        if(templates.isNotEmpty())
        {
            var createFromCache = !config.generateFromSite
            if (!createFromCache)
            {
                try
                {
                    //val url = "https://gitignore.io/api/${templates.toCommaDelimitedString(false)}"
                    val url = "https://www.toptal.com/developers/gitignore/api/${templates.toCommaDelimitedString(false)}"
                    val request1 = HttpRequests.request(url)
                    val responseBody = request1.readString()
                    content.appendLine("# Dynamically generated by IntelliJ IDEA FRC Plugin on ${java.time.format.DateTimeFormatter.ofPattern("EEEE MMMM d, yyyy h:mm:ss a zzz").format(ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()))} using https://www.toptal.com/developers/gitignore/api")
                    content.appendLine(responseBody)
                }
                catch (e: Exception)
                {
                    // TODO: we need to notify the user that the gitignore was not generated from the site and therefore may be out of date: https://gitlab.com/Javaru/frc-intellij-idea-plugin/-/issues/72
                    LOG.warn("[FRC] An exception occurred when attempting to dynamically create .gitignore file from gitignore APT at https://www.toptal.com/developers/gitignore/api. Will generate from cache. Cause Summary: $e", e)
                    createFromCache = true
                }
            }

            if (createFromCache)
            {
                content.appendLine(generateGitIgnoreSiteContentFromCachedFiles(templates, config.additionalGitignoreTemplates))
            }
        }
        
        if (manualEntries.isNotBlank())
        {
            content.appendLine("# === Entries from IntelliJ IDEA FRC Plugin New FRC Project Wizard ===")
            content.appendLine()
            content.append(manualEntries)
            content.appendLine()
            content.appendLine()
            content.appendLine("# End entries from IntelliJ IDEA FRC Plugin")
            content.appendLine()
        }
        
        return content.toString()
    }

   
    private fun generateGitIgnoreSiteContentFromCachedFiles(templates: MutableList<String>, additionalTemplates: List<String>): String
    {
        val filteredTemplates = templates.filterNot { additionalTemplates.contains(it) }
        val templatesString = filteredTemplates.toCommaDelimitedString(false)
        val content = StringBuilder()

        content.appendLine()
        content.appendLine("# Created by https://www.toptal.com/developers/gitignore/api/$templatesString")
        content.appendLine("# Edit at https://www.toptal.com/developers/gitignore?templates=$templatesString")
        content.appendLine()
        filteredTemplates.sorted().forEach { templateName ->
            val path = "frc-wizard-gitignore/$templateName.txt"
            val inputStream = getPluginResourceAsStream(path)
            if (inputStream == null) {
                LOG.warn("[FRC] could not find gitignore cached template '$path' in plugin resources")
            }
            else {
                inputStream.use { innerStream ->
                    val reader = BufferedReader(InputStreamReader(innerStream, Charsets.UTF_8))
                    reader.lines().forEach { line: String? ->
                        content.appendLine(line)
                    }
                }
                content.appendLine()
            }
        }
        content.appendLine()
        content.appendLine("# End of https://www.toptal.com/developers/gitignore/api/$templatesString")
        content.appendLine()
        content.appendLine()
        
        return content.toString()
    }
    
    
    private fun extractGitIgnoreTemplates(config: GitIgnoreConfiguration): Pair<MutableList<String>, StringBuilder>
    {
        val manualEntries = StringBuilder()
        val templates = mutableListOf<String>()

        if (config.java) templates.add("java")
        if (config.gradle) templates.add("gradle")


        when (config.intellij)
        {
            IdeConfigOption.Share   -> templates.add("intellij+iml")
            IdeConfigOption.Ignore  -> templates.add("intellij+all")
            IdeConfigOption.NoEntry -> { /* Do nothing */ }
        }

        @Suppress("SpellCheckingInspection")
        when (config.vscode)
        {
            IdeConfigOption.Share   -> templates.add("visualstudiocode")
            IdeConfigOption.Ignore  -> manualEntries.append(
                    """
                        ### VisualStudioCode ###
                        # Ignores the whole .vscode folder 
                        .vscode/
                        """.trimIndent())
            IdeConfigOption.NoEntry -> { /* Do nothing */ }
        }

        //TODO add Eclipse and NetBeans ?

        if (config.linux) templates.add("linux")
        if (config.macOS) templates.add("macos")
        if (config.windows) templates.add("windows")
        if (config.cpp) templates.add("c++")

        templates.addAll(config.additionalGitignoreTemplates)
        return Pair(templates, manualEntries)
    }
    

    override fun createWizardSteps(wizardContext: WizardContext, modulesProvider: ModulesProvider): Array<ModuleWizardStep>
    {
        LOG.trace("[FRC] FrcModuleBuilder.createWizardSteps() called")
        myWizardContext = wizardContext
        // These are the steps that come after the initial "built-in" step. 
        // The FrcInitialCustomOptionsWizardStep shows as a pane in the initial built-in step
        LOG.trace("[FRC] FrcModuleBuilder.createWizardSteps() completed")
        return arrayOf(FrcTemplateSelectionWizardStep(this, wizardContext),
                       FrcProjectSettingsWizardStep(this, wizardContext))
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
    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep
    {
        // This *normally* determines the potential frameworks  that can be selected (like kotlin, groovy, Thymeleaf, Ruby, etc., etc., etc.
        //     Notice that when setProviders is called  "java" is set for the "preselected" parameter    In IDEA project: service/project/wizard/GradleFrameworksWizardStep.java:99 as well as  service/project/wizard/GradleFrameworksWizardStep.java:91 for the Kotlin DSL
        //     Others are dynamically loaded via extension point definitions as far as I can tell.
        // Normally this is where we would put the option to select Kotlin
        //     But since for us we are just adding something to build.gradle.ftl template and not anything more sophisticated (i.e. having to set things),
        //     and we want to auto add Kotlin if a Kotlin template is selected, we will do this in a later step via a simple check box
        //   
        // It looks like typically  these can be defined/configured via an extension is the plugin.xml
        //     For example with Gradle, there is:
        //           <frameworkSupport implementation="org.jetbrains.plugins.gradle.frameworkSupport.GradleGroovyFrameworkSupportProvider"/>
        //     in the gradle-groovy-integration.xml file.
        //     in turn that file is defined as an optional depends in the gradle-java-integration.xml file when defining "org.intellij.groovy" as an (optional) dependency
        //     this would allow other plugins to add frameworks for a project type. Note something we need to worry about
        // So..... with all that said, we are not going to do a traditional "FrameworksWizardStep" or even an "options step",
        //     but rather a fairly simple "show some information" step
        LOG.trace("[FRC] FrcModuleBuilder.getCustomOptionsStep() called")
        val step = FrcInitialCustomOptionsWizardStep(this, context)
        Disposer.register(parentDisposable, step)
        LOG.trace("[FRC] FrcModuleBuilder.getCustomOptionsStep() completed")
        return step
    }


    override fun modifyProjectTypeStep(settingsStep: SettingsStep): ModuleWizardStep
    {
        // Implementation based on to the following forum answer:
        //     https://intellij-support.jetbrains.com/hc/en-us/community/posts/360006464099-Valdating-slected-Project-SDK-version-on-the-first-wizard-page?page=1#community_comment_360000894059
        //     Please see:                   com.jetbrains.python.module.PythonModuleBuilder#modifyProjectTypeStep 
        //     and implement your logic in:  com.intellij.ide.util.projectWizard.SdkSettingsStep#onSdkSelected

        
        // The parent ModuleBuilder class also has the  `boolean isSuitableSdkType(SdkTypeId sdkType)`  method, but that is limited to the type, so no version info
        
        // We apply filters so that only JDKs that meet the required version level show in the "Project SDK" drop down list
        LOG.trace("[FRC] FrcModuleBuilder.modifyProjectTypeStep() Executing")
        return object : SdkSettingsStep(settingsStep, 
                                        this, 
                                        Condition { id: SdkTypeId -> JavaSdkImpl.getInstance() === id },
                                        Condition {  sdk:Sdk -> (sdk as ProjectJdkImpl).isValidJavaVersion(DEFAULT_MIN_REQUIRED_JAVA_VERSION)})
        {
            override fun onSdkSelected(sdk: Sdk?)
            {
                // this gets called when ever the selected SDK changes, including when the new project wizard is first opened (and FRC project is selected because it was last used)
                // So we don't want to pop up a (modal) dialog. But we can set an internal "selected SDK tracking" property and then use that value in the validate method
                // If no valid JDK is available in the list (after filtering), this method is simply NOT called. (i.e. it is not called with a null value)
                @Suppress("SpellCheckingInspection")
                LOG.debug("[FRC] onSdkSelected called with sdk: sdk='$sdk' [type='${sdk?.sdkType}' version='${sdk?.versionString}' name = '${sdk?.name}'  homePath = '${sdk?.homePath}' sdkModificator = '${sdk?.sdkModificator}'" )
                selectedSdk = sdk
            }
        }
    }

    
    fun addSdkChangedListener(runnable: Runnable?)
    {
        if (runnable != null)
        {
            mySdkChangedListeners.add(runnable)
        }
    }


    fun isSelectedSdkValid(): Boolean = selectedSdk.isValidJdk()


    
    companion object
    {
        private val LOG = Logger.getInstance(FrcModuleBuilder::class.java)
    }
}
