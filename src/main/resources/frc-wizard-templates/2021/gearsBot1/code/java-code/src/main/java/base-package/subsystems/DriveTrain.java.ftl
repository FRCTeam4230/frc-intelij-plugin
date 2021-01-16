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
${data.copyright}

package ${data.basePackage}.subsystems;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import ${data.basePackage}.${data.robotClassSimpleName};
import ${data.basePackage}.commands.TankDriveWithJoystick;

/**
 * The DriveTrain subsystem incorporates the sensors and actuators attached to
 * the robots chassis. These include four drive motors, a left and right encoder
 * and a gyro.
 */
public class DriveTrain extends Subsystem
{
    private final SpeedController leftMotor
            = new SpeedControllerGroup(new PWMVictorSPX(0), new PWMVictorSPX(1));
    private final SpeedController rightMotor
            = new SpeedControllerGroup(new PWMVictorSPX(2), new PWMVictorSPX(3));

    private final DifferentialDrive drive
            = new DifferentialDrive(leftMotor, rightMotor);

    private final Encoder leftEncoder = new Encoder(1, 2);
    private final Encoder rightEncoder = new Encoder(3, 4);
    private final AnalogInput rangefinder = new AnalogInput(6);
    private final AnalogGyro gyro = new AnalogGyro(1);

    /**
     * Create a new drive train subsystem.
     */
    public DriveTrain()
    {
        super();

        // Encoders may measure differently in the real world and in
        // simulation. In this example the robot moves 0.042 barleycorns
        // per tick in the real world, but the simulated encoders
        // simulate 360 tick encoders. This if statement allows for the
        // real robot to handle this difference in devices.
        if (Robot.isReal())
        {
            leftEncoder.setDistancePerPulse(0.042);
            rightEncoder.setDistancePerPulse(0.042);
        } else
        {
            // Circumference in ft = 4in/12(in/ft)*PI
            leftEncoder.setDistancePerPulse((4.0 / 12.0 * Math.PI) / 360.0);
            rightEncoder.setDistancePerPulse((4.0 / 12.0 * Math.PI) / 360.0);
        }

        // Let's name the sensors on the LiveWindow
        addChild("Drive", drive);
        addChild("Left Encoder", leftEncoder);
        addChild("Right Encoder", rightEncoder);
        addChild("Rangefinder", rangefinder);
        addChild("Gyro", gyro);
    }

    /**
     * When no other command is running let the operator drive around using the
     * PS3 joystick.
     */
    @Override
    public void initDefaultCommand()
    {
        setDefaultCommand(new TankDriveWithJoystick());
    }

    /**
     * The log method puts interesting information to the SmartDashboard.
     */
    public void log()
    {
        SmartDashboard.putNumber("Left Distance", leftEncoder.getDistance());
        SmartDashboard.putNumber("Right Distance", rightEncoder.getDistance());
        SmartDashboard.putNumber("Left Speed", leftEncoder.getRate());
        SmartDashboard.putNumber("Right Speed", rightEncoder.getRate());
        SmartDashboard.putNumber("Gyro", gyro.getAngle());
    }

    /**
     * Tank style driving for the DriveTrain.
     *
     * @param left  Speed in range [-1,1]
     * @param right Speed in range [-1,1]
     */
    public void drive(double left, double right)
    {
        drive.tankDrive(left, right);
    }

    /**
     * Tank style driving for the DriveTrain.
     *
     * @param joy The ps3 style joystick to use to drive tank style.
     */
    public void drive(Joystick joy)
    {
        drive(-joy.getY(), -joy.getThrottle());
    }

    /**
     * Get the robot's heading.
     *
     * @return The robots heading in degrees.
     */
    public double getHeading()
    {
        return gyro.getAngle();
    }

    /**
     * Reset the robots sensors to the zero states.
     */
    public void reset()
    {
        gyro.reset();
        leftEncoder.reset();
        rightEncoder.reset();
    }

    /**
     * Get the average distance of the encoders since the last reset.
     *
     * @return The distance driven (average of left and right encoders).
     */
    public double getDistance()
    {
        return (leftEncoder.getDistance() + rightEncoder.getDistance()) / 2;
    }

    /**
     * Get the distance to the obstacle.
     *
     * @return The distance to the obstacle detected by the rangefinder.
     */
    public double getDistanceToObstacle()
    {
        // Really meters in simulation since it's a rangefinder...
        return rangefinder.getAverageVoltage();
    }
}
