<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:    https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when luanching IntelliJ IDEA -->
</#compress>
${data.copyright}

package ${data.basePackage}.commands;

import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDSource;
import edu.wpi.first.wpilibj.PIDSourceType;
import edu.wpi.first.wpilibj.command.Command;
import ${data.basePackage}.${data.robotClassSimpleName};

/**
 * Drive the given distance straight (negative values go backwards). Uses a
 * local PID controller to run a simple PID loop that is only enabled while this
 * command is running. The input is the averaged values of the left and right
 * encoders.
 */
public class DriveStraight extends Command
{
    private final PIDController pid;

    /**
     * Create a new DriveStraight command.
     *
     * @param distance The distance to drive
     */
    public DriveStraight(double distance)
    {
        requires(Robot.drivetrain);
        pid = new PIDController(4, 0, 0, new PIDSource()
        {
            PIDSourceType sourceType = PIDSourceType.kDisplacement;

            @Override
            public double pidGet()
            {
                return Robot.drivetrain.getDistance();
            }

            @Override
            public void setPIDSourceType(PIDSourceType pidSource)
            {
                sourceType = pidSource;
            }

            @Override
            public PIDSourceType getPIDSourceType()
            {
                return sourceType;
            }
        }, d -> Robot.drivetrain.drive(d, d));

        pid.setAbsoluteTolerance(0.01);
        pid.setSetpoint(distance);
    }

    // Called just before this Command runs the first time
    @Override
    protected void initialize()
    {
        // Get everything in a safe starting state.
        Robot.drivetrain.reset();
        pid.reset();
        pid.enable();
    }

    // Make this return true when this Command no longer needs to run execute()
    @Override
    protected boolean isFinished()
    {
        return pid.onTarget();
    }

    // Called once after isFinished returns true
    @Override
    protected void end()
    {
        // Stop PID and the wheels
        pid.disable();
        Robot.drivetrain.drive(0, 0);
    }
}
