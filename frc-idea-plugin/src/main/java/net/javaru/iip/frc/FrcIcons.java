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

import com.intellij.openapi.util.IconLoader;



public final class FrcIcons 
{
    /** A 13 x 13 FIRST Icon. */
    public static final Icon FIRST_ICON_SMALL = loadIcon("/frc/icons/FIRST_icon_13x13.png"); // 13x13
    /** A 13 x 13 FIRST Icon. Elevated to the top of the 'box' to allow clarity when overlays are added.. */
    public static final Icon FIRST_ICON_SMALL_ELEVATED = loadIcon("/frc/icons/FIRST_icon_13x13_elevated.png"); // 13x13
    /** A 16 x 16 FIRST Icon. */
    public static final Icon FIRST_ICON_MEDIUM = loadIcon("/frc/icons/FIRST_icon_16x16.png"); // 16x16
    /** A 24 x 24 FIRST Icon. */
    public static final Icon FIRST_ICON_MEDIUM_LARGE = loadIcon("/frc/icons/FIRST_icon_24x24.png"); // 24x24

    private static Icon loadIcon(String path)
    {
        return IconLoader.getIcon(path, FrcIcons.class);
    }
    
    private FrcIcons() { }
}
