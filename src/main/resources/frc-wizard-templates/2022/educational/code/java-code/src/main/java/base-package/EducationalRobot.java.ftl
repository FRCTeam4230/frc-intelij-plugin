<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement-->
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

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;



/** Educational robot base class. Do NOT use for competions. This is a simple robot used for teaching purposes. */
public class EducationalRobot extends RobotBase
{
    public void robotInit() {}
    
    
    public void disabled() {}
    
    
    public void run() {}
    
    
    public void autonomous()
    {
        run();
    }
    
    
    public void teleop()
    {
        run();
    }
    
    
    public void test()
    {
        run();
    }
    
    
    private volatile boolean exit;
    
    
    @Override
    public void startCompetition()
    {
        robotInit();
        
        // Tell the DS that the robot is ready to be enabled
        HAL.observeUserProgramStarting();
        
        while (!Thread.currentThread().isInterrupted() && !exit)
        {
            if (isDisabled())
            {
                DriverStation.inDisabled(true);
                disabled();
                DriverStation.inDisabled(false);
                while (isDisabled())
                {
                    DriverStation.waitForData();
                }
            }
            else if (isAutonomous())
            {
                DriverStation.inAutonomous(true);
                autonomous();
                DriverStation.inAutonomous(false);
                while (isAutonomousEnabled())
                {
                    DriverStation.waitForData();
                }
            }
            else if (isTest())
            {
                DriverStation.inTest(true);
                test();
                DriverStation.inTest(false);
                while (isTest() && isEnabled())
                {
                    DriverStation.waitForData();
                }
            }
            else
            {
                DriverStation.inTeleop(true);
                teleop();
                DriverStation.inTeleop(false);
                while (isTeleopEnabled())
                {
                    DriverStation.waitForData();
                }
            }
        }
    }
    
    
    @Override
    public void endCompetition()
    {
        exit = true;
    }
}
