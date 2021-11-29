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

import edu.wpi.first.wpilibj.GenericHID.RumbleType;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;

/**
 * This is a demo program showing the use of GenericHID's rumble feature.
 * (HID = Human Interface Device)
 * <p>
 * The VM is configured to automatically run this class, and to call the
 * methods corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    private final XboxController hid = new XboxController(0);

    /**
     * This method is run once each time the robot enters autonomous mode.
     */
    @Override
    public void autonomousInit()
    {
        // Turn on rumble at the start of auto
        hid.setRumble(RumbleType.kLeftRumble, 1.0);
        hid.setRumble(RumbleType.kRightRumble, 1.0);
    }

    /**
     * This method is called once each time the robot enters Disabled mode.
     * You can use it to reset any subsystem information you want to clear when
     * the robot is disabled.
     */
    @Override
    public void disabledInit()
    {
        // Stop the rumble when entering disabled
        hid.setRumble(RumbleType.kLeftRumble, 0.0);
        hid.setRumble(RumbleType.kRightRumble, 0.0);
    }
}
