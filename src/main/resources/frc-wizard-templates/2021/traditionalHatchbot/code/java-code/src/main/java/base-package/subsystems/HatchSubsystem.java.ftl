<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:    https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when luanching the testing instance of IntelliJ IDEA -->
</#compress>
${data.copyright}

package ${data.basePackage}.subsystems;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import static edu.wpi.first.wpilibj.DoubleSolenoid.Value.kForward;
import static edu.wpi.first.wpilibj.DoubleSolenoid.Value.kReverse;
import static ${data.basePackage}.Constants.HatchConstants.HATCH_SOLENOID_MODULE;
import static ${data.basePackage}.Constants.HatchConstants.HATCH_SOLENOID_PORTS;

/**
 * A hatch mechanism actuated by a single {@link DoubleSolenoid}.
 */
public class HatchSubsystem extends SubsystemBase
{
    private final DoubleSolenoid hatchSolenoid =
            new DoubleSolenoid(HATCH_SOLENOID_MODULE, HATCH_SOLENOID_PORTS[0], HATCH_SOLENOID_PORTS[1]);

    /**
     * Grabs the hatch.
     */
    public void grabHatch()
    {
        hatchSolenoid.set(kForward);
    }

    /**
     * Releases the hatch.
     */
    public void releaseHatch()
    {
        hatchSolenoid.set(kReverse);
    }
}
