<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:    https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when luanching the testing instance of IntelliJ IDEA -->
</#compress>
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage};

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import ${data.basePackage}.commands.Autonomous;
import ${data.basePackage}.subsystems.Claw;
import ${data.basePackage}.subsystems.DriveTrain;
import ${data.basePackage}.subsystems.Elevator;
import ${data.basePackage}.subsystems.Wrist;

/**
 * The VM is configured to automatically run this class, and to call the
 * methods corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    private Command autonomousCommand;

    public static DriveTrain drivetrain;
    public static Elevator elevator;
    public static Wrist wrist;
    public static Claw claw;
    public static OI oi;

    /**
     * This method is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit()
    {
        // Initialize all subsystems
        drivetrain = new DriveTrain();
        elevator = new Elevator();
        wrist = new Wrist();
        claw = new Claw();
        oi = new OI();

        // instantiate the command used for the autonomous period
        autonomousCommand = new Autonomous();

        // Show what command your subsystem is running on the SmartDashboard
        SmartDashboard.putData(drivetrain);
        SmartDashboard.putData(elevator);
        SmartDashboard.putData(wrist);
        SmartDashboard.putData(claw);
    }

    @Override
    public void autonomousInit()
    {
        autonomousCommand.start(); // schedule the autonomous command (example)
    }

    /**
     * This method is called periodically during autonomous.
     */
    @Override
    public void autonomousPeriodic()
    {
        Scheduler.getInstance().run();
        log();
    }

    @Override
    public void teleopInit()
    {
        // This makes sure that the autonomous stops running when
        // teleop starts running. If you want the autonomous to
        // continue until interrupted by another command, remove
        // this line or comment it out.
        autonomousCommand.cancel();
    }

    /**
     * This method is called periodically during teleoperated mode.
     */
    @Override
    public void teleopPeriodic()
    {
        Scheduler.getInstance().run();
        log();
    }

    /**
     * This method is called periodically during test mode.
     */
    @Override
    public void testPeriodic()
    {
    }

    /**
     * The log method puts interesting information to the SmartDashboard.
     */
    private void log()
    {
        wrist.log();
        elevator.log();
        drivetrain.log();
        claw.log();
    }
}