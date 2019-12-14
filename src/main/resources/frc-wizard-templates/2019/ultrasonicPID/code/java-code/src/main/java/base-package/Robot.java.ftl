<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Refernce: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:    https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when luanching the testing instance of IntelliJ IDEA -->
</#compress>
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage};

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

/**
 * This is a sample program to demonstrate the use of a PIDController with an
 * ultrasonic sensor to reach and maintain a set distance from an object.
 * <p>
 * The VM is configured to automatically run this class, and to call the
 * methods corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    /** Distance in inches the robot wants to stay from an object. */
    private static final double HOLD_DISTANCE = 12.0;

    /** Maximum distance in inches we expect the robot to see. */
    private static final double MAX_DISTANCE = 24.0;

    /** Factor to convert sensor values to a distance in inches. */
    private static final double VALUE_TO_INCHES = 0.125;

    /** Proportional speed constant. */
    private static final double P = 7.0;

    /** Integral speed constant. */
    private static final double I = 0.018;

    /** Derivative speed constant. */
    private static final double D = 1.5;

    private static final int LEFT_MOTOR_PORT = 0;
    private static final int RIGHT_MOTOR_PORT = 1;
    private static final int ULTRASONIC_PORT = 0;

    private final AnalogInput ultrasonic = new AnalogInput(ULTRASONIC_PORT);
    private final DifferentialDrive robotDrive
            = new DifferentialDrive(new PWMVictorSPX(LEFT_MOTOR_PORT), new PWMVictorSPX(RIGHT_MOTOR_PORT));
    private final PIDController pidController = new PIDController(P, I, D, ultrasonic, new MyPidOutput());

    /**
     * Drives the robot a set distance from an object using PID control and the
     * ultrasonic sensor.
     */
    @Override
    public void teleopInit()
    {
        // Set expected range to 0-24 inches; e.g. at 24 inches from object go
        // full forward, at 0 inches from object go full backward.
        pidController.setInputRange(0, MAX_DISTANCE * VALUE_TO_INCHES);
        // Set setpoint of the pid controller
        pidController.setSetpoint(HOLD_DISTANCE * VALUE_TO_INCHES);
        pidController.enable(); // begin PID control
    }

    private class MyPidOutput implements PIDOutput
    {
        @Override
        public void pidWrite(double output)
        {
            robotDrive.arcadeDrive(output, 0);
        }
    }
}
