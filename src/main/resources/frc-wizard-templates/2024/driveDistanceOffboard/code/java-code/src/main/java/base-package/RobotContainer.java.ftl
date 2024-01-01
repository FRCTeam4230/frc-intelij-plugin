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

import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj.XboxController;
import ${data.basePackage}.Constants.DriveConstants;
import ${data.basePackage}.Constants.OIConstants;
import ${data.basePackage}.commands.DriveDistanceProfiled;
import ${data.basePackage}.subsystems.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.TrapezoidProfileCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;



/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer
{
    // The robot's subsystems
    private final DriveSubsystem robotDrive = new DriveSubsystem();
    
    // Retained command references
    private final Command driveFullSpeed = Commands.runOnce(() -> robotDrive.setMaxOutput(1));
    private final Command driveHalfSpeed = Commands.runOnce(() -> robotDrive.setMaxOutput(0.5));
    
    // The driver's controller
    CommandXboxController driverController =
            new CommandXboxController(OIConstants.DRIVER_CONTROLLER_PORT);
    
    
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
                Commands.run(
                        () ->
                                robotDrive.arcadeDrive(
                                        -driverController.getLeftY(), -driverController.getRightX()),
                        robotDrive));
    }
    
    
    /**
     * Use this method to define your button->command mappings. Buttons can be created by
     * instantiating a {@link edu.wpi.first.wpilibj.GenericHID} or one of its subclasses ({@link
     * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
     * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
     */
    private void configureButtonBindings()
    {
        // Drive at half speed when the bumper is held
        driverController.rightBumper().onTrue(driveHalfSpeed).onFalse(driveFullSpeed);
        
        // Drive forward by 3 meters when the 'A' button is pressed, with a timeout of 10 seconds
        driverController.a().onTrue(new DriveDistanceProfiled(3, robotDrive).withTimeout(10));
        
        // Do the same thing as above when the 'B' button is pressed, but defined inline
        driverController
                .b()
                .onTrue(
                        new TrapezoidProfileCommand(
                                new TrapezoidProfile(
                                        // Limit the max acceleration and velocity
                                        new TrapezoidProfile.Constraints(
                                                DriveConstants.MAX_SPEED_METERS_PER_SECOND,
                                                DriveConstants.MAX_ACCELERATION_METERS_PER_SECOND_SQUARED)),
                                // Pipe the profile state to the drive
                                setpointState -> robotDrive.setDriveStates(setpointState, setpointState),
                                // End at desired position in meters; implicitly starts at 0
                                () -> new TrapezoidProfile.State(3, 0),
                                // Current position
                                TrapezoidProfile.State::new,
                                // Require the drive
                                robotDrive)
                                .beforeStarting(robotDrive::resetEncoders)
                                .withTimeout(10));
    }
    
    
    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */
    public Command getAutonomousCommand()
    {
        return Commands.none();
    }
}
