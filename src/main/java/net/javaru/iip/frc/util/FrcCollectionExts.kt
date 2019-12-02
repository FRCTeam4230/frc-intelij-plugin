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
 * transformation. Input elements that cause an exception to occur during transformation
 * are dropped/ignored, other than logging a warning.
 */
inline fun <T, R : Any> Iterable<T>.mapNoException(transform: (T) -> R?): List<R>
{
    return mapNoExceptionTo(ArrayList<R>(), transform)
}



/**
 * Applies the given [transform] function to each element in the original collection
 * and appends only the items that do not cause an exception during transformation to
 * the given [destination]. Input elements that cause an exception to occur during
 * transformation are dropped/ignored, other than logging a warning.
 */
inline fun <T, R : Any, C : MutableCollection<in R>> Iterable<T>.mapNoExceptionTo(destination: C, transform: (T) -> R?): C
{
    forEach { element ->
        try
        {
            transform(element)?.let { destination.add(it) }
        }
        catch (t: Throwable)
        {
            COL_EXT_LOG.warn("[FRC] Could not transform '${element}' due to an exception: $t")
        }
    }
    return destination
}
