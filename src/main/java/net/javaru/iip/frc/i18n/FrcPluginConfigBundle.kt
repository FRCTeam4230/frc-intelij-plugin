/*
 * Copyright 2015-2021 the original author or authors.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *     
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package net.javaru.iip.frc.i18n

import org.jetbrains.annotations.Contract
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey

/**
 * FRC Plugin Config Message Bundle.
 */
object FrcPluginConfigBundle: FrcBundleBase()
{
    const val bundleName = "i18n.FrcPluginConfigBundle"
    override val ourBundleName: String
        get() = bundleName

    @JvmStatic
    fun message(@PropertyKey(resourceBundle = bundleName) key: String, vararg params: Any): String = messageImpl(key, *params)

    @JvmStatic
    fun message(messageKey: FrcMessageKey): String = messageImpl(messageKey)

    /**
     * Returns the message for the provided `FrcMessageKey`, which wil be null if the provided key is null.
     */
    @Contract("null->null, !null->!null")
    @JvmStatic
    fun messageNullable(messageKey: FrcMessageKey?): String? = messageNullableImpl(messageKey)

    @JvmStatic
    fun messageOrDefault(@PropertyKey(resourceBundle = bundleName) key: String, defaultValue: String?, vararg params: Any): String = messageOrDefaultImpl(key, defaultValue, *params)

    @JvmStatic
    fun messageOrDefault(messageKey: FrcMessageKey, defaultValue: String?): String = messageOrDefaultImpl(messageKey, defaultValue)

    @JvmStatic
    fun messageOrNull(@PropertyKey(resourceBundle = bundleName) key: String, vararg params: Any): String? = messageOrNullImpl(key, *params)

    @JvmStatic
    fun messageOrNull(messageKey: FrcMessageKey): String? = messageOrNullImpl(messageKey)

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
    fun messageLabelCentered(@PropertyKey(resourceBundle = bundleName) key: String, vararg params: Any): String = messageLabelCenteredImpl(key, *params)

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
    fun messageLabelCentered(messageKey: FrcMessageKey): String = messageLabelCenteredImpl(messageKey)

    @JvmStatic
    fun actionText(@NonNls actionId: String): String = actionTextImpl(actionId)

    @JvmStatic
    fun actionDescription(@NonNls actionId: String): String = actionDescriptionImpl(actionId)
}