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

import edu.wpi.first.wpilibj2.command.CommandBase;
import ${data.basePackage}.subsystems.Wrist;


/**
 * Move the wrist to a given angle. This command finishes when it is within the tolerance, but
 * leaves the PID loop running to maintain the position. Other commands using the wrist should make
 * sure they disable PID!
 */
public class SetWristSetpoint extends CommandBase
{
    private final Wrist wrist;
    private final double setpoint;

    /**
     * Create a new SetWristSetpoint command.
     *
     * @param setpoint The setpoint to set the wrist to
     * @param wrist    The wrist to use
     */
    public SetWristSetpoint(double setpoint, Wrist wrist)
    {
        this.wrist = wrist;
        this.setpoint = setpoint;
        addRequirements(this.wrist);
    }

    // Called just before this Command runs the first time
    @Override
    public void initialize()
    {
        wrist.enable();
        wrist.setSetpoint(setpoint);
    }

    // Make this return true when this Command no longer needs to run execute()
    @Override
    public boolean isFinished()
    {
        return wrist.getController().atSetpoint();
    }
}
