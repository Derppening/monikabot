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

import com.derppening.monikabot.core.Core
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.impl.WarframeService.worldState
import com.derppening.monikabot.models.warframe.worldstate.WorldState
import com.derppening.monikabot.util.helpers.ChronoHelper.formatDuration
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.util.EmbedBuilder
import java.time.Duration
import java.time.Instant

object BaroService : ILogger {
    fun isBaroInWorldState(): Boolean = worldState.voidTraders.isNotEmpty()

    fun getBaro(): WorldState.VoidTrader {
        if (worldState.voidTraders.size > 1) {
            fix("worldState[\"voidTraders\"] has more than 1 entry!", Core.getMethodName())
        }

        return worldState.voidTraders.first()
    }

    fun WorldState.VoidTrader.toEmbed(): EmbedObject {
        return EmbedBuilder().apply {
            withTitle("Baro Ki'Teer Information")

            if (manifest.isEmpty()) {
                val nextTimeDuration = Duration.between(Instant.now(), activation.date.numberLong)
                appendField("Time to Next Appearance", nextTimeDuration.formatDuration(), true)
                appendField("Relay", WorldState.getSolNode(node).value, true)
            } else {
                val expiryTimeDuration = Duration.between(Instant.now(), expiry.date.numberLong)
                appendField("Time Left", expiryTimeDuration.formatDuration(), false)
                manifest.forEach {
                    val item = WorldState.getLanguageFromAsset(it.itemType).let { fmt ->
                        if (fmt.isEmpty()) {
                            it.itemType
                        } else {
                            fmt
                        }
                    }
                    val ducats = it.primePrice
                    val credits = it.regularPrice
                    appendField(item, "$ducats Ducats - $credits Credits", true)
                }
            }

            withTimestamp(Instant.now())
        }.build()
    }
}
