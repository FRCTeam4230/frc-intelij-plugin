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
/* Copyright (c) 2017-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage};

import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

/**
 * This is a demo program showing the use of the DifferentialDrive class.
 * Runs the motors with tank steering and an Xbox controller.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    private final PWMVictorSPX leftMotor = new PWMVictorSPX(0);
    private final PWMVictorSPX rightMotor = new PWMVictorSPX(1);
    private final DifferentialDrive robotDrive = new DifferentialDrive(leftMotor, rightMotor);
    private final XboxController driverController = new XboxController(0);

    @Override
    public void teleopPeriodic()
    {
        // Drive with tank drive.
        // That means that the Y axis of the left stick moves the left side
        // of the robot forward and backward, and the Y axis of the right stick
        // moves the right side of the robot forward and backward.
        robotDrive.tankDrive(driverController.getY(Hand.kLeft), driverController.getY(Hand.kRight));
    }
}