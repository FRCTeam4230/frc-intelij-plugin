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

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.util.SystemInfoRt
import com.intellij.reference.SoftReference
import com.intellij.util.text.OrdinalFormat
import net.javaru.iip.frc.util.asserted
import net.javaru.iip.frc.util.centerLabelText
import org.jetbrains.annotations.NonNls
import java.lang.ref.Reference
import java.text.MessageFormat
import java.util.*

private const val MNEMONIC = 0x1B.toChar()
/** A marker character that is added to a all resolved messages if either `idea.l10n` or `frc.l10n` system property is set to true. */
private const val L10N_MARKER = "🔅"

// Heavily based on com.intellij.BundleBase which is an internal API class
abstract class FrcBundleBase
{
    protected val logger = logger<FrcBundleBase>()

    protected abstract val ourBundleName: String

    private val showLocalizedMessages = java.lang.Boolean.getBoolean("idea.l10n") || java.lang.Boolean.getBoolean("frc.l10n")
    private val suffixes = arrayOf("</body></html>", "</html>")

    private var ourBundle: Reference<ResourceBundle?>? = null

    protected val bundle: ResourceBundle
        get()
        {
            var bundle = SoftReference.dereference(ourBundle)
            if (bundle == null)
            {
                bundle = ResourceBundle.getBundle(ourBundleName)
                if (bundle == null)
                {
                    logger.asserted("[FRC] Could not find resource bundle '${ourBundleName}'")
                    bundle = object : ResourceBundle()
                    {
                        override fun handleGetObject(key: String): Any = "!${ourBundleName}!!$key!"

                        override fun getKeys(): Enumeration<String>
                        {
                            return object : Enumeration<String>
                            {
                                override fun hasMoreElements(): Boolean = false
                                override fun nextElement(): String = "<no keys>"
                            }
                        }
                    }
                }
                ourBundle = java.lang.ref.SoftReference(bundle)
            }
            return bundle
        }

    protected fun messageImpl(key: String, vararg params: Any): String = messageOrDefaultImpl(key, null, *params)

    protected fun messageImpl(messageKey: FrcMessageKey): String = messageImpl(messageKey.key, *messageKey.params)

    protected fun messageNullableImpl(messageKey: FrcMessageKey?): String? = if (messageKey == null) null else messageImpl(messageKey)

    protected fun messageOrDefaultImpl(
        key: String,
        defaultValue: String?,
        vararg params: Any
                                    ): String
    {
        var resourceFound = true
        var value: String?
        try
        {
            value = bundle.getString(key)
        } catch (e: MissingResourceException)
        {
            resourceFound = false
            value = useDefaultValue(key, defaultValue)
        }
        val result = postprocessValue(value!!, *params)

        return if (showLocalizedMessages && resourceFound) appendLocalizationMarker(result) else result
    }

    protected fun messageOrDefaultImpl(messageKey: FrcMessageKey, defaultValue: String?): String = messageOrDefaultImpl(messageKey.key, defaultValue, *messageKey.params)

    protected fun messageOrNullImpl(key: String, vararg params: Any): String?
    {
        val value = messageOrDefaultImpl(key, key, *params)
        return if (key == value) null else value
    }

    protected fun messageOrNullImpl(messageKey: FrcMessageKey): String? = messageOrNullImpl(messageKey.key, *messageKey.params)

    protected fun messageLabelCenteredImpl(key: String, vararg params: Any): String = centerLabelText(messageImpl(key, *params))

    protected fun messageLabelCenteredImpl(messageKey: FrcMessageKey): String = messageLabelCenteredImpl(messageKey.key, *messageKey.params)

    protected fun actionTextImpl(@NonNls actionId: String): String = messageImpl("action.$actionId.text")

    protected fun actionDescriptionImpl(@NonNls actionId: String): String = messageImpl("action.$actionId.description")

    private fun useDefaultValue(key: String, defaultValue: String?): String
    {
        if (defaultValue != null)
        {
            return defaultValue
        }

        logger.asserted("[FRC] '$key' is not found in $bundle")
        return "!$key!"
    }

    private fun postprocessValue(value: String, vararg params: Any): String
    {
        var ourValue = value
        ourValue = replaceMnemonicAmpersand(ourValue)
        if (params.isNotEmpty() && ourValue.indexOf('{') >= 0)
        {
            val locale = bundle.locale
            ourValue = try
            {
                val format = locale?.let { MessageFormat(ourValue, it) } ?: MessageFormat(ourValue)
                OrdinalFormat.apply(format)
                format.format(params)
            } catch (e: IllegalArgumentException)
            {
                "!invalid format: `$ourValue`!"
            }
        }
        return ourValue
    }


    private fun replaceMnemonicAmpersand(value: String): String
    {
        if (!value.contains('&'))
        {
            return value
        }
        val builder = StringBuilder()
        val macMnemonic = value.contains("&&")
        var i = 0
        while (i < value.length)
        {
            val c = value[i]
            if (c == '\\')
            {
                if (i < value.length - 1 && value[i + 1] == '&')
                {
                    builder.append('&')
                    i++
                } else
                {
                    builder.append(c)
                }
            } else if (c == '&')
            {
                if (i < value.length - 1 && value[i + 1] == '&')
                {
                    if (SystemInfoRt.isMac)
                    {
                        builder.append(MNEMONIC)
                    }
                    i++
                } else if (!SystemInfoRt.isMac || !macMnemonic)
                {
                    builder.append(MNEMONIC)
                }
            } else
            {
                builder.append(c)
            }
            i++
        }
        return builder.toString()
    }

    private fun appendLocalizationMarker(result: String): String
    {
        for (suffix in suffixes)
        {
            if (result.endsWith(suffix)) return result.substring(0, result.length - suffix.length) + L10N_MARKER + suffix
        }
        return result + L10N_MARKER
    }
}