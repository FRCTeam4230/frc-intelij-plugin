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
import com.intellij.ide.util.projectWizard.SdkSettingsStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.notification.Notification
import com.intellij.notification.NotificationsManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.externalSystem.model.project.ProjectData
import com.intellij.openapi.externalSystem.model.project.ProjectId
import com.intellij.openapi.module.JavaModuleType
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.options.ConfigurationException
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
import com.intellij.util.containers.ContainerUtil
import freemarker.template.Template
import net.javaru.iip.frc.FrcIcons.FRC
import net.javaru.iip.frc.FrcPluginGlobals.DEFAULT_MIN_REQUIRED_JAVA_VERSION
import net.javaru.iip.frc.freemarker.FM_TEMPLATE_EXT_WITH_DOT
import net.javaru.iip.frc.freemarker.freemarkerConfiguration
import net.javaru.iip.frc.settings.FrcApplicationSettings
import net.javaru.iip.frc.util.getPluginResource
import net.javaru.iip.frc.util.getPluginResourceAsStream
import net.javaru.iip.frc.util.isValidJavaVersion
import net.javaru.iip.frc.util.isValidJdk
import net.javaru.iip.frc.util.reader
import net.javaru.iip.frc.util.removeBasePath
import net.javaru.iip.frc.util.toCommaDelimitedString
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.jetbrains.plugins.gradle.service.project.GradleNotification
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

class FrcModuleBuilder : JavaModuleBuilder(), ModuleBuilderListener
{
    private val frcWizardTemplatesBaseDir = Paths.get("frc-wizard-templates")
    private val defaultFilesResourceBase = frcWizardTemplatesBaseDir.resolve("default-files/")
    private val gradleGroovyDslSubPath = Paths.get("gradle/groovy-dsl")
    private val gradleGroovyDslResourceBase = defaultFilesResourceBase.resolve(gradleGroovyDslSubPath)
    private val gradleKotlinDslSubPath = Paths.get("gradle/kotlin-dsl")
    private val gradleKotlinDslResourceBase = defaultFilesResourceBase.resolve(gradleKotlinDslSubPath)
    private val gradleWrapperSubPath = Paths.get("gradle/gradle-wrapper")
    private val gradleWrapperResourceBase = defaultFilesResourceBase.resolve(gradleWrapperSubPath)
    private val configsSubPath = "configs"
    private val configsResourceBase = defaultFilesResourceBase.resolve(configsSubPath)
    private val extrasSubPath = "extras"
    private val extrasResourceBase = defaultFilesResourceBase.resolve(extrasSubPath)
    private val vsCodeConfigsSubPath = "vs-code-configs"
    private val vsCodeConfigsResourceBase = extrasResourceBase.resolve(vsCodeConfigsSubPath)
    private val commonCodeSubPath = Paths.get("code/common-code")
    private val commonCodeResourceBase = defaultFilesResourceBase.resolve(commonCodeSubPath)
    private val javaCodeSubPath = Paths.get("code/java-code")
    private val javaCodeResourceBase = defaultFilesResourceBase.resolve(javaCodeSubPath)    
    private val kotlinCodeSubPath = Paths.get("code/kotlin-code")
    private val kotlinCodeResourceBase = defaultFilesResourceBase.resolve(kotlinCodeSubPath)

    private val mySdkChangedListeners: MutableList<Runnable> = ContainerUtil.createLockFreeCopyOnWriteList()
    private val fmConfig = freemarkerConfiguration(this, "/")
    /** 
     * Tracks the configured SDK since the `myJdk` property (in the super class `ModuleBuilder`) and the value in `WizardContext.getProjectJdk()` 
     * is not set until we pass the initial step. 
     * We need to certain to keep this updated based on activities. A null value indicates not only that an SDK has not been selected,
     * but more likely a valid one (Type * Version) is not available in the listing.
     */
    var selectedSdk: Sdk? = null
        set(sdk) 
        {
            LOG.trace("[FRC] setter called with value of '$sdk'  Previous value was '$field'")
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
    
    
    private val myUseKotlinDSL = false
    
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

        if(FrcApplicationSettings.INSTANCE().enableGradleImportUponNewProjectCreation)
        {
            LOG.info("[FRC] Programmatic Gradle Import of new FRC Project is enabled in Application Settings. Executing Gradle Import")
            autoImportGradleProject(module)
        }
        else
        {
            LOG.info("[FRC] Programmatic Gradle Import of new FRC Project is NOT enabled in Application Settings. A gradle import will not occur.")

        }
    }

    private fun autoImportGradleProject(module: Module?)
    {
        if (module?.project == null)
        {
            LOG.warn("[FRC] project or module is null. Cannot programmatically import Gradle project.")
        }
        else
        {
            StartupManager.getInstance(module.project).runWhenProjectIsInitialized() {
                try
                {
                    if (module.project.basePath != null)
                    {
                        val project = module.project
                        val notificationsManager = NotificationsManager.getNotificationsManager()
                        val notifications = notificationsManager.getNotificationsOfType(Notification::class.java, project)

                        // unfortunately the notification does not have a unique ID, so we can only filter on the group. But it should be the only Gradle notification
                        val gradleNotifications = notifications.filter { it.groupId == GradleNotification.NOTIFICATION_GROUP.displayId }
                        if (gradleNotifications.size == 1)
                        {
                            gradleNotifications.forEach { it.expire() }
                            // We only import if we found the notification. If we import and the user then clicks on the import action on the notification, the IDE throws an error
                            ApplicationManager.getApplication().runWriteAction() {
                                // TODO: When a change is made to only support IDEA v2910.3 or greater, we can use the linkAndRefreshGradleProject from it. Note that it is marked experimental in the EAP version
                                // /* v2019.3 */ org.jetbrains.plugins.gradle.service.project.open.linkAndRefreshGradleProject(module.project.basePath!!, module.project)
                                /* v2019.2 */ org.jetbrains.plugins.gradle.service.project.open.importProject(module.project.basePath!!, module.project)
                            }
                        }
                    }
                }
                catch (e: Exception)
                {
                    LOG.warn("[FRC] Could not programmatically import Gradle project due to an exception: $e", e)
                }
            }
        }
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
        
        

        //TODO Need to enhance the calls to the defaults check if the template has overridden any of the files
        copyAllResourcesToModuleRoot(modelContentRootDir, gradleGroovyDslResourceBase)
        copyAllResourcesToModuleRoot(modelContentRootDir, gradleWrapperResourceBase)
        copyAllResourcesToModuleRoot(modelContentRootDir, configsResourceBase)
        copyAllResourcesToModuleRoot(modelContentRootDir, commonCodeResourceBase)
        copyAllResourcesToModuleRoot(modelContentRootDir, javaCodeResourceBase)
        if (dataModel.includeVsCodeConfigs)
        {
            copyAllResourcesToModuleRoot(modelContentRootDir, vsCodeConfigsResourceBase)
        }
        
        if (dataModel.gitIgnoreConfiguration.includeGitIgnoreFile)
        {
            try
            {
                val gitIgnoreContent = generateGitIgnoreFileContent(dataModel.gitIgnoreConfiguration)
                val file = Paths.get(modelContentRootDir.path).resolve(".gitignore")
                Files.newBufferedWriter(file, Charsets.UTF_8).use { it.write(gitIgnoreContent) }
            }
            catch (e: Exception)
            {
                LOG.warn("[FRC] Unable to create .gitignore file due to an exception: $e", e)
            }
        }
        
        val selectedTemplateResourceBase = frcWizardTemplatesBaseDir.resolve(dataModel.frcWizardTemplateDefinition.templateResourcesDirName()).resolve(javaCodeSubPath)
        copyAllResourcesToModuleRoot(modelContentRootDir, selectedTemplateResourceBase)
        
        modelContentRootDir.refresh(false, true)
        
        LOG.trace("[FRC] FrcModuleBuilder.setupRootModel() completed")
    }

    private fun copyAllResourcesToModuleRoot(modelContentRootDir: VirtualFile, resourceDirBase: Path)
    {
        val pluginResourceDirUrl = getPluginResource(resourceDirBase)
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
                VfsUtil.collectChildrenRecursively(srcFqBaseDir).filter { !it.isDirectory }.forEach {
                    val resourceRelativePath = Paths.get(it.toString().removePrefix("$srcFqBaseDir")).removeBasePath(Paths.get("/"))
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
        try
        {
            val endPath = Paths.get(srcFqVf.toString().removePrefix("$srcFqBaseDir")).removeBasePath(Paths.get("/"))
            val target = resolveTargetPath(modelContentRootDir, endPath)
            Files.createDirectories(target.parent)
            val file = target.toFile()
            
            
            FileUtils.copyInputStreamToFile(srcFqVf.inputStream, file)
            return LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file)
        }
        catch (e: Exception)
        {
            LOG.warn("[FRC] Could not copy new project wizard file '$srcFqVf' to new project root '${modelContentRootDir}' exception: $e", e)
            return null
        }
    }
    
    
    private fun copyFreemarkerTemplate(modelContentRootDir: VirtualFile, srcFqVf: VirtualFile, srcFqBaseDir: VirtualFile)
    {
        try
        {
            val srcEndPath = Paths.get(srcFqVf.toString().removePrefix("$srcFqBaseDir")).removeBasePath(Paths.get("/"))
            val fmTemplateName = srcEndPath.fileName.toString()
            val targetEndPath = srcEndPath.resolveSibling(fmTemplateName.removeSuffix(FM_TEMPLATE_EXT_WITH_DOT))
            val target = resolveTargetPath(modelContentRootDir, targetEndPath)
            val template = Template(FilenameUtils.separatorsToUnix(srcEndPath.toString()), srcFqVf.reader(), fmConfig)
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
                    val url = "https://gitignore.io/api/${templates.toCommaDelimitedString(false)}"
                    val client = ApacheClient()
                    val request = Request(Method.GET, url)
                    val response = client(request)
                    if (response.status.code != 200)
                    {
                        LOG.warn("[FRC] Did not get a successful response when querying gitignore.io in order dynamically create .gitignore file. Response status was ${response.status.code} : ${response.status.description}  Request URI was >>${request.uri}<<")
                        createFromCache = true
                    }
                    else
                    {
                        content.appendln("# Dynamically generated by IntelliJ IDEA FRC Plugin on ${java.time.format.DateTimeFormatter.ofPattern("EEEE MMMM d, yyyy h:mm:ss a zzz").format(ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault()))} using https://gitignore.io")
                        content.appendln(response.bodyString())
                    }
                }
                catch (e: Exception)
                {
                    LOG.warn("[FRC] An exception occurred when attempting to dynamically create .gitignore file from gitignore.io website. Will generate from cache. Cause Summary: $e", e)
                    createFromCache = true
                }
            }

            if (createFromCache)
            {
                content.appendln(generateGitIgnoreSiteContentFromCachedFiles(templates, config.additionalGitignoreTemplates))
            }
        }
        
        if (manualEntries.isNotBlank())
        {
            content.appendln("# === Entries from IntelliJ IDEA FRC Plugin New FRC Project Wizard ===")
            content.appendln()
            content.append(manualEntries)
            content.appendln()
            content.appendln()
            content.appendln("# End entries from IntelliJ IDEA FRC Plugin")
            content.appendln()
        }
        
        return content.toString()
    }


   
    private fun generateGitIgnoreSiteContentFromCachedFiles(templates: MutableList<String>, additionalTemplates: List<String>): String
    {
        val filteredTemplates = templates.filterNot { additionalTemplates.contains(it) }
        val templatesString = filteredTemplates.toCommaDelimitedString(false)
        val content = StringBuilder()

        content.appendln()
        content.appendln("# Created by https://www.gitignore.io/api/$templatesString")
        content.appendln("# Edit at https://www.gitignore.io/?templates=$templatesString")
        content.appendln()
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
                        content.appendln(line)
                    }
                }
                content.appendln()
            }
        }
        content.appendln()
        content.appendln("# End of https://www.gitignore.io/api/$templatesString")
        content.appendln()
        content.appendln()
        
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
    override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep?
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
        LOG.trace("[FRC] Executing FrcModuleBuilder.modifyProjectTypeStep()")
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
                LOG.trace("[FRC] onSdkSelected called with sdk: sdk='$sdk' [type='${sdk?.sdkType}' version='${sdk?.versionString}' name = '${sdk?.name}'" )
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
