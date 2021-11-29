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
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.commands;

import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.StartEndCommand;
import ${data.basePackage}.subsystems.DriveSubsystem;
import ${data.basePackage}.subsystems.HatchSubsystem;

import static ${data.basePackage}.Constants.AutoConstants.*;

/**
 * A complex auto command that drives forward, releases a hatch, and then drives backward.
 */
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
                new StartEndCommand(
                        // Start driving forward at the start of the command
                        () -> driveSubsystem.arcadeDrive(AUTO_DRIVE_SPEED, 0),
                        // Stop driving at the end of the command
                        () -> driveSubsystem.arcadeDrive(0, 0), driveSubsystem
                )
                        // Reset the encoders before starting
                        .beforeStarting(driveSubsystem::resetEncoders)
                        // End the command when the robot's driven distance exceeds the desired value
                        .withInterrupt(
                                () -> driveSubsystem.getAverageEncoderDistance() >= AUTO_DRIVE_DISTANCE_INCHES),

                // Release the hatch
                new InstantCommand(hatchSubsystem::releaseHatch, hatchSubsystem),

                // Drive backward the specified distance
                new StartEndCommand(
                        () -> driveSubsystem.arcadeDrive(-AUTO_DRIVE_SPEED, 0),
                        () -> driveSubsystem.arcadeDrive(0, 0),
                        driveSubsystem
                )
                        .beforeStarting(driveSubsystem::resetEncoders)
                        .withInterrupt(
                                () -> driveSubsystem.getAverageEncoderDistance() <= -AUTO_BACKUP_DISTANCE_INCHES)
        );
    }

}
