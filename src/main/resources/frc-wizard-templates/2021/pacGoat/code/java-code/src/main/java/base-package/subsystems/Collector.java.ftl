<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Refernce: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:    https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when luanching IntelliJ IDEA -->
</#compress>
/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2019 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package ${data.basePackage}.subsystems;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.command.Subsystem;

/**
 * The Collector subsystem has one motor for the rollers, a limit switch for
 * ball detection, a piston for opening and closing the claw, and a reed switch
 * to check if the piston is open.
 */
public class Collector extends Subsystem implements AutoCloseable
{
    // Constants for some useful speeds
    public static final double FORWARD = 1;
    public static final double STOP = 0;
    public static final double REVERSE = -1;

    // Subsystem devices
    private final SpeedController rollerMotor = new Victor(6);
    private final DigitalInput ballDetector = new DigitalInput(10);
    private final DigitalInput openDetector = new DigitalInput(6);
    private final Solenoid piston = new Solenoid(1, 1);

    /**
     * Create a new collector subsystem.
     */
    public Collector()
    {
        // Put everything to the LiveWindow for testing.
        addChild("Roller Motor", (Victor) rollerMotor);
        addChild("Ball Detector", ballDetector);
        addChild("Claw Open Detector", openDetector);
        addChild("Piston", piston);
    }

    /**
     * Whether or not the robot has the ball.
     * <p>
     * NOTE: The current simulation model uses the the lower part of the claw
     * since the limit switch wasn't exported. At some point, this will be
     * updated.
     *
     * @return Whether or not the robot has the ball.
     */
    public boolean hasBall()
    {
        return ballDetector.get(); // TODO: prepend ! to reflect real robot
    }

    /**
     * Set the speed to spin the collector rollers.
     *
     * @param speed The speed to spin the rollers.
     */
    public void setSpeed(double speed)
    {
        rollerMotor.set(-speed);
    }

    /**
     * Stop the rollers from spinning.
     */
    public void stop()
    {
        rollerMotor.set(0);
    }

    /**
     * Whether or not the claw is open.
     *
     * @return Whether or not the claw is open.
     */
    public boolean isOpen()
    {
        return openDetector.get(); // TODO: prepend ! to reflect real robot
    }

    /**
     * Open the claw up (For shooting).
     */
    public void open()
    {
        piston.set(true);
    }

    /**
     * Close the claw (For collecting and driving).
     */
    @Override
    public void close()
    {
        piston.set(false);
    }

    /**
     * No default command.
     */
    @Override
    protected void initDefaultCommand()
    {
    }
}
