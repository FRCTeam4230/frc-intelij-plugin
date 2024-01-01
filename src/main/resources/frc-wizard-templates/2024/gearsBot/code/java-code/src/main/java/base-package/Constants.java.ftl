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

public final class Constants
{
    public static final class DriveConstants
    {
        public static final int LEFT_MOTOR_PORT_1 = 0;
        public static final int LEFT_MOTOR_PORT_2 = 1;
        
        public static final int RIGHT_MOTOR_PORT_1 = 2;
        public static final int RIGHT_MOTOR_PORT_2 = 3;
        
        public static final int[] LEFT_ENCODER_PORTS = {0, 1};
        public static final int[] RIGHT_ENCODER_PORTS = {2, 3};
        public static final boolean LEFT_ENCODER_REVERSED = false;
        public static final boolean RIGHT_ENCODER_REVERSED = false;
        
        public static final int RANGE_FINDER_PORT = 6;
        public static final int ANALOG_GYRO_PORT = 1;
        
        public static final int ENCODER_CPR = 1024;
        public static final double WHEEL_DIAMETER_INCHES = 6;
        public static final double ENCODER_DISTANCE_PER_PULSE =
                // Assumes the encoders are directly mounted on the wheel shafts
                (WHEEL_DIAMETER_INCHES * Math.PI) / (double) ENCODER_CPR;
    }
    
    public static final class ClawConstants
    {
        public static final int MOTOR_PORT = 7;
        public static final int CONTACT_PORT = 5;
    }
    
    public static final class WristConstants
    {
        public static final int MOTOR_PORT = 6;
        
        // these pid constants are not real, and will need to be tuned
        public static final double P = 0.1;
        public static final double I = 0.0;
        public static final double D = 0.0;
        
        public static final double TOLERANCE = 2.5;
        
        public static final int POTENTIOMETER_PORT = 3;
    }
    
    public static final class ElevatorConstants
    {
        public static final int MOTOR_PORT = 5;
        public static final int POTENTIOMETER_PORT = 2;
        
        // these pid constants are not real, and will need to be tuned
        public static final double P_REAL = 4;
        public static final double I_REAL = 0.007;
        
        public static final double P_SIMULATION = 18;
        public static final double I_SIMULATION = 0.2;
        
        public static final double D = 0.0;
        
        public static final double TOLERANCE = 0.005;
    }
    
    public static final class AutoConstants
    {
        public static final double DIST_TO_BOX_1 = 0.10;
        public static final double DIST_TO_BOX_2 = 0.60;
        
        public static final double WRIST_SETPOINT = -45.0;
    }
    
    public static final class DriveStraightConstants
    {
        // these pid constants are not real, and will need to be tuned
        public static final double P = 4.0;
        public static final double I = 0.0;
        public static final double D = 0.0;
    }
    
    public static final class Positions
    {
        public static final class Pickup
        {
            public static final double WRIST_SETPOINT = -45.0;
            public static final double ELEVATOR_SETPOINT = 0.25;
        }
        
        public static final class Place
        {
            public static final double WRIST_SETPOINT = 0.0;
            public static final double ELEVATOR_SETPOINT = 0.25;
        }
        
        public static final class PrepareToPickup
        {
            public static final double WRIST_SETPOINT = 0.0;
            public static final double ELEVATOR_SETPOINT = 0.0;
        }
    }
}
