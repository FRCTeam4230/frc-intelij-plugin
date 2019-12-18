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

import edu.wpi.first.wpilibj2.command.CommandBase;
import ${data.basePackage}.subsystems.DriveSubsystem;

public class DriveDistance extends CommandBase
{
    private final DriveSubsystem drive;
    private final double distance;
    private final double speed;

    /**
     * Creates a new DriveDistance.
     *
     * @param inches The number of inches the robot will drive
     * @param speed  The speed at which the robot will drive
     * @param drive  The drive subsystem on which this command will run
     */
    public DriveDistance(double inches, double speed, DriveSubsystem drive)
    {
        distance = inches;
        this.speed = speed;
        this.drive = drive;
    }

    @Override
    public void initialize()
    {
        drive.resetEncoders();
        drive.arcadeDrive(speed, 0);
    }

    @Override
    public void end(boolean interrupted)
    {
        drive.arcadeDrive(0, 0);
    }

    @Override
    public boolean isFinished()
    {
        return Math.abs(drive.getAverageEncoderDistance()) >= distance;
    }
}
