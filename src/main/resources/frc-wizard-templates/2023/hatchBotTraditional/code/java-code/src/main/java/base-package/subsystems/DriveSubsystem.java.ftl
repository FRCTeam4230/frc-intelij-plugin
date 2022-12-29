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

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import ${data.basePackage}.Constants.DriveConstants;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.SubsystemBase;



public class DriveSubsystem extends SubsystemBase
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
    
    
    /** Creates a new DriveSubsystem. */
    public DriveSubsystem()
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
     * Drives the robot using arcade controls.
     *
     * @param fwd      the commanded forward movement
     * @param rotation the commanded rotation
     */
    public void arcadeDrive(double fwd, double rotation)
    {
        drive.arcadeDrive(fwd, rotation);
    }
    
    
    /** Resets the drive encoders to currently read a position of 0. */
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
     * Sets the max output of the drive. Useful for scaling the drive to drive more slowly.
     *
     * @param maxOutput the maximum output to which the drive will be constrained
     */
    public void setMaxOutput(double maxOutput)
    {
        drive.setMaxOutput(maxOutput);
    }
}
