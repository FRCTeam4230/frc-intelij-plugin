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
import edu.wpi.first.math.controller.RamseteController;
import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.math.trajectory.TrajectoryGenerator;
import edu.wpi.first.math.trajectory.constraint.DifferentialDriveVoltageConstraint;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.XboxController.Button;
import ${data.basePackage}.subsystems.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RamseteCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;

import java.util.List;



/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the Robot periodic
 * methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer
{
    // The robot's subsystems
    private final DriveSubsystem robotDrive = new DriveSubsystem();
    
    // The driver's controller
    XboxController driverController =
            new XboxController(Constants.OIConstants.DRIVER_CONTROLLER_PORT);
    
    
    /** The container for the robot. Contains subsystems, OI devices, and commands. */
    public RobotContainer()
    {
        // Configure the button bindings
        configureButtonBindings();
        
        // Configure default commands
        // Set the default drive command to split-stick arcade drive
        robotDrive.setDefaultCommand(
                // A split-stick arcade command, with forward/backward controlled by the left
                // hand, and turning controlled by the right.
                // If you are using the keyboard as a joystick, it is recommended that you go
                // to the following link to read about editing the keyboard settings.
                // https://docs.wpilib.org/en/stable/docs/software/wpilib-tools/robot-simulation/simulation-gui.html#using-the-keyboard-as-a-joystick
                new RunCommand(
                        () ->
                                robotDrive.arcadeDrive(
                                        -driverController.getLeftY(), -driverController.getRightX()),
                        robotDrive));
    }
    
    
    /**
     * Use this method to define your button->command mappings. Buttons can be created by
     * instantiating a {@link edu.wpi.first.wpilibj.GenericHID} or one of its subclasses ({@link
     * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then calling passing it to a
     * {@link JoystickButton}.
     */
    private void configureButtonBindings()
    {
        // Drive at half speed when the right bumper is held
        new JoystickButton(driverController, Button.kRightBumper.value)
                .onTrue(new InstantCommand(() -> robotDrive.setMaxOutput(0.5)))
                .onFalse(new InstantCommand(() -> robotDrive.setMaxOutput(1)));
    }
    
    
    public DriveSubsystem getRobotDrive()
    {
        return robotDrive;
    }
    
    
    /** Zeros the outputs of all subsystems. */
    public void zeroAllOutputs()
    {
        robotDrive.tankDriveVolts(0, 0);
    }
    
    
    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */
    public Command getAutonomousCommand()
    {
        // Create a voltage constraint to ensure we don't accelerate too fast
        var autoVoltageConstraint =
                new DifferentialDriveVoltageConstraint(
                        new SimpleMotorFeedforward(
                                Constants.DriveConstants.ksVolts,
                                Constants.DriveConstants.kvVoltSecondsPerMeter,
                                Constants.DriveConstants.kaVoltSecondsSquaredPerMeter),
                        Constants.DriveConstants.DRIVE_KINEMATICS,
                        7);
        
        // Create config for trajectory
        TrajectoryConfig config =
                new TrajectoryConfig(
                        Constants.AutoConstants.MAX_SPEED_METERS_PER_SECOND,
                        Constants.AutoConstants.MAX_ACCELERATION_METERS_PER_SECOND_SQUARED)
                        // Add kinematics to ensure max speed is actually obeyed
                        .setKinematics(Constants.DriveConstants.DRIVE_KINEMATICS)
                        // Apply the voltage constraint
                        .addConstraint(autoVoltageConstraint);
        
        // An example trajectory to follow.  All units in meters.
        Trajectory exampleTrajectory =
                TrajectoryGenerator.generateTrajectory(
                        // Start at (1, 2) facing the +X direction
                        new Pose2d(1, 2, new Rotation2d(0)),
                        // Pass through these two interior waypoints, making an 's' curve path
                        List.of(new Translation2d(2, 3), new Translation2d(3, 1)),
                        // End 3 meters straight ahead of where we started, facing forward
                        new Pose2d(4, 2, new Rotation2d(0)),
                        // Pass config
                        config);
        
        RamseteCommand ramseteCommand =
                new RamseteCommand(
                        exampleTrajectory,
                        robotDrive::getPose,
                        new RamseteController(
                                Constants.AutoConstants.RAMSETE_B, Constants.AutoConstants.RAMSETE_ZETA),
                        new SimpleMotorFeedforward(
                                Constants.DriveConstants.ksVolts,
                                Constants.DriveConstants.kvVoltSecondsPerMeter,
                                Constants.DriveConstants.kaVoltSecondsSquaredPerMeter),
                        Constants.DriveConstants.DRIVE_KINEMATICS,
                        robotDrive::getWheelSpeeds,
                        new PIDController(Constants.DriveConstants.P_DRIVE_VEL, 0, 0),
                        new PIDController(Constants.DriveConstants.P_DRIVE_VEL, 0, 0),
                        // RamseteCommand passes volts to the callback
                        robotDrive::tankDriveVolts,
                        robotDrive);
        
        // Reset odometry to starting pose of trajectory.
        robotDrive.resetOdometry(exampleTrajectory.getInitialPose());
        
        // Run path following command, then stop at the end.
        return ramseteCommand.andThen(() -> robotDrive.tankDriveVolts(0, 0));
    }
}
