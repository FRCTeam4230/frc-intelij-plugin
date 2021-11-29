<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
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

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.PIDController;
import edu.wpi.first.wpilibj.PIDOutput;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

/**
 * This is a sample program to demonstrate the use of a PIDController with an
 * ultrasonic sensor to reach and maintain a set distance from an object.
 * <p>
 * The VM is configured to automatically run this class, and to call the
 * methods corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    // distance in inches the robot wants to stay from an object
    private static final double HOLD_DISTANCE = 12.0;

    // factor to convert sensor values to a distance in inches
    private static final double VALUE_TO_INCHES = 0.125;

    // proportional speed constant
    private static final double P = 7.0;

    // integral speed constant
    private static final double I = 0.018;

    // derivative speed constant
    private static final double D = 1.5;

    private static final int LEFT_MOTOR_PORT = 0;
    private static final int RIGHT_MOTOR_PORT = 1;
    private static final int ULTRASONIC_PORT = 0;

    // median filter to discard outliers; filters over 5 samples
    private final MedianFilter filter = new MedianFilter(5);

    private final AnalogInput ultrasonic = new AnalogInput(ULTRASONIC_PORT);
    private final DifferentialDrive robotDrive
            = new DifferentialDrive(new PWMVictorSPX(LEFT_MOTOR_PORT), new PWMVictorSPX(RIGHT_MOTOR_PORT));
    private final PIDController pidController = new PIDController(P, I, D);

    @Override
    public void teleopInit()
    {
        // Set setpoint of the pid controller
        pidController.setSetpoint(HOLD_DISTANCE * VALUE_TO_INCHES);
    }

    @Override
    public void teleopPeriodic()
    {
        // returned value is filtered with a rolling median filter, since ultrasonics
        // tend to be quite noisy and susceptible to sudden outliers
        double pidOutput = pidController.calculate(filter.calculate(ultrasonic.getVoltage()));

        robotDrive.arcadeDrive(pidOutput, 0);
    }
}
