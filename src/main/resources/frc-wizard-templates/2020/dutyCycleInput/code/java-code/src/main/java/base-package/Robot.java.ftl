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

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DutyCycle;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class ${data.robotClassSimpleName} extends TimedRobot
{
    private DigitalInput input;
    private DutyCycle dutyCycle;

    @Override
    public void robotInit()
    {
        input = new DigitalInput(0);
        dutyCycle = new DutyCycle(input);
    }

    @Override
    public void robotPeriodic()
    {
        // Duty Cycle Frequency in Hz
        int frequency = dutyCycle.getFrequency();

        // Output of duty cycle, ranging from 0 to 1
        // 1 is fully on, 0 is fully off
        double output = dutyCycle.getOutput();

        SmartDashboard.putNumber("Frequency", frequency);
        SmartDashboard.putNumber("Duty Cycle", output);
    }
}
