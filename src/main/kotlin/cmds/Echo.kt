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

package cmds

import core.BuilderHelper.buildEmbed
import core.BuilderHelper.buildMessage
import core.IChannelLogger
import core.Parser
import insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

object Echo : IBase, IChannelLogger {
    override fun handler(event: MessageReceivedEvent): Parser.HandleState {
        val args = getArgumentList(event.message.content)
        if (args.isEmpty()) {
            help(event, false)
            return Parser.HandleState.HANDLED
        }

        val channel = event.channel
        val message = getArgumentList(event.message.content)

        if (!event.channel.isPrivate) {
            try {
                event.message.delete()
            } catch (e: DiscordException) {
                log(IChannelLogger.LogLevel.ERROR, "Cannot delete \"$message\"") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
                e.printStackTrace()
            }
        }

        try {
            buildMessage(channel) {
                withContent(message.joinToString(" "))
            }
        } catch (e: DiscordException) {
            log(IChannelLogger.LogLevel.ERROR, "\"$message\" not handled") {
                message { event.message }
                author { event.author }
                channel { event.channel }
                info { e.errorMessage }
            }
            e.printStackTrace()
        }

        return Parser.HandleState.HANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        try {
            buildEmbed(event.channel) {
                withTitle("Help Text for `echo`")
                withDesc("Echo: Repeats a string, and erases it from the current channel.")
                insertSeparator()
                appendField("Usage", "```echo [string]```", false)
                appendField("`[string]`", "String to repeat.", false)
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
