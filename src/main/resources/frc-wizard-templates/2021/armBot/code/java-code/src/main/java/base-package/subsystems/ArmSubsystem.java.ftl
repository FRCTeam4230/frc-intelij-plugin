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
${data.copyright}

package ${data.basePackage}.subsystems;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.controller.ArmFeedforward;
import edu.wpi.first.wpilibj.controller.ProfiledPIDController;
import edu.wpi.first.wpilibj.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.ProfiledPIDSubsystem;

import static ${data.basePackage}.Constants.ArmConstants.*;

/**
 * A robot arm subsystem that moves with a motion profile.
 */
public class ArmSubsystem extends ProfiledPIDSubsystem
{
    private final PWMVictorSPX motor = new PWMVictorSPX(MOTOR_PORT);
    private final Encoder encoder = new Encoder(ENCODER_PORTS[0], ENCODER_PORTS[1]);
    private final ArmFeedforward feedforward =
            new ArmFeedforward(S_VOLTS, COS_VOLTS, V_VOLT_SECOND_PER_RAD, A_VOLT_SECOND_SQUARED_PER_RAD);

    /**
     * Create a new ArmSubsystem.
     */
    public ArmSubsystem()
    {
        super(new ProfiledPIDController(P, 0, 0,
                                        new TrapezoidProfile.Constraints(MAX_VELOCITY_RAD_PER_SECOND, MAX_ACCELERATION_RAD_PER_SEC_SQUARED)), 0);
        encoder.setDistancePerPulse(ENCODER_DISTANCE_PER_PULSE);
        // Start arm at rest in neutral position
        setGoal(ARM_OFFSET_RADS);
    }

    @Override
    public void useOutput(double output, TrapezoidProfile.State setpoint)
    {
        // Calculate the feedforward from the setpoint
        double feedforward = this.feedforward.calculate(setpoint.position, setpoint.velocity);
        // Add the feedforward to the PID output to get the motor output
        motor.setVoltage(output + feedforward);
    }

    @Override
    public double getMeasurement()
    {
        return encoder.getDistance() + ARM_OFFSET_RADS;
    }
}
