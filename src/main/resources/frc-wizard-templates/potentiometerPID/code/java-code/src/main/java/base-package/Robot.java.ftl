<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Refernce: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:    https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when luanching the testing instance of IntelliJ IDEA -->
</#compress>
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage};

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

/**
 * This is a sample program to demonstrate how to use a soft potentiometer and a
 * PID controller to reach and maintain position setpoints on an elevator mechanism.
 * <p>
 * The VM is configured to automatically run this class, and to call the
 * methods corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    private static final int POT_CHANNEL = 1;
    private static final int MOTOR_CHANNEL = 7;
    private static final int JOYSTICK_CHANNEL = 0;

    /** Bottom, middle, and top elevator setpoint. */
    private static final double[] SET_POINTS = {1.0, 2.6, 4.3};

    // proportional, integral, and derivative speed constants; motor inverted
    // DANGER: when tuning PID constants, high/inappropriate values for P, I,
    // and D may cause dangerous, uncontrollable, or undesired behavior!
    // These may need to be positive for a non-inverted motor
    /** Proportional speed constant; motor inverted. May need to be positive for a non-inverted motor */
    private static final double P = -5.0;
    /** Integral speed constant; motor inverted. May need to be positive for a non-inverted motor */
    private static final double I = -0.02;
    /** Derivative speed constant; motor inverted. May need to be positive for a non-inverted motor */
    private static final double D = -2.0;

    private PIDController pidController;
    @SuppressWarnings({"PMD.SingularField", "FieldCanBeLocal"})
    private AnalogInput potentiometer;
    @SuppressWarnings({"PMD.SingularField", "FieldCanBeLocal"})
    private SpeedController elevatorMotor;
    private Joystick joystick;

    private int index;
    private boolean previousButtonValue;

    /**
     * This method is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit()
    {
        potentiometer = new AnalogInput(POT_CHANNEL);
        elevatorMotor = new PWMVictorSPX(MOTOR_CHANNEL);
        joystick = new Joystick(JOYSTICK_CHANNEL);

        pidController = new PIDController(P, I, D, potentiometer, elevatorMotor);
        pidController.setInputRange(0, 5);
    }

    /**
     * This method is called once each time the robot enters teleoperated mode.
     */
    @Override
    public void teleopInit()
    {
        pidController.enable();
    }

    /**
     * This method is called periodically during teleoperated mode.
     */
    @Override
    public void teleopPeriodic()
    {
        // when the button is pressed once, the selected elevator setpoint is incremented
        boolean currentButtonValue = joystick.getTrigger();
        if (currentButtonValue && !previousButtonValue)
        {
            // index of the elevator setpoint wraps around.
            index = (index + 1) % SET_POINTS.length;
        }
        previousButtonValue = currentButtonValue;

        pidController.setSetpoint(SET_POINTS[index]);
    }
}

