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
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage};

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import ${data.basePackage}.commands.Collect;
import ${data.basePackage}.commands.DriveForward;
import ${data.basePackage}.commands.LowGoal;
import ${data.basePackage}.commands.SetCollectionSpeed;
import ${data.basePackage}.commands.SetPivotSetpoint;
import ${data.basePackage}.commands.Shoot;
import ${data.basePackage}.subsystems.Collector;
import ${data.basePackage}.subsystems.Pivot;
import ${data.basePackage}.triggers.DoubleButton;

/**
 * The Operator Interface (OI) of the robot, it has been simplified from the
 * real robot to allow control with a single PS3 joystick. As a result, not
 * all functionality from the real robot is available.
 */
public class OI
{
    public Joystick joystick = new Joystick(0);

    /**
     * Create a new OI and all of the buttons on it.
     */
    public OI()
    {
        new JoystickButton(joystick, 12).whenPressed(new LowGoal());
        new JoystickButton(joystick, 10).whenPressed(new Collect());

        new JoystickButton(joystick, 11).whenPressed(new SetPivotSetpoint(Pivot.SHOOT));
        new JoystickButton(joystick, 9).whenPressed(new SetPivotSetpoint(Pivot.SHOOT_NEAR));

        new DoubleButton(joystick, 2, 3).whenActive(new Shoot());

        // SmartDashboard Buttons
        SmartDashboard.putData("Drive Forward", new DriveForward(2.25));
        SmartDashboard.putData("Drive Backward", new DriveForward(-2.25));
        SmartDashboard.putData("Start Rollers", new SetCollectionSpeed(Collector.FORWARD));
        SmartDashboard.putData("Stop Rollers", new SetCollectionSpeed(Collector.STOP));
        SmartDashboard.putData("Reverse Rollers", new SetCollectionSpeed(Collector.REVERSE));
    }

    public Joystick getJoystick()
    {
        return joystick;
    }
}
