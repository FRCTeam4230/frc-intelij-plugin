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
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;
import edu.wpi.first.wpilibj.simulation.BatterySim;
import edu.wpi.first.wpilibj.simulation.ElevatorSim;
import edu.wpi.first.wpilibj.simulation.EncoderSim;
import edu.wpi.first.wpilibj.simulation.RoboRioSim;
import edu.wpi.first.wpilibj.smartdashboard.Mechanism2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismLigament2d;
import edu.wpi.first.wpilibj.smartdashboard.MechanismRoot2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;



/** This is a sample program to demonstrate the use of elevator simulation with existing code. */
public class ${data.robotClassSimpleName} extends TimedRobot
{
    private static final int MOTOR_PORT = 0;
    private static final int ENCODER_A_CHANNEL = 0;
    private static final int ENCODER_B_CHANNEL = 1;
    private static final int JOYSTICK_PORT = 0;
    
    private static final double ELEVATOR_KP = 5.0;
    private static final double ELEVATOR_GEARING = 10.0;
    private static final double ELEVATOR_DRUM_RADIUS = Units.inchesToMeters(2.0);
    private static final double CARRIAGE_MASS = 4.0; // kg
    
    private static final double MIN_ELEVATOR_HEIGHT = Units.inchesToMeters(2);
    private static final double MAX_ELEVATOR_HEIGHT = Units.inchesToMeters(50);
    
    // distance per pulse = (distance per revolution) / (pulses per revolution)
    //  = (Pi * D) / ppr
    private static final double ELEVATOR_ENCODER_DIST_PER_PULSE =
            2.0 * Math.PI * ELEVATOR_DRUM_RADIUS / 4096;
    
    private final DCMotor elevatorGearbox = DCMotor.getVex775Pro(4);
    
    // Standard classes for controlling our elevator
    private final PIDController controller = new PIDController(ELEVATOR_KP, 0, 0);
    private final Encoder encoder = new Encoder(ENCODER_A_CHANNEL, ENCODER_B_CHANNEL);
    private final PWMSparkMax motor = new PWMSparkMax(MOTOR_PORT);
    private final Joystick joystick = new Joystick(JOYSTICK_PORT);
    
    // Simulation classes help us simulate what's going on, including gravity.
    private final ElevatorSim elevatorSim =
            new ElevatorSim(
                    elevatorGearbox,
                    ELEVATOR_GEARING,
                    CARRIAGE_MASS,
                    ELEVATOR_DRUM_RADIUS,
                    MIN_ELEVATOR_HEIGHT,
                    MAX_ELEVATOR_HEIGHT,
                    true,
                    VecBuilder.fill(0.01));
    private final EncoderSim encoderSim = new EncoderSim(encoder);
    
    // Create a Mechanism2d visualization of the elevator
    private final Mechanism2d mech2d = new Mechanism2d(20, 50);
    private final MechanismRoot2d mech2dRoot = mech2d.getRoot("Elevator Root", 10, 0);
    private final MechanismLigament2d elevatorMech2d =
            mech2dRoot.append(
                    new MechanismLigament2d(
                            "Elevator", Units.metersToInches(elevatorSim.getPositionMeters()), 90));
    
    
    @Override
    public void robotInit()
    {
        encoder.setDistancePerPulse(ELEVATOR_ENCODER_DIST_PER_PULSE);
        
        // Publish Mechanism2d to SmartDashboard
        // To view the Elevator Sim in the simulator, select Network Tables -> SmartDashboard ->
        // Elevator Sim
        SmartDashboard.putData("Elevator Sim", mech2d);
    }
    
    
    @Override
    public void simulationPeriodic()
    {
        // In this method, we update our simulation of what our elevator is doing
        // First, we set our "inputs" (voltages)
        elevatorSim.setInput(motor.get() * RobotController.getBatteryVoltage());
        
        // Next, we update it. The standard loop time is 20ms.
        elevatorSim.update(0.020);
        
        // Finally, we set our simulated encoder's readings and simulated battery voltage
        encoderSim.setDistance(elevatorSim.getPositionMeters());
        // SimBattery estimates loaded battery voltages
        RoboRioSim.setVInVoltage(
                BatterySim.calculateDefaultBatteryLoadedVoltage(elevatorSim.getCurrentDrawAmps()));
        
        // Update elevator visualization with simulated position
        elevatorMech2d.setLength(Units.metersToInches(elevatorSim.getPositionMeters()));
    }
    
    
    @Override
    public void teleopPeriodic()
    {
        if (joystick.getTrigger())
        {
            // Here, we run PID control like normal, with a constant setpoint of 30in.
            double pidOutput = controller.calculate(encoder.getDistance(), Units.inchesToMeters(30));
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
