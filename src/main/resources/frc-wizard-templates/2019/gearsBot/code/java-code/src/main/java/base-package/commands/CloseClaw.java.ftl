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
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.commands;

import edu.wpi.first.wpilibj.command.Command;
import ${data.basePackage}.${data.robotClassSimpleName};

/**
 * Closes the claw for one second. Real robots should use sensors, stalling
 * motors is BAD!
 */
public class CloseClaw extends Command
{
    public CloseClaw()
    {
        requires(Robot.claw);
    }

    // Called just before this Command runs the first time
    @Override
    protected void initialize()
    {
        Robot.claw.close();
    }

    // Make this return true when this Command no longer needs to run execute()
    @Override
    protected boolean isFinished()
    {
        return Robot.claw.isGrabbing();
    }

    // Called once after isFinished returns true
    @Override
    protected void end()
    {
        // NOTE: Doesn't stop in simulation due to lower friction causing the
        // can to fall out
        // + there is no need to worry about stalling the motor or crushing the
        // can.
        if (!Robot.isSimulation())
        {
            Robot.claw.stop();
        }
    }
}