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
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.commands;

import edu.wpi.first.wpilibj2.command.WaitCommand;
import ${data.basePackage}.subsystems.Claw;

/**
 * Opens the claw for one second. Real robots should use sensors, stalling motors is BAD!
 */
public class OpenClaw extends WaitCommand
{
    private final Claw claw;

    /**
     * Creates a new OpenClaw command.
     *
     * @param claw The claw to use
     */
    public OpenClaw(Claw claw)
    {
        super(1);
        this.claw = claw;
        addRequirements(this.claw);
    }

    // Called just before this Command runs the first time
    @Override
    public void initialize()
    {
        claw.open();
        super.initialize();
    }

    // Called once after isFinished returns true
    @Override
    public void end(boolean interrupted)
    {
        claw.stop();
    }
}
