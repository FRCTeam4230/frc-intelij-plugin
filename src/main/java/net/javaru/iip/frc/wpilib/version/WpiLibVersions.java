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

package net.javaru.iip.frc.wpilib.version;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.diagnostic.Logger;



public class WpiLibVersions
{
    private static final Logger LOG = Logger.getInstance(WpiLibVersions.class);

    private static final WpiLibVersion WPILIB_2018_VERSION_CHECK = WpiLibVersionImpl.parse("2017.9999.0");
    
    private static final WpiLibVersion WPILIB_GRADLE_CHECK = WpiLibVersionImpl.parse("2019.0.0.alpha-1");
    
    public static boolean is2018OrLater(@NotNull WpiLibVersion wpiLibVersion) { return wpiLibVersion.isNewerThan(WPILIB_2018_VERSION_CHECK); }
    
    public static boolean usesGradle(@NotNull WpiLibVersion wpiLibVersion) { return wpiLibVersion.isSameOrNewerThan(WPILIB_GRADLE_CHECK); }
    
    public static boolean usesAnt(@NotNull WpiLibVersion wpiLibVersion) { return wpiLibVersion.isOlderThan(WPILIB_GRADLE_CHECK); }
    
}
