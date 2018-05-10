/*
 *  This file is part of MonikaBot.
 *
 *  Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 *  MonikaBot is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MonikaBot is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MonikaBot.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.derppening.monikabot.util.helpers

import java.time.Duration
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

object ChronoHelper {
    /**
     * Formats a duration.
     */
    fun Duration.formatDuration(): String =
            (if (toDays() > 0) "${toDays()}d " else "") +
                    (if (toHours() % 24 > 0) "${toHours() % 24}h " else "") +
                    (if (toMinutes() % 60 > 0) "${toMinutes() % 60}m " else "") +
                    "${seconds % 60}s"

    /**
     * Rounds a duration to the smallest time unit, from Seconds to Days.
     */
    fun Duration.toNearestChronoDay(): String =
            when {
                toDays() > 0 -> "${toDays()}d"
                toHours() > 0 -> "${toHours()}h"
                toMinutes() > 0 -> "${toMinutes()}m"
                else -> "${seconds}s"
            }

    /**
     * Rounds a duration to the smallest time unit, from Days to (approximated) Years.
     */
    fun Duration.toNearestChronoYear(): String =
            when {
                toDays() > 365 -> "${toDays() / 365} years"
                toDays() > 30 -> "${toDays() / 30} months"
                else -> "${toDays()} days"
            }

    /**
     * Formats an integer into "00" format.
     */
    fun formatTimeElement(int: Int): String {
        return int.toString().padStart(2, '0')
    }

    /**
     * Formats a date.
     */
    val dateTimeFormatter = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withLocale(Locale.ENGLISH)
            .withZone(ZoneId.of("UTC"))
}