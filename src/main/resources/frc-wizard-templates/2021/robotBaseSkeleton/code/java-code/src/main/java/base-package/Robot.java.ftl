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
${data.copyright}

package ${data.basePackage};

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;

/**
 * The VM is configured to automatically run this class. If you change the name of this class or the
 * package after creating this project, you must also update the build.gradle file in the project.
 */
public class ${data.robotClassSimpleName} extends RobotBase
{
    // ***THIS ROBOT TEMPLATE IS MEANT FOR *HIGHLY ADVANCED/EXPERIENCED* USERS ONLY***
    //    Intermediate to advanced users should consider the "Timed Skeleton" or "Command Based Robot" templates
    //    Novice to intermediate users should use either the "Timed Robot" or "Command Based Robot" templates
 
    public void robotInit()
    {
        
    }

    public void disabled()
    {
        
    }

    public void autonomous()
    {
        
    }

    public void teleop()
    {
        
    }

    public void test()
    {
        
    }

    private volatile boolean exit;

    @SuppressWarnings("PMD.CyclomaticComplexity")
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
                m_ds.InDisabled(true);
                disabled();
                m_ds.InDisabled(false);
                while (isDisabled())
                {
                    m_ds.waitForData();
                }
            }
            else if (isAutonomous())
            {
                m_ds.InAutonomous(true);
                autonomous();
                m_ds.InAutonomous(false);
                while (isAutonomousEnabled())
                {
                    m_ds.waitForData();
                }
            }
            else if (isTest())
            {
                LiveWindow.setEnabled(true);
                Shuffleboard.enableActuatorWidgets();
                m_ds.InTest(true);
                test();
                m_ds.InTest(false);
                while (isTest() && isEnabled())
                {
                    m_ds.waitForData();
                }
                LiveWindow.setEnabled(false);
                Shuffleboard.disableActuatorWidgets();
            }
            else
            {
                m_ds.InOperatorControl(true);
                teleop();
                m_ds.InOperatorControl(false);
                while (isOperatorControlEnabled())
                {
                    m_ds.waitForData();
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
