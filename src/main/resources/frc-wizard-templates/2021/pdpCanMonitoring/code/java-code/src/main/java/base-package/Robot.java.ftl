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

import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * This is a sample program showing how to retrieve information from the Power Distribution Panel
 * via CAN. The information will be displayed under variables through the SmartDashboard.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    private static final int PDP_ID = 0;

    private final PowerDistributionPanel pdp = new PowerDistributionPanel(PDP_ID);

    @Override
    public void robotPeriodic()
    {
        /*
           Get the current going through channel 7, in Amperes. The PDP returns the
           current in increments of 0.125A. At low currents
           the current readings tend to be less accurate.
         */
        SmartDashboard.putNumber("Current Channel 7", pdp.getCurrent(7));

        /*
           Get the voltage going into the PDP, in Volts.
           The PDP returns the voltage in increments of 0.05 Volts.
         */
        SmartDashboard.putNumber("Voltage", pdp.getVoltage());

        /*
           Retrieves the temperature of the PDP, in degrees Celsius.
         */
        SmartDashboard.putNumber("Temperature", pdp.getTemperature());
    }
}
