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
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.formatDuration
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.handle.impl.obj.Embed
import sx.blah.discord.handle.obj.IEmbed
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter

object BaroService : ILogger {
    fun isBaroInWorldState(): Boolean = worldState.voidTraders.isNotEmpty()

    fun getBaroEmbed(): List<EmbedObject> {
        if (worldState.voidTraders.size > 1) {
            fix("worldState[\"voidTraders\"] has more than 1 entry!", Core.getMethodName())
        }

        return worldState.voidTraders.first().toEmbed()
    }

    fun WorldState.VoidTrader.toEmbed(): List<EmbedObject> {
        return List<EmbedObject>(manifest.takeIf { it.isNotEmpty() }?.size?.div(25)?.plus(1) ?: 1) {
            when (it) {
                0 -> {
                    buildEmbed {
                        withTitle("Baro Ki'Teer Information")

                        if (manifest.isEmpty()) {
                            val nextTimeDuration = Duration.between(Instant.now(), activation.date.numberLong)
                            appendField("Time to Next Appearance", nextTimeDuration.formatDuration(), true)
                            appendField("Relay", WorldState.getSolNode(node).value, true)
                        } else {
                            val expiryTimeDuration = Duration.between(Instant.now(), expiry.date.numberLong)
                            appendField("Time Left", expiryTimeDuration.formatDuration(), false)
                            manifest.take(24).forEach { item ->
                                appendField(item.toEmbedField())
                            }
                        }
                    }.build()
                }
                manifest.takeIf { it.isNotEmpty() }?.size?.div(25) -> {
                    buildEmbed {
                        manifest.drop(24 + (it - 1) * 25).take(25).forEach { item ->
                            appendField(item.toEmbedField())
                        }

                        withFooterText("Leaves at ${DateTimeFormatter.ISO_INSTANT.format(expiry.date.numberLong)}")
                    }.build()
                }
                else -> {
                    buildEmbed {
                        manifest.drop(24 + (it - 1) * 25).take(25).forEach { item ->
                            appendField(item.toEmbedField())
                        }
                    }.build()
                }
            }
        }
    }

    fun WorldState.VoidTrader.Item.toEmbedField(): IEmbed.IEmbedField {
        val item =
            WorldState.getLanguageFromAsset(this.itemType).takeIf { it.isNotEmpty() } ?: this.itemType
        val ducats = this.primePrice
        val credits = this.regularPrice

        return Embed.EmbedField(item, "$ducats Ducats - $credits Credits", true)
    }
}
