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

package ${data.basePackage};

/**
 * This class is the glue that binds the controls on the physical Operator Interface (OI)
 * to the commands and command groups that allow control of the robot.
 */
public class OI
{
    //// CREATING BUTTONS
    //      One type of button is a joystick button which is any button on a
    //      joystick.
    //      You create one by telling it which joystick it's on and which button
    //      number it is.
    //      Joystick stick = new Joystick(port);
    //      Button button = new JoystickButton(stick, buttonNumber);
    //
    //      There are a few additional built in buttons you can use. Additionally,
    //      by subclassing Button you can create custom triggers and bind those to
    //      commands the same as any other Button.
    //
    //// TRIGGERING COMMANDS WITH BUTTONS
    //      Once you have a button, it's trivial to bind it to a button in one of
    //      three ways:
    //
    //      Start the command when the button is pressed and let it run the command
    //      until it is finished as determined by it's isFinished method.
    //      button.whenPressed(new ExampleCommand());
    //
    //      Run the command while the button is being held down and interrupt it once
    //      the button is released.
    //      button.whileHeld(new ExampleCommand());
    //
    //      Start the command when the button is released and let it run the command
    //      until it is finished as determined by it's isFinished method.
    //      button.whenReleased(new ExampleCommand());
}