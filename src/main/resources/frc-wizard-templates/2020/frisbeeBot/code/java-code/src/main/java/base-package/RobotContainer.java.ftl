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
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage};

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.ConditionalCommand;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.WaitUntilCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import ${data.basePackage}.subsystems.DriveSubsystem;
import ${data.basePackage}.subsystems.ShooterSubsystem;

import static edu.wpi.first.wpilibj.XboxController.Button;
import static ${data.basePackage}.Constants.AutoConstants.AUTO_SHOOT_TIME_SECONDS;
import static ${data.basePackage}.Constants.AutoConstants.AUTO_TIMEOUT_SECONDS;
import static ${data.basePackage}.Constants.OIConstants.DRIVER_CONTROLLER_PORT;

/**
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer
{
    // The robot's subsystems
    private final DriveSubsystem robotDrive = new DriveSubsystem();
    private final ShooterSubsystem shooter = new ShooterSubsystem();

    // A simple autonomous routine that shoots the loaded frisbees
    private final Command autoCommand =
            // Start the command by spinning up the shooter...
            new InstantCommand(shooter::enable, shooter).andThen(
                    // Wait until the shooter is at speed before feeding the frisbees
                    new WaitUntilCommand(shooter::atSetpoint),
                    // Start running the feeder
                    new InstantCommand(shooter::runFeeder, shooter),
                    // Shoot for the specified time
                    new WaitCommand(AUTO_SHOOT_TIME_SECONDS))
                    // Add a timeout (will end the command if, for instance, the shooter never gets up to
                    // speed)
                    .withTimeout(AUTO_TIMEOUT_SECONDS)
                    // When the command ends, turn off the shooter and the feeder
                    .andThen(() ->
                    {
                        shooter.disable();
                        shooter.stopFeeder();
                    });

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
                new RunCommand(() -> robotDrive
                        .arcadeDrive(driverController.getY(GenericHID.Hand.kLeft),
                                driverController.getX(GenericHID.Hand.kRight)), robotDrive));

    }

    /**
     * Use this method to define your button->command mappings.  Buttons can be created by
     * instantiating a {@link GenericHID} or one of its subclasses ({@link
     * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a
     * {@link edu.wpi.first.wpilibj2.command.button.JoystickButton}.
     */
    private void configureButtonBindings()
    {
        // Spin up the shooter when the 'A' button is pressed
        new JoystickButton(driverController, Button.kA.value)
                .whenPressed(new InstantCommand(shooter::enable, shooter));

        // Turn off the shooter when the 'B' button is pressed
        new JoystickButton(driverController, Button.kB.value)
                .whenPressed(new InstantCommand(shooter::disable, shooter));

        // Run the feeder when the 'X' button is held, but only if the shooter is at speed
        new JoystickButton(driverController, Button.kX.value).whenPressed(new ConditionalCommand(
                // Run the feeder
                new InstantCommand(shooter::runFeeder, shooter),
                // Do nothing
                new InstantCommand(),
                // Determine which of the above to do based on whether the shooter has reached the
                // desired speed
                shooter::atSetpoint)).whenReleased(new InstantCommand(shooter::stopFeeder, shooter));

        // Drive at half speed when the bumper is held
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
        return autoCommand;
    }
}
