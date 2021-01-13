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

package ${data.basePackage}.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import ${data.basePackage}.subsystems.DriveSubsystem;

public class HalveDriveSpeed extends CommandBase
{
    private final DriveSubsystem drive;
    
    public HalveDriveSpeed(DriveSubsystem drive)
    {
        this.drive = drive;
    }
    
    @Override
    public void initialize()
    {
        drive.setMaxOutput(0.5);
    }
    
    @Override
    public void end(boolean interrupted)
    {
        drive.setMaxOutput(1);
    }
}

