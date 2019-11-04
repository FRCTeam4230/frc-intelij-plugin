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

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.drive.MecanumDrive;

/**
 * This is a demo program showing how to use Mecanum control with the RobotDrive class.
 * <p>
 * The VM is configured to automatically run this class, and to call the
 * methods corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    private static final int FRONT_LEFT_CHANNEL = 2;
    private static final int REAR_LEFT_CHANNEL = 3;
    private static final int FRONT_RIGHT_CHANNEL = 1;
    private static final int REAR_RIGHT_CHANNEL = 0;

    private static final int JOYSTICK_CHANNEL = 0;

    private MecanumDrive robotDrive;
    private Joystick stick;

    /**
     * This method is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit()
    {
        PWMVictorSPX frontLeft = new PWMVictorSPX(FRONT_LEFT_CHANNEL);
        PWMVictorSPX rearLeft = new PWMVictorSPX(REAR_LEFT_CHANNEL);
        PWMVictorSPX frontRight = new PWMVictorSPX(FRONT_RIGHT_CHANNEL);
        PWMVictorSPX rearRight = new PWMVictorSPX(REAR_RIGHT_CHANNEL);

        // Invert the left side motors.
        // You may need to change or remove this to match your robot.
        frontLeft.setInverted(true);
        rearLeft.setInverted(true);

        robotDrive = new MecanumDrive(frontLeft, rearLeft, frontRight, rearRight);

        stick = new Joystick(JOYSTICK_CHANNEL);
    }

     /**
     * This method is called periodically during teleoperated mode.
     */
    @Override
    public void teleopPeriodic()
    {
        // Use the joystick:
        //    X axis for lateral movement 
        //    Y axis for forward movement
        //    Z axis for rotation
        robotDrive.driveCartesian(stick.getX(), stick.getY(), stick.getZ(), 0.0);
    }
}
