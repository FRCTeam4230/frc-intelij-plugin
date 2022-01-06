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

/**
 * Educational robot base class. Do NOT use for competitions.
 * This is a simple robot used for teaching purposes.
 */
abstract class EducationalRobot : RobotBase() {
    open fun robotInit() {}
    abstract fun run()

    open fun disabled() {}
    open fun autonomous() = run()
    open fun teleop() = run()
    open fun test() = run()

    @Volatile
    private var exit = false
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
                DriverStation.inTest(true)
                test()
                DriverStation.inTest(false)
                while (isTest && isEnabled) {
                    DriverStation.waitForData()
                }
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