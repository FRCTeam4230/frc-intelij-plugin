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



package net.javaru.iip.frc.templates

import java.io.BufferedReader
import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate

val allwpilibProjectRootDir: Path = Path.of("P:\\dev\\proj\\open\\wpi\\allwpilib")

val wpilibjDir: Path = allwpilibProjectRootDir.resolve("wpilibjExamples\\src\\main\\java\\edu\\wpi\\first\\wpilibj")
val wpiExamplesDir: Path = wpilibjDir.resolve("examples")
val wpiTemplatesDir: Path = wpilibjDir.resolve("templates")
val targetInnerPath:Path = Path.of("code/java-code/src/main/java/base-package")
val templatesDirMapping = createTemplatesDirMapping()
val examplesDirMapping = createExamplesDirMapping()
val combinedMappings = templatesDirMapping.toMutableMap().also { it.putAll(examplesDirMapping) }.toMap()
val missingMappings = mutableListOf<String>()
val outdatedMappings = combinedMappings.keys.toMutableSet()
val year = determineTemplatesYear()
val ourTemplatesDir = findOutputDir().resolve(year.toString())

// runs without doing the copy. Allows for updating of the template mappings.
const val IS_DRY_RUN = false


private object TemplatesPrepTool

fun main()
{

    println("Starting. IS_DRY_RUN = $IS_DRY_RUN")
    if (Files.notExists(wpilibjDir))
    {
        println("ERROR: wpilibjDir not found: $wpilibjDir")
    }

    println("Using template year of $year")
    println("yearly templates dir calculated to be:     $ourTemplatesDir")

    if (Files.notExists(ourTemplatesDir)) Files.createDirectory(ourTemplatesDir)

    processAWpiDir(wpiExamplesDir, examplesDirMapping)
    processAWpiDir(wpiTemplatesDir, templatesDirMapping)

    checkForMappingIssues(outdatedMappings, missingMappings)
}

private fun processAWpiDir(theWpiDir: Path, dirMapping: Map<String, String>)
{
    println("Processing: $theWpiDir")
    var currentTemplatesDir = ourTemplatesDir.resolve("PLACEHOLDER")
    var currentTemplateName = ""
    var currentWpiTemplateName = ""
    var currentWpiTemplateDir = theWpiDir
    theWpiDir
        .toFile()
        .walk()
        .onEnter {
            val asPath = it.toPath()
            if (asPath.parent.fileName.toString() == theWpiDir.fileName.toString())
            {
                if (IS_DRY_RUN) println("Entering ${asPath.fileName}")
                currentWpiTemplateDir = asPath
                currentWpiTemplateName = asPath.fileName.toString()
                currentTemplateName = if (dirMapping.containsKey(currentWpiTemplateName))
                {
                    outdatedMappings.remove(currentWpiTemplateName)
                    dirMapping[currentWpiTemplateName]!!
                }
                else
                {
                    missingMappings.add(currentWpiTemplateName)
                    "$currentWpiTemplateName-NEEDS_RENAME"
                }
                currentTemplatesDir = ourTemplatesDir.resolve(currentTemplateName)
                if (!IS_DRY_RUN) Files.createDirectories(currentTemplatesDir)
            }
            true
        }
        .onLeave {

        }
        .forEach {
            val file = it.toPath()
            if (Files.isRegularFile(file) && file.fileName.toString() != "Main.java" && !file.fileName.toString().endsWith("json"))
            {
                val relativeSource = currentWpiTemplateDir.relativize(file)
                val target = currentTemplatesDir.resolve(targetInnerPath).resolve(relativeSource).parent.resolve("${file.fileName}.ftl")
                //println("Will copy: $file\nto:        $target\n")
                if (!IS_DRY_RUN)
                {
                    copyJavaFile(currentWpiTemplateName, theWpiDir.fileName.toString(), file, relativeSource, target)
                }
            }
        }
}

/**
 * @param currentWpiTemplateName The name of the current WPI template, in particular the name of the directory
 * @param wpiTemplateGroupName The template group, such as "examples" or "templates".
 * @param srcFile The source file from the WPI template that is being copied
 * @param relativeSrcPath The relative source path of the file from the template base directory.
 *                        For example `Robot.java`, 'commands\OpenClawCommand.java`, `subsystems\DrivetrainSubsystem.java`
 * @param targetFile The target file the temple is being copied to
 */
fun copyJavaFile(currentWpiTemplateName: String, wpiTemplateGroupName: String, srcFile: Path, relativeSrcPath: Path, targetFile: Path)
{
    var pastHeader = false
    var haveSeenPackageLine = false
    Files.newBufferedReader(srcFile, StandardCharsets.UTF_8).use { reader: BufferedReader ->
        Files.deleteIfExists(targetFile)
        Files.createDirectories(targetFile.parent)
        Files.createFile(targetFile)
        PrintWriter(Files.newOutputStream(targetFile), true, StandardCharsets.UTF_8).use { pw: PrintWriter ->
            pw.printLine(getHeader(relativeSrcPath))
            reader.lines().forEachOrdered { line: String ->
                val trimmedLine: String = line.trim()
                pastHeader = pastHeader || haveSeenPackageLine || trimmedLine.startsWith("import ")
                haveSeenPackageLine = trimmedLine.startsWith("package ")
                if (pastHeader)
                {
                    var newLine = if (trimmedLine.startsWith("import "))
                    {
                        line.replace("edu.wpi.first.wpilibj.$wpiTemplateGroupName.$currentWpiTemplateName", "${'$'}{data.basePackage}")
                    }
                    else if (srcFile.fileName.toString() == "Robot.java" && (line.contains("class Robot ") || line.contains("class Robot{")))
                    {
                        line.replace("class Robot", "class ${'$'}{data.robotClassSimpleName}")
                    }
                    else if (line.contains("how to to "))
                    {
                        line.replace("how to to ", "how to ")
                    }
                    else if (line.contains("Educational robot base class."))
                    {
                        line.replace("Educational robot base class.", "Educational robot base class. Do NOT use for competitions. This is a simple robot used for teaching purposes.")
                    }
                    else if(line.contains("function"))
                    {
                        if (trimmedLine.startsWith("*") || trimmedLine.startsWith("/*") || trimmedLine.startsWith("//"))
                        {
                            if (line.contains("This function is run"))
                            {
                                line.replace("This function is run", "This method is run")
                            }
                            else if (line.contains("This function is called"))
                            {
                                line.replace("This function is called", "This method is called")
                            }
                            else if (line.contains("the run() function"))
                            {
                                line.replace("the run() function", "the run() method")
                            }
                            else if (line.contains("The RobotPeriodic function is called"))
                            {
                                line.replace("The RobotPeriodic function is called", "The RobotPeriodic method is called")
                            }
                            else if (line.contains("call the functions corresponding"))
                            {
                                line.replace("call the functions corresponding", "call the methods corresponding")
                            }
                            else if (line.contains("Main initialization function."))
                            {
                                line.replace("Main initialization function.", "Main initialization method.")
                            }
                            else if (line.contains("specific periodic functions,"))
                            {
                                line.replace("specific periodic functions,", "specific periodic methods,")
                            }
                            else
                            {
                                if (!line.contains("functional"))
                                {
                                    println("WARN: Potentially missed 'function' use rather than method.\n    line: $line\n    srcFile: $srcFile\n    targetFile: $targetFile")
                                }
                                line
                            }
                        }
                        else
                        {
                            line
                        }
                    }
                    else
                    {
                        line
                    }

                    newLine = fixSpellingTypos(newLine)
                    pw.printLine(newLine)
                }
            }
            pw.flush()
        }
    }
}

/** Prints a line using the specified newline character(s). */
private fun PrintWriter.printLine(s: String, newline:String = "\n")
{
    this.print(s)
    this.print(newline)
    this.flush()
}

@Suppress("SpellCheckingInspection")
private fun fixSpellingTypos(theLine: String) : String
{
    var line = theLine
    line = line.replace("channnel", "channel")
    line = line.replace("from the sepoint", "from the setpoint")
    line = line.replace("whose setpoint's change", "whose setpoint change")
    line = line.replace("next timestep", "next timestep")
    line = line.replace("motion profilied robot", "motion profiled robot")
    line = line.replace("This includes the pushbuttons", "This includes the push buttons")
    line = line.replace("propotional turning", "proportional turning")
    line = line.replace("vectorsin relation", "vectors in relation")
    line = line.replace("// qelms", "// QELMs")
    line = line.replace("// relms", "// RELMs")
    line = line.replace("selectcommand.", "SelectCommand")
    line = line.replace("Position contollers", "Position controllers")
    line = line.replace("TRACKWIDTH_METERS", "TRACK_WIDTH_METERS")
    line = line.replace("// Velocity PID's", "// Velocity PIDs")
    return line
}

private fun checkForMappingIssues(
    outdatedMappings: MutableSet<String>,
    missingMappings: MutableList<String>
                                 )
{
    if (outdatedMappings.isNotEmpty())
    {
        println("********************************")
        println("The following WPI Templates mappings are invalid, likely outdated by having been remove from use.")
        println(outdatedMappings)
        println("********************************")
    }

    if (missingMappings.isNotEmpty())
    {
        println("********************************")
        println("The following WPI Templates were missing mappings to our names. They will need to be added and the directory names adjusted.")
        println(missingMappings)
        println("********************************")
    }
}

private const val FRC_WIZARD_TEMPLATES = "frc-wizard-templates"

private fun findOutputDir(): Path
{
    val resource = TemplatesPrepTool::class.java.classLoader.getResource(FRC_WIZARD_TEMPLATES) ?: throw IllegalStateException("Could not find 'frc-wizard-templates' directory as classpath resource")
    // file:/{projectRoot}/out/production/resources/frc-wizard-templates
    var dir = Path.of(resource.toURI())
    while (dir.parent != null && dir.fileName.toString() != "out")
    {
        dir = dir.parent
    }


    val retval =  dir.parent.resolve("src/main/resources/$FRC_WIZARD_TEMPLATES")
    println("$FRC_WIZARD_TEMPLATES dir calculated to be: $retval")
    if (Files.notExists(retval))
    {
        throw IllegalStateException("Calculated $FRC_WIZARD_TEMPLATES dir does not exist: $retval")
    }

    return retval
}

private fun determineTemplatesYear(): Int
{
    val now = LocalDate.now()
    val year = now.year
    return if (now.monthValue >= 8)
    {
        year + 1
    }
    else
    {
        year
    }
}

fun createTemplatesDirMapping() = mapOf(
    "commandbased" to "commandBased",
    "educational" to "educational",
    "robotbaseskeleton" to "robotBaseSkeleton",
    "romicommandbased" to "romiCommand",
    "romieducational" to "romiEducational",
    "romitimed" to "romiTimed",
    "timed" to "timed",
    "timedskeleton" to "timedSkeleton",
                                       )

fun createExamplesDirMapping() = mapOf(
    "addressableled" to "addressableLED",
    "arcadedrive" to "arcadeDrive",
    "arcadedrivexboxcontroller" to "arcadeDriveXboxController",
    "armbot" to "armBot",
    "armbotoffboard" to "armBotOffboard",
    "armsimulation" to "armSimulation",
    "axiscamera" to "axisCameraSample",
    "canpdp" to "canPDP",
    "differentialdrivebot" to "differentialDriveBot",
    "differentialdriveposeestimator" to "DifferentialDrivePoseEstimator",
    "dma" to "DMA",
    "drivedistanceoffboard" to "driveDistanceOffboard",
    "dutycycleencoder" to "dutyCycleEncoder",
    "dutycycleinput" to "dutyCycleInput",
    "elevatorprofiledpid" to "elevatorProfiledPidController",
    "elevatorsimulation" to "elevatorSimulation",
    "elevatortrapezoidprofile" to "elevatorTrapezoidProfiledPid",
    "encoder" to "encoder",
    "frisbeebot" to "frisbeeBot",
    "gearsbot" to "gearsBot",
    "gettingstarted" to "gettingStarted",
    "gyro" to "gyro",
    "gyrodrivecommands" to "gyroDriveCommands",
    "gyromecanum" to "gyroMecanum",
    "hatchbotinlined" to "hatchBotInlined",
    "hatchbottraditional" to "hatchBotTraditional",
    "hidrumble" to "HIDRumble",
    "intermediatevision" to "intermediateVision",
    "mechanism2d" to "Mechanism2D",
    "mecanumbot" to "mecanumBot",
    "mecanumcontrollercommand" to "mecanumControllerCommand",
    "mecanumdrive" to "mecanumDrive",
    "mecanumdriveposeestimator" to "mecanumDrivePoseEstimator",
    "motorcontrol" to "motorController",
    "motorcontrolencoder" to "motorControlWithEncoder",
//    "pacgoat" to "pacGoat",
    "potentiometerpid" to "potentiometerPID",
    "quickvision" to "quickVision",
    "ramsetecommand" to "ramseteCommand",
    "ramsetecontroller" to "ramseteController",
    "relay" to "relay",
    "romireference" to "romiReference",
    "schedulereventlogging" to "schedulerEventLogging",
    "selectcommand" to "selectCommand",
    "shuffleboard" to "shuffleboardSample",
    "simpledifferentialdrivesimulation" to "simpleDifferentialDriveSimulation",
    "solenoid" to "solenoids",
    "statespacearm" to "stateSpaceArm",
    "statespacedifferentialdrivesimulation" to "stateSpaceDifferentialDriveSimulation",
    "statespaceelevator" to "stateSpaceElevator",
    "statespaceflywheel" to "stateSpaceFlywheel",
    "statespaceflywheelsysid" to "stateSpaceFlywheelSysId",
    "swervebot" to "swerveBot",
    "swervecontrollercommand" to "swerveControllerCommand",
    "swervedriveposeestimator" to "swerveDrivePoseEstimator",
    "tankdrive" to "tankDrive",
    "tankdrivexboxcontroller" to "tankDriveXboxController",
    "ultrasonic" to "ultrasonic",
    "ultrasonicpid" to "ultrasonicPID",
                              )


fun getHeader(relativeSrcPath: Path): String
{
    val parent:Path? = relativeSrcPath.parent
    val subPkgDeclaration = if (parent == null) "" else ".${parent.toString().replace('/', '.').replace('\\', '.')}"
    return """
<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:     https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when launching the testing instance of IntelliJ IDEA -->
</#compress>
${'$'}{data.copyright}

package ${'$'}{data.basePackage}$subPkgDeclaration;
""".trimMargin()
}