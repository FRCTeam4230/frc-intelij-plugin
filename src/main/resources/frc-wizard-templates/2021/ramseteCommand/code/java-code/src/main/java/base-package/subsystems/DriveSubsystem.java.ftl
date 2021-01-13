<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:    https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when luanching the testing instance of IntelliJ IDEA -->
</#compress>
${data.copyright}

package ${data.basePackage}.subsystems;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import static ${data.basePackage}.Constants.DriveConstants.*;

public class DriveSubsystem extends SubsystemBase
{
    // The motors on the left side of the drive.
    private final SpeedControllerGroup leftMotors =
            new SpeedControllerGroup(new PWMVictorSPX(LEFT_MOTOR_1_PORT), new PWMVictorSPX(LEFT_MOTOR_2_PORT));

    // The motors on the right side of the drive.
    private final SpeedControllerGroup rightMotors =
            new SpeedControllerGroup(new PWMVictorSPX(RIGHT_MOTOR_1_PORT), new PWMVictorSPX(RIGHT_MOTOR_2_PORT));

    // The robot's drive
    private final DifferentialDrive drive = new DifferentialDrive(leftMotors, rightMotors);

    // The left-side drive encoder
    private final Encoder leftEncoder =
            new Encoder(LEFT_ENCODER_PORTS[0], LEFT_ENCODER_PORTS[1], LEFT_ENCODER_REVERSED);

    // The right-side drive encoder
    private final Encoder rightEncoder =
            new Encoder(RIGHT_ENCODER_PORTS[0], RIGHT_ENCODER_PORTS[1], RIGHT_ENCODER_REVERSED);

    // The gyro sensor
    private final Gyro gyro = new ADXRS450_Gyro();

    // Odometry class for tracking robot pose
    private final DifferentialDriveOdometry odometry;

    /**
     * Creates a new DriveSubsystem.
     */
    public DriveSubsystem()
    {
        // Sets the distance per pulse for the encoders
        leftEncoder.setDistancePerPulse(ENCODER_DISTANCE_PER_PULSE);
        rightEncoder.setDistancePerPulse(ENCODER_DISTANCE_PER_PULSE);

        resetEncoders();
        odometry = new DifferentialDriveOdometry(Rotation2d.fromDegrees(getHeading()));
    }

    @Override
    public void periodic()
    {
        // Update the odometry in the periodic block
        odometry.update(Rotation2d.fromDegrees(getHeading()), leftEncoder.getDistance(), rightEncoder.getDistance());
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
     * Returns the current wheel speeds of the robot.
     *
     * @return The current wheel speeds.
     */
    public DifferentialDriveWheelSpeeds getWheelSpeeds()
    {
        return new DifferentialDriveWheelSpeeds(leftEncoder.getRate(), rightEncoder.getRate());
    }

    /**
     * Resets the odometry to the specified pose.
     *
     * @param pose The pose to which to set the odometry.
     */
    public void resetOdometry(Pose2d pose)
    {
        resetEncoders();
        odometry.resetPosition(pose, Rotation2d.fromDegrees(getHeading()));
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
     * Controls the left and right sides of the drive directly with voltages.
     *
     * @param leftVolts  the commanded left output
     * @param rightVolts the commanded right output
     */
    public void tankDriveVolts(double leftVolts, double rightVolts)
    {
        leftMotors.setVoltage(leftVolts);
        rightMotors.setVoltage(-rightVolts);
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
     * Gets the average distance of the two encoders.
     *
     * @return the average of the two encoder readings
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
