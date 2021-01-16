<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:    https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when luanching IntelliJ IDEA -->
</#compress>
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.commands;

import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj2.command.PIDCommand;
import ${data.basePackage}.subsystems.DriveTrain;

/**
 * Drive the given distance straight (negative values go backwards). Uses a
 * local PID controller to run a simple PID loop that is only enabled while this
 * command is running. The input is the averaged values of the left and right
 * encoders.
 */
public class DriveStraight extends PIDCommand
{
    private final DriveTrain driveTrain;

    /**
     * Create a new DriveStraight command.
     *
     * @param distance The distance to drive
     */
    public DriveStraight(double distance, DriveTrain driveTrain)
    {
        super(new PIDController(4, 0, 0),
                driveTrain::getDistance,
                distance,
                d -> driveTrain.drive(d, d));

        this.driveTrain = driveTrain;
        addRequirements(this.driveTrain);

        getController().setTolerance(0.01);
    }

    // Called just before this Command runs the first time
    @Override
    public void initialize()
    {
        // Get everything in a safe starting state.
        driveTrain.reset();
        super.initialize();
    }

    // Make this return true when this Command no longer needs to run execute()
    @Override
    public boolean isFinished()
    {
        return getController().atSetpoint();
    }
}
