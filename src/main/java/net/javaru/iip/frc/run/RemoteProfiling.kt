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

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.ExecutionException
import com.intellij.execution.Executor
import com.intellij.execution.ExecutorRegistry
import com.intellij.execution.configurations.RunProfile
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.configurations.RunnerSettings
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.execution.remote.RemoteConfiguration
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.GenericProgramRunner
import com.intellij.execution.runners.RunContentBuilder
import com.intellij.execution.ui.RunContentDescriptor
import com.intellij.icons.AllIcons
import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.fileTypes.INativeFileType
import com.intellij.openapi.progress.EmptyProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.wm.ToolWindowId
import com.intellij.util.concurrency.AppExecutorUtil
import jdk.management.jfr.FlightRecorderMXBean
import net.javaru.iip.frc.facet.isFrcFacetedProject
import net.javaru.iip.frc.notify.FrcNotifyType
import net.javaru.iip.frc.wpilib.getConfiguredProjectYear
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean
import javax.management.JMX
import javax.management.ObjectName
import javax.management.remote.JMXConnectionNotification
import javax.management.remote.JMXConnector
import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL
import javax.swing.Icon

/*
 * Profiling of the robot program running on the robot (i.e. the roboRIO, or for 2027+ the SystemCore), as described in
 * https://docs.wpilib.org/en/stable/docs/software/advanced-gradlerio/profiling-with-visualvm.html
 *
 * The 'Profile Robot' executor is available for the 'Debug Robot via IP' and 'Debug Robot via USB' (Remote JVM Debug) run
 * configurations, and uses their host. It deploys the robot program with JMX enabled (see remoteProfilingGradleSnippet), and
 * makes a Java Flight Recorder (JFR) recording via JMX. When stopped, the recording is downloaded, and opened in the IntelliJ
 * Profiler (if it is available).
 */

private object RemoteProfiling
private val logger = logger<RemoteProfiling>()

const val defaultProfilingJmxPort = 1198
private const val maxRecordingSizeMb = 32
/** The JFR settings (i.e. the `.jfc` file in the JDK) used. `profile` samples the methods more often than `default`, with a little more overhead. */
private const val jfrSettings = "profile"

private const val profileModeProperty = "profileMode"
private const val profileHostProperty = "profileHost"
private const val profileJmxPortProperty = "profileJmxPort"
/** Used to identify if the build file already has the [remoteProfilingGradleSnippet]. */
private const val remoteProfilingGradleSnippetMarker = "'$profileModeProperty'"
/** The robot's deploy artifact is assigned to `deployArtifact` in the WPILib robot project templates. */
private const val deployArtifactVariable = "deployArtifact"

private val flightRecorderObjectName = ObjectName("jdk.management.jfr:type=FlightRecorder")

/**
 * The (Groovy) build file addition that enables JMX, as described in the WPILib profiling docs, when the robot code is deployed
 * with `-PprofileMode=true`. It is the same for the roboRIO (`frcJava` artifact) and SystemCore (`wpilibJava` artifact), as both
 * WPILib templates assign the artifact to `deployArtifact`.
 */
val remoteProfilingGradleSnippet = """

// Remote profiling. When deployed with -P$profileModeProperty=true (as done by the FRC plugin's 'Profile Robot' action for the
// 'Debug Robot via IP' and 'Debug Robot via USB' run configurations) the robot program can be profiled via JMX and Java Flight
// Recorder. JMX is not secured, so only deploy for profiling on a trusted network. A normal deploy disables it.
// See https://docs.wpilib.org/en/stable/docs/software/advanced-gradlerio/profiling-with-visualvm.html
if (project.findProperty('$profileModeProperty')?.toString()?.toBoolean()) {
    def profileHost = project.findProperty('$profileHostProperty') ?: '127.0.0.1'
    def profileJmxPort = project.findProperty('$profileJmxPortProperty') ?: '$defaultProfilingJmxPort'
    $deployArtifactVariable.jvmArgs.addAll([
        '-Dcom.sun.management.jmxremote=true',
        "-Dcom.sun.management.jmxremote.port=${'$'}{profileJmxPort}",
        "-Dcom.sun.management.jmxremote.rmi.port=${'$'}{profileJmxPort}",
        '-Dcom.sun.management.jmxremote.local.only=false',
        '-Dcom.sun.management.jmxremote.ssl=false',
        '-Dcom.sun.management.jmxremote.authenticate=false',
        "-Djava.rmi.server.hostname=${'$'}{profileHost}",
    ].collect { it.toString() })
}
"""

// region Build file

private fun Project.groovyBuildFile(): Path? = wpiLibProjectRoot()?.resolve("build.gradle")?.takeIf { Files.isRegularFile(it) }

private fun Path.readTextOrNull(): String? = try { Files.readString(this) } catch (e: IOException) { null }

/**
 * Adds the [remoteProfilingGradleSnippet] to the project's build file, if it is not already there, after asking the user.
 * Returns `null` if the build file has (or now has) the snippet, otherwise the reason it does not.
 */
private fun Project.ensureRemoteProfilingGradleSupport(): String?
{
    val buildFile = groovyBuildFile() ?: return "The project's build.gradle file was not found. (Only Groovy build files are supported.)"
    val text = buildFile.readTextOrNull() ?: return "The project's build file ($buildFile) could not be read."
    if (text.contains(remoteProfilingGradleSnippetMarker)) return null
    if (!text.contains(deployArtifactVariable)) return "The project's build file does not define '$deployArtifactVariable', as the WPILib robot project templates do. " +
        "Add the JVM arguments from https://docs.wpilib.org/en/stable/docs/software/advanced-gradlerio/profiling-with-visualvm.html to the robot's Java artifact."

    var add = false
    ApplicationManager.getApplication().invokeAndWait({
        add = Messages.showYesNoDialog(this,
                                       "To profile the robot program, the build.gradle file needs to enable JMX when deploying for profiling (-P$profileModeProperty=true). " +
                                       "Normal deploys are not changed.\n\nAdd it to the build.gradle file?",
                                       "Enable Robot Profiling", Messages.getQuestionIcon()) == Messages.YES
    }, ModalityState.any())
    if (!add) return "Profiling support was not added to the build.gradle file."
    return try
    {
        Files.writeString(buildFile, text.trimEnd() + "\n" + remoteProfilingGradleSnippet)
        LocalFileSystem.getInstance().refreshAndFindFileByNioFile(buildFile)
        logger.info("[FRC] Added remote profiling support to $buildFile")
        null
    }
    catch (e: IOException)
    {
        "The build file ($buildFile) could not be updated: ${e.message}"
    }
}

// endregion

// region Executor & runner

/** The 'Profile Robot' executor, for the Remote JVM Debug (i.e. 'Debug Robot via IP' and 'Debug Robot via USB') run configurations of FRC projects. */
class FrcProfileRobotExecutor : Executor()
{
    companion object
    {
        // Must match the id attribute of the executor element in the plugin.xml file
        const val EXECUTOR_ID = "FrcProfileRobot"

        @JvmStatic
        fun getInstance(): Executor? = ExecutorRegistry.getInstance().getExecutorById(EXECUTOR_ID)
    }

    override fun getToolWindowId(): String = ToolWindowId.RUN
    override fun getToolWindowIcon(): Icon = AllIcons.Toolwindows.ToolWindowRun
    override fun getIcon(): Icon = AllIcons.Actions.Profile
    override fun getDisabledIcon(): Icon = IconLoader.getDisabledIcon(icon)
    override fun getDescription(): String = "Profile the robot program on the robot with Java Flight Recorder"
    override fun getActionName(): String = "Profile Robot"
    override fun getId(): String = EXECUTOR_ID
    override fun getStartActionText(): String = "Profile Robot"
    override fun getContextActionId(): String = "FrcProfileRobotContextAction"
    override fun getHelpId(): String? = null
    override fun isApplicable(project: Project): Boolean = project.isFrcFacetedProject()
}

/**
 * Runs the [FrcProfileRobotExecutor] for a Remote JVM Debug run configuration, using its host as the robot's address. See
 * [RemoteJfrRecordingProcessHandler].
 */
class FrcRobotProfilingProgramRunner : GenericProgramRunner<RunnerSettings>()
{
    override fun getRunnerId(): String = "FrcRobotProfilingRunner"

    override fun canRun(executorId: String, profile: RunProfile): Boolean =
        executorId == FrcProfileRobotExecutor.EXECUTOR_ID && profile is RemoteConfiguration && profile.project.isFrcFacetedProject()

    override fun doExecute(state: RunProfileState, environment: ExecutionEnvironment): RunContentDescriptor?
    {
        val configuration = environment.runProfile as RemoteConfiguration
        val project = environment.project
        val host = configuration.HOST?.trim()?.ifBlank { null } ?: throw ExecutionException("The '${configuration.name}' run configuration does not specify the robot's host.")
        val projectRoot = project.wpiLibProjectRoot() ?: throw ExecutionException("Could not determine the project's root directory.")
        val console = TextConsoleBuilderFactory.getInstance().createBuilder(project).console
        val handler = RemoteJfrRecordingProcessHandler(project, projectRoot, host, defaultProfilingJmxPort)
        console.attachToProcess(handler)
        return RunContentBuilder(DefaultExecutionResult(console, handler), environment).showRunContent(environment.contentToReuse)
    }
}

// endregion

// region Profiling session

/**
 * The "process" for a robot profiling session: deploys the robot code for profiling, connects to the robot program's JMX, and
 * starts a JFR recording. When stopped, the recording is downloaded to `build/profiling`, and opened. The work is done, in
 * order, on a single background thread.
 */
private class RemoteJfrRecordingProcessHandler(private val project: Project,
                                               private val projectRoot: Path,
                                               private val host: String,
                                               private val jmxPort: Int) : ProcessHandler()
{
    private val executor = AppExecutorUtil.createBoundedApplicationPoolExecutor("FRC Robot Profiling", 1)
    private val stopRequested = AtomicBoolean(false)
    private val finished = AtomicBoolean(false)
    @Volatile private var deployIndicator: EmptyProgressIndicator? = null
    @Volatile private var connector: JMXConnector? = null
    private var recorder: FlightRecorderMXBean? = null
    private var recordingId: Long? = null

    override fun startNotify()
    {
        super.startNotify()
        executor.execute {
            if (deployForProfiling()) connectAndStartRecording() else finish(if (stopRequested.get()) 0 else 1)
        }
    }

    /** Runs `gradlew deploy -PprofileMode=true -PprofileHost=<host> -PprofileJmxPort=<port>` with the WPILib JDK. */
    private fun deployForProfiling(): Boolean
    {
        project.ensureRemoteProfilingGradleSupport()?.let { error(it); return false }
        val projectYear = project.getConfiguredProjectYear()
        val jdkHome = wpiLibJdkHomeCandidates(projectYear).firstOrNull { it.hasJavaExecutable() } ?: run { error(wpiLibJdkNotFoundMessage(projectYear)); return false }
        val parameters = listOf("deploy", "-P$profileModeProperty=true", "-P$profileHostProperty=$host", "-P$profileJmxPortProperty=$jmxPort")
        output("Deploying the robot code for profiling via $host ...")
        val indicator = EmptyProgressIndicator()
        deployIndicator = indicator
        if (stopRequested.get()) return false
        val result = runGradleWrapperWithWpiLibJdk(projectRoot, jdkHome, parameters, "Deploying the robot code for profiling", indicator) { text, outputType ->
            notifyTextAvailable(text, outputType)
        }
        deployIndicator = null
        return when (result)
        {
            WpiLibGradleResult.Succeeded -> true
            WpiLibGradleResult.Cancelled -> false
            is WpiLibGradleResult.Failed -> { error(result.message); false }
        }
    }

    private fun connectAndStartRecording()
    {
        val url = JMXServiceURL("service:jmx:rmi:///jndi/rmi://$host:$jmxPort/jmxrmi")
        output("Connecting to the robot program at $host:$jmxPort ...")
        // The robot program takes a few seconds to start after it is deployed
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(90)
        var lastError: Exception? = null
        while (connector == null && !stopRequested.get())
        {
            try
            {
                connector = connect(url)
            }
            catch (e: Exception)
            {
                lastError = e
                if (System.nanoTime() > deadline) break
                Thread.sleep(2000)
            }
        }
        val connector = connector
        if (connector == null)
        {
            if (!stopRequested.get()) error("Could not connect to the robot program at $host:$jmxPort: ${lastError?.message}")
            finish(if (stopRequested.get()) 0 else 1)
            return
        }
        connector.addConnectionNotificationListener({ notification, _ ->
            if ((notification.type == JMXConnectionNotification.FAILED || notification.type == JMXConnectionNotification.CLOSED) && !stopRequested.get())
            {
                error("The connection to the robot program was lost (for example, the robot program was restarted), so the recording is lost.")
                finish(1)
            }
        }, null, null)

        try
        {
            val recorder = JMX.newMXBeanProxy(connector.mBeanServerConnection, flightRecorderObjectName, FlightRecorderMXBean::class.java)
            val id = recorder.newRecording()
            recorder.setPredefinedConfiguration(id, jfrSettings)
            recorder.setRecordingOptions(id, mapOf("name" to "FRC IntelliJ Profiling",
                                                   "disk" to "true",
                                                   "maxSize" to (maxRecordingSizeMb.toLong() * 1024 * 1024).toString()))
            recorder.startRecording(id)
            this.recorder = recorder
            this.recordingId = id
            output("Recording the robot program with Java Flight Recorder ('$jfrSettings' settings, at most $maxRecordingSizeMb MB).")
            output("Run the robot code you want to profile, then click Stop to download the recording and open it.")
        }
        catch (e: Exception)
        {
            error("Could not start the Java Flight Recorder recording: $e")
            closeConnection()
            finish(1)
        }
    }

    private fun connect(url: JMXServiceURL): JMXConnector
    {
        // An unreachable host can block for a long time, so the connection attempt is limited
        val future = AppExecutorUtil.getAppExecutorService().submit(Callable { JMXConnectorFactory.connect(url) })
        return try
        {
            future.get(10, TimeUnit.SECONDS)
        }
        catch (e: TimeoutException)
        {
            future.cancel(true)
            throw IOException("Timed out connecting to $url")
        }
        catch (e: java.util.concurrent.ExecutionException)
        {
            throw (e.cause as? Exception) ?: e
        }
    }

    private fun stopRecordingAndDownload()
    {
        val recorder = recorder
        val id = recordingId
        if (recorder == null || id == null || finished.get())
        {
            closeConnection()
            finish(0)
            return
        }
        var exitCode = 0
        try
        {
            output("Stopping the recording ...")
            recorder.stopRecording(id)
            val outputDir = projectRoot.resolve("build").resolve("profiling")
            Files.createDirectories(outputDir)
            val file = outputDir.resolve("robot-${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))}.jfr")
            val streamId = recorder.openStream(id, null)
            var bytes = 0L
            Files.newOutputStream(file).use { out ->
                while (true)
                {
                    val chunk = recorder.readStream(streamId) ?: break
                    out.write(chunk)
                    bytes += chunk.size
                }
            }
            recorder.closeStream(streamId)
            recorder.closeRecording(id)
            output("Downloaded the recording (${bytes / 1024} KB) to $file")
            openRecording(file)
        }
        catch (e: Exception)
        {
            error("Could not download the recording: $e")
            exitCode = 1
        }
        closeConnection()
        finish(exitCode)
    }

    private fun openRecording(file: Path)
    {
        ApplicationManager.getApplication().invokeLater({
            if (project.isDisposed) return@invokeLater
            val virtualFile = LocalFileSystem.getInstance().refreshAndFindFileByNioFile(file)
            val fileType = virtualFile?.fileType
            // The IntelliJ Profiler (IntelliJ IDEA Ultimate) opens JFR files
            if (virtualFile != null && fileType is INativeFileType && fileType.openFileInAssociatedApplication(project, virtualFile))
            {
                logger.info("[FRC] Opened the profiling recording $file with the '${fileType.name}' file type")
                return@invokeLater
            }
            logger.info("[FRC] The profiling recording $file could not be opened in the IDE (file type: ${fileType?.name})")
            FrcNotifyType.ACTIONABLE_INFO.builder()
                .withContent("The robot program's Java Flight Recorder recording was saved to $file. Open it with JDK Mission Control, or the IntelliJ Profiler (IntelliJ IDEA Ultimate).")
                .withFrcPrefixedTitle("Robot Profiling Recording Saved")
                .withNoSubTitle()
                .withActionBasic(RevealFileAction.getActionName()) { RevealFileAction.openFile(file) }
                .noMoreActions()
                .notify(project)
        }, ModalityState.nonModal())
    }

    private fun closeConnection()
    {
        try { connector?.close() } catch (e: Exception) { logger.debug("[FRC] Could not close the JMX connection: $e") }
    }

    private fun output(text: String) = notifyTextAvailable("$text\n", ProcessOutputTypes.SYSTEM)

    private fun error(text: String) = notifyTextAvailable("$text\n", ProcessOutputTypes.STDERR)

    private fun finish(exitCode: Int)
    {
        if (finished.compareAndSet(false, true)) notifyProcessTerminated(exitCode)
    }

    override fun destroyProcessImpl()
    {
        stopRequested.set(true)
        deployIndicator?.cancel()
        executor.execute { stopRecordingAndDownload() }
    }

    override fun detachProcessImpl() = destroyProcessImpl()

    override fun detachIsDefault(): Boolean = false

    override fun getProcessInput(): OutputStream? = null
}

// endregion
