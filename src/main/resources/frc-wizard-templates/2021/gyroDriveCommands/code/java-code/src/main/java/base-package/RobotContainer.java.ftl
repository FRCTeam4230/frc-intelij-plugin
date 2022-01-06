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

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.PIDCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import ${data.basePackage}.commands.TurnToAngle;
import ${data.basePackage}.commands.TurnToAngleProfiled;
import ${data.basePackage}.subsystems.DriveSubsystem;

import static edu.wpi.first.wpilibj.XboxController.Button;
import static ${data.basePackage}.Constants.DriveConstants.*;
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
     * edu.wpi.first.wpilibj.Joystick Joystick} or {@link XboxController}), and then passing
     * it to a {@link edu.wpi.first.wpilibj2.command.button.JoystickButton JoystickButton}.
     */
    private void configureButtonBindings()
    {
        // Drive at half speed when the right bumper is held
        new JoystickButton(driverController, Button.kBumperRight.value)
                .whenPressed(() -> robotDrive.setMaxOutput(0.5))
                .whenReleased(() -> robotDrive.setMaxOutput(1));

        // Stabilize robot to drive straight with gyro when left bumper is held
        new JoystickButton(driverController, Button.kBumperLeft.value).whenHeld(
                new PIDCommand(
                        new PIDController(STABILIZATION_P, STABILIZATION_I, STABILIZATION_D),
                        // Close the loop on the turn rate
                        robotDrive::getTurnRate,
                        // Setpoint is 0
                        0,
                        // Pipe the output to the turning controls
                        output -> robotDrive
                                .arcadeDrive(driverController.getY(GenericHID.Hand.kLeft), output),
                        // Require the robot drive
                        robotDrive));

        // Turn to 90 degrees when the 'X' button is pressed, with a 5 second timeout
        new JoystickButton(driverController, Button.kX.value)
                .whenPressed(new TurnToAngle(90, robotDrive).withTimeout(5));

        // Turn to -90 degrees with a profile when the 'A' button is pressed, with a 5 second timeout
        new JoystickButton(driverController, Button.kA.value)
                .whenPressed(new TurnToAngleProfiled(-90, robotDrive).withTimeout(5));
    }


    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */
    public Command getAutonomousCommand()
    {
        // no auto
        return new InstantCommand();
    }
}
