/*
 * Copyright 2015-2022 the original author or authors.
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

@file:Suppress("unused")

package net.javaru.iip.frc.util

import org.jetbrains.annotations.Contract
import java.util.*


enum class EOL(
    /**
     * The end of line character(s).
     */
    val eol: String
              )
{
    UNIX("\n"),
    WINDOWS("\r\n"),
    MAC_OLD("\r"),
    SYSTEM(System.lineSeparator())
}

@Deprecated("Use tryQuietly", ReplaceWith("tryQuietly(block)") /* We keep around for discovery purposes*/ )
fun trySafely( block: () -> Unit ) = tryQuietly  { block() }

/**
 * Runs the block of code, catching and ignoring any exceptions.
 * 
 * @see letSafely
 * @see tryQuietlyIf
 */
fun tryQuietly( block: () -> Unit )
{
    // @formatter:off
    try { block() } catch (ignore: Throwable) { }
    // @formatter:on
}

/**
 * Runs the block of code if the `predicate` evaluates to `true`, catching and ignoring any exceptions, both
 * when evaluating the `predicate` and the `block`.
 *
 * @see tryQuietly
 * @see letSafely
 */
fun tryQuietlyIf(predicate: ()-> Boolean, block: () -> Unit )
{
    // @formatter:off
    try { if (predicate()) block() } catch (ignore: Throwable) { }
    // @formatter:on
}

@Deprecated("Use letSafely", ReplaceWith("letSafely(block)") /* We keep around for discovery purposes*/ )
inline fun <T, R> T?.trySafely(block: (T) -> R): R? = this.letSafely(block)

/**
 * A safe implementation of the standard [let] scope function that only runs the block if the receiver is non-null, and 
 * returns `null` in the event an exception occurs in the execution of the provided block. For example: 
 * `val uuid: UUID? = uuidString.letSafely { UUID.fromString(it) }`
 * will return `null` in the event `uuidString` is null or is not a valid UUID.
 * 
 * @return the result of the block, or `null` if the receiver is `null` or an exception occurs during the execution of the block.
 * 
 * @see tryQuietly
 */
inline fun <T, R> T?.letSafely(block: (T) -> R): R?
{
    return try
    {
       this?.let(block)
    }
    catch (ignore: Throwable)
    {
        null
    }
}

/**
 * Appends all on null arguments to the given [Appendable].
 */
fun <T : Appendable> T.appendIfNonNull(vararg value: CharSequence?): T
{
    for (item in value)
        if (item != null) this.append(item)
    return this
}

/**
 * Appends all non-null arguments to the given StringBuilder.
 */
fun StringBuilder.appendIfNonNull(vararg value: Any?): StringBuilder
{
    for (item in value)
        if (item != null) this.append(item)
    return this
}

/**
 * Appends all non-null arguments to the given StringBuilder.
 */
fun StringBuilder.appendIfNonNull(vararg value: CharSequence?): StringBuilder
{
    for (item in value)
        if (item != null) this.append(item)
    return this
}

/**
 * Appends all non-null arguments to the given StringBuilder.
 */
fun StringBuilder.appendIfNonNull(vararg value: String?): StringBuilder
{
    for (item in value)
        if (item != null) this.append(item)
    return this
}

/**
 * Appends all non-null and non-blank arguments to the given [Appendable].
 */
fun <T : Appendable> T.appendIfNotBlank(vararg value: CharSequence?): T
{
    for (item in value)
        if (!item.isNullOrBlank()) this.append(item)
    return this
}

/**
 * Appends all non-null and non-blank arguments to the given StringBuilder.
 */
fun StringBuilder.appendIfNotBlank(vararg value: CharSequence?): StringBuilder
{
    for (item in value)
        if (!item.isNullOrBlank()) this.append(item)
    return this
}

/**
 * Appends all non-null and non-blank arguments to the given StringBuilder.
 */
fun StringBuilder.appendIfNotBlank(vararg value: String?): StringBuilder
{
    for (item in value)
        if (!item.isNullOrBlank()) this.append(item)
    return this
}

/**
 * Appends all non-null and non-empty arguments to the given [Appendable].
 */
fun <T : Appendable> T.appendIfNotEmpty(vararg value: CharSequence?): T
{
    for (item in value)
        if (!item.isNullOrEmpty()) this.append(item)
    return this
}

/**
 * Appends all non-null and non-empty arguments to the given StringBuilder.
 */
fun StringBuilder.appendIfNotEmpty(vararg value: CharSequence?): StringBuilder
{
    for (item in value)
        if (!item.isNullOrEmpty()) this.append(item)
    return this
}

/**
 * Appends all non-null and non-empty arguments to the given StringBuilder.
 */
fun StringBuilder.appendIfNotEmpty(vararg value: String?): StringBuilder
{
    for (item in value)
        if (!item.isNullOrEmpty()) this.append(item)
    return this
}

/**
 * Appends all arguments to the given [Appendable] if the provided condition evaluates to `true`.
 * If the condition is `false`, none of arguments are appended.
 */
fun <T : Appendable> T.appendIf(vararg value: CharSequence?, condition: () -> Boolean): T
{
    if (condition())
        for (item in value)
            this.append(item)
    return this
}

/**
 * Appends all arguments to the given StringBuilder if the provided condition evaluates to `true`.
 * If the condition is `false`, none of arguments are appended.
 */
fun StringBuilder.appendIf(vararg value: CharSequence?, condition: () -> Boolean): StringBuilder
{
    if (condition())
        for (item in value)
            this.append(item)
    return this
}


/**
 * Appends all arguments to the given StringBuilder if the provided condition evaluates to `true`.
 * If the condition is `false`, none of arguments are appended.
 */
fun StringBuilder.appendIf(vararg value: String?, condition: () -> Boolean): StringBuilder
{
    if (condition())
        for (item in value)
            this.append(item)
    return this
}

/**
 * Appends all arguments to the given StringBuilder if the provided condition evaluates to `true`.
 * If the condition is `false`, none of arguments are appended.
 */
fun StringBuilder.appendIf(vararg value: Any?, condition: () -> Boolean): StringBuilder
{
    if (condition())
        for (item in value)
            this.append(item)
    return this
}

/**
 * Appends each argument to the given [Appendable] if the provided condition evaluates to `true` for
 * the individual item.
 */
fun <T : Appendable> T.appendItemIf(vararg value: CharSequence?, condition: (CharSequence?) -> Boolean): T
{
    for (item in value)
        if (condition(item))
            this.append(item)
    return this
}

/**
 * Appends each argument to the given StringBuilder if the provided condition evaluates to `true` for
 * the individual item.
 */
fun StringBuilder.appendItemIf(vararg value: CharSequence?, condition: (CharSequence?) -> Boolean): StringBuilder
{
    for (item in value)
        if (condition(item))
            this.append(item)
    return this
}

/**
 * Appends each argument to the given StringBuilder if the provided condition evaluates to `true` for
 * the individual item.
 */
fun StringBuilder.appendItemIf(vararg value: String?, condition: (String?) -> Boolean): StringBuilder
{
    for (item in value)
        if (condition(item))
            this.append(item)
    return this
}

/**
 * Appends each argument to the given StringBuilder if the provided condition evaluates to `true` for
 * the individual item.
 */
fun StringBuilder.appendItemIf(vararg value: Any?, condition: (Any?) -> Boolean): StringBuilder
{
    for (item in value)
        if (condition(item))
            this.append(item)
    return this
}


/**
 * A null safe implementation of Kotlin's built-in [kotlin.text.lines] method.
 * The method splits this char sequence to a list of lines delimited by any of the following
 * character sequences: CRLF, LF or CR.
 *
 * @return List of Strings representing the lines in the CharSequence. In the event the CharSequence
 * is null, an empty list is returned.
 */
fun CharSequence?.textToLines(): List<String> = this?.lines() ?: ArrayList()


/**
 * Normalizes a CharSequence to a desired line ending, Unix LF by default.
 *
 * @param eol the EOL to change the text to.
 */
@JvmOverloads
fun CharSequence.eolTo(eol: EOL = EOL.UNIX): String = this.textToLines().linesToText(eol, false)

/**
 * Normalizes a CharSequence to a desired line ending, Unix LF by default.
 *
 * @param eol the EOL to change the text to.
 */
@Contract("null,_ -> null, !null,_ -> !null")
@JvmOverloads
fun CharSequence?.eolToOrNull(eol: EOL = EOL.UNIX): String? = this?.eolTo(eol)

/** Convenience method for de-capitalization since kotlin built-in one was deprecated. This implements the suggested replacement   */
fun String.decapitalize2(): String = this.replaceFirstChar { it.lowercase(Locale.getDefault()) }

/** Convenience method for capitalization since kotlin built-in one was deprecated. This implements the suggested replacement  */
fun String.capitalize2(): String = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

/**
 * Inserts the supplied [value] before the last occurrence of [delimiter]. If the string does not contain the delimiter,
 * the `value` added to the end of the string. Some examples for a `delimiter` of `.` and an insert value of "-baz":
 *n
 * - "foo.txt" ➜ "foo-baz.txt"
 * - "foo.bar.txt" ➜ "foo.bar-baz.txt"
 * - "foo" ➜ "foo-baz"
 * - ".foo" ➜ "-baz.foo"
 */
fun String.insertBeforeLast(delimiter: Char, value: String): String
{
    val prefix = this.substringBeforeLast(delimiter)
    val suffix = this.substringAfterLast(delimiter, "").let {
        if (it.isEmpty()) "" else ".$it"
    }
    return "$prefix$value$suffix"
}