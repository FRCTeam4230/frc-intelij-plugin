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

import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 * Make sure the robot is in a state to pickup soda cans.
 */
public class PrepareToPickup extends CommandGroup
{
    /**
     * Create a new prepare to pickup command.
     */
    public PrepareToPickup()
    {
        addParallel(new OpenClaw());
        addParallel(new SetWristSetpoint(0));
        addSequential(new SetElevatorSetpoint(0));
    }
}
