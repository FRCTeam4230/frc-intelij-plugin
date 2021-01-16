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
 * Spit the ball out into the low goal assuming that the robot is in front of
 * it.
 */
public class LowGoal extends CommandGroup
{
    /**
     * Create a new low goal command.
     */
    public LowGoal()
    {
        addSequential(new SetPivotSetpoint(Pivot.LOW_GOAL));
        addSequential(new SetCollectionSpeed(Collector.REVERSE));
        addSequential(new ExtendShooter());
    }
}
