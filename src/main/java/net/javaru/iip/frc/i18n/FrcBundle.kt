/*
 * Copyright 2015-2020 the original author or authors
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
package net.javaru.iip.frc.i18n

import com.intellij.BundleBase
import com.intellij.reference.SoftReference
import net.javaru.iip.frc.util.centerLabelText
import org.jetbrains.annotations.Contract
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.lang.ref.Reference
import java.util.*

/**
 * FRC Message Bundle.
 */
object FrcBundle
{
    @NonNls
    const val BUNDLE_NAME = "i18n.FrcBundle"

    private var ourBundle: Reference<ResourceBundle?>? = null

    private val bundle: ResourceBundle?
        get()
        {
            var bundle = SoftReference.dereference(ourBundle)
            if (bundle == null)
            {
                bundle = ResourceBundle.getBundle(BUNDLE_NAME)
                ourBundle = java.lang.ref.SoftReference(bundle)
            }
            return bundle
        }

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String, vararg params: Any): String = BundleBase.message(bundle!!, key, *params)

    @JvmStatic
    fun message(messageKey: FrcMessageKey): String = message(messageKey.key, *messageKey.params)

    @JvmStatic
    @Contract("_,!null,_ -> !null")
    fun messageOrDefault(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String,
                         defaultValue: String?,
                         vararg params: Any): String?
    {
        return BundleBase.messageOrDefault(bundle, key, defaultValue, *params)
    }


    @JvmStatic
    @Contract("_,!null -> !null")
    fun messageOrDefault(messageKey: FrcMessageKey, defaultValue: String?): String? = messageOrDefault(messageKey.key, defaultValue, *messageKey.params)

    @JvmStatic
    fun messageOrNull(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String,
                      vararg params: Any): String?
    {
        val value = messageOrDefault(key, key, *params)
        return if (key == value) null else value
    }

    @JvmStatic
    fun messageOrNull(messageKey: FrcMessageKey): String? = messageOrNull(messageKey.key, *messageKey.params)



    /**
     * Gets a resource bundled message and returns it in inside HTML tags centering the text for use on a Swing label.
     * For example, given the key to the message 'My Message', this will return:
     * ```
     * <html><div style='text-align: center;'>My Message</div></html>
     * ```
     *
     * @param key the resource bundle key
     * @param params any parameters used within the message
     * @return the localized message inside HTML tags centering the text
     */
    @JvmStatic
    fun messageLabelCentered(@PropertyKey(resourceBundle = BUNDLE_NAME) key: String,
                             vararg params: Any): String
    {
        return centerLabelText(message(key, *params))
    }

    /**
     * Gets a resource bundled message and returns it in inside HTML tags centering the text for use on a Swing label.
     * For example, given the key to the message 'My Message', this will return:
     * ```
     * <html><div style='text-align: center;'>My Message</div></html>
     * ```
     *
     * @param messageKey the FrcMessageKey
     * @return the localized message inside HTML tags centering the text
     */
    @JvmStatic
    fun messageLabelCentered(messageKey: FrcMessageKey): String = messageLabelCentered(messageKey.key, *messageKey.params)

}
