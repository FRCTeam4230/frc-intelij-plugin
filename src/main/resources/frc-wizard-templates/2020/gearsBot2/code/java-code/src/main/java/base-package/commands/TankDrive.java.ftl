<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:     https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when launching IntelliJ IDEA -->
</#compress>
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import ${data.basePackage}.subsystems.DriveTrain;

import java.util.function.DoubleSupplier;

/**
 * Have the robot drive tank style.
 */
public class TankDrive extends CommandBase
{
    private final DriveTrain driveTrain;
    private final DoubleSupplier left;
    private final DoubleSupplier right;

    /**
     * Creates a new TankDrive command.
     *
     * @param left       The control input for the left side of the drive
     * @param right      The control input for the right sight of the drive
     * @param driveTrain The driveTrain subsystem to drive
     */
    public TankDrive(DoubleSupplier left, DoubleSupplier right, DriveTrain driveTrain)
    {
        this.driveTrain = driveTrain;
        this.left = left;
        this.right = right;
        addRequirements(this.driveTrain);
    }

    // Called repeatedly when this Command is scheduled to run
    @Override
    public void execute()
    {
        driveTrain.drive(left.getAsDouble(), right.getAsDouble());
    }

    // Make this return true when this Command no longer needs to run execute()
    @Override
    public boolean isFinished()
    {
        return false; // Runs until interrupted
    }

    // Called once after isFinished returns true
    @Override
    public void end(boolean interrupted)
    {
        driveTrain.drive(0, 0);
    }
}
