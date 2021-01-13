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

import edu.wpi.first.wpilibj.command.Command;
import ${data.basePackage}.${data.robotClassSimpleName};

/**
 * Move the elevator to a given location. This command finishes when it is
 * within the tolerance, but leaves the PID loop running to maintain the
 * position. Other commands using the elevator should make sure they disable
 * PID!
 */
public class SetElevatorSetpoint extends Command
{
    private final double setpoint;

    public SetElevatorSetpoint(double setpoint)
    {
        this.setpoint = setpoint;
        requires(Robot.elevator);
    }

    // Called just before this Command runs the first time
    @Override
    protected void initialize()
    {
        Robot.elevator.enable();
        Robot.elevator.setSetpoint(setpoint);
    }

    // Make this return true when this Command no longer needs to run execute()
    @Override
    protected boolean isFinished()
    {
        return Robot.elevator.onTarget();
    }
}
