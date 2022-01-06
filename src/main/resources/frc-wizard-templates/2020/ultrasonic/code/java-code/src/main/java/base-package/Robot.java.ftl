<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:     https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when launching the testing instance of IntelliJ IDEA -->
</#compress>
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage};

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.MedianFilter;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

/**
 * This is a sample program demonstrating how to use an ultrasonic sensor and
 * proportional control to maintain a set distance from an object.
 * <p>
 * The VM is configured to automatically run this class, and to call the
 * methods corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    // distance in inches the robot wants to stay from an object
    private static final double HOLD_DISTANCE = 12.0;

    // factor to convert sensor values to a distance in inches
    private static final double VALUE_TO_INCHES = 0.125;

    // proportional speed constant
    private static final double P = 0.05;

    private static final int LEFT_MOTOR_PORT = 0;
    private static final int RIGHT_MOTOR_PORT = 1;
    private static final int ULTRASONIC_PORT = 0;

    // median filter to discard outliers; filters over 10 samples
    private final MedianFilter filter = new MedianFilter(10);

    private final AnalogInput ultrasonic = new AnalogInput(ULTRASONIC_PORT);
    private final DifferentialDrive robotDrive
            = new DifferentialDrive(new PWMVictorSPX(LEFT_MOTOR_PORT), new PWMVictorSPX(RIGHT_MOTOR_PORT));
    /**
     * Tells the robot to drive to a set distance (in inches) from an object
     * using proportional control.
     */
    @Override
    public void teleopPeriodic()
    {
        // sensor returns a value from 0-4095 that is scaled to inches
        // returned value is filtered with a rolling median filter, since ultrasonics
        // tend to be quite noisy and susceptible to sudden outliers
        double currentDistance = filter.calculate(ultrasonic.getValue()) * VALUE_TO_INCHES;

        // convert distance error to a motor speed
        double currentSpeed = (HOLD_DISTANCE - currentDistance) * P;

        // drive robot
        robotDrive.arcadeDrive(currentSpeed, 0);
    }
}
