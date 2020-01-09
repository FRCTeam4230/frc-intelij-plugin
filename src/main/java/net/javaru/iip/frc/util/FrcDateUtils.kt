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
@file:Suppress("unused")

package net.javaru.iip.frc.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

@JvmOverloads
fun LocalDate.asDate(zoneId: ZoneId? = ZoneId.systemDefault()): Date
{
    return Date.from(this.atStartOfDay().atZone(zoneId).toInstant())
}

@JvmOverloads
fun LocalDateTime.asDate(zoneId: ZoneId? = ZoneId.systemDefault()): Date
{
    return Date.from(this.atZone(zoneId).toInstant())
}

@JvmOverloads
fun Date.asLocalDate(zoneId: ZoneId? = ZoneId.systemDefault()): LocalDate
{
    return Instant.ofEpochMilli(this.time).atZone(zoneId).toLocalDate()
}

@JvmOverloads
fun Date.asLocalDateTime(zoneId: ZoneId? = ZoneId.systemDefault()): LocalDateTime
{
    return Instant.ofEpochMilli(this.time).atZone(zoneId).toLocalDateTime()
}