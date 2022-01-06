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
${data.copyright}

package ${data.basePackage}.subsystems;

import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.PIDSubsystem;
import ${data.basePackage}.Robot;

/**
 * The wrist subsystem is like the elevator, but with a rotational joint instead of a linear joint.
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
        super(new PIDController(P_REAL, 0, 0));
        if (Robot.isSimulation())
        { // Check for simulation and update PID values
            getController().setPID(P_SIMULATION, 0, 0);
        }
        getController().setTolerance(2.5);

        motor = new Victor(6);

        // Conversion value of potentiometer varies between the real world and
        // simulation
        if (Robot.isReal())
        {
            pot = new AnalogPotentiometer(3, -270.0 / 5);
        }
        else
        {
            pot = new AnalogPotentiometer(3); // Defaults to degrees
        }

        // Let's name everything on the LiveWindow
        addChild("Motor", motor);
        addChild("Pot", pot);
    }

    /**
     * The log method puts interesting information to the SmartDashboard.
     */
    public void log()
    {
        SmartDashboard.putData("Wrist Angle", pot);
    }

    /**
     * Use the potentiometer as the PID sensor. This method is automatically called by the subsystem.
     */
    @Override
    public double getMeasurement()
    {
        return pot.get();
    }

    /**
     * Use the motor as the PID output. This method is automatically called by the subsystem.
     */
    @Override
    public void useOutput(double output, double setpoint)
    {
        motor.set(output);
    }
}
