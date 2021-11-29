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
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.commands;

import edu.wpi.first.wpilibj.command.Command;
import ${data.basePackage}.${data.robotClassSimpleName};

/**
 * This command drives the robot over a given distance with simple proportional
 * control This command will drive a given distance limiting to a maximum speed.
 */
public class DriveForward extends Command
{
    private final double driveForwardSpeed;
    private final double distance;
    private double error;
    private static final double TOLERANCE = 0.1;
    private static final double P = -1.0 / 5.0;

    public DriveForward()
    {
        this(10, 0.5);
    }

    public DriveForward(double dist)
    {
        this(dist, 0.5);
    }

    /**
     * Create a new drive forward command.
     *
     * @param dist     The distance to drive
     * @param maxSpeed The maximum speed to drive at
     */
    public DriveForward(double dist, double maxSpeed)
    {
        requires(Robot.drivetrain);
        distance = dist;
        driveForwardSpeed = maxSpeed;
    }

    @Override
    protected void initialize()
    {
        Robot.drivetrain.getRightEncoder().reset();
        setTimeout(2);
    }

    @Override
    protected void execute()
    {
        error = distance - Robot.drivetrain.getRightEncoder().getDistance();
        if (driveForwardSpeed * P * error >= driveForwardSpeed)
        {
            Robot.drivetrain.tankDrive(driveForwardSpeed, driveForwardSpeed);
        } else
        {
            Robot.drivetrain.tankDrive(driveForwardSpeed * P * error,
                    driveForwardSpeed * P * error);
        }
    }

    @Override
    protected boolean isFinished()
    {
        return Math.abs(error) <= TOLERANCE || isTimedOut();
    }

    @Override
    protected void end()
    {
        Robot.drivetrain.stop();
    }
}
