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

import edu.wpi.first.wpilibj.command.InstantCommand;
import ${data.basePackage}.${data.robotClassSimpleName};

/**
 * Close the claw.
 * <p>
 * NOTE: It doesn't wait for the claw to close since there is no sensor to
 * detect that.
 */
public class CloseClaw extends InstantCommand
{
    public CloseClaw()
    {
        requires(Robot.collector);
    }

    // Called just before this Command runs the first time
    @Override
    protected void initialize()
    {
        Robot.collector.close();
    }
}
