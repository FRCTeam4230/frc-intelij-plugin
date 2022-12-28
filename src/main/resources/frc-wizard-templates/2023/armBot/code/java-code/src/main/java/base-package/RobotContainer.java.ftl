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

import edu.wpi.first.wpilibj.XboxController;
import ${data.basePackage}.Constants.OIConstants;
import ${data.basePackage}.subsystems.ArmSubsystem;
import ${data.basePackage}.subsystems.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;



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
    private final ArmSubsystem robotArm = new ArmSubsystem();
    
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
     * JoystickButton}.
     */
    private void configureButtonBindings()
    {
        // Move the arm to 2 radians above horizontal when the 'A' button is pressed.
        driverController
                .a()
                .onTrue(
                        Commands.runOnce(
                                () -> {
                                    robotArm.setGoal(2);
                                    robotArm.enable();
                                },
                                robotArm));
        
        // Move the arm to neutral position when the 'B' button is pressed.
        driverController
                .b()
                .onTrue(
                        Commands.runOnce(
                                () -> {
                                    robotArm.setGoal(Constants.ArmConstants.ARM_OFFSET_RADS);
                                    robotArm.enable();
                                },
                                robotArm));
        
        // Disable the arm controller when Y is pressed.
        driverController.y().onTrue(Commands.runOnce(robotArm::disable));
        
        // Drive at half speed when the bumper is held
        driverController
                .rightBumper()
                .onTrue(Commands.runOnce(() -> robotDrive.setMaxOutput(0.5)))
                .onFalse(Commands.runOnce(() -> robotDrive.setMaxOutput(1.0)));
    }
    
    
    /**
     * Disables all ProfiledPIDSubsystem and PIDSubsystem instances. This should be called on robot
     * disable to prevent integral windup.
     */
    public void disablePIDSubsystems()
    {
        robotArm.disable();
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
