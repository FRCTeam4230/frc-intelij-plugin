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

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.Encoder;
import ${data.basePackage}.Constants.ShooterConstants;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj2.command.PIDSubsystem;



public class ShooterSubsystem extends PIDSubsystem
{
    private final PWMSparkMax shooterMotor = new PWMSparkMax(ShooterConstants.SHOOTER_MOTOR_PORT);
    private final PWMSparkMax feederMotor = new PWMSparkMax(ShooterConstants.FEEDER_MOTOR_PORT);
    private final Encoder shooterEncoder =
            new Encoder(
                    ShooterConstants.ENCODER_PORTS[0],
                    ShooterConstants.ENCODER_PORTS[1],
                    ShooterConstants.ENCODER_REVERSED);
    private final SimpleMotorFeedforward shooterFeedforward =
            new SimpleMotorFeedforward(
                    ShooterConstants.S_VOLTS, ShooterConstants.V_VOLT_SECONDS_PER_ROTATION);
    
    
    /** The shooter subsystem for the robot. */
    public ShooterSubsystem()
    {
        super(new PIDController(ShooterConstants.P, ShooterConstants.I, ShooterConstants.D));
        getController().setTolerance(ShooterConstants.SHOOTER_TOLERANCE_RPS);
        shooterEncoder.setDistancePerPulse(ShooterConstants.ENCODER_DISTANCE_PER_PULSE);
        setSetpoint(ShooterConstants.SHOOTER_TARGET_RPS);
    }
    
    
    @Override
    public void useOutput(double output, double setpoint)
    {
        shooterMotor.setVoltage(output + shooterFeedforward.calculate(setpoint));
    }
    
    
    @Override
    public double getMeasurement()
    {
        return shooterEncoder.getRate();
    }
    
    
    public boolean atSetpoint()
    {
        return m_controller.atSetpoint();
    }
    
    
    public void runFeeder()
    {
        feederMotor.set(ShooterConstants.FEEDER_SPEED);
    }
    
    
    public void stopFeeder()
    {
        feederMotor.set(0);
    }
}
