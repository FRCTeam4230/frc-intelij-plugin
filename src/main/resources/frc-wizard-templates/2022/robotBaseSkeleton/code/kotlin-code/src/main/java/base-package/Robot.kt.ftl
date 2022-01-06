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

import edu.wpi.first.hal.HAL
import edu.wpi.first.wpilibj.DriverStation
import edu.wpi.first.wpilibj.RobotBase
import edu.wpi.first.wpilibj.livewindow.LiveWindow
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard

/**
 * The VM is configured to automatically run this object (which basically function as a singleton class).
 * This is written as an object rather than a class since there should only ever be a single instance, and
 * it cannot take any constructor arguments. This makes it a natural fit to be an object in Kotlin.
 *
 * If you change the name of this object or its package after creating this project, you must also update
 * the `Main.kt` file in the project. (If you use the IDE's Rename or Move refactorings when renaming the
 * object or package, it will get changed everywhere.)
 */
object [=data.robotClassSimpleName] : RobotBase() {
    @Volatile
    private var exit = false

    private fun robotInit() {}
    private fun disabled() {}
    private fun autonomous() {}
    private fun teleop() {}
    private fun test() {}

    override fun startCompetition() {
        robotInit()

        // Tell the DS that the robot is ready to be enabled
        HAL.observeUserProgramStarting()
        while (!Thread.currentThread().isInterrupted && !exit) {
            if (isDisabled) {
                DriverStation.inDisabled(true)
                disabled()
                DriverStation.inDisabled(false)
                while (isDisabled) {
                    DriverStation.waitForData()
                }
            } else if (isAutonomous) {
                DriverStation.inAutonomous(true)
                autonomous()
                DriverStation.inAutonomous(false)
                while (isAutonomousEnabled) {
                    DriverStation.waitForData()
                }
            } else if (isTest) {
                LiveWindow.setEnabled(true)
                Shuffleboard.enableActuatorWidgets()
                DriverStation.inTest(true)
                test()
                DriverStation.inTest(false)
                while (isTest && isEnabled) {
                    DriverStation.waitForData()
                }
                LiveWindow.setEnabled(false)
                Shuffleboard.disableActuatorWidgets()
            } else {
                DriverStation.inTeleop(true)
                teleop()
                DriverStation.inTeleop(false)
                while (isTeleopEnabled) {
                    DriverStation.waitForData()
                }
            }
        }
    }

    override fun endCompetition() {
        exit = true
    }
}