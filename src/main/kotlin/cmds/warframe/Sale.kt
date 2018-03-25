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
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.time.Duration
import java.time.Instant

object Sale : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)
        if (args.isNotEmpty() && args.any { it.matches(Regex("-{0,2}help")) }) {
            help(event, false)

            return Parser.HandleState.HANDLED
        }

        event.channel.toggleTypingStatus()
        val saleItems = Warframe.worldState.flashSales
        buildEmbed(event.channel) {
            withTitle("Sales")

            saleItems.filterNot { it.typeName.contains("PrimeAccess") }.sortedBy { it.bannerIndex }.forEach {
                val itemName = WorldState.getLanguageFromAsset(it.typeName).let { item ->
                    if (item.isBlank()) {
                        it.typeName
                    } else {
                        item
                    }
                }
                val appendStr = when {
                    it.featured && it.popular -> "Featured/Popular: "
                    it.featured -> "Featured: "
                    it.popular -> "Popular: "
                    else -> ""
                }
                val valueStr = when {
                    it.regularOverride != 0 && it.premiumOverride != 0 -> "${it.premiumOverride} Platinum + ${it.regularOverride} Credits"
                    it.premiumOverride != 0 -> "${it.premiumOverride} Platinum"
                    it.regularOverride != 0 -> "${it.regularOverride} Credits"
                    else -> ""
                }

                if (valueStr.isNotBlank()) {
                    appendField("$appendStr$itemName", valueStr, false)
                }
            }

            withTimestamp(Instant.now())
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-sale`")
            withDesc("Displays the ongoing market sales.")
            insertSeparator()
            appendField("Usage", "```warframe sale```", false)

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
     * Formats a duration.
     */
    private fun formatTimeDuration(duration: Duration): String {
        return (if (duration.toDays() > 0) "${duration.toDays()}d " else "") +
                (if (duration.toHours() % 24 > 0) "${duration.toHours() % 24}h " else "") +
                (if (duration.toMinutes() % 60 > 0) "${duration.toMinutes() % 60}m " else "") +
                "${duration.seconds % 60}s"
    }
}
