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

import org.jetbrains.annotations.PropertyKey

class FrcMessageKey private constructor(@field:PropertyKey(resourceBundle = FrcBundle.BUNDLE_NAME) @param:PropertyKey(resourceBundle = FrcBundle.BUNDLE_NAME) val key: String,
                                        val params: Array<out Any>)
{
    companion object
    {
        @JvmStatic
        fun of(@PropertyKey(resourceBundle = FrcBundle.BUNDLE_NAME) key: String, vararg params: Any): FrcMessageKey
        {
            return FrcMessageKey(key, params)
        }
    }

    override fun toString(): String = "FrcMessageKey(key='$key', params=${params.contentToString()})"

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FrcMessageKey

        if (key != other.key) return false
        if (!params.contentEquals(other.params)) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = key.hashCode()
        result = 31 * result + params.contentHashCode()
        return result
    }
}
