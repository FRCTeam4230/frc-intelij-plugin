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
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.commands;

import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import ${data.basePackage}.subsystems.DriveSubsystem;
import ${data.basePackage}.subsystems.HatchSubsystem;

import static ${data.basePackage}.Constants.AutoConstants.*;

/**
 * A complex auto command that drives forward, releases a hatch, and then drives backward.
 */
public class ComplexAuto extends SequentialCommandGroup
{
    /**
     * Creates a new ComplexAuto.
     *
     * @param drive The drive subsystem this command will run on
     * @param hatch The hatch subsystem this command will run on
     */
    public ComplexAuto(DriveSubsystem drive, HatchSubsystem hatch)
    {
        addCommands(
                // Drive forward the specified distance
                new DriveDistance(AUTO_DRIVE_DISTANCE_INCHES, AUTO_DRIVE_SPEED, drive),
                
                // Release the hatch
                new ReleaseHatch(hatch),
                
                // Drive backward the specified distance
                new DriveDistance(AUTO_BACKUP_DISTANCE_INCHES, -AUTO_DRIVE_SPEED, drive)
        );
    }
}
