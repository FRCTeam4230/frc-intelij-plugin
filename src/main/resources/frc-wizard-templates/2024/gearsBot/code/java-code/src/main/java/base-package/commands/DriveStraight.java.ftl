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

package ${data.basePackage}.commands;

import edu.wpi.first.math.controller.PIDController;
import ${data.basePackage}.Constants.DriveStraightConstants;
import ${data.basePackage}.subsystems.Drivetrain;
import edu.wpi.first.wpilibj2.command.PIDCommand;



/**
 * Drive the given distance straight (negative values go backwards). Uses a local PID controller to
 * run a simple PID loop that is only enabled while this command is running. The input is the
 * averaged values of the left and right encoders.
 */
public class DriveStraight extends PIDCommand
{
    private final Drivetrain drivetrain;
    
    
    /**
     * Create a new DriveStraight command.
     *
     * @param distance The distance to drive
     */
    public DriveStraight(double distance, Drivetrain drivetrain)
    {
        super(
                new PIDController(
                        DriveStraightConstants.P, DriveStraightConstants.I, DriveStraightConstants.D),
                drivetrain::getDistance,
                distance,
                d -> drivetrain.drive(d, d));
        
        this.drivetrain = drivetrain;
        addRequirements(this.drivetrain);
        
        getController().setTolerance(0.01);
    }
    
    
    // Called just before this Command runs the first time
    @Override
    public void initialize()
    {
        // Get everything in a safe starting state.
        drivetrain.reset();
        super.initialize();
    }
    
    
    // Make this return true when this Command no longer needs to run execute()
    @Override
    public boolean isFinished()
    {
        return getController().atSetpoint();
    }
}
