/*
 * Copyright 2015-2019 the original author or authors
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

package net.javaru.iip.frc.wizard;

import org.intellij.lang.annotations.Language;



public enum FrcRobotProjectType
{
    CommandBased("Command Based Robot",
                 "<html lang='en'>A robot project that allows robots to be implemented using the command based model to allow complex functionality to be developed from simpler functionality.</html>"),
    Iterative("Iterative Robot",
              "<html lang='en'>A robot project that allow robots to be implemented in an iterative manner synced to receiving driver station packets.</html>"),
    Timed("Timed Robot",
          "<html lang='en'>A robot project that allows robots to be implemented in an iterative manner synced to a timer.</html>"),
    TimedSkeleton("Rimed Skeleton (Advanced)",
                  "<html lang='en'>Skeleton (stub) code for TimeRobot.</html>"),
    
    Sample("Sample Robot",
           "<html lang='en'>A robot project used for small sample programs or for highly advanced programs with more complete control over program flow. This is <em>not</em> a good choice to use for competition, especially for the inexperienced. Use Timed Robot or Command Based Robot instead.</html>"),
    ;
    
    private final String labelName;
    
    @Language("HTML")
    private final String description;
    
    
    FrcRobotProjectType(String labelName, @Language("HTML") String description)
    {
        this.labelName = labelName;
        this.description = description;
    }
    
    
    public String getLabelName() { return labelName; }
    
    
    @Language("HTML")
    public String getDescription() { return description; }
}
