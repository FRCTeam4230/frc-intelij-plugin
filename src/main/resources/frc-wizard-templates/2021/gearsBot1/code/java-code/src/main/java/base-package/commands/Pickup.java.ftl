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
 * Pickup a soda can (if one is between the open claws) and get it in a safe
 * state to drive around.
 */
public class Pickup extends CommandGroup
{
    /**
     * Create a new pickup command.
     */
    public Pickup()
    {
        addSequential(new CloseClaw());
        addParallel(new SetWristSetpoint(-45));
        addSequential(new SetElevatorSetpoint(0.25));
    }
}
