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
${data.copyright}

package ${data.basePackage};

import edu.wpi.first.wpilibj.TimedRobot;

/**
 * The VM is configured to automatically run this class, and to call the methods corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class
 * or the package after creating this project, you must also update the build.gradle file in the
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
     * This method is run when the robot is first started up and should be used for any
     * initialization code.
     */
    @Override
    public void robotInit()
    {

    }

    @Override
    public void robotPeriodic()
    {

    }

    @Override
    public void autonomousInit()
    {

    }

    @Override
    public void autonomousPeriodic()
    {
        
    }

    @Override
    public void teleopInit()
    {
        
    }

    @Override
    public void teleopPeriodic()
    {
        
    }

    @Override
    public void disabledInit()
    {

    }

    @Override
    public void disabledPeriodic()
    {

    }

    @Override
    public void testInit()
    {
        
    }

    @Override
    public void testPeriodic()
    {
        
    }
}
