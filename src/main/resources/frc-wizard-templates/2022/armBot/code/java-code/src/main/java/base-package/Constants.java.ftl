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
 * The Constants class provides a convenient place for teams to hold robot-wide numerical or boolean
 * constants. This class should not be used for any other purpose. All constants should be declared
 * globally (i.e. public static). Do not put anything functional in this class.
 *
 * <p>It is advised to statically import this class (or one of its inner classes) wherever the
 * constants are needed, to reduce verbosity.
 */
public final class Constants
{
    public static final class DriveConstants
    {
        public static final int LEFT_MOTOR_1_PORT = 0;
        public static final int LEFT_MOTOR_2_PORT = 1;
        public static final int RIGHT_MOTOR_1_PORT = 2;
        public static final int RIGHT_MOTOR_2_PORT = 3;
        
        public static final int[] LEFT_ENCODER_PORTS = new int[] {0, 1};
        public static final int[] RIGHT_ENCODER_PORTS = new int[] {2, 3};
        public static final boolean LEFT_ENCODER_REVERSED = false;
        public static final boolean RIGHT_ENCODER_REVERSED = true;
        
        public static final int ENCODER_CPR = 1024;
        public static final double WHEEL_DIAMETER_INCHES = 6;
        public static final double ENCODER_DISTANCE_PER_PULSE =
                // Assumes the encoders are directly mounted on the wheel shafts
                (WHEEL_DIAMETER_INCHES * Math.PI) / (double) ENCODER_CPR;
    }
    
    public static final class ArmConstants
    {
        public static final int MOTOR_PORT = 4;
        
        public static final double P = 1;
        
        // These are fake gains; in actuality these must be determined individually for each robot
        public static final double S_VOLTS = 1;
        public static final double COS_VOLTS = 1;
        public static final double V_VOLT_SECOND_PER_RAD = 0.5;
        public static final double A_VOLT_SECOND_SQUARED_PER_RAD = 0.1;
        
        public static final double MAX_VELOCITY_RAD_PER_SECOND = 3;
        public static final double MAX_ACCELERATION_RAD_PER_SEC_SQUARED = 10;
        
        public static final int[] ENCODER_PORTS = new int[] {4, 5};
        public static final int ENCODER_PPR = 256;
        public static final double ENCODER_DISTANCE_PER_PULSE = 2.0 * Math.PI / ENCODER_PPR;
        
        // The offset of the arm from the horizontal in its neutral position,
        // measured from the horizontal
        public static final double ARM_OFFSET_RADS = 0.5;
    }
    
    public static final class AutoConstants
    {
        public static final double AUTO_TIMEOUT_SECONDS = 12;
        public static final double AUTO_SHOOT_TIME_SECONDS = 7;
    }
    
    public static final class OIConstants
    {
        public static final int DRIVER_CONTROLLER_PORT = 0;
    }
}
