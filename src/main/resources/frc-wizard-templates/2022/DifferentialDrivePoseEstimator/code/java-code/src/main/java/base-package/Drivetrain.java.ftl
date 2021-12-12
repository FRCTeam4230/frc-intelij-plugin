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

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.estimator.DifferentialDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.math.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;



/** Represents a differential drive style drivetrain. */
public class Drivetrain
{
    public static final double MAX_SPEED = 3.0; // meters per second
    public static final double MAX_ANGULAR_SPEED = 2 * Math.PI; // one rotation per second
    
    private static final double TRACK_WIDTH = 0.381 * 2; // meters
    private static final double WHEEL_RADIUS = 0.0508; // meters
    private static final int ENCODER_RESOLUTION = 4096;
    
    private final MotorController leftLeader = new PWMSparkMax(1);
    private final MotorController leftFollower = new PWMSparkMax(2);
    private final MotorController rightLeader = new PWMSparkMax(3);
    private final MotorController rightFollower = new PWMSparkMax(4);
    
    private final Encoder leftEncoder = new Encoder(0, 1);
    private final Encoder rightEncoder = new Encoder(2, 3);
    
    private final MotorControllerGroup leftGroup =
            new MotorControllerGroup(leftLeader, leftFollower);
    private final MotorControllerGroup rightGroup =
            new MotorControllerGroup(rightLeader, rightFollower);
    
    private final AnalogGyro gyro = new AnalogGyro(0);
    
    private final PIDController leftPIDController = new PIDController(1, 0, 0);
    private final PIDController rightPIDController = new PIDController(1, 0, 0);
    
    private final DifferentialDriveKinematics kinematics =
            new DifferentialDriveKinematics(TRACK_WIDTH);
    
    /* Here we use DifferentialDrivePoseEstimator so that we can fuse odometry readings. The
    numbers used  below are robot specific, and should be tuned. */
    private final DifferentialDrivePoseEstimator poseEstimator =
            new DifferentialDrivePoseEstimator(
                    gyro.getRotation2d(),
                    new Pose2d(),
                    VecBuilder.fill(0.05, 0.05, Units.degreesToRadians(5), 0.01, 0.01),
                    VecBuilder.fill(0.02, 0.02, Units.degreesToRadians(1)),
                    VecBuilder.fill(0.5, 0.5, Units.degreesToRadians(30)));
    
    // Gains are for example purposes only - must be determined for your own robot!
    private final SimpleMotorFeedforward feedforward = new SimpleMotorFeedforward(1, 3);
    
    
    /**
     * Constructs a differential drive object. Sets the encoder distance per pulse and resets the
     * gyro.
     */
    public Drivetrain()
    {
        gyro.reset();
        
        // We need to invert one side of the drivetrain so that positive voltages
        // result in both sides moving forward. Depending on how your robot's
        // gearbox is constructed, you might have to invert the left side instead.
        rightGroup.setInverted(true);
        
        // Set the distance per pulse for the drive encoders. We can simply use the
        // distance traveled for one rotation of the wheel divided by the encoder
        // resolution.
        leftEncoder.setDistancePerPulse(2 * Math.PI * WHEEL_RADIUS / ENCODER_RESOLUTION);
        rightEncoder.setDistancePerPulse(2 * Math.PI * WHEEL_RADIUS / ENCODER_RESOLUTION);
        
        leftEncoder.reset();
        rightEncoder.reset();
    }
    
    
    /**
     * Sets the desired wheel speeds.
     *
     * @param speeds The desired wheel speeds.
     */
    public void setSpeeds(DifferentialDriveWheelSpeeds speeds)
    {
        final double leftFeedforward = feedforward.calculate(speeds.leftMetersPerSecond);
        final double rightFeedforward = feedforward.calculate(speeds.rightMetersPerSecond);
        
        final double leftOutput =
                leftPIDController.calculate(leftEncoder.getRate(), speeds.leftMetersPerSecond);
        final double rightOutput =
                rightPIDController.calculate(rightEncoder.getRate(), speeds.rightMetersPerSecond);
        leftGroup.setVoltage(leftOutput + leftFeedforward);
        rightGroup.setVoltage(rightOutput + rightFeedforward);
    }
    
    
    /**
     * Drives the robot with the given linear velocity and angular velocity.
     *
     * @param xSpeed   Linear velocity in m/s.
     * @param rotation Angular velocity in rad/s.
     */
    @SuppressWarnings("ParameterName")
    public void drive(double xSpeed, double rotation)
    {
        var wheelSpeeds = kinematics.toWheelSpeeds(new ChassisSpeeds(xSpeed, 0.0, rotation));
        setSpeeds(wheelSpeeds);
    }
    
    
    /** Updates the field-relative position. */
    public void updateOdometry()
    {
        poseEstimator.update(
                gyro.getRotation2d(),
                new DifferentialDriveWheelSpeeds(leftEncoder.getRate(), rightEncoder.getRate()),
                leftEncoder.getDistance(),
                rightEncoder.getDistance());
        
        // Also apply vision measurements. We use 0.3 seconds in the past as an example -- on
        // a real robot, this must be calculated based either on latency or timestamps.
        poseEstimator.addVisionMeasurement(
                ExampleGlobalMeasurementSensor.getEstimatedGlobalPose(
                        poseEstimator.getEstimatedPosition()),
                Timer.getFPGATimestamp() - 0.3);
    }
}
