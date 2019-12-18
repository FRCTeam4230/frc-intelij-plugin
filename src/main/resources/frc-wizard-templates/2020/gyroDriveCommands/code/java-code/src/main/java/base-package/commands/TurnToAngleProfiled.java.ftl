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

import edu.wpi.first.wpilibj.controller.ProfiledPIDController;
import edu.wpi.first.wpilibj.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.ProfiledPIDCommand;
import ${data.basePackage}.subsystems.DriveSubsystem;

import static ${data.basePackage}.Constants.DriveConstants.*;

/**
 * A command that will turn the robot to the specified angle using a motion profile.
 */
public class TurnToAngleProfiled extends ProfiledPIDCommand
{
    /**
     * Turns to robot to the specified angle using a motion profile.
     *
     * @param targetAngleDegrees The angle to turn to
     * @param drive              The drive subsystem to use
     */
    public TurnToAngleProfiled(double targetAngleDegrees, DriveSubsystem drive)
    {
        super(
                new ProfiledPIDController(TURN_P, TURN_I, TURN_D,
                        new TrapezoidProfile.Constraints(
                                MAX_TURN_RATE_DEG_PER_S,
                                MAX_TURN_ACCELERATION_DEG_PER_S_SQUARED
                        )),
                // Close loop on heading
                drive::getHeading,
                // Set reference to target
                targetAngleDegrees,
                // Pipe output to turn robot
                (output, setpoint) -> drive.arcadeDrive(0, output),
                // Require the drive
                drive);

        // Set the controller to be continuous (because it is an angle controller)
        getController().enableContinuousInput(-180, 180);
        // Set the controller tolerance - the delta tolerance ensures the robot is stationary at the
        // setpoint before it is considered as having reached the reference
        getController().setTolerance(TURN_TOLERANCE_DEG, TURN_RATE_TOLERANCE_DEG_PER_S);
    }

    @Override
    public boolean isFinished()
    {
        // End when the controller is at the reference.
        return getController().atGoal();
    }
}
