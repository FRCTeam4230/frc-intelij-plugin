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


/**
 * Converts a collection to a comma delimited string.
 * @param withSpaces if true (the default), a space will appear after each comma: `one, two, three, four`; if false, no spaces will be present: `one,two,three,four`
 */
@JvmOverloads
fun Collection<*>.toCommaDelimitedString(withSpaces: Boolean = true): String
{
    val result = this.toTypedArray().contentToString().removePrefix("[").removeSuffix("]")
    return if (withSpaces) result else result.replace(" ", "")
}

/**
 * Converts a comma delimited String to a Mutable List of Strings, properly trimming the values. 
 */
fun String.commaDelimitedToList(): MutableList<String>
{
    val list = this.split(',').map { it.trim() }.toMutableList()
    return if (list.size == 1 && list[0].isBlank()) mutableListOf() else list
}