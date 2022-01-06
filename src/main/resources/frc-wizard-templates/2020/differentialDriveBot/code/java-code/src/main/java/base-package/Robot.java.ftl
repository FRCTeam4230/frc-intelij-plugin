<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:     https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when launching the testing instance of IntelliJ IDEA -->
</#compress>
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage};

import edu.wpi.first.wpilibj.GenericHID;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;

import static ${data.basePackage}.Drivetrain.MAX_ANGULAR_SPEED;
import static ${data.basePackage}.Drivetrain.MAX_SPEED;

public class ${data.robotClassSimpleName} extends TimedRobot
{
    private final XboxController controller = new XboxController(0);
    private final Drivetrain drive = new Drivetrain();

    @Override
    public void autonomousPeriodic()
    {
        teleopPeriodic();
        drive.updateOdometry();
    }

    @Override
    public void teleopPeriodic()
    {
        // Get the x speed. We are inverting this because Xbox controllers return
        // negative values when we push forward.
        final var xSpeed = -controller.getY(GenericHID.Hand.kLeft) * MAX_SPEED;

        // Get the rate of angular rotation. We are inverting this because we want a
        // positive value when we pull to the left (remember, CCW is positive in
        // mathematics). Xbox controllers return positive values when you pull to
        // the right by default.
        final var rot = -controller.getX(GenericHID.Hand.kRight) * MAX_ANGULAR_SPEED;

        drive.drive(xSpeed, rot);
    }
}
