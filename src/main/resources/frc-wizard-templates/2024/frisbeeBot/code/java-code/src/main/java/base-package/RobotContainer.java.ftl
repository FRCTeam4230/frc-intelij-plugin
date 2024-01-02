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

import ${data.basePackage}.Constants.AutoConstants;
import ${data.basePackage}.Constants.OIConstants;
import ${data.basePackage}.subsystems.DriveSubsystem;
import ${data.basePackage}.subsystems.ShooterSubsystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
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
    private final ShooterSubsystem shooter = new ShooterSubsystem();
    
    private final Command spinUpShooter = Commands.runOnce(shooter::enable, shooter);
    private final Command stopShooter = Commands.runOnce(shooter::disable, shooter);
    
    // An autonomous routine that shoots the loaded frisbees
    Command autonomousCommand =
            Commands.sequence(
                            // Start the command by spinning up the shooter...
                            Commands.runOnce(shooter::enable, shooter),
                            // Wait until the shooter is at speed before feeding the frisbees
                            Commands.waitUntil(shooter::atSetpoint),
                            // Start running the feeder
                            Commands.runOnce(shooter::runFeeder, shooter),
                            // Shoot for the specified time
                            Commands.waitSeconds(AutoConstants.AUTO_SHOOT_TIME_SECONDS))
                    // Add a timeout (will end the command if, for instance, the shooter
                    // never gets up to speed)
                    .withTimeout(AutoConstants.AUTO_TIMEOUT_SECONDS)
                    // When the command ends, turn off the shooter and the feeder
                    .andThen(
                            Commands.runOnce(
                                    () -> {
                                        shooter.disable();
                                        shooter.stopFeeder();
                                    }));
    
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
     * Use this method to define your button->command mappings. Buttons can be created via the button
     * factories on {@link edu.wpi.first.wpilibj2.command.button.CommandGenericHID} or one of its
     * subclasses ({@link edu.wpi.first.wpilibj2.command.button.CommandJoystick} or {@link
     * CommandXboxController}).
     */
    private void configureButtonBindings()
    {
        // Configure your button bindings here
        
        // We can bind commands while retaining references to them in RobotContainer
        
        // Spin up the shooter when the 'A' button is pressed
        driverController.a().onTrue(spinUpShooter);
        
        // Turn off the shooter when the 'B' button is pressed
        driverController.b().onTrue(stopShooter);
        
        // We can also write them as temporary variables outside the bindings
        
        // Shoots if the shooter wheel has reached the target speed
        Command shoot =
                Commands.either(
                        // Run the feeder
                        Commands.runOnce(shooter::runFeeder, shooter),
                        // Do nothing
                        Commands.none(),
                        // Determine which of the above to do based on whether the shooter has reached the
                        // desired speed
                        shooter::atSetpoint);
        
        Command stopFeeder = Commands.runOnce(shooter::stopFeeder, shooter);
        
        // Shoot when the 'X' button is pressed
        driverController.x().onTrue(shoot).onFalse(stopFeeder);
        
        // We can also define commands inline at the binding!
        
        // While holding the shoulder button, drive at half speed
        driverController
                .rightBumper()
                .onTrue(Commands.runOnce(() -> robotDrive.setMaxOutput(0.5)))
                .onFalse(Commands.runOnce(() -> robotDrive.setMaxOutput(1)));
    }
    
    
    /**
     * Use this to pass the autonomous command to the main {@link Robot} class.
     *
     * @return the command to run in autonomous
     */
    public Command getAutonomousCommand()
    {
        return autonomousCommand;
    }
}
