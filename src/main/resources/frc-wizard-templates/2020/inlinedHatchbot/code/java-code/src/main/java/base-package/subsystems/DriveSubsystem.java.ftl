<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement-->
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

package ${data.basePackage}.subsystems;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import static ${data.basePackage}.Constants.DriveConstants.*;

public class DriveSubsystem extends SubsystemBase
{
    // The motors on the left side of the drive.
    private final SpeedControllerGroup leftMotors =
            new SpeedControllerGroup(new PWMVictorSPX(LEFT_MOTOR_1_PORT),
                    new PWMVictorSPX(LEFT_MOTOR_2_PORT));

    // The motors on the right side of the drive.
    private final SpeedControllerGroup rightMotors =
            new SpeedControllerGroup(new PWMVictorSPX(RIGHT_MOTOR_1_PORT),
                    new PWMVictorSPX(RIGHT_MOTOR_2_PORT));

    // The robot's drive
    private final DifferentialDrive drive = new DifferentialDrive(leftMotors, rightMotors);

    // The left-side drive encoder
    private final Encoder leftEncoder =
            new Encoder(LEFT_ENCODER_PORTS[0], LEFT_ENCODER_PORTS[1], LEFT_ENCODER_REVERSED);

    // The right-side drive encoder
    private final Encoder rightEncoder =
            new Encoder(RIGHT_ENCODER_PORTS[0], RIGHT_ENCODER_PORTS[1], RIGHT_ENCODER_REVERSED);

    /**
     * Creates a new DriveSubsystem.
     */
    public DriveSubsystem()
    {
        // Sets the distance per pulse for the encoders
        leftEncoder.setDistancePerPulse(ENCODER_DISTANCE_PER_PULSE);
        rightEncoder.setDistancePerPulse(ENCODER_DISTANCE_PER_PULSE);
    }

    /**
     * Drives the robot using arcade controls.
     *
     * @param fwd the commanded forward movement
     * @param rot the commanded rotation
     */
    public void arcadeDrive(double fwd, double rot)
    {
        drive.arcadeDrive(fwd, rot);
    }

    /**
     * Resets the drive encoders to currently read a position of 0.
     */
    public void resetEncoders()
    {
        leftEncoder.reset();
        rightEncoder.reset();
    }

    /**
     * Gets the average distance of the TWO encoders.
     *
     * @return the average of the TWO encoder readings
     */
    public double getAverageEncoderDistance()
    {
        return (leftEncoder.getDistance() + rightEncoder.getDistance()) / 2.0;
    }

    /**
     * Gets the left drive encoder.
     *
     * @return the left drive encoder
     */
    public Encoder getLeftEncoder()
    {
        return leftEncoder;
    }

    /**
     * Gets the right drive encoder.
     *
     * @return the right drive encoder
     */
    public Encoder getRightEncoder()
    {
        return rightEncoder;
    }

    /**
     * Sets the max output of the drive.  Useful for scaling the drive to drive more slowly.
     *
     * @param maxOutput the maximum output to which the drive will be constrained
     */
    public void setMaxOutput(double maxOutput)
    {
        drive.setMaxOutput(maxOutput);
    }
}
