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
import core.BuilderHelper.buildEmbed
import core.BuilderHelper.insertSeparator
import core.ILogger
import core.Parser
import models.warframe.worldstate.WorldState
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Invasion : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        when {
            args.isEmpty() -> getInvasionData(event)
            "timer".startsWith(args[0]) -> getInvasionTimer(event)
            else -> help(event, false)
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-invasion`")
            withDesc("Displays the invasion progress in Warframe.")
            insertSeparator()
            appendField("Usage", "```warframe invasion [timer]```", false)
            appendField("`timer`", "If appended, show the construction progress for Balor Fomorian and Razorback.", false)

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
     * Retrieves and outputs the list of current invasions.
     */
    private fun getInvasionData(event: MessageReceivedEvent) {
        val invasions = Warframe.worldState.invasions.filterNot { it.completed }

        invasions.forEach {
            buildEmbed(event.channel) {
                val defenderFaction = WorldState.getFactionString(it.attackerMissionInfo.faction)
                val attackerFaction = WorldState.getFactionString(it.defenderMissionInfo.faction)
                @Suppress("DIVISION_BY_ZERO")
                val percentageDouble = (it.count * 100.0 / it.goal)
                val percentage = formatReal(percentageDouble)
                val percentageText = "${percentage.dropWhile { it == '-' }} ${if (percentageDouble < 0) attackerFaction else defenderFaction}"

                withAuthorName("$attackerFaction vs $defenderFaction")
                withTitle("Invasion in ${WorldState.getSolNode(it.node).value}")

                appendField("Percentage", percentageText, false)

                val attackerRewards = it.attackerReward.joinToString("\n") {
                    it.countedItems.joinToString("\n") {
                        "${it.itemCount}x ${WorldState.getLanguageFromAsset(it.itemType)}"
                    }
                }
                val defenderRewards = it.defenderReward.countedItems.joinToString("\n") {
                    "${it.itemCount}x ${WorldState.getLanguageFromAsset(it.itemType)}"
                }

                if (attackerRewards.isNotBlank()) {
                    appendField("Attacker Rewards", attackerRewards, true)
                }
                if (defenderRewards.isNotBlank()) {
                    appendField("Defender Rewards", defenderRewards, true)
                }

                withTimestamp(Warframe.worldState.time)
            }

            Thread.sleep(100)
        }
    }

    /**
     * Outputs the current build progress of Balor Fomorian/Razorback.
     */
    private fun getInvasionTimer(event: MessageReceivedEvent) {
        buildEmbed(event.channel) {
            withTitle("Invasions - Construction Status")
            appendField("Grineer - Fomorian", formatReal(Warframe.worldState.projectPct[0]), true)
            appendField("Corpus - Razorback", formatReal(Warframe.worldState.projectPct[1]), true)
            withTimestamp(Warframe.worldState.time)
        }
    }

    /**
     * Reformats a real number to 2 decimal places.
     */
    private fun formatReal(double: Double): String {
        return "%.2f%%".format(double)
    }
}