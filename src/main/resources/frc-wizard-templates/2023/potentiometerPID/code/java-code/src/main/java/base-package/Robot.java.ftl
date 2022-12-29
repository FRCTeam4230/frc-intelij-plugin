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

package ${data.basePackage};

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;



/**
 * This is a sample program to demonstrate how to use a soft potentiometer and a PID controller to
 * reach and maintain position setpoints on an elevator mechanism.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    private static final int POT_CHANNEL = 1;
    private static final int MOTOR_CHANNEL = 7;
    private static final int JOYSTICK_CHANNEL = 0;
    
    // bottom, middle, and top elevator setpoints
    private static final double[] SET_POINTS = {1.0, 2.6, 4.3};
    
    // proportional, integral, and derivative speed constants; motor inverted
    // DANGER: when tuning PID constants, high/inappropriate values for P, I,
    // and D may cause dangerous, uncontrollable, or undesired behavior!
    // these may need to be positive for a non-inverted motor
    private static final double P = -5.0;
    private static final double I = -0.02;
    private static final double D = -2.0;
    
    private PIDController pidController;
    private AnalogInput potentiometer;
    private MotorController elevatorMotor;
    private Joystick joystick;
    
    private int index;
    private boolean previousButtonValue;
    
    
    @Override
    public void robotInit()
    {
        potentiometer = new AnalogInput(POT_CHANNEL);
        elevatorMotor = new PWMSparkMax(MOTOR_CHANNEL);
        joystick = new Joystick(JOYSTICK_CHANNEL);
        
        pidController = new PIDController(P, I, D);
        pidController.setSetpoint(SET_POINTS[index]);
    }
    
    
    @Override
    public void teleopPeriodic()
    {
        // Run the PID Controller
        double pidOut = pidController.calculate(potentiometer.getAverageVoltage());
        elevatorMotor.set(pidOut);
        
        // when the button is pressed once, the selected elevator setpoint
        // is incremented
        boolean currentButtonValue = joystick.getTrigger();
        if (currentButtonValue && !previousButtonValue)
        {
            // index of the elevator setpoint wraps around.
            index = (index + 1) % SET_POINTS.length;
            pidController.setSetpoint(SET_POINTS[index]);
        }
        previousButtonValue = currentButtonValue;
    }
}
