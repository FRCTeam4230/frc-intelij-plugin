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
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.subsystems;
import edu.wpi.first.wpilibj.controller.ArmFeedforward;
import edu.wpi.first.wpilibj.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.TrapezoidProfileSubsystem;
import ${data.basePackage}.ExampleSmartMotorController;

import static ${data.basePackage}.Constants.ArmConstants.*;

/**
 * A robot arm subsystem that moves with a motion profile.
 */
public class ArmSubsystem extends TrapezoidProfileSubsystem
{
    private final ExampleSmartMotorController motor = new ExampleSmartMotorController(MOTOR_PORT);
    private final ArmFeedforward feedforward =
            new ArmFeedforward(S_VOLTS, COS_VOLTS, V_VOLT_SECOND_PER_RAD, A_VOLT_SECOND_SQUARED_PER_RAD);

    /**
     * Create a new ArmSubsystem.
     */
    public ArmSubsystem()
    {
        super(new TrapezoidProfile.Constraints(MAX_VELOCITY_RAD_PER_SECOND, MAX_ACCELERATION_RAD_PER_SEC_SQUARED),
                ARM_OFFSET_RADS);
        motor.setPID(P, 0, 0);
    }

    @Override
    public void useState(TrapezoidProfile.State setpoint)
    {
        // Calculate the feedforward from the sepoint
        double feedforward = this.feedforward.calculate(setpoint.position, setpoint.velocity);
        // Add the feedforward to the PID output to get the motor output
        motor.setSetpoint(ExampleSmartMotorController.PIDMode.Position,
                setpoint.position,
                feedforward / 12.0);
    }
}
