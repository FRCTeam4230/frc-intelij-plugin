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

package ${data.basePackage}.commands;

import ${data.basePackage}.Constants.AutoConstants;
import ${data.basePackage}.subsystems.Claw;
import ${data.basePackage}.subsystems.Drivetrain;
import ${data.basePackage}.subsystems.Elevator;
import ${data.basePackage}.subsystems.Wrist;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;



/** The main autonomous command to pickup and deliver the soda to the box. */
public class Autonomous extends SequentialCommandGroup
{
    /** Create a new autonomous command. */
    public Autonomous(Drivetrain drive, Claw claw, Wrist wrist, Elevator elevator)
    {
        addCommands(
                new PrepareToPickup(claw, wrist, elevator),
                new Pickup(claw, wrist, elevator),
                new SetDistanceToBox(AutoConstants.DIST_TO_BOX_1, drive),
                // new DriveStraight(4), // Use encoders if ultrasonic is broken
                new Place(claw, wrist, elevator),
                new SetDistanceToBox(AutoConstants.DIST_TO_BOX_2, drive),
                // new DriveStraight(-2), // Use Encoders if ultrasonic is broken
                Commands.parallel(
                        new SetWristSetpoint(AutoConstants.WRIST_SETPOINT, wrist), new CloseClaw(claw)));
    }
}
