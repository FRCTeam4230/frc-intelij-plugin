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

@file:Suppress("HtmlRequiredLangAttribute")

package net.javaru.iip.frc.wizard

import net.javaru.iip.frc.wpilib.version.WpiLibVersion
import org.intellij.lang.annotations.Language


interface FrcWizardTemplateDefinition
{
    /** Returns the display name for use in the UI's list of available templates. */
    val displayName: String
    
    /* 
     * A 1 or 2 sentence description of the template. Implementations must return a value inside `<html>` tags. 
     * Inner HTML tags such as `<em>` are allowed. Implementations must return some value and never `null` or an
     * empty or blank value. Minimally, return the display name or something to the effect of: "${displayName} Template"
     */
    @get:Language("HTML")
    val description: String
    
    /** Returns the displayName and Description in the format:
     * 
     * `<html><strong>{displayName}</strong> : {description}</html>`
     * 
     * Implementations must be careful to not cause double `<html>` tags when concatenating the description.
     * A recommended implementation is to store the description as a (final) variable without the `<html>` tags (but inner tags are OK).
     * Then implement the `description` getter to return the value wrapped in HTML tags. and then use the description field (and not the
     * getter) for this getter, concatenating with the `displayName`, a colon, and `description` inside `<html>` tags to create this value
     */
    @get:Language("HTML")
    val displayNameAndDescription: String
    
    val isDeprecated: Boolean
    
    val deprecationAlternative: String?
    
    /** 
     * The `commandVersion` was introduced in 2020 when they created a new Command Based Robot implementation. A value of `1` 
     * means it uses the old command classes from the `edu.wpi.first.wpilibj` package in the `wpilibOldCommands-java-2020-x.x.jar` 
     * library and a value of `2` means it uses the new command classes from the `edu.wpi.first.wpilibj2` package (note the 2) in 
     * the `wpilibNewCommands-java-2020-x.x.jar` library. There is a JSON file in the `vendordeps` directory that indicates which 
     * dependency is pulled in (i.e. the wpilibOldCommands or wpilibNewCommands JAR). unfortunately, they also chose to make the 
     * name of that JSON file different (although ultimately I do not think it matters), so we can't purely handle this in the template.
     * For now, we are setting 2019 templates to a 0 as it may be useful for filtering in the pending new design.
     */
    val commandVersion: Int

    /** The base name of the template's resource directory. It is highly recommended that this value not include any spaces. */
    fun templateResourcesDirName(): String

    /**
     * Indicates if the project is designed to be a template for bootstrapping a Robot project, and not as an example project.
     * Project Bootstrap Templates have more customization options presented at creation time.
     */
    fun isProjectBootstrapTemplate(): Boolean
    
    fun id(): String = "${templateResourcesDirName()}--$displayName"
}


fun projectTemplateDefinitionsFor(version: WpiLibVersion): Array<FrcWizardTemplateDefinition> = projectTemplateDefinitionsFor(version.frcYear)
@Suppress("UNCHECKED_CAST")
fun projectTemplateDefinitionsFor(year: Int): Array<FrcWizardTemplateDefinition>
{
    return when (year)
    {
        2019 -> FrcWizard2019ProjectTemplateDefinition.values() as Array<FrcWizardTemplateDefinition>
        2020 -> FrcWizard2020ProjectTemplateDefinition.values() as Array<FrcWizardTemplateDefinition>
        2021 -> FrcWizard2021ProjectTemplateDefinition.values() as Array<FrcWizardTemplateDefinition>
        else -> FrcWizard2021ProjectTemplateDefinition.values() as Array<FrcWizardTemplateDefinition>
    }
}
fun exampleTemplateDefinitionsFor(version: WpiLibVersion): Array<FrcWizardTemplateDefinition> = exampleTemplateDefinitionsFor(version.frcYear)
@Suppress("UNCHECKED_CAST")
fun exampleTemplateDefinitionsFor(year:Int): Array<FrcWizardTemplateDefinition>
{
    return when (year)
    {
        2019 -> FrcWizard2019ExampleTemplateDefinition.values() as Array<FrcWizardTemplateDefinition>
        2020 -> FrcWizard2020ExampleTemplateDefinition.values() as Array<FrcWizardTemplateDefinition>
        2021 -> FrcWizard2021ExampleTemplateDefinition.values() as Array<FrcWizardTemplateDefinition>
        else -> FrcWizard2021ExampleTemplateDefinition.values() as Array<FrcWizardTemplateDefinition>
    }
}

fun createDeprecationNotice(templateDefinition: FrcWizardTemplateDefinition): String
{
    return if (templateDefinition.isDeprecated)
    {
        val alt = if (templateDefinition.deprecationAlternative == null) "" else """: use "${templateDefinition.deprecationAlternative}" instead."""
        "<br><strong>DEPRECATED${alt}</strong>"
    } else
    {
        ""
    }
}


// For "official" descriptions, see:
//   https://github.com/wpilibsuite/allwpilib/blob/master/wpilibjExamples/src/main/java/edu/wpi/first/wpilibj/examples/examples.json
//   https://github.com/wpilibsuite/allwpilib/blob/master/wpilibjExamples/src/main/java/edu/wpi/first/wpilibj/templates/templates.json


enum class FrcWizard2019ProjectTemplateDefinition(
        override val displayName: String,
        @field:Language("HTML") @param:Language("HTML") private val _description: String,
        override val isDeprecated: Boolean = false,
        override val deprecationAlternative: String? = null,
        override val commandVersion: Int = 0
                                             ) : FrcWizardTemplateDefinition
{
    CommandBased("Command Based Robot", "A robot project that allows robots to be implemented using the command based model to allow complex functionality to be developed from simpler functionality."),
    Iterative("Iterative Robot", "A robot project that allow robots to be implemented in an iterative manner synced to receiving driver station packets.", true, "Timed Robot"),
    Timed("Timed Robot", "A robot project that allows robots to be implemented in an iterative manner synced to a timer."),
    TimedSkeleton("Timed Skeleton (Advanced)", "A skeleton (stub) Timed Robot project."),
    Sample("Sample Robot", "A robot project used for small sample programs or for highly advanced programs with more complete control over program flow. This is <em>not</em> a good choice to use for competition, especially for the inexperienced. Use Timed Robot or Command Based Robot instead.")
    ;


    override val description: String
        @get:Language("HTML") get() = "<html>$_description</html>"

    override val displayNameAndDescription: String
        @get:Language("HTML") get() = "<html><strong>$displayName</strong> : ${_description}${createDeprecationNotice(this)}</html>"

    override fun toString(): String = "${displayName}${if (this.isDeprecated) " (Deprecated)" else ""}"
   
    override fun templateResourcesDirName(): String = if (name.length >= 2 && Character.isUpperCase(name[0]) && Character.isUpperCase(name[1])) name else name.decapitalize()

    override fun isProjectBootstrapTemplate(): Boolean = true
}

enum class FrcWizard2019ExampleTemplateDefinition(
        override val displayName: String,
        @field:Language("HTML") @param:Language("HTML") private val _description: String,
        override val isDeprecated: Boolean = false,
        override val deprecationAlternative: String? = null,
        override val commandVersion: Int = 0
                                                 ) : FrcWizardTemplateDefinition
{
    GettingStarted("Getting Started", "An example project which demonstrates the simplest autonomous and teleoperated routines."),
    TankDrive("Tank Drive", "Demonstrates the use of the RobotDrive class doing teleop driving with tank drive/steering (i.e. two joysticks)."),
    ArcadeDrive("Arcade Drive", "Demonstrates the use of the DifferentialDrive class to drive a robot with arcade drive/steering (i.e. single joystick)."),
    MecanumDrive("Mecanum Drive", "Demonstrate the use of the RobotDrive class doing teleop driving with a Mecanum drivetrain."),
    Ultrasonic("Ultrasonic", "Demonstrates maintaining a set distance using an ultrasonic sensor."),
    UltrasonicPID("Ultrasonic PID", "Demonstrates maintaining a set distance using an ultrasonic sensor and PID Control."),
    PotentiometerPID("Potentiometer PID", "Demonstrates the use of a potentiometer and PID control to reach elevator position setpoints."),
    Gyro("Gyro", "Demonstrates how to drive straight using a gyro sensor."),
    GyroMecanum("Gyro Mecanum", "Demonstrates how to perform mecanum drive with field oriented controls."),
    HIDRumble("HID Rumble", "Demonstrates how to make human interface devices rumble."),
    MotorController("Motor Controller", "Demonstrates controlling a single motor with a joystick."),
    MotorControlWithEncoder("Motor Control with Encoder", "Demonstrates controlling a single motor with a Joystick and displaying the net movement of the motor using an encoder."),
    GearsBot("GearsBot", "A fully functional example Command Based program for WPIs GearsBot robot, ported to the Command Based library. This code can run on your computer if it supports simulation."),
    PacGoat("PacGoat", "A fully functional example Command Based program for FRC Team 190's 2014 robot. This code can run on your computer if it supports simulation."),
    SimpleVision("Simple Vision", "Demonstrates the use of the CameraServer class to stream from a USB Webcam without processing the images."),
    IntermediateVision("Intermediate Vision", "Demonstrates the use of the NIVision class to capture image from a Webcam, process them, and then send them to the dashboard."),
    AxisCameraSample("Axis Camera Sample", "An example program that acquires images from an Axis network camera and adds some annotation to the image as you might do for showing operators the result of some image recognition, and sends it to the dashboard for display. This demonstrates the use of the AxisCamera class."),
    ShuffleboardSample("Shuffleboard Sample", "An example program that adds data to various Shuffleboard tabs, demonstrating the Shuffleboard API."),
    ;   


    override val description: String
        @get:Language("HTML") get() = "<html>$_description</html>"

    override val displayNameAndDescription: String
        @get:Language("HTML") get() = "<html><strong>$displayName</strong> : ${_description}${createDeprecationNotice(this)}</html>"

    override fun toString(): String = "${displayName}${if (this.isDeprecated) " (Deprecated)" else ""}"

    override fun templateResourcesDirName(): String = if (name.length >= 2 && Character.isUpperCase(name[0]) && Character.isUpperCase(name[1])) name else name.decapitalize()

    override fun isProjectBootstrapTemplate(): Boolean = false
}

enum class FrcWizard2020ProjectTemplateDefinition(
        override val displayName: String,
        @field:Language("HTML") @param:Language("HTML") private val _description: String,
        override val isDeprecated: Boolean = false,
        override val deprecationAlternative: String? = null,
        override val commandVersion: Int = 1
                                                 ) : FrcWizardTemplateDefinition
{
    // Old Command Based docs: https://docs.wpilib.org/en/latest/docs/software/old-commandbased/index.html
    // New Command Based docs: https://docs.wpilib.org/en/latest/docs/software/commandbased/index.html

    CommandBased("Command Based (v2) Robot", "A robot project for coding robots using version 2 of the Command Based framework/API introduced in 2020. Command Based robots allow complex functionality to be developed from simpler functionality/components. This is the updated for 2020 Command Based Robot, using classes from the <tt>edu.wpi.first.wpilibj<strong>2</strong></tt> package from the <tt>wpilibNewCommands-java-2020-x.x.jar</tt> See <em>Command-Based Programming</em> in the WPI Lib Docs for more information and details on the changes.", commandVersion = 2),
    CommandBasedOld("Command Based (v1) Robot", "A robot project for coding robots using using version 1 of the Command Based framework/API introduced in 2012. Command Based robots allow complex functionality to be developed from simpler functionality/components. This is the original Command Based Robot introduced in 2012, using classes from the <tt>edu.wpi.first.wpilibj</tt> package from the <tt>wpilibOldCommands-java-2020-x.x.jar</tt>", commandVersion = 1),
    Timed("Timed Robot", "A robot project that allows robots to be implemented in an iterative manner synced to a timer."),
    TimedSkeleton("Timed Skeleton (Advanced)", "A skeleton (stub) Timed Robot project for advanced programmers."),
    RobotBaseSkeleton("RobotBase Skeleton (Advanced)", "A skeleton (stub) for RobotBase, intended for <strong>highly advanced/experienced</strong> programmers, that provides more complete control over program flow.")
    ;


    override val description: String
        @get:Language("HTML") get() = "<html>$_description</html>"

    override val displayNameAndDescription: String
        @get:Language("HTML") get() = "<html><strong>$displayName</strong> : ${_description}${createDeprecationNotice(this)}</html>"

    override fun toString(): String = "${displayName}${if (this.isDeprecated) " (Deprecated)" else ""}"

    override fun templateResourcesDirName(): String = if (name.length >= 2 && Character.isUpperCase(name[0]) && Character.isUpperCase(name[1])) name else name.decapitalize()

    override fun isProjectBootstrapTemplate(): Boolean = true
}

enum class FrcWizard2020ExampleTemplateDefinition(
        override val displayName: String,
        @field:Language("HTML") @param:Language("HTML") private val _description: String,
        override val isDeprecated: Boolean = false,
        override val deprecationAlternative: String? = null,
        override val commandVersion: Int = 1
                                                 ) : FrcWizardTemplateDefinition
{
    GettingStarted("Getting Started", "An example project which demonstrates the simplest autonomous and teleoperated routines."),
    TankDrive("Tank Drive", "Demonstrates the use of the RobotDrive class doing teleop driving with tank steering (i.e. two joysticks)."),
    ArcadeDrive("Arcade Drive", "Demonstrates the use of the DifferentialDrive class to drive a robot with arcade drive/steering (i.e. single joystick).."),
    MecanumDrive("Mecanum Drive", "Demonstrates the use of the RobotDrive class doing teleop driving with a Mecanum drivetrain."),
    PdpCanMonitoring("PDP CAN Monitoring", "Demonstrates using CAN to monitor the voltage, current, and temperature in the Power Distribution Panel (PDP)."),
    Solenoids("Solenoids", "Demonstrates controlling a single and double solenoid from Joystick buttons."),
    Encoder("Encoder", "Demonstrates displaying the value of a quadrature encoder on the SmartDashboard."),
    Relay("Relay", "Demonstrates controlling a Relay from Joystick buttons."),
    Ultrasonic("Ultrasonic", "Demonstrates maintaining a set distance using an ultrasonic sensor."),
    UltrasonicPID("Ultrasonic PID", "Demonstrates maintaining a set distance using an ultrasonic sensor and PID Control."),
    PotentiometerPID("Potentiometer PID", "Demonstrates the use of a potentiometer and PID control to reach elevator position setpoints."),
    ElevatorTrapezoidProfiledPid("Elevator with trapezoid profiled PID", "An example to demonstrate the use of an encoder and trapezoid profiled PID control to reach elevator position setpoints."),
    ElevatorProfiledPidController("Elevator with profiled PID controller", "An example to demonstrate the use of an encoder and profiled PID control to reach elevator position setpoints."),
    Gyro("Gyro", "Demonstrates how to drive straight using a gyro sensor."),
    GyroMecanum("Gyro Mecanum", "Demonstrates how to perform mecanum drive with field oriented controls."),
    HIDRumble("HID Rumble", "Demonstrates how to make human interface devices rumble."),
    MotorController("Motor Controller", "Demonstrates controlling a single motor with a joystick."),
    MotorControlWithEncoder("Motor Control with Encoder", "Demonstrates controlling a single motor with a Joystick and displaying the net movement of the motor using an encoder."),
    GearsBot1("GearsBot v1", "A fully functional example version 1 Command Based program for WPIs GearsBot robot. Uses version 1 of the Command Based framework/API introduced in 2012. This code can run on your computer if it supports simulation.", commandVersion = 1),
    GearsBot2("GearsBot v2", "A fully functional example version 2 Command Based program for WPIs GearsBot robot. Uses version 2 of the Command Based framework/API introduced in 2020. This code can run on your computer if it supports simulation.", commandVersion = 2),
    PacGoat("PacGoat", "A fully functional example Command Based (version 1) program for FRC Team 190's 2014 robot. Uses version 1 of the Command Based framework/API introduced in 2012. This code can run on your computer if it supports simulation.", commandVersion = 1),
    SimpleVision("Simple Vision", "Demonstrates the use of the CameraServer class to stream from a USB Webcam without processing the images."),
    IntermediateVision("Intermediate Vision", "Demonstrates the use of the NIVision class to capture image from a Webcam, process them, and then send them to the dashboard."),
    AxisCameraSample("Axis Camera Sample", "An example program that acquires images from an Axis network camera and adds some annotation to the image as you might do for showing operators the result of some image recognition, and sends it to the dashboard for display. This demonstrates the use of the AxisCamera class."),
    ShuffleboardSample("Shuffleboard Sample", "An example program that adds data to various Shuffleboard tabs, demonstrating the Shuffleboard API."),
    TraditionalHatchbot("'Traditional' Hatchbot", "A fully-functional command-based hatch bot for the 2019 game using the new experimental (version 2) command framework/API.  Written in the 'traditional' style, i.e. commands are given their own classes.", commandVersion = 2),
    InlinedHatchbot("'Inlined' Hatchbot", "A fully-functional command-based hatch bot for the 2019 game using the new experimental (version 2) command framework/API.  Written in the 'inlined' style, i.e. many commands are defined inline with lambdas.", commandVersion = 2),
    SelectCommand("Select Command Example", "An example showing how to use the SelectCommand class from the experimental (version 2) command framework/API.", commandVersion = 2),
    SchedulerEventLogging("Scheduler Event Logging", "An example showing how to use Shuffleboard to log Command events from the CommandScheduler in the experimental (version 2) command framework/API.", commandVersion = 2),
    FrisbeeBot("FrisbeeBot", "An example robot project for a simple frisbee shooter for the 2013 FRC game, Ultimate Ascent, demonstrating use of PID functionality in the experimental (version 2) command framework/API.", commandVersion = 2),
    GyroDriveCommands("Gyro Drive Commands", "An example (version 2) command-based robot project demonstrating simple PID functionality utilizing a gyroscope to keep a robot driving straight and to turn to specified angles.", commandVersion = 2),
    SwerveBot("SwerveBot", "An example program for a swerve drive that uses swerve drive kinematics and odometry.", commandVersion = 2),
    MecanumBot("MecanumBot", "An example program for a mecanum drive that uses mecanum drive kinematics and odometry.", commandVersion = 2),
    DifferentialDriveBot("DifferentialDriveBot", "An example program for a differential drive that uses differential drive kinematics and odometry.", commandVersion = 2),
    RamseteCommand("RamseteCommand", "An example (version 2) command-based robot demonstrating the use of a RamseteCommand to follow a pregenerated trajectory.", commandVersion = 2),
    ArcadeDriveXboxController("Arcade Drive Xbox Controller", "Demonstrates the use of the DifferentialDrive class to drive a robot with Arcade Drive and an Xbox Controller"),
    TankDriveXboxController("Tank Drive Xbox Controller", "Demonstrates the use of the DifferentialDrive class to drive a robot with Tank Drive and an Xbox Controller"),
    DutyCycleEncoder("Duty Cycle Encoder", "Demonstrates the use of the Duty Cycle Encoder class.", commandVersion = 2),
    DutyCycleInput("Duty Cycle Input", "Demonstrates the use of the Duty Cycle class.", commandVersion = 2),
    AddressableLED("Addressable LED", "Demonstrates the use of the Addressable LED class.", commandVersion = 2),
    ArmBot("ArmBot", "An example command-based robot demonstrating the use of a ProfiledPIDSubsystem to control an arm.", commandVersion = 2),
    ArmBotOffboard("ArmBot Offboard", "An example command-based robot demonstrating the use of a TrapezoidProfileSubsystem to control an arm with an offboard PID.", commandVersion = 2),
    DriveDistanceOffboard("Drive Distance Offboard", "An example command-based robot demonstrating the use of a TrapezoidProfileCommand to drive a robot a set distance with offboard PID on the drive.", commandVersion = 2),
    MecanumControllerCommand("MecanumControllerCommand", "An example command-based robot demonstrating the use of a MecanumControllerCommand to follow a pregenerated trajectory.", commandVersion = 2),
    SwerveControllerCommand("SwerveControllerCommand", "An example command-based robot demonstrating the use of a SwerveControllerCommand to follow a pregenerated trajectory.", commandVersion = 2),
    ;


    override val description: String
        @get:Language("HTML") get() = "<html>$_description</html>"

    override val displayNameAndDescription: String
        @get:Language("HTML") get() = "<html><strong>$displayName</strong> : ${_description}${createDeprecationNotice(this)}</html>"

    override fun toString(): String = "${displayName}${if (this.isDeprecated) " (Deprecated)" else ""}"

    override fun templateResourcesDirName(): String = if (name.length >= 2 && Character.isUpperCase(name[0]) && Character.isUpperCase(name[1])) name else name.decapitalize()

    override fun isProjectBootstrapTemplate(): Boolean = false
}

enum class FrcWizard2021ProjectTemplateDefinition(
    override val displayName: String,
    @field:Language("HTML") @param:Language("HTML") private val _description: String,
    override val isDeprecated: Boolean = false,
    override val deprecationAlternative: String? = null,
    override val commandVersion: Int = 1
                                                 ) : FrcWizardTemplateDefinition
{
    // Old Command Based docs: https://docs.wpilib.org/en/latest/docs/software/old-commandbased/index.html
    // New Command Based docs: https://docs.wpilib.org/en/latest/docs/software/commandbased/index.html
    CommandBased(
        "Command Based (v2) Robot",
        "A robot project for coding robots using version 2 of the Command Based framework/API introduced in 2020. Command Based robots allow complex functionality to be developed from simpler functionality/components. This is the updated for 2020 Command Based Robot, using classes from the <tt>edu.wpi.first.wpilibj<strong>2</strong></tt> package from the <tt>wpilibNewCommands-java-2020-x.x.jar</tt> See the <em>Command-Based Programming</em> section of the WPI Lib Docs for more information and details on programming Command Based robots.",
        commandVersion = 2
                ),
    CommandBasedOld(
        "Command Based (v1) Robot (aka Old Command)",
        "A robot project for coding robots using using version 1 of the Command Based framework/API introduced in 2012. Command Based robots allow complex functionality to be developed from simpler functionality/components. This is the original Command Based Robot introduced in 2012, using classes from the <tt>edu.wpi.first.wpilibj</tt> package from the <tt>wpilibOldCommands-java-2020-x.x.jar</tt> Version 1 has been deprecated, and new robot projects are strongly encouraged to use the <em>Command Based (v2) Robot</em> Template. See the <em>Command-Based Programming</em> section of the WPI Lib Docs for more information and details.",
        commandVersion = 1,
        isDeprecated = true
                   ),
    Timed("Timed Robot", "A robot project that allows robots to be implemented in an iterative manner synced to a timer."),
    TimedSkeleton("Timed Skeleton (Advanced)", "A skeleton (stub) Timed Robot project for advanced programmers."),
    RobotBaseSkeleton(
        "RobotBase Skeleton (Advanced)",
        "A skeleton (stub) for RobotBase, intended for <strong>highly advanced/experienced</strong> programmers, that provides more complete control over program flow."
                     ),
    RomiCommand("Romi - Command Robot", "Romi Robot using (version 2 of) the Command Based framework/API, allowing complex functionality to be developed from simpler functionality/components. See the <em>Romi Robot</em> section of the WPI Lib Docs for more information and details.", commandVersion = 2),
    RomiTimed("Romi - Timed Robot", "Romi Robot using TimedRobot as the base class, allowing robots to be implemented in an iterative manner synced to a timer. See the <em>Romi Robot</em> section of the WPI Lib Docs for more information and details.")

    ;


    override val description: String
        @get:Language("HTML") get() = "<html>$_description</html>"

    override val displayNameAndDescription: String
        @get:Language("HTML") get() = "<html><strong>$displayName</strong> : ${_description}${createDeprecationNotice(this)}</html>"

    override fun toString(): String = "${displayName}${if (this.isDeprecated) " (Deprecated)" else ""}"

    override fun templateResourcesDirName(): String = if (name.length >= 2 && Character.isUpperCase(name[0]) && Character.isUpperCase(name[1])) name else name.decapitalize()

    override fun isProjectBootstrapTemplate(): Boolean = true
}

enum class FrcWizard2021ExampleTemplateDefinition(
    override val displayName: String,
    @field:Language("HTML") @param:Language("HTML") private val _description: String,
    override val isDeprecated: Boolean = false,
    override val deprecationAlternative: String? = null,
    override val commandVersion: Int = 1
                                                 ) : FrcWizardTemplateDefinition
{
    GettingStarted("Getting Started", "An example project which demonstrates the simplest autonomous and teleoperated routines."),
    TankDrive("Tank Drive", "Demonstrates the use of the RobotDrive class doing teleop driving with tank steering (i.e. two joysticks)."),
    ArcadeDrive("Arcade Drive", "Demonstrates the use of the DifferentialDrive class to drive a robot with arcade drive/steering (i.e. single joystick).."),
    MecanumDrive("Mecanum Drive", "Demonstrates the use of the RobotDrive class doing teleop driving with a Mecanum drivetrain."),
    PdpCanMonitoring("PDP CAN Monitoring", "Demonstrates using CAN to monitor the voltage, current, and temperature in the Power Distribution Panel (PDP)."),
    Solenoids("Solenoids", "Demonstrates controlling a single and double solenoid from Joystick buttons."),
    Encoder("Encoder", "Demonstrates displaying the value of a quadrature encoder on the SmartDashboard."),
    Relay("Relay", "Demonstrates controlling a Relay from Joystick buttons."),
    Ultrasonic("Ultrasonic", "Demonstrates maintaining a set distance using an ultrasonic sensor."),
    UltrasonicPID("Ultrasonic PID", "Demonstrates maintaining a set distance using an ultrasonic sensor and PID Control."),
    PotentiometerPID("Potentiometer PID", "Demonstrates the use of a potentiometer and PID control to reach elevator position setpoints."),
    ElevatorTrapezoidProfiledPid("Elevator with trapezoid profiled PID", "An example to demonstrate the use of an encoder and trapezoid profiled PID control to reach elevator position setpoints."),
    ElevatorProfiledPidController("Elevator with profiled PID controller", "An example to demonstrate the use of an encoder and profiled PID control to reach elevator position setpoints."),
    Gyro("Gyro", "Demonstrates how to drive straight using a gyro sensor."),
    GyroMecanum("Gyro Mecanum", "Demonstrates how to perform mecanum drive with field oriented controls."),
    HIDRumble("HID Rumble", "Demonstrates how to make human interface devices rumble."),
    MotorController("Motor Controller", "Demonstrates controlling a single motor with a joystick."),
    MotorControlWithEncoder("Motor Control with Encoder", "Demonstrates controlling a single motor with a Joystick and displaying the net movement of the motor using an encoder."),
    GearsBot1(
        "GearsBot v1",
        "A fully functional example version 1 Command Based program for WPIs GearsBot robot. Uses version 1 of the Command Based framework/API introduced in 2012. This code can run on your computer if it supports simulation.",
        commandVersion = 1
             ),
    GearsBot2(
        "GearsBot v2",
        "A fully functional example version 2 Command Based program for WPIs GearsBot robot. Uses version 2 of the Command Based framework/API introduced in 2020. This code can run on your computer if it supports simulation.",
        commandVersion = 2
             ),
    PacGoat(
        "PacGoat",
        "A fully functional example Command Based (version 1) program for FRC Team 190's 2014 robot. Uses version 1 of the Command Based framework/API introduced in 2012. This code can run on your computer if it supports simulation.",
        commandVersion = 1
           ),
    SimpleVision("Simple Vision", "Demonstrates the use of the CameraServer class to stream from a USB Webcam without processing the images."),
    IntermediateVision("Intermediate Vision", "Demonstrates the use of the NIVision class to capture image from a Webcam, process them, and then send them to the dashboard."),
    AxisCameraSample(
        "Axis Camera Sample",
        "An example program that acquires images from an Axis network camera and adds some annotation to the image as you might do for showing operators the result of some image recognition, and sends it to the dashboard for display. This demonstrates the use of the AxisCamera class."
                    ),
    ShuffleboardSample("Shuffleboard Sample", "An example program that adds data to various Shuffleboard tabs, demonstrating the Shuffleboard API."),
    TraditionalHatchbot(
        "'Traditional' Hatchbot",
        "A fully-functional command-based hatch bot for the 2019 game using the new experimental (version 2) command framework/API.  Written in the 'traditional' style, i.e. commands are given their own classes.",
        commandVersion = 2
                       ),
    InlinedHatchbot(
        "'Inlined' Hatchbot",
        "A fully-functional command-based hatch bot for the 2019 game using the new experimental (version 2) command framework/API.  Written in the 'inlined' style, i.e. many commands are defined inline with lambdas.",
        commandVersion = 2
                   ),
    SelectCommand("Select Command Example", "An example showing how to use the SelectCommand class from the experimental (version 2) command framework/API.", commandVersion = 2),
    SchedulerEventLogging(
        "Scheduler Event Logging",
        "An example showing how to use Shuffleboard to log Command events from the CommandScheduler in the experimental (version 2) command framework/API.",
        commandVersion = 2
                         ),
    FrisbeeBot(
        "FrisbeeBot",
        "An example robot project for a simple frisbee shooter for the 2013 FRC game, Ultimate Ascent, demonstrating use of PID functionality in the experimental (version 2) command framework/API.",
        commandVersion = 2
              ),
    GyroDriveCommands(
        "Gyro Drive Commands",
        "An example (version 2) command-based robot project demonstrating simple PID functionality utilizing a gyroscope to keep a robot driving straight and to turn to specified angles.",
        commandVersion = 2
                     ),
    SwerveBot("SwerveBot", "An example program for a swerve drive that uses swerve drive kinematics and odometry.", commandVersion = 2),
    MecanumBot("MecanumBot", "An example program for a mecanum drive that uses mecanum drive kinematics and odometry.", commandVersion = 2),
    DifferentialDriveBot("DifferentialDriveBot", "An example program for a differential drive that uses differential drive kinematics and odometry.", commandVersion = 2),
    RamseteCommand("RamseteCommand", "An example (version 2) command-based robot demonstrating the use of a RamseteCommand to follow a pregenerated trajectory.", commandVersion = 2),
    ArcadeDriveXboxController("Arcade Drive Xbox Controller", "Demonstrates the use of the DifferentialDrive class to drive a robot with Arcade Drive and an Xbox Controller"),
    TankDriveXboxController("Tank Drive Xbox Controller", "Demonstrates the use of the DifferentialDrive class to drive a robot with Tank Drive and an Xbox Controller"),
    DutyCycleEncoder("Duty Cycle Encoder", "Demonstrates the use of the Duty Cycle Encoder class.", commandVersion = 2),
    DutyCycleInput("Duty Cycle Input", "Demonstrates the use of the Duty Cycle class.", commandVersion = 2),
    AddressableLED("Addressable LED", "Demonstrates the use of the Addressable LED class.", commandVersion = 2),
    ArmBot("ArmBot", "An example command-based robot demonstrating the use of a ProfiledPIDSubsystem to control an arm.", commandVersion = 2),
    ArmBotOffboard("ArmBot Offboard", "An example command-based robot demonstrating the use of a TrapezoidProfileSubsystem to control an arm with an offboard PID.", commandVersion = 2),
    DriveDistanceOffboard(
        "Drive Distance Offboard",
        "An example command-based robot demonstrating the use of a TrapezoidProfileCommand to drive a robot a set distance with offboard PID on the drive.",
        commandVersion = 2
                         ),
    MecanumControllerCommand("MecanumControllerCommand", "An example command-based robot demonstrating the use of a MecanumControllerCommand to follow a pregenerated trajectory.", commandVersion = 2),
    SwerveControllerCommand("SwerveControllerCommand", "An example command-based robot demonstrating the use of a SwerveControllerCommand to follow a pregenerated trajectory.", commandVersion = 2),
    ;


    override val description: String
        @get:Language("HTML") get() = "<html>$_description</html>"

    override val displayNameAndDescription: String
        @get:Language("HTML") get() = "<html><strong>$displayName</strong> : ${_description}${createDeprecationNotice(this)}</html>"

    override fun toString(): String = "${displayName}${if (this.isDeprecated) " (Deprecated)" else ""}"

    override fun templateResourcesDirName(): String = if (name.length >= 2 && Character.isUpperCase(name[0]) && Character.isUpperCase(name[1])) name else name.decapitalize()

    override fun isProjectBootstrapTemplate(): Boolean = false
}