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

package net.javaru.iip.frc.ui.notify;

import javax.swing.*;

import com.intellij.icons.AllIcons;



public final class FrcNotifications
{
    public static final String FRC_GENERAL_NOTIFICATION_GROUP = "FRC General Notifications";
    public static final String FRC_ACTIONABLE_NOTIFICATION_GROUP = "FRC Actionable Notifications";

    public static final Icon IconInfo = AllIcons.General.BalloonInformation;
    public static final Icon IconWarn = AllIcons.General.BalloonWarning;
    public static final Icon IconError = AllIcons.General.BalloonError;
    
    public static final String Title = "FRC";
    
    
    private FrcNotifications() { }
}
