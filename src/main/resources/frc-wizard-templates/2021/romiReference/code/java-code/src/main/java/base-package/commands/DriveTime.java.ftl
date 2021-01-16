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
import ${data.basePackage}.subsystems.Drivetrain;

public class DriveTime extends CommandBase
{
    private final double duration;
    private final double speed;
    private final Drivetrain drivetrain;
    private long startTime;


    /**
     * Creates a new DriveTime. This command will drive your robot for a desired speed and time.
     *
     * @param speed      The speed which the robot will drive. Negative is in reverse.
     * @param time       How much time to drive in seconds
     * @param drivetrain The drivetrain subsystem on which this command will run
     */
    public DriveTime(double speed, double time, Drivetrain drivetrain)
    {
        this.speed = speed;
        duration = time * 1000;
        this.drivetrain = drivetrain;
        addRequirements(drivetrain);
    }


    // Called when the command is initially scheduled.
    @Override
    public void initialize()
    {
        startTime = System.currentTimeMillis();
        drivetrain.arcadeDrive(0, 0);
    }


    // Called every time the scheduler runs while the command is scheduled.
    @Override
    public void execute()
    {
        drivetrain.arcadeDrive(speed, 0);
    }


    // Called once the command ends or is interrupted.
    @Override
    public void end(boolean interrupted)
    {
        drivetrain.arcadeDrive(0, 0);
    }


    // Returns true when the command should end.
    @Override
    public boolean isFinished()
    {
        return (System.currentTimeMillis() - startTime) >= duration;
    }
}
