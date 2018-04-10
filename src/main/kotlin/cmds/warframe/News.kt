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

package cmds.warframe

import cmds.IBase
import cmds.Warframe
import cmds.Warframe.toNearestChronoDay
import core.BuilderHelper.buildEmbed
import core.BuilderHelper.insertSeparator
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import java.time.Duration
import java.time.Instant
import java.util.*

object News : IBase {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        buildEmbed(event.channel) {
            withTitle("Warframe News")

            val eventPairs = mutableMapOf<Instant, String>()

            for (eventItem in Warframe.worldState.events) {
                eventPairs[eventItem.date.date.numberLong] = eventItem.messages.find { it.languageCode == Locale.ENGLISH }?.message ?: ""
            }

            val sortedPairs = eventPairs.entries.sortedBy { it.key }.reversed()
            sortedPairs.forEach { (k, v) ->
                val diff = Duration.between(k, Instant.now())
                val diffString = diff.toNearestChronoDay()
                if (v.isNotBlank()) {
                    appendDesc("\n[$diffString] $v")
                }
            }

            withTimestamp(Warframe.worldState.time)
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-news`")
            withDesc("Displays the latest Warframe news.")
            insertSeparator()
            appendField("Usage", "```warframe news```", false)

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }
}