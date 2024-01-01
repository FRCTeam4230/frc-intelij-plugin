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

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import ${data.basePackage}.Constants.DriveConstants;
import ${data.basePackage}.Robot;
import edu.wpi.first.wpilibj.motorcontrol.MotorController;
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;



public class Drivetrain extends SubsystemBase
{
    /**
     * The Drivetrain subsystem incorporates the sensors and actuators attached to the robots chassis.
     * These include four drive motors, a left and right encoder and a gyro.
     */
    private final MotorController leftMotor =
            new MotorControllerGroup(
                    new PWMSparkMax(DriveConstants.LEFT_MOTOR_PORT_1),
                    new PWMSparkMax(DriveConstants.LEFT_MOTOR_PORT_1));
    
    private final MotorController rightMotor =
            new MotorControllerGroup(
                    new PWMSparkMax(DriveConstants.RIGHT_MOTOR_PORT_2),
                    new PWMSparkMax(DriveConstants.LEFT_MOTOR_PORT_2));
    
    private final DifferentialDrive drive = new DifferentialDrive(leftMotor, rightMotor);
    
    private final Encoder leftEncoder =
            new Encoder(
                    DriveConstants.LEFT_ENCODER_PORTS[0],
                    DriveConstants.LEFT_ENCODER_PORTS[1],
                    DriveConstants.LEFT_ENCODER_REVERSED);
    private final Encoder rightEncoder =
            new Encoder(
                    DriveConstants.RIGHT_ENCODER_PORTS[0],
                    DriveConstants.RIGHT_ENCODER_PORTS[1],
                    DriveConstants.RIGHT_ENCODER_REVERSED);
    private final AnalogInput rangefinder = new AnalogInput(DriveConstants.RANGE_FINDER_PORT);
    private final AnalogGyro gyro = new AnalogGyro(DriveConstants.ANALOG_GYRO_PORT);
    
    
    /** Create a new drivetrain subsystem. */
    public Drivetrain()
    {
        super();
        
        // We need to invert one side of the drivetrain so that positive voltages
        // result in both sides moving forward. Depending on how your robot's
        // gearbox is constructed, you might have to invert the left side instead.
        rightMotor.setInverted(true);
        
        // Encoders may measure differently in the real world and in
        // simulation. In this example the robot moves 0.042 barleycorns
        // per tick in the real world, but the simulated encoders
        // simulate 360 tick encoders. This if statement allows for the
        // real robot to handle this difference in devices.
        if (Robot.isReal())
        {
            leftEncoder.setDistancePerPulse(DriveConstants.ENCODER_DISTANCE_PER_PULSE);
            rightEncoder.setDistancePerPulse(DriveConstants.ENCODER_DISTANCE_PER_PULSE);
        }
        else
        {
            // Circumference = diameter in feet * pi. 360 tick simulated encoders.
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
    
    
    /** The log method puts interesting information to the SmartDashboard. */
    public void log()
    {
        SmartDashboard.putNumber("Left Distance", leftEncoder.getDistance());
        SmartDashboard.putNumber("Right Distance", rightEncoder.getDistance());
        SmartDashboard.putNumber("Left Speed", leftEncoder.getRate());
        SmartDashboard.putNumber("Right Speed", rightEncoder.getRate());
        SmartDashboard.putNumber("Gyro", gyro.getAngle());
    }
    
    
    /**
     * Tank style driving for the Drivetrain.
     *
     * @param left  Speed in range [-1,1]
     * @param right Speed in range [-1,1]
     */
    public void drive(double left, double right)
    {
        drive.tankDrive(left, right);
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
    
    
    /** Reset the robots sensors to the zero states. */
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
    
    
    /** Call log method every loop. */
    @Override
    public void periodic()
    {
        log();
    }
}
