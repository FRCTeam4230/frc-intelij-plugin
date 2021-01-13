<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
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

package ${data.basePackage};

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.wpilibj.AnalogPotentiometer;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardLayout;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;

/**
 * This is a sample program that demonstrates adds data to various Shuffleboard tabs, 
 * demonstrating the Shuffleboard API.
 * <p>
 * The VM is configured to automatically run this class, and to call the
 * methods corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    private final DifferentialDrive tankDrive =
            new DifferentialDrive(new PWMVictorSPX(0), new PWMVictorSPX(1));
    private final Encoder leftEncoder = new Encoder(0, 1);
    private final Encoder rightEncoder = new Encoder(2, 3);

    private final PWMVictorSPX elevatorMotor = new PWMVictorSPX(2);
    private final AnalogPotentiometer elevatorPot = new AnalogPotentiometer(0);
    private NetworkTableEntry maxSpeed;

    /**
     * This method is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit()
    {
        // Add a 'max speed' widget to a tab named 'Configuration', using a number slider
        // The widget will be placed in the second column and row and will be TWO columns wide
        maxSpeed = Shuffleboard
                .getTab("Configuration")
                .add("Max Speed", 1)
                .withWidget("Number Slider")
                .withPosition(1, 1)
                .withSize(2, 1)
                .getEntry();
        // Add the tank drive and encoders to a 'Drivebase' tab
        ShuffleboardTab driveBaseTab = Shuffleboard.getTab("Drivebase");
        driveBaseTab.add("Tank Drive", tankDrive);
        // Put both encoders in a list layout
        ShuffleboardLayout encoders = driveBaseTab.getLayout("List Layout", "Encoders")
                .withPosition(0, 0)
                .withSize(2, 2);
        encoders.add("Left Encoder", leftEncoder);
        encoders.add("Right Encoder", rightEncoder);

        // Add the elevator motor and potentiometer to an 'Elevator' tab
        ShuffleboardTab elevatorTab = Shuffleboard.getTab("Elevator");
        elevatorTab.add("Motor", elevatorMotor);
        elevatorTab.add("Potentiometer", elevatorPot);
    }

    /**
     * This method is run once each time the robot enters autonomous mode.
     */
    @Override
    public void autonomousInit()
    {
        // Read the value of the 'max speed' widget from the dashboard
        tankDrive.setMaxOutput(maxSpeed.getDouble(1.0));
    }
}