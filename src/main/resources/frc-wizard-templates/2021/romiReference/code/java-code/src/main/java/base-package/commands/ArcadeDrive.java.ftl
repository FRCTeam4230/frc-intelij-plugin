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

package ${data.basePackage}.commands;

import edu.wpi.first.wpilibj2.command.CommandBase;
import ${data.basePackage}.subsystems.Drivetrain;

import java.util.function.Supplier;

public class ArcadeDrive extends CommandBase
{
    private final Drivetrain drivetrain;
    private final Supplier<Double> xaxisSpeedSupplier;
    private final Supplier<Double> zaxisRotateSupplier;


    /**
     * Creates a new ArcadeDrive. This command will drive your robot according to the speed supplier
     * lambdas. This command does not terminate.
     *
     * @param drivetrain          The drivetrain subsystem on which this command will run
     * @param xaxisSpeedSupplier  Lambda supplier of forward/backward speed
     * @param zaxisRotateSupplier Lambda supplier of rotational speed
     */
    public ArcadeDrive(
            Drivetrain drivetrain,
            Supplier<Double> xaxisSpeedSupplier,
            Supplier<Double> zaxisRotateSupplier)
    {
        this.drivetrain = drivetrain;
        this.xaxisSpeedSupplier = xaxisSpeedSupplier;
        this.zaxisRotateSupplier = zaxisRotateSupplier;
        addRequirements(drivetrain);
    }


    // Called when the command is initially scheduled.
    @Override
    public void initialize() {}


    // Called every time the scheduler runs while the command is scheduled.
    @Override
    public void execute()
    {
        drivetrain.arcadeDrive(xaxisSpeedSupplier.get(), zaxisRotateSupplier.get());
    }


    // Called once the command ends or is interrupted.
    @Override
    public void end(boolean interrupted) {}


    // Returns true when the command should end.
    @Override
    public boolean isFinished()
    {
        return false;
    }
}
