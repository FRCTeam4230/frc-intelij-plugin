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
/* Copyright (c) 2017-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import ${data.basePackage}.subsystems.Elevator;

/**
 * Move the elevator to a given location. This command finishes when it is within the tolerance, but
 * leaves the PID loop running to maintain the position. Other commands using the elevator should
 * make sure they disable PID!
 */
public class SetElevatorSetpoint extends CommandBase
{
    private final Elevator elevator;
    private final double setpoint;

    /**
     * Create a new SetElevatorSetpoint command.
     *
     * @param setpoint The setpoint to set the elevator to
     * @param elevator The elevator to use
     */
    public SetElevatorSetpoint(double setpoint, Elevator elevator)
    {
        this.elevator = elevator;
        this.setpoint = setpoint;
        addRequirements(this.elevator);
    }

    // Called just before this Command runs the first time
    @Override
    public void initialize()
    {
        elevator.setSetpoint(setpoint);
        elevator.enable();
    }

    // Make this return true when this Command no longer needs to run execute()
    @Override
    public boolean isFinished()
    {
        return elevator.getController().atSetpoint();
    }
}
