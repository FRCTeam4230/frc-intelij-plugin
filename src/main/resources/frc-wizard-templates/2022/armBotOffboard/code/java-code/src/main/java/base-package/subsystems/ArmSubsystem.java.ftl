<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:     https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when launching the testing instance of IntelliJ IDEA -->
</#compress>
${data.copyright}

package ${data.basePackage}.subsystems;

import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import ${data.basePackage}.Constants.ArmConstants;
import ${data.basePackage}.ExampleSmartMotorController;
import edu.wpi.first.wpilibj2.command.TrapezoidProfileSubsystem;



/** A robot arm subsystem that moves with a motion profile. */
public class ArmSubsystem extends TrapezoidProfileSubsystem
{
    private final ExampleSmartMotorController motor =
            new ExampleSmartMotorController(ArmConstants.MOTOR_PORT);
    private final ArmFeedforward feedforward =
            new ArmFeedforward(
                    ArmConstants.S_VOLTS, ArmConstants.COS_VOLTS,
                    ArmConstants.V_VOLT_SECOND_PER_RAD, ArmConstants.A_VOLT_SECOND_SQUARED_PER_RAD);
    
    
    /** Create a new ArmSubsystem. */
    public ArmSubsystem()
    {
        super(
                new TrapezoidProfile.Constraints(
                        ArmConstants.MAX_VELOCITY_RAD_PER_SECOND, ArmConstants.MAX_ACCELERATION_RAD_PER_SEC_SQUARED),
                ArmConstants.ARM_OFFSET_RADS);
        motor.setPID(ArmConstants.P, 0, 0);
    }
    
    
    @Override
    public void useState(TrapezoidProfile.State setpoint)
    {
        // Calculate the feedforward from the setpoint
        double feedforward = this.feedforward.calculate(setpoint.position, setpoint.velocity);
        // Add the feedforward to the PID output to get the motor output
        motor.setSetpoint(
                ExampleSmartMotorController.PIDMode.Position, setpoint.position, feedforward / 12.0);
    }
}
