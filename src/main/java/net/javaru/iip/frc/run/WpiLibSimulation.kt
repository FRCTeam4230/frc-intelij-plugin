/*
 * Copyright 2015-2026 the original author or authors.
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

package net.javaru.iip.frc.run

import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.intellij.application.options.ModulesComboBox
import com.intellij.execution.BeforeRunTask
import com.intellij.execution.BeforeRunTaskProvider
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.RunManager
import com.intellij.execution.application.ApplicationConfiguration
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.JavaCommandLineState
import com.intellij.execution.configurations.JavaParameters
import com.intellij.execution.configurations.JavaRunConfigurationModule
import com.intellij.execution.configurations.ModuleBasedConfiguration
import com.intellij.execution.configurations.ModuleBasedConfigurationOptions
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.openapi.util.SystemInfo
import com.intellij.testFramework.LightVirtualFile
import com.intellij.ui.RawCommandLineEditor
import com.intellij.util.execution.ParametersListUtil
import com.intellij.util.ui.FormBuilder
import icons.FrcIcons
import net.javaru.iip.frc.notify.FrcNotifyType
import net.javaru.iip.frc.util.getMainModule
import net.javaru.iip.frc.wpilib.extractProjectYear
import net.javaru.iip.frc.wpilib.findWpiLibProjectRootDirs
import net.javaru.iip.frc.wpilib.getConfiguredProjectYear
import net.javaru.iip.frc.wpilib.getDefaultWpiLibRootPath
import org.jdom.Element
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.Year
import javax.swing.Icon
import javax.swing.JCheckBox
import javax.swing.JComponent

private object WpiLibSimulation
private val logger = logger<WpiLibSimulation>()

const val simulateRobotCodeRunConfigName = "Simulate Robot Code"
const val simulateRobotCodeHwSimRunConfigName = "Simulate Robot Code (Hardware Sim)"
const val cleanBuildAndSimulateRobotCodeRunConfigName = "Clean Build & Simulate Robot Code"

private const val simulateExternalJavaGradleTask = "simulateExternalJava"
private const val hwSimArgument = "-PhwSim"
private const val halSimExtensionsEnvVar = "HALSIM_EXTENSIONS"

// region JDK

/**
 * Finds the JDK installed by the WPILib installer for the project's year. The WPILib install directory is named for the
 * project's `projectYear` (e.g. `C:\Users\Public\wpilib\2027_alpha7`), with the plain year (e.g. `2026`) as a fallback.
 * **Must not be called on the EDT** as determining the project year is a slow operation.
 */
fun Project.findWpiLibJdkHome(): Path? = wpiLibJdkHomeCandidates(getConfiguredProjectYear()).firstOrNull { it.hasJavaExecutable() }

/** The possible WPILib JDK locations for the project year, in order of preference. */
fun wpiLibJdkHomeCandidates(projectYear: String?): List<Path>
{
    val year = extractProjectYear(projectYear) ?: Year.now().value
    // e.g. C:\Users\Public\wpilib (Windows) or ~/wpilib (Mac & Linux)
    val wpiLibBaseDir = getDefaultWpiLibRootPath(year).parent
    return listOfNotNull(projectYear?.trim()?.ifBlank { null }, year.toString())
        .distinct()
        .map { wpiLibBaseDir.resolve(it).resolve("jdk") }
}

private fun Path.hasJavaExecutable(): Boolean = Files.isRegularFile(resolve("bin").resolve(if (SystemInfo.isWindows) "java.exe" else "java"))

// endregion

// region VM options

private const val libraryPathVmOption = "-Djava.library.path="
private const val addOpensVmOption = "--add-opens"

/**
 * The JVM options GradleRIO uses when running a Java simulation (see its `WPIJavaExtension`). They are required, for
 * example, by Commands v3, which fails to start without access to `jdk.internal.vm`.
 */
private val gradleRioSimulationVmOptions: List<String> = listOf(
    addOpensVmOption, "java.base/jdk.internal.vm=ALL-UNNAMED",
    addOpensVmOption, "java.base/java.lang=ALL-UNNAMED",
    "--enable-native-access=ALL-UNNAMED",
                                                               ) + if (SystemInfo.isMac) listOf("-XstartOnFirstThread") else emptyList()

/**
 * Returns the VM options for the simulation: the user's options, with the simulation options appended. Any simulation
 * options already in the user's options are removed so they are not duplicated.
 */
fun simulationVmOptions(existingOptions: List<String>, libraryDir: String?): List<String>
{
    val managedAddOpensValues = gradleRioSimulationVmOptions.zipWithNext().filter { it.first == addOpensVmOption }.map { it.second }.toSet()
    val userOptions = mutableListOf<String>()
    var index = 0
    while (index < existingOptions.size)
    {
        val option = existingOptions[index]
        when
        {
            option == addOpensVmOption && existingOptions.getOrNull(index + 1) in managedAddOpensValues -> index++ // also skip its value
            // (a user's own '--add-opens' flag, for a value not managed by the plugin, is kept)
            option.startsWith(libraryPathVmOption) || (option != addOpensVmOption && option in gradleRioSimulationVmOptions) -> {}
            else -> userOptions.add(option)
        }
        index++
    }
    return userOptions + gradleRioSimulationVmOptions + listOfNotNull(libraryDir?.let { "$libraryPathVmOption$it" })
}

// endregion

// region Run configuration creation

/**
 * Creates the simulation run configurations (simulate, simulate with hardware simulation, and clean build & simulate) if they
 * do not already exist. They are only created once the project's main module exists (i.e. after the Gradle import).
 */
fun ensureWpiLibSimulationRunConfigurations(project: Project)
{
    if (project.isDisposed || project.getMainModule() == null) return
    createSimulationRunConfiguration(project, simulateRobotCodeRunConfigName, hwSim = false, clean = false)
    createSimulationRunConfiguration(project, simulateRobotCodeHwSimRunConfigName, hwSim = true, clean = false)
    createSimulationRunConfiguration(project, cleanBuildAndSimulateRobotCodeRunConfigName, hwSim = false, clean = true)
}

private fun createSimulationRunConfiguration(project: Project, name: String, hwSim: Boolean, clean: Boolean)
{
    try
    {
        val runManager = RunManager.getInstance(project)
        val existing = runManager.findConfigurationByName(name)
        if (existing != null)
        {
            // An earlier version of the plugin created these as Application configurations, which do not work since IDEA
            // delegates running them to Gradle. Those are replaced; any other configuration with the name is left alone.
            if (existing.configuration is WpiLibSimulationRunConfiguration || existing.configuration !is ApplicationConfiguration) return
            runManager.removeConfiguration(existing)
            logger.info("[FRC] Replacing the '$name' Application run configuration with a WPILib Simulation configuration for project '${project.name}'")
        }
        val settings = runManager.createConfiguration(name, WpiLibSimulationConfigurationType.getInstance().configurationFactories[0])
        val configuration = settings.configuration as WpiLibSimulationRunConfiguration
        project.getMainModule()?.let { configuration.setModule(it) }
        configuration.hwSim = hwSim
        configuration.clean = clean
        runManager.addConfiguration(settings)
        logger.info("[FRC] Created '$name' run configuration for project '${project.name}'")
    }
    catch (e: Exception)
    {
        logger.warn("[FRC] Could not create '$name' Run Configuration for project '${project.name}' due to an exception: $e", e)
    }
}

private fun Project.wpiLibProjectRoot(): Path? = (findWpiLibProjectRootDirs().firstOrNull() ?: guessProjectDir())?.toNioPath()

// endregion

// region Run configuration type

class WpiLibSimulationConfigurationType : ConfigurationTypeBase(
    ID, "WPILib Simulation", "Runs the robot program in the WPILib simulator",
    NotNullLazyValue.createValue { FrcIcons.FRC.FIRST_ICON_MEDIUM_16 })
{
    init
    {
        addFactory(object : ConfigurationFactory(this)
                   {
                       override fun getId(): String = ID
                       override fun createTemplateConfiguration(project: Project): RunConfiguration = WpiLibSimulationRunConfiguration(project, this, "WPILib Simulation")
                       override fun getOptionsClass(): Class<WpiLibSimulationOptions> = WpiLibSimulationOptions::class.java
                   })
    }

    companion object
    {
        const val ID = "FRC.WpiLibSimulation"

        @JvmStatic
        fun getInstance(): WpiLibSimulationConfigurationType = com.intellij.execution.configurations.ConfigurationTypeUtil.findConfigurationType(WpiLibSimulationConfigurationType::class.java)
    }
}

class WpiLibSimulationOptions : ModuleBasedConfigurationOptions()
{
    var hwSim by property(false)
    var clean by property(false)
    var vmOptions by string()
}

/** The simulation launch details, determined by the before run task from the generated `build/sim/java.json`. */
data class WpiLibSimulationLaunchInfo(
    val jdkHome: Path,
    val projectRoot: Path,
    val mainClass: String,
    val libraryDir: String?,
    val extensionLibraries: List<String>,
    val environment: Map<String, String>,
                                     )

/**
 * Runs the robot program in the WPILib simulator, as the WPILib VS Code extension does: the [WpiLibSimulateExternalBeforeRunTask]
 * runs the `simulateExternalJava` Gradle task, which generates the simulation launch details, and then the robot program is run
 * with the WPILib JDK, the simulation extensions (e.g. the Sim GUI), and the simulation's native libraries. Run and Debug are supported.
 */
class WpiLibSimulationRunConfiguration(project: Project, factory: ConfigurationFactory, name: String) :
    ModuleBasedConfiguration<JavaRunConfigurationModule, Element>(name, JavaRunConfigurationModule(project, false), factory)
{
    /** Set by the before run task for the run being launched. */
    @Transient
    var launchInfo: WpiLibSimulationLaunchInfo? = null

    override fun getOptions(): WpiLibSimulationOptions = super.getOptions() as WpiLibSimulationOptions

    var hwSim: Boolean
        get() = options.hwSim
        set(value) { options.hwSim = value }

    var clean: Boolean
        get() = options.clean
        set(value) { options.clean = value }

    var vmOptions: String?
        get() = options.vmOptions
        set(value) { options.vmOptions = value }

    override fun getValidModules(): Collection<Module> = ModuleManager.getInstance(project).modules.toList()

    override fun checkConfiguration()
    {
        if (configurationModule.module == null) throw RuntimeConfigurationError("The module is not specified. Select the robot project's 'main' module.")
    }

    override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> = WpiLibSimulationSettingsEditor(project)

    override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState
    {
        val module = configurationModule.module ?: throw ExecutionException("The module is not specified for '$name'.")
        return object : JavaCommandLineState(environment)
        {
            override fun createJavaParameters(): JavaParameters
            {
                val info = launchInfo ?: throw ExecutionException(
                    "The simulation has not been prepared. The '${WpiLibSimulateExternalBeforeRunTaskProvider.NAME}' before launch task is required.")
                val parameters = JavaParameters()
                parameters.configureByModule(module, JavaParameters.JDK_AND_CLASSES)
                parameters.jdk = JavaSdk.getInstance().createJdk("WPILib JDK", info.jdkHome.toString(), false)
                parameters.mainClass = info.mainClass
                parameters.workingDirectory = info.projectRoot.toString()
                parameters.vmParametersList.addAll(simulationVmOptions(ParametersListUtil.parse(vmOptions ?: ""), info.libraryDir))

                val env = LinkedHashMap(info.environment)
                if (info.extensionLibraries.isNotEmpty()) env[halSimExtensionsEnvVar] = info.extensionLibraries.joinToString(File.pathSeparator)
                // The simulation's native libraries must be on the OS library path, as done by WPILib
                if (info.libraryDir != null)
                {
                    val libraryPathEnvVar = when
                    {
                        SystemInfo.isWindows -> System.getenv().keys.firstOrNull { it.equals("PATH", ignoreCase = true) } ?: "PATH"
                        SystemInfo.isMac     -> "DYLD_LIBRARY_PATH"
                        else                 -> "LD_LIBRARY_PATH"
                    }
                    env[libraryPathEnvVar] = listOfNotNull(info.libraryDir, System.getenv(libraryPathEnvVar)?.ifBlank { null }).joinToString(File.pathSeparator)
                }
                parameters.env = env
                parameters.isPassParentEnvs = true
                return parameters
            }
        }
    }
}

private class WpiLibSimulationSettingsEditor(project: Project) : SettingsEditor<WpiLibSimulationRunConfiguration>()
{
    private val modulesComboBox = ModulesComboBox().apply { fillModules(project) }
    private val hwSimCheckBox = JCheckBox("Hardware simulation ($hwSimArgument)")
    private val cleanCheckBox = JCheckBox("Clean before building")
    private val vmOptionsEditor = RawCommandLineEditor()
    private val panel = FormBuilder.createFormBuilder()
        .addLabeledComponent("Module:", modulesComboBox)
        .addComponent(hwSimCheckBox)
        .addComponent(cleanCheckBox)
        .addLabeledComponent("Additional VM options:", vmOptionsEditor)
        .addComponentFillVertically(javax.swing.JPanel(), 0)
        .panel

    override fun resetEditorFrom(configuration: WpiLibSimulationRunConfiguration)
    {
        modulesComboBox.selectedModule = configuration.configurationModule.module
        hwSimCheckBox.isSelected = configuration.hwSim
        cleanCheckBox.isSelected = configuration.clean
        vmOptionsEditor.text = configuration.vmOptions ?: ""
    }

    override fun applyEditorTo(configuration: WpiLibSimulationRunConfiguration)
    {
        configuration.setModule(modulesComboBox.selectedModule)
        configuration.hwSim = hwSimCheckBox.isSelected
        configuration.clean = cleanCheckBox.isSelected
        configuration.vmOptions = vmOptionsEditor.text.ifBlank { null }
    }

    override fun createEditor(): JComponent = panel
}

// endregion

// region Before run task

/** A before run task, for [WpiLibSimulationRunConfiguration]s, that runs WPILib's `simulateExternalJava` Gradle task. */
class WpiLibSimulateExternalBeforeRunTask : BeforeRunTask<WpiLibSimulateExternalBeforeRunTask>(WpiLibSimulateExternalBeforeRunTaskProvider.ID)
{
    init
    {
        isEnabled = true
    }
}

/**
 * Runs `gradlew [clean] simulateExternalJava [-PhwSim] -Dorg.gradle.java.home=<WPILib JDK>`, as the WPILib VS Code extension
 * does, then reads the generated `build/sim/java.json` to determine how to launch the simulation.
 */
class WpiLibSimulateExternalBeforeRunTaskProvider : BeforeRunTaskProvider<WpiLibSimulateExternalBeforeRunTask>()
{
    companion object
    {
        @JvmField
        val ID: Key<WpiLibSimulateExternalBeforeRunTask> = Key.create("FRC.WpiLibSimulateExternal")
        const val NAME = "WPILib: Prepare Simulation ($simulateExternalJavaGradleTask)"
    }

    override fun getId(): Key<WpiLibSimulateExternalBeforeRunTask> = ID

    override fun getName(): String = NAME

    override fun getIcon(): Icon = FrcIcons.FRC.FIRST_ICON_MEDIUM_16

    // Added (enabled) to every WPILib Simulation run configuration
    override fun createTask(runConfiguration: RunConfiguration): WpiLibSimulateExternalBeforeRunTask? =
        if (runConfiguration is WpiLibSimulationRunConfiguration) WpiLibSimulateExternalBeforeRunTask() else null

    override fun executeTask(context: DataContext, configuration: RunConfiguration, environment: ExecutionEnvironment, task: WpiLibSimulateExternalBeforeRunTask): Boolean
    {
        val project = environment.project
        val simConfiguration = configuration as? WpiLibSimulationRunConfiguration
            ?: return failed(project, "The '$NAME' before launch task can only be used with WPILib Simulation run configurations.")
        simConfiguration.launchInfo = null
        val projectRoot = project.wpiLibProjectRoot()
            ?: return failed(project, "Could not determine the project's root directory.")

        val projectYear = project.getConfiguredProjectYear()
        val jdkHome = wpiLibJdkHomeCandidates(projectYear).firstOrNull { it.hasJavaExecutable() }
            ?: return failed(project, "The WPILib JDK for '${projectYear ?: "the project year"}' was not found. Looked in: " +
                wpiLibJdkHomeCandidates(projectYear).joinToString(", ") + ". Install WPILib for the project's year, or update the project's year.")

        val gradlew = projectRoot.resolve(if (SystemInfo.isWindows) "gradlew.bat" else "gradlew")
        if (!Files.isRegularFile(gradlew)) return failed(project, "The Gradle wrapper ($gradlew) was not found.")
        if (!SystemInfo.isWindows) gradlew.toFile().setExecutable(true)

        val commandLine = GeneralCommandLine(gradlew.toString())
            .apply { if (simConfiguration.clean) addParameter("clean") }
            .withParameters(simulateExternalJavaGradleTask)
            .apply { if (simConfiguration.hwSim) addParameter(hwSimArgument) }
            .withParameters("-Dorg.gradle.java.home=$jdkHome")
            .withWorkDirectory(projectRoot.toFile())
            .withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)
            // The wrapper script launches Gradle with JAVA_HOME
            .withEnvironment("JAVA_HOME", jdkHome.toString())

        logger.info("[FRC] Running: ${commandLine.commandLineString}")
        val indicator = ProgressManager.getInstance().progressIndicator
        indicator?.text = "Running $simulateExternalJavaGradleTask" + if (simConfiguration.hwSim) " (hardware simulation)" else ""
        val handler = CapturingProcessHandler(commandLine)
        handler.addProcessListener(object : ProcessListener
                                   {
                                       override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>)
                                       {
                                           event.text.trim().takeIf { it.isNotEmpty() }?.let { indicator?.text2 = it }
                                       }
                                   })
        val output = if (indicator != null) handler.runProcessWithProgressIndicator(indicator) else handler.runProcess()
        if (output.isCancelled) return false
        if (output.exitCode != 0)
        {
            val fullOutput = "> ${commandLine.commandLineString}\n\n${output.stdout}\n${output.stderr}"
            return failed(project, "'$simulateExternalJavaGradleTask' failed (exit code ${output.exitCode}).", fullOutput)
        }

        val simConfigFile = projectRoot.resolve("build").resolve("sim").resolve("java.json")
        val simConfig = try
        {
            (Files.newBufferedReader(simConfigFile).use { Parser.default().parse(it) } as JsonArray<*>)
                .filterIsInstance<JsonObject>()
                .firstOrNull { it.string("type") == "java" }
                ?: throw IllegalStateException("No Java simulation configuration found in $simConfigFile")
        }
        catch (e: Exception)
        {
            return failed(project, "Could not read the simulation configuration '$simConfigFile': ${e.message}")
        }
        val mainClass = simConfig.string("mainClassName")
            ?: return failed(project, "The simulation configuration '$simConfigFile' does not specify a main class.")

        simConfiguration.launchInfo = WpiLibSimulationLaunchInfo(
            jdkHome = jdkHome,
            projectRoot = projectRoot,
            mainClass = mainClass,
            libraryDir = simConfig.string("libraryDir"),
            // The extensions (e.g. the Sim GUI) that are enabled by default in the project's build.gradle
            extensionLibraries = simConfig.array<JsonObject>("extensions")
                ?.filter { it.boolean("defaultEnabled") == true }
                ?.mapNotNull { it.string("libName") }
                ?: emptyList(),
            environment = (simConfig.obj("environment")?.map ?: emptyMap<String, Any?>())
                .mapNotNull { (key, value) -> value?.let { key to it.toString() } }
                .toMap(),
                                                               )
        return true
    }

    private fun failed(project: Project, message: String, output: String? = null): Boolean
    {
        logger.warn("[FRC] WPILib simulation could not be started: $message")
        val builder = FrcNotifyType.ACTIONABLE_ERROR.builder()
            .withContent(message)
            .withFrcPrefixedTitle("Robot Simulation Failed")
            .withNoSubTitle()
        val withActions = if (output != null)
        {
            builder.withActionBasic("Show Gradle output") {
                ApplicationManager.getApplication().invokeLater({
                    if (!project.isDisposed)
                    {
                        val outputFile = LightVirtualFile("simulateExternalJava-output.log", PlainTextFileType.INSTANCE, output)
                        outputFile.isWritable = false
                        FileEditorManager.getInstance(project).openFile(outputFile, true)
                    }
                }, ModalityState.nonModal())
            }.noMoreActions()
        }
        else
        {
            builder.withNoActions()
        }
        withActions.notify(project)
        return false
    }
}

// endregion
