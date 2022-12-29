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
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.Encoder;
import ${data.basePackage}.Constants.ArmConstants;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.ProfiledPIDSubsystem;



/** A robot arm subsystem that moves with a motion profile. */
public class ArmSubsystem extends ProfiledPIDSubsystem
{
    private final PWMSparkMax motor = new PWMSparkMax(ArmConstants.MOTOR_PORT);
    private final Encoder encoder =
            new Encoder(ArmConstants.ENCODER_PORTS[0], ArmConstants.ENCODER_PORTS[1]);
    private final ArmFeedforward feedforward =
            new ArmFeedforward(
                    ArmConstants.S_VOLTS, ArmConstants.G_VOLTS,
                    ArmConstants.V_VOLT_SECOND_PER_RAD, ArmConstants.A_VOLT_SECOND_SQUARED_PER_RAD);
    
    
    /** Create a new ArmSubsystem. */
    public ArmSubsystem()
    {
        super(
                new ProfiledPIDController(
                        ArmConstants.P,
                        0,
                        0,
                        new TrapezoidProfile.Constraints(
                                ArmConstants.MAX_VELOCITY_RAD_PER_SECOND,
                                ArmConstants.MAX_ACCELERATION_RAD_PER_SEC_SQUARED)),
                0);
        encoder.setDistancePerPulse(ArmConstants.ENCODER_DISTANCE_PER_PULSE);
        // Start arm at rest in neutral position
        setGoal(ArmConstants.ARM_OFFSET_RADS);
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
        return encoder.getDistance() + ArmConstants.ARM_OFFSET_RADS;
    }
}
