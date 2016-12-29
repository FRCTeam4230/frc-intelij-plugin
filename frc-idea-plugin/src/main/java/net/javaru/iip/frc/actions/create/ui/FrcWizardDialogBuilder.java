/*
 * Copyright 2015-2016 Mark Vedder
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

package net.javaru.iip.frc.actions.create.ui;

import java.awt.*;

import com.intellij.util.ui.JBUI;



public class FrcWizardDialogBuilder 
{
    private static final Dimension DEFAULT_MIN_SIZE = JBUI.size(600, 450);
    /**
     * The minimum (and initial) size of a dialog should be no bigger than the user's screen (or,
     * a percentage of the user's screen, to leave a bit of space on the sides). This prevents
     * developers from specifying a size that looks good on their monitor but won't fit on a low
     * resolution screen. Worst case, the UI may end up squished for some users, but the
     * prev/next/cancel buttons will always be visible.
     */
    private static final float SCREEN_PERCENT = 0.8f;
}
