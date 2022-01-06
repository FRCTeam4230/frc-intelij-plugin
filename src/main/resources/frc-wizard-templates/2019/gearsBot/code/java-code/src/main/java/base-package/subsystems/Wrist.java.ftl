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
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.subsystems;

import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.command.PIDSubsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import ${data.basePackage}.${data.robotClassSimpleName};

/**
 * The wrist subsystem is like the elevator, but with a rotational joint instead
 * of a linear joint.
 */
public class Wrist extends PIDSubsystem
{
    private final Victor motor;
    private final AnalogPotentiometer pot;

    private static final double P_REAL = 1;
    private static final double P_SIMULATION = 0.05;

    /**
     * Create a new wrist subsystem.
     */
    public Wrist()
    {
        super(P_REAL, 0, 0);
        if (Robot.isSimulation())
        { // Check for simulation and update PID values
            getPIDController().setPID(P_SIMULATION, 0, 0, 0);
        }
        setAbsoluteTolerance(2.5);

        motor = new Victor(6);

        // Conversion value of potentiometer varies between the real world and
        // simulation
        if (Robot.isReal())
        {
            pot = new AnalogPotentiometer(3, -270.0 / 5);
        } else
        {
            pot = new AnalogPotentiometer(3); // Defaults to degrees
        }

        // Let's name everything on the LiveWindow
        addChild("Motor", motor);
        addChild("Pot", pot);
    }

    @Override
    public void initDefaultCommand()
    {
    }

    /**
     * The log method puts interesting information to the SmartDashboard.
     */
    public void log()
    {
        SmartDashboard.putData("Wrist Angle", pot);
    }

    /**
     * Use the potentiometer as the PID sensor. This method is automatically
     * called by the subsystem.
     */
    @Override
    protected double returnPIDInput()
    {
        return pot.get();
    }

    /**
     * Use the motor as the PID output. This method is automatically called by
     * the subsystem.
     */
    @Override
    protected void usePIDOutput(double power)
    {
        motor.set(power);
    }
}
