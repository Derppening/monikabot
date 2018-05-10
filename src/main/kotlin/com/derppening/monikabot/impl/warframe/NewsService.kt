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

package com.derppening.monikabot.impl.warframe

import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.impl.WarframeService.worldState
import com.derppening.monikabot.models.warframe.worldstate.WorldState
import com.derppening.monikabot.util.helpers.ChronoHelper.toNearestChronoDay
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.util.EmbedBuilder
import java.time.Duration
import java.time.Instant
import java.util.*

object NewsService : ILogger {
    fun getNewsEmbed(): EmbedObject {
        return worldState.events.toEmbed()
    }

    fun List<WorldState.Event>.toEmbed(): EmbedObject {
        return EmbedBuilder().apply {
            withTitle("Warframe News")

            this@toEmbed.associate {
                it.date.date.numberLong to (it.messages.find { it.languageCode == Locale.ENGLISH }?.message ?: "")
            }.entries.sortedBy {
                it.key
            }.reversed().forEach { (k, v) ->
                val diff = Duration.between(k, Instant.now())
                val diffString = diff.toNearestChronoDay()
                if (v.isNotBlank()) {
                    appendDesc("\n[$diffString] $v")
                }
            }

            withTimestamp(Instant.now())
        }.build()
    }
}