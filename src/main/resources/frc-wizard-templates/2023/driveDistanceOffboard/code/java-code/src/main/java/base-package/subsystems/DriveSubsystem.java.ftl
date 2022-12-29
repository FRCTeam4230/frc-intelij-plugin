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

import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import ${data.basePackage}.Constants.DriveConstants;
import ${data.basePackage}.ExampleSmartMotorController;
import edu.wpi.first.wpilibj2.command.SubsystemBase;



public class DriveSubsystem extends SubsystemBase
{
    // The motors on the left side of the drive.
    private final ExampleSmartMotorController leftLeader =
            new ExampleSmartMotorController(DriveConstants.LEFT_MOTOR_1_PORT);
    
    private final ExampleSmartMotorController leftFollower =
            new ExampleSmartMotorController(DriveConstants.LEFT_MOTOR_2_PORT);
    
    private final ExampleSmartMotorController rightLeader =
            new ExampleSmartMotorController(DriveConstants.RIGHT_MOTOR_1_PORT);
    
    private final ExampleSmartMotorController rightFollower =
            new ExampleSmartMotorController(DriveConstants.RIGHT_MOTOR_2_PORT);
    
    private final SimpleMotorFeedforward feedforward =
            new SimpleMotorFeedforward(
                    DriveConstants.ksVolts,
                    DriveConstants.kvVoltSecondsPerMeter,
                    DriveConstants.kaVoltSecondsSquaredPerMeter);
    
    // The robot's drive
    private final DifferentialDrive drive = new DifferentialDrive(leftLeader, rightLeader);
    
    
    /** Creates a new DriveSubsystem. */
    public DriveSubsystem()
    {
        // We need to invert one side of the drivetrain so that positive voltages
        // result in both sides moving forward. Depending on how your robot's
        // gearbox is constructed, you might have to invert the left side instead.
        rightLeader.setInverted(true);
        
        // You might need to not do this depending on the specific motor controller
        // that you are using -- contact the respective vendor's documentation for
        // more details.
        rightFollower.setInverted(true);
        
        leftFollower.follow(leftLeader);
        rightFollower.follow(rightLeader);
        
        leftLeader.setPID(DriveConstants.kp, 0, 0);
        rightLeader.setPID(DriveConstants.kp, 0, 0);
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
    
    
    /**
     * Attempts to follow the given drive states using offboard PID.
     *
     * @param left  The left wheel state.
     * @param right The right wheel state.
     */
    public void setDriveStates(TrapezoidProfile.State left, TrapezoidProfile.State right)
    {
        leftLeader.setSetpoint(
                ExampleSmartMotorController.PIDMode.Position,
                left.position,
                feedforward.calculate(left.velocity));
        rightLeader.setSetpoint(
                ExampleSmartMotorController.PIDMode.Position,
                right.position,
                feedforward.calculate(right.velocity));
    }
    
    
    /**
     * Returns the left encoder distance.
     *
     * @return the left encoder distance
     */
    public double getLeftEncoderDistance()
    {
        return leftLeader.getEncoderDistance();
    }
    
    
    /**
     * Returns the right encoder distance.
     *
     * @return the right encoder distance
     */
    public double getRightEncoderDistance()
    {
        return rightLeader.getEncoderDistance();
    }
    
    
    /** Resets the drive encoders. */
    public void resetEncoders()
    {
        leftLeader.resetEncoder();
        rightLeader.resetEncoder();
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
