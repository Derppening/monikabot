/**
 * This file is part of MonikaBot.
 *
 * Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 * MonikaBot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MonikaBot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MonikaBot.  If not, see <http://www.gnu.org/licenses/>.
 */

package cmds.warframe

import cmds.IBase
import cmds.Warframe
import cmds.Warframe.formatDuration
import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.BuilderHelper.insertSeparator
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.time.Duration
import java.time.Instant

object Alert : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        try {
            when {
                args.any { it.matches(Regex("-{0,2}help")) } -> help(event, false)
                args.isEmpty() -> {
                    getGoals(event)
                    getAlerts(event)
                }
                "alert".startsWith(args[0]) -> getAlerts(event)
                "special".startsWith(args[0]) -> getGoals(event)
                else -> {
                    help(event, false)
                }
            }
        } catch (e: Exception) {
            buildMessage(event.channel) {
                withContent("Warframe is currently updating its information. Please be patient!")
            }
            e.printStackTrace()

            log(ILogger.LogLevel.ERROR, e.message ?: "Unknown Exception")
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-alert`")
            withDesc("Displays all currently ongoing alerts.")
            insertSeparator()
            appendField("Usage", "```warframe alert [--alert|--special]```", false)
            appendField("`--alert`", "Only show normal mission alerts.", false)
            appendField("`--special`", "Only show special alerts.", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }

    /**
     * Retrieves and outputs a list of alerts.
     */
    private fun getAlerts(event: MessageReceivedEvent) {
        val alerts = Warframe.worldState.alerts

        if (alerts.isEmpty()) {
            buildMessage(event.channel) {
                withContent("There are currently no alerts!")
            }
        }

        alerts.forEach {
            buildEmbed(event.channel) {
                val alert = it.missionInfo
                val archwing = when {
                    alert.isSharkwingMission -> "[Sharkwing]"
                    alert.archwingRequired -> "[Archwing]"
                    else -> ""
                }
                val nightmare = if (alert.nightmare) "[Nightmare]" else ""
                val titleText = "$archwing$nightmare ${WorldState.getMissionType(alert.missionType)} in ${WorldState.getSolNode(alert.location).value}"

                if (alert.descText.isNotBlank()) {
                    withAuthorName(WorldState.getLanguageFromAsset(alert.descText))
                }
                withTitle(titleText)
                appendField("Faction", WorldState.getFactionString(alert.faction), true)
                appendField("Enemy Level", "${alert.minEnemyLevel}-${alert.maxEnemyLevel}", true)
                appendField("Mission Credits", alert.missionReward.credits.toString(), true)
                if (alert.missionReward.items.isNotEmpty()) {
                    appendField("Item Rewards", alert.missionReward.items.joinToString("\n") {
                        WorldState.getLanguageFromAsset(it)
                    }, false)
                    withImage(Manifest.getImageLinkFromAssetLocation(alert.missionReward.items[0]))
                }
                if (alert.missionReward.countedItems.isNotEmpty()) {
                    appendField("Item Rewards", alert.missionReward.countedItems.joinToString("\n") {
                        "${it.itemCount}x ${WorldState.getLanguageFromAsset(it.itemType)}"
                    }, false)
                    withImage(Manifest.getImageLinkFromAssetLocation(alert.missionReward.countedItems[0].itemType))
                }
                appendField("Time Remaining", Duration.between(Instant.now(), it.expiry.date.numberLong).formatDuration(), true)

                withTimestamp(Instant.now())
            }
        }
    }

    /**
     * Retrieves and outputs a list of special alerts ("goals").
     */
    private fun getGoals(event: MessageReceivedEvent) {
        val goals = Warframe.worldState.goals.filterNot { it.tag == "GhoulEmergence" }

        if (goals.isEmpty()) {
            buildMessage(event.channel) {
                withContent("There are currently no special alerts!")
            }
        }

        goals.forEach { goal ->
            buildEmbed(event.channel) {
                WorldState.getLanguageFromAsset(goal.missionKeyName).let {
                    when {
                        it.isNotBlank() -> it
                        goal.missionKeyName.isNotBlank() -> goal.missionKeyName
                        else -> ""
                    }
                }.also { if (it.isNotBlank()) withDesc(it) }
                WorldState.getLanguageFromAsset(goal.desc).let {
                    when {
                        it.isNotBlank() -> it
                        goal.desc.isNotBlank() -> goal.desc
                        else -> ""
                    }
                }.also { if (it.isNotBlank()) withTitle("Special: $it") }

                if (goal.reward.items.isNotEmpty()) {
                    appendField("Item Rewards", goal.reward.items.joinToString("\n") {
                        if (WorldState.getLanguageFromAsset(it).isNotBlank()) {
                            WorldState.getLanguageFromAsset(it)
                        } else {
                            it
                        }
                    }, false)
                    withImage(Manifest.getImageLinkFromAssetLocation(goal.reward.items[0]))
                }

                appendField("Time Remaining", Duration.between(Instant.now(), goal.expiry.date.numberLong).formatDuration(), true)

                withTimestamp(Instant.now())
            }
        }
    }
}