/*
 * Copyright 2015-2020 the original author or authors
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

package net.javaru.iip.frc.templates;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;

import icons.FrcIcons;



public class FrcFileTemplateGroupDescriptorFactory implements FileTemplateGroupDescriptorFactory
{
    // COMMAND BASED FRAMEWORK v1
    public static final FileTemplateDescriptor COMMAND1 = new FileTemplateDescriptor("FRC v1 Command.java", FrcIcons.Components.COMMAND);
    public static final FileTemplateDescriptor COMMAND_GROUP1 = new FileTemplateDescriptor("FRC v1 CommandGroup.java", FrcIcons.Components.COMMAND_GROUP);
    public static final FileTemplateDescriptor SUBSYSTEM1 = new FileTemplateDescriptor("FRC v1 Subsystem.java", FrcIcons.Components.SUBSYSTEM);
    public static final FileTemplateDescriptor PID_SUBSYSTEM1 = new FileTemplateDescriptor("FRC v1 PIDSubsystem.java", FrcIcons.Components.PID_SUBSYSTEM);
    public static final FileTemplateDescriptor TRIGGER1 = new FileTemplateDescriptor("FRC v1 Trigger.java", FrcIcons.Components.BUTTON);
    
    // COMMAND BASED FRAMEWORK v2
    public static final FileTemplateDescriptor COMMAND2 = new FileTemplateDescriptor("FRC v2 Command.java", FrcIcons.Components.COMMAND);
    public static final FileTemplateDescriptor COMMAND2_KOT = new FileTemplateDescriptor("FRC v2 Command.kt", FrcIcons.Components.COMMAND);
    public static final FileTemplateDescriptor COMMAND_GROUP2 = new FileTemplateDescriptor("FRC v2 CommandGroup.java", FrcIcons.Components.COMMAND_GROUP);
    public static final FileTemplateDescriptor SUBSYSTEM2 = new FileTemplateDescriptor("FRC v2 Subsystem.java", FrcIcons.Components.SUBSYSTEM);
    public static final FileTemplateDescriptor SUBSYSTEM2_KOT = new FileTemplateDescriptor("FRC v2 Subsystem.kt", FrcIcons.Components.SUBSYSTEM);
    public static final FileTemplateDescriptor TRAPEZOID_PROFILED_SUBSYSTEM2 = new FileTemplateDescriptor("FRC v2 Trapezoid Profile Subsystem.java", FrcIcons.Components.SUBSYSTEM);
    public static final FileTemplateDescriptor PID_SUBSYSTEM2 = new FileTemplateDescriptor("FRC v2 PIDSubsystem.java", FrcIcons.Components.PID_SUBSYSTEM);
    public static final FileTemplateDescriptor PROFILED_PID_SUBSYSTEM2 = new FileTemplateDescriptor("FRC v2 ProfiledPIDSubsystem.java", FrcIcons.Components.PID_SUBSYSTEM);
    public static final FileTemplateDescriptor TRIGGER2 = new FileTemplateDescriptor("FRC v2 Trigger.java", FrcIcons.Components.BUTTON);
    public static final FileTemplateDescriptor TRIGGER2_KOT = new FileTemplateDescriptor("FRC v2 Trigger.kt", FrcIcons.Components.BUTTON);
    
    // GENERAL FRC/WPI
    /** Legacy Trigger… the action for it is not included in the popup at this time. We keep it around for reference at this time. */
    public static final FileTemplateDescriptor TRIGGER = new FileTemplateDescriptor("FRC Trigger.java", FrcIcons.Components.BUTTON);
    
    private static final FileTemplateGroupDescriptor FILE_TEMPLATE_GROUP_DESCRIPTOR =
        new FileTemplateGroupDescriptor("FRC", FrcIcons.FRC.FIRST_ICON_MEDIUM_16,
                                        COMMAND1,
                                        COMMAND_GROUP1,
                                        COMMAND_GROUP2,
                                        SUBSYSTEM1,
                                        PID_SUBSYSTEM1,
                                        TRIGGER1,
                                        COMMAND2,
                                        COMMAND2_KOT,
                                        SUBSYSTEM2,
                                        SUBSYSTEM2_KOT,
                                        TRAPEZOID_PROFILED_SUBSYSTEM2,
                                        PID_SUBSYSTEM2,
                                        PROFILED_PID_SUBSYSTEM2,
                                        TRIGGER2,
                                        TRIGGER2_KOT 
                                        );


    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor()
    {
        return FILE_TEMPLATE_GROUP_DESCRIPTOR;
    }
}