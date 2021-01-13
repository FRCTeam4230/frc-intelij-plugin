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
 * The elevator subsystem uses PID to go to a given height. Unfortunately, in
 * it's current state PID values for simulation are different than in the real
 * world do to minor differences.
 */
public class Elevator extends PIDSubsystem
{
    private final Victor motor;
    private final AnalogPotentiometer pot;

    private static final double P_REAL = 4;
    private static final double I_REAL = 0.07;
    private static final double P_SIMULATION = 18;
    private static final double I_SIMULATION = 0.2;

    /**
     * Create a new elevator subsystem.
     */
    public Elevator()
    {
        super(P_REAL, I_REAL, 0);
        if (Robot.isSimulation())
        { // Check for simulation and update PID values
            getPIDController().setPID(P_SIMULATION, I_SIMULATION, 0, 0);
        }
        setAbsoluteTolerance(0.005);

        motor = new Victor(5);

        // Conversion value of potentiometer varies between the real world and
        // simulation
        if (Robot.isReal())
        {
            pot = new AnalogPotentiometer(2, -2.0 / 5);
        } else
        {
            pot = new AnalogPotentiometer(2); // Defaults to meters
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
        SmartDashboard.putData("Elevator Pot", (AnalogPotentiometer) pot);
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
