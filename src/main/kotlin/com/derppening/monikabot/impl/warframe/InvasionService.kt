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
import com.derppening.monikabot.models.warframe.Manifest
import com.derppening.monikabot.models.warframe.worldstate.WorldState
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.NumericHelper.clamp
import com.derppening.monikabot.util.helpers.NumericHelper.formatReal
import com.derppening.monikabot.util.helpers.formatDuration
import sx.blah.discord.api.internal.json.objects.EmbedObject
import java.time.Duration
import java.time.Instant

object InvasionService : ILogger {
    fun getInvasionEmbeds(): List<EmbedObject> {
        return worldState.invasions.filterNot {
            it.completed
        }.map {
            it.toEmbed()
        }
    }

    fun WorldState.Invasion.toEmbed(): EmbedObject {
        return buildEmbed {
            val defenderFaction = WorldState.getFactionString(attackerMissionInfo.faction)
            val attackerFaction = WorldState.getFactionString(defenderMissionInfo.faction)
            @Suppress("DIVISION_BY_ZERO")
            val percentageDouble = (count * 100.0 / goal)
            val percentage = formatReal(percentageDouble)
            val percentageText = "${percentage.dropWhile { it == '-' }}% ${if (percentageDouble < 0) attackerFaction else defenderFaction}"

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

    fun getInvasionAlertEmbed(): EmbedObject? {
        return worldState.goals.filter {
            it.fomorian
        }.also {
            if (it.size > 1) {
                fix("worldState[\"goals\"].filter { it.fomorian } has more than 1 entry!", Core.getMethodName())
            }
        }.map {
            it.toEmbed()
        }.firstOrNull()
    }

    fun WorldState.Goal.toEmbed(): EmbedObject {
        return buildEmbed {
            WorldState.getLanguageFromAsset(missionKeyName).let {
                when {
                    it.isNotBlank() -> it
                    missionKeyName.isNotBlank() -> missionKeyName
                    else -> ""
                }
            }.also { if (it.isNotBlank()) withDesc(it) }
            when (faction) {
                "FC_CORPUS" -> "Razorback Armada"
                "FC_GRINEER" -> "Balor Fomorian"
                else -> throw IllegalStateException("Unable to determine invasion boss")
            }.also { withTitle(it) }

            if (reward.items.isNotEmpty()) {
                appendField("Item Rewards", reward.items.joinToString("\n") {
                    if (WorldState.getLanguageFromAsset(it).isNotBlank()) {
                        WorldState.getLanguageFromAsset(it)
                    } else {
                        it
                    }
                }, false)
                if (reward.items.size == 1) {
                    withImage(Manifest.getImageLinkFromAssetLocation(reward.items[0]))
                }
            }

            appendField("Health", formatReal(healthPct, isPercent = true), true)
            appendField("Time Remaining", Duration.between(Instant.now(), expiry.date.numberLong).formatDuration(), true)

            withTimestamp(Instant.now())
        }.build()
    }

    fun getInvasionTimerEmbed(): EmbedObject {
        return buildEmbed {
            withTitle("Invasions - Construction Status")

            val fomorianPercent = clamp(worldState.projectPct[0], 0.0, 100.0, compareBy { it })
            val razorbackPercent = clamp(worldState.projectPct[1], 0.0, 100.0, compareBy { it })
            appendField("Grineer - Fomorian", "${formatReal(fomorianPercent)}%", true)
            appendField("Corpus - Razorback", "${formatReal(razorbackPercent)}%", true)

            withTimestamp(Instant.now())
        }.build()
    }
}