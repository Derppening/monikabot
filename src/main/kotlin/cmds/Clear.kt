/**
 * This file is part of MonikaBot.
 *
 * Copyright (C) 2018 Derppening <david.18.19.21@gmail.com>
 *
 * RTLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RTLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RTLib.  If not, see <http://www.gnu.org/licenses/>.
 */

package cmds

import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.IChannelLogger
import core.Parser
import core.PersistentMessage
import insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

object Clear : IBase, IChannelLogger {
    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)

        val allFlag = args.any { it.matches(Regex("-{0,2}all")) }

        if (event.channel.isPrivate) {
            buildMessage(event.channel) {
                withContent("I can't delete clear messages in private channels!")
            }

            log(IChannelLogger.LogLevel.ERROR, "Cannot bulk delete messages") {
                author { event.author }
                channel { event.channel }
                info { "In a private channel" }
            }
        } else {
            val messages = if (allFlag) event.channel.fullMessageHistory else event.channel.messageHistory
            event.channel.bulkDelete(messages.filterNot { it.longID == PersistentMessage.messageId })
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildEmbed(event.channel) {
                withTitle("Help Text for `clear`")
                withDesc("Clears all channel messages that are younger than 14 days.")
                appendDesc("\nThis command does not work in private channels.")
                insertSeparator()
                appendField("Usage", "```clear [--all]```", false)
                appendField("`--all`", "Retrieves all messages from the channel, not only ones which " +
                        "are locally cached.", false)
            }
        } catch (e: DiscordException) {
            log(IChannelLogger.LogLevel.ERROR, "Cannot display help text") {
                author { event.author }
                channel { event.channel }
                info { e.errorMessage }
            }
            e.printStackTrace()
        }
    }
}
