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
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.interfaces.Gyro;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.SwerveDriveKinematics;
import edu.wpi.first.wpilibj.kinematics.SwerveDriveOdometry;
import edu.wpi.first.wpilibj.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import static ${data.basePackage}.Constants.AutoConstants.MAX_SPEED_METERS_PER_SECOND;
import static ${data.basePackage}.Constants.DriveConstants.*;

@SuppressWarnings("PMD.ExcessiveImports")
public class DriveSubsystem extends SubsystemBase
{
    //Robot swerve modules
    private final SwerveModule frontLeft = new SwerveModule(FRONT_LEFT_DRIVE_MOTOR_PORT,
            FRONT_LEFT_TURNING_MOTOR_PORT,
            FRONT_LEFT_DRIVE_ENCODER_PORTS,
            FRONT_LEFT_TURNING_ENCODER_PORTS,
            FRONT_LEFT_DRIVE_ENCODER_REVERSED,
            FRONT_LEFT_TURNING_ENCODER_REVERSED);

    private final SwerveModule rearLeft = new SwerveModule(REAR_LEFT_DRIVE_MOTOR_PORT,
            REAR_LEFT_TURNING_MOTOR_PORT,
            REAR_LEFT_DRIVE_ENCODER_PORTS,
            REAR_LEFT_TURNING_ENCODER_PORTS,
            REAR_LEFT_DRIVE_ENCODER_REVERSED,
            REAR_LEFT_TURNING_ENCODER_REVERSED);


    private final SwerveModule frontRight = new SwerveModule(FRONT_RIGHT_DRIVE_MOTOR_PORT,
            FRONT_RIGHT_TURNING_MOTOR_PORT,
            FRONT_RIGHT_DRIVE_ENCODER_PORTS,
            FRONT_RIGHT_TURNING_ENCODER_PORTS,
            FRONT_RIGHT_DRIVE_ENCODER_REVERSED,
            FRONT_RIGHT_TURNING_ENCODER_REVERSED);

    private final SwerveModule rearRight = new SwerveModule(REAR_RIGHT_DRIVE_MOTOR_PORT,
            REAR_RIGHT_TURNING_MOTOR_PORT,
            REAR_RIGHT_DRIVE_ENCODER_PORTS,
            REAR_RIGHT_TURNING_ENCODER_PORTS,
            REAR_RIGHT_DRIVE_ENCODER_REVERSED,
            REAR_RIGHT_TURNING_ENCODER_REVERSED);

    // The gyro sensor
    private final Gyro gyro = new ADXRS450_Gyro();

    // Odometry class for tracking robot pose
    private SwerveDriveOdometry odometry = new SwerveDriveOdometry(DRIVE_KINEMATICS, getAngle());

    /**
     * Creates a new DriveSubsystem.
     */
    public DriveSubsystem()
    {

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
        odometry.update(
                new Rotation2d(getHeading()),
                frontLeft.getState(),
                rearLeft.getState(),
                frontRight.getState(),
                rearRight.getState());
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
     * Method to drive the robot using joystick info.
     *
     * @param xSpeed        Speed of the robot in the x direction (forward).
     * @param ySpeed        Speed of the robot in the y direction (sideways).
     * @param rot           Angular rate of the robot.
     * @param fieldRelative Whether the provided x and y speeds are relative to the field.
     */
    @SuppressWarnings("ParameterName")
    public void drive(double xSpeed, double ySpeed, double rot, boolean fieldRelative)
    {
        var swerveModuleStates = DRIVE_KINEMATICS.toSwerveModuleStates(
                fieldRelative ? ChassisSpeeds.fromFieldRelativeSpeeds(
                        xSpeed, ySpeed, rot, getAngle())
                        : new ChassisSpeeds(xSpeed, ySpeed, rot)
        );
        SwerveDriveKinematics.normalizeWheelSpeeds(swerveModuleStates, MAX_SPEED_METERS_PER_SECOND);
        frontLeft.setDesiredState(swerveModuleStates[0]);
        frontRight.setDesiredState(swerveModuleStates[1]);
        rearLeft.setDesiredState(swerveModuleStates[2]);
        rearRight.setDesiredState(swerveModuleStates[3]);
    }

    /**
     * Sets the swerve ModuleStates.
     *
     * @param desiredStates The desired SwerveModule states.
     */
    public void setModuleStates(SwerveModuleState[] desiredStates)
    {
        SwerveDriveKinematics.normalizeWheelSpeeds(desiredStates, MAX_SPEED_METERS_PER_SECOND);
        frontLeft.setDesiredState(desiredStates[0]);
        frontRight.setDesiredState(desiredStates[1]);
        rearLeft.setDesiredState(desiredStates[2]);
        rearRight.setDesiredState(desiredStates[3]);
    }

    /**
     * Resets the drive encoders to currently read a position of 0.
     */
    public void resetEncoders()
    {
        frontLeft.resetEncoders();
        rearLeft.resetEncoders();
        frontRight.resetEncoders();
        rearRight.resetEncoders();
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
