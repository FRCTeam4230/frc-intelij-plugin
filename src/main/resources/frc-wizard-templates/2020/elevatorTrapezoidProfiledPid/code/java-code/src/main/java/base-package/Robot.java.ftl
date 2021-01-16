<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
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

package ${data.basePackage};

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.trajectory.TrapezoidProfile;

public class ${data.robotClassSimpleName} extends TimedRobot
{
    private static double PERIOD = 0.02;

    private final Joystick joystick = new Joystick(1);
    private final ExampleSmartMotorController motor = new ExampleSmartMotorController(1);
    // Note: These gains are fake, and will have to be tuned for your robot.
    private final SimpleMotorFeedforward feedforward = new SimpleMotorFeedforward(1, 1.5);

    private final TrapezoidProfile.Constraints constraints =
            new TrapezoidProfile.Constraints(1.75, 0.75);
    private TrapezoidProfile.State goal = new TrapezoidProfile.State();
    private TrapezoidProfile.State setpoint = new TrapezoidProfile.State();

    @Override
    public void robotInit()
    {
        // Note: These gains are fake, and will have to be tuned for your robot.
        motor.setPID(1.3, 0.0, 0.7);
    }

    @Override
    public void teleopPeriodic()
    {
        if (joystick.getRawButtonPressed(2))
        {
            goal = new TrapezoidProfile.State(5, 0);
        }
        else if (joystick.getRawButtonPressed(3))
        {
            goal = new TrapezoidProfile.State(0, 0);
        }

        // Create a motion profile with the given maximum velocity and maximum
        // acceleration constraints for the next setpoint, the desired goal, and the
        // current setpoint.
        var profile = new TrapezoidProfile(constraints, goal, setpoint);

        // Retrieve the profiled setpoint for the next time step. This setpoint moves
        // toward the goal while obeying the constraints.
        setpoint = profile.calculate(PERIOD);

        // Send setpoint to off board controller PID
        motor.setSetpoint(ExampleSmartMotorController.PIDMode.Position, setpoint.position,
                feedforward.calculate(setpoint.velocity) / 12.0);
    }
    
}
