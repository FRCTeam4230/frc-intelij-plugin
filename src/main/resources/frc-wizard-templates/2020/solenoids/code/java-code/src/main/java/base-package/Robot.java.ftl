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
/* Copyright (c) 2017-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage};

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.TimedRobot;

/**
 * This is a sample program showing the use of the solenoid classes during operator control. Three
 * buttons from a joystick will be used to control two solenoids: One button to control the position
 * of a single solenoid and the other two buttons to control a double solenoid. Single solenoids can
 * either be on or off, such that the air diverted through them goes through either one channel or
 * the other. Double solenoids have three states: Off, Forward, and Reverse. Forward and Reverse
 * divert the air through the two channels and correspond to the on and off of a single solenoid,
 * but a double solenoid can also be "off", where the solenoid will remain in its default power off
 * state. Additionally, double solenoids take up two channels on your PCM whereas single solenoids
 * only take a single channel.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    private final Joystick joystick = new Joystick(0);

    // Solenoid corresponds to a single solenoid.
    private final Solenoid solenoid = new Solenoid(0);

    // DoubleSolenoid corresponds to a double solenoid.
    private final DoubleSolenoid doubleSolenoid = new DoubleSolenoid(1, 2);

    private static final int SOLENOID_BUTTON = 1;
    private static final int DOUBLE_SOLENOID_FORWARD = 2;
    private static final int DOUBLE_SOLENOID_REVERSE = 3;

    @Override
    public void teleopPeriodic()
    {
        /*
           The output of GetRawButton is true/false depending on whether
           the button is pressed; Set takes a boolean for whether
           to use the default (false) channel or the other (true).
         */
        solenoid.set(joystick.getRawButton(SOLENOID_BUTTON));

        /*
           In order to set the double solenoid, if just one button
           is pressed, set the solenoid to correspond to that button.
           If both are pressed, set the solenoid will be set to Forwards.
         */
        if (joystick.getRawButton(DOUBLE_SOLENOID_FORWARD))
        {
            doubleSolenoid.set(DoubleSolenoid.Value.kForward);
        } else if (joystick.getRawButton(DOUBLE_SOLENOID_REVERSE))
        {
            doubleSolenoid.set(DoubleSolenoid.Value.kReverse);
        }
    }
}
