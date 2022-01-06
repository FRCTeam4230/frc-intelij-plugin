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

import edu.wpi.first.wpilibj2.command.CommandBase;
import ${data.basePackage}.Robot;
import ${data.basePackage}.subsystems.Claw;

/**
 * Closes the claw for one second. Real robots should use sensors, stalling motors is BAD!
 */
public class CloseClaw extends CommandBase
{
    private final Claw claw;

    public CloseClaw(Claw claw)
    {
        this.claw = claw;
        addRequirements(this.claw);
    }

    // Called just before this Command runs the first time
    @Override
    public void initialize()
    {
        claw.close();
    }

    // Make this return true when this Command no longer needs to run execute()
    @Override
    public boolean isFinished()
    {
        return claw.isGrabbing();
    }

    // Called once after isFinished returns true
    @Override
    public void end(boolean interrupted)
    {
        // NOTE: Doesn't stop in simulation due to lower friction causing the
        // can to fall out
        // + there is no need to worry about stalling the motor or crushing the
        // can.
        if (!Robot.isSimulation())
        {
            claw.stop();
        }
    }
}
