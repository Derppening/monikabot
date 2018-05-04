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
import com.derppening.monikabot.util.NumericHelper.formatReal
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.util.EmbedBuilder
import java.time.Instant

object InvasionService : ILogger {
    fun getInvasionEmbeds(): List<EmbedObject> {
        return worldState.invasions.filterNot {
            it.completed
        }.map {
            it.toEmbed()
        }
    }

    fun getInvasionTimerEmbed(): EmbedObject {
        return EmbedBuilder().apply {
            withTitle("Invasions - Construction Status")

            appendField("Grineer - Fomorian", formatReal(worldState.projectPct[0]), true)
            appendField("Corpus - Razorback", formatReal(worldState.projectPct[1]), true)

            withTimestamp(Instant.now())
        }.build()
    }

    fun WorldState.Invasion.toEmbed(): EmbedObject {
        return EmbedBuilder().apply {
            val defenderFaction = WorldState.getFactionString(attackerMissionInfo.faction)
            val attackerFaction = WorldState.getFactionString(defenderMissionInfo.faction)
            @Suppress("DIVISION_BY_ZERO")
            val percentageDouble = (count * 100.0 / goal)
            val percentage = formatReal(percentageDouble)
            val percentageText = "${percentage.dropWhile { it == '-' }} ${if (percentageDouble < 0) attackerFaction else defenderFaction}"

            withAuthorName("$attackerFaction vs $defenderFaction")
            withTitle("Invasion in ${WorldState.getSolNode(node).value}")

            appendField("Percentage", percentageText, false)

            val attackerRewards = attackerReward.joinToString("\n") {
                it.countedItems.joinToString("\n") {
                    "${it.itemCount}x ${WorldState.getLanguageFromAsset(it.itemType)}"
                }
            }
            val defenderRewards = defenderReward.countedItems.joinToString("\n") {
                "${it.itemCount}x ${WorldState.getLanguageFromAsset(it.itemType)}"
            }

            if (attackerRewards.isNotBlank()) {
                appendField("Attacker Rewards", attackerRewards, true)
            }
            if (defenderRewards.isNotBlank()) {
                appendField("Defender Rewards", defenderRewards, true)
            }

            withTimestamp(Instant.now())
        }.build()
    }
}