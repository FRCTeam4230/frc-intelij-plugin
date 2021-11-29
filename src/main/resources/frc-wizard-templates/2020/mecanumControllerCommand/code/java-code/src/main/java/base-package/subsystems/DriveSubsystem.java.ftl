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
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.subsystems;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveMotorVoltages;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveOdometry;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveWheelSpeeds;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import static ${data.basePackage}.Constants.DriveConstants.*;

public class DriveSubsystem extends SubsystemBase
{
    private final PWMVictorSPX frontLeft = new PWMVictorSPX(FRONT_LEFT_MOTOR_PORT);
    private final PWMVictorSPX rearLeft = new PWMVictorSPX(REAR_LEFT_MOTOR_PORT);
    private final PWMVictorSPX frontRight = new PWMVictorSPX(FRONT_RIGHT_MOTOR_PORT);
    private final PWMVictorSPX rearRight = new PWMVictorSPX(REAR_RIGHT_MOTOR_PORT);

    private final MecanumDrive drive = new MecanumDrive(
            frontLeft,
            rearLeft,
            frontRight,
            rearRight);

    // The front-left-side drive encoder
    private final Encoder frontLeftEncoder =
            new Encoder(FRONT_LEFT_ENCODER_PORTS[0], FRONT_LEFT_ENCODER_PORTS[1], FRONT_LEFT_ENCODER_REVERSED);

    // The rear-left-side drive encoder
    private final Encoder rearLeftEncoder =
            new Encoder(REAR_LEFT_ENCODER_PORTS[0], REAR_LEFT_ENCODER_PORTS[1], REAR_LEFT_ENCODER_REVERSED);

    // The front-right--side drive encoder
    private final Encoder frontRightEncoder =
            new Encoder(FRONT_RIGHT_ENCODER_PORTS[0], FRONT_RIGHT_ENCODER_PORTS[1], FRONT_RIGHT_ENCODER_REVERSED);

    // The rear-right-side drive encoder
    private final Encoder rearRightEncoder =
            new Encoder(REAR_RIGHT_ENCODER_PORTS[0], REAR_RIGHT_ENCODER_PORTS[1], REAR_RIGHT_ENCODER_REVERSED);

    // The gyro sensor
    private final Gyro gyro = new ADXRS450_Gyro();

    // Odometry class for tracking robot pose
    MecanumDriveOdometry odometry = new MecanumDriveOdometry(DRIVE_KINEMATICS, getAngle());

    /**
     * Creates a new DriveSubsystem.
     */
    public DriveSubsystem()
    {
        // Sets the distance per pulse for the encoders
        frontLeftEncoder.setDistancePerPulse(ENCODER_DISTANCE_PER_PULSE);
        rearLeftEncoder.setDistancePerPulse(ENCODER_DISTANCE_PER_PULSE);
        frontRightEncoder.setDistancePerPulse(ENCODER_DISTANCE_PER_PULSE);
        rearRightEncoder.setDistancePerPulse(ENCODER_DISTANCE_PER_PULSE);
    }

    /**
     * Returns the angle of the robot as a Rotation2d.
     *
     * @return The angle of the robot.
     */
    public Rotation2d getAngle()
    {
        // Negating the angle because WPILib gyros are CW positive.
        return Rotation2d.fromDegrees(gyro.getAngle() * (GYRO_REVERSED ? 1.0 : -1.0));
    }

    @Override
    public void periodic()
    {
        // Update the odometry in the periodic block
        odometry.update(getAngle(),
                new MecanumDriveWheelSpeeds(
                        frontLeftEncoder.getRate(),
                        rearLeftEncoder.getRate(),
                        frontRightEncoder.getRate(),
                        rearRightEncoder.getRate()));
    }

    /**
     * Returns the currently-estimated pose of the robot.
     *
     * @return The pose.
     */
    public Pose2d getPose()
    {
        return odometry.getPoseMeters();
    }

    /**
     * Resets the odometry to the specified pose.
     *
     * @param pose The pose to which to set the odometry.
     */
    public void resetOdometry(Pose2d pose)
    {
        odometry.resetPosition(pose, getAngle());
    }

    /**
     * Drives the robot at given x, y and theta speeds. Speeds range from [-1, 1] and the linear
     * speeds have no effect on the angular speed.
     *
     * @param xSpeed        Speed of the robot in the x direction (forward/backwards).
     * @param ySpeed        Speed of the robot in the y direction (sideways).
     * @param rot           Angular rate of the robot.
     * @param fieldRelative Whether the provided x and y speeds are relative to the field.
     */
    @SuppressWarnings("ParameterName")
    public void drive(double xSpeed, double ySpeed, double rot, boolean fieldRelative)
    {
        if (fieldRelative)
        {
            drive.driveCartesian(ySpeed, xSpeed, rot, -gyro.getAngle());
        }
        else
        {
            drive.driveCartesian(ySpeed, xSpeed, rot);
        }

    }

    /**
     * Sets the front left drive SpeedController to a voltage.
     */
    public void setDriveSpeedControllersVolts(MecanumDriveMotorVoltages volts)
    {
        frontLeft.setVoltage(volts.frontLeftVoltage);
        rearLeft.setVoltage(volts.rearLeftVoltage);
        frontRight.setVoltage(volts.frontRightVoltage);
        rearRight.setVoltage(volts.rearRightVoltage);
    }


    /**
     * Resets the drive encoders to currently read a position of 0.
     */
    public void resetEncoders()
    {
        frontLeftEncoder.reset();
        rearLeftEncoder.reset();
        frontRightEncoder.reset();
        rearRightEncoder.reset();
    }

    /**
     * Gets the front left drive encoder.
     *
     * @return the front left drive encoder
     */

    public Encoder getFrontLeftEncoder()
    {
        return frontLeftEncoder;
    }

    /**
     * Gets the rear left drive encoder.
     *
     * @return the rear left drive encoder
     */

    public Encoder getRearLeftEncoder()
    {
        return rearLeftEncoder;
    }

    /**
     * Gets the front right drive encoder.
     *
     * @return the front right drive encoder
     */

    public Encoder getFrontRightEncoder()
    {
        return frontRightEncoder;
    }

    /**
     * Gets the rear right drive encoder.
     *
     * @return the rear right encoder
     */

    public Encoder getRearRightEncoder()
    {
        return rearRightEncoder;
    }

    /**
     * Gets the current wheel speeds.
     *
     * @return the current wheel speeds in a MecanumDriveWheelSpeeds object.
     */

    public MecanumDriveWheelSpeeds getCurrentWheelSpeeds()
    {
        return new MecanumDriveWheelSpeeds(frontLeftEncoder.getRate(),
                rearLeftEncoder.getRate(),
                frontRightEncoder.getRate(),
                rearRightEncoder.getRate());
    }


    /**
     * Sets the max output of the drive. Useful for scaling the drive to drive more slowly.
     *
     * @param maxOutput the maximum output to which the drive will be constrained
     */
    public void setMaxOutput(double maxOutput)
    {
        drive.setMaxOutput(maxOutput);
    }

    /**
     * Zeroes the heading of the robot.
     */
    public void zeroHeading()
    {
        gyro.reset();
    }

    /**
     * Returns the heading of the robot.
     *
     * @return the robot's heading in degrees, from 180 to 180
     */
    public double getHeading()
    {
        return Math.IEEEremainder(gyro.getAngle(), 360) * (GYRO_REVERSED ? -1.0 : 1.0);
    }

    /**
     * Returns the turn rate of the robot.
     *
     * @return The turn rate of the robot, in degrees per second
     */
    public double getTurnRate()
    {
        return gyro.getRate() * (GYRO_REVERSED ? -1.0 : 1.0);
    }
}
