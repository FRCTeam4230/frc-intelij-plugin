<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:    https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when luanching the testing instance of IntelliJ IDEA -->
</#compress>
${data.copyright}

package ${data.basePackage};

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import ${data.basePackage}.commands.DriveAndShootAutonomous;
import ${data.basePackage}.commands.DriveForward;
import ${data.basePackage}.subsystems.Collector;
import ${data.basePackage}.subsystems.DriveTrain;
import ${data.basePackage}.subsystems.Pivot;
import ${data.basePackage}.subsystems.Pneumatics;
import ${data.basePackage}.subsystems.Shooter;

/**
 * This is the main class for running the PacGoat code.
 * <p>
 * The VM is configured to automatically run this class, and to call the
 * methods corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project..
 */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    private Command autonomousCommand;
    public static OI oi;

    // Initialize the subsystems
    public static DriveTrain drivetrain = new DriveTrain();
    public static Collector collector = new Collector();
    public static Shooter shooter = new Shooter();
    public static Pneumatics pneumatics = new Pneumatics();
    public static Pivot pivot = new Pivot();

    private SendableChooser<Command> autoChooser;

    // This method is run when the robot is first started up and should be
    // used for any initialization code.
    @Override
    public void robotInit()
    {
        SmartDashboard.putData(drivetrain);
        SmartDashboard.putData(collector);
        SmartDashboard.putData(shooter);
        SmartDashboard.putData(pneumatics);
        SmartDashboard.putData(pivot);

        // This MUST be here. If the OI creates Commands (which it very likely
        // will), constructing it during the construction of CommandBase (from
        // which commands extend), subsystems are not guaranteed to be
        // yet. Thus, their requires() statements may grab null pointers. Bad
        // news. Don't move it.
        oi = new OI();

        // instantiate the command used for the autonomous period
        autoChooser = new SendableChooser<>();
        autoChooser.setDefaultOption("Drive and Shoot", new DriveAndShootAutonomous());
        autoChooser.addOption("Drive Forward", new DriveForward());
        SmartDashboard.putData("Auto Mode", autoChooser);
    }

    @Override
    public void autonomousInit()
    {
        autonomousCommand = autoChooser.getSelected();
        autonomousCommand.start();
    }

    // This method is called periodically during autonomous
    @Override
    public void autonomousPeriodic()
    {
        Scheduler.getInstance().run();
        log();
    }

    @Override
    public void teleopInit()
    {
        // This makes sure that the autonomous stops running when
        // teleop starts running. If you want the autonomous to
        // continue until interrupted by another command, remove
        // this line or comment it out.
        if (autonomousCommand != null)
        {
            autonomousCommand.cancel();
        }
    }

    // This method is called periodically during operator control
    @Override
    public void teleopPeriodic()
    {
        Scheduler.getInstance().run();
        log();
    }

    // This method called periodically during test mode
    @Override
    public void testPeriodic()
    {
    }

    @Override
    public void disabledInit()
    {
        Robot.shooter.unlatch();
    }

    // This method is called periodically while disabled
    @Override
    public void disabledPeriodic()
    {
        log();
    }

    /**
     * Log interesting values to the SmartDashboard.
     */
    private void log()
    {
        Robot.pneumatics.writePressure();
        SmartDashboard.putNumber("Pivot Pot Value", Robot.pivot.getAngle());
        SmartDashboard.putNumber("Left Distance", drivetrain.getLeftEncoder().getDistance());
        SmartDashboard.putNumber("Right Distance", drivetrain.getRightEncoder().getDistance());
    }
}