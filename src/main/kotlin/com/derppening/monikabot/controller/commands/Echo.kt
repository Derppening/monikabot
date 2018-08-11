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

package com.derppening.monikabot.controller.commands

import com.derppening.monikabot.controller.CommandInterpreter
import com.derppening.monikabot.core.ILogger
import com.derppening.monikabot.impl.EchoService
import com.derppening.monikabot.impl.EchoService.toGuildChannel
import com.derppening.monikabot.impl.EchoService.toPrivateChannel
import com.derppening.monikabot.util.helpers.EmbedHelper.buildEmbed
import com.derppening.monikabot.util.helpers.HelpTextBuilder.buildHelpText
import com.derppening.monikabot.util.helpers.MessageHelper.buildMessage
import com.derppening.monikabot.util.helpers.insertSeparator
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent

object Echo : IBase, ILogger {
    override fun cmdName(): String = "echo"

    override fun handler(event: MessageReceivedEvent): CommandInterpreter.HandleState {
        val args = if (event.channel.isPrivate) {
            getArgumentList(event.message.content)
        } else {
            getArgumentList(event.message.formattedContent, event.channel.guild)
        }
        if (args.isEmpty()) {
            help(event, false)
            return CommandInterpreter.HandleState.HANDLED
        }

        if (args[0] == "-d" || args[0] == "--destination") {
            if (args.size == 1) {
                buildMessage(event.channel) {
                    content {
                        withContent("Please specify a destination and a message!")
                    }
                }

                return CommandInterpreter.HandleState.HANDLED
            }

            return if (args[1].any { it == '/' }) {
                messageToGuildChannel(args, event)
                CommandInterpreter.HandleState.HANDLED
            } else {
                CommandInterpreter.HandleState.PERMISSION_DENIED
            }
        }

        val message = getArgumentList(event.message.content).joinToString(" ")

        buildMessage(event.channel) {
            content {
                withContent(message)
            }

            onError {
                discordException { e ->
                    logToChannel(ILogger.LogLevel.ERROR, "Cannot echo message \"$message\"!") {
                        message { event.message }
                        author { event.author }
                        channel { event.channel }
                        info { e.errorMessage }
                        stackTrace { e.stackTrace }
                    }
                }
            }
        }

        return CommandInterpreter.HandleState.HANDLED
    }

    override fun handlerSu(event: MessageReceivedEvent): CommandInterpreter.HandleState {
        val args = if (event.channel.isPrivate) {
            getArgumentList(event.message.content)
        } else {
            getArgumentList(event.message.formattedContent, event.channel.guild)
        }
        if (args.isEmpty()) {
            help(event, true)
            return CommandInterpreter.HandleState.HANDLED
        }

        if (args[0] == "-d" || args[0] == "--destination") {
            return when {
                args.size == 1 -> {
                    buildMessage(event.channel) {
                        content {
                            withContent("Please specify a destination and a message!")
                        }
                    }
                    CommandInterpreter.HandleState.HANDLED
                }
                args[1].any { it == '/' } -> // delegate to normal handler
                    CommandInterpreter.HandleState.UNHANDLED
                else -> {
                    messageToPrivateChannel(args, event)
                    CommandInterpreter.HandleState.HANDLED
                }
            }
        }

        return CommandInterpreter.HandleState.UNHANDLED
    }

    private fun messageToPrivateChannel(args: List<String>, event: MessageReceivedEvent) {
        val result = toPrivateChannel(args)
        when (result) {
            is EchoService.Result.Failure -> {
                buildMessage(event.channel) {
                    content {
                        withContent(result.message)
                    }
                }
            }
            else -> {
            }
        }
    }

    private fun messageToGuildChannel(args: List<String>, event: MessageReceivedEvent) {
        val result = toGuildChannel(args, event)
        when (result) {
            is EchoService.Result.Failure -> {
                buildMessage(event.channel) {
                    content {
                        withContent(result.message)
                    }
                }
            }
            else -> {
            }
        }
    }

    override fun help(event: MessageReceivedEvent, isSu: Boolean) {
        buildEmbed(event.channel) {
            fields {
                withTitle("Help Text for `echo`")
                withDesc("Echo: Repeats a string.")
                insertSeparator()
                appendField("Usage", "```echo [string]```", false)
                appendField("`[string]`", "String to repeat.", false)

                insertSeparator()
                appendField("Usage", "```echo -d [destination] [string]```", false)

                val destinationText = "Destination of the string. Recognized formats include:" +
                        "\n\t- `/channel`: Sends to `channel` in current server." +
                        if (isSu) {
                            "\n\t- `server/channel`: Sends to `channel` in `server`." +
                                    "\n\t- `username#discriminator`: Sends to user with this Discord Tag."
                        } else ""
                appendField("`[destination]`", destinationText, false)
                appendField("`[string]`", "String to repeat.", false)
            }

            onError {
                discordException { e ->
                    logToChannel(ILogger.LogLevel.ERROR, "Cannot display help text") {
                        author { event.author }
                        channel { event.channel }
                        info { e.errorMessage }
                    }
                }
            }
        }

        buildHelpText(cmdInvocation(), event) {
            description { "Echo: Repeats a string." }

            usage("echo [STRING]") {
                option("STRING") { "String to repeat." }
            }
            usage("echo -d [DESTINATION] [STRING]") {
                option("DESTINATION") {
                    "Destination of the string. Recognized formats include:" +
                            "\n\t- `/channel`: Sends to `channel` in current server." +
                            if (isSu) {
                                "\n\t- `server/channel`: Sends to `channel` in `server`." +
                                        "\n\t- `username#discriminator`: Sends to user with this Discord Tag."
                            } else ""
                }
                option("STRING") { "String to repeat." }
            }
        }
    }
}
