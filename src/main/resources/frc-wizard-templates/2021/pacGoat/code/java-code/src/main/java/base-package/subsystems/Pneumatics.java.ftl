<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:    https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when luanching IntelliJ IDEA -->
</#compress>
${data.copyright}

package ${data.basePackage}.subsystems;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import ${data.basePackage}.${data.robotClassSimpleName};

/**
 * The Pneumatics subsystem contains a pressure sensor.
 * <p>
 * NOTE: The simulator currently doesn't support the compressor or pressure sensors.
 */
public class Pneumatics extends Subsystem
{
    AnalogInput pressureSensor = new AnalogInput(3);

    private static final double MAX_PRESSURE = 2.55;

    public Pneumatics()
    {
        addChild("Pressure Sensor", pressureSensor);
    }

    /**
     * No default command.
     */
    @Override
    public void initDefaultCommand()
    {
    }

    /**
     * Whether or not the system is fully pressurized.
     */
    public boolean isPressurized()
    {
        if (Robot.isReal())
        {
            return MAX_PRESSURE <= pressureSensor.getVoltage();
        } else
        {
            return true; // NOTE: Simulation always has full pressure
        }
    }

    /**
     * Puts the pressure on the SmartDashboard.
     */
    public void writePressure()
    {
        SmartDashboard.putNumber("Pressure", pressureSensor.getVoltage());
    }
}
