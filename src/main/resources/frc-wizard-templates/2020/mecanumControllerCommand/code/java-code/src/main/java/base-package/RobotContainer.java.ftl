<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Refernce: https://freemarker.apache.org/docs/ref.html
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

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.XboxController.Button;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj.controller.ProfiledPIDController;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.MecanumControllerCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import ${data.basePackage}.subsystems.DriveSubsystem;

import java.util.List;

import static ${data.basePackage}.Constants.AutoConstants.*;
import static ${data.basePackage}.Constants.DriveConstants.*;
import static ${data.basePackage}.Constants.OIConstants.DRIVER_CONTROLLER_PORT;

/*
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
@SuppressWarnings("PMD.ExcessiveImports")
public class RobotContainer
{
    // The robot's subsystems
    private final DriveSubsystem robotDrive = new DriveSubsystem();

    // The driver's controller
    XboxController driverController = new XboxController(DRIVER_CONTROLLER_PORT);

    /**
     * The container for the robot.  Contains subsystems, OI devices, and commands.
     */
    public RobotContainer()
    {
        // Configure the button bindings
        configureButtonBindings();

        // Configure default commands
        // Set the default drive command to split-stick arcade drive
        robotDrive.setDefaultCommand(
                // A split-stick arcade command, with forward/backward controlled by the left
                // hand, and turning controlled by the right.
                new RunCommand(() -> robotDrive.drive(
                        driverController.getY(GenericHID.Hand.kLeft),
                        driverController.getX(GenericHID.Hand.kRight),
                        driverController.getX(GenericHID.Hand.kLeft), false)));

    }

    /**
     * Use this method to define your button->command mappings.  Buttons can be created by
     * instantiating a {@link GenericHID} or one of its subclasses ({@link
     * edu.wpi.first.wpilibj.Joystick Joystick} or {@link XboxController}), and then calling 
     * passing it to a {@link JoystickButton}.
     */
    private void configureButtonBindings()
    {
        // Drive at half speed when the right bumper is held
        new JoystickButton(driverController, Button.kBumperRight.value)
                .whenPressed(() -> robotDrive.setMaxOutput(0.5))
                .whenReleased(() -> robotDrive.setMaxOutput(1));

    }

    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */
    public Command getAutonomousCommand()
    {
        // Create config for trajectory
        TrajectoryConfig config =
                new TrajectoryConfig(MAX_SPEED_METERS_PER_SECOND, MAX_ACCELERATION_METERS_PER_SECOND_SQUARED)
                        // Add kinematics to ensure max speed is actually obeyed
                        .setKinematics(DRIVE_KINEMATICS);

        // An example trajectory to follow.  All units in meters.
        Trajectory exampleTrajectory = TrajectoryGenerator.generateTrajectory(
                // Start at the origin facing the +X direction
                new Pose2d(0, 0, new Rotation2d(0)),
                // Pass through these two interior waypoints, making an 's' curve path
                List.of(
                        new Translation2d(1, 1),
                        new Translation2d(2, -1)
                ),
                // End 3 meters straight ahead of where we started, facing forward
                new Pose2d(3, 0, new Rotation2d(0)),
                config
        );

        MecanumControllerCommand mecanumControllerCommand = new MecanumControllerCommand(
                exampleTrajectory,
                robotDrive::getPose,

                FEEDFORWARD,
                DRIVE_KINEMATICS,

                //Position controllers
                new PIDController(P_X_CONTROLLER, 0, 0),
                new PIDController(P_Y_CONTROLLER, 0, 0),
                new ProfiledPIDController(P_THETA_CONTROLLER, 0, 0, THETA_CONTROLLER_CONSTRAINTS),

                //Needed for normalizing wheel speeds
                MAX_SPEED_METERS_PER_SECOND,

                //Velocity PID's
                new PIDController(P_FRONT_LEFT_VEL, 0, 0),
                new PIDController(P_REAR_LEFT_VEL, 0, 0),
                new PIDController(P_FRONT_RIGHT_VEL, 0, 0),
                new PIDController(P_REAR_RIGHT_VEL, 0, 0),

                robotDrive::getCurrentWheelSpeeds,

                robotDrive::setDriveSpeedControllersVolts, //Consumer for the output motor voltages

                robotDrive
        );

        // Run path following command, then stop at the end.
        return mecanumControllerCommand.andThen(() -> robotDrive.drive(0, 0, 0, false));
    }
}

