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

object Syndicate : IBase, ILogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content).drop(1)

        when {
            args.isNotEmpty() -> {
                getMissionsForSyndicate(event)
            }
            else -> {
                help(event, false)
            }
        }


        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `warframe-syndicate`")
            withDesc("Displays missions of a given syndicate.")
            insertSeparator()
            appendField("Usage", "```warframe syndicate [syndicate]```", false)
            appendField("`[syndicate]`", "The syndicate to show missions for.", false)

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
     * Retrieves and outputs a list of missions for a given syndicate.
     */
    private fun getMissionsForSyndicate(event: MessageReceivedEvent) {
        val args = getArgumentList(event.message.content).drop(1).joinToString(" ")

        val matches = Warframe.worldState.syndicateMissions.filter {
            WorldState.getSyndicateName(it.tag).contains(args, true)
        }
        if (matches.size > 1) {
            buildMessage(event.channel) {
                withContent("The given syndicate name matches more than one syndicate!")
                appendContent("\nYour provided syndicate name matches: \n" +
                        matches.joinToString("\n") { "- ${WorldState.getSyndicateName(it.tag)}" })
            }
            return
        } else if (matches.isEmpty()) {
            buildMessage(event.channel) {
                withContent("The given syndicate name doesn't match any syndicate!")
            }
            return
        }

        val syndicate = matches.first()
        buildEmbed(event.channel) {
            val name = WorldState.getSyndicateName(syndicate.tag)
            val timeToExpiry = Duration.between(Instant.now(), syndicate.expiry.date.numberLong).formatDuration()

            withTitle(name)
            appendField("Expires in", timeToExpiry, false)
            if (syndicate.nodes.isNotEmpty()) {
                appendField("Nodes", syndicate.nodes.joinToString("\n") { "- ${WorldState.getSolNode(it).value}" }, false)
            }

            withTimestamp(Instant.now())
        }
    }
}