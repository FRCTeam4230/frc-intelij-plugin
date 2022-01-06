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

import edu.wpi.first.wpilibj.DutyCycleEncoder;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * methods corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    private DutyCycleEncoder dutyCycleEncoder;

    @Override
    public void robotInit()
    {
        dutyCycleEncoder = new DutyCycleEncoder(0);

        // Set to 0.5 units per rotation
        dutyCycleEncoder.setDistancePerRotation(0.5);
    }

    @Override
    public void robotPeriodic()
    {
        // Connected can be checked, and uses the frequency of the encoder
        boolean connected = dutyCycleEncoder.isConnected();

        // Duty Cycle Frequency in Hz
        int frequency = dutyCycleEncoder.getFrequency();

        // Output of encoder
        double output = dutyCycleEncoder.get();

        // Output scaled by DistancePerPulse
        double distance = dutyCycleEncoder.getDistance();

        SmartDashboard.putBoolean("Connected", connected);
        SmartDashboard.putNumber("Frequency", frequency);
        SmartDashboard.putNumber("Output", output);
        SmartDashboard.putNumber("Distance", distance);
    }
}
