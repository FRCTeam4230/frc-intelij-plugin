<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:    https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when luanching the testing instance of IntelliJ IDEA -->
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
 * An example command.  You can replace me with your own command.
 */
public class ExampleCommand extends Command
{
    public ExampleCommand()
    {
        // Use requires() here to declare subsystem dependencies
        requires(${data.robotClassSimpleName}.exampleSubsystem);
    }

    // Called just before this Command runs the first time
    @Override
    protected void initialize()
    {
        
    }

    // Called repeatedly when this Command is scheduled to run
    @Override
    protected void execute()
    {
        
    }

    // Make this return true when this Command no longer needs to run execute()
    @Override
    protected boolean isFinished()
    { 
        return false;
    }

    // Called once after isFinished returns true
    @Override
    protected void end()
    {
        
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    @Override
    protected void interrupted()
    {
        
    }
}
