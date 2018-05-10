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
import com.derppening.monikabot.models.warframe.Manifest
import com.derppening.monikabot.models.warframe.worldstate.WorldState
import com.derppening.monikabot.util.helpers.ChronoHelper.formatDuration
import sx.blah.discord.api.internal.json.objects.EmbedObject
import sx.blah.discord.util.EmbedBuilder
import java.time.Duration
import java.time.Instant

object AlertService : ILogger {
    /**
     * List of tags to filter out when displaying alerts.
     */
    private val filteredTags = listOf(
            "GhoulEmergence",
            "InfestedPlains",
            "FriendlyFireTacAlert"
    )

    fun getAlertEmbeds(): List<EmbedObject> {
        return worldState.alerts.map {
            it.toEmbed()
        }
    }

    private fun WorldState.Alert.toEmbed(): EmbedObject {
        return EmbedBuilder().apply {
            val detail = missionInfo
            val archwing = when {
                detail.isSharkwingMission -> "[Sharkwing]"
                detail.archwingRequired -> "[Archwing]"
                else -> ""
            }
            val nightmare = if (detail.nightmare) "[Nightmare]" else ""
            val titleText = "$archwing$nightmare ${WorldState.getMissionType(detail.missionType)} in ${WorldState.getSolNode(detail.location).value}"

            if (detail.descText.isNotBlank()) {
                withAuthorName(WorldState.getLanguageFromAsset(detail.descText))
            }
            withTitle(titleText)
            appendField("Faction", WorldState.getFactionString(detail.faction), true)
            appendField("Enemy Level", "${detail.minEnemyLevel}-${detail.maxEnemyLevel}", true)
            appendField("Mission Credits", detail.missionReward.credits.toString(), true)
            if (detail.missionReward.items.isNotEmpty()) {
                appendField("Item Rewards", detail.missionReward.items.joinToString("\n") {
                    WorldState.getLanguageFromAsset(it)
                }, false)
                withImage(Manifest.getImageLinkFromAssetLocation(detail.missionReward.items[0]))
            }
            if (detail.missionReward.countedItems.isNotEmpty()) {
                appendField("Item Rewards", detail.missionReward.countedItems.joinToString("\n") {
                    "${it.itemCount}x ${WorldState.getLanguageFromAsset(it.itemType)}"
                }, false)
                withImage(Manifest.getImageLinkFromAssetLocation(detail.missionReward.countedItems[0].itemType))
            }
            appendField("Time Remaining", Duration.between(Instant.now(), expiry.date.numberLong).formatDuration(), true)

            withTimestamp(Instant.now())
        }.build()
    }

    fun getGoalEmbeds(): List<EmbedObject> {
        return worldState.goals.filterNot { goal ->
            filteredTags.any { it == goal.tag }
        }.map {
            it.toEmbed()
        }
    }

    private fun WorldState.Goal.toEmbed(): EmbedObject {
        return EmbedBuilder().apply {
            WorldState.getLanguageFromAsset(missionKeyName).let {
                when {
                    it.isNotBlank() -> it
                    missionKeyName.isNotBlank() -> missionKeyName
                    else -> ""
                }
            }.also { if (it.isNotBlank()) withDesc(it) }
            WorldState.getLanguageFromAsset(desc).let {
                when {
                    it.isNotBlank() -> it
                    desc.isNotBlank() -> desc
                    else -> ""
                }
            }.also { if (it.isNotBlank()) withTitle("Special: $it") }

            if (reward.items.isNotEmpty()) {
                appendField("Item Rewards", reward.items.joinToString("\n") {
                    if (WorldState.getLanguageFromAsset(it).isNotBlank()) {
                        WorldState.getLanguageFromAsset(it)
                    } else {
                        it
                    }
                }, false)
                withImage(Manifest.getImageLinkFromAssetLocation(reward.items[0]))
            }

            appendField("Time Remaining", Duration.between(Instant.now(), expiry.date.numberLong).formatDuration(), true)

            withTimestamp(Instant.now())
        }.build()
    }
}