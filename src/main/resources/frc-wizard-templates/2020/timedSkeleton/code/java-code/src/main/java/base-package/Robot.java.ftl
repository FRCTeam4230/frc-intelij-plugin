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
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage};

import edu.wpi.first.wpilibj.TimedRobot;

/**
 * The VM is configured to automatically run this class, and to call the
 * methods corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.<br>
 * <br>
 * This class, via its super class, provides the following methods which are called by the main loop:<br>
 * <br>
 *   - startCompetition(), at the appropriate times:<br>
 * <br>
 *   - robotInit() -- provide for initialization at robot power-on<br>
 * <br>
 * init methods -- each of the following methods is called once when the appropriate mode is entered:<br>
 *     - disabledInit()   -- called each and every time disabled is entered from another mode<br>
 *     - autonomousInit() -- called each and every time autonomous is entered from another mode<br>
 *     - teleopInit()     -- called each and every time teleop is entered from another mode<br>
 *     - testInit()       -- called each and every time test is entered from another mode<br>
 * <br>
 * periodic methods -- each of these methods is called on an interval:<br>
 *   - robotPeriodic()<br>
 *   - disabledPeriodic()<br>
 *   - autonomousPeriodic()<br>
 *   - teleopPeriodic()<br>
 *   - testPeriodic()<br>
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    /**
     * This method is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit()
    {
        
    }

    /**
     * This method is called every robot packet, no matter the mode. Use
     * this for items like diagnostics that you want ran during disabled,
     * autonomous, teleoperated and test.
     * <p>
     * This runs after the mode specific periodic methods, but before
     * LiveWindow and SmartDashboard integrated updating.
     */
    @Override
    public void robotPeriodic()
    {
        
    }    

  /**
   * Initialization code for autonomous mode should go here.
   * <p>
   * Users should use this method for initialization code which will be called each time the
   * robot enters autonomous mode.
   */
    @Override
    public void autonomousInit()
    {
        
    }

    /**
     * This method is called periodically during autonomous.
     */
    @Override
    public void autonomousPeriodic()
    {
        
    }

    /**
     * Initialization code for teleop mode should go here.
     * <p>
     * Users should use this method for initialization code which will be called each time the
     * robot enters teleop mode.
     */
    @Override
    public void teleopInit()
    {
        
    }

    /**
     * This method is called periodically during operator control.
     */
    @Override
    public void teleopPeriodic()
    {
        
    }

  /**
   * Initialization code for test mode should go here.
   * <p>
   * Users should use this method for initialization code which will be called each time the
   * robot enters test mode.
   */
    @Override
    public void testInit()
    {
        
    }

    /**
     * This method is called periodically during test mode.
     */
    @Override
    public void testPeriodic()
    {
        
    }
}
