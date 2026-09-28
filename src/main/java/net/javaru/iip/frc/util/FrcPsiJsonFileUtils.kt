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

package net.javaru.iip.frc.util

import com.intellij.json.JsonUtil
import com.intellij.json.psi.JsonElement
import com.intellij.json.psi.JsonFile
import com.intellij.json.psi.JsonNumberLiteral
import com.intellij.json.psi.JsonObject
import com.intellij.json.psi.JsonStringLiteral
import com.intellij.openapi.application.runReadActionBlocking
import com.intellij.util.ObjectUtils


fun <T : JsonElement?> JsonFile?.getPropertyValueOfType(name: String, jsonElementClazz: Class<T>): T?
{
    return runReadActionBlocking {
        JsonUtil.getTopLevelObject(this).getPropertyValueOfType(name, jsonElementClazz)
    }
}

fun <T : JsonElement?> JsonObject?.getPropertyValueOfType(name: String, jsonElementClazz: Class<T>): T?
{
    return runReadActionBlocking {
        val property = this?.findProperty(name)
        if (property == null)
            null
        else
            ObjectUtils.tryCast(property.value, jsonElementClazz)
    }
}

fun JsonFile?.getIntPropertyValue(name: String): Int?
{
    return runReadActionBlocking {
        val jsonLiteral = this.getPropertyValueOfType(name, JsonNumberLiteral::class.java)
        jsonLiteral?.value?.toInt()
    }
}

fun JsonObject?.getIntPropertyValue(name: String): Int?
{
    return runReadActionBlocking {
        val jsonLiteral = this.getPropertyValueOfType(name, JsonNumberLiteral::class.java)
        jsonLiteral?.value?.toInt()
    }
}

fun JsonFile?.getStringPropertyValue(name: String): String?
{
    return runReadActionBlocking {
        val jsonLiteral = this.getPropertyValueOfType(name, JsonStringLiteral::class.java)
        jsonLiteral?.value
    }
}

fun JsonObject?.getStringPropertyValue(name: String): String?
{
    return runReadActionBlocking {
        val jsonLiteral = this.getPropertyValueOfType(name, JsonStringLiteral::class.java)
        jsonLiteral?.value
    }
}