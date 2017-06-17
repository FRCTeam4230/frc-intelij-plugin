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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import net.javaru.iip.frc.i18n.FrcMessageBundle;



/**
 * @deprecated use {@link net.javaru.iip.frc.i18n.FrcMessageBundle} instead
 */
@Deprecated
public class FrcBundle
{
    private static final String BUNDLE_NAME = "messages.FrcBundle";
    
    /**
     * @deprecated use {@link net.javaru.iip.frc.i18n.FrcMessageBundle#message(String, Object...)} instead
     */
    @Deprecated
    @NotNull
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE_NAME) String key, @NotNull Object... params)
    {
        return FrcMessageBundle.message(key, params);
    }

}
