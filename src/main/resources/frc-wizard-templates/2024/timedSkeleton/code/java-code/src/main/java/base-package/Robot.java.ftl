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

<#if data.getIncludeKotlinSupport()>
import edu.wpi.first.hal.FRCNetComm.tInstances;
import edu.wpi.first.hal.FRCNetComm.tResourceType;
import edu.wpi.first.hal.HAL;
</#if>
import edu.wpi.first.wpilibj.TimedRobot;
<#if data.getIncludeKotlinSupport()>
import edu.wpi.first.wpilibj.util.WPILibVersion;
</#if>

/**
 * The VM is configured to automatically run this class, and to call the methods corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    /**
     * This method is run when the robot is first started up and should be used for any
     * initialization code.
     */
    @Override
    <#if data.getIncludeKotlinSupport()>
    public void robotInit()
    {
        // Report the use of Kotlin for "FRC Usage Report" statistics.
        // Please only remove if you remove *all* use of Kotlin from the robot.
        HAL.report(tResourceType.kResourceType_Language, tInstances.kLanguage_Kotlin, 0, WPILibVersion.Version);
    }
    <#else>
    public void robotInit() {}
    </#if>
    
    
    @Override
    public void robotPeriodic() {}
    
    
    @Override
    public void autonomousInit() {}
    
    
    @Override
    public void autonomousPeriodic() {}
    
    
    @Override
    public void teleopInit() {}
    
    
    @Override
    public void teleopPeriodic() {}
    
    
    @Override
    public void disabledInit() {}
    
    
    @Override
    public void disabledPeriodic() {}
    
    
    @Override
    public void testInit() {}
    
    
    @Override
    public void testPeriodic() {}
    
    
    @Override
    public void simulationInit() {}
    
    
    @Override
    public void simulationPeriodic() {}
}
