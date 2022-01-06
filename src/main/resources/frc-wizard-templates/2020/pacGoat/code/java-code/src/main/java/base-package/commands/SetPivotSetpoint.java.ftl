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
 * Moves the pivot to a given angle. This command finishes when it is within the
 * tolerance, but leaves the PID loop running to maintain the position. Other
 * commands using the pivot should make sure they disable PID!
 */
public class SetPivotSetpoint extends Command
{
    private final double setpoint;
    
    
    public SetPivotSetpoint(double setpoint)
    {
        this.setpoint = setpoint;
        requires(Robot.pivot);
    }
    
    
    // Called just before this Command runs the first time
    @Override
    protected void initialize()
    {
        Robot.pivot.enable();
        Robot.pivot.setSetpoint(setpoint);
    }
    
    
    // Make this return true when this Command no longer needs to run execute()
    @Override
    protected boolean isFinished()
    {
        return Robot.pivot.onTarget();
    }
}
