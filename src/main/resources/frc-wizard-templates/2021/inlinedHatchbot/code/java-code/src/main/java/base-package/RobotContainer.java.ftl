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

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.StartEndCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import ${data.basePackage}.commands.ComplexAutoCommand;
import ${data.basePackage}.subsystems.DriveSubsystem;
import ${data.basePackage}.subsystems.HatchSubsystem;

import static edu.wpi.first.wpilibj.XboxController.Button;
import static ${data.basePackage}.Constants.AutoConstants.AUTO_DRIVE_DISTANCE_INCHES;
import static ${data.basePackage}.Constants.AutoConstants.AUTO_DRIVE_SPEED;
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
    private final HatchSubsystem hatchSubsystem = new HatchSubsystem();
    
    // The autonomous routines
    
    // A simple auto routine that drives forward a specified distance, and then stops.
    private final Command simpleAuto =
            new StartEndCommand(
                    // Start driving forward at the start of the command
                    () -> robotDrive.arcadeDrive(AUTO_DRIVE_SPEED, 0),
                    // Stop driving at the end of the command
                    () -> robotDrive.arcadeDrive(0, 0),
                    // Requires the drive subsystem
                    robotDrive
            )
                    // Reset the encoders before starting
                    .beforeStarting(robotDrive::resetEncoders)
                    // End the command when the robot's driven distance exceeds the desired value
                    .withInterrupt(() -> robotDrive.getAverageEncoderDistance()
                                         >= AUTO_DRIVE_DISTANCE_INCHES);
    
    // A complex auto routine that drives forward, drops a hatch, and then drives backward.
    private final Command complexAuto = new ComplexAutoCommand(robotDrive, hatchSubsystem);
    
    // A chooser for autonomous commands
    SendableChooser<Command> chooser = new SendableChooser<>();
    
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
                new RunCommand(() -> robotDrive.arcadeDrive(
                        driverController.getY(GenericHID.Hand.kLeft),
                        driverController.getX(GenericHID.Hand.kRight)),
                               robotDrive)
        );
        
        // Add commands to the autonomous command chooser
        chooser.addOption("Simple Auto", simpleAuto);
        chooser.addOption("Complex Auto", complexAuto);
        
        // Put the chooser on the dashboard
        Shuffleboard.getTab("Autonomous").add(chooser);
    }
    
    
    /**
     * Use this method to define your button->command mappings.  Buttons can be created by
     * instantiating a {@link GenericHID} or one of its subclasses ({@link
     * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a
     * {@link edu.wpi.first.wpilibj2.command.button.JoystickButton}.
     */
    private void configureButtonBindings()
    {
        // Grab the hatch when the 'A' button is pressed.
        new JoystickButton(driverController, Button.kA.value)
                .whenPressed(new InstantCommand(hatchSubsystem::grabHatch, hatchSubsystem));
        // Release the hatch when the 'B' button is pressed.
        new JoystickButton(driverController, Button.kB.value)
                .whenPressed(new InstantCommand(hatchSubsystem::releaseHatch, hatchSubsystem));
        // While holding the shoulder button, drive at half speed
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
        return chooser.getSelected();
    }
}
