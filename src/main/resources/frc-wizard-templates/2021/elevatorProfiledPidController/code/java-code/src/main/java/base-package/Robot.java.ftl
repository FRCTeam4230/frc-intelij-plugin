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
${data.copyright}

package ${data.basePackage};

import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.controller.ProfiledPIDController;
import edu.wpi.first.wpilibj.trajectory.TrapezoidProfile;

public class ${data.robotClassSimpleName} extends TimedRobot
{
   private static double PERIOD = 0.02;

    private final Joystick joystick = new Joystick(1);
    private final Encoder encoder = new Encoder(1, 2);
    private final SpeedController motor = new PWMVictorSPX(1);

    // Create a PID controller whose setpoint's change is subject to maximum
    // velocity and acceleration constraints.
    private final TrapezoidProfile.Constraints constraints = new TrapezoidProfile.Constraints(1.75, 0.75);
    private final ProfiledPIDController controller =new ProfiledPIDController(1.3, 0.0, 0.7, constraints, PERIOD);

    @Override
    public void robotInit()
    {
        encoder.setDistancePerPulse(1.0 / 360.0 * 2.0 * Math.PI * 1.5);
    }

    @Override
    public void teleopPeriodic()
    {
        if (joystick.getRawButtonPressed(2))
        {
            controller.setGoal(5);
        }
        else if (joystick.getRawButtonPressed(3))
        {
            controller.setGoal(0);
        }

        // Run controller and update motor output
        motor.set(controller.calculate(encoder.getDistance()));
    }    
}
