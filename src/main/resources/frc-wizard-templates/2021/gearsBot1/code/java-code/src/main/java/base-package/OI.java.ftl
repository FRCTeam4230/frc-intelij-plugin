<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement-->
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

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import ${data.basePackage}.commands.Autonomous;
import ${data.basePackage}.commands.CloseClaw;
import ${data.basePackage}.commands.OpenClaw;
import ${data.basePackage}.commands.Pickup;
import ${data.basePackage}.commands.Place;
import ${data.basePackage}.commands.PrepareToPickup;
import ${data.basePackage}.commands.SetElevatorSetpoint;
import ${data.basePackage}.commands.SetWristSetpoint;

/**
 * This class is the glue that binds the controls on the physical Operator
 * Interface (OI) to the commands and command groups that allow control of 
 * the robot.
 */
public class OI
{
    private final Joystick joystick = new Joystick(0);

    /**
     * Construct the OI and all of the buttons on it.
     */
    public OI()
    {
        // Put Some buttons on the SmartDashboard
        SmartDashboard.putData("Elevator Bottom", new SetElevatorSetpoint(0));
        SmartDashboard.putData("Elevator Platform", new SetElevatorSetpoint(0.2));
        SmartDashboard.putData("Elevator Top", new SetElevatorSetpoint(0.3));

        SmartDashboard.putData("Wrist Horizontal", new SetWristSetpoint(0));
        SmartDashboard.putData("Raise Wrist", new SetWristSetpoint(-45));

        SmartDashboard.putData("Open Claw", new OpenClaw());
        SmartDashboard.putData("Close Claw", new CloseClaw());

        SmartDashboard.putData("Deliver Soda", new Autonomous());

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
        dpadUp.whenPressed(new SetElevatorSetpoint(0.2));
        dpadDown.whenPressed(new SetElevatorSetpoint(-0.2));
        dpadRight.whenPressed(new CloseClaw());
        dpadLeft.whenPressed(new OpenClaw());

        r1.whenPressed(new PrepareToPickup());
        r2.whenPressed(new Pickup());
        l1.whenPressed(new Place());
        l2.whenPressed(new Autonomous());
    }

    public Joystick getJoystick()
    {
        return joystick;
    }
}
