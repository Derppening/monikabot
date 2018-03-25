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
import core.BuilderHelper.insertSeparator
import core.Core
import core.ILogger
import core.Parser
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent
import sx.blah.discord.util.DiscordException

object Echo : IBase, ILogger {
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
                log(ILogger.LogLevel.ERROR, "Cannot delete \"$message\"") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
                e.printStackTrace()
            }
        }

        buildMessage(channel) {
            withContent(message.joinToString(" "))

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "\"$message\" not handled") {
                    message { event.message }
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }


        return Parser.HandleState.HANDLED
    }

    override fun handlerSu(event: MessageReceivedEvent): Parser.HandleState {
        val args = if (event.channel.isPrivate) {
            getArgumentList(event.message.content)
        } else {
            getArgumentList(event.message.formattedContent, event.channel.guild)
        }
        if (args.isEmpty()) {
            help(event, true)
            return Parser.HandleState.HANDLED
        }

        if (args[0] == "-d" || args[0] == "--destination") {
            if (args.size == 1) {
                buildMessage(event.channel) {
                    withContent("Please specify a destination and a message!")
                }

                return Parser.HandleState.HANDLED
            }

            if (args[1].any { it == '/' }) {
                messageToGuildChannel(args, event)
            } else {
                messageToPrivateChannel(args, event)
            }

            return Parser.HandleState.HANDLED
        }

        return Parser.HandleState.UNHANDLED
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            withTitle("Help Text for `echo`")
            withDesc("Echo: Repeats a string.")
            insertSeparator()
            appendField("Usage", "```echo [string]```", false)
            appendField("`[string]`", "String to repeat.", false)

            if (isSu) {
                insertSeparator()
                appendField("Usage", "```echo -d [destination] [string]```", false)
                appendField("`[destination]`", "Destination of the string. Recognized formats include:" +
                        "\n\t- `/channel`: Sends to `channel` in current server." +
                        "\n\t- `server/channel`: Sends to `channel` in `server`." +
                        "\n\t- `username#discriminator`: Sends to user with this Discord Tag.", false)
                appendField("`[string]`", "String to repeat.", false)
            }

            onDiscordError { e ->
                log(ILogger.LogLevel.ERROR, "Cannot display help text") {
                    author { event.author }
                    channel { event.channel }
                    info { e.errorMessage }
                }
            }
        }
    }

    private fun messageToPrivateChannel(args: List<String>, event: MessageReceivedEvent) {
        val username = args[1].dropLastWhile { it != '#' }.dropLastWhile { it == '#' }
        val discriminator = args[1].dropWhile { it != '#' }.dropWhile { it == '#' }

        if (username.isBlank() || discriminator.isBlank()) {
            buildMessage(event.channel) {
                withContent("Please specify a destination!")
            }
        } else if (discriminator.toIntOrNull() == null) {
            buildMessage(event.channel) {
                withContent("The Discord Tag is formatted incorrectly!")
            }
        }

        try {
            val channel = Core.getUserByTag(username, discriminator.toInt())?.orCreatePMChannel
                    ?: error("Cannot find user!")

            val message = args.drop(2).joinToString(" ")
            buildMessage(channel) {
                withContent(message)

                onDiscordError { e ->
                    buildMessage(event.channel) {
                        withContent("I can't deliver the message! Reason: ${e.errorMessage}")
                    }
                }
            }
        } catch (e: Exception) {
            buildMessage(event.channel) {
                withContent("I can't deliver the message! Reason: ${e.message}")
            }
            e.printStackTrace()
        }
    }

    private fun messageToGuildChannel(args: List<String>, event: MessageReceivedEvent) {
        val guildStr = args[1].dropLastWhile { it != '/' }.dropLastWhile { it == '/' }.let {
            if (it.isBlank() && !event.channel.isPrivate) {
                event.channel.guild.name
            } else {
                it
            }
        }
        val channelStr = args[1].dropWhile { it != '/' }.dropWhile { it == '/' || it == '#' }

        if (guildStr.isBlank() || channelStr.isBlank()) {
            buildMessage(event.channel) {
                withContent("Please specify a destination!")
            }

            return
        }

        try {
            val guild = Core.getGuildByName(guildStr) ?: error("Cannot find guild $guildStr")
            val channel = Core.getChannelByName(channelStr, guild) ?: error("Cannot find channel $channelStr")

            val message = args.drop(2).joinToString(" ")
            buildMessage(channel) {
                withContent(message)

                onDiscordError { e ->
                    buildMessage(event.channel) {
                        withContent("I can't deliver the message! Reason: ${e.errorMessage}")
                    }
                }
            }
        } catch (e: Exception) {
            buildMessage(event.channel) {
                withContent("I can't deliver the message! Reason: ${e.message}")
            }
            e.printStackTrace()
        }

        return
    }
}
