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

package net.javaru.iip.frc.templates;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;

import net.javaru.iip.frc.FrcIcons;



public class FrcFileTemplateGroupDescriptorFactory implements FileTemplateGroupDescriptorFactory
{
    public static final FileTemplateDescriptor COMMAND = new FileTemplateDescriptor("FRCCommand.java", FrcIcons.Components.COMMAND);
    public static final FileTemplateDescriptor COMMAND_GROUP = new FileTemplateDescriptor("FRCCommandGroup.java", FrcIcons.Components.COMMAND_GROUP);
    public static final FileTemplateDescriptor SUBSYSTEM = new FileTemplateDescriptor("FRCSubsystem.java", FrcIcons.Components.SUBSYSTEM);
    public static final FileTemplateDescriptor PID_SUBSYSTEM = new FileTemplateDescriptor("FRCPIDSubsystem.java", FrcIcons.Components.PID_SUBSYSTEM);
    public static final FileTemplateDescriptor TRIGGER = new FileTemplateDescriptor("FRCTrigger.java", FrcIcons.Components.BUTTON);
    
    public static final FileTemplateDescriptor COMMAND2 = new FileTemplateDescriptor("FRCCommand2.java", FrcIcons.Components.COMMAND);

    private static final FileTemplateGroupDescriptor FILE_TEMPLATE_GROUP_DESCRIPTOR =
        new FileTemplateGroupDescriptor("FRC", FrcIcons.FRC.FIRST_ICON_MEDIUM_16,
                                        COMMAND,
                                        COMMAND_GROUP,
                                        SUBSYSTEM,
                                        PID_SUBSYSTEM,
                                        TRIGGER
        );


    @Override
    public FileTemplateGroupDescriptor getFileTemplatesDescriptor()
    {
        return FILE_TEMPLATE_GROUP_DESCRIPTOR;
    }
}