<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
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

package ${data.basePackage};

import edu.wpi.first.wpilibj.SpeedController;

/**
 * A simplified stub class that simulates the API of a common "smart" motor controller.
 * <p>
 * Has no actual functionality.
 */
public class ExampleSmartMotorController implements SpeedController
{
    public enum PIDMode
    {
        Position,
        Velocity,
        MovementWitchcraft
    }

    /**
     * Creates a new ExampleSmartMotorController.
     *
     * @param port The port for the controller.
     */
    public ExampleSmartMotorController(int port)
    {

    }

    /**
     * Example method for setting the PID gains of the smart controller.
     *
     * @param p The proportional gain.
     * @param i The integral gain.
     * @param d The derivative gain.
     */
    public void setPID(double p, double i, double d)
    {
    }

    /**
     * Example method for setting the setpoint of the smart controller in PID mode.
     *
     * @param mode           The mode of the PID controller.
     * @param setpoint       The controller setpoint.
     * @param arbFeedForward An arbitrary feed forward output (from -1 to 1).
     */
    public void setSetpoint(PIDMode mode, double setpoint, double arbFeedForward)
    {

    }

    /**
     * Places this motor controller in follower mode.
     *
     * @param master The master to follow.
     */
    public void follow(ExampleSmartMotorController master)
    {
    }

    /**
     * Returns the encoder distance.
     *
     * @return The current encoder distance.
     */
    public double getEncoderDistance()
    {
        return 0;
    }

    /**
     * Returns the encoder rate.
     *
     * @return The current encoder rate.
     */
    public double getEncoderRate()
    {
        return 0;
    }

    /**
     * Resets the encoder to zero distance.
     */
    public void resetEncoder()
    {

    }

    @Override
    public void set(double speed)
    {

    }

    @Override
    public double get()
    {
        return 0;
    }

    @Override
    public void setInverted(boolean isInverted)
    {

    }

    @Override
    public boolean getInverted()
    {
        return false;
    }

    @Override
    public void disable()
    {

    }

    @Override
    public void stopMotor()
    {

    }

    @Override
    public void pidWrite(double output)
    {

    }    
}
