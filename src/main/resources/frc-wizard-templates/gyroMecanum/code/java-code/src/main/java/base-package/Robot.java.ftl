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

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.drive.MecanumDrive;

/**
 *  This is a sample program that uses mecanum drive with a gyro sensor to
 *  maintain rotation vectors in relation to the starting orientation of the
 *  robot (field-oriented controls).
 *  <p>
 * The VM is configured to automatically run this class, and to call the
 * methods corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    /**
     * Gyro calibration constant, may need to be adjusted.
     * Gyro value of 360 is set to correspond to one full revolution.
     */
    private static final double VOLTS_PER_DEGREE_PER_SECOND = 0.0128;

    private static final int FRONT_LEFT_CHANNEL = 0;
    private static final int REAR_LEFT_CHANNEL = 1;
    private static final int FRONT_RIGHT_CHANNEL = 2;
    private static final int REAR_RIGHT_CHANNEL = 3;
    private static final int GYRO_PORT = 0;
    private static final int JOYSTICK_PORT = 0;

    private MecanumDrive robotDrive;
    private final AnalogGyro gyro = new AnalogGyro(GYRO_PORT);
    private final Joystick joystick = new Joystick(JOYSTICK_PORT);

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

        gyro.setSensitivity(VOLTS_PER_DEGREE_PER_SECOND);
    }

    /**
     * Mecanum drive is used with the gyro angle as an input.
     * <p>
     * This method is called periodically during teleoperated mode.
     */
    @Override
    public void teleopPeriodic()
    {
      // Use the joystick:
      //    X axis for lateral movement
      //    Y axis for forward movement
      //    Z axis for rotation
        robotDrive.driveCartesian(joystick.getX(), joystick.getY(), joystick.getZ(), gyro.getAngle());
    }
}
