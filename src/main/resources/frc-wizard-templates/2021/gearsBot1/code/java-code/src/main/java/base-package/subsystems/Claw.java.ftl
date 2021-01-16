<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement,DanglingJavadoc-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--    
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:    https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when luanching IntelliJ IDEA -->
</#compress>
${data.copyright}

package ${data.basePackage}.subsystems;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.command.Subsystem;

/**
 * The claw subsystem is a simple system with a motor for opening and closing.
 * If using stronger motors, you should probably use a sensor so that the motors
 * don't stall.
 */
public class Claw extends Subsystem
{
    private final Victor motor = new Victor(7);
    private final DigitalInput contact = new DigitalInput(5);

    /**
     * Create a new claw subsystem.
     */
    public Claw()
    {
        super();

        // Let's name everything on the LiveWindow
        addChild("Motor", motor);
        addChild("Limit Switch", contact);
    }

    @Override
    public void initDefaultCommand()
    {
    }

    public void log()
    {
    }

    /**
     * Set the claw motor to move in the open direction.
     */
    public void open()
    {
        motor.set(-1);
    }

    /**
     * Set the claw motor to move in the close direction.
     */
    @Override
    public void close()
    {
        motor.set(1);
    }

    /**
     * Stops the claw motor from moving.
     */
    public void stop()
    {
        motor.set(0);
    }

    /**
     * Return true when the robot is grabbing an object hard enough to trigger
     * the limit switch.
     */
    public boolean isGrabbing()
    {
        return contact.get();
    }
}
