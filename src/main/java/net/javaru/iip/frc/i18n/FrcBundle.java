/*
 * Copyright 2015-2019 the original author or authors
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

package net.javaru.iip.frc.i18n;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;
import com.intellij.BundleBase;

import net.javaru.iip.frc.util.FrcUiUtils;



/**
 * FRC Message Bundle.
 */
public class FrcBundle
{
    //private static final Logger LOG = Logger.getInstance(FrcBundle.class);
    
    @NonNls
    static final String BUNDLE_NAME = "i18n.FrcBundle";
    private static Reference<ResourceBundle> ourBundle;


    private FrcBundle() { }


    @SuppressWarnings("Duplicates")
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
    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE_NAME) String key, 
                                 @NotNull Object... params)
    {
        return BundleBase.message(getBundle(), key, params);
    }

    
    @NotNull
    public static String message(@NotNull FrcMessageKey messageKey)
    {
        return message(messageKey.getKey(), messageKey.getParams());
    }

    @Nullable
    @Contract("_,!null,_ -> !null")
    public static String messageOrDefault(@NotNull @PropertyKey(resourceBundle = BUNDLE_NAME) String key,
                                          @Nullable final String defaultValue,
                                          @NotNull Object... params)
    {
        return BundleBase.messageOrDefault(getBundle(), key, defaultValue, params);
    }
    
    
    public static String messageOrDefault(@NotNull FrcMessageKey messageKey, @Nullable final String defaultValue)
    {
        return BundleBase.messageOrDefault(getBundle(), messageKey.getKey(), defaultValue, messageKey.getParams());
    }

    @Nullable
    public static String messageOrNull(@NotNull @PropertyKey(resourceBundle = BUNDLE_NAME) String key,
                                       @NotNull Object... params)
    {
        final String value = messageOrDefault(key, key, params);
        return (key.equals(value)) ? null :value;
    }
    
    
    @Nullable
    public static String messageOrNull(@NotNull FrcMessageKey messageKey)
    {
        String value = messageOrDefault(messageKey.getKey(), messageKey.getKey(), messageKey.getParams());
        return (messageKey.getKey().equals(value)) ? null : value;
    
    }

    /**
     * Gets a resource bundled message and returns it in inside HTML tags centering the text for use on a Swing label. 
     * For example, given the key to the message 'My Message', this will return:
     * <pre>
     *     "&lt;html&gt;&lt;div style='text-align: center;'&gt;" + text + "&lt;/div&gt;&lt;/html&gt;"
     * </pre>
     * @param key the resource bundle key
     * @param params any parameters used within the message
     * @return the localized message inside HTML tags centering the text
     */
    @NotNull
    public static String messageLabelCentered(@NotNull @PropertyKey(resourceBundle = BUNDLE_NAME) String key, @NotNull Object... params)
    {
        return FrcUiUtils.centerLabelText(message(key, params));
    }
}
