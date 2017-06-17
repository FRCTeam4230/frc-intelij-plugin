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

package net.javaru.iip.frc.i18n;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;
import com.intellij.CommonBundle;
import com.intellij.openapi.diagnostic.Logger;



/**
 * FRC Message Bundle.
 */
public class FrcMessageBundle
{
    private static final Logger LOG = Logger.getInstance(FrcMessageBundle.class);
    
    @NonNls
    private static final String BUNDLE_NAME = "messages.FrcBundle";
    private static Reference<ResourceBundle> ourBundle;


    private FrcMessageBundle() { }


    private static ResourceBundle getBundle()
    {
        ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(ourBundle);
        if (bundle == null)
        {
            bundle = ResourceBundle.getBundle(BUNDLE_NAME);
            ourBundle = new SoftReference<>(bundle);
        }
        return bundle;
    }


    @NotNull
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE_NAME) String key, @NotNull Object... params)
    {
        return CommonBundle.message(getBundle(), key, params);
    }
}
