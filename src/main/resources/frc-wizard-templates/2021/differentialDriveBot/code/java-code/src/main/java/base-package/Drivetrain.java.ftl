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
${data.copyright}

package ${data.basePackage};

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveOdometry;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveWheelSpeeds;

/**
 * Represents a differential drive style drivetrain.
 */
public class Drivetrain
{
    public static final double MAX_SPEED = 3.0; // meters per second
    public static final double MAX_ANGULAR_SPEED = 2 * Math.PI; // one rotation per second

    private static final double TRACK_WIDTH = 0.381 * 2; // meters
    private static final double WHEEL_RADIUS = 0.0508; // meters
    private static final int ENCODER_RESOLUTION = 4096;

    private final SpeedController leftMaster = new PWMVictorSPX(1);
    private final SpeedController leftFollower = new PWMVictorSPX(2);
    private final SpeedController rightMaster = new PWMVictorSPX(3);
    private final SpeedController rightFollower = new PWMVictorSPX(4);

    private final Encoder leftEncoder = new Encoder(0, 1);
    private final Encoder rightEncoder = new Encoder(2, 3);

    private final SpeedControllerGroup leftGroup = new SpeedControllerGroup(leftMaster, leftFollower);
    private final SpeedControllerGroup rightGroup = new SpeedControllerGroup(rightMaster, rightFollower);

    private final AnalogGyro gyro = new AnalogGyro(0);

    private final PIDController leftPIDController = new PIDController(1, 0, 0);
    private final PIDController rightPIDController = new PIDController(1, 0, 0);

    private final DifferentialDriveKinematics kinematics = new DifferentialDriveKinematics(TRACK_WIDTH);

    private final DifferentialDriveOdometry odometry;

    /**
     * Constructs a differential drive object.
     * Sets the encoder distance per pulse and resets the gyro.
     */
    public Drivetrain()
    {
        gyro.reset();

        // Set the distance per pulse for the drive encoders. We can simply use the
        // distance traveled for one rotation of the wheel divided by the encoder
        // resolution.
        leftEncoder.setDistancePerPulse(2 * Math.PI * WHEEL_RADIUS / ENCODER_RESOLUTION);
        rightEncoder.setDistancePerPulse(2 * Math.PI * WHEEL_RADIUS / ENCODER_RESOLUTION);

        leftEncoder.reset();
        rightEncoder.reset();

        odometry = new DifferentialDriveOdometry(getAngle());
    }

    /**
     * Returns the angle of the robot as a Rotation2d.
     *
     * @return The angle of the robot.
     */
    public Rotation2d getAngle()
    {
        // Negating the angle because WPILib gyros are CW positive.
        return Rotation2d.fromDegrees(-gyro.getAngle());
    }

    /**
     * Sets the desired wheel speeds.
     *
     * @param speeds The desired wheel speeds.
     */
    public void setSpeeds(DifferentialDriveWheelSpeeds speeds)
    {
        double leftOutput = leftPIDController.calculate(leftEncoder.getRate(), speeds.leftMetersPerSecond);
        double rightOutput = rightPIDController.calculate(rightEncoder.getRate(), speeds.rightMetersPerSecond);
        leftGroup.set(leftOutput);
        rightGroup.set(rightOutput);
    }

    /**
     * Drives the robot with the given linear velocity and angular velocity.
     *
     * @param xSpeed Linear velocity in m/s.
     * @param rot    Angular velocity in rad/s.
     */
    public void drive(double xSpeed, double rot)
    {
        var wheelSpeeds = kinematics.toWheelSpeeds(new ChassisSpeeds(xSpeed, 0.0, rot));
        setSpeeds(wheelSpeeds);
    }

    /**
     * Updates the field-relative position.
     */
    public void updateOdometry()
    {
        odometry.update(getAngle(), leftEncoder.getDistance(), rightEncoder.getDistance());
    }
}
