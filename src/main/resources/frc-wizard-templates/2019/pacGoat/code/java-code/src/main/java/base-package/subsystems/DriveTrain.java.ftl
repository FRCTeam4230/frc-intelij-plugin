<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:     https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when launching IntelliJ IDEA -->
</#compress>
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.subsystems;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.CounterBase.EncodingType;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import ${data.basePackage}.${data.robotClassSimpleName};
import ${data.basePackage}.commands.DriveWithJoystick;

/**
 * The DriveTrain subsystem controls the robot's chassis and reads in
 * information about it's speed and position.
 */
public class DriveTrain extends Subsystem
{
    // Subsystem devices
    private final SpeedController frontLeftCIM = new Victor(1);
    private final SpeedController frontRightCIM = new Victor(2);
    private final SpeedController rearLeftCIM = new Victor(3);
    private final SpeedController rearRightCIM = new Victor(4);
    private final SpeedControllerGroup leftCIMs = new SpeedControllerGroup(frontLeftCIM, rearLeftCIM);
    private final SpeedControllerGroup rightCIMs = new SpeedControllerGroup(frontRightCIM, rearRightCIM);
    private final DifferentialDrive drive;
    private final Encoder rightEncoder = new Encoder(1, 2, true, EncodingType.k4X);
    private final Encoder leftEncoder = new Encoder(3, 4, false, EncodingType.k4X);
    private final AnalogGyro gyro = new AnalogGyro(0);
    
    
    /**
     * Create a new drive train subsystem.
     */
    public DriveTrain()
    {
        // Configure drive motors
        addChild("Front Left CIM", (Victor) frontLeftCIM);
        addChild("Front Right CIM", (Victor) frontRightCIM);
        addChild("Back Left CIM", (Victor) rearLeftCIM);
        addChild("Back Right CIM", (Victor) rearRightCIM);
        
        // Configure the DifferentialDrive to reflect the fact that all motors
        // are wired backwards (right is inverted in DifferentialDrive).
        leftCIMs.setInverted(true);
        drive = new DifferentialDrive(leftCIMs, rightCIMs);
        drive.setSafetyEnabled(true);
        drive.setExpiration(0.1);
        drive.setMaxOutput(1.0);
        
        // Configure encoders
        rightEncoder.setPIDSourceType(PIDSourceType.kDisplacement);
        leftEncoder.setPIDSourceType(PIDSourceType.kDisplacement);
        
        if (Robot.isReal())
        { // Converts to feet
            rightEncoder.setDistancePerPulse(0.0785398);
            leftEncoder.setDistancePerPulse(0.0785398);
        }
        else
        {
            // Convert to feet 4in diameter wheels with 360 tick sim encoders
            rightEncoder.setDistancePerPulse(
                    (4.0/* in */ * Math.PI) / (360.0 * 12.0/* in/ft */));
            leftEncoder.setDistancePerPulse(
                    (4.0/* in */ * Math.PI) / (360.0 * 12.0/* in/ft */));
        }
        
        addChild("Right Encoder", rightEncoder);
        addChild("Left Encoder", leftEncoder);
        
        // Configure gyro
        if (Robot.isReal())
        {
            gyro.setSensitivity(0.007); // TODO: Handle more gracefully?
        }
        addChild("Gyro", gyro);
    }
    
    
    /**
     * When other commands aren't using the drivetrain, allow tank drive with
     * the joystick.
     */
    @Override
    public void initDefaultCommand()
    {
        setDefaultCommand(new DriveWithJoystick());
    }
    
    
    /**
     * Tank drive using a PS3 joystick.
     *
     * @param joy PS3 style joystick to use as the input for tank drive.
     */
    public void tankDrive(Joystick joy)
    {
        drive.tankDrive(joy.getY(), joy.getRawAxis(4));
    }
    
    
    /**
     * Tank drive using individual joystick axes.
     *
     * @param leftAxis  Left sides value
     * @param rightAxis Right sides value
     */
    public void tankDrive(double leftAxis, double rightAxis)
    {
        drive.tankDrive(leftAxis, rightAxis);
    }
    
    
    /**
     * Stop the drivetrain from moving.
     */
    public void stop()
    {
        drive.tankDrive(0, 0);
    }
    
    
    /**
     * The encoder getting the distance and speed of left side of the
     * drivetrain.
     */
    public Encoder getLeftEncoder()
    {
        return leftEncoder;
    }
    
    
    /**
     * The encoder getting the distance and speed of right side of the
     * drivetrain.
     */
    public Encoder getRightEncoder()
    {
        return rightEncoder;
    }
    
    
    /**
     * The current angle of the drivetrain as measured by the Gyro.
     */
    public double getAngle()
    {
        return gyro.getAngle();
    }
}
