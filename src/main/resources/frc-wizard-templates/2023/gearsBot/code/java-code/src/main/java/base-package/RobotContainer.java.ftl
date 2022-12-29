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
import ${data.basePackage}.commands.Autonomous;
import ${data.basePackage}.commands.CloseClaw;
import ${data.basePackage}.commands.OpenClaw;
import ${data.basePackage}.commands.Pickup;
import ${data.basePackage}.commands.Place;
import ${data.basePackage}.commands.PrepareToPickup;
import ${data.basePackage}.commands.SetElevatorSetpoint;
import ${data.basePackage}.commands.SetWristSetpoint;
import ${data.basePackage}.commands.TankDrive;
import ${data.basePackage}.subsystems.Claw;
import ${data.basePackage}.subsystems.Drivetrain;
import ${data.basePackage}.subsystems.Elevator;
import ${data.basePackage}.subsystems.Wrist;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;



/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer
{
    // The robot's subsystems and commands are defined here...
    private final Drivetrain drivetrain = new Drivetrain();
    private final Elevator elevator = new Elevator();
    private final Wrist wrist = new Wrist();
    private final Claw claw = new Claw();
    
    private final XboxController joystick = new XboxController(0);
    
    private final CommandBase autonomousCommand =
            new Autonomous(drivetrain, claw, wrist, elevator);
    
    
    /** The container for the robot. Contains subsystems, OI devices, and commands. */
    public RobotContainer()
    {
        // Put Some buttons on the SmartDashboard
        SmartDashboard.putData("Elevator Bottom", new SetElevatorSetpoint(0, elevator));
        SmartDashboard.putData("Elevator Top", new SetElevatorSetpoint(0.25, elevator));
        
        SmartDashboard.putData("Wrist Horizontal", new SetWristSetpoint(0, wrist));
        SmartDashboard.putData("Raise Wrist", new SetWristSetpoint(-45, wrist));
        
        SmartDashboard.putData("Open Claw", new OpenClaw(claw));
        SmartDashboard.putData("Close Claw", new CloseClaw(claw));
        
        SmartDashboard.putData(
                "Deliver Soda", new Autonomous(drivetrain, claw, wrist, elevator));
        
        // Assign default commands
        drivetrain.setDefaultCommand(
                new TankDrive(() -> -joystick.getLeftY(), () -> -joystick.getRightY(), drivetrain));
        
        // Show what command your subsystem is running on the SmartDashboard
        SmartDashboard.putData(drivetrain);
        SmartDashboard.putData(elevator);
        SmartDashboard.putData(wrist);
        SmartDashboard.putData(claw);
        
        // Configure the button bindings
        configureButtonBindings();
    }
    
    
    /**
     * Use this method to define your button->command mappings. Buttons can be created by
     * instantiating a {@link edu.wpi.first.wpilibj.GenericHID} or one of its subclasses ({@link
     * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then passing it to a {@link
     * edu.wpi.first.wpilibj2.command.button.JoystickButton}.
     */
    private void configureButtonBindings()
    {
        // Create some buttons
        final JoystickButton dpadUp = new JoystickButton(joystick, 5);
        final JoystickButton dpadRight = new JoystickButton(joystick, 6);
        final JoystickButton dpadDown = new JoystickButton(joystick, 7);
        final JoystickButton dpadLeft = new JoystickButton(joystick, 8);
        final JoystickButton l2 = new JoystickButton(joystick, 9);
        final JoystickButton r2 = new JoystickButton(joystick, 10);
        final JoystickButton l1 = new JoystickButton(joystick, 11);
        final JoystickButton r1 = new JoystickButton(joystick, 12);
        
        // Connect the buttons to commands
        dpadUp.onTrue(new SetElevatorSetpoint(0.25, elevator));
        dpadDown.onTrue(new SetElevatorSetpoint(0.0, elevator));
        dpadRight.onTrue(new CloseClaw(claw));
        dpadLeft.onTrue(new OpenClaw(claw));
        
        r1.onTrue(new PrepareToPickup(claw, wrist, elevator));
        r2.onTrue(new Pickup(claw, wrist, elevator));
        l1.onTrue(new Place(claw, wrist, elevator));
        l2.onTrue(new Autonomous(drivetrain, claw, wrist, elevator));
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
