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

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj2.command.PIDSubsystem;

import static ${data.basePackage}.Constants.ShooterConstants.*;

public class ShooterSubsystem extends PIDSubsystem
{
    private final PWMVictorSPX shooterMotor = new PWMVictorSPX(SHOOTER_MOTOR_PORT);
    private final PWMVictorSPX feederMotor = new PWMVictorSPX(FEEDER_MOTOR_PORT);
    private final Encoder shooterEncoder =
            new Encoder(ENCODER_PORTS[0], ENCODER_PORTS[1], ENCODER_REVERSED);
    private final SimpleMotorFeedforward shooterFeedForward
            = new SimpleMotorFeedforward(S_VOLTS, V_VOLT_SECONDS_PER_ROTATION);

    /**
     * The shooter subsystem for the robot.
     */
    public ShooterSubsystem()
    {
        super(new PIDController(P, I, D));
        getController().setTolerance(SHOOTER_TOLERANCE_RPS);
        shooterEncoder.setDistancePerPulse(ENCODER_DISTANCE_PER_PULSE);
        setSetpoint(SHOOTER_TARGET_RPS);
    }

    @Override
    public void useOutput(double output, double setpoint)
    {
        shooterMotor.setVoltage(output + shooterFeedForward.calculate(setpoint));
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
        feederMotor.set(FEEDER_SPEED);
    }

    public void stopFeeder()
    {
        feederMotor.set(0);
    }
}
