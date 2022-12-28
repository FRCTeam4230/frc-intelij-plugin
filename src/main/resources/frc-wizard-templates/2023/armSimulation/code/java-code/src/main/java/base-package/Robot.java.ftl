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

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.simulation.BatterySim;
import edu.wpi.first.wpilibj.simulation.EncoderSim;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismRoot2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.util.Color;
import edu.wpi.first.wpilibj.util.Color8Bit;



/** This is a sample program to demonstrate the use of arm simulation with existing code. */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    private static final int MOTOR_PORT = 0;
    private static final int ENCODER_A_CHANNEL = 0;
    private static final int ENCODER_B_CHANNEL = 1;
    private static final int JOYSTICK_PORT = 0;
    
    public static final String ARM_POSITION_KEY = "ArmPosition";
    public static final String ARM_P_KEY = "ArmP";
    
    // The P gain for the PID controller that drives this arm.
    private static double kArmKp = 50.0;
    
    private static double armPositionDeg = 75.0;
    
    // distance per pulse = (angle per revolution) / (pulses per revolution)
    //  = (2 * PI rads) / (4096 pulses)
    private static final double ARM_ENCODER_DIST_PER_PULSE = 2.0 * Math.PI / 4096;
    
    // The arm gearbox represents a gearbox containing two Vex 775pro motors.
    private final DCMotor armGearbox = DCMotor.getVex775Pro(2);
    
    // Standard classes for controlling our arm
    private final PIDController controller = new PIDController(kArmKp, 0, 0);
    private final Encoder encoder = new Encoder(ENCODER_A_CHANNEL, ENCODER_B_CHANNEL);
    private final PWMSparkMax motor = new PWMSparkMax(MOTOR_PORT);
    private final Joystick joystick = new Joystick(JOYSTICK_PORT);
    
    // Simulation classes help us simulate what's going on, including gravity.
    private static final double armReduction = 600;
    private static final double armMass = 5.0; // Kilograms
    private static final double armLength = Units.inchesToMeters(30);
    // This arm sim represents an arm that can travel from -75 degrees (rotated down front)
    // to 255 degrees (rotated down in the back).
    private final SingleJointedArmSim armSim =
            new SingleJointedArmSim(
                    armGearbox,
                    armReduction,
                    SingleJointedArmSim.estimateMOI(armLength, armMass),
                    armLength,
                    Units.degreesToRadians(-75),
                    Units.degreesToRadians(255),
                    armMass,
                    true,
                    VecBuilder.fill(ARM_ENCODER_DIST_PER_PULSE) // Add noise with a std-dev of 1 tick
            );
    private final EncoderSim encoderSim = new EncoderSim(encoder);
    
    // Create a Mechanism2d display of an Arm with a fixed ArmTower and moving Arm.
    private final Mechanism2d mech2d = new Mechanism2d(60, 60);
    private final MechanismRoot2d armPivot = mech2d.getRoot("ArmPivot", 30, 30);
    private final MechanismLigament2d armTower =
            armPivot.append(new MechanismLigament2d("ArmTower", 30, -90));
    private final MechanismLigament2d arm =
            armPivot.append(
                    new MechanismLigament2d(
                            "Arm",
                            30,
                            Units.radiansToDegrees(armSim.getAngleRads()),
                            6,
                            new Color8Bit(Color.kYellow)));
    
    
    @Override
    public void robotInit()
    {
        encoder.setDistancePerPulse(ARM_ENCODER_DIST_PER_PULSE);
        
        // Put Mechanism 2d to SmartDashboard
        SmartDashboard.putData("Arm Sim", mech2d);
        armTower.setColor(new Color8Bit(Color.kBlue));
        
        // Set the Arm position setpoint and P constant to Preferences if the keys don't already exist
        if (!Preferences.containsKey(ARM_POSITION_KEY))
        {
            Preferences.setDouble(ARM_POSITION_KEY, armPositionDeg);
        }
        if (!Preferences.containsKey(ARM_P_KEY))
        {
            Preferences.setDouble(ARM_P_KEY, kArmKp);
        }
    }
    
    
    @Override
    public void simulationPeriodic()
    {
        // In this method, we update our simulation of what our arm is doing
        // First, we set our "inputs" (voltages)
        armSim.setInput(motor.get() * RobotController.getBatteryVoltage());
        
        // Next, we update it. The standard loop time is 20ms.
        armSim.update(0.020);
        
        // Finally, we set our simulated encoder's readings and simulated battery voltage
        encoderSim.setDistance(armSim.getAngleRads());
        // SimBattery estimates loaded battery voltages
        RoboRioSim.setVInVoltage(
                BatterySim.calculateDefaultBatteryLoadedVoltage(armSim.getCurrentDrawAmps()));
        
        // Update the Mechanism Arm angle based on the simulated arm angle
        arm.setAngle(Units.radiansToDegrees(armSim.getAngleRads()));
    }
    
    
    @Override
    public void teleopInit()
    {
        // Read Preferences for Arm setpoint and P on entering Teleop
        armPositionDeg = Preferences.getDouble(ARM_POSITION_KEY, armPositionDeg);
        if (kArmKp != Preferences.getDouble(ARM_P_KEY, kArmKp))
        {
            kArmKp = Preferences.getDouble(ARM_P_KEY, kArmKp);
            controller.setP(kArmKp);
        }
    }
    
    
    @Override
    public void teleopPeriodic()
    {
        if (joystick.getTrigger())
        {
            // Here, we run PID control like normal, with a constant setpoint of 75 degrees.
            var pidOutput =
                    controller.calculate(encoder.getDistance(), Units.degreesToRadians(armPositionDeg));
            motor.setVoltage(pidOutput);
        }
        else
        {
            // Otherwise, we disable the motor.
            motor.set(0.0);
        }
    }
    
    
    @Override
    public void disabledInit()
    {
        // This just makes sure that our simulation code knows that the motor's off.
        motor.set(0.0);
    }
}
