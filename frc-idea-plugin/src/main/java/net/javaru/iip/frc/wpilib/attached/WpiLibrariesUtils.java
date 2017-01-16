/*
 * Copyright 2015-2017 Mark Vedder
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

package net.javaru.iip.frc.wpilib.attached;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.project.Project;

import static net.javaru.iip.frc.util.FindClassUtils.isLibraryPresent;



public class WpiLibrariesUtils
{

    @Contract("null -> false")
    public static boolean isWpilibPresent(@Nullable Project project)
    {
        return isLibraryPresent(project, WpilibConstants.ROBOT_BASE_FQN) ||
               isLibraryPresent(project, WpilibConstants.ITERATIVE_ROBOT_FQN);
    }

//    @Contract("null -> false")
//    public static boolean isWpilibSourcePresent(@Nullable Project project)
//    {
//        return isLibrarySourcePresent(project, WpilibConstants.ROBOT_BASE_FQN) ||
//               isLibrarySourcePresent(project, WpilibConstants.ITERATIVE_ROBOT_FQN);
//    }


    @Contract("null -> false")
    public static boolean isCsCorePresent(@Nullable Project project)
    {
        return isLibraryPresent(project, "edu.wpi.cscore.VideoCamera") ||
               isLibraryPresent(project, "edu.wpi.cscore.CameraServerJNI");
    }
    
//    @Contract("null -> false")
//    public static boolean isCsCoreSourcePresent(@Nullable Project project)
//    {
//        return isLibrarySourcePresent(project, "edu.wpi.cscore.VideoCamera") ||
//               isLibrarySourcePresent(project, "edu.wpi.cscore.CameraServerJNI");
//    }

    @Contract("null -> false")
    public static boolean isNetworkTablesPresent(@Nullable Project project)
    {
        return isLibraryPresent(project, "edu.wpi.first.wpilibj.networktables.NetworkTable") ||
               isLibraryPresent(project, "edu.wpi.first.wpilibj.tables.ITable");
    }
    
//    @Contract("null -> false")
//    public static boolean isNetworkTablesSourcePresent(@Nullable Project project)
//    {
//        return isLibrarySourcePresent(project, "edu.wpi.first.wpilibj.networktables.NetworkTable") ||
//               isLibrarySourcePresent(project, "edu.wpi.first.wpilibj.tables.ITable");
//    }
    

    @Contract("null -> false")
    public static boolean isOpenCvPresent(@Nullable Project project)
    {
        return isLibraryPresent(project, "org.opencv.core.Core") ||
               isLibraryPresent(project, "org.opencv.video.Video") ||
               isLibraryPresent(project, "org.opencv.videoio.VideoCapture") ||
               isLibraryPresent(project, "org.opencv.objdetect.Objdetect");
    }
    
//    @Contract("null -> false")
//    public static boolean isOpenCvSourcePresent(@Nullable Project project)
//    {
//        return isLibrarySourcePresent(project, "org.opencv.core.Core") ||
//               isLibrarySourcePresent(project, "org.opencv.video.Video") ||
//               isLibrarySourcePresent(project, "org.opencv.videoio.VideoCapture") ||
//               isLibrarySourcePresent(project, "org.opencv.objdetect.Objdetect");
//    }


    public static boolean areAllPresent(@Nullable Project project)
    {
        return isWpilibPresent(project) && isNetworkTablesPresent(project) && isOpenCvPresent(project) && isCsCorePresent(project);
    }
}
