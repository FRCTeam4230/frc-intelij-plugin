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
import ${data.basePackage}.subsystems.Collector;
import ${data.basePackage}.subsystems.Pivot;

/**
 * Get the robot set to collect balls.
 */
public class Collect extends CommandGroup
{
    /**
     * Create a new collect command.
     */
    public Collect()
    {
        addSequential(new SetCollectionSpeed(Collector.FORWARD));
        addParallel(new CloseClaw());
        addSequential(new SetPivotSetpoint(Pivot.COLLECT));
        addSequential(new WaitForBall());
    }
}
