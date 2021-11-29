<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:     https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when launching IntelliJ IDEA -->
</#compress>
${data.copyright}

package ${data.basePackage}.commands;

import edu.wpi.first.wpilibj.command.Command;
import ${data.basePackage}.${data.robotClassSimpleName};

/**
 * Move the wrist to a given angle. This command finishes when it is within the
 * tolerance, but leaves the PID loop running to maintain the position. Other
 * commands using the wrist should make sure they disable PID!
 */
public class SetWristSetpoint extends Command
{
    private final double setpoint;

    public SetWristSetpoint(double setpoint)
    {
        this.setpoint = setpoint;
        requires(Robot.wrist);
    }

    // Called just before this Command runs the first time
    @Override
    protected void initialize()
    {
        Robot.wrist.enable();
        Robot.wrist.setSetpoint(setpoint);
    }

    // Make this return true when this Command no longer needs to run execute()
    @Override
    protected boolean isFinished()
    {
        return Robot.wrist.onTarget();
    }
}
