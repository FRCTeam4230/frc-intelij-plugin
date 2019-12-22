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
 * Appends all on null arguments to the given [Appendable].
 */
fun <T : Appendable> T.appendIfNonNull(vararg value: CharSequence?): T
{
    for (item in value)
        if (item != null) this.append(item)
    return this
}

/**
 * Appends all non null arguments to the given StringBuilder.
 */
fun StringBuilder.appendIfNonNull(vararg value: Any?): StringBuilder
{
    for (item in value)
        if (item != null) this.append(item)
    return this;
}

/**
 * Appends all non null arguments to the given StringBuilder.
 */
fun StringBuilder.appendIfNonNull(vararg value: CharSequence?): StringBuilder
{
    for (item in value)
        if (item != null) this.append(item)
    return this;
}

/**
 * Appends all non null arguments to the given StringBuilder.
 */
fun StringBuilder.appendIfNonNull(vararg value: String?): StringBuilder
{
    for (item in value)
        if (item != null) this.append(item)
    return this;
}

/**
 * Appends all non null and non blank arguments to the given [Appendable].
 */
fun <T : Appendable> T.appendIfNotBlank(vararg value: CharSequence?): T
{
    for (item in value)
        if (!item.isNullOrBlank()) this.append(item)
    return this
}

/**
 * Appends all non null and non blank arguments to the given StringBuilder.
 */
fun StringBuilder.appendIfNotBlank(vararg value: CharSequence?): StringBuilder
{
    for (item in value)
        if (!item.isNullOrBlank()) this.append(item)
    return this
}

/**
 * Appends all non null and non blank arguments to the given StringBuilder.
 */
fun StringBuilder.appendIfNotBlank(vararg value: String?): StringBuilder
{
    for (item in value)
        if (!item.isNullOrBlank()) this.append(item)
    return this
}

/**
 * Appends all non null and non empty arguments to the given [Appendable].
 */
fun <T : Appendable> T.appendIfNotEmpty(vararg value: CharSequence?): T
{
    for (item in value)
        if (!item.isNullOrEmpty()) this.append(item)
    return this
}

/**
 * Appends all non null and non empty arguments to the given StringBuilder.
 */
fun StringBuilder.appendIfNotEmpty(vararg value: CharSequence?): StringBuilder
{
    for (item in value)
        if (!item.isNullOrEmpty()) this.append(item)
    return this
}

/**
 * Appends all non null and non empty arguments to the given StringBuilder.
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
