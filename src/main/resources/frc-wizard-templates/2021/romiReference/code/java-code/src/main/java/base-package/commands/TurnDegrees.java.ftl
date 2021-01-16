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

public class TurnDegrees extends CommandBase
{
    private final Drivetrain drivetrain;
    private final double degrees;
    private final double speed;


    /**
     * Creates a new TurnDegrees. This command will turn your robot for a desired rotation (in
     * degrees) and rotational speed.
     *
     * @param speed   The speed which the robot will drive. Negative is in reverse.
     * @param degrees Degrees to turn. Leverages encoders to compare distance.
     * @param drivetrain   The drivetrain subsystem on which this command will run
     */
    public TurnDegrees(double speed, double degrees, Drivetrain drivetrain)
    {
        this.degrees = degrees;
        this.speed = speed;
        this.drivetrain = drivetrain;
        addRequirements(drivetrain);
    }


    // Called when the command is initially scheduled.
    @Override
    public void initialize()
    {
        // Set motors to stop, read encoder values for starting point
        drivetrain.arcadeDrive(0, 0);
        drivetrain.resetEncoders();
    }


    // Called every time the scheduler runs while the command is scheduled.
    @Override
    public void execute()
    {
        drivetrain.arcadeDrive(0, speed);
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
        /*
           Need to convert distance travelled to degrees. The Standard
           Romi Chassis found here, https://www.pololu.com/category/203/romi-chassis-kits,
           has a wheel placement diameter (149 mm) - width of the wheel (8 mm) = 141 mm
           or 5.551 inches. We then take into consideration the width of the tires.
        */
        double inchPerDegree = Math.PI * 5.551 / 360;
        // Compare distance travelled from start to distance based on degree turn
        return getAverageTurningDistance() >= (inchPerDegree * degrees);
    }


    private double getAverageTurningDistance()
    {
        double leftDistance = Math.abs(drivetrain.getLeftDistanceInch());
        double rightDistance = Math.abs(drivetrain.getRightDistanceInch());
        return (leftDistance + rightDistance) / 2.0;
    }
}
