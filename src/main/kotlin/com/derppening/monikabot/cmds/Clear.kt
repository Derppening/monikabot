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

package com.derppening.monikabot.cmds

import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.core.Parser
import com.derppening.monikabot.util.BuilderHelper.buildEmbed
import com.derppening.monikabot.util.BuilderHelper.buildMessage
import com.derppening.monikabot.util.BuilderHelper.insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Clear : IBase, ILogger {
    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)

        val allFlag = args.any { it.matches(Regex("-{0,2}all")) }

        if (event.channel.isPrivate) {
            buildMessage(event.channel) {
                withContent("I can't delete clear messages in private channels!")
            }

            log(ILogger.LogLevel.ERROR, "Cannot bulk delete messages") {
                author { event.author }
                channel { event.channel }
                info { "In a private channel" }
            }
        } else {
            val messages = if (allFlag) event.channel.fullMessageHistory else event.channel.messageHistory
            event.channel.bulkDelete(messages)
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `clear`")
            withDesc("Clears all channel messages that are younger than 14 days.")
            appendDesc("\nThis command does not work in private channels.")
            insertSeparator()
            appendField("Usage", "```clear [--all]```", false)
            appendField("`--all`", "Retrieves all messages from the channel, not only ones which " +
                    "are locally cached.", false)

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
