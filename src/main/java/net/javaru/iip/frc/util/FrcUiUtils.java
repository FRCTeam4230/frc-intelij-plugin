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

package net.javaru.iip.frc.util;

import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;



public final class FrcUiUtils
{
    private FrcUiUtils() {}


    /**
     * Returns the provided text inside HTML tags centering the text for use on a Swing label. 
     * For example, given the text 'My Message', this will return:
     * <pre>
     *     "&lt;html&gt;&lt;div style='text-align: center;'&gt;" + text + "&lt;/div&gt;&lt;/html&gt;"
     * </pre>
     * @param text the text to wrap
     * @return the provided text inside HTML tags centering the text
     */
    @Language("HTML")
    public static String centerLabelText(@NotNull String text)
    {
        //noinspection LanguageMismatch
        return "<html><div style='text-align: center;'>" + text + "</div></html>";
    }


    @Language("HTML")
    public static String boldLabelText(@NotNull String text)
    {
        //noinspection LanguageMismatch
        return  "<html><div style='font-weight: bold;'>" + text + "</div></html>";
    }
}
