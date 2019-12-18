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
/* Copyright (c) 2018-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage};

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.PWMVictorSPX;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveKinematics;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveOdometry;
import edu.wpi.first.wpilibj.kinematics.MecanumDriveWheelSpeeds;

/**
 * Represents a mecanum drive style drivetrain.
 */
@SuppressWarnings("PMD.TooManyFields")
public class Drivetrain
{
    public static final double MAX_SPEED = 3.0; // 3 meters per second
    public static final double MAX_ANGULAR_SPEED = Math.PI; // 1/2 rotation per second

    private final SpeedController frontLeftMotor = new PWMVictorSPX(1);
    private final SpeedController frontRightMotor = new PWMVictorSPX(2);
    private final SpeedController backLeftMotor = new PWMVictorSPX(3);
    private final SpeedController backRightMotor = new PWMVictorSPX(4);

    private final Encoder frontLeftEncoder = new Encoder(0, 1);
    private final Encoder frontRightEncoder = new Encoder(2, 3);
    private final Encoder backLeftEncoder = new Encoder(4, 5);
    private final Encoder backRightEncoder = new Encoder(6, 7);

    private final Translation2d frontLeftLocation = new Translation2d(0.381, 0.381);
    private final Translation2d frontRightLocation = new Translation2d(0.381, -0.381);
    private final Translation2d backLeftLocation = new Translation2d(-0.381, 0.381);
    private final Translation2d backRightLocation = new Translation2d(-0.381, -0.381);

    private final PIDController frontLeftPIDController = new PIDController(1, 0, 0);
    private final PIDController frontRightPIDController = new PIDController(1, 0, 0);
    private final PIDController backLeftPIDController = new PIDController(1, 0, 0);
    private final PIDController backRightPIDController = new PIDController(1, 0, 0);

    private final AnalogGyro gyro = new AnalogGyro(0);

    private final MecanumDriveKinematics kinematics =
            new MecanumDriveKinematics(frontLeftLocation, frontRightLocation, backLeftLocation, backRightLocation);

    private final MecanumDriveOdometry m_odometry = new MecanumDriveOdometry(kinematics, getAngle());

    /**
     * Constructs a MecanumDrive and resets the gyro.
     */
    public Drivetrain()
    {
        gyro.reset();
    }

    /**
     * Returns the angle of the robot as a Rotation2d.
     *
     * @return The angle of the robot.
     */
    public Rotation2d getAngle()
    {
        // Negating the angle because WPILib gyros are CW positive.
        return Rotation2d.fromDegrees(-gyro.getAngle());
    }

    /**
     * Returns the current state of the drivetrain.
     *
     * @return The current state of the drivetrain.
     */
    public MecanumDriveWheelSpeeds getCurrentState()
    {
        return new MecanumDriveWheelSpeeds(
                frontLeftEncoder.getRate(),
                frontRightEncoder.getRate(),
                backLeftEncoder.getRate(),
                backRightEncoder.getRate()
        );
    }

    /**
     * Set the desired speeds for each wheel.
     *
     * @param speeds The desired wheel speeds.
     */
    public void setSpeeds(MecanumDriveWheelSpeeds speeds)
    {
        final var frontLeftOutput = frontLeftPIDController
                .calculate(frontLeftEncoder.getRate(), speeds.frontLeftMetersPerSecond);
        final var frontRightOutput = frontRightPIDController
                .calculate(frontRightEncoder.getRate(), speeds.frontRightMetersPerSecond);
        final var backLeftOutput = backLeftPIDController
                .calculate(backLeftEncoder.getRate(), speeds.rearLeftMetersPerSecond);
        final var backRightOutput = backRightPIDController
                .calculate(backRightEncoder.getRate(), speeds.rearRightMetersPerSecond);

        frontLeftMotor.set(frontLeftOutput);
        frontRightMotor.set(frontRightOutput);
        backLeftMotor.set(backLeftOutput);
        backRightMotor.set(backRightOutput);
    }

    /**
     * Method to drive the robot using joystick info.
     *
     * @param xSpeed        Speed of the robot in the x direction (forward).
     * @param ySpeed        Speed of the robot in the y direction (sideways).
     * @param rot           Angular rate of the robot.
     * @param fieldRelative Whether the provided x and y speeds are relative to the field.
     */
    @SuppressWarnings("ParameterName")
    public void drive(double xSpeed, double ySpeed, double rot, boolean fieldRelative)
    {
        var mecanumDriveWheelSpeeds = kinematics.toWheelSpeeds(
                fieldRelative ? ChassisSpeeds.fromFieldRelativeSpeeds(
                        xSpeed, ySpeed, rot, getAngle()
                ) : new ChassisSpeeds(xSpeed, ySpeed, rot)
        );
        mecanumDriveWheelSpeeds.normalize(MAX_SPEED);
        setSpeeds(mecanumDriveWheelSpeeds);
    }

    /**
     * Updates the field relative position of the robot.
     */
    public void updateOdometry()
    {
        m_odometry.update(getAngle(), getCurrentState());
    }
}
