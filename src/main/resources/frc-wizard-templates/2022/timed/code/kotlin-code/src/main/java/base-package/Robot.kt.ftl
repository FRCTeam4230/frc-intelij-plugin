<#ftl output_format="plainText" encoding="UTF-8">
<#--noinspection WrongPackageStatement-->
<#compress>
<#-- @ftlvariable name="data" type="net.javaru.iip.frc.wizard.FrcProjectWizardData" -->
<#--
    IMPORTANT: This tempolate uses alternate square bracket interpolation syntax
               For example:
                    [=data.robotClassSimpleName]
               rather than:
                    ${data.robotClassSimpleName}
               so as to not clash with Kotlin string templates syntax
               The option to use that can't be set in the template, but has to be set as an
               option on the Configuration object in the code. Note that this only affects interpolation
               syntax, and *NOT* Tag syntax. So we will still use `<#if isSuchAndSuch>` and not `[#if isSuchAndSuch]`.
               Tag syntax can be changed if desired, but we are not.
    Template Language Reference: https://freemarker.apache.org/docs/ref.html
    Template Author's Guide:     https://freemarker.apache.org/docs/dgui.html
-->
<#--  To DEBUG templates, set system property 'frc.freemarker.debug' to true when launching the testing instance of IntelliJ IDEA -->
</#compress>
package [=data.basePackage]

import edu.wpi.first.wpilibj.TimedRobot
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard
import [=data.basePackage].[=data.robotClassSimpleName].AutoMode.*

/**
 * The VM is configured to automatically run this object (which basically function as a singleton class),
 * and to call the functions corresponding to each mode, as described in the TimedRobot documentation.
 *
 * If you change the name of this object or the package after creating this project, you must also update
 * the `Main.kt` file in the project. (If you use the IDE's Rename or Move refactorings when renaming the
 * object or package, it will get changed everywhere.)
 */
object [=data.robotClassSimpleName] : TimedRobot()
{
    private var autoSelected = DEFAULT_AUTO
    private val chooser = SendableChooser<AutoMode>()

    private enum class AutoMode(val description: String, val isDefault: Boolean = false)
    {
        DEFAULT_AUTO("Default Auto Mode", isDefault = true),
        CUSTOM_AUTO_1("Custom Auto Mode 1"),
        CUSTOM_AUTO_2("Custom Auto Mode 2"),
    }

    /**
     * This method is run when the robot is first started up and should be used for any
     * initialization code.
     */
    override fun robotInit()
    {
        initAutoChooser()
    }

    private fun initAutoChooser()
    {
        AutoMode.values().forEach {
            if (it.isDefault)
                chooser.setDefaultOption(it.description, it)
            else
                chooser.addOption(it.description, it)
        }
        SmartDashboard.putData("Auto choices", chooser)
    }

    /**
     * This method is called every robot packet, no matter the mode. Use this for items like
     * diagnostics that you want ran during disabled, autonomous, teleoperated and test.
     *
     *
     * This runs after the mode specific periodic methods, but before LiveWindow and
     * SmartDashboard integrated updating.
     */
    override fun robotPeriodic() {}

    override fun autonomousInit() {
        /* This autonomousInit function (along with the initAutoChooser function) shows how to select
        between different autonomous modes using the dashboard. The sendable chooser code works with the
        SmartDashboard. You can add additional auto modes by adding additional options to the AutoMode enum
        and then adding them to the `when` statement in the [autonomousPeriodic] function.

        If you prefer the LabVIEW Dashboard, remove all the chooser code and uncomment the alternate line: */
        // autoSelected = AutoMode.valueOf(SmartDashboard.getString("Auto Selector", DEFAULT_AUTO.name))
        autoSelected = chooser.selected
        println("Auto selected: ${autoSelected.description}")
    }

    /** This method is called periodically during autonomous.  */
    override fun autonomousPeriodic()
    {
        when (autoSelected) {
            CUSTOM_AUTO_1 -> autoMode1()
            CUSTOM_AUTO_2 -> autoMode2()
            DEFAULT_AUTO -> defaultAutoMode()
        }
    }

    private fun defaultAutoMode()
    {
        TODO("Write default auto mode")
    }

    private fun autoMode1()
    {
        TODO("Write custom auto mode 1")
    }

    private fun autoMode2()
    {
        TODO("Write custom auto mode 2")
    }

    /** This method is called once when teleop is enabled.  */
    override fun teleopInit() {}

    /** This method is called periodically during operator control.  */
    override fun teleopPeriodic() {}

    /** This method is called once when the robot is disabled.  */
    override fun disabledInit() {}

    /** This method is called periodically when disabled.  */
    override fun disabledPeriodic() {}

    /** This method is called once when test mode is enabled.  */
    override fun testInit() {}

    /** This method is called periodically during test mode.  */
    override fun testPeriodic() {}
}