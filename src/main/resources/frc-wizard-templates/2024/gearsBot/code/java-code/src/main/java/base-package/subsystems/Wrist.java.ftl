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
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import ${data.basePackage}.Constants.WristConstants;
import ${data.basePackage}.Robot;
import edu.wpi.first.wpilibj.motorcontrol.Victor;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.PIDSubsystem;



/**
 * The wrist subsystem is like the elevator, but with a rotational joint instead of a linear joint.
 */
public class Wrist extends PIDSubsystem
{
    private final Victor motor = new Victor(WristConstants.MOTOR_PORT);
    private final AnalogPotentiometer pot;
    
    
    /** Create a new wrist subsystem. */
    public Wrist()
    {
        super(new PIDController(WristConstants.P, WristConstants.I, WristConstants.D));
        getController().setTolerance(WristConstants.TOLERANCE);
        
        // Conversion value of potentiometer varies between the real world and
        // simulation
        if (Robot.isReal())
        {
            pot = new AnalogPotentiometer(WristConstants.POTENTIOMETER_PORT, -270.0 / 5);
        }
        else
        {
            // Defaults to degrees
            pot = new AnalogPotentiometer(WristConstants.POTENTIOMETER_PORT);
        }
        
        // Let's name everything on the LiveWindow
        addChild("Motor", motor);
        addChild("Pot", pot);
    }
    
    
    /** The log method puts interesting information to the SmartDashboard. */
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
    
    
    /** Use the motor as the PID output. This method is automatically called by the subsystem. */
    @Override
    public void useOutput(double output, double setpoint)
    {
        motor.set(output);
    }
    
    
    /** Call log method every loop. */
    @Override
    public void periodic()
    {
        log();
    }
}
