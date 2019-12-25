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

import com.intellij.openapi.diagnostic.Logger

val COL_EXT_LOG = Logger.getInstance("#net.javaru.iip.frc.util.FrcCollectionExts")

/**
 * Returns a list containing only the results of applying the given [transform] function
 * to each element in the original collection that do not throw an exception during
 * transformation. Input elements that throw an exception to during transformation
 * are dropped/ignored, other than logging a warning. Additionally, transformation 
 * results that are `null` are also (silently) dropped/ignored.
 * 
 * @param transform the function that transforms the input [T] to the nullable output [R]
 * @param loggingAction the optional action to log the fact that an exception occurred. The default is to log a warning.
 * @param T the "input" element type to transform
 * @param R the result type of the transforming of `T`
 */
inline fun <T, R : Any> Iterable<T>.mapExceptionFreeAndNotNull(loggingAction: (T, Throwable) -> Unit = { element: T, t: Throwable -> COL_EXT_LOG.warn("[FRC] Could not transform '${element}' due to an exception: $t", t)},
                                                               transform: (T) -> R?): List<R>
{
    @Suppress("RemoveExplicitTypeArguments") // Having the <R> in the ArrayList constructor helps with clarity
    return mapExceptionFreeAndNotNullTo(ArrayList<R>(), loggingAction, transform)
}



/**
 * Applies the given [transform] function to each element in the original collection
 * and appends only the items to the given [destination] that do not cause an exception 
 * during transformation. Input elements that throw an exception during transformation 
 * are dropped/ignored, other than logging a warning. Additionally, transformation results 
 * that are `null` are also (silently) dropped/ignored.
 * 
 * @param transform the function that transforms the input [T] to the nullable output [R]
 * @param loggingAction the optional action to log the fact that an exception occurred. The default is to log a warning.
 * @param T the "input" element type to transform
 * @param R the result type of the transforming of `T`
 */
inline fun <T, R : Any, C : MutableCollection<in R>> Iterable<T>.mapExceptionFreeAndNotNullTo(destination: C,
                                                                                              loggingAction: (T, Throwable) -> Unit = {element: T, t: Throwable -> COL_EXT_LOG.warn("[FRC] Could not transform '${element}' due to an exception: $t", t)},
                                                                                              transform: (T) -> R?): C
{
    forEach { element ->
        try
        {
            transform(element)?.let { destination.add(it) }
        }
        catch (t: Throwable)
        {
            loggingAction.invoke(element, t)
        }
    }
    return destination
}

/**
 * Converts a collection to a comma delimited string. Null values are not allowed.
 * 
 * @param withSpaces if true (the default), a space will appear after each comma: `one, two, three, four`; 
 *                   if false, no spaces will be present: `one,two,three,four`.
 * @param toStringFunction the function/lambda to use to convert collection elements to a String value. By default, the 
 *                         element's `toString()` method is used
 */
@JvmOverloads
fun <T: Any> Collection<T>.toCommaDelimitedString(withSpaces: Boolean = true, toStringFunction: (T) -> String = {it.toString()}): String
{
    val result = this.map { toStringFunction.invoke(it) }.toTypedArray().contentToString().removePrefix("[").removeSuffix("]")
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