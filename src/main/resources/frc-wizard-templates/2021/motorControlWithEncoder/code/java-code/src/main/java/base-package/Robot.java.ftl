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
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * This sample program shows how to control a motor using a joystick. In the
 * operator control part of the program, the joystick is read and the value is
 * written to the motor.
 * <p>
 * Joystick analog values range from -1 to 1 and speed controller inputs also
 * range from -1 to 1 making it easy to work together.
 * <p>
 * In addition, the encoder value of an encoder connected to ports 0 and 1 is
 * consistently sent to the Dashboard.
 * <p>
 * The VM is configured to automatically run this class, and to call the
 * methods corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    private static final int MOTOR_PORT = 0;
    private static final int JOYSTICK_PORT = 0;
    private static final int ENCODER_PORT_A = 0;
    private static final int ENCODER_PORT_B = 1;

    private SpeedController motor;
    private Joystick joystick;
    private Encoder encoder;

    @Override
    public void robotInit()
    {
        motor = new PWMVictorSPX(MOTOR_PORT);
        joystick = new Joystick(JOYSTICK_PORT);
        encoder = new Encoder(ENCODER_PORT_A, ENCODER_PORT_B);
        // Use SetDistancePerPulse to set the multiplier for GetDistance
        // This is set up assuming a 6 inch wheel with a 360 CPR encoder.
        encoder.setDistancePerPulse((Math.PI * 6) / 360.0);
    }

    /*
     * The robotPeriodic method is called every control packet no matter the robot mode.
     */
    @Override
    public void robotPeriodic()
    {
        SmartDashboard.putNumber("Encoder", encoder.getDistance());
    }

    /**
     * This method is called periodically during test mode.
     */
    @Override
    public void teleopPeriodic()
    {
        motor.set(joystick.getY());
    }
}
