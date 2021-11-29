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

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

/**
 * This is a sample program to demonstrate how to use a gyro sensor to make a
 * robot drive straight. This program uses a joystick to drive forwards and
 * backwards while the gyro is used for direction keeping.
 * <p>
 * The VM is configured to automatically run this class, and to call the
 * methods corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    private static final double ANGLE_SETPOINT = 0.0;
    /** Proportional turning constant. */
    private static final double P = 0.005;

    /**
     * Gyro calibration constant, may need to be adjusted.
     * Gyro value of 360 is set to correspond to one full revolution.
     */
    private static final double VOLTS_PER_DEGREE_PER_SECOND = 0.0128;

    private static final int LEFT_MOTOR_PORT = 0;
    private static final int RIGHT_MOTOR_PORT = 1;
    private static final int GYRO_PORT = 0;
    private static final int JOYSTICK_PORT = 0;

    private final DifferentialDrive myRobot
            = new DifferentialDrive(new PWMVictorSPX(LEFT_MOTOR_PORT), new PWMVictorSPX(RIGHT_MOTOR_PORT));
    private final AnalogGyro gyro = new AnalogGyro(GYRO_PORT);
    private final Joystick joystick = new Joystick(JOYSTICK_PORT);

    @Override
    public void robotInit()
    {
        gyro.setSensitivity(VOLTS_PER_DEGREE_PER_SECOND);
    }

    /**
     * The motor speed is set from the joystick while the RobotDrive turning
     * value is assigned from the error between the setpoint and the gyro angle.
     * <p>
     * This method is called periodically during teleoperated mode.
     */
    @Override
    public void teleopPeriodic()
    {
        double turningValue = (ANGLE_SETPOINT - gyro.getAngle()) * P;
        // Invert the direction of the turn if we are going backwards
        turningValue = Math.copySign(turningValue, joystick.getY());
        myRobot.arcadeDrive(joystick.getY(), turningValue);
    }
}
