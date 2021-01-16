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
/* Copyright (c) 2017-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.commands;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import ${data.basePackage}.subsystems.Claw;
import ${data.basePackage}.subsystems.Elevator;
import ${data.basePackage}.subsystems.Wrist;

/**
 * Make sure the robot is in a state to pickup soda cans.
 */
public class PrepareToPickup extends SequentialCommandGroup
{
    /**
     * Create a new prepare to pickup command.
     *
     * @param claw     The claw subsystem to use
     * @param wrist    The wrist subsystem to use
     * @param elevator The elevator subsystem to use
     */
    public PrepareToPickup(Claw claw, Wrist wrist, Elevator elevator)
    {
        addCommands(
                new OpenClaw(claw),
                parallel(
                        new SetWristSetpoint(0, wrist),
                        new SetElevatorSetpoint(0, elevator)));
    }
}
