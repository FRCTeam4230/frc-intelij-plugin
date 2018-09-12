/*
 * Copyright 2015-2018 the original author or authors
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package net.javaru.iip.frc.wpilib;

import java.util.regex.Pattern;



public class WpiLibConstants
{
    /**
     * Fully Qualified Name (FQN) of the abstract {@code RobotBase} class. 
     * {@code SampleRobot} and the abstract {@code IterativeRobotBase} both directly extend {@code RobotBase}.
     * Finally, {@code IterativeRobot} and {@code TimedRobot} extend {@code IterativeRobotBase}.
     * 
     * <pre>
     *     RobotBase (abstract)
     *     +- SampleRobot
     *     +- IterativeRobotBase (abstract)
     *        +- IterativeRobot
     *        +- TimesRobot
     * </pre>
     * 
     */
    public static final String ROBOT_BASE_FQN =             "edu.wpi.first.wpilibj.RobotBase";
    public static final String SAMPLE_ROBOT_FQN =           "edu.wpi.first.wpilibj.SampleRobot";
    public static final String ITERATIVE_ROBOT_BASE_FQN =   "edu.wpi.first.wpilibj.IterativeRobotBase";
    public static final String ITERATIVE_ROBOT_FQN =        "edu.wpi.first.wpilibj.IterativeRobot";
    public static final String TIMED_ROBOT_FQN =            "edu.wpi.first.wpilibj.TimedRobot";
    
    public static final String VERSION_CLASS_FQN = "edu.wpi.first.wpilibj.util.WPILibVersion";
    public static final String VERSION_FIELD_NAME = "Version";

    public static final Pattern EXTENDS_A_ROBOT_REGEX = Pattern.compile("extends\\s+(edu\\.wpi\\.first\\.wpilibj\\.)?(IterativeRobot|RobotBase)");
}
