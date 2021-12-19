/*
 * Copyright 2015-2021 the original author or authors.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package icons;

import javax.swing.*;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;

// "Working with Icons and Images" in the SDK DevGuide: http://www.jetbrains.org/intellij/sdk/docs/reference_guide/work_with_icons_and_images.html
// Icon design guidelines: https://jetbrains.design/intellij/principles/icons/
// SVG images are supported since v2018.2
//
// Also see IntelliJ Icon Generator (A third party tool for generating 'standard' IntelliJ circle and square icons)
//     https://bjansen.github.io/intellij-icon-generator/
// Web Page showing all the built-in IDE Icons
//     https://jetbrains.design/intellij/resources/icons_list/


public final class FrcIcons
{
    private static final Logger LOG = Logger.getInstance(FrcIcons.class);

    private static final Icon NOT_FOUND_ICON = loadNotFoundIcon();

    public static class FRC
    {
        /** FIRST Logo sized 60 x 42. */
        public static final Icon FIRST_LOGO_60x42 = loadIcon("/icons/first/FRIST_logo_60x42.png"); // 60x42
        
        /** FIRST Icon for use with Wizard Panels. */
        public static final Icon FIRST_ICON_WIZARD_PANELS = FIRST_LOGO_60x42;
        
        /** FIRST Logo sized 60 x 42. */
        public static final Icon FIRST_LOGO_70x49 = loadIcon("/icons/first/FRIST_logo_70x49.png"); // 70x49
        
        /** FIRST Icon for use with Dialog Window. */
        public static final Icon FIRST_ICON_DIALOG_WINDOW = FIRST_LOGO_70x49;
        
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
        
        /** FIRST Icon sized 32 x 32. */
        public static final Icon FIRST_ICON_LARGE_32 = loadIcon("/icons/first/FIRST_icon_32x32.png"); // 32x32
        
    }

    public static class RioLog
    {
        public static final Icon RIOLOG = loadIcon("/icons/riolog/riolog.png"); // 16x16
        public static final Icon RIOLOG_x2 = loadIcon("/icons/riolog/riolog@2x.png"); // 32x32
        public static final Icon RIOLOG_SSH_CONSOLE = loadIcon("/icons/riolog/letter-S.png");
        public static final Icon RIOLOG_UDP_CONSOLE = loadIcon("/icons/riolog/letter-N.png");
    }

    public static class Components
    {
        /** A 16x16 Icon for Command classes. */
        public static final Icon COMMAND_WPI = loadIcon("/icons/wpi/Command.png"); // 16x16

        /** A 16x16 Icon for Command classes. */
        public static final Icon COMMAND = loadIcon("/icons/components/Command-16.png"); // 16x16

        /** A 16x16 Icon for Command Group classes. */
        public static final Icon COMMAND_GROUP_WPI = loadIcon("/icons/wpi/CommandGroup.png"); // 16x16

        /** A 16x16 Icon for Command Group classes. */
        public static final Icon COMMAND_GROUP = loadIcon("/icons/components/Command-Group-16.png"); // 16x16

        /** A 16x16 Icon for Trigger (i.e. a simple Button) classes. */
        public static final Icon BUTTON_WPI = loadIcon("/icons/wpi/Button.png"); // 16x16

        /** A 16x16 Icon for Trigger (i.e. a simple Button) classes. */
        public static final Icon BUTTON = loadIcon("/icons/components/Button-Joystick-16.png"); // 16x16

        /** A 16x16 Icon for Subsystem classes. */
        public static final Icon SUBSYSTEM_WPI = loadIcon("/icons/wpi/Subsystem.png"); // 16x16

        /** A 16x16 Icon for Subsystem classes. */
        public static final Icon SUBSYSTEM = loadIcon("/icons/components/Subsystem-16.png"); // 16x16

        /** A 16x16 Icon for PID Subsystem classes. */
        public static final Icon PID_SUBSYSTEM_WPI = loadIcon("/icons/wpi/PIDSubsystem.png"); // 16x16

        /** A 16x16 Icon for PID Subsystem classes. */
        public static final Icon PID_SUBSYSTEM = loadIcon("/icons/components/PID-Subsystem-16.png"); // 16x16

    }
    
    public static class Tech
    {
        public static class Git
        {
            /** A 16x16 PNG Icon for Git. */
            public static final String GIT_PNG_PATH = "/icons/technologies/git.png";
            /** A 16x16 PNG Icon for Git. */
            public static final Icon GIT_PNG = loadIcon(GIT_PNG_PATH); // 16x16
            /** A 16x16 SVG Icon for Git. */
            public static final String GIT_SVG_PATH = "/icons/technologies/git.svg";
            /** A 16x16 SVG Icon for Git. */
            public static final Icon GIT_SVG = loadIcon(GIT_SVG_PATH); // 16x16
        }
    
        public static class Kotlin
        {
            /** A 16x16 PNG Icon for Kotlin. */
            public static final String KOTLIN_PNG_PATH = "/icons/technologies/kotlin.png";
            /** A 16x16 PNG Icon for Kotlin. */
            public static final Icon KOTLIN_PNG = loadIcon(KOTLIN_PNG_PATH); // 16x16
            /** A 16x16 SVG Icon for Kotlin. */
            public static final String KOTLIN_SVG_PATH = "/icons/technologies/kotlin.svg";
            /** A 16x16 SVG Icon for Kotlin Script. */
            public static final String KOTLIN_SCRIPT_SVG_PATH = KOTLIN_SVG_PATH;
            /** A 16x16 SVG Icon for Kotlin Script. */
            public static final Icon KOTLIN_SCRIPT_SVG = loadIcon(KOTLIN_SCRIPT_SVG_PATH); // 16x16
            /** A 16x16 SVG Icon for Kotlin launch configuration. */
            public static final String KOTLIN_LAUNCH_CONFIGURATION_SVG_PATH = KOTLIN_SVG_PATH;
            /** A 16x16 SVG Icon for Kotlin launch configuration. */
            public static final Icon KOTLIN_LAUNCH_CONFIGURATION_SVG = loadIcon(KOTLIN_LAUNCH_CONFIGURATION_SVG_PATH); // 16x16
            /** A 16x16 SVG Icon for Kotlin Gradle Script. */
            public static final String KOTLIN_GRADLE_SCRIPT_SVG_PATH = KOTLIN_SVG_PATH;
            /** A 16x16 SVG Icon for Kotlin Gradle Script. */
            public static final Icon KOTLIN_GRADLE_SCRIPT_SVG = loadIcon(KOTLIN_GRADLE_SCRIPT_SVG_PATH); // 16x16
            /** A 16x16 SVG Icon for Kotlin. */
            public static final Icon KOTLIN_SVG = loadIcon(KOTLIN_SVG_PATH); // 16x16
            /** A 13x13 PNG Icon for Kotlin. */
            public static final String KOTLIN_13_PNG_PATH = "/icons/technologies/kotlin13.png";
            /** A 13x13 PNG Icon for Kotlin. */
            public static final Icon KOTLIN_13_PNG = loadIcon(KOTLIN_13_PNG_PATH); // 13x13
            /** A 13x13 SVG Icon for Kotlin. */
            public static final String KOTLIN_13_SVG_PATH = "/icons/technologies/kotlin13.svg";
            /** A 13x13 SVG Icon for Kotlin. */
            public static final Icon KOTLIN_13_SVG = loadIcon(KOTLIN_13_SVG_PATH); // 13x13
            /** A 16x16 SVG Icon for Kotlin File. */
            public static final String KOTLIN_FILE_SVG_PATH = "/icons/technologies/kotlin_file.svg";
            /** A 16x16 SVG Icon for Kotlin FIle. */
            public static final Icon KOTLIN_FILE_SVG = loadIcon(KOTLIN_FILE_SVG_PATH); // 16x16
        }
    }
    
    public static class WpiLib
    {
        /** An SVG file of the new (2019+) WpiLib 'official' icon.  */
        public static final Icon WPI_LIB = loadIcon("/icons/wpi/wpilib.svg");
        
        /** A WpiLib ico Icon file with 16x16 through 256x256 icons of the new (2019+) WpiLib 'official' icon.  */
        public static final Icon WPI_LIB_ICO_16_THRU_256 = loadIcon("/icons/wpi/wpilib-256.ico");
        
        /** A 16x16 Icon of the new (2019+) WpiLib 'official' icon.  */
        public static final Icon WPI_LIB_16 = loadIcon("/icons/wpi/wpilib-16.png");
        
        /** A 24x24 Icon of the new (2019+) WpiLib 'official' icon.  */
        public static final Icon WPI_LIB_24 = loadIcon("/icons/wpi/wpilib-24.png");
        
        /** A 32x32 Icon of the new (2019+) WpiLib 'official' icon.  */
        public static final Icon WPI_LIB_32 = loadIcon("/icons/wpi/wpilib-32.png");
        
        /** A 40x40 Icon of the new (2019+) WpiLib 'official' icon.  */
        public static final Icon WPI_LIB_40 = loadIcon("/icons/wpi/wpilib-40.png");
        
        /** A 48x48 Icon of the new (2019+) WpiLib 'official' icon.  */
        public static final Icon WPI_LIB_48 = loadIcon("/icons/wpi/wpilib-48.png");
        
        /** A 64x64 Icon of the new (2019+) WpiLib 'official' icon.  */
        public static final Icon WPI_LIB_64 = loadIcon("/icons/wpi/wpilib-64.png");
        
        /** A 96x96 Icon of the new (2019+) WpiLib 'official' icon.  */
        public static final Icon WPI_LIB_96 = loadIcon("/icons/wpi/wpilib-96.png");
        
        /** A 128x128 Icon of the new (2019+) WpiLib 'official' icon.  */
        public static final Icon WPI_LIB_128 = loadIcon("/icons/wpi/wpilib-128.png");
        
        /** A 192x192 Icon of the new (2019+) WpiLib 'official' icon.  */
        public static final Icon WPI_LIB_192 = loadIcon("/icons/wpi/wpilib-192.png");
        
        /** A 256x256 Icon of the new (2019+) WpiLib 'official' icon.  */
        public static final Icon WPI_LIB_256 = loadIcon("/icons/wpi/wpilib-256.png");
        
        /** A 16x16 Icon of the legacy (i.e. pre 2019) Wpi 'official' icon.  */
        public static final Icon WPI_LIB_LEGACY_16 = loadIcon("/icons/wpi/wpi-16.png");
        
        /** A 16x16 ico file Icon of the legacy (i.e. pre 2019) Wpi 'official' icon.  */
        public static final Icon WPI_LIB_LEGACY_16_ICO = loadIcon("/icons/wpi/wpi-16.ico");
    }
    
    public static class FileAndDirTypes
    {
        public static final Icon Idea = loadIcon("/icons/fileAndDirTypes/idea-ce_16.png");
        public static final Icon IdeaDir = AllIcons.Nodes.IdeaProject;
        public static final Icon License = loadIcon("/icons/fileAndDirTypes/license.png");
        public static final Icon RunDir = AllIcons.RunConfigurations.Compound;
        public static final Icon VendordepsDir = AllIcons.Nodes.PpLibFolder; //AllIcons.General.ProjectStructure
        public static final Icon VSCode = loadIcon("/icons/fileAndDirTypes/vscode-16.png");
        public static final Icon VsCodeDir = loadIcon("/icons/fileAndDirTypes/vscode-dir.png");
        public static final Icon WpiLibDir = loadIcon("/icons/fileAndDirTypes/wpilib-dir.png");
    }
    /**
     * Safely uses the IDEA {@link IconLoader} to load an icon from the classpath. In the event
     * the icon can not be found, or an exception occurs during the load, the exception is logged
     * and a stand-in Icon is returned to prevent nullability issues.
     * @param path the path to the icon. MUST START WITH A LEADING SLASH
     *
     * @return the icon
     */
    public static Icon loadIcon(String path)
    {
        try
        {
            // The path to the icon passed in as argument to IconLoader.getIcon() must start with leading slash
            return IconLoader.getIcon(path, FrcIcons.class);
        }
        catch (Throwable throwable)
        {
            // IntelliJ IDEA will still log an independent error in the Events window, but by using a replacement icon, 
            // it will prevent things (such as actions) from completely breaking because an icon was not loaded. We
            // use what is basically a blank icon as it seems like a good substitute.
            LOG.warn("[FRC] An exception occurred when loading the icon from '" + path + "'; Cause Summary: " + throwable.toString());
            return NOT_FOUND_ICON;
        }
    }

    
    private static Icon loadNotFoundIcon()
    {
        try
        {
            // The path to the icon passed in as argument to IconLoader.getIcon() must start with leading slash
            return IconLoader.getIcon("/icons/iconNotFoundIcon.svg", FrcIcons.class);
        }
        catch (Throwable throwable)
        {
            
            LOG.warn("[FRC] An exception occurred when loading the Not Found stand-in icon Cause Summary: " + throwable.toString());
            return AllIcons.Nodes.EmptyNode;
        }
    }

    private FrcIcons() { }
}
