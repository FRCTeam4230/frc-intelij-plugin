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
import edu.wpi.first.wpilibj.util.WPILibVersion;
</#if>

/**
 * The VM is configured to automatically run this class, and to call the run() method when the
 * robot is enabled. If you change the name of this class or the package after creating this
 * project, you must also update the build.gradle file in the project.
 */
public class ${data.robotClassSimpleName} extends EducationalRobot
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
    
    
    /** This method is run when the robot is enabled. */
    @Override
    public void run() {}
}
