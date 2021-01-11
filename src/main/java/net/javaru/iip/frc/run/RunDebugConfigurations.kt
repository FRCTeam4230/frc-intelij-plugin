/*
 * Copyright 2015-2021 the original author or authors
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

package net.javaru.iip.frc.run

import com.intellij.configurationStore.MODERN_NAME_CONVERTER
import com.intellij.execution.RunManager
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.remote.RemoteConfiguration
import com.intellij.execution.remote.RemoteConfigurationType
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import net.javaru.iip.frc.settings.FrcApplicationSettings
import net.javaru.iip.frc.settings.FrcRoboRioSettings
import net.javaru.iip.frc.settings.RoboRioAddressType
import net.javaru.iip.frc.settings.getProjectTeamNumber
import net.javaru.iip.frc.util.getMainModule
import org.jetbrains.plugins.gradle.service.execution.GradleExternalTaskConfigurationType
import org.jetbrains.plugins.gradle.service.execution.GradleRunConfiguration

private object RunDebugConfigurations 

private val logger = Logger.getInstance(RunDebugConfigurations::class.java)

/**
 * @param arguments the (optional) arguments to send to Gradle when it runs. For example "-PdebugMode=true" 
 * @param vmOptions the (optional) VM Arguments to set. For example "-Dorg.gradle.java.home=C:/java/11"
 */
@JvmOverloads
fun createGradleRunConfiguration(project: Project,
                                 runConfigName: String,
                                 gradleTasks: List<String>,
                                 setAsSelected: Boolean = false,
                                 setAsShared: Boolean = true,
                                 arguments:String? = null,
                                 vmOptions: String? = null)
{
    // For reference and examples:
    //     A) runCustomTask in org.jetbrains.plugins.gradle.service.task.GradleTaskManager
    //     B) org.jetbrains.plugins.gradle.service.execution.GradleRunConfigurationImporter
    //     C) CreateAndEditPolicy static class in com.intellij.execution.actions.CreateAction
    //          This is what is used for the "Create 'FRC [build]'.." popup in the Gradle tool window
    //          Found via the "create.run.configuration.for.item.action.name" bundle message from P:\ij\platform\platform-resources-en\src\messages\ExecutionBundle.properties
    //          It extends CreatePolicy
    //          While it has/uses a ConfigurationContext, the context is used to get:
    //              1) RunManager
    //              2) RunnerAndConfigurationSettings
    //          which we can get by the fact we have a handle on the project
    //
    // Based on org.jetbrains.idea.devkit.module.PluginModuleBuilder, it appears the commitModule method is the place to create the configurations.

    try
    {
        if (project.basePath != null)
        {
            val runManager = RunManager.getInstance(project)
            val configurationFactory = GradleExternalTaskConfigurationType.getInstance().configurationFactories[0]
            val runnerAndConfigurationSettings = runManager.createConfiguration(runConfigName, configurationFactory)
            val gradleRunConfiguration = runnerAndConfigurationSettings.configuration as GradleRunConfiguration

            val gradleTaskSettings = gradleRunConfiguration.settings
            gradleTaskSettings.externalProjectPath = project.basePath
            gradleTaskSettings.taskNames = gradleTasks
            if (arguments != null) gradleTaskSettings.scriptParameters = arguments
            if (vmOptions != null) gradleTaskSettings.vmOptions = arguments

            if (setAsShared)
            {
                shareRunConfiguration(project, runnerAndConfigurationSettings)
            }

            runManager.addConfiguration(runnerAndConfigurationSettings)
            if (setAsSelected)
            {
                runManager.selectedConfiguration = runnerAndConfigurationSettings
            }
        }
        else
        {
            logger.info("[FRC] Could not create '$runConfigName' Run Configuration for project '${project.name}' because the project's basDir was null")
        }
    }
    catch (e: Exception)
    {
        logger.warn("[FRC] Could not create '$runConfigName' Run Configuration for project '${project.name}' due to an exception: $e", e)
    }
}


@JvmOverloads
fun createDebuggingRunConfiguration(project: Project, teamNumber: Int = project.getProjectTeamNumber(), addressType: RoboRioAddressType, setAsShared: Boolean = true)
{
    // Some notes: https://firstmncsa.org/2019/01/01/debugging-java-remote-debugging/#intellij
    try
    {
        val mainModule = project.getMainModule()
        if (project.basePath != null && mainModule != null)
        {
            val runManager = RunManager.getInstance(project)
            
            val baseName = "Debug Robot via ${addressType.name}"
            val runConfigName = determineNextName(runManager, baseName, RemoteConfigurationType::class.java)
            val configurationFactory = RemoteConfigurationType.getInstance().configurationFactories[0]
            val runnerAndConfigurationSettings = runManager.createConfiguration(runConfigName, configurationFactory)

            val remoteConfiguration = runnerAndConfigurationSettings.configuration as RemoteConfiguration
            remoteConfiguration.HOST = FrcRoboRioSettings.createRoboRioAddressDefault(teamNumber, addressType)
            remoteConfiguration.PORT = FrcApplicationSettings.getInstance().debuggingPort.toString()
            remoteConfiguration.setModule(mainModule)
            if (setAsShared)
            {
                shareRunConfiguration(project, runnerAndConfigurationSettings)
            }

            runManager.addConfiguration(runnerAndConfigurationSettings)
        }
        else
        {
            logger.info("[FRC] Could not create Debugging Configuration for project '${project.name}' because the project's basDir was null")
        }
    }
    catch (e: Exception)
    {
        logger.warn("[FRC] Could not create Debugging Configuration for project '${project.name}' due to an exception: $e", e)
    }
}

private fun shareRunConfiguration(project: Project, settings: RunnerAndConfigurationSettings)
{
    val baseDir = LocalFileSystem.getInstance().findFileByPath(StringUtil.notNullize(project.basePath))
    if (baseDir != null)
    {
        val dirPath = "${baseDir.path}/.run"
        val fileName = createRunConfigFileName(settings.name)
        val filePath = "$dirPath/$fileName"
        settings.storeInArbitraryFileInProject(filePath)
    }
}

/** Creates a safe file name for a run config. This is a copy of the private `RunConfigurationStorageUi.getFileNameByRCName` method. */
private fun createRunConfigFileName(runConfigName: String): String = MODERN_NAME_CONVERTER.invoke(runConfigName) + ".run.xml"


fun determineNextName(project: Project, baseName:String, type: Class<out ConfigurationType>): String = determineNextName(RunManager.getInstance(project), baseName, type)

fun determineNextName(runManager: RunManager, baseName:String, type: Class<out ConfigurationType>): String = determineNextName(baseName, runManager.getConfigurationSettingsList(type))

fun determineNextName(baseName:String, configurationSettingsList: List<RunnerAndConfigurationSettings>): String
{
    val names = configurationSettingsList.map { it.name }

    return determineNextName(names, baseName)
}

fun determineNextName(names: List<String>, baseName: String): String
{
    val nameRegex = """${baseName}( \(([\d]*)\))?""".toRegex()
    val filteredList = names.filter { it.matches(nameRegex) }
    return if (filteredList.isEmpty())
    {
        baseName
    }
    else
    {
        val name = filteredList.maxOrNull() ?: ""
        val matchResult = nameRegex.matchEntire(name)
        if (matchResult == null)
        {
            baseName
        }
        else
        {
            val matchGroup = matchResult.groups[2]
            if (matchGroup == null)
            {
                "$baseName (1)"
            }
            else
            {
                val last = matchGroup.value.toInt()
                val next = last + 1
                "$baseName ($next)"
            }
        }
    }
}
