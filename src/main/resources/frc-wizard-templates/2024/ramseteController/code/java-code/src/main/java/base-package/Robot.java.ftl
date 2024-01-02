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

import edu.wpi.first.math.controller.RamseteController;
import edu.wpi.first.math.filter.SlewRateLimiter;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import java.util.List;



public class ${data.robotClassSimpleName} extends TimedRobot
{
    private final XboxController controller = new XboxController(0);
    private final Drivetrain drive = new Drivetrain();
    
    // Slew rate limiters to make joystick inputs more gentle; 1/3 sec from 0 to 1.
    private final SlewRateLimiter speedLimiter = new SlewRateLimiter(3);
    private final SlewRateLimiter rotateLimiter = new SlewRateLimiter(3);
    
    // An example trajectory to follow during the autonomous period.
    private Trajectory trajectory;
    
    // The Ramsete Controller to follow the trajectory.
    private final RamseteController ramseteController = new RamseteController();
    
    // The timer to use during the autonomous period.
    private Timer timer;
    
    // Create Field2d for robot and trajectory visualizations.
    private Field2d field;
    
    
    @Override
    public void robotInit()
    {
        // Create the trajectory to follow in autonomous. It is best to initialize
        // trajectories here to avoid wasting time in autonomous.
        trajectory =
                TrajectoryGenerator.generateTrajectory(
                        new Pose2d(0, 0, Rotation2d.fromDegrees(0)),
                        List.of(new Translation2d(1, 1), new Translation2d(2, -1)),
                        new Pose2d(3, 0, Rotation2d.fromDegrees(0)),
                        new TrajectoryConfig(Units.feetToMeters(3.0), Units.feetToMeters(3.0)));
        
        // Create and push Field2d to SmartDashboard.
        field = new Field2d();
        SmartDashboard.putData(field);
        
        // Push the trajectory to Field2d.
        field.getObject("traj").setTrajectory(trajectory);
    }
    
    
    @Override
    public void autonomousInit()
    {
        // Initialize the timer.
        timer = new Timer();
        timer.start();
        
        // Reset the drivetrain's odometry to the starting pose of the trajectory.
        drive.resetOdometry(trajectory.getInitialPose());
    }
    
    
    @Override
    public void autonomousPeriodic()
    {
        // Update odometry.
        drive.updateOdometry();
        
        // Update robot position on Field2d.
        field.setRobotPose(drive.getPose());
        
        if (timer.get() < trajectory.getTotalTimeSeconds())
        {
            // Get the desired pose from the trajectory.
            var desiredPose = trajectory.sample(timer.get());
            
            // Get the reference chassis speeds from the Ramsete controller.
            var refChassisSpeeds = ramseteController.calculate(drive.getPose(), desiredPose);
            
            // Set the linear and angular speeds.
            drive.drive(refChassisSpeeds.vxMetersPerSecond, refChassisSpeeds.omegaRadiansPerSecond);
        }
        else
        {
            drive.drive(0, 0);
        }
    }
    
    
    @Override
    public void teleopPeriodic()
    {
        // Get the x speed. We are inverting this because Xbox controllers return
        // negative values when we push forward.
        final var xSpeed = -speedLimiter.calculate(controller.getLeftY()) * Drivetrain.MAX_SPEED;
        
        // Get the rate of angular rotation. We are inverting this because we want a
        // positive value when we pull to the left (remember, CCW is positive in
        // mathematics). Xbox controllers return positive values when you pull to
        // the right by default.
        final var rot = -rotateLimiter.calculate(controller.getRightX()) * Drivetrain.MAX_ANGULAR_SPEED;
        
        drive.drive(xSpeed, rot);
    }
}
