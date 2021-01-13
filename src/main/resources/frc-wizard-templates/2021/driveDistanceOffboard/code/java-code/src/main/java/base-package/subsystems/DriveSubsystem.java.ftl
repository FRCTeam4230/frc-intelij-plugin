<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Refernce: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:    https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when luanching the testing instance of IntelliJ IDEA -->
</#compress>
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2019 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.subsystems;

import edu.wpi.first.wpilibj.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.trajectory.TrapezoidProfile;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import ${data.basePackage}.ExampleSmartMotorController;

import static ${data.basePackage}.Constants.DriveConstants.*;

public class DriveSubsystem extends SubsystemBase
{
    // The motors on the left side of the drive.
    private final ExampleSmartMotorController leftMaster =
            new ExampleSmartMotorController(LEFT_MOTOR_1_PORT);

    private final ExampleSmartMotorController leftSlave =
            new ExampleSmartMotorController(LEFT_MOTOR_2_PORT);

    private final ExampleSmartMotorController rightMaster =
            new ExampleSmartMotorController(RIGHT_MOTOR_1_PORT);

    private final ExampleSmartMotorController rightSlave =
            new ExampleSmartMotorController(RIGHT_MOTOR_2_PORT);

    private final SimpleMotorFeedforward feedforward =
            new SimpleMotorFeedforward(S_VOLTS,
                    V_VOLT_SECONDS_PER_METER,
                    A_VOLT_SECONDS_SQUARED_PER_METER);

    // The robot's drive
    private final DifferentialDrive drive = new DifferentialDrive(leftMaster, rightMaster);

    /**
     * Creates a new DriveSubsystem.
     */
    public DriveSubsystem()
    {
        leftSlave.follow(leftMaster);
        rightSlave.follow(rightMaster);

        leftMaster.setPID(P, 0, 0);
        rightMaster.setPID(P, 0, 0);
    }

    /**
     * Drives the robot using arcade controls.
     *
     * @param fwd the commanded forward movement
     * @param rot the commanded rotation
     */
    public void arcadeDrive(double fwd, double rot)
    {
        drive.arcadeDrive(fwd, rot);
    }

    /**
     * Attempts to follow the given drive states using offboard PID.
     *
     * @param left  The left wheel state.
     * @param right The right wheel state.
     */
    public void setDriveStates(TrapezoidProfile.State left, TrapezoidProfile.State right)
    {
        leftMaster.setSetpoint(ExampleSmartMotorController.PIDMode.Position,
                left.position,
                feedforward.calculate(left.velocity));
        rightMaster.setSetpoint(ExampleSmartMotorController.PIDMode.Position,
                right.position,
                feedforward.calculate(right.velocity));
    }

    /**
     * Returns the left encoder distance.
     *
     * @return the left encoder distance
     */
    public double getLeftEncoderDistance()
    {
        return leftMaster.getEncoderDistance();
    }

    /**
     * Returns the right encoder distance.
     *
     * @return the right encoder distance
     */
    public double getRightEncoderDistance()
    {
        return rightMaster.getEncoderDistance();
    }

    /**
     * Resets the drive encoders.
     */
    public void resetEncoders()
    {
        leftMaster.resetEncoder();
        rightMaster.resetEncoder();
    }

    /**
     * Sets the max output of the drive.  Useful for scaling the drive to drive more slowly.
     *
     * @param maxOutput the maximum output to which the drive will be constrained
     */
    public void setMaxOutput(double maxOutput)
    {
        drive.setMaxOutput(maxOutput);
    }
}
