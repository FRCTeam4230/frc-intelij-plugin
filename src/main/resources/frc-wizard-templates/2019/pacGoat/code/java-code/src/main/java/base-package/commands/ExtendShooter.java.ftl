<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Refernce: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:    https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when luanching IntelliJ IDEA -->
</#compress>
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.commands;

import edu.wpi.first.wpilibj.command.TimedCommand;
import ${data.basePackage}.${data.robotClassSimpleName};

/**
 * Extend the shooter and then retract it after a second.
 */
public class ExtendShooter extends TimedCommand
{
    public ExtendShooter()
    {
        super(1);
        requires(Robot.shooter);
    }
    
    
    // Called just before this Command runs the first time
    @Override
    protected void initialize()
    {
        Robot.shooter.extendBoth();
    }
    
    
    // Called once after isFinished returns true
    @Override
    protected void end()
    {
        Robot.shooter.retractBoth();
    }
}
