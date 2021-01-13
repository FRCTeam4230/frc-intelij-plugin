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
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.commands;

import edu.wpi.first.wpilibj.command.CommandGroup;
import ${data.basePackage}.${data.robotClassSimpleName};

/**
 * Drive over the line and then shoot the ball. If the hot goal is not detected,
 * it will wait briefly.
 */
public class DriveAndShootAutonomous extends CommandGroup
{
    /**
     * Create a new drive and shoot autonomous.
     */
    public DriveAndShootAutonomous()
    {
        addSequential(new CloseClaw());
        addSequential(new WaitForPressure(), 2);
        if (Robot.isReal())
        {
            // NOTE: Simulation doesn't currently have the concept of hot.
            addSequential(new CheckForHotGoal(2));
        }
        addSequential(new SetPivotSetpoint(45));
        addSequential(new DriveForward(8, 0.3));
        addSequential(new Shoot());
    }
}
