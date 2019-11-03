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

    /** The base name of the template's resource directory. It is highly recommended that this value not include any spaces. */
    fun templateResourcesDirName(): String

    /**
     * Indicates if the project is designed to be a template for bootstrapping a Robot project, and not as an example project.
     * Project Bootstrap Templates have more customization options presented at creation time.
     */
    fun isProjectBootstrapTemplate(): Boolean
}

enum class FrcWizardProjectTemplateDefinition(
        override val displayName: String,
        @field:Language("HTML") @param:Language("HTML") private val _description: String

                                             ) : FrcWizardTemplateDefinition
{
    CommandBased("Command Based Robot", "A robot project that allows robots to be implemented using the command based model to allow complex functionality to be developed from simpler functionality."),
    Iterative("Iterative Robot", "A robot project that allow robots to be implemented in an iterative manner synced to receiving driver station packets."),
    Timed("Timed Robot", "A robot project that allows robots to be implemented in an iterative manner synced to a timer."),
    TimedSkeleton("Timed Skeleton (Advanced)", "A skeleton (stub) Timed Robot project."),
    Sample("Sample Robot", "A robot project used for small sample programs or for highly advanced programs with more complete control over program flow. This is <em>not</em> a good choice to use for competition, especially for the inexperienced. Use Timed Robot or Command Based Robot instead.")
    ;


    override val description: String
        @get:Language("HTML") get() = "<html>$_description</html>"

    override val displayNameAndDescription: String
        @get:Language("HTML") get() = "<html><strong>$displayName</strong> : $_description</html>"

    override fun toString(): String = displayName
   
    override fun templateResourcesDirName(): String = name.decapitalize()

    override fun isProjectBootstrapTemplate(): Boolean = true
}

enum class FrcWizardExampleTemplateDefinition(
        override val displayName: String,
        @field:Language("HTML") @param:Language("HTML") private val _description: String

                                           ) : FrcWizardTemplateDefinition
{
    GettingStarted("Getting Started", "An example project which demonstrates the simplest autonomous and teleoperated routines."),
    TankDrive("Tank Drive", "Demonstrates the use of the RobotDrive class doing teleop driving with tank steering (i.e. two joysticks)."),
    //ArcadeDrive("Arcade Drive", "Demonstrates the use of the DifferentialDrive class to drive a robot with arcade drive/steering (i.e. single joystick)."),
    MecanumDrive("Mecanum Drive", "Demonstrate the use of the RobotDrive class doing teleop driving with a Mecanum drivetrain."),
    Ultrasonic("Ultrasonic", "Demonstrates maintaining a set distance using an ultrasonic sensor."),
    UltrasonicPID("Ultrasonic PID", "Demonstrates maintaining a set distance using an ultrasonic sensor and PID Control."),
    PotentiometerPID("Potentiometer PID", "Demonstrates the use of a potentiometer and PID control to reach elevator position setpoints."),
    Gyro("Gyro", "Demonstrates how to drive straight using a gyro sensor."),
    GyroMecanum("Gyro Mecanum", "Demonstrates how to perform mecanum drive with field oriented controls."),
    HIDRumble("HID Rumble", "Demonstrates how to make human interface devices rumble."),
    MotorController("Motor Controller", "Demonstrates controlling a single motor with a joystick."),
    MotorControlWithEncoder("Motor Control With Encoder", "Demonstrates controlling a single motor with a Joystick and displaying the net movement of the motor using an encoder."),
    GearsBot("GearsBot", "A fully functional example CommandBased program for WPIs GearsBot robot, ported to the new CommandBased library. This code can run on your computer if it supports simulation."),
    PacGoat("PacGoat", "A fully functional example CommandBased program for FRC Team 190's 2014 robot. This code can run on your computer if it supports simulation."),
    SimpleVision("Simple Vision", "Demonstrates the use of the CameraServer class to stream from a USB Webcam without processing the images."),
    IntermediateVision("Intermediate Vision", "Demonstrates the use of the NIVision class to capture image from a Webcam, process them, and then send them to the dashboard."),
    AxisCameraSample("Axis Camera Sample", "An example program that acquires images from an Axis network camera and adds some annotation to the image as you might do for showing operators the result of some image recognition, and sends it to the dashboard for display. This demonstrates the use of the AxisCamera class."),
    ShuffleboardSample("Shuffleboard Sample", "An example program that adds data to various Shuffleboard tabs that demonstrates the Shuffleboard API."),
    ;   


    override val description: String
        @get:Language("HTML") get() = "<html>$_description</html>"

    override val displayNameAndDescription: String
        @get:Language("HTML") get() = "<html><strong>$displayName</strong> : $_description</html>"

    override fun toString(): String = displayName

    override fun templateResourcesDirName(): String = name.decapitalize()

    override fun isProjectBootstrapTemplate(): Boolean = false
}