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
import ${data.basePackage}.subsystems.DriveSubsystem;
import ${data.basePackage}.subsystems.HatchSubsystem;
import edu.wpi.first.wpilibj2.command.FunctionalCommand;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;



/** A complex auto command that drives forward, releases a hatch, and then drives backward. */
public class ComplexAutoCommand extends SequentialCommandGroup
{
    /**
     * Creates a new ComplexAutoCommand.
     *
     * @param driveSubsystem The drive subsystem this command will run on
     * @param hatchSubsystem The hatch subsystem this command will run on
     */
    public ComplexAutoCommand(DriveSubsystem driveSubsystem, HatchSubsystem hatchSubsystem)
    {
        addCommands(
                // Drive forward up to the front of the cargo ship
                new FunctionalCommand(
                        // Reset encoders on command start
                        driveSubsystem::resetEncoders,
                        // Drive forward while the command is executing
                        () -> driveSubsystem.arcadeDrive(AutoConstants.AUTO_DRIVE_SPEED, 0),
                        // Stop driving at the end of the command
                        interrupt -> driveSubsystem.arcadeDrive(0, 0),
                        // End the command when the robot's driven distance exceeds the desired value
                        () ->
                                driveSubsystem.getAverageEncoderDistance()
                                >= AutoConstants.AUTO_DRIVE_DISTANCE_INCHES,
                        // Require the drive subsystem
                        driveSubsystem),
                
                // Release the hatch
                new InstantCommand(hatchSubsystem::releaseHatch, hatchSubsystem),
                
                // Drive backward the specified distance
                new FunctionalCommand(
                        // Reset encoders on command start
                        driveSubsystem::resetEncoders,
                        // Drive backward while the command is executing
                        () -> driveSubsystem.arcadeDrive(-AutoConstants.AUTO_DRIVE_SPEED, 0),
                        // Stop driving at the end of the command
                        interrupt -> driveSubsystem.arcadeDrive(0, 0),
                        // End the command when the robot's driven distance exceeds the desired value
                        () ->
                                driveSubsystem.getAverageEncoderDistance()
                                <= AutoConstants.AUTO_BACKUP_DISTANCE_INCHES,
                        // Require the drive subsystem
                        driveSubsystem));
    }
}
