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

    /** The base name of the template's resource directory. It is highly recommended that this value not include any spaces. */
    fun templateResourcesDirName(): String

    /**
     * Indicates if the project is designed to be a template for bootstrapping a Robot project, and not as an example project.
     * Project Bootstrap Templates have more customization options presented at creation time.
     */
    fun isProjectBootstrapTemplate(): Boolean
    
}


fun projectTemplateDefinitionsFor(version: WpiLibVersion): Array<FrcWizardTemplateDefinition> = projectTemplateDefinitionsFor(version.major)
@Suppress("UNCHECKED_CAST")
fun projectTemplateDefinitionsFor(year: Int): Array<FrcWizardTemplateDefinition>
{
    return when (year)
    {
        2019 -> FrcWizard2019ProjectTemplateDefinition.values() as Array<FrcWizardTemplateDefinition>
        2020 -> FrcWizard2020ProjectTemplateDefinition.values() as Array<FrcWizardTemplateDefinition>
        else -> FrcWizard2020ProjectTemplateDefinition.values() as Array<FrcWizardTemplateDefinition>
    }
}
fun exampleTemplateDefinitionsFor(version: WpiLibVersion): Array<FrcWizardTemplateDefinition> = exampleTemplateDefinitionsFor(version.major)
@Suppress("UNCHECKED_CAST")
fun exampleTemplateDefinitionsFor(year:Int): Array<FrcWizardTemplateDefinition>
{
    return when (year)
    {
        2019 -> FrcWizard2019ProjectTemplateDefinition.values() as Array<FrcWizardTemplateDefinition>
        2020 -> FrcWizard2020ProjectTemplateDefinition.values() as Array<FrcWizardTemplateDefinition>
        else -> FrcWizard2020ProjectTemplateDefinition.values() as Array<FrcWizardTemplateDefinition>
    }
}

enum class FrcWizard2019ProjectTemplateDefinition(
        override val displayName: String,
        @field:Language("HTML") @param:Language("HTML") private val _description: String,
        override val isDeprecated: Boolean,
        override val deprecationAlternative: String?

                                             ) : FrcWizardTemplateDefinition
{
    CommandBased("Command Based Robot", "A robot project that allows robots to be implemented using the command based model to allow complex functionality to be developed from simpler functionality.", false, null),
    Iterative("Iterative Robot", "A robot project that allow robots to be implemented in an iterative manner synced to receiving driver station packets.", true, "Timed Robot"),
    Timed("Timed Robot", "A robot project that allows robots to be implemented in an iterative manner synced to a timer.", false, null),
    TimedSkeleton("Timed Skeleton (Advanced)", "A skeleton (stub) Timed Robot project.", false, null),
    Sample("Sample Robot", "A robot project used for small sample programs or for highly advanced programs with more complete control over program flow. This is <em>not</em> a good choice to use for competition, especially for the inexperienced. Use Timed Robot or Command Based Robot instead.", false, null)
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
        override val isDeprecated: Boolean,
        override val deprecationAlternative: String?

                                                 ) : FrcWizardTemplateDefinition
{
    GettingStarted("Getting Started", "An example project which demonstrates the simplest autonomous and teleoperated routines.", false, null),
    TankDrive("Tank Drive", "Demonstrates the use of the RobotDrive class doing teleop driving with tank steering (i.e. two joysticks).", false, null),
    //ArcadeDrive("Arcade Drive", "Demonstrates the use of the DifferentialDrive class to drive a robot with arcade drive/steering (i.e. single joystick)."),
    MecanumDrive("Mecanum Drive", "Demonstrate the use of the RobotDrive class doing teleop driving with a Mecanum drivetrain.", false, null),
    Ultrasonic("Ultrasonic", "Demonstrates maintaining a set distance using an ultrasonic sensor.", false, null),
    UltrasonicPID("Ultrasonic PID", "Demonstrates maintaining a set distance using an ultrasonic sensor and PID Control.", false, null),
    PotentiometerPID("Potentiometer PID", "Demonstrates the use of a potentiometer and PID control to reach elevator position setpoints.", false, null),
    Gyro("Gyro", "Demonstrates how to drive straight using a gyro sensor.", false, null),
    GyroMecanum("Gyro Mecanum", "Demonstrates how to perform mecanum drive with field oriented controls.", false, null),
    HIDRumble("HID Rumble", "Demonstrates how to make human interface devices rumble.", false, null),
    MotorController("Motor Controller", "Demonstrates controlling a single motor with a joystick.", false, null),
    MotorControlWithEncoder("Motor Control with Encoder", "Demonstrates controlling a single motor with a Joystick and displaying the net movement of the motor using an encoder.", false, null),
    GearsBot("GearsBot", "A fully functional example CommandBased program for WPIs GearsBot robot, ported to the new CommandBased library. This code can run on your computer if it supports simulation.", false, null),
    PacGoat("PacGoat", "A fully functional example CommandBased program for FRC Team 190's 2014 robot. This code can run on your computer if it supports simulation.", false, null),
    SimpleVision("Simple Vision", "Demonstrates the use of the CameraServer class to stream from a USB Webcam without processing the images.", false, null),
    IntermediateVision("Intermediate Vision", "Demonstrates the use of the NIVision class to capture image from a Webcam, process them, and then send them to the dashboard.", false, null),
    AxisCameraSample("Axis Camera Sample", "An example program that acquires images from an Axis network camera and adds some annotation to the image as you might do for showing operators the result of some image recognition, and sends it to the dashboard for display. This demonstrates the use of the AxisCamera class.", false, null),
    ShuffleboardSample("Shuffleboard Sample", "An example program that adds data to various Shuffleboard tabs, demonstrating the Shuffleboard API.", false, null),
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
        override val isDeprecated: Boolean,
        override val deprecationAlternative: String?

                                                 ) : FrcWizardTemplateDefinition
{
    CommandBased("Command Based Robot", "A robot project that allows robots to be implemented using the command based model to allow complex functionality to be developed from simpler functionality.", false, null),
    Iterative("Iterative Robot", "A robot project that allow robots to be implemented in an iterative manner synced to receiving driver station packets.", true, "Timed Robot"),
    Timed("Timed Robot", "A robot project that allows robots to be implemented in an iterative manner synced to a timer.", false, null),
    TimedSkeleton("Timed Skeleton (Advanced)", "A skeleton (stub) Timed Robot project.", false, null),
    Sample("Sample Robot", "A robot project used for small sample programs or for highly advanced programs with more complete control over program flow. This is <em>not</em> a good choice to use for competition, especially for the inexperienced. Use Timed Robot or Command Based Robot instead.", false, null)
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
        override val isDeprecated: Boolean,
        override val deprecationAlternative: String?

                                                 ) : FrcWizardTemplateDefinition
{
    GettingStarted("Getting Started", "An example project which demonstrates the simplest autonomous and teleoperated routines.", false, null),
    TankDrive("Tank Drive", "Demonstrates the use of the RobotDrive class doing teleop driving with tank steering (i.e. two joysticks).", false, null),
    //ArcadeDrive("Arcade Drive", "Demonstrates the use of the DifferentialDrive class to drive a robot with arcade drive/steering (i.e. single joystick)."),
    MecanumDrive("Mecanum Drive", "Demonstrate the use of the RobotDrive class doing teleop driving with a Mecanum drivetrain.", false, null),
    Ultrasonic("Ultrasonic", "Demonstrates maintaining a set distance using an ultrasonic sensor.", false, null),
    UltrasonicPID("Ultrasonic PID", "Demonstrates maintaining a set distance using an ultrasonic sensor and PID Control.", false, null),
    PotentiometerPID("Potentiometer PID", "Demonstrates the use of a potentiometer and PID control to reach elevator position setpoints.", false, null),
    Gyro("Gyro", "Demonstrates how to drive straight using a gyro sensor.", false, null),
    GyroMecanum("Gyro Mecanum", "Demonstrates how to perform mecanum drive with field oriented controls.", false, null),
    HIDRumble("HID Rumble", "Demonstrates how to make human interface devices rumble.", false, null),
    MotorController("Motor Controller", "Demonstrates controlling a single motor with a joystick.", false, null),
    MotorControlWithEncoder("Motor Control with Encoder", "Demonstrates controlling a single motor with a Joystick and displaying the net movement of the motor using an encoder.", false, null),
    GearsBot("GearsBot", "A fully functional example CommandBased program for WPIs GearsBot robot, ported to the new CommandBased library. This code can run on your computer if it supports simulation.", false, null),
    PacGoat("PacGoat", "A fully functional example CommandBased program for FRC Team 190's 2014 robot. This code can run on your computer if it supports simulation.", false, null),
    SimpleVision("Simple Vision", "Demonstrates the use of the CameraServer class to stream from a USB Webcam without processing the images.", false, null),
    IntermediateVision("Intermediate Vision", "Demonstrates the use of the NIVision class to capture image from a Webcam, process them, and then send them to the dashboard.", false, null),
    AxisCameraSample("Axis Camera Sample", "An example program that acquires images from an Axis network camera and adds some annotation to the image as you might do for showing operators the result of some image recognition, and sends it to the dashboard for display. This demonstrates the use of the AxisCamera class.", false, null),
    ShuffleboardSample("Shuffleboard Sample", "An example program that adds data to various Shuffleboard tabs, demonstrating the Shuffleboard API.", false, null),
    ;


    override val description: String
        @get:Language("HTML") get() = "<html>$_description</html>"

    override val displayNameAndDescription: String
        @get:Language("HTML") get() = "<html><strong>$displayName</strong> : ${_description}${createDeprecationNotice(this)}</html>"

    override fun toString(): String = "${displayName}${if (this.isDeprecated) " (Deprecated)" else ""}"

    override fun templateResourcesDirName(): String = if (name.length >= 2 && Character.isUpperCase(name[0]) && Character.isUpperCase(name[1])) name else name.decapitalize()

    override fun isProjectBootstrapTemplate(): Boolean = false
}

fun createDeprecationNotice(templateDefinition: FrcWizardTemplateDefinition): String
{
    return if (templateDefinition.isDeprecated)
    {
        val alt = if (templateDefinition.deprecationAlternative == null) "" else """: use "${templateDefinition.deprecationAlternative}" instead."""
        "<br><strong>DEPRECATED${alt}</strong>"
    }
    else
    {
        ""
    }
}
