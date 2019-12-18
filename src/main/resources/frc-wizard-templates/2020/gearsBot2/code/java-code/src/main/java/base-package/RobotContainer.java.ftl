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
/* Copyright (c) 2017-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage};

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandBase;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
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
import ${data.basePackage}.subsystems.DriveTrain;
import ${data.basePackage}.subsystems.Elevator;
import ${data.basePackage}.subsystems.Wrist;

/**
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer
{
    // The robot's subsystems and commands are defined here...
    private final DriveTrain drivetrain = new DriveTrain();
    private final Elevator elevator = new Elevator();
    private final Wrist wrist = new Wrist();
    private final Claw claw = new Claw();

    private final Joystick joystick = new Joystick(0);

    private final CommandBase autonomousCommand = new Autonomous(drivetrain, claw, wrist, elevator);

    /**
     * The container for the robot.  Contains subsystems, OI devices, and commands.
     */
    public RobotContainer()
    {
        // Put Some buttons on the SmartDashboard
        SmartDashboard.putData("Elevator Bottom", new SetElevatorSetpoint(0, elevator));
        SmartDashboard.putData("Elevator Platform", new SetElevatorSetpoint(0.2, elevator));
        SmartDashboard.putData("Elevator Top", new SetElevatorSetpoint(0.3, elevator));

        SmartDashboard.putData("Wrist Horizontal", new SetWristSetpoint(0, wrist));
        SmartDashboard.putData("Raise Wrist", new SetWristSetpoint(-45, wrist));

        SmartDashboard.putData("Open Claw", new OpenClaw(claw));
        SmartDashboard.putData("Close Claw", new CloseClaw(claw));

        SmartDashboard.putData("Deliver Soda", new Autonomous(drivetrain, claw, wrist, elevator));

        // Assign default commands
        drivetrain.setDefaultCommand(new TankDrive(() -> joystick.getY(Hand.kLeft),
                () -> joystick.getY(Hand.kRight), drivetrain));

        // Show what command your subsystem is running on the SmartDashboard
        SmartDashboard.putData(drivetrain);
        SmartDashboard.putData(elevator);
        SmartDashboard.putData(wrist);
        SmartDashboard.putData(claw);

        // Call log method on all subsystems
        wrist.log();
        elevator.log();
        drivetrain.log();
        claw.log();

        // Configure the button bindings
        configureButtonBindings();
    }

    /**
     * Use this method to define your button->command mappings.  Buttons can be created by
     * instantiating a {@link GenericHID} or one of its subclasses ({@link
     * edu.wpi.first.wpilibj.Joystick Joystick} or {@link XboxController}), and then passing
     * it to a {@link edu.wpi.first.wpilibj2.command.button.JoystickButton JoystickButton}.
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
        dpadUp.whenPressed(new SetElevatorSetpoint(0.2, elevator));
        dpadDown.whenPressed(new SetElevatorSetpoint(-0.2, elevator));
        dpadRight.whenPressed(new CloseClaw(claw));
        dpadLeft.whenPressed(new OpenClaw(claw));

        r1.whenPressed(new PrepareToPickup(claw, wrist, elevator));
        r2.whenPressed(new Pickup(claw, wrist, elevator));
        l1.whenPressed(new Place(claw, wrist, elevator));
        l2.whenPressed(new Autonomous(drivetrain, claw, wrist, elevator));
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
