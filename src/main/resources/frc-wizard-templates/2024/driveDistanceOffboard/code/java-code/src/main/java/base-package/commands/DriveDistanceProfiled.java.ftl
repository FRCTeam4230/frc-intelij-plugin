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

import edu.wpi.first.math.trajectory.TrapezoidProfile;
import ${data.basePackage}.Constants.DriveConstants;
import ${data.basePackage}.subsystems.DriveSubsystem;
import edu.wpi.first.wpilibj2.command.TrapezoidProfileCommand;



/** Drives a set distance using a motion profile. */
public class DriveDistanceProfiled extends TrapezoidProfileCommand
{
    /**
     * Creates a new DriveDistanceProfiled command.
     *
     * @param meters The distance to drive.
     * @param drive  The drive subsystem to use.
     */
    public DriveDistanceProfiled(double meters, DriveSubsystem drive)
    {
        super(
                new TrapezoidProfile(
                        // Limit the max acceleration and velocity
                        new TrapezoidProfile.Constraints(
                                DriveConstants.MAX_SPEED_METERS_PER_SECOND,
                                DriveConstants.MAX_ACCELERATION_METERS_PER_SECOND_SQUARED)),
                // Pipe the profile state to the drive
                setpointState -> drive.setDriveStates(setpointState, setpointState),
                // End at desired position in meters; implicitly starts at 0
                () -> new TrapezoidProfile.State(meters, 0),
                // Current position
                TrapezoidProfile.State::new,
                // Require the drive
                drive);
        // Reset drive encoders since we're starting at 0
        drive.resetEncoders();
    }
}
