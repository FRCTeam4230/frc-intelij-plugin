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
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage};

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * This is a demo program showing the use of the RobotDrive class. The
 * SampleRobot class is the base of a robot application that will automatically
 * call your Autonomous and OperatorControl methods at the right time as
 * controlled by the switches on the driver station or the field controls.
 * <p>
 * The VM is configured to automatically run this class, and to call the
 * methods corresponding to each mode, as described in the SampleRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.properties file in the
 * project.
 * <p>
 * <b>WARNING:</b> While it may look like a good choice to use for your code if
 * you're inexperienced, don't. Unless you know what you are doing, complex code
 * will be much more difficult under this system. Use TimedRobot or
 * Command-Based instead if you're new.
 */
public class ${data.robotClassSimpleName} extends SampleRobot
{
    private static final String DEFAULT_AUTO = "Default";
    private static final String CUSTOM_AUTO = "My Auto";

    private final DifferentialDrive robotDrive
            = new DifferentialDrive(new PWMVictorSPX(0), new PWMVictorSPX(1));
    private final Joystick stick = new Joystick(0);
    private final SendableChooser<String> chooser = new SendableChooser<>();

    public Robot()
    {
        robotDrive.setExpiration(0.1);
    }

    @Override
    public void robotInit()
    {
        chooser.setDefaultOption("Default Auto", DEFAULT_AUTO);
        chooser.addOption("My Auto", CUSTOM_AUTO);
        SmartDashboard.putData("Auto modes", chooser);
    }

    /**
     * This autonomous (along with the chooser code above) shows how to select
     * between different autonomous modes using the dashboard. The sendable
     * chooser code works with the Java SmartDashboard. If you prefer the
     * LabVIEW Dashboard, remove all of the chooser code and uncomment the
     * getString line to get the auto name from the text box below the Gyro
     * <p>
     * You can add additional auto modes by adding additional comparisons to
     * the if-else structure below with additional strings. If using the
     * SendableChooser make sure to add them to the chooser code above as well.
     * <p>
     * If you wanted to run a similar autonomous mode with an TimedRobot
     * you would write:
     *
     * <blockquote><pre>{@code
     * Timer timer = new Timer();
     *
     * // This method is run once each time the robot enters autonomous mode
     * public void autonomousInit() {
     *     timer.reset();
     *     timer.start();
     * }
     *
     * // This method is called periodically during autonomous
     * public void autonomousPeriodic() {
     * // Drive for 2 seconds
     *     if (timer.get() < 2.0) {
     *         myRobot.drive(-0.5, 0.0); // drive forwards half speed
     *     } else if (timer.get() < 5.0) {
     *         myRobot.drive(-1.0, 0.0); // drive forwards full speed
     *     } else {
     *         myRobot.drive(0.0, 0.0); // stop robot
     *     }
     * }
     * }</pre></blockquote>
     */
    @Override
    public void autonomous()
    {
        String autoSelected = chooser.getSelected();
        // String autoSelected = SmartDashboard.getString("Auto Selector",
        // defaultAuto);
        System.out.println("Auto selected: " + autoSelected);

        // MotorSafety improves safety when motors are updated in loops
        // but is disabled here because motor updates are not looped in
        // this autonomous mode.
        robotDrive.setSafetyEnabled(false);

        switch (autoSelected)
        {
            case CUSTOM_AUTO:
                // Spin at half speed for two seconds
                robotDrive.arcadeDrive(0.0, 0.5);
                Timer.delay(2.0);

                // Stop robot
                robotDrive.arcadeDrive(0.0, 0.0);
                break;
            case DEFAULT_AUTO:
            default:
                // Drive forwards for two seconds
                robotDrive.arcadeDrive(-0.5, 0.0);
                Timer.delay(2.0);

                // Stop robot
                robotDrive.arcadeDrive(0.0, 0.0);
                break;
        }
    }

    /**
     * Runs the motors with arcade steering.
     * <p>
     * If you wanted to run a similar teleoperated mode with an TimedRobot
     * you would write:
     *
     * <blockquote><pre>{@code
     * // This method is called periodically during operator control
     * public void teleopPeriodic() {
     *     myRobot.arcadeDrive(stick);
     * }
     * }</pre></blockquote>
     */
    @Override
    public void operatorControl()
    {
        robotDrive.setSafetyEnabled(true);
        while (isOperatorControl() && isEnabled())
        {
            // Drive arcade style
            robotDrive.arcadeDrive(-stick.getY(), stick.getX());

            // The motors will be updated every 5ms
            Timer.delay(0.005);
        }
    }

    /**
     * Runs during test mode.
     */
    @Override
    public void test()
    {
    }
}
