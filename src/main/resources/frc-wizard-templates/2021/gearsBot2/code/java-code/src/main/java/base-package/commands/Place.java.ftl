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

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import ${data.basePackage}.subsystems.Claw;
import ${data.basePackage}.subsystems.Elevator;
import ${data.basePackage}.subsystems.Wrist;

/**
 * Place a held soda can onto the platform.
 */
public class Place extends SequentialCommandGroup
{
    /**
     * Create a new place command.
     *
     * @param claw     The claw subsystem to use
     * @param wrist    The wrist subsystem to use
     * @param elevator The elevator subsystem to use
     */
    public Place(Claw claw, Wrist wrist, Elevator elevator)
    {
        addCommands(
                new SetElevatorSetpoint(0.25, elevator),
                new SetWristSetpoint(0, wrist),
                new OpenClaw(claw));
    }
}
