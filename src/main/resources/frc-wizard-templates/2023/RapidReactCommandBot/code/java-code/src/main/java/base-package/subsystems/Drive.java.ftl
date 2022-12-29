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

package ${data.basePackage}.subsystems;

import java.util.function.DoubleSupplier;

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import ${data.basePackage}.Constants.DriveConstants;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;



public class Drive extends SubsystemBase
{
    // The motors on the left side of the drive.
    private final MotorControllerGroup leftMotors =
            new MotorControllerGroup(
                    new PWMSparkMax(DriveConstants.LEFT_MOTOR_1_PORT),
                    new PWMSparkMax(DriveConstants.LEFT_MOTOR_2_PORT));
    
    // The motors on the right side of the drive.
    private final MotorControllerGroup rightMotors =
            new MotorControllerGroup(
                    new PWMSparkMax(DriveConstants.RIGHT_MOTOR_1_PORT),
                    new PWMSparkMax(DriveConstants.RIGHT_MOTOR_2_PORT));
    
    // The robot's drive
    private final DifferentialDrive drive = new DifferentialDrive(leftMotors, rightMotors);
    
    // The left-side drive encoder
    private final Encoder leftEncoder =
            new Encoder(
                    DriveConstants.LEFT_ENCODER_PORTS[0],
                    DriveConstants.LEFT_ENCODER_PORTS[1],
                    DriveConstants.LEFT_ENCODER_REVERSED);
    
    // The right-side drive encoder
    private final Encoder rightEncoder =
            new Encoder(
                    DriveConstants.RIGHT_ENCODER_PORTS[0],
                    DriveConstants.RIGHT_ENCODER_PORTS[1],
                    DriveConstants.RIGHT_ENCODER_REVERSED);
    
    
    /** Creates a new Drive subsystem. */
    public Drive()
    {
        // We need to invert one side of the drivetrain so that positive voltages
        // result in both sides moving forward. Depending on how your robot's
        // gearbox is constructed, you might have to invert the left side instead.
        rightMotors.setInverted(true);
        
        // Sets the distance per pulse for the encoders
        leftEncoder.setDistancePerPulse(DriveConstants.ENCODER_DISTANCE_PER_PULSE);
        rightEncoder.setDistancePerPulse(DriveConstants.ENCODER_DISTANCE_PER_PULSE);
    }
    
    
    /**
     * Returns a command that drives the robot with arcade controls.
     *
     * @param fwd the commanded forward movement
     * @param rot the commanded rotation
     */
    public CommandBase arcadeDriveCommand(DoubleSupplier fwd, DoubleSupplier rot)
    {
        // A split-stick arcade command, with forward/backward controlled by the left
        // hand, and turning controlled by the right.
        return run(() -> drive.arcadeDrive(fwd.getAsDouble(), rot.getAsDouble()))
                .withName("arcadeDrive");
    }
    
    
    /**
     * Returns a command that drives the robot forward a specified distance at a specified speed.
     *
     * @param distanceMeters The distance to drive forward in meters
     * @param speed          The fraction of max speed at which to drive
     */
    public CommandBase driveDistanceCommand(double distanceMeters, double speed)
    {
        return runOnce(
                () -> {
                    // Reset encoders at the start of the command
                    leftEncoder.reset();
                    rightEncoder.reset();
                })
                // Drive forward at specified speed
                .andThen(run(() -> drive.arcadeDrive(speed, 0)))
                // End command when we've traveled the specified distance
                .until(
                        () ->
                                Math.max(leftEncoder.getDistance(), rightEncoder.getDistance())
                                >= distanceMeters)
                // Stop the drive when the command ends
                .finallyDo(interrupted -> drive.stopMotor());
    }
}
