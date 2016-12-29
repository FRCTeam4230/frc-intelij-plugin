/*
 * Copyright 2015 Mark Vedder
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package net.javaru.iip.frc;

import javax.swing.*;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;



public final class FrcIcons 
{
    private static final Logger LOG = Logger.getInstance(FrcIcons.class);
    
    
    
    /** FIRST Icon sized 60 x 42. */
    public static final Icon FIRST_ICON_60x42 = loadIcon("/icons/first/FIRST_icon_60x42.png"); // 60x42
    
    /** FIRST Icon for use with Wizard Panels. */
    public static final Icon FIRST_ICON_WIZARD_PANELS = FIRST_ICON_60x42;
    
    
    /** FIRST Icon sized 10 x 10. */
    public static final Icon FIRST_ICON_EXTRA_SMALL_10 = loadIcon("/icons/first/FIRST_icon_10x10.png"); // 10x10
    
    /** FIRST Icon sized 11 x 11. */
    public static final Icon FIRST_ICON_EXTRA_SMALL_11 = loadIcon("/icons/first/FIRST_icon_11x11.png"); // 11x11
    
    /** FIRST Icon sized 12 x 12. */
    public static final Icon FIRST_ICON_EXTRA_SMALL_12 = loadIcon("/icons/first/FIRST_icon_12x12.png"); // 12x12
    
    /** FIRST Icon sized 13 x 13. */
    public static final Icon FIRST_ICON_SMALL_13 = loadIcon("/icons/first/FIRST_icon_13x13.png"); // 13x13
    
    /** FIRST Icon sized 13 x 13. Elevated to the top of the 'box' to allow clarity when overlays are added.. */
    public static final Icon FIRST_ICON_SMALL_13_ELEVATED = loadIcon("/icons/first/FIRST_icon_13x13_elevated.png"); // 13x13

    /** FIRST Icon sized 14 x 14. */
    public static final Icon FIRST_ICON_MEDIUM_SMALL_14 = loadIcon("/icons/first/FIRST_icon_14x14.png"); // 14x14
    
    /** FIRST Icon sized 15 x 15. */
    public static final Icon FIRST_ICON_MEDIUM_SMALL_15 = loadIcon("/icons/first/FIRST_icon_15x15.png"); // 15x15
    
    /** FIRST Icon sized 16 x 16. */
    public static final Icon FIRST_ICON_MEDIUM_16 = loadIcon("/icons/first/FIRST_icon_16x16.png"); // 16x16
    
    /** FIRST Icon sized 24 x 24. */
    public static final Icon FIRST_ICON_MEDIUM_LARGE_24 = loadIcon("/icons/first/FIRST_icon_24x24.png"); // 24x24


    /** A 16x16 Icon for Command classes. */
    public static final Icon COMPONENTS_COMMAND_WPI = loadIcon("/icons/wpi/Command.png"); // 16x16
    
    /** A 16x16 Icon for Command classes. */
    public static final Icon COMPONENTS_COMMAND = loadIcon("/icons/components/Command-16.png"); // 16x16
    
    /** A 16x16 Icon for Command Group classes. */
    public static final Icon COMPONENTS_COMMAND_GROUP_WPI = loadIcon("/icons/wpi/CommandGroup.png"); // 16x16
    
    /** A 16x16 Icon for Command Group classes. */
    public static final Icon COMPONENTS_COMMAND_GROUP = loadIcon("/icons/components/Command-Group-16.png"); // 16x16
    
    /** A 16x16 Icon for Trigger (i.e. Button) classes. */
    public static final Icon COMPONENTS_BUTTON_WPI = loadIcon("/icons/wpi/Button.png"); // 16x16
    
    /** A 16x16 Icon for Trigger (i.e. Button) classes. */
    public static final Icon COMPONENTS_BUTTON = loadIcon("/icons/components/Button-16.png"); // 16x16
    
    /** A 16x16 Icon for Subsystem classes. */
    public static final Icon COMPONENTS_SUBSYSTEM_WPI = loadIcon("/icons/wpi/Subsystem.png"); // 16x16
    
    /** A 16x16 Icon for Subsystem classes. */
    public static final Icon COMPONENTS_SUBSYSTEM = loadIcon("/icons/components/Subsystem.png"); // 16x16
    
    /** A 16x16 Icon for PID Subsystem classes. */
    public static final Icon COMPONENTS_PID_SUBSYSTEM_WPI = loadIcon("/icons/wpi/PIDSubsystem.png"); // 16x16

    /** A 16x16 Icon for PID Subsystem classes. */
    public static final Icon COMPONENTS_PID_SUBSYSTEM = loadIcon("/icons/components/PID-Subsystem-16.png"); // 16x16


    private static Icon loadIcon(String path)
    {
        try
        {
            return IconLoader.getIcon(path, FrcIcons.class);
        }
        catch (Throwable throwable)
        {
            //IntelliJ IDEA will still log an independent error in the Events window, but by loading a replacement icon, it will
            //prevent things (such as actions) from completely breaking because an icon was not loaded.
            LOG.error("[FRC] An exception occurred when loading the icon from '" + path + "'. Cause Summary: " + throwable.toString());
            return IconLoader.getIcon("/icons/InvalidIconPlaceholder-16.png", FrcIcons.class);
        }
    }
    
    private FrcIcons() { }
}
